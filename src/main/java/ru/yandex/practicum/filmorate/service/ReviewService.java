package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.ExcessiveActionException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;


@Service
@Slf4j
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Autowired
    public ReviewService(final ReviewDbStorage reviewStorage, final UserDbStorage userStorage,
                         final FilmDbStorage filmStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public ReviewDto create(final NewReviewRequest newReviewRequest) {
        validationUserId(newReviewRequest.getUserId());
        validationFilmId(newReviewRequest.getFilmId());

        Review review = ReviewMapper.mapToReview(newReviewRequest);
        review = reviewStorage.create(review);

        return ReviewMapper.mapToReviewDto(review);
    }

    public ReviewDto getReviewById(final Long id) {
        return reviewStorage.getReviewById(id)
                .map(ReviewMapper::mapToReviewDto)
                .orElseThrow(() -> new NotFoundException("Отзыва с id = " + id + " не существует"));
    }

    public void deleteReviewById(final Long id) {
        if (!reviewStorage.delete(id)) {
            throw new NotFoundException("Отзыва с id = " + id + " не существует");
        }
    }

    public ReviewDto update(final UpdateReviewRequest request) {
        if (request.getReviewId() == null) {
            throw new ConditionsNotMetException("Вы не передали id фильма");
        }

        Review updateReview = reviewStorage.getReviewById(request.getReviewId())
                .map(review -> ReviewMapper.updateReviewFields(review, request))
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));

        updateReview = reviewStorage.update(updateReview);
        return getReviewById(updateReview.getReviewId());
    }

    public Collection<ReviewDto> getReviewByIdFilms(final Long filmId, final Integer count) {
        if (filmId == null) {
            return reviewStorage.getAllReviews(count)
                    .stream()
                    .map(ReviewMapper::mapToReviewDto)
                    .toList();
        }
        validationFilmId(filmId);
        return reviewStorage.getAllReviewsByIdFilms(filmId, count)
                .stream()
                .map(ReviewMapper::mapToReviewDto)
                .toList();
    }

    public void addLikeReview(final Long id, final Long userId) {
        validationUserId(userId);
        ReviewDto reviewDto = getReviewById(id);
        if (reviewStorage.existsLikeOnReview(id, userId, true)) {
            log.debug("Лайк от пользователя с id = {} к отзыву с id = {} уже добавлен", userId, id);
            throw new ExcessiveActionException("Лайк от пользователя с id = " + userId + " уже добавлен к отзыву с " +
                    "id = " + id);
        } else if (reviewStorage.existsLikeOnReview(id, userId, false)) {
            reviewStorage.replaceLikeAndDislike(id, userId, true);
            reviewStorage.updateUseful(reviewDto.getUseful() + 2, id);
        } else {
            log.debug("Лайк от пользователя с id = {} к отзыву с id = {} успешно добавлен", userId, id);
            reviewStorage.addLikeReview(id, userId);
            reviewStorage.updateUseful(reviewDto.getUseful() + 1, id);
        }
    }

    public void addDislikeReview(final Long id, final Long userId) {
        validationUserId(userId);
        ReviewDto reviewDto = getReviewById(id);
        if (reviewStorage.existsLikeOnReview(id, userId, false)) {
            log.debug("Дизлайк от пользователя с id = {} к отзыву с id = {} уже добавлен", userId, id);
            throw new ExcessiveActionException("Дизлайк от пользователя с id = " + userId + " уже добавлен к отзыву с " +
                    "id = " + id);
        } else if (reviewStorage.existsLikeOnReview(id, userId, true)) {
            reviewStorage.replaceLikeAndDislike(id, userId, false);
            reviewStorage.updateUseful(reviewDto.getUseful() - 2, id);
        } else {
            log.debug("Дизлайк от пользователя с id = {} к отзыву с id = {} успешно добавлен", userId, id);
            reviewStorage.addDislikeReview(id, userId);
            reviewStorage.updateUseful(reviewDto.getUseful() - 1, id);
        }
    }

    public void deleteDislikeReview(final Long id, final Long userId) {
        validationUserId(userId);
        ReviewDto reviewDto = getReviewById(id);
        if (reviewStorage.existsLikeOnReview(id, userId, false)) {
            if (reviewStorage.deleteLikeReview(id, userId)) {
                reviewStorage.updateUseful(reviewDto.getUseful() + 1, id);
                log.debug("Дизлайк от пользователя с id = {} к отзыву с id = {} успешно удален", userId, id);
            }
        } else if (reviewStorage.existsLikeOnReview(id, userId, true)) {
            log.debug("Дизлайк от пользователя с id = {} к отзыву с id = {} невозможно удалить. " +
                    "На его месте стоит лайк", userId, id);
            throw new NotFoundException("Вы собираетесь убрать дизлайк, хотя на этот отзыв стоит лайк");
        } else {
            log.debug("Дизлайк от пользователя с id = {} к отзыву с id = {} не найден", userId, id);
            throw new NotFoundException("Вы собираетесь убрать дизлайк, хотя его не существует");
        }
    }

    public void deleteLikeReview(final Long id, final Long userId) {
        validationUserId(userId);
        ReviewDto reviewDto = getReviewById(id);
        if (reviewStorage.existsLikeOnReview(id, userId, true)) {
            if (reviewStorage.deleteLikeReview(id, userId)) {
                reviewStorage.updateUseful(reviewDto.getUseful() - 1, id);
                log.debug("Лайк от пользователя с id = {} к отзыву с id = {} успешно удален", userId, id);
            }
        } else if (reviewStorage.existsLikeOnReview(id, userId, false)) {
            log.debug("Лайк от пользователя с id = {} к отзыву с id = {} невозможно удалить. " +
                    "На его месте стоит дизлайк", userId, id);
            throw new NotFoundException("Вы собираетесь убрать лайк, хотя на этот отзыв стоит дизлайк");
        } else {
            log.debug("Лайк от пользователя с id = {} к отзыву с id = {} не найден", userId, id);
            throw new NotFoundException("Вы собираетесь убрать лайк, хотя его не существует");
        }
    }


    private void validationUserId(final Long userId) {
        if (userStorage.getUserById(userId).isEmpty()) {
            throw new NotFoundException("Пользователя с указанным id не существует");
        }
    }

    private void validationFilmId(final Long filmId) {
        if (filmStorage.getFilmById(filmId).isEmpty()) {
            throw new NotFoundException("Фильма с указанным id не существует");
        }
    }
}

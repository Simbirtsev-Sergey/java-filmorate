package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {
    Review create(final Review review);

    Optional<Review> getReviewById(final Long id);

    boolean delete(final Long id);

    Review update(Review updateReview);

    Collection<Review> getAllReviews(final Integer count);

    Collection<Review> getAllReviewsByIdFilms(final Long filmId, final Integer count);

    void addLikeReview(final Long id, final Long userId);

    void addDislikeReview(final Long id, final Long userId);

    boolean existsLikeOnReview(final Long id, final Long userId, boolean isLike);

    void replaceLikeAndDislike(final Long id, final Long userId, boolean isLike);

    void updateUseful(final int useful, final Long reviewId);

    boolean deleteLikeReview(final Long id, final Long userId);
}

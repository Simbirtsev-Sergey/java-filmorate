package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.rating.RatingStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmDbStorage;
    private final UserStorage userDbStorage;
    private final GenreStorage genreStorage;
    private final RatingStorage ratingStorage;

    @Autowired
    public FilmService(final FilmStorage filmDbStorage, final UserStorage userDbStorage,
                       final GenreStorage genreStorage, final RatingStorage ratingStorage) {
        this.filmDbStorage = filmDbStorage;
        this.userDbStorage = userDbStorage;
        this.genreStorage = genreStorage;
        this.ratingStorage = ratingStorage;
    }

    public Collection<FilmDto> getFilms() {
        return filmDbStorage.getFilms()
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public FilmDto getFilmById(final Long id) {
        return FilmMapper.mapToFilmDto(getFilmOrThrow(id));
    }

    public FilmDto create(final NewFilmRequest request) {
        validationDateRelease(request.getReleaseDate());
        validationDuration(request.getDuration());

        Film film = FilmMapper.mapToFilm(request);
        checkMpaAndGenresExist(film);

        film = filmDbStorage.create(film);

        // Перечитываем фильм из базы: так в ответе будут названия рейтинга и жанров, а жанры — по порядку id
        return getFilmById(film.getId());
    }

    public FilmDto update(final UpdateFilmRequest request) {
        if (request.getId() == null) {
            throw new ConditionsNotMetException("Вы не передали id фильма");
        }
        validationDateRelease(request.getReleaseDate());
        validationDuration(request.getDuration());
        Film updatedFilm = filmDbStorage.getFilmById(request.getId())
                .map(film -> FilmMapper.updateFilmFields(film, request))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        checkMpaAndGenresExist(updatedFilm);
        updatedFilm = filmDbStorage.update(updatedFilm);
        return getFilmById(updatedFilm.getId());
    }

    // Добавление лайка
    public void addLike(final Long filmId, final Long userId) {
        checkUserExists(userId);
        log.debug("Проверка на существование пользователя с id = {} при добавлении лайка пройдена успешно", userId);
        if (filmDbStorage.getFilmById(filmId).isEmpty()) {
            throw new NotFoundException("Переданного фильма не существует");
        }

        if (filmDbStorage.existsUserAndFilm(filmId, userId)) {
            log.debug("Лайк от пользователя с id = {} уже добавлен", userId);
            throw new ExcessiveActionException("Лайк от пользователя с id = " + userId + " уже добавлен");
        } else {
            log.debug("Лайк от пользователя с id = {} успешно добавлен", userId);
            filmDbStorage.insertLike(filmId, userId);
        }
    }

    // Удаление лайка
    public void deleteLike(final Long filmId, final Long userId) {
        checkUserExists(userId);
        log.debug("Проверка на существование пользователя с id = {} при удалении лайка пройдена успешно", userId);

        if (filmDbStorage.existsUserAndFilm(filmId, userId)) {
            filmDbStorage.deleteLike(filmId, userId);
            log.debug("Лайк от пользователя с id = {} успешно удален", userId);
        } else {
            log.debug("Лайк от пользователя с id = {} уже удален или его не существовало", userId);
            throw new InternalServerException("Лайк от пользователя с id = " + userId + " не найден");
        }
    }

    // Вывод 10 наиболее популярных фильмов по количеству лайков
    public Collection<FilmDto> topFilmsByLikes(final int count) {
        return filmDbStorage.topFilmsByLikes(count)
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    private void checkUserExists(final Long userId) {
        if (userDbStorage.getUsers().stream().mapToLong(User::getId).noneMatch(id -> id == userId)) {
            throw new NotFoundException("Пользователя с id = " + userId + " не существует");
        }
    }

    @NonNull
    public Film getFilmOrThrow(final Long id) {
        return filmDbStorage.getFilmById(id).orElseThrow(() ->
                new NotFoundException("Фильма с id = " + id + " не существует"));
    }

    // Без этой проверки несуществующий рейтинг или жанр дойдёт до базы, нарушит внешний ключ и вернёт 500
    private void checkMpaAndGenresExist(final Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null
                && ratingStorage.getRatingById(film.getMpa().getId()).isEmpty()) {
            throw new NotFoundException("Рейтинга с id = " + film.getMpa().getId() + " не существует");
        }
        if (film.getGenres() != null) {
            for (GenreDto genre : film.getGenres()) {
                if (genreStorage.getGenreById(genre.getId()).isEmpty()) {
                    throw new NotFoundException("Жанра с id = " + genre.getId() + " не существует");
                }
            }
        }
    }

    private void validationDateRelease(final LocalDate date) {
        if (date.isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    private void validationDuration(final Duration duration) {
        if (!duration.isPositive()) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExcessiveActionException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(final FilmStorage filmStorage, final UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film create(final Film film) {
        return filmStorage.create(film);
    }

    public Film update(final Film newFilm) {
        return filmStorage.update(newFilm);
    }

    // Добавление лайка
    public void addLike(final Long filmId, final Long userId) {
        checkUserExists(userId);
        log.debug("Проверка на существование пользователя с id = {} при добавлении лайка пройдена успешно", userId);

        final Film film = getFilmOrThrow(filmId);
        log.debug("Проверка на существование фильма с id = {} при добавлении лайка пройдена успешно", userId);

        if (film.getLikes().contains(userId)) {
            log.debug("Лайк от пользователя с id = {} уже добавлен", userId);
            throw new ExcessiveActionException("Лайк от пользователя с id = " + userId + " уже добавлен");
        } else {
            log.debug("Лайк от пользователя с id = {} успешно добавлен", userId);
            film.getLikes().add(userId);
        }
    }

    // Удаление лайка
    public void deleteLike(final Long filmId, final Long userId) {
        checkUserExists(userId);
        log.debug("Проверка на существование пользователя с id = {} при удалении лайка пройдена успешно", userId);

        final Film film = getFilmOrThrow(filmId);
        log.debug("Проверка на существование фильма с id = {} при удалении лайка пройдена успешно", userId);

        if (film.getLikes().contains(userId)) {
            film.getLikes().remove(userId);
            log.debug("Лайк от пользователя с id = {} успешно удален", userId);
        } else {
            log.debug("Лайк от пользователя с id = {} уже удален или его не существовало", userId);
            throw new ExcessiveActionException("Лайк от пользователя с id = " + userId + " не найден");
        }
    }

    // Вывод 10 наиболее популярных фильмов по количеству лайков
    public Collection<Film> topFilmsByLikes(final int count) {
        return filmStorage.getFilms()
                .stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    private void checkUserExists(final Long userId) {
        if (userStorage.getUsers().stream().mapToLong(User::getId).noneMatch(id -> id == userId)) {
            throw new NotFoundException("Пользователя с id = " + userId + " не существует");
        }
    }

    @NonNull
    public Film getFilmOrThrow(final Long id) {
        return filmStorage.getFilmById(id).orElseThrow(() ->
                new NotFoundException("Фильма с id = " + id + " не существует"));
    }
}
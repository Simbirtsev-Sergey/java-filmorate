package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Film create(final Film film) {
        long nextId = getNextId();
        film.setId(nextId);
        log.trace("Фильму успешно установлен id = {}", nextId);
        films.put(nextId, film);
        log.info("Фильм успешно добавлен");
        return film;
    }

    @Override
    public Film update(final Film newFilm) {
        if (newFilm.getId() == null) {
            log.debug("В теле запроса не указан Id фильма");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            log.trace("В теле запроса был указан корректный Id фильма");
            oldFilm.setName(newFilm.getName());
            log.trace("Название фильма успешно установлено");
            oldFilm.setDescription(newFilm.getDescription());
            log.trace("Описание фильма успешно установлено");
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            log.trace("Дата релиза фильма успешно установлена");
            oldFilm.setDuration(newFilm.getDuration());
            log.trace("Продолжительность фильма успешно установлена");
            log.info("Фильм успешно обновлен");
            return oldFilm;
        }
        log.debug("Фильм с id {} не был найден", newFilm.getId());
        throw new NotFoundException(String.format("Фильм с id = " + newFilm.getId() + " не найден"));
    }

    @Override
    public Optional<Film> getFilmById(final Long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public boolean existsUserAndFilm(final Long filmId, final Long userId) {
        return false;
    }

    @Override
    public void insertLike(final Long filmId, final Long userId) {
    }

    @Override
    public void deleteLike(final Long filmId, final Long userId) {
    }

    @Override
    public List<Film> topFilmsByLikes(final int count) {
        return List.of();
    }

    private Long getNextId() {
        return films.values()
                .stream()
                .mapToLong(Film::getId)
                .max()
                .orElse(0) + 1;
    }
}
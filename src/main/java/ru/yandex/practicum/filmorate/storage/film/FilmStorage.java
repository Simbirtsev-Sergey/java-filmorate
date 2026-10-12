package ru.yandex.practicum.filmorate.storage.film;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> getFilms();

    Film create(@Valid @RequestBody final Film film);

    Film update(@Valid @RequestBody final Film newFilm);

    Optional<Film> getFilmById(final Long filmId);

    boolean existsUserAndFilm(final Long filmId, final Long userId);

    void insertLike(final Long filmId, final Long userId);

    void deleteLike(final Long filmId, final Long userId);

    List<Film> topFilmsByLikes(final int count);
}

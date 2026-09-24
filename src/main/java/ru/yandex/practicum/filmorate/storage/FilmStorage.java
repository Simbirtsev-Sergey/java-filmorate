package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> getFilms();

    Film create(@Valid @RequestBody final Film film);

    Film update(@Valid @RequestBody final Film newFilm);

    Optional<Film> getFilmById(final Long filmId);
}

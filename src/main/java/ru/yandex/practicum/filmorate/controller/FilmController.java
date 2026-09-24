package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(final FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @PostMapping
    public Film create(@Valid @RequestBody final Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody final Film newFilm) {
        return filmService.update(newFilm);
    }

    @PutMapping("/{id}")
    public Film getFilmById(@Positive @PathVariable final Long id) {
        return filmService.getFilmOrThrow(id);
    }

    @PostMapping("/{id}/like/{userId}")
    public void userLikesMovie(@PathVariable final Long id, @PathVariable final Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void userDeleteLikesMovie(@PathVariable final Long id, @PathVariable final Long userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> mostPopularFilms(@Positive @RequestParam(defaultValue = "10") int count) {
        return filmService.topFilmsByLikes(count);
    }
}
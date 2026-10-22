package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;
    private final String uri = "/{id}/like/{userId}";

    @Autowired
    public FilmController(final FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<FilmDto> getFilms() {
        return filmService.getFilms();
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody final NewFilmRequest newFilmRequest) {
        return filmService.create(newFilmRequest);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody final UpdateFilmRequest updateFilmRequest) {
        return filmService.update(updateFilmRequest);
    }

    @GetMapping("/{id}")
    public FilmDto getFilmById(@Positive @PathVariable final Long id) {
        return filmService.getFilmById(id);
    }

    @PutMapping(uri)
    public void userLikesMovie(@PathVariable final Long id, @PathVariable final Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable final Long filmId) {
        filmService.deleteById(filmId);
    }

    @DeleteMapping(uri)
    public void userDeleteLikesMovie(@PathVariable final Long id, @PathVariable final Long userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<FilmDto> mostPopularFilms(@Positive @RequestParam(defaultValue = "10") int count) {
        return filmService.topFilmsByLikes(count);
    }

    @GetMapping("/search")
    public Collection<FilmDto> searchByDirectorAndName(@RequestParam String query, @RequestParam List<String> by) {
        return filmService.searchByDirectorAndName(query, by);
    }

    @GetMapping("/common")
    public Collection<FilmDto> getCommon(@RequestParam Long userId, @RequestParam Long friendId) {
        return filmService.getCommon(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public Collection<FilmDto> getFilmsByDirector(@PathVariable @Positive Long directorId, @RequestParam String sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }
}
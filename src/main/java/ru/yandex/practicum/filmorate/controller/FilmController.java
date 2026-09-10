package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody final Film film) {
        validation(film);
        log.debug("Валидация при создании фильма успешно пройдена");
        long nextId = getNextId();
        film.setId(nextId);
        log.trace("Id фильма успешно установлен");
        films.put(nextId, film);
        log.info("Фильм успешно добавлен");
        return film;
    }

    @PutMapping
    public Film update(@RequestBody final Film newFilm) {
        if (newFilm.getId() == null) {
            log.debug("В теле запроса не указан Id фильма");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            log.trace("В теле запроса был указан корректный Id фильма");
            validation(newFilm);
            log.debug("Валидация при обновлении фильма успешно пройдена");
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
        log.debug("Введенный id фильма не был найден");
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    private Long getNextId() {
        return films.values()
                .stream()
                .mapToLong(Film::getId)
                .max()
                .orElse(0) + 1;
    }

    private void validation(final Film film) {
        if (film.getName().isBlank()) {
            log.warn("Неправильный ввод названия");
            throw new ValidationException("Название не должно быть пустым");
        } else if (film.getDescription().length() > 200) {
            log.warn("Название фильма превышает 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        } else if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Дата релиза не может быть раньше день рождения кино");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        } else if (film.getDuration().isNegative()) {
            log.warn("Продолжительность фильма не может быть отрицательной");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}

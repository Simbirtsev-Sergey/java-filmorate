package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // Тестируем валидацию полей фильма (аннотации стоят в NewFilmRequest)

    @Test
    void validFilmHasNoViolations() {
        assertTrue(validator.validate(validFilm()).isEmpty());
    }

    @Test
    void createFilmWithEmptyName() {
        NewFilmRequest film = validFilm();
        film.setName("");

        assertTrue(hasViolation(film, "name"));
    }

    @Test
    void createFilmWithLongDescription() {
        NewFilmRequest film = validFilm();
        film.setDescription("F".repeat(201));

        assertTrue(hasViolation(film, "description"));
    }

    @Test
    void createFilmWithoutReleaseDate() {
        NewFilmRequest film = validFilm();
        film.setReleaseDate(null);

        assertTrue(hasViolation(film, "releaseDate"));
    }

    @Test
    void createFilmWithoutDuration() {
        NewFilmRequest film = validFilm();
        film.setDuration(null);

        assertTrue(hasViolation(film, "duration"));
    }

    // Эти правила проверяет FilmService, а не аннотации

    @Test
    void createFilmWithEarlyReleaseDate() {
        NewFilmRequest film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        FilmController controller = createController();

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void createFilmWithNegativeDuration() {
        NewFilmRequest film = validFilm();
        film.setDuration(Duration.ofMinutes(-50));

        FilmController controller = createController();

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    // Тестируем POST-метод для фильмов

    @Test
    void createValidFilm() {
        FilmController controller = createController();

        FilmDto created = controller.create(validFilm());

        assertNotNull(created.getId());
        assertEquals("Движение вверх", created.getName());
    }

    // Тестируем GET-метод для фильмов

    @Test
    void getFilmsReturnsCreatedFilm() {
        FilmController controller = createController();

        NewFilmRequest secondFilm = validFilm();
        secondFilm.setName("Колобок");

        FilmDto film1 = controller.create(validFilm());
        FilmDto film2 = controller.create(secondFilm);

        Collection<FilmDto> films = controller.getFilms();

        assertEquals(2, films.size());

        assertNotNull(film1.getId());
        assertNotEquals(film1.getId(), film2.getId());
        assertTrue(films.containsAll(List.of(film1, film2)));
    }

    @Test
    void getFilmsReturnsNoCreatedFilm() {
        FilmController controller = createController();

        Collection<FilmDto> collection = controller.getFilms();

        assertTrue(collection.isEmpty());
    }

    // Тестируем PUT-метод для фильмов

    @Test
    void updateFilm() {
        FilmController controller = createController();

        FilmDto created = controller.create(validFilm());

        FilmDto updated = controller.update(updateRequest(created.getId()));

        assertEquals(created.getId(), updated.getId());
        assertEquals("Колобок", updated.getName());
        assertEquals(Duration.ofMinutes(100), updated.getDuration());
    }

    @Test
    void updateFilmWithUnknownIdFails() {
        FilmController controller = createController();

        controller.create(validFilm());

        assertThrows(ConditionsNotMetException.class, () -> controller.update(updateRequest(null)));
    }

    @Test
    void updateFilmWithIncorrectId() {
        FilmController controller = createController();

        controller.create(validFilm());

        assertThrows(NotFoundException.class, () -> controller.update(updateRequest(2L)));
    }

    // Хранилища жанров и рейтингов здесь не нужны: фильмы в этих тестах создаются без жанров и рейтинга
    private FilmController createController() {
        FilmService filmService = new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage(), null, null);
        return new FilmController(filmService);
    }

    private NewFilmRequest validFilm() {
        NewFilmRequest film = new NewFilmRequest();
        film.setName("Движение вверх");
        film.setDescription("О победе на последних трёх секундах");
        film.setReleaseDate(LocalDate.of(2017, 12, 14));
        film.setDuration(Duration.ofMinutes(140));
        return film;
    }

    private UpdateFilmRequest updateRequest(Long id) {
        UpdateFilmRequest request = new UpdateFilmRequest();
        request.setId(id);
        request.setName("Колобок");
        request.setDescription("Замес года");
        request.setReleaseDate(LocalDate.of(2026, 1, 29));
        request.setDuration(Duration.ofMinutes(100));
        return request;
    }

    // true, если среди нарушений есть нарушение именно на этом поле
    private boolean hasViolation(Object object, String field) {
        return validator.validate(object).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals(field));
    }
}

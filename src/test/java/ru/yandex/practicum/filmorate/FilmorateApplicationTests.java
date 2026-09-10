package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmorateApplicationTests {

    @Autowired
    private MockMvc mockMvc; // им шлем запросы

    @Autowired
    private ObjectMapper objectMapper; // им превращаем объект в JSON

    // Тестируем POST-метод для фильмов

    @Test
    void createValidFilm() throws Exception {
        Film film = new Film();
        film.setName("Движение вверх");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2017, 12, 14));
        film.setDuration(Duration.ofSeconds(140));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Движение вверх"))
                .andExpect(jsonPath("$.duration").value(140));
    }

    @Test
    void createFilmWithEmptyName() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("");

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void createFilmWithLongDescription() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("Движение вверх");
        film.setDescription("F".repeat(201));

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void createFilmWithEarlyReleaseDate() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("Движение вверх");
        film.setDescription("О победе на последних трёх секундах");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void createFilmWithNegativeDuration() {
        FilmController controller = new FilmController();
        Film film = new Film();

        film.setName("Движение вверх");
        film.setDescription("О победе на последних трёх секундах");
        film.setReleaseDate(LocalDate.of(1895, 12, 29));
        film.setDuration(Duration.ofSeconds(-50));

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    // Тестируем GET-метод для фильмов

    @Test
    void getFilmsReturnsCreatedFilm() {
        FilmController controller = new FilmController();

        Film film1 = new Film();
        Film film2 = new Film();

        film1.setName("Движение вверх");
        film1.setDescription("О победе на последних трёх секундах");
        film1.setReleaseDate(LocalDate.of(1895, 12, 29));
        film1.setDuration(Duration.ofSeconds(50));

        film2.setName("Колобок");
        film2.setDescription("Замес года");
        film2.setReleaseDate(LocalDate.of(2026, 1, 29));
        film2.setDuration(Duration.ofSeconds(100));

        controller.create(film1);
        controller.create(film2);

        Collection<Film> films = controller.getFilms();

        assertEquals(2, films.size());

        assertNotNull(film1.getId());
        assertNotEquals(film1.getId(), film2.getId());
        assertTrue(films.containsAll(List.of(film1, film2)));
    }

    @Test
    void getFilmsReturnsNoCreatedFilm() {
        FilmController controller = new FilmController();
        Collection<Film> collection = controller.getFilms();

        assertTrue(collection.isEmpty());
    }

    // Тестируем PUT-метод для фильмов

    @Test
    void updateFilm() {
        FilmController controller = new FilmController();
        Film film = new Film();

        film.setName("Движение вверх");
        film.setDescription("О победе на последних трёх секундах");
        film.setReleaseDate(LocalDate.of(1895, 12, 29));
        film.setDuration(Duration.ofSeconds(50));

        controller.create(film);

        Film newFilm = new Film();

        newFilm.setId(1L);
        newFilm.setName("Колобок");
        newFilm.setDescription("Замес года");
        newFilm.setReleaseDate(LocalDate.of(2026, 1, 29));
        newFilm.setDuration(Duration.ofSeconds(100));

        controller.update(newFilm);

        Collection<Film> films = controller.getFilms();

        assertTrue(films.contains((newFilm)));
    }

    @Test
    void updateFilmWithUnknownIdFails() {
        FilmController controller = new FilmController();
        Film film = new Film();

        film.setName("Движение вверх");
        film.setDescription("О победе на последних трёх секундах");
        film.setReleaseDate(LocalDate.of(1895, 12, 29));
        film.setDuration(Duration.ofSeconds(50));

        controller.create(film);

        Film newFilm = new Film();

        newFilm.setName("Колобок");

        assertThrows(ConditionsNotMetException.class, () -> controller.update(newFilm));
    }

    @Test
    void updateFilmWithIncorrectId() {
        FilmController controller = new FilmController();
        Film film = new Film();

        film.setName("Движение вверх");
        film.setDescription("О победе на последних трёх секундах");
        film.setReleaseDate(LocalDate.of(1895, 12, 29));
        film.setDuration(Duration.ofSeconds(50));

        controller.create(film);

        Film newFilm = new Film();

        newFilm.setId(2L);
        newFilm.setName("Колобок");

        assertThrows(NotFoundException.class, () -> controller.update(newFilm));
    }

    // Тестируем POST-метод для пользователей

    @Test
    void createValidUser() {
        User user = new User();
        user.setEmail("SergeySi@mail.ru");
        user.setLogin("sting");
        user.setName("Sergey");
        user.setBirthday(LocalDate.of(2005, 11, 5));

        UserController controller = new UserController();

        User created = controller.create(user);

        assertNotNull(created.getId());
        assertEquals("Sergey", created.getName());
    }

    @Test
    void createdUserWithEmptyEmail() {
        User user = new User();

        user.setLogin("sting");
        user.setName("Sergey");
        user.setBirthday(LocalDate.of(2005, 11, 5));

        UserController controller = new UserController();

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void createdUserWithEmailNotContainingDog() {
        User user = new User();

        user.setEmail("SergeySimail.ru");
        user.setLogin("sting");
        user.setName("Sergey");
        user.setBirthday(LocalDate.of(2005, 11, 5));

        UserController controller = new UserController();

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void createdUserWithEmptyLogin() {
        User user = new User();

        user.setEmail("SergeySimail.ru");
        user.setName("Sergey");
        user.setBirthday(LocalDate.of(2005, 11, 5));

        UserController controller = new UserController();

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void createdUserWithBirthdayInFuture() {
        User user = new User();

        user.setEmail("SergeySimail.ru");
        user.setName("Sergey");
        user.setBirthday(LocalDate.of(2035, 11, 5));

        UserController controller = new UserController();

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void createdUserWithEmptyName() {
        User user = new User();

        user.setEmail("SergeySi@mail.ru");
        user.setLogin("sting");
        user.setBirthday(LocalDate.of(2015, 11, 5));

        UserController controller = new UserController();
        controller.create(user);

        assertEquals(user.getName(), user.getLogin());
    }

    // Тестируем GET-метод для пользователей
    @Test
    void getUsersReturnsCreatedUser() {
        UserController controller = new UserController();

        User user1 = new User();
        User user2 = new User();

        user1.setEmail("SergeySi@mail.ru");
        user1.setLogin("sting");
        user1.setName("Sergey");
        user1.setBirthday(LocalDate.of(2005, 11, 5));

        user2.setEmail("IvanDi@mail.ru");
        user2.setLogin("Dra");
        user2.setName("Ivan");
        user2.setBirthday(LocalDate.of(2015, 1, 18));

        controller.create(user1);
        controller.create(user2);

        Collection<User> users = controller.getUsers();

        assertEquals(2, users.size());

        assertNotNull(user1.getId());
        assertNotEquals(user1.getId(), user2.getId());
        assertTrue(users.containsAll(List.of(user1, user2)));
    }

    @Test
    void getUsersReturnsNoCreatedUser() {
        UserController controller = new UserController();
        Collection<User> collection = controller.getUsers();

        assertTrue(collection.isEmpty());
    }

    // Тестируем PUT-метод для пользователей

    @Test
    void updateUser() {
        UserController controller = new UserController();
        User user = new User();

        user.setEmail("SergeySi@mail.ru");
        user.setLogin("sting");
        user.setName("Sergey");
        user.setBirthday(LocalDate.of(2005, 11, 5));

        controller.create(user);

        User newUser = new User();

        newUser.setId(1L);
        newUser.setName("Danil");
        newUser.setLogin("Dan");
        newUser.setEmail("DanilColbasenko@mail.ru");
        newUser.setBirthday(LocalDate.of(2014, 5, 9));

        controller.update(newUser);

        Collection<User> users = controller.getUsers();

        assertTrue(users.contains((newUser)));
    }

    @Test
    void updateUserWithUnknownIdFails() {
        UserController controller = new UserController();
        User user = new User();

        user.setEmail("SergeySi@mail.ru");
        user.setLogin("sting");
        user.setName("Sergey");
        user.setBirthday(LocalDate.of(2005, 11, 5));

        controller.create(user);

        User newUser = new User();

        newUser.setName("Колобок");

        assertThrows(ConditionsNotMetException.class, () -> controller.update(newUser));
    }

    @Test
    void updateUserWithIncorrectId() {
        UserController controller = new UserController();
        User user = new User();

        user.setEmail("SergeySi@mail.ru");
        user.setLogin("sting");
        user.setName("Sergey");
        user.setBirthday(LocalDate.of(2005, 11, 5));

        controller.create(user);

        User newUser = new User();

        newUser.setId(2L);
        newUser.setName("Колобок");

        assertThrows(NotFoundException.class, () -> controller.update(newUser));
    }
}
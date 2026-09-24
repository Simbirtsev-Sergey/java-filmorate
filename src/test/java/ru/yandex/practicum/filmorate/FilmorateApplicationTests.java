package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    // Тестируем POST-метод для фильмов

    @Test
    void createValidFilm() {
        Film film = new Film();
        film.setName("Движение вверх");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2017, 12, 14));
        film.setDuration(Duration.ofSeconds(140));

        UserStorage userStorage = new InMemoryUserStorage();

        FilmStorage filmStorage = new InMemoryFilmStorage();
        Film created = filmStorage.create(film);

        FilmService filmService = new FilmService(filmStorage, userStorage);

        FilmController controller = new FilmController(filmService);

        assertNotNull(created.getId());
        assertTrue(controller.getFilms().contains(created));
    }

    @Test
    void createFilmWithEmptyName() {
        Film film = new Film();
        film.setName("");

        UserStorage userStorage = new InMemoryUserStorage();

        FilmStorage filmStorage = new InMemoryFilmStorage();

        FilmService filmService = new FilmService(filmStorage, userStorage);

        FilmController controller = new FilmController(filmService);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void createFilmWithLongDescription() {
        Film film = new Film();
        film.setName("Движение вверх");
        film.setDescription("F".repeat(201));

        UserStorage userStorage = new InMemoryUserStorage();

        FilmStorage filmStorage = new InMemoryFilmStorage();

        FilmService filmService = new FilmService(filmStorage, userStorage);

        FilmController controller = new FilmController(filmService);
        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void createFilmWithEarlyReleaseDate() {
        Film film = new Film();
        film.setName("Движение вверх");
        film.setDescription("О победе на последних трёх секундах");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        UserStorage userStorage = new InMemoryUserStorage();

        FilmStorage filmStorage = new InMemoryFilmStorage();

        FilmService filmService = new FilmService(filmStorage, userStorage);

        FilmController controller = new FilmController(filmService);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void createFilmWithNegativeDuration() {
        Film film = new Film();
        film.setName("Движение вверх");
        film.setDescription("О победе на последних трёх секундах");
        film.setReleaseDate(LocalDate.of(1895, 12, 29));
        film.setDuration(Duration.ofSeconds(-50));

        UserStorage userStorage = new InMemoryUserStorage();

        FilmStorage filmStorage = new InMemoryFilmStorage();

        FilmService filmService = new FilmService(filmStorage, userStorage);

        FilmController controller = new FilmController(filmService);

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    // Тестируем GET-метод для фильмов

    @Test
    void getFilmsReturnsCreatedFilm() {
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

        UserStorage userStorage = new InMemoryUserStorage();

        FilmStorage filmStorage = new InMemoryFilmStorage();

        FilmService filmService = new FilmService(filmStorage, userStorage);

        FilmController controller = new FilmController(filmService);

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
        UserStorage userStorage = new InMemoryUserStorage();

        FilmStorage filmStorage = new InMemoryFilmStorage();

        FilmService filmService = new FilmService(filmStorage, userStorage);

        FilmController controller = new FilmController(filmService);

        Collection<Film> collection = controller.getFilms();

        assertTrue(collection.isEmpty());
    }

    // Тестируем PUT-метод для фильмов

    @Test
    void updateFilm() {
        Film film = new Film();

        film.setName("Движение вверх");
        film.setDescription("О победе на последних трёх секундах");
        film.setReleaseDate(LocalDate.of(1895, 12, 29));
        film.setDuration(Duration.ofSeconds(50));

        UserStorage userStorage = new InMemoryUserStorage();

        FilmStorage filmStorage = new InMemoryFilmStorage();

        FilmService filmService = new FilmService(filmStorage, userStorage);

        FilmController controller = new FilmController(filmService);

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
        Film film = new Film();

        film.setName("Движение вверх");
        film.setDescription("О победе на последних трёх секундах");
        film.setReleaseDate(LocalDate.of(1895, 12, 29));
        film.setDuration(Duration.ofSeconds(50));

        UserStorage userStorage = new InMemoryUserStorage();

        FilmStorage filmStorage = new InMemoryFilmStorage();

        FilmService filmService = new FilmService(filmStorage, userStorage);

        FilmController controller = new FilmController(filmService);

        controller.create(film);

        Film newFilm = new Film();

        newFilm.setName("Колобок");

        assertThrows(ConditionsNotMetException.class, () -> controller.update(newFilm));
    }

    @Test
    void updateFilmWithIncorrectId() {
        Film film = new Film();

        film.setName("Движение вверх");
        film.setDescription("О победе на последних трёх секундах");
        film.setReleaseDate(LocalDate.of(1895, 12, 29));
        film.setDuration(Duration.ofSeconds(50));

        UserStorage userStorage = new InMemoryUserStorage();

        FilmStorage filmStorage = new InMemoryFilmStorage();

        FilmService filmService = new FilmService(filmStorage, userStorage);

        FilmController controller = new FilmController(filmService);

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

        UserStorage userStorage = new InMemoryUserStorage();

        UserService userService = new UserService(userStorage);

        UserController controller = new UserController(userService);

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

        UserStorage userStorage = new InMemoryUserStorage();

        UserService userService = new UserService(userStorage);

        UserController controller = new UserController(userService);

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void createdUserWithEmailNotContainingDog() {
        User user = new User();

        user.setEmail("SergeySimail.ru");
        user.setLogin("sting");
        user.setName("Sergey");
        user.setBirthday(LocalDate.of(2005, 11, 5));

        UserStorage userStorage = new InMemoryUserStorage();

        UserService userService = new UserService(userStorage);

        UserController controller = new UserController(userService);

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void createdUserWithEmptyLogin() {
        User user = new User();

        user.setEmail("SergeySimail.ru");
        user.setName("Sergey");
        user.setBirthday(LocalDate.of(2005, 11, 5));

        UserStorage userStorage = new InMemoryUserStorage();

        UserService userService = new UserService(userStorage);

        UserController controller = new UserController(userService);

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void createdUserWithBirthdayInFuture() {
        User user = new User();

        user.setEmail("SergeySimail.ru");
        user.setName("Sergey");
        user.setBirthday(LocalDate.of(2035, 11, 5));

        UserStorage userStorage = new InMemoryUserStorage();

        UserService userService = new UserService(userStorage);

        UserController controller = new UserController(userService);

        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void createdUserWithEmptyName() {
        User user = new User();

        user.setEmail("SergeySi@mail.ru");
        user.setLogin("sting");
        user.setBirthday(LocalDate.of(2015, 11, 5));

        UserStorage userStorage = new InMemoryUserStorage();

        UserService userService = new UserService(userStorage);

        UserController controller = new UserController(userService);

        controller.create(user);

        assertEquals(user.getName(), user.getLogin());
    }

    // Тестируем GET-метод для пользователей
    @Test
    void getUsersReturnsCreatedUser() {
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

        UserStorage userStorage = new InMemoryUserStorage();

        UserService userService = new UserService(userStorage);

        UserController controller = new UserController(userService);

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
        UserStorage userStorage = new InMemoryUserStorage();

        UserService userService = new UserService(userStorage);

        UserController controller = new UserController(userService);

        Collection<User> collection = controller.getUsers();

        assertTrue(collection.isEmpty());
    }

    // Тестируем PUT-метод для пользователей

    @Test
    void updateUser() {
        User user = new User();

        user.setEmail("SergeySi@mail.ru");
        user.setLogin("sting");
        user.setName("Sergey");
        user.setBirthday(LocalDate.of(2005, 11, 5));

        UserStorage userStorage = new InMemoryUserStorage();

        UserService userService = new UserService(userStorage);

        UserController controller = new UserController(userService);

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
        User user = new User();

        user.setEmail("SergeySi@mail.ru");
        user.setLogin("sting");
        user.setName("Sergey");
        user.setBirthday(LocalDate.of(2005, 11, 5));

        UserStorage userStorage = new InMemoryUserStorage();

        UserService userService = new UserService(userStorage);

        UserController controller = new UserController(userService);

        controller.create(user);

        User newUser = new User();

        newUser.setName("Колобок");

        assertThrows(ConditionsNotMetException.class, () -> controller.update(newUser));
    }

    @Test
    void updateUserWithIncorrectId() {
        User user = new User();

        user.setEmail("SergeySi@mail.ru");
        user.setLogin("sting");
        user.setName("Sergey");
        user.setBirthday(LocalDate.of(2005, 11, 5));

        UserStorage userStorage = new InMemoryUserStorage();

        UserService userService = new UserService(userStorage);

        UserController controller = new UserController(userService);

        controller.create(user);

        User newUser = new User();

        newUser.setId(2L);
        newUser.setName("Колобок");

        assertThrows(NotFoundException.class, () -> controller.update(newUser));
    }
}
package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // Тестируем валидацию полей пользователя

    @Test
    void validUserHasNoViolations() {
        assertTrue(validator.validate(validUser()).isEmpty());
    }

    @Test
    void createdUserWithEmptyEmail() {
        User user = validUser();
        user.setEmail("");

        assertTrue(hasViolation(user, "email"));
    }

    @Test
    void createdUserWithEmailNotContainingDog() {
        User user = validUser();
        user.setEmail("SergeySimail.ru");

        assertTrue(hasViolation(user, "email"));
    }

    @Test
    void createdUserWithEmptyLogin() {
        User user = validUser();
        user.setLogin("");

        assertTrue(hasViolation(user, "login"));
    }

    @Test
    void createdUserWithSpaceInLogin() {
        User user = validUser();
        user.setLogin("st ing");

        assertTrue(hasViolation(user, "login"));
    }

    @Test
    void createdUserWithBirthdayInFuture() {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        assertTrue(hasViolation(user, "birthday"));
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

    private User validUser() {
        User user = new User();
        user.setEmail("SergeySi@mail.ru");
        user.setLogin("sting");
        user.setName("Sergey");
        user.setBirthday(LocalDate.of(2005, 11, 5));
        return user;
    }

    // true, если среди нарушений есть нарушение именно на этом поле
    private boolean hasViolation(Object object, String field) {
        return validator.validate(object).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals(field));
    }
}

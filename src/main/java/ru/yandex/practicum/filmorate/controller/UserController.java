package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody final User user) {
        validation(user);
        log.debug("Валидация при создании пользователя успешно пройдена");
        long nextId = getNextId();
        user.setId(nextId);
        log.trace("Id пользователя успешно установлен");
        users.put(nextId, user);
        log.info("Пользователь успешно добавлен");
        return user;
    }

    @PutMapping
    public User update(@RequestBody final User newUser) {
        if (newUser.getId() == null) {
            log.debug("В теле запроса не указан Id пользователя");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            log.trace("В теле запроса был указан корректный Id пользователя");
            validation(newUser);
            log.debug("Валидация при обновлении пользователя успешно пройдена");
            oldUser.setEmail(newUser.getEmail());
            log.trace("Email успешно установлен");
            oldUser.setLogin(newUser.getLogin());
            log.trace("Логин успешно установлен");
            oldUser.setName(newUser.getName());
            log.trace("Имя успешно установлено");
            oldUser.setBirthday(newUser.getBirthday());
            log.trace("День рождения успешно установлен");
            log.info("Пользователь успешно обновлен");
            return oldUser;
        }
        log.debug("Введенный id пользователя не был найден");
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    private Long getNextId() {
        return users.values()
                .stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0) + 1;
    }

    private void validation(final User user) {
        if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Неправильный ввод email");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        } else if (user.getLogin().isBlank()) {
            log.warn("Неправильный ввод логина");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        } else if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Неправильный ввод даты рождения");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
    }
}

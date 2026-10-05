package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public User create(final User user) {
        validationName(user);
        log.debug("Валидация при создании пользователя успешно пройдена");
        long nextId = getNextId();
        user.setId(nextId);
        log.trace("Пользователю успешно установлен id = {}", nextId);
        users.put(nextId, user);
        log.info("Пользователь успешно добавлен");
        return user;
    }

    @Override
    public User update(final User newUser) {
        if (newUser.getId() == null) {
            log.debug("В теле запроса не указан Id пользователя");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            log.trace("В теле запроса был указан корректный Id пользователя");
            validationName(newUser);
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
        log.debug("Пользователь с id {} не был найден", newUser.getId());
        throw new NotFoundException(String.format("Пользователь с id = " + newUser.getId() + " не найден"));
    }

    private Long getNextId() {
        return users.values()
                .stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0) + 1;
    }

    private void validationName(final User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
    }

    public Optional<User> getUserById(final Long userId) {
        return Optional.ofNullable(users.get(userId));
    }
}

package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExcessiveActionException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userDbStorage;

    @Autowired
    public UserService(final UserDbStorage userDbStorage) {
        this.userDbStorage = userDbStorage;
    }

    public UserService(final InMemoryUserStorage userStorage) {
        this.userDbStorage = userStorage;
    }

    public UserService(final UserStorage userStorage) {
        this.userDbStorage = userStorage;
    }

    public Collection<User> getUsers() {
        return userDbStorage.getUsers();
    }

    public User create(final User user) {
        validationName(user);
        return userDbStorage.create(user);
    }

    public User update(final User newUser) {
        validationName(newUser);
        return userDbStorage.update(newUser);
    }

    // Добавление в друзья
    public void addFriend(final Long id, final Long friendId) {
        if (id.equals(friendId)) {
            throw new ValidationException("Id не должны совпадать");
        }
        checkingUser(id);
        log.debug("Проверка на существование пользователя с id = {} при добавлении друга пройдена успешно", id);
        checkingUser(friendId);
        log.debug("Проверка на существование пользователя с otherId = {} при добавлении друга пройдена успешно",
                friendId);

        if (userDbStorage.hasFriendAdded(id, friendId)) {
            log.debug("Друг с friendId = {} пользователя с id = {} уже добавлен", friendId, id);
            throw new ExcessiveActionException("Пользователь с id = " + friendId + " уже добавлен в друзья");
        }
        userDbStorage.addFriend(id, friendId);
        log.debug("Друг с friendId = {} пользователя с id = {} успешно добавлен", friendId, id);
    }

    // Удаление из друзей
    public void deleteFriend(final Long id, final Long friendId) {
        checkingUser(id);
        log.debug("Проверка на существование пользователя с id = {} при удалении друга пройдена успешно", id);
        checkingUser(friendId);
        log.debug("Проверка на существование пользователя с otherId = {} при удалении друга пройдена успешно",
                friendId);

        userDbStorage.deleteFriend(id, friendId);
        log.debug("Друг с friendId = {} пользователя с id = {} успешно удален", friendId, id);
    }

    // Вывод друзей пользователя
    public Collection<User> usersFriends(final Long id) {
        checkingUser(id);
        log.debug("Проверка на существование пользователя с id = {} при выводе друзей пройдена успешно", id);
        return userDbStorage.findFriends(id);
    }

    // Вывод общих друзей
    public Collection<User> mutualFriends(final Long id, final Long otherId) {
        checkingUser(id);
        log.debug("Проверка на существование пользователя с id = {} при поиске общих друзей пройдена успешно", id);
        checkingUser(otherId);
        log.debug("Проверка на существование пользователя с otherId = {} при поиске общих друзей пройдена успешно",
                otherId);

        List<User> friendsUsers = userDbStorage.findFriends(id);

        List<Long> commonFriends = findCommonFriends(friendsUsers, userDbStorage.findFriends(otherId));

        return friendsUsers.stream()
                .map(User::getId)
                .filter(commonFriends::contains)
                .map((idUser) -> userDbStorage.getUserById(idUser).orElseThrow())
                .toList();
    }

    private void checkingUser(final Long id) {
        if (userDbStorage.getUserById(id).isEmpty()) {
            throw new NotFoundException("Пользователя с id = " + id + " не существует");
        }
    }

    private List<Long> findCommonFriends(List<User> list1, List<User> list2) {
        List<Long> q1 = list1.stream()
                .map(User::getId)
                .toList();
        List<Long> q2 = list2.stream()
                .map(User::getId)
                .toList();


        return q1.stream().filter(q2::contains).collect(Collectors.toList());
    }

    private void validationName(final User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ExcessiveActionException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(final UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User create(final User user) {
        return userStorage.create(user);
    }

    public User update(final User newUser) {
        return userStorage.update(newUser);
    }

    // Добавление в друзья
    public void addFriend(final Long id, final Long friendId) {
        final User user = getUserOrThrow(id);
        log.debug("Проверка на существование пользователя с id = {} при добавлении друга пройдена успешно", id);
        final User usersFriend = getUserOrThrow(friendId);
        log.debug("Проверка на существование пользователя с otherId = {} при добавлении друга пройдена успешно",
                friendId);

        if (user.getFriends().contains(friendId)) {
            log.debug("Друг с friendId = {} пользователя с id = {} при добавлении не найден", friendId, id);
            throw new ExcessiveActionException("Пользователь с id = " + friendId + " уже добавлен в друзья");
        }
        user.getFriends().add(friendId);
        usersFriend.getFriends().add(id);
        log.debug("Друг с friendId = {} пользователя с id = {} успешно добавлен", friendId, id);
    }

    // Удаление из друзей
    public void deleteFriend(final Long id, final Long friendId) {
        final User user = getUserOrThrow(id);
        log.debug("Проверка на существование пользователя с id = {} при удалении друга пройдена успешно", id);
        final User usersFriend = getUserOrThrow(friendId);
        log.debug("Проверка на существование пользователя с otherId = {} при удалении друга пройдена успешно",
                friendId);

        if (!user.getFriends().contains(friendId)) {
            log.debug("Друг с friendId = {} пользователя с id = {} при удалении не найден", friendId, id);
            throw new ExcessiveActionException("Друг пользователя с id = " + friendId + " не найден");
        }

        user.getFriends().remove(friendId);
        usersFriend.getFriends().remove(id);
        log.debug("Друг с friendId = {} пользователя с id = {} успешно удален", friendId, id);
    }

    // Вывод друзей пользователя
    public Collection<User> usersFriends(final Long id) {
        final User user = getUserOrThrow(id);
        log.debug("Проверка на существование пользователя с id = {} при выводе друзей пройдена успешно", id);

        return getUsers().stream()
                .filter(us -> user.getFriends().contains(us.getId()))
                .toList();
    }

    // Вывод общих друзей
    public Collection<User> mutualFriends(final Long id, final Long otherId) {
        final User user = getUserOrThrow(id);
        log.debug("Проверка на существование пользователя с id = {} при поиске общих друзей пройдена успешно", id);
        final User usersFriend = getUserOrThrow(otherId);
        log.debug("Проверка на существование пользователя с otherId = {} при поиске общих друзей пройдена успешно",
                otherId);

        Set<Long> commonFriends = findCommonFriends(user.getFriends(), usersFriend.getFriends());

        return user.getFriends()
                .stream()
                .filter(commonFriends::contains)
                .map(this::getUserOrThrow)
                .toList();
    }

    @NonNull
    private User getUserOrThrow(final Long id) {
        return userStorage.getUserById(id).orElseThrow(() ->
                new NotFoundException("Пользователя с id = " + id + " не существует"));
    }

    private Set<Long> findCommonFriends(Set<Long> set1, Set<Long> set2) {
        return set1.stream()
                .filter(set2::contains)
                .collect(Collectors.toSet());
    }
}
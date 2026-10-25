package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.action.ActionDto;
import ru.yandex.practicum.filmorate.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.exception.ExcessiveActionException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.ActionMapper;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.action.ActionDbStorage;
import ru.yandex.practicum.filmorate.storage.action.ActionStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userDbStorage;
    private final ActionStorage actionStorage;
    private final FilmStorage filmStorage;

    @Autowired
    public UserService(final UserDbStorage userDbStorage, final FilmStorage filmStorage,
                       final ActionDbStorage actionDbStorage) {
        this.userDbStorage = userDbStorage;
        this.filmStorage = filmStorage;
        this.actionStorage = actionDbStorage;
    }

    public UserService(final InMemoryUserStorage userStorage, final FilmStorage filmStorage,
                       final ActionStorage actionStorage) {
        this.userDbStorage = userStorage;
        this.filmStorage = filmStorage;
        this.actionStorage = actionStorage;
    }

    public UserService(final UserStorage userStorage, final FilmStorage filmStorage,
                       final ActionStorage actionStorage) {
        this.userDbStorage = userStorage;
        this.filmStorage = filmStorage;
        this.actionStorage = actionStorage;
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

    public void delete(final Long userId) {
        userDbStorage.delete(userId);
    }

    public Optional<User> getUserById(final Long userId) {
        checkingUser(userId);
        return userDbStorage.getUserById(userId);
    }

    // Добавление в друзья
    public void addFriend(final Long id, final Long friendId) {
        if (id.equals(friendId)) {
            throw new ValidationException("Id не должны совпадать");
        }
        checkingUser(id);
        log.debug("Проверка на существование пользователя с id = {} при добавлении друга пройдена успешно", id);
        checkingUser(friendId);
        log.debug("Проверка на существование пользователя с otherId = {} при добавлении друга пройдена успешно", friendId);

        if (userDbStorage.hasFriendAdded(id, friendId)) {
            log.debug("Друг с friendId = {} пользователя с id = {} уже добавлен", friendId, id);
            throw new ExcessiveActionException("Пользователь с id = " + friendId + " уже добавлен в друзья");
        }
        userDbStorage.addFriend(id, friendId);
        actionStorage.createAction(actionStorage.create(id, 2L, 3L, friendId));
        log.debug("Друг с friendId = {} пользователя с id = {} успешно добавлен", friendId, id);
    }

    // Удаление из друзей
    public void deleteFriend(final Long id, final Long friendId) {
        checkingUser(id);
        log.debug("Проверка на существование пользователя с id = {} при удалении друга пройдена успешно", id);
        checkingUser(friendId);
        log.debug("Проверка на существование пользователя с otherId = {} при удалении друга пройдена успешно", friendId);

        userDbStorage.deleteFriend(id, friendId);
        actionStorage.createAction(actionStorage.create(id, 1L, 3L, friendId));
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

        return friendsUsers.stream().map(User::getId).filter(commonFriends::contains).map((idUser) ->
                userDbStorage.getUserById(idUser).orElseThrow()).toList();
    }

    public Collection<FilmDto> getRecommendations(Long userId) {
        checkingUser(userId);
        Collection<Long> filmIds = userDbStorage.getRecommendations(userId);

        if (filmIds.isEmpty()) {
            return List.of();
        }

        Set<Long> recommendationIds = new HashSet<>(filmIds);

        return filmStorage.getFilms()
                .stream()
                .filter(film -> recommendationIds.contains(film.getId()))
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public Collection<ActionDto> activityFeed(final Long id) {
        checkingUser(id);
        log.debug("Проверка на существование пользователя с id = {} " +
                "при получении последнего действия друга пройдена успешно", id);
        return actionStorage.activityFeed(id)
                .stream()
                .map(ActionMapper::mapToActionDto)
                .toList();
    }

    private void checkingUser(final Long id) {
        if (userDbStorage.getUserById(id).isEmpty()) {
            throw new NotFoundException("Пользователя с id = " + id + " не существует");
        }
    }

    private List<Long> findCommonFriends(List<User> list1, List<User> list2) {
        List<Long> q1 = list1.stream().map(User::getId).toList();
        List<Long> q2 = list2.stream().map(User::getId).toList();

        return q1.stream().filter(q2::contains).collect(Collectors.toList());
    }

    private void validationName(final User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
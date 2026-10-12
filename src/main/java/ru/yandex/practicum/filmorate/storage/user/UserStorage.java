package ru.yandex.practicum.filmorate.storage.user;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    Collection<User> getUsers();

    User create(@Valid @RequestBody final User user);

    User update(@Valid @RequestBody final User newUser);

    Optional<User> getUserById(final Long userId);

    void addFriend(final Long id, final Long friendId);

    void deleteFriend(final Long id, final Long friendId);

    boolean hasFriendAdded(final Long id, final Long friendId);

    List<User> findFriends(final Long id);
}

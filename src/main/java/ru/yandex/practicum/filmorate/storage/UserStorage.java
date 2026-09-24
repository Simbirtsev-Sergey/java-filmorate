package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> getUsers();

    User create(@Valid @RequestBody final User user);

    User update(@Valid @RequestBody final User newUser);

    Optional<User> getUserById(final Long userId);
}

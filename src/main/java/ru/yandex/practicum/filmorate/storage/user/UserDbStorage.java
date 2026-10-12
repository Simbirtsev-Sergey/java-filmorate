package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class UserDbStorage extends BaseStorage<User> implements UserStorage {
    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM Users";
    private static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM Users WHERE user_id = ?";
    private static final String UPDATE_USER_QUERY = "UPDATE Users SET email = ?, login = ?, name = ?, birthday = ? " +
            "WHERE user_id = ?";
    private static final String CREATE_USER_QUERY = "INSERT INTO Users(email, login, name, birthday) " +
            "VALUES(?, ?, ?, ?)";
    private static final String CREATE_FRIENDSHIP_QUERY = "INSERT INTO Friendships(sender_id, recipient_id, " +
            "application_id) VALUES(?, ?, ?)";
    private static final String DELETE_FRIENDSHIP_QUERY =
            "DELETE FROM Friendships WHERE sender_id = ? AND recipient_id = ?";
    private static final String FIND_FRIENDSHIP_QUERY = "SELECT COUNT(*) FROM Friendships WHERE sender_id = ? AND " +
            "recipient_id  = ?";
    private static final String FIND_FRIENDS_QUERY = "SELECT * FROM Users WHERE user_id IN " +
            "(SELECT recipient_id FROM Friendships WHERE sender_id = ?) ORDER BY user_id";
    private static final String UPDATE_FRIENDSHIP_STATUS_QUERY =
            "UPDATE Friendships SET application_id = ? WHERE sender_id = ? AND recipient_id = ?";

    @Autowired
    public UserDbStorage(final JdbcTemplate jdbc, final RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<User> getUsers() {
        return findMany(FIND_ALL_USERS_QUERY);
    }

    @Override
    public User create(final User user) {
        long id = insert(CREATE_USER_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    @Override
    public User update(final User newUser) {
        update(UPDATE_USER_QUERY,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday(),
                newUser.getId()
        );
        return newUser;
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return findOne(FIND_USER_BY_ID_QUERY, userId);
    }

    @Override
    public void addFriend(final Long id, final Long friendId) {
        boolean isMutual = doesRowExist(FIND_FRIENDSHIP_QUERY, friendId, id);
        update(CREATE_FRIENDSHIP_QUERY, id, friendId, isMutual ? 1 : 2);
        if (isMutual) {
            update(UPDATE_FRIENDSHIP_STATUS_QUERY, 1, friendId, id);
        }
    }

    @Override
    public void deleteFriend(final Long id, final Long friendId) {
        if (delete(DELETE_FRIENDSHIP_QUERY, id, friendId)
                && doesRowExist(FIND_FRIENDSHIP_QUERY, friendId, id)) {
            update(UPDATE_FRIENDSHIP_STATUS_QUERY, 2, friendId, id);
        }
    }

    @Override
    public boolean hasFriendAdded(final Long id, final Long friendId) {
        return doesRowExist(FIND_FRIENDSHIP_QUERY, id, friendId);
    }

    @Override
    public List<User> findFriends(final Long id) {
        return findMany(FIND_FRIENDS_QUERY, id);
    }
}

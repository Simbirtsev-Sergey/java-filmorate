package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.dto.rating.RatingDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class, FilmDbStorage.class, FilmRowMapper.class})
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    // Тестируем UserDbStorage

    @Test
    public void testCreate() {
        User created = userStorage.create(newUser("sting"));

        assertThat(created.getId()).isNotNull();
    }

    @Test
    public void testGetUserById() {
        User created = userStorage.create(newUser("sting"));

        Optional<User> userOptional = userStorage.getUserById(created.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", created.getId());
                    assertThat(user).hasFieldOrPropertyWithValue("email", "sting@mail.ru");
                    assertThat(user).hasFieldOrPropertyWithValue("login", "sting");
                    assertThat(user).hasFieldOrPropertyWithValue("name", "Name");
                    assertThat(user).hasFieldOrPropertyWithValue("birthday", LocalDate.of(1990, 1, 1));
                });
    }

    @Test
    public void testGetUserByUnknownId() {
        assertThat(userStorage.getUserById(9999L)).isEmpty();
    }

    @Test
    public void testGetUsers() {
        User user1 = userStorage.create(newUser("first"));
        User user2 = userStorage.create(newUser("second"));

        assertThat(userStorage.getUsers())
                .extracting(User::getId)
                .contains(user1.getId(), user2.getId());
    }

    @Test
    public void testUpdate() {
        User user = userStorage.create(newUser("sting"));
        user.setEmail("updated@mail.ru");
        user.setLogin("updated");
        user.setName("Updated");
        user.setBirthday(LocalDate.of(2000, 5, 5));

        userStorage.update(user);

        assertThat(userStorage.getUserById(user.getId()))
                .hasValueSatisfying(updated -> {
                    assertThat(updated.getEmail()).isEqualTo("updated@mail.ru");
                    assertThat(updated.getLogin()).isEqualTo("updated");
                    assertThat(updated.getName()).isEqualTo("Updated");
                    assertThat(updated.getBirthday()).isEqualTo(LocalDate.of(2000, 5, 5));
                });
    }

    @Test
    public void testFindFriends() {
        User user = userStorage.create(newUser("user"));
        User friend1 = userStorage.create(newUser("friend1"));
        User friend2 = userStorage.create(newUser("friend2"));

        userStorage.addFriend(user.getId(), friend1.getId());
        userStorage.addFriend(user.getId(), friend2.getId());

        assertThat(userStorage.findFriends(user.getId()))
                .extracting(User::getId)
                .containsExactly(friend1.getId(), friend2.getId());
    }

    @Test
    public void testAddFriendIsOneSided() {
        User user = userStorage.create(newUser("user"));
        User friend = userStorage.create(newUser("friend"));

        userStorage.addFriend(user.getId(), friend.getId());

        assertThat(userStorage.findFriends(user.getId()))
                .extracting(User::getId)
                .containsExactly(friend.getId());
        assertThat(userStorage.findFriends(friend.getId())).isEmpty();
    }

    @Test
    public void testAddFriendFromBothSides() {
        User user = userStorage.create(newUser("user"));
        User friend = userStorage.create(newUser("friend"));

        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.addFriend(friend.getId(), user.getId());

        assertThat(userStorage.findFriends(user.getId()))
                .extracting(User::getId)
                .containsExactly(friend.getId());
        assertThat(userStorage.findFriends(friend.getId()))
                .extracting(User::getId)
                .containsExactly(user.getId());
    }

    @Test
    public void testHasFriendAdded() {
        User user = userStorage.create(newUser("user"));
        User friend = userStorage.create(newUser("friend"));

        assertThat(userStorage.hasFriendAdded(user.getId(), friend.getId())).isFalse();

        userStorage.addFriend(user.getId(), friend.getId());

        assertThat(userStorage.hasFriendAdded(user.getId(), friend.getId())).isTrue();
        assertThat(userStorage.hasFriendAdded(friend.getId(), user.getId())).isFalse();
    }

    @Test
    public void testDeleteFriend() {
        User user = userStorage.create(newUser("user"));
        User friend = userStorage.create(newUser("friend"));
        userStorage.addFriend(user.getId(), friend.getId());

        userStorage.deleteFriend(user.getId(), friend.getId());

        assertThat(userStorage.findFriends(user.getId())).isEmpty();
        assertThat(userStorage.hasFriendAdded(user.getId(), friend.getId())).isFalse();
    }

    @Test
    public void testDeleteFriendKeepsOtherSide() {
        User user = userStorage.create(newUser("user"));
        User friend = userStorage.create(newUser("friend"));
        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.addFriend(friend.getId(), user.getId());

        userStorage.deleteFriend(user.getId(), friend.getId());

        assertThat(userStorage.findFriends(user.getId())).isEmpty();
        assertThat(userStorage.findFriends(friend.getId()))
                .extracting(User::getId)
                .containsExactly(user.getId());
    }

    @Test
    public void testDeleteNotFriend() {
        User user = userStorage.create(newUser("user"));
        User other = userStorage.create(newUser("other"));

        userStorage.deleteFriend(user.getId(), other.getId());

        assertThat(userStorage.findFriends(user.getId())).isEmpty();
    }

    // Тестируем FilmDbStorage

    @Test
    public void testCreateFilm() {
        Film created = filmStorage.create(newFilm("Матрица", 1L, 1L));

        assertThat(created.getId()).isNotNull();
    }

    @Test
    public void testGetFilmById() {
        Film created = filmStorage.create(newFilm("Матрица", 3L, 2L, 1L));

        Optional<Film> filmOptional = filmStorage.getFilmById(created.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getId()).isEqualTo(created.getId());
                    assertThat(film.getName()).isEqualTo("Матрица");
                    assertThat(film.getDescription()).isEqualTo("Описание");
                    assertThat(film.getReleaseDate()).isEqualTo(LocalDate.of(1999, 3, 31));
                    assertThat(film.getDuration()).isEqualTo(Duration.ofMinutes(136));
                    assertThat(film.getMpa().getId()).isEqualTo(3L);
                    assertThat(film.getMpa().getName()).isEqualTo("PG-13");
                    // Жанры возвращаются с названиями и по порядку id, даже если сохранялись в другом порядке
                    assertThat(film.getGenres()).extracting(GenreDto::getId).containsExactly(1L, 2L);
                    assertThat(film.getGenres()).extracting(GenreDto::getName).containsExactly("Комедия", "Драма");
                });
    }

    @Test
    public void testGetFilmWithoutMpaAndGenres() {
        Film created = filmStorage.create(newFilm("Без рейтинга", null));

        assertThat(filmStorage.getFilmById(created.getId()))
                .hasValueSatisfying(film -> {
                    assertThat(film.getMpa()).isNull();
                    assertThat(film.getGenres()).isEmpty();
                });
    }

    @Test
    public void testGetFilmByUnknownId() {
        assertThat(filmStorage.getFilmById(9999L)).isEmpty();
    }

    @Test
    public void testGetFilms() {
        Film film1 = filmStorage.create(newFilm("Первый", 1L, 1L, 2L));
        Film film2 = filmStorage.create(newFilm("Второй", 2L));

        assertThat(filmStorage.getFilms())
                .extracting(Film::getId)
                .contains(film1.getId(), film2.getId());
        assertThat(filmStorage.getFilms())
                .filteredOn(film -> film.getId().equals(film1.getId()))
                .singleElement()
                .satisfies(film -> assertThat(film.getGenres()).extracting(GenreDto::getId).containsExactly(1L, 2L));
    }

    @Test
    public void testUpdateFilm() {
        Film film = filmStorage.create(newFilm("Матрица", 1L, 1L));
        Film newFilm = newFilm("Матрица: Перезагрузка", 3L, 4L, 3L);
        newFilm.setId(film.getId());
        newFilm.setDescription("Новое описание");
        newFilm.setReleaseDate(LocalDate.of(2003, 5, 15));
        newFilm.setDuration(Duration.ofMinutes(138));

        filmStorage.update(newFilm);

        assertThat(filmStorage.getFilmById(film.getId()))
                .hasValueSatisfying(updated -> {
                    assertThat(updated.getName()).isEqualTo("Матрица: Перезагрузка");
                    assertThat(updated.getDescription()).isEqualTo("Новое описание");
                    assertThat(updated.getReleaseDate()).isEqualTo(LocalDate.of(2003, 5, 15));
                    assertThat(updated.getDuration()).isEqualTo(Duration.ofMinutes(138));
                    assertThat(updated.getMpa().getId()).isEqualTo(3L);
                    // Старый жанр 1 должен исчезнуть: жанры при обновлении заменяются целиком
                    assertThat(updated.getGenres()).extracting(GenreDto::getId).containsExactly(3L, 4L);
                });
    }

    @Test
    public void testUpdateFilmRemovesGenres() {
        Film film = filmStorage.create(newFilm("Матрица", 1L, 1L, 2L));
        film.setGenres(new LinkedHashSet<>());

        filmStorage.update(film);

        assertThat(filmStorage.getFilmById(film.getId()))
                .hasValueSatisfying(updated -> assertThat(updated.getGenres()).isEmpty());
    }

    @Test
    public void testInsertLike() {
        User user = userStorage.create(newUser("user"));
        Film film = filmStorage.create(newFilm("Матрица", 1L));

        assertThat(filmStorage.existsUserAndFilm(film.getId(), user.getId())).isFalse();

        filmStorage.insertLike(film.getId(), user.getId());

        assertThat(filmStorage.existsUserAndFilm(film.getId(), user.getId())).isTrue();
    }

    @Test
    public void testDeleteLike() {
        User user = userStorage.create(newUser("user"));
        Film film = filmStorage.create(newFilm("Матрица", 1L));
        filmStorage.insertLike(film.getId(), user.getId());

        filmStorage.deleteLike(film.getId(), user.getId());

        assertThat(filmStorage.existsUserAndFilm(film.getId(), user.getId())).isFalse();
    }

    @Test
    public void testTopFilmsByLikes() {
        User user1 = userStorage.create(newUser("user1"));
        User user2 = userStorage.create(newUser("user2"));
        User user3 = userStorage.create(newUser("user3"));
        Film oneLike = filmStorage.create(newFilm("Один лайк", 1L));
        Film threeLikes = filmStorage.create(newFilm("Три лайка", 1L));
        Film twoLikes = filmStorage.create(newFilm("Два лайка", 1L));
        Film noLikes = filmStorage.create(newFilm("Без лайков", 1L));

        filmStorage.insertLike(oneLike.getId(), user1.getId());
        filmStorage.insertLike(threeLikes.getId(), user1.getId());
        filmStorage.insertLike(threeLikes.getId(), user2.getId());
        filmStorage.insertLike(threeLikes.getId(), user3.getId());
        filmStorage.insertLike(twoLikes.getId(), user1.getId());
        filmStorage.insertLike(twoLikes.getId(), user2.getId());

        assertThat(filmStorage.topFilmsByLikes(10))
                .extracting(Film::getId)
                .containsSubsequence(threeLikes.getId(), twoLikes.getId(), oneLike.getId())
                .doesNotContain(noLikes.getId());
        assertThat(filmStorage.topFilmsByLikes(1)).hasSize(1);
    }

    private User newUser(String login) {
        User user = new User();
        user.setEmail(login + "@mail.ru");
        user.setLogin(login);
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    // mpaId может быть null — тогда фильм создаётся без рейтинга
    private Film newFilm(String name, Long mpaId, Long... genreIds) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(Duration.ofMinutes(136));

        if (mpaId != null) {
            RatingDto mpa = new RatingDto();
            mpa.setId(mpaId);
            film.setMpa(mpa);
        }

        Set<GenreDto> genres = new LinkedHashSet<>();
        for (Long genreId : genreIds) {
            GenreDto genre = new GenreDto();
            genre.setId(genreId);
            genres.add(genre);
        }
        film.setGenres(genres);

        return film;
    }
}

package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.BaseStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
@Primary
public class FilmDbStorage extends BaseStorage<Film> implements FilmStorage {
    // Название рейтинга подтягивается сразу вместе с фильмом; FilmRowMapper читает его из колонки rating_name,
    // поэтому она должна быть во всех запросах, которые читают фильмы
    private static final String SELECT_FILMS = "SELECT f.*, r.name AS rating_name FROM Films f " +
            "LEFT JOIN Ratings r ON f.rating_id = r.rating_id ";
    private static final String FIND_ALL_QUERY = SELECT_FILMS + "ORDER BY f.film_id";
    private static final String FIND_FILM_BY_ID_QUERY = SELECT_FILMS + "WHERE f.film_id = ?";
    private static final String UPDATE_FILM_QUERY = "UPDATE Films SET name = ?,  description = ?, release_date = ?, " +
            "duration = ?, rating_id = ? WHERE film_id = ?";
    private static final String CREATE_FILM_QUERY = "INSERT INTO Films (name, description, release_date, " +
            "duration, rating_id) VALUES(?, ?, ?, ?, ?)";
    private static final String FIND_FILM_LIKES_QUERY = "SELECT COUNT(*) FROM Film_likes " +
            "WHERE user_id = ? AND film_id = ?";
    private static final String ADD_LIKE_TO_FILM_QUERY = "INSERT INTO Film_likes(user_id, film_id) VALUES(?, ?)";
    private static final String DELETE_LIKE_TO_FILM_QUERY = "DELETE FROM Film_likes WHERE user_id = ? AND film_id = ?";
    private static final String TOP_FILMS_BY_LIKES_QUERY = "SELECT f.*, r.name AS rating_name FROM Films f " +
            "JOIN (SELECT film_id, COUNT(*) AS likes FROM Film_likes GROUP BY film_id) l ON f.film_id = l.film_id " +
            "LEFT JOIN Ratings r ON f.rating_id = r.rating_id " +
            "ORDER BY l.likes DESC, f.film_id LIMIT ?";
    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO Film_genres(film_id, genre_id) VALUES(?, ?)";
    private static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM Film_genres WHERE film_id = ?";
    private static final String FIND_GENRES_BY_FILM_QUERY = "SELECT g.genre_id, g.name FROM Film_genres fg " +
            "JOIN Genres g ON g.genre_id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.genre_id";
    private static final String FIND_ALL_FILM_GENRES_QUERY = "SELECT fg.film_id, g.genre_id, g.name " +
            "FROM Film_genres fg JOIN Genres g ON g.genre_id = fg.genre_id ORDER BY fg.film_id, g.genre_id";

    @Autowired
    public FilmDbStorage(final JdbcTemplate jdbc, final RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Film> getFilms() {
        return withGenres(findMany(FIND_ALL_QUERY));
    }

    @Override
    public Film create(Film film) {
        long id = insert(CREATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration().toMinutes(),
                mpaId(film)
        );
        film.setId(id);
        saveGenres(id, film.getGenres());
        return film;
    }

    @Override
    public Film update(final Film newFilm) {
        update(UPDATE_FILM_QUERY,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration().toMinutes(),
                mpaId(newFilm),
                newFilm.getId()
        );
        // Жанры заменяются целиком: удаляем старые связи и записываем текущий набор
        jdbc.update(DELETE_FILM_GENRES_QUERY, newFilm.getId());
        saveGenres(newFilm.getId(), newFilm.getGenres());
        return newFilm;
    }

    @Override
    public Optional<Film> getFilmById(final Long filmId) {
        Optional<Film> film = findOne(FIND_FILM_BY_ID_QUERY, filmId);
        film.ifPresent(f -> f.setGenres(new LinkedHashSet<>(
                jdbc.query(FIND_GENRES_BY_FILM_QUERY, (rs, rowNum) -> mapGenre(rs), f.getId()))));
        return film;
    }

    @Override
    public void insertLike(final Long filmId, final Long userId) {
        update(ADD_LIKE_TO_FILM_QUERY, userId, filmId);
    }

    @Override
    public void deleteLike(final Long filmId, final Long userId) {
        delete(DELETE_LIKE_TO_FILM_QUERY, userId, filmId);
    }

    @Override
    public boolean existsUserAndFilm(final Long filmId, final Long userId) {
        return doesRowExist(FIND_FILM_LIKES_QUERY, userId, filmId);
    }

    @Override
    public List<Film> topFilmsByLikes(final int count) {
        return withGenres(findMany(TOP_FILMS_BY_LIKES_QUERY, count));
    }

    // В колонку rating_id пишется id рейтинга, а не объект целиком; у фильма без рейтинга — NULL
    private Long mpaId(final Film film) {
        return film.getMpa() == null ? null : film.getMpa().getId();
    }

    // Дубликаты отбрасываются по id: повторная строка (film_id, genre_id) нарушила бы первичный ключ
    private void saveGenres(final Long filmId, final Set<GenreDto> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        List<Object[]> rows = genres.stream()
                .map(GenreDto::getId)
                .distinct()
                .map(genreId -> new Object[]{filmId, genreId})
                .toList();
        jdbc.batchUpdate(INSERT_FILM_GENRE_QUERY, rows);
    }

    // Жанры для списка фильмов загружаются одним запросом, а не отдельным запросом на каждый фильм
    private List<Film> withGenres(final List<Film> films) {
        if (films.isEmpty()) {
            return films;
        }
        Map<Long, Set<GenreDto>> genresByFilm = new HashMap<>();
        jdbc.query(FIND_ALL_FILM_GENRES_QUERY, (RowCallbackHandler) rs ->
                genresByFilm.computeIfAbsent(rs.getLong("film_id"), key -> new LinkedHashSet<>()).add(mapGenre(rs)));
        films.forEach(film -> film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>())));
        return films;
    }

    private static GenreDto mapGenre(final ResultSet rs) throws SQLException {
        GenreDto genre = new GenreDto();
        genre.setId(rs.getLong("genre_id"));
        genre.setName(rs.getString("name"));
        return genre;
    }
}

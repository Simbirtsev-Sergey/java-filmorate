package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.BaseStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Primary
public class FilmDbStorage extends BaseStorage<Film> implements FilmStorage {
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
    private static final String TOP_FILMS_BY_LIKES_QUERY = SELECT_FILMS +
            "LEFT JOIN (SELECT film_id, COUNT(*) AS likes FROM Film_likes GROUP BY film_id) l ON f.film_id = l.film_id " +
            "ORDER BY l.likes DESC, f.film_id LIMIT ?";
    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO Film_genres(film_id, genre_id) VALUES(?, ?)";
    private static final String INSERT_FILM_DIRECTOR_QUERY = "INSERT INTO Film_directors(film_id, director_id) VALUES(?, ?)";
    private static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM Film_genres WHERE film_id = ?";
    private static final String DELETE_FILM_DIRECTORS_QUERY = "DELETE FROM Film_directors WHERE film_id = ?";
    private static final String FIND_GENRES_BY_FILM_QUERY = "SELECT g.genre_id, g.name FROM Film_genres fg " +
            "JOIN Genres g ON g.genre_id = fg.genre_id WHERE fg.film_id = ? ORDER BY g.genre_id";
    private static final String FIND_DIRECTORS_BY_FILM_QUERY = "SELECT d.director_id, d.name FROM Film_directors fd " +
            "JOIN Directors d ON d.director_id = fd.director_id WHERE fd.film_id = ? ORDER BY d.director_id";
    private static final String FIND_ALL_FILM_GENRES_QUERY = "SELECT fg.film_id, g.genre_id, g.name " +
            "FROM Film_genres fg JOIN Genres g ON g.genre_id = fg.genre_id ORDER BY fg.film_id, g.genre_id";
    private static final String FIND_ALL_FILM_DIRECTORS_QUERY = "SELECT fd.film_id, d.director_id, d.name " +
            "FROM Film_directors fd JOIN Directors d ON d.director_id = fd.director_id ORDER BY fd.film_id, d.director_id";
    private static final String COMMON_FILMS_QUERY = SELECT_FILMS +
            "JOIN (SELECT film_id, COUNT(*) AS likes FROM Film_likes GROUP BY film_id) l " +
            "ON f.film_id = l.film_id " +
            "WHERE f.film_id IN (SELECT film_id FROM Film_likes WHERE user_id = ?) " +
            "AND f.film_id IN (SELECT film_id FROM Film_likes WHERE user_id = ?) " +
            "ORDER BY l.likes DESC, f.film_id";

    @Autowired
    public FilmDbStorage(final JdbcTemplate jdbc, final RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Film> getFilms() {
        return withGenresAndDirectors(findMany(FIND_ALL_QUERY));
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
        saveGenresAndDirectors(id, film.getGenres(), film.getDirectors());
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
        jdbc.update(DELETE_FILM_DIRECTORS_QUERY, newFilm.getId());
        saveGenresAndDirectors(newFilm.getId(), newFilm.getGenres(), newFilm.getDirectors());
        return newFilm;
    }

    @Override
    public void delete(Long filmId) {
        String sql = "DELETE FROM Films WHERE film_id = ?";
        jdbc.update(sql, filmId);
    }

    @Override
    public Optional<Film> getFilmById(final Long filmId) {
        Optional<Film> film = findOne(FIND_FILM_BY_ID_QUERY, filmId);

        film.ifPresent(f -> f.setGenres(new LinkedHashSet<>(
                jdbc.query(FIND_GENRES_BY_FILM_QUERY, (rs, rowNum) -> mapGenre(rs), f.getId()))));
        film.ifPresent(f -> f.setDirectors(new ArrayList<>(
                jdbc.query(FIND_DIRECTORS_BY_FILM_QUERY, (rs, rowNum) -> mapDirector(rs), f.getId()))));
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
        return withGenresAndDirectors(findMany(TOP_FILMS_BY_LIKES_QUERY, count));
    }

    @Override
    public Collection<Film> getCommon(Long userId, Long friendId) {
        return withGenresAndDirectors(findMany(COMMON_FILMS_QUERY, userId, friendId));
    }

    @Override
    public Collection<Film> getByDirector(Long directorId, String sortBy) {
        String query = SELECT_FILMS +
                " LEFT JOIN (SELECT film_id, COUNT(*) AS likes FROM Film_likes GROUP BY film_id) l ON f.film_id = l.film_id "
                + "LEFT JOIN Film_directors fd ON f.film_id = fd.film_id "
                + "WHERE fd.director_id = ? ORDER BY " + sortBy;
        return withGenresAndDirectors(findMany(query, directorId));
    }

    // В колонку rating_id пишется id рейтинга, а не объект целиком; у фильма без рейтинга — NULL
    private Long mpaId(final Film film) {
        return film.getMpa() == null ? null : film.getMpa().getId();
    }

    // Дубликаты отбрасываются по id: повторная строка (film_id, genre_id) нарушила бы первичный ключ
    private void saveGenresAndDirectors(final Long filmId, final Set<GenreDto> genres, List<DirectorDto> directors) {
        if (genres != null && !genres.isEmpty()) {
            List<Object[]> rows = genres.stream()
                    .map(GenreDto::getId)
                    .distinct()
                    .map(genreId -> new Object[]{filmId, genreId})
                    .toList();
            jdbc.batchUpdate(INSERT_FILM_GENRE_QUERY, rows);
        }

        if (directors != null && !directors.isEmpty()) {
            List<Object[]> rows = directors.stream()
                    .map(DirectorDto::getId)
                    .distinct()
                    .map(directorId -> new Object[]{filmId, directorId})
                    .toList();
            jdbc.batchUpdate(INSERT_FILM_DIRECTOR_QUERY, rows);
        }
    }

    // Жанры и режиссеры для списка фильмов загружаются одним запросом, а не отдельным запросом на каждый фильм
    private List<Film> withGenresAndDirectors(final List<Film> films) {
        if (films.isEmpty()) {
            return films;
        }
        Map<Long, Set<GenreDto>> genresByFilm = new HashMap<>();
        jdbc.query(FIND_ALL_FILM_GENRES_QUERY, (RowCallbackHandler) rs ->
                genresByFilm.computeIfAbsent(rs.getLong("film_id"), key -> new LinkedHashSet<>()).add(mapGenre(rs)));
        films.forEach(film -> film.setGenres(genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>())));

        Map<Long, List<DirectorDto>> directorsByFilm = new HashMap<>();
        jdbc.query(FIND_ALL_FILM_DIRECTORS_QUERY, (RowCallbackHandler) rs ->
                directorsByFilm.computeIfAbsent(rs.getLong("film_id"), key -> new ArrayList<>()).add(mapDirector(rs)));
        films.forEach(film -> film.setDirectors(directorsByFilm.getOrDefault(film.getId(), new ArrayList<>())));
        return films;
    }

    private static GenreDto mapGenre(final ResultSet rs) throws SQLException {
        GenreDto genre = new GenreDto();
        genre.setId(rs.getLong("genre_id"));
        genre.setName(rs.getString("name"));
        return genre;
    }

    private static DirectorDto mapDirector(final ResultSet rs) throws SQLException {
        DirectorDto director = DirectorDto.builder()
                .id(rs.getLong("director_id"))
                .name(rs.getString("name"))
                .build();
        return director;
    }
}

package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseStorage;

import java.util.Collection;
import java.util.Optional;

@Repository
public class GenreDbStorage extends BaseStorage<Genre> implements GenreStorage {
    private static final String FIND_ALL_GENRES_QUERY = "SELECT * FROM Genres ORDER BY genre_id";
    private static final String FIND_GENRE_BY_ID_QUERY = "SELECT * FROM Genres WHERE genre_id = ?";

    @Autowired
    public GenreDbStorage(final JdbcTemplate jdbc, final RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Genre> getGenres() {
        return findMany(FIND_ALL_GENRES_QUERY);
    }

    @Override
    public Optional<Genre> getGenreById(final Long id) {
        return findOne(FIND_GENRE_BY_ID_QUERY, id);
    }
}

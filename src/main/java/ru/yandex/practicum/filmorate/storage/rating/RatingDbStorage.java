package ru.yandex.practicum.filmorate.storage.rating;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.BaseStorage;

import java.util.Collection;
import java.util.Optional;

@Repository
public class RatingDbStorage extends BaseStorage<Rating> implements RatingStorage {
    private static final String FIND_ALL_RATINGS_QUERY = "SELECT * FROM Ratings ORDER BY rating_id";
    private static final String FIND_RATING_BY_ID_QUERY = "SELECT * FROM Ratings WHERE rating_id = ?";


    @Autowired
    public RatingDbStorage(final JdbcTemplate jdbc, final RowMapper<Rating> mapper) {
        super(jdbc, mapper);
    }

    public Collection<Rating> getRatings() {
        return findMany(FIND_ALL_RATINGS_QUERY);
    }

    public Optional<Rating> getRatingById(final Long id) {
        return findOne(FIND_RATING_BY_ID_QUERY, id);
    }
}

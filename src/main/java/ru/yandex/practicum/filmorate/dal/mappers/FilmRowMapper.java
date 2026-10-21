package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.rating.RatingDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {
        Film film = new Film();

        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());

        Duration duration = Duration.of(resultSet.getInt("duration"), ChronoUnit.MINUTES);
        film.setDuration(duration);

        Long ratingId = resultSet.getObject("rating_id", Long.class);
        if (ratingId != null) {
            RatingDto rating = new RatingDto();
            rating.setId(ratingId);
            rating.setName(resultSet.getString("rating_name"));
            film.setMpa(rating);
        }

        return film;
    }
}
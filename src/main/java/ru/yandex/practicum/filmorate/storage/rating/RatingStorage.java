package ru.yandex.practicum.filmorate.storage.rating;

import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.Optional;

public interface RatingStorage {
    Collection<Rating> getRatings();

    Optional<Rating> getRatingById(final Long id);
}

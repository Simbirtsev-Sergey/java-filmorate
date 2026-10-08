package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.rating.RatingStorage;

import java.util.Collection;

@Service
public class RatingService {
    private final RatingStorage ratingStorage;

    @Autowired
    public RatingService(final RatingStorage ratingStorage) {
        this.ratingStorage = ratingStorage;
    }

    public Collection<Rating> getRatings() {
        return ratingStorage.getRatings();
    }

    public Rating getRatingById(final Long id) {
        return ratingStorage.getRatingById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинга с id = " + " не найдено"));
    }
}

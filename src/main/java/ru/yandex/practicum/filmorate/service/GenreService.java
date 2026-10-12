package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;

@Service
public class GenreService {
    private final GenreStorage genreStorage;

    @Autowired
    public GenreService(final GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Collection<Genre> getGenres() {
        return genreStorage.getGenres();
    }

    public Genre getGenreById(final Long id) {
        return genreStorage.getGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанра с id = " + id + " не найдено"));
    }
}

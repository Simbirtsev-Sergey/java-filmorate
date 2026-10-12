package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.dto.rating.RatingDto;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    Duration duration;
    RatingDto mpa;
    Set<GenreDto> genres = new LinkedHashSet<>();
}
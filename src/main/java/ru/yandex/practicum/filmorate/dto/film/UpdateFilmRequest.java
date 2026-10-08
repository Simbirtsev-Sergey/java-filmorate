package ru.yandex.practicum.filmorate.dto.film;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.deserializer.DurationDeserializer;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.dto.rating.RatingDto;
import ru.yandex.practicum.filmorate.serializer.DurationSerializer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateFilmRequest {
    private Long id;
    @NotBlank(message = "Название не должно быть пустым")
    private String name;
    @Size(
            max = 200,
            message = "Максимальная длина описания — 200 символов"
    )
    private String description;
    @NotNull(message = "Дата релиза обязательна")
    private LocalDate releaseDate;
    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonSerialize(using = DurationSerializer.class)
    @NotNull
    private Duration duration;
    private RatingDto mpa;
    private Set<GenreDto> genres;

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return releaseDate != null;
    }

    public boolean hasDuration() {
        return duration != null;
    }

    public boolean hasMpa() {
        return mpa != null && mpa.getId() != null;
    }

    public boolean hasGenres() {
        return genres != null;
    }
}

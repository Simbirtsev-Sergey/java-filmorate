package ru.yandex.practicum.filmorate.dto.genre;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenreDto {
    Long id;
    String name;
}

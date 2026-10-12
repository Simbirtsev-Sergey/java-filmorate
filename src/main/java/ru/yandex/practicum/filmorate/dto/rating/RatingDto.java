package ru.yandex.practicum.filmorate.dto.rating;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingDto {
    Long id;
    String name;
}

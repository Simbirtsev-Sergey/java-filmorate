package ru.yandex.practicum.filmorate.dto.director;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectorDto {
    Long id;
    String name;
}

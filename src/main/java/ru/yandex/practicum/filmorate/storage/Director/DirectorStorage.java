package ru.yandex.practicum.filmorate.storage.Director;

import ru.yandex.practicum.filmorate.dto.director.DirectorDto;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {
    Collection<DirectorDto> getAllDirectors();

    Optional<DirectorDto> getDirectorById(Long id);

    DirectorDto addDirector(String name);

    DirectorDto updateDirector(DirectorDto newDirector);

    void deleteDirector(Long id);
}

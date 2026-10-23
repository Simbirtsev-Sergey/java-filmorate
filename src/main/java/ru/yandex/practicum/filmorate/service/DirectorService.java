package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.Director.DirectorStorage;

import java.util.Collection;

@Slf4j
@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Collection<DirectorDto> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public DirectorDto getDirectorById(Long id) {
        return directorStorage.getDirectorById(id)
                .orElseThrow(() -> new NotFoundException("Фильма с id = " + id + " не существует"));
    }


    public DirectorDto addDirector(String name) {
        return directorStorage.addDirector(name);
    }

    public DirectorDto updateDirector(DirectorDto newDirector) {
        if (newDirector.getId() == null)
            throw new ConditionsNotMetException("Вы не передали id фильма");
        if (newDirector.getName() == null || newDirector.getName().isBlank())
            throw new ConditionsNotMetException("Вы не передали имя режиссера");
        return directorStorage.updateDirector(newDirector);
    }

    public void deleteDirector(Long id) {
        directorStorage.deleteDirector(id);
    }
}

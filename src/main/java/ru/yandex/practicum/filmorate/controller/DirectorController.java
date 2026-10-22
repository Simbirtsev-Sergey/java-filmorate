package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@Slf4j
public class DirectorController {
    private final DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public Collection<DirectorDto> getDirectors() {
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public DirectorDto getDirectorById(@PathVariable @Positive Long id) {
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public DirectorDto addDirector(@RequestBody DirectorDto director) {
        return directorService.addDirector(director.getName());
    }

    @PutMapping
    public DirectorDto updateDirector(@RequestBody DirectorDto newDirector) {
        return directorService.updateDirector(newDirector);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable @Positive Long id) {
        directorService.deleteDirector(id);
    }
}

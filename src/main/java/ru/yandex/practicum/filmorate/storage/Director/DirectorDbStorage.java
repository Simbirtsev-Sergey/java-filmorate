package ru.yandex.practicum.filmorate.storage.Director;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.director.DirectorDto;
import ru.yandex.practicum.filmorate.storage.BaseStorage;

import java.util.Collection;
import java.util.Optional;

@Repository
public class DirectorDbStorage extends BaseStorage<DirectorDto> implements DirectorStorage {
    public DirectorDbStorage(JdbcTemplate jdbc, RowMapper<DirectorDto> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<DirectorDto> getAllDirectors() {
        String query = "SELECT * FROM DIRECTORS";
        return findMany(query);
    }

    @Override
    public Optional<DirectorDto> getDirectorById(Long id) {
        String query = "SELECT * FROM DIRECTORS WHERE director_id = ?";
        return findOne(query, id);
    }

    @Override
    public DirectorDto addDirector(String name) {
        String query = "INSERT INTO DIRECTORS (name) VALUES (?)";
        long id = insert(query, name);
        return DirectorDto.builder()
                .id(id)
                .name(name)
                .build();
    }

    @Override
    public DirectorDto updateDirector(DirectorDto newDirector) {
        String query = "UPDATE DIRECTORS SET name = ? WHERE director_id = ?";
        update(query, newDirector.getName(), newDirector.getId());
        return newDirector;
    }

    @Override
    public void deleteDirector(Long id) {
        String query = "DELETE FROM DIRECTORS WHERE director_id = ?";
        delete(query, id);
    }
}

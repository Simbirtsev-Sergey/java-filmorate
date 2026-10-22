package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Action;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ActionRowMapper implements RowMapper<Action> {
    @Nullable
    @Override
    public Action mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Action action = new Action();

        action.setEventId(resultSet.getLong("event_id"));
        action.setTimestamp(resultSet.getLong("timestamp"));
        action.setUserId(resultSet.getLong("user_id"));
        action.setOperationId(resultSet.getLong("operation_id"));
        action.setEventTypeId(resultSet.getLong("event_type_id"));
        action.setEntityId(resultSet.getLong("entity_id"));

        return action;
    }
}
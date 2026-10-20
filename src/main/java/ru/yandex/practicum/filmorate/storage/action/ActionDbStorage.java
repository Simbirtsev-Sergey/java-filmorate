package ru.yandex.practicum.filmorate.storage.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Action;
import ru.yandex.practicum.filmorate.storage.BaseStorage;

import java.time.Instant;
import java.util.Collection;

@Repository
@Primary
public class ActionDbStorage extends BaseStorage<Action> implements ActionStorage {
    private static final String CREATE_EVENTS_QUERY = "INSERT INTO Events(timestamp, user_id, operation_id, " +
            "event_type_id, entity_id)" +
            "VALUES(?, ?, ?, ?, ?)";
    private static final String FIND_EVENTS_QUERY = "SELECT timestamp, user_id, event_type_id, operation_id, " +
            "event_id, entity_id " +
            "FROM Events " +
            "WHERE user_id = ?";

    @Autowired
    public ActionDbStorage(JdbcTemplate jdbc, RowMapper<Action> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Action> activityFeed(final Long id) {
        return findMany(FIND_EVENTS_QUERY, id);
    }

    @Override
    public void createAction(final Action action) {
        long id = insert(CREATE_EVENTS_QUERY,
                action.getTimestamp(),
                action.getUserId(),
                action.getOperationId(),
                action.getEventTypeId(),
                action.getEntityId()
        );
        action.setEventId(id);
    }

    @Override
    public Action create(final Long userId,
                         final Long operationId,
                         final Long eventTypeId,
                         final Long entityId) {
        return new Action(Instant.now(),
                userId,
                operationId,
                eventTypeId,
                entityId
        );
    }
}
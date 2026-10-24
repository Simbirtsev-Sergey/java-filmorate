package ru.yandex.practicum.filmorate.storage.action;

import ru.yandex.practicum.filmorate.model.Action;

import java.util.Collection;

public interface ActionStorage {
    Collection<Action> activityFeed(final Long id);

    void createAction(final Action action);

    Action create(final Long userId, final Long operationId, final Long eventTypeId, final Long entityId);
}
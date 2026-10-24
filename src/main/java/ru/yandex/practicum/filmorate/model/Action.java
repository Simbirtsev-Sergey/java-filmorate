package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Action {
    Long eventId;
    Long timestamp;
    Long userId;
    Long operationId;
    Long eventTypeId;
    Long entityId;

    public Action(final Long  timestamp, final Long userId, final Long operationId,
                  final Long eventTypeId, final Long entityId
    ) {
        this.entityId = entityId;
        this.eventTypeId = eventTypeId;
        this.operationId = operationId;
        this.userId = userId;
        this.timestamp = timestamp;
    }
}
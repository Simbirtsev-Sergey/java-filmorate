package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Action {
    Long eventId;
    Instant timestamp;
    Long userId;
    Long operationId;
    Long eventTypeId;
    Long entityId;

    public Action(final Instant  timestamp, final Long userId, final Long operationId,
                  final Long eventTypeId, final Long entityId
    ) {
        this.entityId = entityId;
        this.eventTypeId = eventTypeId;
        this.operationId = operationId;
        this.userId = userId;
        this.timestamp = timestamp;
    }
}
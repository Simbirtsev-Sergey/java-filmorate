package ru.yandex.practicum.filmorate.dto.action;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ActionDto {
    Long eventId;
    Instant timestamp;
    Long userId;
    String operation;
    String eventType;
    Long entityId;

    public ActionDto(final Instant  timestamp, final Long userId, final String operation,
                  final String eventType, final Long entityId
    ) {
        this.entityId = entityId;
        this.eventType = eventType;
        this.operation = operation;
        this.userId = userId;
        this.timestamp = timestamp;
    }
}
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
}
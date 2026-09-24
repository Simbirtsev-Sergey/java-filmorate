package ru.yandex.practicum.filmorate.exception;

public class ExcessiveActionException extends RuntimeException {
    public ExcessiveActionException(String message) {
        super(message);
    }
}

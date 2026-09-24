package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import ru.yandex.practicum.filmorate.model.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.ExcessiveActionException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

@RestControllerAdvice(value = "ru.yandex.practicum.filmorate.controller")
public class ErrorHandler {

    @ExceptionHandler({ValidationException.class, ExcessiveActionException.class, ConditionsNotMetException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(final RuntimeException e) {
        return new ErrorResponse("Validation error", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleInternalError(final NotFoundException e) {
        return new ErrorResponse("Not found", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleConditionsNotMet(final RuntimeException e) {
        return new ErrorResponse("Internal error", e.getMessage());
    }
}

package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.action.ActionDto;
import ru.yandex.practicum.filmorate.model.Action;

public class ActionMapper {
    public static ActionDto mapToActionDto(Action action) {
        ActionDto actionDto = new ActionDto();

        actionDto.setEventId(action.getEventId());
        actionDto.setTimestamp(action.getTimestamp());
        actionDto.setUserId(action.getUserId());
        actionDto.setOperation(convertOperation(action.getOperationId()));
        actionDto.setEventType(convertEventType(action.getEventTypeId()));
        actionDto.setEntityId(actionDto.getEntityId());

        return actionDto;
    }

    private static String convertOperation(final Long operationId) {
        if (operationId == 1L)
            return "REMOVE";
        else if (operationId == 2L)
            return "ADD";
        else
            return "UPDATE";
    }

    private static String convertEventType(final Long eventTypeId) {
        if (eventTypeId == 1L)
            return "LIKE";
        else if (eventTypeId == 2L)
            return "REVIEW";
        else
            return "FRIEND";
    }
}

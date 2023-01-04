package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.RequestDto;
import ru.practicum.explorewithme.model.Request;

public class RequestMapper {
    public static RequestDto toRequestDto(Request request) {
        return new RequestDto(request.getId(),
                request.getCreated(),
                request.getEvent().getId(),
                request.getRequester().getId(),
                request.getStatus());
    }
}

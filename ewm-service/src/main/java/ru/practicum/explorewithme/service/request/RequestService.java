package ru.practicum.explorewithme.service.request;

import ru.practicum.explorewithme.dto.RequestDto;

import java.util.List;

public interface RequestService {

    RequestDto createRequest(Long userId, Long eventId);

    List<RequestDto> getRequestsByUserId(Long userId);

    RequestDto cancelRequest(Long userId, Long requestId);

    List<RequestDto> getRequestByEventIdAndUserId(Long userId, Long eventId);
}

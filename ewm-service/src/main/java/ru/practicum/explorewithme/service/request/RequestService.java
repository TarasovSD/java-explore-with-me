package ru.practicum.explorewithme.service.request;

import ru.practicum.explorewithme.dto.RequestDto;

import java.util.List;

public interface RequestService {

    RequestDto create(Long userId, Long eventId);

    List<RequestDto> getByUserId(Long userId);

    RequestDto cancel(Long userId, Long requestId);

    List<RequestDto> getRequestByEventIdAndUserId(Long userId, Long eventId);
}

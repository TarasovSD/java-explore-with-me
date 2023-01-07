package ru.practicum.explorewithme.service.event;

import org.springframework.data.domain.PageRequest;
import ru.practicum.explorewithme.dto.RequestDto;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventUpdateDto;
import ru.practicum.explorewithme.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto create(EventDto eventDto, Long userId);

    List<EventFullDto> getByUserId(Long userId, PageRequest pageRequest);

    EventFullDto publish(Long eventId);

    EventFullDto getById(Long eventId);

    EventFullDto confirmRequest(Long eventId);

    List<EventFullDto> get(List<Long> users, List<Status> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest pageRequest);

    EventFullDto redact(EventDto eventDto, Long eventId);

    EventFullDto update(EventUpdateDto eventDto, Long userId);

    EventFullDto getByIdByUser(Long userId, Long eventId);

    EventFullDto cancel(Long eventId, Long userId);

    RequestDto confirmRequest(Long eventId, Long userId, Long reqId);

    RequestDto rejectRequest(Long eventId, Long userId, Long reqId);

    List<EventFullDto> getByFilter(String text, List<Long> categories, List<Boolean> paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, PageRequest pageRequest);
}

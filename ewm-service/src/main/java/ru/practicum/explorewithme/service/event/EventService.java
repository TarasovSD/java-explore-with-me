package ru.practicum.explorewithme.service.event;

import org.springframework.data.domain.PageRequest;
import ru.practicum.explorewithme.dto.RequestDto;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventUpdateDto;
import ru.practicum.explorewithme.model.Status;

import java.util.List;

public interface EventService {
    EventFullDto createEvent(EventDto eventDto, Long userId);

    List<EventFullDto> getEventsByUserId(Long userId, PageRequest pageRequest);

    EventFullDto publishEvent(Long eventId);

    EventFullDto getEventById(Long eventId);

    EventFullDto rejectEvent(Long eventId);

    List<EventFullDto> getEvents(List<Long> users, List<Status> states, List<Long> categories, String rangeStart, String rangeEnd, PageRequest pageRequest);

    EventFullDto redactEvent(EventDto eventDto, Long eventId);

    EventFullDto updateEvent(EventUpdateDto eventDto, Long userId);

    EventFullDto getEventByIdByUser(Long userId, Long eventId);

    EventFullDto cancelEvent(Long eventId, Long userId);

    RequestDto rejectOrConfirmRequest(Long eventId, Long userId, Long reqId, Boolean isConfirm);

    List<EventFullDto> getEventsByFilter(String text, List<Long> categories, List<Boolean> paid, String rangeStart, String rangeEnd, Boolean onlyAvailable, String sort, PageRequest pageRequest);
}

package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.model.User;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.model.Location;
import ru.practicum.explorewithme.model.Status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventMapper {
    public static Event toEvent(Long eventId, EventDto eventDto, Location location, LocalDateTime creationOn, User initiatorId,
                                Long confirmedRequests, Long views, LocalDateTime publishedOn, Status state) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime eventDate = LocalDateTime.parse(eventDto.getEventDate(), dateTimeFormatter);
        return new Event(eventId,
                eventDto.getAnnotation(),
                eventDto.getCategory(),
                eventDto.getDescription(),
                eventDate,
                location,
                confirmedRequests,
                eventDto.getPaid(),
                eventDto.getParticipantLimit(),
                eventDto.getRequestModeration(),
                eventDto.getTitle(),
                creationOn,
                initiatorId,
                publishedOn,
                state,
                views);
    }

    public static EventFullDto toEventFullDto(Event event, EventFullDto.CategoryDtoForEvent categoryDtoForEvent,
                                              User user, Location location) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String eventDate = event.getEventDate().format(formatter);
        return new EventFullDto(event.getId(),
                event.getAnnotation(),
                categoryDtoForEvent,
                event.getConfirmedRequests(),
                event.getCreatedOn(),
                event.getDescription(),
                eventDate,
                new EventFullDto.UserShortDtoForEvent(user.getId(), user.getName()),
                new EventFullDto.LocationDtoForEvent(location.getLat(), location.getLon()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                event.getViews());
    }

    public static Event toEventFromEventFullDto(EventFullDto eventDto, Location location, User user) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime eventDate = LocalDateTime.parse(eventDto.getEventDate(), dateTimeFormatter);
        return new Event(eventDto.getId(),
                eventDto.getAnnotation(),
                eventDto.getCategory().getId(),
                eventDto.getDescription(),
                eventDate,
                location,
                eventDto.getConfirmedRequests(),
                eventDto.getPaid(),
                eventDto.getParticipantLimit(),
                eventDto.getRequestModeration(),
                eventDto.getTitle(),
                eventDto.getCreatedOn(),
                user,
                eventDto.getPublishedOn(),
                eventDto.getState(),
                eventDto.getViews());
    }
}

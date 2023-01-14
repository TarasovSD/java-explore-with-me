package ru.practicum.explorewithme.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.model.*;

import java.time.LocalDateTime;

@UtilityClass
public class EventMapper {

    public Event toEvent(Long eventId, EventDto eventDto, Location location, LocalDateTime creationOn, User initiatorId,
                         LocalDateTime publishedOn, Status state, Category category) {
        return new Event(eventId,
                eventDto.getAnnotation(),
                category,
                eventDto.getDescription(),
                eventDto.getEventDate(),
                location,
                eventDto.getPaid(),
                eventDto.getParticipantLimit(),
                eventDto.getRequestModeration(),
                eventDto.getTitle(),
                creationOn,
                initiatorId,
                publishedOn,
                state);
    }

    public EventFullDto toEventFullDto(Event event, EventFullDto.CategoryDtoForEvent categoryDtoForEvent,
                                       User user, Location location, Long numberOfConfirmedRequests, Long numberOfViews) {
        return new EventFullDto(event.getId(),
                event.getAnnotation(),
                categoryDtoForEvent,
                numberOfConfirmedRequests,
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                new EventFullDto.UserShortDtoForEvent(user.getId(), user.getName()),
                new EventFullDto.LocationDtoForEvent(location.getLat(), location.getLon()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                numberOfViews);
    }

    public Event toEventFromEventFullDto(EventFullDto eventDto, Location location, User user, Category category) {
        return new Event(eventDto.getId(),
                eventDto.getAnnotation(),
                category,
                eventDto.getDescription(),
                eventDto.getEventDate(),
                location,
                eventDto.getPaid(),
                eventDto.getParticipantLimit(),
                eventDto.getRequestModeration(),
                eventDto.getTitle(),
                eventDto.getCreatedOn(),
                user,
                eventDto.getPublishedOn(),
                eventDto.getState());
    }
}

package ru.practicum.explorewithme.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.RequestDto;
import ru.practicum.explorewithme.dto.event.EventUpdateDto;
import ru.practicum.explorewithme.exceptions.*;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.mapper.RequestMapper;
import ru.practicum.explorewithme.model.*;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.repository.*;
import ru.practicum.explorewithme.dto.event.EventDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    public EventServiceImpl(EventRepository eventRepository, LocationRepository locationRepository, CategoryRepository categoryRepository, UserRepository userRepository, RequestRepository requestRepository) {
        this.eventRepository = eventRepository;
        this.locationRepository = locationRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    @Transactional
    public EventFullDto createEvent(EventDto eventDto, Long userId) {
        LocalDateTime creationOn = LocalDateTime.now();
        Location location = locationRepository.save(new Location(1L,
                eventDto.getLocation().getLat(), eventDto.getLocation().getLon()));
        User foundUser = userRepository.findById(userId).get();
        Event eventToSave = EventMapper.toEvent(1L, eventDto, location, creationOn, foundUser, 0L,
                0L, null, Status.PENDING);
        Event savedEvent = eventRepository.save(eventToSave);
        Category category = categoryRepository.findById(eventDto.getCategory()).get();
        EventFullDto.CategoryDtoForEvent categoryDtoForEvent = new EventFullDto.CategoryDtoForEvent(category.getId(),
                category.getName());
        User user = userRepository.findById(userId).get();
        return EventMapper.toEventFullDto(savedEvent, categoryDtoForEvent, user, location);
    }

    @Override
    public List<EventFullDto> getEventsByUserId(Long userId, PageRequest pageRequest) {
        User foundUser = userRepository.findById(userId).get();
        List<Event> events = eventRepository.findEventByInitiatorId(foundUser, pageRequest);
        List<EventFullDto> eventsDto = new ArrayList<>();
        for (Event event : events) {
            EventFullDto.CategoryDtoForEvent categoryDtoForEvent = getCategoryDtoForEvent(categoryRepository.findById(event.getCategory()));
            User user = userRepository.findById(event.getInitiatorId().getId()).get();
            Location location = event.getLocation();
            EventFullDto eventFullDto = EventMapper.toEventFullDto(event, categoryDtoForEvent, user, location);
            eventsDto.add(eventFullDto);
        }
        return eventsDto;
    }

    @Override
    @Transactional
    public EventFullDto publishEvent(Long eventId) {
        EventFullDto eventFullDtoForPublish = getEventById(eventId);
        Optional<Event> eventForPublishOpt = eventRepository.findById(eventId);
        Event eventForPublish;
        if (eventForPublishOpt.isPresent()) {
            eventForPublish = eventForPublishOpt.get();
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }
        LocalDateTime publishedOn = LocalDateTime.now();
        eventForPublish.setPublishedOn(publishedOn);
        eventForPublish.setState(Status.PUBLISHED);
        return getEventFullDto(eventFullDtoForPublish, eventForPublish);
    }

    @Override
    @Transactional
    public EventFullDto getEventById(Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        EventFullDto eventFullDto;
        if (event.isPresent()) {
            EventFullDto.CategoryDtoForEvent categoryDtoForEvent =
                    getCategoryDtoForEvent(categoryRepository.findById(event.get().getCategory()));
            User user = userRepository.findById(event.get().getInitiatorId().getId()).get();
            Location location = event.get().getLocation();
            eventFullDto = EventMapper.toEventFullDto(event.get(), categoryDtoForEvent, user, location);
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }
        Event eventForUpdate = event.get();
        eventForUpdate.setViews(eventForUpdate.getViews() + 1);
        eventRepository.save(eventForUpdate);
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto rejectEvent(Long eventId) {
        EventFullDto eventFullDtoForPublish = getEventById(eventId);
        Optional<Event> eventForPublishOpt = eventRepository.findById(eventId);
        Event eventForPublish;
        if (eventForPublishOpt.isPresent()) {
            eventForPublish = eventForPublishOpt.get();
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }
        LocalDateTime publishedOn = LocalDateTime.now();
        eventForPublish.setPublishedOn(publishedOn);
        eventForPublish.setState(Status.CANCELED);
        return getEventFullDto(eventFullDtoForPublish, eventForPublish);
    }

    @Override
    public List<EventFullDto> getEvents(List<Long> users, List<Status> states, List<Long> categories,
                                        String rangeStart, String rangeEnd, PageRequest pageRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (users.isEmpty()) {
            List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                Long userId = user.getId();
                users.add(userId);
            }
        }
        List<User> allUsers = new ArrayList<>();
        for (Long id : users) {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                allUsers.add(user);
            }
        }
        if (states.isEmpty()) {
            states.add(Status.PENDING);
            states.add(Status.PUBLISHED);
            states.add(Status.CANCELED);
        }
        if (categories.isEmpty()) {
            List<Category> allCategories = categoryRepository.findAll();
            for (Category category : allCategories) {
                Long categoryId = category.getId();
                categories.add(categoryId);
            }
        }
        if (rangeStart.isBlank()) {
            rangeStart = "2000-01-01 00:00:00";
        }
        if (rangeEnd.isBlank()) {
            rangeEnd = "3000-01-01 00:00:00";
        }
        LocalDateTime start = LocalDateTime.parse(rangeStart, formatter);
        LocalDateTime end = LocalDateTime.parse(rangeEnd, formatter);
        List<Event> events = eventRepository.getEvents(allUsers, states, categories,
                start, end, pageRequest);
        List<EventFullDto> eventsDto = new ArrayList<>();
        for (Event e : events) {
            Optional<Category> categoryOpt = categoryRepository.findById(e.getCategory());
            Category category;
            if (categoryOpt.isPresent()) {
                category = categoryOpt.get();
            } else {
                throw new CategoryNotFoundException("Категория не найдена");
            }
            EventFullDto.CategoryDtoForEvent categoryDtoForEvent = new EventFullDto.CategoryDtoForEvent(category.getId(),
                    category.getName());
            User user = userRepository.findById(e.getInitiatorId().getId()).get();
            eventsDto.add(EventMapper.toEventFullDto(e, categoryDtoForEvent, user, e.getLocation()));
            e.setViews(e.getViews() + 1);
            eventRepository.save(e);
        }
        return eventsDto;
    }

    @Override
    @Transactional
    public EventFullDto redactEvent(EventDto eventDto, Long eventId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Event event;
        if (eventOpt.isPresent()) {
            event = eventOpt.get();
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }
        if (eventDto.getAnnotation() != null) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        Optional<Category> categoryOpt = categoryRepository.findById(eventDto.getCategory());
        Category category;
        if (categoryOpt.isPresent()) {
            category = categoryOpt.get();
        } else {
            throw new CategoryNotFoundException("Категория не найдена");
        }
        if (eventDto.getCategory() != null) {
            event.setCategory(category.getId());
        }
        if (eventDto.getDescription() != null) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime eventDate = LocalDateTime.parse(eventDto.getEventDate(), formatter);
            event.setEventDate(eventDate);
        }
        if (eventDto.getLocation() != null) {
            Location location = locationRepository.save(new Location(1L,
                    eventDto.getLocation().getLat(), eventDto.getLocation().getLon()));
            event.setLocation(location);
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getTitle() != null) {
            event.setTitle(eventDto.getTitle());
        }

        Event updatedEvent = eventRepository.save(event);
        return getEventFullDto(event, updatedEvent, category);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(EventUpdateDto eventDto, Long userId) {
        Optional<Event> eventOpt = eventRepository.findById(eventDto.getEventId());
        Event event;
        if (eventOpt.isPresent()) {
            event = eventOpt.get();
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }

        if (eventDto.getAnnotation() != null) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategory() != null) {
            event.setCategory(eventDto.getCategory());
        }
        if (eventDto.getDescription() != null) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime eventDate = LocalDateTime.parse(eventDto.getEventDate(), formatter);
            event.setEventDate(eventDate);
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getTitle() != null) {
            event.setTitle(eventDto.getTitle());
        }

        Event updatedEvent = eventRepository.save(event);

        Optional<Category> categoryOpt = categoryRepository.findById(event.getCategory());
        Category category;
        if (categoryOpt.isPresent()) {
            category = categoryOpt.get();
        } else {
            throw new CategoryNotFoundException("Категория не найдена");
        }
        return getEventFullDto(event, updatedEvent, category);
    }

    @Override
    public EventFullDto getEventByIdByUser(Long userId, Long eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        Event event;
        if (eventOptional.isPresent()) {
            event = eventOptional.get();
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }
        if (Objects.equals(event.getInitiatorId().getId(), userId)) {
            return mapToEventFullDto(userId, event);
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }
    }

    @Override
    @Transactional
    public EventFullDto cancelEvent(Long eventId, Long userId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        Event event;
        if (eventOptional.isPresent()) {
            event = eventOptional.get();
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }

        if (event.getState() == Status.PENDING) {
            event.setState(Status.CANCELED);
        } else {
            throw new EventHasNoStatusPendingException("Событие должно иметь статус Pending");
        }
        return mapToEventFullDto(userId, event);
    }

    @Override
    public RequestDto rejectOrConfirmRequest(Long eventId, Long userId, Long reqId, Boolean isConfirm) {
        Optional<Request> requestOpt = requestRepository.findById(reqId);
        Request request;
        if (requestOpt.isPresent()) {
            request = requestOpt.get();
        } else {
            throw new RequestNotFoundException("Запрос не найден");
        }

        Optional<Event> eventOptional = eventRepository.findById(eventId);
        Event event;
        if (eventOptional.isPresent()) {
            event = eventOptional.get();
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }

        if (isConfirm) {
            if (!Objects.equals(request.getRequester().getId(), userId)) {
                List<Request> confirmedRequest = requestRepository.findRequestsByStatusAndEvent(Status.CONFIRMED, event);
                if (confirmedRequest.size() < event.getParticipantLimit()) {
                    request.setStatus(Status.CONFIRMED);
                    requestRepository.save(request);
                    Long participantLimit = event.getParticipantLimit() + 1L;
                    event.setParticipantLimit(participantLimit);
                    eventRepository.save(event);
                } else {
                    throw new RequestNotConfirmedException("Превышено число запросов на участие в событии");
                }

            } else {
                throw new RequestNotFoundException("Запрос не найден");
            }
        } else {
            request.setStatus(Status.REJECTED);
            requestRepository.save(request);
        }
        return RequestMapper.toRequestDto(request);
    }

    @Override
    public List<EventFullDto> getEventsByFilter(String text, List<Long> categories, List<Boolean> paid, String rangeStart,
                                                String rangeEnd, Boolean onlyAvailable, String sort,
                                                PageRequest pageRequest) {
        if (rangeStart.isBlank()) {
            rangeStart = "2000-01-01 00:00:00";
        }
        if (rangeEnd.isBlank()) {
            rangeEnd = "3000-01-01 00:00:00";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(rangeStart, formatter);
        LocalDateTime end = LocalDateTime.parse(rangeEnd, formatter);
        List<Event> events;
        if (categories.isEmpty()) {
            List<Category> allCategories = categoryRepository.findAll();
            for (Category category : allCategories) {
                Long categoryId = category.getId();
                categories.add(categoryId);
            }
        }
        if (sort.equals("EVENT_DATE")) {
            events = eventRepository.getEventsByFilterSortByEventDate(categories, paid, start,
                    end, pageRequest);
        } else if (sort.equals("VIEWS")) {
            events = eventRepository.getEventsByFilterSortByViews(categories, paid, start,
                    end, pageRequest);
        } else {
            events = eventRepository.getEventsByFilterWithoutSort(categories, paid, start,
                    end, pageRequest);
        }
        List<EventFullDto> eventsDto = new ArrayList<>();
        if (onlyAvailable) {
            for (Event event : events) {
                List<Request> confirmedRequest = requestRepository.findRequestsByStatusAndEvent(Status.CONFIRMED, event);
                if (event.getAnnotation().toLowerCase().contains(text.toLowerCase()) || event.getDescription().toLowerCase().contains(text.toLowerCase())) {
                    if (confirmedRequest.size() < event.getParticipantLimit()) {
                        eventsDto.add(mapToEventFullDto(event.getInitiatorId().getId(), event));
                    }
                }
            }
        } else {
            for (Event event : events) {
                if (event.getAnnotation().toLowerCase().contains(text.toLowerCase()) || event.getDescription().toLowerCase().contains(text.toLowerCase())) {
                    eventsDto.add(mapToEventFullDto(event.getInitiatorId().getId(), event));
                }
            }
        }
        return eventsDto;
    }

    private EventFullDto mapToEventFullDto(Long userId, Event event) {
        Optional<Category> categoryOpt = categoryRepository.findById(event.getCategory());
        Category category;
        if (categoryOpt.isPresent()) {
            category = categoryOpt.get();
        } else {
            throw new CategoryNotFoundException("Категория не найдена");
        }
        User user;
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            throw new UserNotFoundException("Пользователь не найден");
        }
        return EventMapper.toEventFullDto(event, CategoryMapper.toCategoryDtoForEvent(category), user, event.getLocation());
    }

    private EventFullDto getEventFullDto(Event event, Event updatedEvent, Category category) {
        EventFullDto.CategoryDtoForEvent categoryDtoForEvent = CategoryMapper.toCategoryDtoForEvent(category);
        Optional<User> userOpt = userRepository.findById(event.getInitiatorId().getId());
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            throw new UserNotFoundException("Пользователь не найден");
        }
        return EventMapper.toEventFullDto(updatedEvent, categoryDtoForEvent, user, event.getLocation());
    }

    private EventFullDto getEventFullDto(EventFullDto eventFullDtoForPublish, Event eventForPublish) {
        User user;
        Optional<User> userOpt = userRepository.findById(eventFullDtoForPublish.getInitiator().getId());
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            throw new UserNotFoundException("Пользователь не найден");
        }
        return EventMapper.toEventFullDto(eventRepository.save(eventForPublish), eventFullDtoForPublish.getCategory(),
                user, eventForPublish.getLocation());
    }

    private EventFullDto.CategoryDtoForEvent getCategoryDtoForEvent(Optional<Category> categoryOptional) {
        Category category;
        if (categoryOptional.isPresent()) {
            category = categoryOptional.get();
        } else {
            throw new CategoryNotFoundException("Категория не найдена");
        }
        return new EventFullDto.CategoryDtoForEvent(category.getId(),
                category.getName());
    }
}

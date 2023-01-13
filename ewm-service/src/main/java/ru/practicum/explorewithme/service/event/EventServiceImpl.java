package ru.practicum.explorewithme.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.HitClient;
import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.dto.RequestDto;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventUpdateDto;
import ru.practicum.explorewithme.exceptions.*;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.mapper.LocationMapper;
import ru.practicum.explorewithme.mapper.RequestMapper;
import ru.practicum.explorewithme.model.*;
import ru.practicum.explorewithme.repository.CategoryRepository;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.RequestRepository;
import ru.practicum.explorewithme.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    private final DateTimeFormatter formatter;

    private final HitClient hitClient;

    public EventServiceImpl(EventRepository eventRepository, CategoryRepository categoryRepository, UserRepository userRepository, RequestRepository requestRepository, HitClient hitClient) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.hitClient = hitClient;
    }

    @Override
    @Transactional
    public EventFullDto create(EventDto eventDto, Long userId) {
        LocalDateTime creationOn = LocalDateTime.now();
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        Category category = categoryRepository.findById(eventDto.getCategory())
                .orElseThrow(() -> new EventNotFoundException("Категория не найдена"));
        Event eventToSave = EventMapper.toEvent(1L, eventDto, LocationMapper.toLocation(eventDto.getLocation()),
                creationOn, foundUser, null, Status.PENDING, category);
        Event savedEvent = eventRepository.save(eventToSave);
        EventFullDto.CategoryDtoForEvent categoryDtoForEvent = CategoryMapper.toCategoryDtoForEvent(category);
        Location location = eventToSave.getLocation();
        return EventMapper.toEventFullDto(savedEvent, categoryDtoForEvent, foundUser, location, 0L, 0L);
    }

    @Override
    public List<EventFullDto> getByUserId(Long userId, PageRequest pageRequest) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        List<Event> events = eventRepository.findEventByInitiatorId(foundUser, pageRequest);
        List<EventFullDto> eventsDto = new ArrayList<>();
        List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvents(Status.CONFIRMED, events);
        List<HitDto> hits = hitClient.hits(events);
        for (Event event : events) {
            Category category = event.getCategory();
            EventFullDto.CategoryDtoForEvent categoryDtoForEvent = CategoryMapper.toCategoryDtoForEvent(category);
            Location location = event.getLocation();
            List<Request> confirmedEventRequests = new ArrayList<>();
            for (Request request : confirmedRequests) {
                if (event.equals(request.getEvent())) {
                    confirmedEventRequests.add(request);
                }
            }
            long numberOfConfirmedRequests = confirmedEventRequests.size();
            List<HitDto> eventHits = new ArrayList<>();
            for (HitDto hitDto : hits) {
                String uri = "/events/" + event.getId();
                if (hitDto.getUri().equals(uri)) {
                    eventHits.add(hitDto);
                }
            }
            long numberOfViews = eventHits.size();
            EventFullDto eventFullDto = EventMapper.toEventFullDto(event, categoryDtoForEvent, foundUser, location, numberOfConfirmedRequests, numberOfViews);
            eventsDto.add(eventFullDto);
        }
        return eventsDto;
    }

    @Override
    @Transactional
    public EventFullDto publish(Long eventId) {
        EventFullDto eventFullDtoForPublish = getById(eventId);
        Event eventForPublish = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
        LocalDateTime publishedOn = LocalDateTime.now();
        eventForPublish.setPublishedOn(publishedOn);
        eventForPublish.setState(Status.PUBLISHED);
        return getEventFullDto(eventFullDtoForPublish, eventForPublish);
    }

    @Override
    @Transactional
    public EventFullDto getById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
        EventFullDto eventFullDto;
        EventFullDto.CategoryDtoForEvent categoryDtoForEvent = CategoryMapper.toCategoryDtoForEvent(event.getCategory());
        User user = event.getInitiatorId();
        Location location = event.getLocation();
        List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvent(Status.CONFIRMED, event);
        long numberOfConfirmedRequests = confirmedRequests.size();
        List<HitDto> hits = hitClient.hits(List.of(event));
        long numberOfViews = hits.size();
        eventFullDto = EventMapper
                .toEventFullDto(event, categoryDtoForEvent, user, location, numberOfConfirmedRequests, numberOfViews);
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto rejectRequest(Long eventId) {
        EventFullDto eventFullDtoForPublish = getById(eventId);
        Event eventForPublish = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
        LocalDateTime publishedOn = LocalDateTime.now();
        eventForPublish.setPublishedOn(publishedOn);
        eventForPublish.setState(Status.CANCELED);
        return getEventFullDto(eventFullDtoForPublish, eventForPublish);
    }

    @Override
    public List<EventFullDto> get(List<Long> users, List<Status> states, List<Long> categories,
                                  LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest pageRequest) {
        if (users.isEmpty()) {
            List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                Long userId = user.getId();
                users.add(userId);
            }
        }
        List<User> allUsers = userRepository.getUsersById(users);
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
        if (rangeStart == null) {
            rangeStart = LocalDateTime.parse("2000-01-01 00:00:00", formatter);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.parse("3000-01-01 00:00:00", formatter);
        }
        List<Event> events = eventRepository.getEvents(allUsers, states, categories,
                rangeStart, rangeEnd, pageRequest);
        List<EventFullDto> eventsDto = new ArrayList<>();
        List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvents(Status.CONFIRMED, events);
        List<HitDto> hits = hitClient.hits(events);
        for (Event e : events) {
            Category category = e.getCategory();
            EventFullDto.CategoryDtoForEvent categoryDtoForEvent = CategoryMapper.toCategoryDtoForEvent(category);
            User user = e.getInitiatorId();
            Location location = e.getLocation();
            List<Request> confirmedEventRequests = new ArrayList<>();
            for (Request request : confirmedRequests) {
                if (e.equals(request.getEvent())) {
                    confirmedEventRequests.add(request);
                }
            }
            long numberOfConfirmedRequests = confirmedEventRequests.size();
            List<HitDto> eventHits = new ArrayList<>();
            for (HitDto hitDto : hits) {
                String uri = "/events/" + e.getId();
                if (hitDto.getUri().equals(uri)) {
                    eventHits.add(hitDto);
                }
            }
            long numberOfViews = eventHits.size();
            eventsDto.add(EventMapper.toEventFullDto(e, categoryDtoForEvent, user, location, numberOfConfirmedRequests, numberOfViews));
        }
        return eventsDto;
    }

    @Override
    @Transactional
    public EventFullDto redact(EventDto eventDto, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
        Category category = categoryRepository.findById(eventDto.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));
        if (eventDto.getAnnotation() != null || !eventDto.getAnnotation().isBlank()) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategory() != null) {
            event.setCategory(category);
        }
        if (eventDto.getDescription() != null || !eventDto.getDescription().isBlank()) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            LocalDateTime eventDate = eventDto.getEventDate();
            event.setEventDate(eventDate);
        }
        if (eventDto.getLocation() != null) {
            event.setLocation(LocationMapper.toLocation(eventDto.getLocation()));
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
        if (eventDto.getTitle() != null || eventDto.getTitle().isBlank()) {
            event.setTitle(eventDto.getTitle());
        }
        Event updatedEvent = eventRepository.save(event);
        return getEventFullDto(updatedEvent);
    }

    @Override
    @Transactional
    public EventFullDto update(EventUpdateDto eventDto, Long userId) {
        Event event = eventRepository.findById(eventDto.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));

        if (eventDto.getAnnotation() != null || !eventDto.getAnnotation().isBlank()) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategory() != null) {
            Category category = categoryRepository.findById(eventDto.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));
            event.setCategory(category);
        }
        if (eventDto.getDescription() != null || !eventDto.getDescription().isBlank()) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(eventDto.getEventDate(), formatter);
            event.setEventDate(eventDate);
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getTitle() != null || !event.getTitle().isBlank()) {
            event.setTitle(eventDto.getTitle());
        }

        Event updatedEvent = eventRepository.save(event);
        return getEventFullDto(updatedEvent);
    }

    @Override
    public EventFullDto getByIdByUser(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
        if (Objects.equals(event.getInitiatorId().getId(), userId)) {
            return mapToEventFullDto(userId, event);
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }
    }

    @Override
    @Transactional
    public EventFullDto cancel(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
        if (event.getState() == Status.PENDING) {
            event.setState(Status.CANCELED);
        } else {
            throw new EventHasNoStatusPendingException("Событие должно иметь статус Pending");
        }
        return mapToEventFullDto(userId, event);
    }

    @Override
    @Transactional
    public RequestDto confirmRequest(Long eventId, Long userId, Long reqId) {
        Request request = requestRepository.findById(reqId)
                .orElseThrow(() -> new RequestNotFoundException("Запрос не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
        if (!event.getInitiatorId().getId().equals(userId)) {
            throw new RequestNotConfirmedException("Подтвердить запрос на участие в событии может только инициатор события");
        }
        if (!eventId.equals(request.getEvent().getId())) {
            throw new RequestNotConfirmedException("Идентификатор события не соответствует идентификатору события в запросе на подтверждение");
        }
        if (event.getParticipantLimit() == 0 || !request.getEvent().getRequestModeration()) {
            throw new RequestNotConfirmedException("Подтверждение на участие в событии не требуется");
        }
        if (!Objects.equals(request.getRequester().getId(), userId)) {
            Long confirmedRequest = requestRepository.getRequestsCountByStatusAndEvent(Status.CONFIRMED, event);
            if (event.getParticipantLimit() > 0 && confirmedRequest < event.getParticipantLimit()) {
                request.setStatus(Status.CONFIRMED);
            } else {
                throw new RequestNotConfirmedException("Превышено число запросов на участие в событии");
            }
            if (event.getParticipantLimit() <= ++confirmedRequest) {
                List<Request> requestsForReject = requestRepository.findRequestsByStatusAndEvent(Status.PENDING, event);
                for (Request requestForReject : requestsForReject) {
                    requestForReject.setStatus(Status.REJECTED);
                }
            }
        } else {
            throw new RequestNotFoundException("Запрос не найден");
        }
        return RequestMapper.toRequestDto(request);
    }

    @Override
    @Transactional
    public RequestDto rejectRequest(Long eventId, Long userId, Long reqId) {
        Request request = requestRepository.findById(reqId)
                .orElseThrow(() -> new RequestNotFoundException("Запрос не найден"));
        if (!Objects.equals(request.getRequester().getId(), userId)) {
            request.setStatus(Status.REJECTED);
        } else {
            throw new RequestNotFoundException("Запрос не найден");
        }
        return RequestMapper.toRequestDto(request);
    }

    @Override
    public List<EventFullDto> getByFilter(String text, List<Long> categories, List<Boolean> paid, LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd, Boolean onlyAvailable, String sort,
                                          PageRequest pageRequest) {
        if (rangeStart == null) {
            rangeStart = LocalDateTime.parse("2000-01-01 00:00:00", formatter);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.parse("3000-01-01 00:00:00", formatter);
        }
        List<Event> events;
        if (categories.isEmpty()) {
            List<Category> allCategories = categoryRepository.findAll();
            for (Category category : allCategories) {
                Long categoryId = category.getId();
                categories.add(categoryId);
            }
        }
        if (sort.equals("EVENT_DATE")) {
            events = eventRepository.getEventsByFilterSortByEventDate(categories, paid, rangeStart,
                    rangeEnd, pageRequest);
        } else if (sort.equals("VIEWS")) {
            List<Event> foundEvents = eventRepository.getEventsByFilterWithoutSort(categories, paid, rangeStart,
                    rangeEnd, pageRequest);
            List<HitDto> hits = hitClient.hits(foundEvents);
            List<String> uris = new ArrayList<>();
            for (HitDto hitDto : hits) {
                uris.add(hitDto.getUri());
            }
            Map<Long, Event> eventViews = new TreeMap<>(Comparator.reverseOrder());
            for (Event event : foundEvents) {
                List<String> eventUris = new ArrayList<>();
                for (String uri : uris) {
                    if (uri.equals("event/" + event.getId())) {
                        eventUris.add(uri);
                    }
                }
                long numberOfViews;
                numberOfViews = eventUris.size();
                eventViews.put(numberOfViews, event);
            }
            events = new ArrayList<>(eventViews.values());
        } else {
            events = eventRepository.getEventsByFilterWithoutSort(categories, paid, rangeStart,
                    rangeEnd, pageRequest);
        }
        List<EventFullDto> eventsDto = new ArrayList<>();
        List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvents(Status.CONFIRMED, events);
        List<HitDto> hits = hitClient.hits(events);
        if (onlyAvailable) {
            for (Event event : events) {
                List<Request> confirmedEventRequests = new ArrayList<>();
                for (Request request : confirmedRequests) {
                    if (event.equals(request.getEvent())) {
                        confirmedEventRequests.add(request);
                    }
                }
                if (event.getAnnotation().toLowerCase().contains(text.toLowerCase())
                        || event.getDescription().toLowerCase().contains(text.toLowerCase())) {
                    if (confirmedEventRequests.size() < event.getParticipantLimit()) {
                        List<HitDto> eventHits = new ArrayList<>();
                        for (HitDto hitDto : hits) {
                            String uri = "/events/" + event.getId();
                            if (hitDto.getUri().equals(uri)) {
                                eventHits.add(hitDto);
                            }
                        }
                        eventsDto.add(EventMapper.toEventFullDto(event,
                                CategoryMapper.toCategoryDtoForEvent(event.getCategory()),
                                event.getInitiatorId(),
                                event.getLocation(), (long) confirmedEventRequests.size(), (long) eventHits.size()));
                    }
                }
            }
        } else {
            for (Event event : events) {
                List<HitDto> eventHits = new ArrayList<>();
                for (HitDto hitDto : hits) {
                    String uri = "/events/" + event.getId();
                    if (hitDto.getUri().equals(uri)) {
                        eventHits.add(hitDto);
                    }
                }
                List<Request> confirmedEventRequests = new ArrayList<>();
                for (Request request : confirmedRequests) {
                    if (event.equals(request.getEvent())) {
                        confirmedEventRequests.add(request);
                    }
                }
                if (event.getAnnotation().toLowerCase().contains(text.toLowerCase()) || event.getDescription().toLowerCase().contains(text.toLowerCase())) {
                    eventsDto.add(EventMapper.toEventFullDto(event,
                            CategoryMapper.toCategoryDtoForEvent(event.getCategory()),
                            event.getInitiatorId(),
                            event.getLocation(), (long) confirmedEventRequests.size(), (long) eventHits.size()));
                }
            }
        }
        return eventsDto;
    }

    private EventFullDto mapToEventFullDto(Long userId, Event event) {
        Category category = event.getCategory();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        Location location = event.getLocation();
        List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvent(Status.CONFIRMED, event);
        long numberOfConfirmedRequests = confirmedRequests.size();
        List<HitDto> hits = hitClient.hits(List.of(event));
        long numberOfViews = hits.size();
        return EventMapper.toEventFullDto(event, CategoryMapper.toCategoryDtoForEvent(category), user, location, numberOfConfirmedRequests, numberOfViews);
    }

    private EventFullDto getEventFullDto(Event updatedEvent) {
        EventFullDto.CategoryDtoForEvent categoryDtoForEvent = CategoryMapper.toCategoryDtoForEvent(updatedEvent.getCategory());
        User user = updatedEvent.getInitiatorId();
        Location location = updatedEvent.getLocation();
        List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvent(Status.CONFIRMED, updatedEvent);
        long numberOfConfirmedRequests = confirmedRequests.size();
        List<HitDto> hits = hitClient.hits(List.of(updatedEvent));
        long numberOfViews = hits.size();
        return EventMapper.toEventFullDto(updatedEvent, categoryDtoForEvent, user, location, numberOfConfirmedRequests, numberOfViews);
    }

    private EventFullDto getEventFullDto(EventFullDto eventFullDtoForPublish, Event eventForPublish) {
        User user = eventForPublish.getInitiatorId();
        Location location = eventForPublish.getLocation();
        List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvent(Status.CONFIRMED, eventForPublish);
        long numberOfConfirmedRequests = confirmedRequests.size();
        List<HitDto> hits = hitClient.hits(List.of(eventForPublish));
        long numberOfViews = hits.size();
        return EventMapper.toEventFullDto(eventRepository.save(eventForPublish), eventFullDtoForPublish.getCategory(),
                user, location, numberOfConfirmedRequests, numberOfViews);
    }
}

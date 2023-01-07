package ru.practicum.explorewithme.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.dto.RequestDto;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventUpdateDto;
import ru.practicum.explorewithme.exceptions.*;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.mapper.EventMapper;
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

    private final WebClient webClient;

    DateTimeFormatter formatter;

    public EventServiceImpl(EventRepository eventRepository, CategoryRepository categoryRepository, UserRepository userRepository, RequestRepository requestRepository, WebClient.Builder builder) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.webClient = builder.baseUrl("http://stats-server:9090/").build();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    @Transactional
    public EventFullDto create(EventDto eventDto, Long userId) {
        LocalDateTime creationOn = LocalDateTime.now();
        User foundUser = userRepository.findById(userId).get();
        Event eventToSave = EventMapper.toEvent(1L, eventDto, eventDto.getLocation().getLat(), eventDto.getLocation().getLon(), creationOn, foundUser,
                null, Status.PENDING);
        Event savedEvent = eventRepository.save(eventToSave);
        Category category = categoryRepository.findById(eventDto.getCategory()).get();
        EventFullDto.CategoryDtoForEvent categoryDtoForEvent = new EventFullDto.CategoryDtoForEvent(category.getId(),
                category.getName());
        User user = userRepository.findById(userId).get();
        Location location = new Location(eventToSave.getLat(), eventToSave.getLon());
        return EventMapper.toEventFullDto(savedEvent, categoryDtoForEvent, user, location, 0L, 0L);
    }

    @Override
    public List<EventFullDto> getByUserId(Long userId, PageRequest pageRequest) {
        User foundUser = userRepository.findById(userId).get();
        List<Event> events = eventRepository.findEventByInitiatorId(foundUser, pageRequest);
        List<EventFullDto> eventsDto = new ArrayList<>();
        List<Category> categories = categoryRepository.findAll();
        for (Event event : events) {
            Category category = null;
            for (Category foundCategory : categories) {
                if (foundCategory.getId().equals(event.getCategory())) {
                    category = foundCategory;
                }
            }
            if (category == null) {
                throw new CategoryNotFoundException("Категория не найдена");
            }
            EventFullDto.CategoryDtoForEvent categoryDtoForEvent = getCategoryDtoForEvent(Optional.of(category));
            Location location = new Location(event.getLat(), event.getLon());
            List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvent(Status.CONFIRMED, event);
            long numberOfConfirmedRequests = confirmedRequests.size();
            String eventId = event.getId().toString();
            HitDto[] hits = webClient
                    .get()
                    .uri("/stats?start=2000-01-01 00:00:00&end=3000-01-01 00:00:00&uris=/events/{eventId}&unique=false", eventId)
                    .retrieve().bodyToMono(HitDto[].class)
                    .block();
            long numberOfViews;
            if (hits != null) {
                numberOfViews = hits.length;
            } else {
                numberOfViews = 0;
            }
            EventFullDto eventFullDto = EventMapper.toEventFullDto(event, categoryDtoForEvent, foundUser, location, numberOfConfirmedRequests, numberOfViews);
            eventsDto.add(eventFullDto);
        }
        return eventsDto;
    }

    @Override
    @Transactional
    public EventFullDto publish(Long eventId) {
        EventFullDto eventFullDtoForPublish = getById(eventId);
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
    public EventFullDto getById(Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        EventFullDto eventFullDto;
        if (event.isPresent()) {
            EventFullDto.CategoryDtoForEvent categoryDtoForEvent =
                    getCategoryDtoForEvent(categoryRepository.findById(event.get().getCategory()));
            User user = userRepository.findById(event.get().getInitiatorId().getId()).get();
            Location location = new Location(event.get().getLat(), event.get().getLon());
            List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvent(Status.CONFIRMED, event.get());
            long numberOfConfirmedRequests = confirmedRequests.size();
            String foundEventId = eventId.toString();
            HitDto[] hits = webClient
                    .get()
                    .uri("/stats?start=2000-01-01 00:00:00&end=3000-01-01 00:00:00&uris=/events/{foundEventId}&unique=false", foundEventId)
                    .retrieve().bodyToMono(HitDto[].class)
                    .block();
            long numberOfViews;
            if (hits != null) {
                numberOfViews = hits.length;
            } else {
                numberOfViews = 0;
            }
            eventFullDto = EventMapper.toEventFullDto(event.get(), categoryDtoForEvent, user, location, numberOfConfirmedRequests, numberOfViews);
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto confirmRequest(Long eventId) {
        EventFullDto eventFullDtoForPublish = getById(eventId);
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
    public List<EventFullDto> get(List<Long> users, List<Status> states, List<Long> categories,
                                  LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest pageRequest) {
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
        if (rangeStart == null) {
            rangeStart = LocalDateTime.parse("2000-01-01 00:00:00", formatter);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.parse("3000-01-01 00:00:00", formatter);
        }
        List<Event> events = eventRepository.getEvents(allUsers, states, categories,
                rangeStart, rangeEnd, pageRequest);
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
            Location location = new Location(e.getLat(), e.getLon());
            List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvent(Status.CONFIRMED, e);
            long numberOfConfirmedRequests = confirmedRequests.size();
            String eventId = e.getId().toString();
            HitDto[] hits = webClient
                    .get()
                    .uri("/stats?start=2000-01-01 00:00:00&end=3000-01-01 00:00:00&uris=/events/{eventId}&unique=false", eventId)
                    .retrieve().bodyToMono(HitDto[].class)
                    .block();
            long numberOfViews;
            if (hits != null) {
                numberOfViews = hits.length;
            } else {
                numberOfViews = 0;
            }
            eventsDto.add(EventMapper.toEventFullDto(e, categoryDtoForEvent, user, location, numberOfConfirmedRequests, numberOfViews));
        }
        return eventsDto;
    }

    @Override
    @Transactional
    public EventFullDto redact(EventDto eventDto, Long eventId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Event event;
        if (eventOpt.isPresent()) {
            event = eventOpt.get();
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }
        if (eventDto.getAnnotation() != null || !eventDto.getAnnotation().isBlank()) {
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
        if (eventDto.getDescription() != null || !eventDto.getDescription().isBlank()) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getEventDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime eventDate = LocalDateTime.parse(eventDto.getEventDate(), formatter);
            event.setEventDate(eventDate);
        }
        if (eventDto.getLocation() != null) {
            event.setLat(eventDto.getLocation().getLat());
            event.setLon(eventDto.getLocation().getLon());
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
        return getEventFullDto(event, updatedEvent, category);
    }

    @Override
    @Transactional
    public EventFullDto update(EventUpdateDto eventDto, Long userId) {
        Event event = eventRepository.findById(eventDto.getEventId()).orElseThrow(() -> new EventNotFoundException("Событие не найдено"));

        if (eventDto.getAnnotation() != null || !eventDto.getAnnotation().isBlank()) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategory() != null) {
            event.setCategory(eventDto.getCategory());
        }
        if (eventDto.getDescription() != null || !eventDto.getDescription().isBlank()) {
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
        if (eventDto.getTitle() != null || !event.getTitle().isBlank()) {
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
    public EventFullDto getByIdByUser(Long userId, Long eventId) {
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
    public EventFullDto cancel(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
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
        Request request = requestRepository.findById(reqId).orElseThrow(() -> new RequestNotFoundException("Запрос не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
        if (!Objects.equals(request.getRequester().getId(), userId)) {
            List<Request> confirmedRequest = requestRepository.findRequestsByStatusAndEvent(Status.CONFIRMED, event);
            if (confirmedRequest.size() < event.getParticipantLimit()) {
                request.setStatus(Status.CONFIRMED);
                requestRepository.save(request);
            } else {
                throw new RequestNotConfirmedException("Превышено число запросов на участие в событии");
            }
        } else {
            throw new RequestNotFoundException("Запрос не найден");
        }
        return RequestMapper.toRequestDto(request);
    }

    @Override
    @Transactional
    public RequestDto rejectRequest(Long eventId, Long userId, Long reqId) {
        Optional<Request> requestOpt = requestRepository.findById(reqId);
        Request request;
        if (requestOpt.isPresent()) {
            request = requestOpt.get();
        } else {
            throw new RequestNotFoundException("Запрос не найден");
        }

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
            HitDto[] hits = webClient
                    .get()
                    .uri("/stats?start=2000-01-01 00:00:00&end=3000-01-01 00:00:00&uris=&unique=false")
                    .retrieve().bodyToMono(HitDto[].class)
                    .block();
            List<String> uris = new ArrayList<>();
            if (hits != null) {
                for (HitDto hitDto : hits) {
                    uris.add(hitDto.getUri());
                }
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
        Location location = new Location(event.getLat(), event.getLon());
        List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvent(Status.CONFIRMED, event);
        long numberOfConfirmedRequests = confirmedRequests.size();
        String eventId = event.getId().toString();
        HitDto[] hits = webClient
                .get()
                .uri("/stats?start=2000-01-01 00:00:00&end=3000-01-01 00:00:00&uris=/events/{eventId}&unique=false", eventId)
                .retrieve().bodyToMono(HitDto[].class)
                .block();
        long numberOfViews;
        if (hits != null) {
            numberOfViews = hits.length;
        } else {
            numberOfViews = 0;
        }
        return EventMapper.toEventFullDto(event, CategoryMapper.toCategoryDtoForEvent(category), user, location, numberOfConfirmedRequests, numberOfViews);
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
        Location location = new Location(event.getLat(), event.getLon());
        List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvent(Status.CONFIRMED, event);
        long numberOfConfirmedRequests = confirmedRequests.size();
        String eventId = event.getId().toString();
        HitDto[] hits = webClient
                .get()
                .uri("/stats?start=2000-01-01 00:00:00&end=3000-01-01 00:00:00&uris=/events/{eventId}&unique=false", eventId)
                .retrieve().bodyToMono(HitDto[].class)
                .block();
        long numberOfViews;
        if (hits != null) {
            numberOfViews = hits.length;
        } else {
            numberOfViews = 0;
        }
        return EventMapper.toEventFullDto(updatedEvent, categoryDtoForEvent, user, location, numberOfConfirmedRequests, numberOfViews);
    }

    private EventFullDto getEventFullDto(EventFullDto eventFullDtoForPublish, Event eventForPublish) {
        User user;
        Optional<User> userOpt = userRepository.findById(eventFullDtoForPublish.getInitiator().getId());
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            throw new UserNotFoundException("Пользователь не найден");
        }
        Location location = new Location(eventForPublish.getLat(), eventForPublish.getLon());
        List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvent(Status.CONFIRMED, eventForPublish);
        long numberOfConfirmedRequests = confirmedRequests.size();
        String eventId = eventForPublish.getId().toString();
        HitDto[] hits = webClient
                .get()
                .uri("/stats?start=2000-01-01 00:00:00&end=3000-01-01 00:00:00&uris=/events/{eventId}&unique=false", eventId)
                .retrieve().bodyToMono(HitDto[].class)
                .block();
        long numberOfViews;
        if (hits != null) {
            numberOfViews = hits.length;
        } else {
            numberOfViews = 0;
        }
        return EventMapper.toEventFullDto(eventRepository.save(eventForPublish), eventFullDtoForPublish.getCategory(),
                user, location, numberOfConfirmedRequests, numberOfViews);
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

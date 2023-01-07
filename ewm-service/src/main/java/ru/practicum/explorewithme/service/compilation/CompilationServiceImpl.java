package ru.practicum.explorewithme.service.compilation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.CompilationFullDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.exceptions.CategoryNotFoundException;
import ru.practicum.explorewithme.exceptions.CompilationNotFoundException;
import ru.practicum.explorewithme.exceptions.EventNotFoundException;
import ru.practicum.explorewithme.exceptions.UserNotFoundException;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.mapper.CompilationMapper;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.model.*;
import ru.practicum.explorewithme.repository.*;

import java.util.*;

@Service
@Slf4j
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private final RequestRepository requestRepository;

    private final WebClient webClient;


    public CompilationServiceImpl(CompilationRepository compilationRepository, EventRepository eventRepository, CategoryRepository categoryRepository, UserRepository userRepository, RequestRepository requestRepository, WebClient.Builder builder) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        webClient = builder.baseUrl("http://stats-server:9090/").build();
    }


    @Override
    @Transactional
    public CompilationFullDto create(CompilationDto compilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(compilationDto);
        List<EventFullDto> events = new ArrayList<>();
        Set<Event> eventsList = new HashSet<>();
        for (Long i : compilationDto.getEvents()) {
            Optional<Event> eventOpt = eventRepository.findById(i);
            Event event;
            if (eventOpt.isPresent()) {
                event = eventOpt.get();
            } else {
                throw new EventNotFoundException("Событие не найдено");
            }
            EventFullDto eventFullDto = mapToEventFullDto(event.getInitiatorId().getId(), event);
            events.add(eventFullDto);
            User user = userRepository.findById(eventFullDto.getInitiator().getId()).get();
            eventsList.add(EventMapper.toEventFromEventFullDto(eventFullDto, event.getLat(), event.getLon(), user));
        }
        compilation.setEvents(eventsList);
        Compilation compilationToSave = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationFullDto(compilationToSave, events);
    }

    @Override
    @Transactional
    public void removeById(Long compId) {
        Optional<Compilation> compilationToDeleteOpt = compilationRepository.findById(compId);
        Compilation compilationToDelete;
        if (compilationToDeleteOpt.isPresent()) {
            compilationToDelete = compilationToDeleteOpt.get();
        } else {
            throw new CompilationNotFoundException("Подборка не найдена");
        }
        compilationRepository.delete(compilationToDelete);
    }

    @Override
    public CompilationFullDto getById(Long compId) {
        Optional<Compilation> compilationOpt = compilationRepository.findById(compId);
        Compilation compilation;
        if (compilationOpt.isPresent()) {
            compilation = compilationOpt.get();
        } else {
            throw new CompilationNotFoundException("Подборка не найдена");
        }
        Set<Event> eventsList = compilation.getEvents();
        List<EventFullDto> events = new ArrayList<>();
        for (Event event : eventsList) {
            EventFullDto eventFullDto = mapToEventFullDto(event.getInitiatorId().getId(), event);
            events.add(eventFullDto);
        }
        return CompilationMapper.toCompilationFullDto(compilation, events);
    }

    @Override
    public List<CompilationFullDto> get(Boolean pinned, PageRequest pageRequest) {
        List<Compilation> compilations = compilationRepository.findAll();
        List<CompilationFullDto> compilationDtoList = new ArrayList<>();
        for (Compilation compilation : compilations) {
            Set<Event> events = compilation.getEvents();
            List<EventFullDto> eventFullDtoList = new ArrayList<>();
            for (Event event : events) {
                EventFullDto eventFullDto = mapToEventFullDto(event.getInitiatorId().getId(), event);
                eventFullDtoList.add(eventFullDto);
            }
            CompilationFullDto compilationFullDto = CompilationMapper.toCompilationFullDto(compilation, eventFullDtoList);
            compilationDtoList.add(compilationFullDto);
        }
        return compilationDtoList;
    }

    @Override
    @Transactional
    public void addEvent(Long eventId, Long compId) {
        Optional<Compilation> compilationOpt = compilationRepository.findById(compId);
        Compilation compilation;
        if (compilationOpt.isPresent()) {
            compilation = compilationOpt.get();
        } else {
            throw new CompilationNotFoundException("Подборка не найдена");
        }
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Event event;
        if (eventOpt.isPresent()) {
            event = eventOpt.get();
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }
        event.addCompilation(compilation);
        compilation.addEvent(event);
        compilationRepository.save(compilation);
    }

    @Override
    @Transactional
    public void pin(Long compId) {
        Optional<Compilation> compilationOpt = compilationRepository.findById(compId);
        Compilation compilation;
        if (compilationOpt.isPresent()) {
            compilation = compilationOpt.get();
        } else {
            throw new CompilationNotFoundException("Подборка не найдена");
        }
        compilation.setPinned(true);
        compilationRepository.save(compilation);
    }

    @Override
    @Transactional
    public void removeEvent(Long eventId, Long compId) {
        Optional<Compilation> compilationOpt = compilationRepository.findById(compId);
        Compilation compilation;
        if (compilationOpt.isPresent()) {
            compilation = compilationOpt.get();
        } else {
            throw new CompilationNotFoundException("Подборка не найдена");
        }
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Event event;
        if (eventOpt.isPresent()) {
            event = eventOpt.get();
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }
        Set<Event> eventList = compilation.getEvents();
        eventList.remove(event);
        compilation.setEvents(eventList);
        compilationRepository.save(compilation);
    }

    @Override
    @Transactional
    public void unpin(Long compId) {
        Optional<Compilation> compilationOpt = compilationRepository.findById(compId);
        Compilation compilation;
        if (compilationOpt.isPresent()) {
            compilation = compilationOpt.get();
        } else {
            throw new CompilationNotFoundException("Подборка не найдена");
        }
        compilation.setPinned(false);
        compilationRepository.save(compilation);
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
        return EventMapper.toEventFullDto(event, CategoryMapper.toCategoryDtoForEvent(category), user, new Location(event.getLat(), event.getLon()), numberOfConfirmedRequests, numberOfViews);
    }
}

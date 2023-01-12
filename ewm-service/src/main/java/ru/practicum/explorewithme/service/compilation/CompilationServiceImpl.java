package ru.practicum.explorewithme.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.HitClient;
import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.CompilationFullDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.exceptions.CompilationNotFoundException;
import ru.practicum.explorewithme.exceptions.EventNotFoundException;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.mapper.CompilationMapper;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.model.*;
import ru.practicum.explorewithme.repository.CompilationRepository;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.RequestRepository;

import java.util.*;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final HitClient hitClient;


    @Override
    @Transactional
    public CompilationFullDto create(CompilationDto compilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(compilationDto);
        List<EventFullDto> eventFullDtos = new ArrayList<>();
        List<Event> events = eventRepository.findEventsByIdList(compilationDto.getEvents());
        Set<Event> eventsList = new HashSet<>();
        List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvents(Status.CONFIRMED, events);
        List<HitDto> hits = hitClient.hits(events);
        for (Event event : events) {
            List<Request> confirmedEventRequests = new ArrayList<>();
            for (Request request : confirmedRequests) {
                if (event.equals(request.getEvent())) {
                    confirmedEventRequests.add(request);
                }
            }
            EventFullDto eventFullDto = mapToEventFullDto(event, confirmedEventRequests, hits);
            eventFullDtos.add(eventFullDto);
            eventsList.add(event);
        }
        compilation.setEvents(eventsList);
        Compilation compilationToSave = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationFullDto(compilationToSave, eventFullDtos);
    }

    @Override
    @Transactional
    public void removeById(Long compId) {
        Compilation compilationToDelete = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException("Подборка не найдена"));
        compilationRepository.delete(compilationToDelete);
    }

    @Override
    public CompilationFullDto getById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException("Подборка не найдена"));
        Set<Event> setOfEvents = compilation.getEvents();
        List<EventFullDto> events = new ArrayList<>();
        List<HitDto> hits = hitClient.hits(new ArrayList<>(setOfEvents));
        List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvents(Status.CONFIRMED, new ArrayList<>(setOfEvents));
        for (Event event : setOfEvents) {
            List<Request> confirmedEventRequests = new ArrayList<>();
            for (Request request : confirmedRequests) {
                if (event.equals(request.getEvent())) {
                    confirmedEventRequests.add(request);
                }
            }
            EventFullDto eventFullDto = mapToEventFullDto(event, confirmedEventRequests, hits);
            events.add(eventFullDto);
        }
        return CompilationMapper.toCompilationFullDto(compilation, events);
    }

    @Override
    public List<CompilationFullDto> get(Boolean pinned, PageRequest pageRequest) {
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.getAllWithPinned(pinned, pageRequest);
        } else {
            compilations = compilationRepository.findAll(pageRequest).getContent();
        }
        List<CompilationFullDto> compilationDtoList = new ArrayList<>();
        List<Event> allCompilationsEvents = new ArrayList<>();
        for (Compilation compilation : compilations) {
            Set<Event> compilationEvents = compilation.getEvents();
            allCompilationsEvents.addAll(compilationEvents);
        }
        List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvents(Status.CONFIRMED, allCompilationsEvents);
        List<HitDto> hits = hitClient.hits(allCompilationsEvents);
        for (Compilation compilation : compilations) {
            Set<Event> events = compilation.getEvents();
            List<EventFullDto> eventFullDtoList = new ArrayList<>();
            for (Event event : events) {
                List<Request> confirmedEventRequests = new ArrayList<>();
                for (Request request : confirmedRequests) {
                    if (event.equals(request.getEvent())) {
                        confirmedEventRequests.add(request);
                    }
                }
                EventFullDto eventFullDto = mapToEventFullDto(event, confirmedEventRequests, hits);
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
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException("Подборка не найдена"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
        event.addCompilation(compilation);
        compilation.addEvent(event);
        compilationRepository.save(compilation);
    }

    @Override
    @Transactional
    public void pin(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException("Подборка не найдена"));
        compilation.setPinned(true);
    }

    @Override
    @Transactional
    public void removeEvent(Long eventId, Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException("Подборка не найдена"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
        Set<Event> eventList = compilation.getEvents();
        eventList.remove(event);
    }

    @Override
    @Transactional
    public void unpin(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException("Подборка не найдена"));
        compilation.setPinned(false);
    }

    private EventFullDto mapToEventFullDto(Event event, List<Request> confirmedRequests, List<HitDto> hits) {
        Category category = event.getCategory();
        User user = event.getInitiatorId();
        long numberOfConfirmedRequests = confirmedRequests.size();
        List<HitDto> eventHits = new ArrayList<>();
        for (HitDto hitDto : hits) {
            String uri = "/events/" + event.getId();
            if (hitDto.getUri().equals(uri)) {
                eventHits.add(hitDto);
            }
        }
        long numberOfViews = eventHits.size();
        return EventMapper.toEventFullDto(event, CategoryMapper.toCategoryDtoForEvent(category), user, event.getLocation(), numberOfConfirmedRequests, numberOfViews);
    }
}

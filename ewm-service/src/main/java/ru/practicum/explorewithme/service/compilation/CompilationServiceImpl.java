package ru.practicum.explorewithme.service.compilation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;


    public CompilationServiceImpl(CompilationRepository compilationRepository, EventRepository eventRepository, CategoryRepository categoryRepository, UserRepository userRepository, LocationRepository locationRepository) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
    }


    @Override
    @Transactional
    public CompilationFullDto createCompilation(CompilationDto compilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(compilationDto);
        List<EventFullDto> events = new ArrayList<>();
        List<Event> eventList = new ArrayList<>();
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
            Location location = event.getLocation();
            User user = userRepository.findById(eventFullDto.getInitiator().getId()).get();
            eventList.add(EventMapper.toEventFromEventFullDto(eventFullDto, location, user));
        }
        compilation.setEventList(eventList);
        Compilation compilationToSave = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationFullDto(compilationToSave, events);
    }

    @Override
    @Transactional
    public void removeCompilationById(Long compId) {
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
    public CompilationFullDto getCompilationById(Long compId) {
        Optional<Compilation> compilationOpt = compilationRepository.findById(compId);
        Compilation compilation;
        if (compilationOpt.isPresent()) {
            compilation = compilationOpt.get();
        } else {
            throw new CompilationNotFoundException("Подборка не найдена");
        }
        List<Event> eventsList = compilation.getEventList();
        List<EventFullDto> events = new ArrayList<>();
        for (Event event : eventsList) {
            EventFullDto eventFullDto = mapToEventFullDto(event.getInitiatorId().getId(), event);
            events.add(eventFullDto);
        }
        return CompilationMapper.toCompilationFullDto(compilation, events);
    }

    @Override
    public List<CompilationFullDto> getCompilations(Boolean pinned, PageRequest pageRequest) {
        List<Compilation> compilations = compilationRepository.findAll();
        List<CompilationFullDto> compilationDtoList = new ArrayList<>();
        for (Compilation compilation : compilations) {
            List<Event> events = compilation.getEventList();
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
    public void addEventToCompilation(Long eventId, Long compId) {
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
    public void pinCompilation(Long compId) {
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
    public void removeEventFromCompilation(Long eventId, Long compId) {
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
        List<Event> eventList = compilation.getEventList();
        eventList.remove(event);
        compilation.setEventList(eventList);
        compilationRepository.save(compilation);
    }

    @Override
    @Transactional
    public void unpinCompilation(Long compId) {
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
        return EventMapper.toEventFullDto(event, CategoryMapper.toCategoryDtoForEvent(category), user, event.getLocation());
    }
}

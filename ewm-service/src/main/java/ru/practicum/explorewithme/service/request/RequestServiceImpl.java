package ru.practicum.explorewithme.service.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.RequestDto;
import ru.practicum.explorewithme.exceptions.EventNotFoundException;
import ru.practicum.explorewithme.exceptions.RequestNotFoundException;
import ru.practicum.explorewithme.exceptions.UserNotFoundException;
import ru.practicum.explorewithme.mapper.RequestMapper;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.model.Request;
import ru.practicum.explorewithme.model.Status;
import ru.practicum.explorewithme.model.User;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.RequestRepository;
import ru.practicum.explorewithme.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public RequestServiceImpl(RequestRepository requestRepository, EventRepository eventRepository, UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }


    @Override
    @Transactional
    public RequestDto createRequest(Long userId, Long eventId) {
        LocalDateTime created = LocalDateTime.now();
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Event event;
        if (eventOpt.isPresent()) {
            event = eventOpt.get();
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }
        Optional<User> userOpt = userRepository.findById(userId);
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            throw new UserNotFoundException("Пользователь не найден");
        }
        Request requestForSave = new Request(1L, created, event, user, Status.PENDING);
        return RequestMapper.toRequestDto(requestRepository.save(requestForSave));
    }

    @Override
    public List<RequestDto> getRequestsByUserId(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            throw new UserNotFoundException("Пользователь не найден");
        }
        List<Request> requests = requestRepository.findAllByRequester(user);
        List<RequestDto> requestsDto = new ArrayList<>();
        for (Request request : requests) {
            requestsDto.add(RequestMapper.toRequestDto(request));
        }
        return requestsDto;
    }

    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        Request request;
        Optional<Request> requestOpt = requestRepository.findById(requestId);
        if (requestOpt.isPresent()) {
            request = requestOpt.get();
        } else {
            throw new RequestNotFoundException("Запрос не найден");
        }
        request.setStatus(Status.CANCELED);
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    public List<RequestDto> getRequestByEventIdAndUserId(Long userId, Long eventId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        Event event;
        if (eventOpt.isPresent()) {
            event = eventOpt.get();
        } else {
            throw new EventNotFoundException("Событие не найдено");
        }
        List<Request> requests = requestRepository.findAllByEvent(event);
        List<RequestDto> requestsDto = new ArrayList<>();
        for (Request request : requests) {
            if (!Objects.equals(request.getRequester().getId(), userId)) {
                requestsDto.add(RequestMapper.toRequestDto(request));
            }
        }
        return requestsDto;
    }
}

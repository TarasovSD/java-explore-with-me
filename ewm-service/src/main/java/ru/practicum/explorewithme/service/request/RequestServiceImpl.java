package ru.practicum.explorewithme.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.RequestDto;
import ru.practicum.explorewithme.exceptions.EventNotFoundException;
import ru.practicum.explorewithme.exceptions.RequestNotCreatedException;
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
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RequestDto create(Long userId, Long eventId) {
        LocalDateTime created = LocalDateTime.now();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        if (userId.equals(event.getInitiatorId().getId())) {
            throw new RequestNotCreatedException("Реквестер не может быть инициатором события");
        }
        if (!event.getState().equals(Status.PUBLISHED)) {
            throw new RequestNotCreatedException("Невозможно создать запрос на участие в неопубликованном событии");
        }
        if (event.getParticipantLimit() == 0) {
            throw new RequestNotCreatedException("Невозможно создать запрос на участие в событии, " +
                    "так как заявленное количество участников события равно нулю");
        }
        if (event.getParticipantLimit() <= requestRepository.findRequestsByStatusAndEvent(Status.CONFIRMED, event).size()) {
            throw new RequestNotCreatedException("Невозможно создать запрос на участие в событии, " +
                    "так как достигнуто максимальное количество подтвежденных участников");
        }
        Request requestForSave = new Request(1L, created, event, user, Status.PENDING);
        return RequestMapper.toRequestDto(requestRepository.findByRequesterIdAndEventId(userId, eventId)
                .orElse(requestRepository.save(requestForSave)));
    }

    @Override
    public List<RequestDto> getByUserId(Long userId) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        List<Request> requests = requestRepository.findAllByRequester(foundUser);
        return RequestMapper.toRequestDtos(requests);
    }

    @Override
    @Transactional
    public RequestDto cancel(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException("Запрос не найден"));
        request.setStatus(Status.CANCELED);
        return RequestMapper.toRequestDto(request);
    }

    @Override
    public List<RequestDto> getRequestByEventIdAndUserId(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));
        return RequestMapper.toRequestDtos(requestRepository
                .findAllByEventIdAndInitiatorId(event.getInitiatorId().getId(), event.getId(), userId));
    }
}

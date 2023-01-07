package ru.practicum.explorewithme.priv.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.RequestDto;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventUpdateDto;
import ru.practicum.explorewithme.service.event.EventService;
import ru.practicum.explorewithme.service.request.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Slf4j
public class EventPrivateController {

    private final EventService eventService;
    private final RequestService requestService;

    public EventPrivateController(EventService eventService, RequestService requestService) {
        this.eventService = eventService;
        this.requestService = requestService;
    }

    @PostMapping("/{userId}/events")
    public EventFullDto create(@RequestBody EventDto eventDto, @PathVariable Long userId) {
        log.info("событие создано");
        return eventService.create(eventDto, userId);
    }

    @GetMapping("/{userId}/events")
    public List<EventFullDto> getByUserId(@PathVariable Long userId,
                                          @RequestParam(name = "from") Integer from,
                                          @RequestParam(name = "size") Integer size) {
        log.info("запрос событий пользователя с ID {}", userId);
        int page = from / size;
        final PageRequest pageRequest = PageRequest.of(page, size);
        return eventService.getByUserId(userId, pageRequest);
    }

    @PatchMapping("/{userId}/events")
    public EventFullDto update(@RequestBody EventUpdateDto eventDto, @PathVariable Long userId) {
        log.info("событие c ID {} обновлено", eventDto.getEventId());
        return eventService.update(eventDto, userId);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getByIdByUser(@PathVariable Long userId,
                                           @PathVariable Long eventId) {
        log.info("запрос события с ID {} пользователем с ID {}", eventId, userId);
        return eventService.getByIdByUser(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto cancel(@PathVariable Long userId,
                                    @PathVariable Long eventId) {
        log.info("событие c ID {} отменено пользователем с ID {}", eventId, userId);
        return eventService.cancel(eventId, userId);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<RequestDto> getRequestByEventIdAndUserId(@PathVariable Long userId,
                                                         @PathVariable Long eventId) {
        log.info("Получение информации о запросах на участие в событии с ID {} пользователя с ID {}", eventId, userId);
        return requestService.getRequestByEventIdAndUserId(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests/{reqId}/reject")
    public RequestDto rejectRequest(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @PathVariable Long reqId) {
        log.info("Заявка с ID {} на участие в событии c ID {} пользователя с ID {} отклонена", reqId, eventId, userId);
        return eventService.rejectRequest(eventId, userId, reqId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests/{reqId}/confirm")
    public RequestDto confirmRequest(@PathVariable Long userId,
                                     @PathVariable Long eventId,
                                     @PathVariable Long reqId) {
        log.info("Заявка с ID {} на участие в событии c ID {} пользователя с ID {} подтверждена", reqId, eventId, userId);
        return eventService.confirmRequest(eventId, userId, reqId);
    }
}

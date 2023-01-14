package ru.practicum.explorewithme.admin.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.model.Status;
import ru.practicum.explorewithme.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@Slf4j
public class EventAdminController {

    private final EventService eventService;

    public EventAdminController(EventService eventService) {
        this.eventService = eventService;
    }

    @PatchMapping("/{eventId}/publish")
    public EventFullDto publish(@PathVariable Long eventId) {
        log.info("Публикация события с ID {}", eventId);
        return eventService.publish(eventId);
    }

    @PatchMapping("/{eventId}/reject")
    public EventFullDto reject(@PathVariable Long eventId) {
        log.info("Отклонение события с ID {}", eventId);
        return eventService.rejectRequest(eventId);
    }

    @GetMapping()
    public List<EventFullDto> get(@RequestParam(required = false) List<Long> users,
                                  @RequestParam(required = false) List<Status> states,
                                  @RequestParam(required = false) List<Long> categories,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                  @RequestParam(required = false) Integer from,
                                  @RequestParam(required = false) Integer size) {
        log.info("Запрос подборки событий");
        int page = from / size;
        final PageRequest pageRequest = PageRequest.of(page, size);
        return eventService.get(users, states, categories, rangeStart, rangeEnd, pageRequest);
    }

    @PutMapping("/{eventId}")
    public EventFullDto redact(@RequestBody EventDto eventDto, @PathVariable Long eventId) {
        log.info("событие отредактировано");
        return eventService.redact(eventDto, eventId);
    }
}

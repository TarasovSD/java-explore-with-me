package ru.practicum.explorewithme.admin.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.model.Status;
import ru.practicum.explorewithme.service.event.EventService;

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
    public EventFullDto publishEvent(@PathVariable Long eventId) {
        log.info("Публикация события с ID: " + eventId);
        return eventService.publishEvent(eventId);
    }

    @PatchMapping("/{eventId}/reject")
    public EventFullDto rejectEvent(@PathVariable Long eventId) {
        log.info("Отклонение события с ID: " + eventId);
        return eventService.rejectEvent(eventId);
    }

    @GetMapping()
    public List<EventFullDto> getEvents(@RequestParam List<Long> users,
                                        @RequestParam List<Status> states,
                                        @RequestParam List<Long> categories,
                                        @RequestParam String rangeStart,
                                        @RequestParam String rangeEnd,
                                        @RequestParam Integer from,
                                        @RequestParam Integer size) {
        log.info("Запрос подборки событий");
        int page = from / size;
        final PageRequest pageRequest = PageRequest.of(page, size);
        return eventService.getEvents(users, states, categories, rangeStart, rangeEnd, pageRequest);
    }

    @PutMapping("/{eventId}")
    public EventFullDto redactEvent(@RequestBody EventDto eventDto, @PathVariable Long eventId) {
        log.info("событие отредактировано");
        return eventService.redactEvent(eventDto, eventId);
    }
}

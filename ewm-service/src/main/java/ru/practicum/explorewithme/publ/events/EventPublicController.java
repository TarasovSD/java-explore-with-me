package ru.practicum.explorewithme.publ.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.service.event.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/events")
@Slf4j
public class EventPublicController {

    private final EventService eventService;

    public EventPublicController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventsBy(@PathVariable Long eventId) {
        log.info("запрос события с ID: " + eventId);
        return eventService.getEventById(eventId);
    }

    @GetMapping()
    public List<EventFullDto> getEvents(@RequestParam String text,
                                        @RequestParam List<Long> categories,
                                        @RequestParam List<Boolean> paid,
                                        @RequestParam String rangeStart,
                                        @RequestParam String rangeEnd,
                                        @RequestParam Boolean onlyAvailable,
                                        @RequestParam String sort,
                                        @RequestParam Integer from,
                                        @RequestParam Integer size) {
        log.info("Запрос событий с возможностью фильтрации");
        int page = from / size;
        final PageRequest pageRequest = PageRequest.of(page, size);
        return eventService.getEventsByFilter(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, pageRequest);
    }
}

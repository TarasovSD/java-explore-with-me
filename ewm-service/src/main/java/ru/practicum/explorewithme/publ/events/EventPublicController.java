package ru.practicum.explorewithme.publ.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@Slf4j
@RequiredArgsConstructor
public class EventPublicController {

    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventFullDto getById(@PathVariable Long eventId) {
        log.info("запрос события с ID {}", eventId);
        return eventService.getById(eventId);
    }

    @GetMapping()
    public List<EventFullDto> get(@RequestParam(required = false) String text,
                                  @RequestParam(required = false) List<Long> categories,
                                  @RequestParam(required = false) List<Boolean> paid,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                  @RequestParam(required = false) Boolean onlyAvailable,
                                  @RequestParam(required = false) String sort,
                                  @RequestParam(required = false) Integer from,
                                  @RequestParam(required = false) Integer size) {
        log.info("Запрос событий с возможностью фильтрации");
        int page = from / size;
        final PageRequest pageRequest = PageRequest.of(page, size);
        return eventService.getByFilter(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, pageRequest);
    }
}

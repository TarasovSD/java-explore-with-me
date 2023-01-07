package ru.practicum.explorewithme.admin.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.explorewithme.Update;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;

import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/admin/events")
@Slf4j
@Validated
public class EventAdminController {

    private final WebClient webClient;

    public EventAdminController(@Value("${base.path}") String basePath, WebClient.Builder builder) {
        webClient = builder.baseUrl(basePath).build();
    }

    @PatchMapping("/{eventId}/publish")
    public EventFullDto publish(@PositiveOrZero @PathVariable Long eventId) {
        log.info("Публикация события с ID {}", eventId);
        return webClient
                .patch()
                .uri("/admin/events/{eventId}/publish", eventId)
                .retrieve()
                .bodyToMono(EventFullDto.class)
                .block();
    }

    @PatchMapping("/{eventId}/reject")
    public EventFullDto reject(@PositiveOrZero @PathVariable Long eventId) {
        log.info("Отклонение события с ID {}", eventId);
        return webClient
                .patch()
                .uri("/admin/events/{eventId}/reject", eventId)
                .retrieve()
                .bodyToMono(EventFullDto.class)
                .block();
    }

    @GetMapping()
    public EventFullDto[] get(@RequestParam(defaultValue = "") List<Long> users,
                              @RequestParam(defaultValue = "") List<String> states,
                              @RequestParam(defaultValue = "") List<Long> categories,
                              @RequestParam(defaultValue = "") String rangeStart,
                              @RequestParam(defaultValue = "") String rangeEnd,
                              @RequestParam(defaultValue = "0") Integer from,
                              @RequestParam(defaultValue = "10") Integer size) {
        log.info("Запрос всех событий");
        String usersStr;
        String statesStr;
        String categoriesStr;
        if (users.size() == 0) {
            usersStr = "";
        } else {
            usersStr = users.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        if (states.size() == 0) {
            statesStr = "";
        } else {
            statesStr = states.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        if (categories.size() == 0) {
            categoriesStr = "";
        } else {
            categoriesStr = categories.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        return webClient
                .get()
                .uri("/admin/events?users={usersStr}&states={statesStr}&categories={categoriesStr}&rangeStart={rangeStart}&rangeEnd={rangeEnd}&from={from}&size={size}",
                        usersStr, statesStr, categoriesStr, rangeStart, rangeEnd, from, size)
                .retrieve()
                .bodyToMono(EventFullDto[].class)
                .block();
    }

    @PutMapping("/{eventId}")
    public EventFullDto redact(@Validated(Update.class) @RequestBody EventDto eventDto,
                               @PositiveOrZero @PathVariable Long eventId) {
        log.info("Редактирование события с ID {}", eventId);
        return webClient
                .put()
                .uri("/admin/events/{eventId}", eventId)
                .body(Mono.just(eventDto), EventDto.class)
                .retrieve()
                .bodyToMono(EventFullDto.class)
                .block();
    }
}

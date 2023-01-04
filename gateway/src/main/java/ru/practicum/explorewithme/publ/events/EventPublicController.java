package ru.practicum.explorewithme.publ.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/events")
@Slf4j
@Validated
public class EventPublicController {

    private final WebClient webClient;
    private final WebClient statsWebClient;

    public EventPublicController(WebClient.Builder builder) {
        webClient = builder.baseUrl("http://localhost:9098/").build();
        statsWebClient = builder.baseUrl("http://localhost:9090").build();
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PositiveOrZero @PathVariable Long eventId, HttpServletRequest request) {
        log.info("Запрос события с ID: " + eventId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);
        HitDto hitDto = new HitDto(null, "emv-service", request.getRequestURI(), request.getRemoteAddr(), timestamp);
        statsWebClient
                .post()
                .uri("/hit")
                .body(Mono.just(hitDto), HitDto.class)
                .retrieve().bodyToMono(HitDto.class)
                .block();
        return webClient
                .get()
                .uri("/events/{eventId}", eventId)
                .retrieve()
                .bodyToMono(EventFullDto.class)
                .block();
    }

    @GetMapping()
    public EventFullDto[] getEvents(@RequestParam(defaultValue = "") String text,
                                    @RequestParam(defaultValue = "") List<Long> categories,
                                    @RequestParam(defaultValue = "false, true") List<Boolean> paid,
                                    @RequestParam(defaultValue = "") String rangeStart,
                                    @RequestParam(defaultValue = "") String rangeEnd,
                                    @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                    @RequestParam(defaultValue = "unsorted") String sort,
                                    @RequestParam(defaultValue = "0") Integer from,
                                    @RequestParam(defaultValue = "10") Integer size, HttpServletRequest request) {
        String categoriesStr;
        if (categories.size() == 0) {
            categoriesStr = "";
        } else {
            categoriesStr = categories.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        String paidStr = paid.stream().map(String::valueOf).collect(Collectors.joining(","));
        log.info("Запрос событий с возможностью фильтрации");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);
        HitDto hitDto = new HitDto(null, "emv-service", request.getRequestURI(), request.getRemoteAddr(), timestamp);
        statsWebClient
                .post()
                .uri("/hit")
                .body(Mono.just(hitDto), HitDto.class)
                .retrieve().bodyToMono(HitDto.class)
                .block();
        return webClient
                .get()
                .uri("/events?text={text}&categories={categoriesStr}&paid={paidStr}&rangeStart={rangeStart}&" +
                                "rangeEnd={rangeEnd}&onlyAvailable={onlyAvailable}&sort={sort}&from={from}&size={size}",
                        text, categoriesStr, paidStr, rangeStart, rangeEnd, onlyAvailable, sort, from, size)
                .retrieve()
                .bodyToMono(EventFullDto[].class)
                .block();
    }
}

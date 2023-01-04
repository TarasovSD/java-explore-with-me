package ru.practicum.explorewithme.priv.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.explorewithme.Create;
import ru.practicum.explorewithme.Update;
import ru.practicum.explorewithme.dto.RequestDto;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventUpdateDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/users")
@Slf4j
@Validated
public class EventPrivateController {

    private final WebClient webClient;

    public EventPrivateController(WebClient.Builder builder) {
        webClient = builder.baseUrl("http://localhost:9092/").build();
    }

    @PostMapping("/{userId}/events")
    public EventFullDto createEvent(@Validated(Create.class) @RequestBody EventDto eventDto,
                                    @PositiveOrZero @PathVariable Long userId) {
        log.info("Создание нового события пользователем с ID: " + userId);
        return webClient
                .post()
                .uri("/users/{id}/events", userId)
                .body(Mono.just(eventDto), EventDto.class)
                .retrieve()
                .bodyToMono(EventFullDto.class)
                .block();
    }

    @GetMapping("/{userId}/events")
    public EventFullDto[] getEventsByUserId(@PositiveOrZero @PathVariable Long userId,
                                            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Запрос событий пользователем с ID: " + userId);
        return webClient
                .get()
                .uri("/users/{userId}/events?from={from}&size={size}", userId, from, size)
                .retrieve()
                .bodyToMono(EventFullDto[].class)
                .block();
    }

    @PatchMapping("/{userId}/events")
    public EventFullDto updateEvent(@Validated(Update.class) @RequestBody EventUpdateDto eventDto,
                                    @PositiveOrZero @PathVariable Long userId) {
        log.info("Обновление события пользователем с ID: " + userId);
        return webClient
                .patch()
                .uri("/users/{id}/events", userId)
                .body(Mono.just(eventDto), EventDto.class)
                .retrieve()
                .bodyToMono(EventFullDto.class)
                .block();
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getEventById(@PositiveOrZero @PathVariable Long userId,
                                     @PositiveOrZero @PathVariable Long eventId) {
        log.info("Запрос события с ID {} пользователем с ID {}", eventId, userId);
        return webClient
                .get()
                .uri("/users/{userId}/events/{eventId}", userId, eventId)
                .retrieve()
                .bodyToMono(EventFullDto.class)
                .block();
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto cancelEvent(@PositiveOrZero @PathVariable Long userId,
                                    @PositiveOrZero @PathVariable Long eventId) {
        log.info("Отмена события с ID {} пользователем с ID {}", eventId, userId);
        return webClient
                .patch()
                .uri("/users/{userId}/events/{eventId}", userId, eventId)
                .retrieve()
                .bodyToMono(EventFullDto.class)
                .block();
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public RequestDto[] getRequestByEventIdAndUserId(@PositiveOrZero @PathVariable Long userId,
                                                     @PositiveOrZero @PathVariable Long eventId) {
        log.info("Получение информации о запросах на участие в событии с ID {} пользователя с ID {}", eventId, userId);
        return webClient
                .get()
                .uri("/users/{userId}/events/{eventId}/requests", userId, eventId)
                .retrieve()
                .bodyToMono(RequestDto[].class)
                .block();
    }

    @PatchMapping("/{userId}/events/{eventId}/requests/{reqId}/confirm")
    public RequestDto confirmRequest(@PositiveOrZero @PathVariable Long userId,
                                     @PositiveOrZero @PathVariable Long eventId,
                                     @PositiveOrZero @PathVariable Long reqId) {
        log.info("Подтверждение заявки c ID {} на участие в событии c ID {} пользователя c ID {}", reqId, eventId, userId);
        return webClient
                .patch()
                .uri("/users/{userId}/events/{eventId}/requests/{reqId}/confirm", userId, eventId, reqId)
                .retrieve()
                .bodyToMono(RequestDto.class)
                .block();
    }

    @PatchMapping("/{userId}/events/{eventId}/requests/{reqId}/reject")
    public RequestDto rejectRequest(@PositiveOrZero @PathVariable Long userId,
                                    @PositiveOrZero @PathVariable Long eventId,
                                    @PositiveOrZero @PathVariable Long reqId) {
        log.info("Отклонение заявки c ID {} на участие в событии c ID {} пользователя c ID {}", reqId, eventId, userId);
        return webClient
                .patch()
                .uri("/users/{userId}/events/{eventId}/requests/{reqId}/reject", userId, eventId, reqId)
                .retrieve()
                .bodyToMono(RequestDto.class)
                .block();
    }
}

package ru.practicum.explorewithme.priv.subscriptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.explorewithme.dto.SubscriptionDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/users/{userId}")
@Slf4j
@Validated
public class SubscriptionPrivateController {

    /**
     * Реализация подписок подьзователем на события, созданные другими пользователями (фича)
     */

    private final WebClient webClient;

    public SubscriptionPrivateController(@Value("${base.path}") String basePath, WebClient.Builder builder) {
        webClient = builder.baseUrl(basePath).build();
    }

    @PostMapping("/subscription/{subscribingId}")
    public SubscriptionDto create(@PositiveOrZero @PathVariable Long userId,
                                  @PositiveOrZero @PathVariable Long subscribingId) {
        log.info("Запрос пользователем с ID {} на создание подписки на события пользователя с ID {} ", userId, subscribingId);
        return webClient
                .post()
                .uri("/users/{userId}/subscription/{subscribingId}", userId, subscribingId)
                .retrieve()
                .bodyToMono(SubscriptionDto.class)
                .block();
    }

    @DeleteMapping("/subscription/{subscribingId}")
    public String remove(@PositiveOrZero @PathVariable Long userId,
                         @PositiveOrZero @PathVariable Long subscribingId) {
        log.info("Запрос пользователем с ID {} на удаление подписки на события пользователя с ID {} ", userId, subscribingId);
        return webClient
                .delete()
                .uri("/users/{userId}/subscription/{subscribingId}", userId, subscribingId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @GetMapping("/subscription/events")
    public EventFullDto[] getEvents(@PositiveOrZero @PathVariable Long userId,
                                    @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                    @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Запрос информации об актуальных событиях пользователей, на которых подписан пользователь с ID {}", userId);
        return webClient
                .get()
                .uri("/users/{userId}/subscription/events?from={from}&size={size}", userId, from, size)
                .retrieve()
                .bodyToMono(EventFullDto[].class)
                .block();
    }

    @GetMapping("/subscription")
    public SubscriptionDto[] get(@PositiveOrZero @PathVariable Long userId,
                                 @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                 @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Запрос информации о подписках пользователя с ID {}", userId);
        return webClient
                .get()
                .uri("/users/{userId}/subscription?from={from}&size={size}", userId, from, size)
                .retrieve()
                .bodyToMono(SubscriptionDto[].class)
                .block();
    }
}

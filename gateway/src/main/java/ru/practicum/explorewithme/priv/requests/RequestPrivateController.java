package ru.practicum.explorewithme.priv.requests;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.explorewithme.dto.RequestDto;

import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/users")
@Slf4j
@Validated
public class RequestPrivateController {

    private final WebClient webClient;

    public RequestPrivateController(@Value("${base.path}") String basePath, WebClient.Builder builder) {
        webClient = builder.baseUrl(basePath).build();
    }

    @PostMapping("/{userId}/requests")
    public RequestDto create(@PositiveOrZero @PathVariable Long userId,
                             @PositiveOrZero @RequestParam Long eventId) {
        log.info("Создание запроса на участие в событии с ID {} пользователем с ID {}", eventId, userId);
        return webClient
                .post()
                .uri("/users/{userId}/requests?eventId={eventId}", userId, eventId)
                .retrieve()
                .bodyToMono(RequestDto.class)
                .block();
    }

    @GetMapping("/{userId}/requests")
    public RequestDto[] getByUserId(@PositiveOrZero @PathVariable Long userId) {
        log.info("Запрос информации о запросах на участие в событиях пользователем с ID {}", userId);
        return webClient
                .get()
                .uri("/users/{userId}/requests", userId)
                .retrieve()
                .bodyToMono(RequestDto[].class)
                .block();
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public RequestDto cancel(@PositiveOrZero @PathVariable Long userId,
                             @PositiveOrZero @PathVariable Long requestId) {
        log.info("Отмена запроса на участие в событии с ID {} пользователем с ID {}", requestId, userId);
        return webClient
                .patch()
                .uri("/users/{userId}/requests/{requestId}/cancel", userId, requestId)
                .retrieve()
                .bodyToMono(RequestDto.class)
                .block();
    }
}

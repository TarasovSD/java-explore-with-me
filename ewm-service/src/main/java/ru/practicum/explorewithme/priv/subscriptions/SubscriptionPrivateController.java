package ru.practicum.explorewithme.priv.subscriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.SubscriptionDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.service.subscription.SubscriptionService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}")
@Slf4j
@RequiredArgsConstructor
public class SubscriptionPrivateController {

    /**
     * Реализация подписок подьзователем на события, созданные другими пользователями (фича)
     */

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscription/{subscribingId}")
    public SubscriptionDto create(@PathVariable Long userId, @PathVariable Long subscribingId) {
        log.info("Запрос пользователем с ID {} на подписку на пользователя с ID {} создан", userId, subscribingId);
        return subscriptionService.create(userId, subscribingId);
    }

    @DeleteMapping("/subscription/{subscribingId}")
    public void remove(@PathVariable Long userId, @PathVariable Long subscribingId) {
        log.info("Запрос пользователем с ID {} на удаление подписки на пользователя с ID {} выполнен", userId, subscribingId);
        subscriptionService.remove(userId, subscribingId);
    }

    @GetMapping("/subscription/events")
    public List<EventFullDto> getEvents(@PathVariable Long userId,
                                        @RequestParam(name = "from") Integer from,
                                        @RequestParam(name = "size") Integer size) {
        log.info("Запрос пользователем с ID {} информации об актуальных событиях пользователей, на которых он подписан", userId);
        int page = from / size;
        final PageRequest pageRequest = PageRequest.of(page, size);
        return subscriptionService.getEvents(userId, pageRequest);
    }

    @GetMapping("/subscription")
    public List<SubscriptionDto> get(@PathVariable Long userId,
                                     @RequestParam(name = "from") Integer from,
                                     @RequestParam(name = "size") Integer size) {
        log.info("Запрос пользователем с ID {} информации о подписках", userId);
        int page = from / size;
        final PageRequest pageRequest = PageRequest.of(page, size);
        return subscriptionService.get(userId, pageRequest);
    }
}

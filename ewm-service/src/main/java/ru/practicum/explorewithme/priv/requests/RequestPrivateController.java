package ru.practicum.explorewithme.priv.requests;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.RequestDto;
import ru.practicum.explorewithme.service.request.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Slf4j
public class RequestPrivateController {

    private final RequestService requestService;

    public RequestPrivateController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/{userId}/requests")
    public RequestDto create(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("Запрос на участие в событии с ID {} пользователем с ID {} создан", userId, eventId);
        return requestService.create(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    public List<RequestDto> getByUserId(@PathVariable Long userId) {
        log.info("Запрос пользователем с ID {} информации о запросах на участие в событиях", userId);
        return requestService.getByUserId(userId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public RequestDto cancel(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("Отмена запроса с ID {} на участие в событии пользователем с ID {} создан", requestId, userId);
        return requestService.cancel(userId, requestId);
    }
}

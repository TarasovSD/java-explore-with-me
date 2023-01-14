package ru.practicum.explorewithme.service.subscription;

import org.springframework.data.domain.PageRequest;
import ru.practicum.explorewithme.dto.SubscriptionDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;

import java.util.List;

public interface SubscriptionService {

    SubscriptionDto create(Long userId, Long subscribingId);

    void remove(Long userId, Long subscribingId);

    List<EventFullDto> getEvents(Long userId, PageRequest pageRequest);

    List<SubscriptionDto> get(Long userId, PageRequest pageRequest);
}

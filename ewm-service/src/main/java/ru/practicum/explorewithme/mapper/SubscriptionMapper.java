package ru.practicum.explorewithme.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.dto.SubscriptionDto;
import ru.practicum.explorewithme.model.Subscription;

@UtilityClass
public class SubscriptionMapper {
    public SubscriptionDto toSubscriptionDto(Subscription subscription) {
        return new SubscriptionDto(subscription.getId(),
                subscription.getSubscriber().getId(),
                subscription.getSubscribing().getId(),
                subscription.getCreated());
    }
}

package ru.practicum.explorewithme.service.subscription;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.HitClient;
import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.dto.SubscriptionDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.exceptions.SubscriptionAlreadyExistsException;
import ru.practicum.explorewithme.exceptions.SubscriptionNotFoundException;
import ru.practicum.explorewithme.exceptions.UserNotFoundException;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.mapper.SubscriptionMapper;
import ru.practicum.explorewithme.model.*;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.RequestRepository;
import ru.practicum.explorewithme.repository.SubscriptionRepository;
import ru.practicum.explorewithme.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final HitClient hitClient;

    @Override
    @Transactional
    public SubscriptionDto create(Long userId, Long subscribingId) {
        User subscriber = getUser(userId);
        User subscribing = getUser(subscribingId);
        if (subscriptionRepository.getSubscriptionByUserIdAndSubscribingId(userId, subscribingId).isPresent()) {
            throw new SubscriptionAlreadyExistsException("Подписка уже была создана ранее");
        }
        return SubscriptionMapper.toSubscriptionDto(subscriptionRepository
                .save(new Subscription(null, subscriber, subscribing, LocalDateTime.now())));
    }

    @Override
    @Transactional
    public void remove(Long userId, Long subscribingId) {
        getUser(userId);
        Subscription subscriptionForRemove = subscriptionRepository.getSubscriptionByUserIdAndSubscribingId(userId, subscribingId)
                .orElseThrow(() -> new SubscriptionNotFoundException("Подписка не найдена"));
        subscriptionRepository.delete(subscriptionForRemove);
    }

    @Override
    public List<EventFullDto> getEvents(Long userId, PageRequest pageRequest) {
        List<Long> userSubscribingIds = subscriptionRepository.findByUserId(userId);
        if (userSubscribingIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Event> events = eventRepository.getEventsByInitiatorAndStatus(userSubscribingIds,
                Status.PUBLISHED, LocalDateTime.now(), pageRequest);
        Set<EventFullDto> eventFullDtos = new HashSet<>();
        List<Request> confirmedRequests = requestRepository.findRequestsByStatusAndEvents(Status.CONFIRMED, events);
        List<HitDto> hits = hitClient.hits(events);
        for (Event event : events) {
            List<Request> confirmedEventRequests = new ArrayList<>();
            for (Request request : confirmedRequests) {
                if (event.equals(request.getEvent())) {
                    confirmedEventRequests.add(request);
                }
            }
            List<HitDto> eventHits = new ArrayList<>();
            for (HitDto hitDto : hits) {
                String uri = "/events/" + event.getId();
                if (hitDto.getUri().equals(uri)) {
                    eventHits.add(hitDto);
                }
            }
            long numberOfConfirmedRequests = confirmedEventRequests.size();
            long numberOfViews = eventHits.size();
            EventFullDto eventFullDto = EventMapper.toEventFullDto(event,
                    CategoryMapper.toCategoryDtoForEvent(event.getCategory()), event.getInitiatorId(),
                    event.getLocation(), numberOfConfirmedRequests, numberOfViews);
            eventFullDtos.add(eventFullDto);
        }
        return new ArrayList<>(eventFullDtos);
    }

    @Override
    public List<SubscriptionDto> get(Long userId, PageRequest pageRequest) {
        List<Subscription> subscriptions = subscriptionRepository.getSubscriptionsBySubscriber(userId, pageRequest);
        return subscriptions.stream().map(SubscriptionMapper::toSubscriptionDto).collect(Collectors.toList());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }
}

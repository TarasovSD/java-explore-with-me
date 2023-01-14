package ru.practicum.explorewithme.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.model.Subscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("select s from Subscription s " +
            "where s.subscriber.id = :userId and s.subscribing.id = :subscribingId")
    Optional<Subscription> getSubscriptionByUserIdAndSubscribingId(Long userId, Long subscribingId);

    @Query("select s.subscribing.id from Subscription s where s.subscriber.id = :userId")
    List<Long> findByUserId(Long userId);

    @Query("select s from Subscription s where s.subscriber.id = :userId order by s.created asc")
    List<Subscription> getSubscriptionsBySubscriber(Long userId, PageRequest pageRequest);
}

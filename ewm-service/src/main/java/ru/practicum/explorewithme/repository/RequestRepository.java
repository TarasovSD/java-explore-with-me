package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.model.Request;
import ru.practicum.explorewithme.model.Status;
import ru.practicum.explorewithme.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequester(User user);

    List<Request> findRequestsByStatusAndEvent(Status status, Event event);

    @Query("select r from Request r where r.event.state = :status and r.event in :events")
    List<Request> findRequestsByStatusAndEvents(Status status, List<Event> events);

    @Query("select r from Request r where r.event.initiatorId.id = :initiatorId and r.event.id = :eventId and r.requester.id <> :userId")
    List<Request> findAllByEventIdAndInitiatorId(Long initiatorId, Long eventId, Long userId);

    Optional<Request> findByRequesterIdAndEventId(Long userId, Long eventId);
}

package ru.practicum.explorewithme.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.model.Status;
import ru.practicum.explorewithme.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findEventByInitiatorId(User user, PageRequest pageRequest);

    @Query("select e from Event e where e.initiatorId in :users and e.state in :states and e.category.id in :categories " +
            "and e.eventDate between :rangeStart and :rangeEnd")
    List<Event> getEvents(List<User> users, List<Status> states, List<Long> categories,
                          LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest pageRequest);

    @Query("select e from Event e where  e.category.id in :categories and e.paid in :paid " +
            "and e.eventDate between :rangeStart and :rangeEnd order by e.eventDate desc")
    List<Event> getEventsByFilterSortByEventDate(List<Long> categories, List<Boolean> paid, LocalDateTime rangeStart,
                                                 LocalDateTime rangeEnd, PageRequest pageRequest);

    @Query("select e from Event e where  e.category.id in :categories and e.paid in :paid " +
            "and e.eventDate between :rangeStart and :rangeEnd")
    List<Event> getEventsByFilterWithoutSort(List<Long> categories, List<Boolean> paid, LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd, PageRequest pageRequest);

    @Query("select e from Event e where e.id in :eventsIds")
    List<Event> findEventsByIdList(List<Long> eventsIds);

    @Query("select e from Event e where e.initiatorId.id in :userSubscribingIds and e.state = :status and e.eventDate > :nowMoment")
    List<Event> getEventsByInitiatorAndStatus(List<Long> userSubscribingIds, Status status, LocalDateTime nowMoment, PageRequest pageRequest);
}

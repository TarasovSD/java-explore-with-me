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

    List<Event> findEventsByInitiatorId(User initiatorId);

    @Query("select e from Event e where e.initiatorId in :users and e.state in :states and e.category in :categories " +
            "and e.eventDate > :rangeStart and e.eventDate < :rangeEnd")
    List<Event> getEvents(List<User> users, List<Status> states, List<Long> categories,
                          LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest pageRequest);

    @Query("select e from Event e where  e.category in :categories and e.paid in :paid " +
            "and e.eventDate > :rangeStart and e.eventDate < :rangeEnd order by e.eventDate desc")
    List<Event> getEventsByFilterSortByEventDate(List<Long> categories, List<Boolean> paid, LocalDateTime rangeStart,
                                                 LocalDateTime rangeEnd, PageRequest pageRequest);

    @Query("select e from Event e where  e.category in :categories and e.paid in :paid " +
            "and e.eventDate > :rangeStart and e.eventDate < :rangeEnd")
    List<Event> getEventsByFilterWithoutSort(List<Long> categories, List<Boolean> paid, LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd, PageRequest pageRequest);
}

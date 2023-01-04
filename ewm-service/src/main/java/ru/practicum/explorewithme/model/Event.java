package ru.practicum.explorewithme.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "events")
@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "annotation")
    private String annotation;
    @Column(name = "category")
    private Long category;
    @Column(name = "description")
    private String description;
    @Column(name = "event_date")
    private LocalDateTime eventDate;
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
    @Column(name = "confirmed_requests")
    private Long confirmedRequests;
    @Column(name = "paid")
    private Boolean paid;
    @Column(name = "participant_limit")
    private Long participantLimit;
    @Column(name = "request_moderation")
    private Boolean requestModeration;
    @Column(name = "title")
    private String title;
    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private User initiatorId;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private Status state;
    @Column(name = "views")
    private Long views;


    @ManyToMany
    @JoinTable(name = "events_compilations",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "compilation_id"))
    private List<Compilation> compilations = new ArrayList<>();

    public Event(Long id, String annotation, Long category, String description, LocalDateTime eventDate, Location location, Long confirmedRequests, Boolean paid, Long participantLimit, Boolean requestModeration, String title, LocalDateTime createdOn, User initiatorId, LocalDateTime publishedOn, Status state, Long views) {
        this.id = id;
        this.annotation = annotation;
        this.category = category;
        this.description = description;
        this.eventDate = eventDate;
        this.location = location;
        this.confirmedRequests = confirmedRequests;
        this.paid = paid;
        this.participantLimit = participantLimit;
        this.requestModeration = requestModeration;
        this.title = title;
        this.createdOn = createdOn;
        this.initiatorId = initiatorId;
        this.publishedOn = publishedOn;
        this.state = state;
        this.views = views;
    }

    public void addCompilation(Compilation compilation) {
        compilations.add(compilation);
    }
}

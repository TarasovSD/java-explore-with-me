package ru.practicum.explorewithme.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    @Column(name = "annotation", length = 500)
    @NotBlank
    private String annotation;
//    @Column(name = "category", nullable = false)
    @JoinColumn(name = "category", nullable = false)
    @ManyToOne
    private Category category;
    @Column(name = "description", length = 5000)
    @NotBlank
    private String description;
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    @Column(nullable = false)
    private Location location;
    @Column(name = "paid", nullable = false)
    private Boolean paid;
    @Column(name = "participant_limit", nullable = false)
    private Long participantLimit;
    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration;
    @Column(name = "title", length = 2000)
    @NotBlank
    private String title;
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiatorId;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @Column(name = "state", length = 100, nullable = false)
    @Enumerated(EnumType.STRING)
    private Status state;

    @ManyToMany (mappedBy = "events")
    private Set<Compilation> compilations = new HashSet<>();

    public Event(Long id, String annotation, Category category, String description, LocalDateTime eventDate, Location location, Boolean paid, Long participantLimit, Boolean requestModeration, String title, LocalDateTime createdOn, User initiatorId, LocalDateTime publishedOn, Status state) {
        this.id = id;
        this.annotation = annotation;
        this.category = category;
        this.description = description;
        this.eventDate = eventDate;
        this.location = location;
        this.paid = paid;
        this.participantLimit = participantLimit;
        this.requestModeration = requestModeration;
        this.title = title;
        this.createdOn = createdOn;
        this.initiatorId = initiatorId;
        this.publishedOn = publishedOn;
        this.state = state;
    }

    public void addCompilation(Compilation compilation) {
        compilations.add(compilation);
    }
}

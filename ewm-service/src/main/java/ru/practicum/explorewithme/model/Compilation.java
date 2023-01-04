package ru.practicum.explorewithme.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "compilations")
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "pined")
    private Boolean pinned;
    @Column(name = "title")
    private String title;

    //    @ManyToMany(mappedBy = "compilations")
    @ManyToMany
    @JoinTable(name = "events_compilations",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    private List<Event> eventList = new ArrayList<>();

    public Compilation(Long id, Boolean pinned, String title) {
        this.id = id;
        this.pinned = pinned;
        this.title = title;
    }

    public void addEvent(Event event) {
        eventList.add(event);
    }
}

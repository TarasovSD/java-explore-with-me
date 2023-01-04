package ru.practicum.explorewithme.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @NotBlank
    @Column(name = "name")
    private String name;
    @Email
    @Column(name = "email")
    private String email;

    @OneToMany(mappedBy = "id", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Event> events;

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public void addEvent(Event event) {
        events.add(event);
    }
}
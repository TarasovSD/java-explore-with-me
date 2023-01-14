package ru.practicum.explorewithme.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class UserDto {

    private Long id;
    private String name;
    private String email;
}

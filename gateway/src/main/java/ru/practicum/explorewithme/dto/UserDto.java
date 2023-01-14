package ru.practicum.explorewithme.dto;

import lombok.*;
import ru.practicum.explorewithme.Create;
import ru.practicum.explorewithme.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class UserDto {


    private Long id;

    @NotBlank(groups = {Create.class})
    private String name;

    @Email(groups = {Create.class, Update.class})
    @NotBlank(groups = {Create.class, Update.class})
    private String email;
}

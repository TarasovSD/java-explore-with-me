package ru.practicum.explorewithme.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.explorewithme.Create;
import ru.practicum.explorewithme.Update;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestDto {
    @NotNull(groups = {Update.class})
    private Long id;
    @NotNull(groups = {Create.class})
    private LocalDateTime created;
    @NotNull(groups = {Create.class})
    private Long event;
    @NotNull(groups = {Create.class})
    private Long requester;
    @NotBlank
    private String status;
}

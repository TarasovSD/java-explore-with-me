package ru.practicum.explorewithme.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.explorewithme.Create;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    private Long id;
    @NotNull(groups = {Create.class})
    private List<Long> events;
    @NotNull(groups = {Create.class})
    private Boolean pinned;
    @NotNull(groups = {Create.class})
    private String title;

}

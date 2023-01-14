package ru.practicum.explorewithme.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.CompilationFullDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.model.Compilation;

import java.util.List;

@UtilityClass
public class CompilationMapper {
    public Compilation toCompilation(CompilationDto compilationDto) {
        return new Compilation(compilationDto.getId(), compilationDto.getPinned(), compilationDto.getTitle());
    }

    public CompilationFullDto toCompilationFullDto(Compilation compilation, List<EventFullDto> events) {
        return new CompilationFullDto(compilation.getId(), events, compilation.getPinned(), compilation.getTitle());
    }
}

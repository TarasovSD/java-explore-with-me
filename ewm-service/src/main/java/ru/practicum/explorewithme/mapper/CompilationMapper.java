package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.CompilationFullDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.model.Compilation;

import java.util.List;

public class CompilationMapper {
    public static Compilation toCompilation(CompilationDto compilationDto) {
        return new Compilation(compilationDto.getId(), compilationDto.getPinned(), compilationDto.getTitle());
    }

    public static CompilationFullDto toCompilationFullDto(Compilation compilation, List<EventFullDto> events) {
        return new CompilationFullDto(compilation.getId(), events, compilation.getPinned(), compilation.getTitle());
    }
}

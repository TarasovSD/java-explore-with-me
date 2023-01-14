package ru.practicum.explorewithme.service.compilation;

import org.springframework.data.domain.PageRequest;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.CompilationFullDto;

import java.util.List;

public interface CompilationService {


    CompilationFullDto create(CompilationDto compilationDto);

    void removeById(Long compId);

    CompilationFullDto getById(Long compId);

    List<CompilationFullDto> get(Boolean pinned, PageRequest pageRequest);

    void addEvent(Long eventId, Long compId);

    void pin(Long compId);

    void removeEvent(Long eventId, Long compId);

    void unpin(Long compId);
}

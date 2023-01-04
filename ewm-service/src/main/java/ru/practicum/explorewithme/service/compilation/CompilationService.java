package ru.practicum.explorewithme.service.compilation;

import org.springframework.data.domain.PageRequest;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.CompilationFullDto;

import java.util.List;

public interface CompilationService {


    CompilationFullDto createCompilation(CompilationDto compilationDto);

    void removeCompilationById(Long compId);

    CompilationFullDto getCompilationById(Long compId);

    List<CompilationFullDto> getCompilations(Boolean pinned, PageRequest pageRequest);

    void addEventToCompilation(Long eventId, Long compId);

    void pinCompilation(Long compId);

    void removeEventFromCompilation(Long eventId, Long compId);

    void unpinCompilation(Long compId);
}

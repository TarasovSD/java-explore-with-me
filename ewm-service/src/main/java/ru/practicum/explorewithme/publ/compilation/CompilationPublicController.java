package ru.practicum.explorewithme.publ.compilation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.compilation.CompilationFullDto;
import ru.practicum.explorewithme.service.compilation.CompilationService;

import java.util.List;

@RestController
@RequestMapping(path = "/compilations")
@Slf4j
public class CompilationPublicController {

    private final CompilationService compilationService;

    public CompilationPublicController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @GetMapping("/{compId}")
    public CompilationFullDto getById(@PathVariable Long compId) {
        log.info("Запрос информации о подборке с ID {}", compId);
        return compilationService.getById(compId);
    }

    @GetMapping()
    public List<CompilationFullDto> get(@RequestParam(required = false) Boolean pinned,
                                        @RequestParam Integer from,
                                        @RequestParam Integer size) {
        log.info("Запрос подборок. Параметр pinned: {}", pinned);
        int page = from / size;
        final PageRequest pageRequest = PageRequest.of(page, size);
        return compilationService.get(pinned, pageRequest);
    }
}

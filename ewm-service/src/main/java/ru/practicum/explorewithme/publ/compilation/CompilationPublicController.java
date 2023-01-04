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
    public CompilationFullDto getCompilationById(@PathVariable Long compId) {
        log.info("Запрос информации о подборке с ID:" + compId);
        return compilationService.getCompilationById(compId);
    }

    @GetMapping()
    public List<CompilationFullDto> getCompilations(@RequestParam Boolean pinned,
                                                    @RequestParam Integer from,
                                                    @RequestParam Integer size) {
        log.info("Запрос подборок. Параметр pinned: " + pinned);
        int page = from / size;
        final PageRequest pageRequest = PageRequest.of(page, size);
        return compilationService.getCompilations(pinned, pageRequest);
    }
}

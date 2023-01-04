package ru.practicum.explorewithme.admin.compilations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.CompilationFullDto;
import ru.practicum.explorewithme.service.compilation.CompilationService;

@RestController
@RequestMapping(path = "/admin/compilations")
@Slf4j
public class CompilationAdminController {

    private final CompilationService compilationService;

    public CompilationAdminController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping()
    public CompilationFullDto createCompilation(@RequestBody CompilationDto compilationDto) {
        log.info("Подборка создана");
        return compilationService.createCompilation(compilationDto);
    }

    @DeleteMapping("/{compId}")
    public void removeCompilationById(@PathVariable Long compId) {
        log.info("Запрос на удаление подборки с ID:" + compId);
        compilationService.removeCompilationById(compId);
    }

    @GetMapping("/{compId}")
    public CompilationFullDto getCompilationById(@PathVariable Long compId) {
        log.info("Запрос информации о подборке с ID:" + compId);
        return compilationService.getCompilationById(compId);
    }

    @PatchMapping("/{compId}/events/{eventId}")
    public void addEventToCompilation(@PathVariable Long compId, @PathVariable Long eventId) {
        log.info("Запрос на добавление события с ID {} в подборку с ID {}", eventId, compId);
        compilationService.addEventToCompilation(eventId, compId);
    }

    @PatchMapping("/{compId}/pin")
    public void pinCompilation(@PathVariable Long compId) {
        log.info("Запрос на закрепление на главной странице подборки с ID {}", compId);
        compilationService.pinCompilation(compId);
    }

    @DeleteMapping("/{compId}/events/{eventId}")
    public void removeEventFromCompilation(@PathVariable Long compId, @PathVariable Long eventId) {
        log.info("Запрос на удаление события с ID {} из подборки с ID {}", eventId, compId);
        compilationService.removeEventFromCompilation(eventId, compId);
    }

    @DeleteMapping("/{compId}/pin")
    public void unpinCompilation(@PathVariable Long compId) {
        log.info("Запрос на открепление от главной страницы подборки с ID {}", compId);
        compilationService.unpinCompilation(compId);
    }
}

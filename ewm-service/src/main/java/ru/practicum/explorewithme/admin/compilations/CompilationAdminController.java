package ru.practicum.explorewithme.admin.compilations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.CompilationFullDto;
import ru.practicum.explorewithme.service.compilation.CompilationService;

@RestController
@RequestMapping(path = "/admin/compilations")
@Slf4j
@RequiredArgsConstructor
public class CompilationAdminController {

    private final CompilationService compilationService;

    @PostMapping()
    public CompilationFullDto create(@RequestBody CompilationDto compilationDto) {
        log.info("Подборка создана");
        return compilationService.create(compilationDto);
    }

    @DeleteMapping("/{compId}")
    public void removeById(@PathVariable Long compId) {
        log.info("Запрос на удаление подборки с ID {}", compId);
        compilationService.removeById(compId);
    }

    @GetMapping("/{compId}")
    public CompilationFullDto getById(@PathVariable Long compId) {
        log.info("Запрос информации о подборке с ID {}", compId);
        return compilationService.getById(compId);
    }

    @PatchMapping("/{compId}/events/{eventId}")
    public void addEvent(@PathVariable Long compId, @PathVariable Long eventId) {
        log.info("Запрос на добавление события с ID {} в подборку с ID {}", eventId, compId);
        compilationService.addEvent(eventId, compId);
    }

    @PatchMapping("/{compId}/pin")
    public void pin(@PathVariable Long compId) {
        log.info("Запрос на закрепление на главной странице подборки с ID {}", compId);
        compilationService.pin(compId);
    }

    @DeleteMapping("/{compId}/events/{eventId}")
    public void removeEvent(@PathVariable Long compId, @PathVariable Long eventId) {
        log.info("Запрос на удаление события с ID {} из подборки с ID {}", eventId, compId);
        compilationService.removeEvent(eventId, compId);
    }

    @DeleteMapping("/{compId}/pin")
    public void unpin(@PathVariable Long compId) {
        log.info("Запрос на открепление от главной страницы подборки с ID {}", compId);
        compilationService.unpin(compId);
    }
}

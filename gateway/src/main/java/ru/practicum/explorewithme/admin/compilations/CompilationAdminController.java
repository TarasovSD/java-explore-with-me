package ru.practicum.explorewithme.admin.compilations;

import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.explorewithme.Create;
import ru.practicum.explorewithme.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.dto.compilation.CompilationFullDto;

import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/admin/compilations")
@Slf4j
@Validated
public class CompilationAdminController {

    private final WebClient webClient;

    public CompilationAdminController(@Value("${base.path}") String basePath, WebClient.Builder builder) {
        webClient = builder.baseUrl(basePath).build();
    }

    @PostMapping()
    public CompilationFullDto create(@Validated(Create.class) @RequestBody CompilationDto compilationDto) {
        log.info("Создание новой подборки");
        return webClient
                .post()
                .uri("/admin/compilations")
                .body(Mono.just(compilationDto), CompilationDto.class)
                .retrieve()
                .bodyToMono(CompilationFullDto.class)
                .block();
    }

    @DeleteMapping("/{compId}")
    public String removeById(@PositiveOrZero @PathVariable Long compId) {
        log.info("Удаление подборки");
        return webClient
                .delete()
                .uri("/admin/compilations/" + compId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @PatchMapping("/{compId}/events/{eventId}")
    public String addToCompilation(@PositiveOrZero @PathVariable Long compId,
                                   @PositiveOrZero @PathVariable Long eventId) {
        log.info("Добавление события с ID {} в подборку с ID {}", eventId, compId);
        return webClient
                .patch()
                .uri("/admin/compilations/{compId}/events/{eventId}", compId, eventId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @PatchMapping("/{compId}/pin")
    public String pin(@PositiveOrZero @PathVariable Long compId) {
        log.info("Закрепление  на главной странице подборки с ID {}", compId);
        return webClient
                .patch()
                .uri("/admin/compilations/{compId}/pin", compId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @DeleteMapping("/{compId}/events/{eventId}")
    public String removeFromCompilation(@PositiveOrZero @PathVariable Long compId,
                                        @PositiveOrZero @PathVariable Long eventId) {
        log.info("Удаление события с ID {} из подборки с ID {}", eventId, compId);
        return webClient
                .delete()
                .uri("/admin/compilations/{compId}/events/{eventId}", compId, eventId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @DeleteMapping("/{compId}/pin")
    public String unpin(@PositiveOrZero @PathVariable Long compId) {
        log.info("Открепление от главной страницы подборки с ID {}", compId);
        return webClient
                .delete()
                .uri("/admin/compilations/{compId}/pin", compId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}

package ru.practicum.explorewithme.publ.compilation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.explorewithme.dto.compilation.CompilationFullDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/compilations")
@Slf4j
@Validated
public class CompilationPublicController {

    private final WebClient webClient;

    public CompilationPublicController(@Value("${base.path}") String basePath, WebClient.Builder builder) {
        webClient = builder.baseUrl(basePath).build();
    }

    @GetMapping("/{compId}")
    public CompilationFullDto getById(@PathVariable Long compId) {
        log.info("Запрос подборки с ID {}", compId);
        return webClient
                .get()
                .uri("/compilations/{compId}", compId)
                .retrieve()
                .bodyToMono(CompilationFullDto.class)
                .block();
    }

    @GetMapping()
    public CompilationFullDto[] get(@RequestParam(required = false) Boolean pinned,
                                    @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                    @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Запрос подборок");
        return webClient
                .get()
                .uri("/compilations?pinned={pinned}&from={from}&size={size}", pinned, from, size)
                .retrieve()
                .bodyToMono(CompilationFullDto[].class)
                .block();
    }
}

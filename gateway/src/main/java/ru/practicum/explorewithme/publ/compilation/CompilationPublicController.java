package ru.practicum.explorewithme.publ.compilation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.explorewithme.dto.compilation.CompilationFullDto;

@RestController
@RequestMapping(path = "/compilations")
@Slf4j
@Validated
public class CompilationPublicController {

    private final WebClient webClient;

    public CompilationPublicController(WebClient.Builder builder) {
        webClient = builder.baseUrl("http://localhost:9098/").build();
    }

    @GetMapping("/{compId}")
    public CompilationFullDto getCompilationById(@PathVariable Long compId) {
        log.info("Запрос подборки с ID: " + compId);
        return webClient
                .get()
                .uri("/compilations/{compId}", compId)
                .retrieve()
                .bodyToMono(CompilationFullDto.class)
                .block();
    }

    @GetMapping()
    public CompilationFullDto[] getCompilations(@RequestParam(defaultValue = "false") Boolean pinned,
                                                @RequestParam(defaultValue = "0") Integer from,
                                                @RequestParam(defaultValue = "10") Integer size) {
        log.info("Запрос подборок");
        return webClient
                .get()
                .uri("/compilations?pinned={pinned}&from={from}&size={size}", pinned, from, size)
                .retrieve()
                .bodyToMono(CompilationFullDto[].class)
                .block();
    }
}

package ru.practicum.explorewithme.publ.categories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.explorewithme.dto.CategoryDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/categories")
@Slf4j
@Validated
public class CategoryPublicController {

    private final WebClient webClient;

    public CategoryPublicController(@Value("${base.path}") String basePath, WebClient.Builder builder) {
        webClient = builder.baseUrl(basePath).build();
    }

    @GetMapping("/{id}")
    public CategoryDto getById(@PathVariable Long id) {
        log.info("Запрос категории с ID {}", id);
        return webClient
                .get()
                .uri("/categories/" + id)
                .retrieve()
                .bodyToMono(CategoryDto.class)
                .block();
    }

    @GetMapping()
    public CategoryDto[] get(@PositiveOrZero @RequestParam(defaultValue = "0") Long from,
                             @Positive @RequestParam(defaultValue = "10") Long size) {
        log.info("Запрос всех категорий");
        return webClient
                .get()
                .uri("/categories?from={from}&size={size}", from, size)
                .retrieve()
                .bodyToMono(CategoryDto[].class)
                .block();
    }
}

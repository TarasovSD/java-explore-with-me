package ru.practicum.explorewithme.publ.categories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.explorewithme.dto.CategoryDto;

@RestController
@RequestMapping("/categories")
@Slf4j
@Validated
public class CategoryPublicController {

    private final WebClient webClient;

    public CategoryPublicController(WebClient.Builder builder) {
        webClient = builder.baseUrl("http://localhost:9092/").build();
    }

    @GetMapping("/{id}")
    public CategoryDto getCategoryById(@PathVariable Long id) {
        log.info("Запрос категории с ID: " + id);
        return webClient
                .get()
                .uri("/categories/" + id)
                .retrieve()
                .bodyToMono(CategoryDto.class)
                .block();
    }

    @GetMapping()
    public CategoryDto[] getCategories() {
        log.info("Запрос всех категорий");
        return webClient
                .get()
                .uri("/categories")
                .retrieve()
                .bodyToMono(CategoryDto[].class)
                .block();
    }
}

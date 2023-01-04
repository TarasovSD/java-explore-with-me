package ru.practicum.explorewithme.admin.categories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.explorewithme.Create;
import ru.practicum.explorewithme.Update;
import ru.practicum.explorewithme.dto.CategoryDto;

@RestController
@RequestMapping("/admin/categories")
@Slf4j
@Validated
public class CategoryController {

    private final WebClient webClient;

    public CategoryController(WebClient.Builder builder) {
        webClient = builder.baseUrl("http://localhost:9092/").build();
    }

    @PostMapping()
    public CategoryDto createCategory(@Validated(Create.class) @RequestBody CategoryDto categoryDto) {
        log.info("Создание новой категории");
        return webClient
                .post()
                .uri("/admin/categories")
                .body(Mono.just(categoryDto), CategoryDto.class)
                .retrieve()
                .bodyToMono(CategoryDto.class)
                .block();
    }

    @PatchMapping()
    public CategoryDto updateCategory(@Validated(Update.class) @RequestBody CategoryDto categoryDto) {
        log.info("Обновление категории");
        return webClient
                .patch()
                .uri("/admin/categories")
                .body(Mono.just(categoryDto), CategoryDto.class)
                .retrieve()
                .bodyToMono(CategoryDto.class)
                .block();
    }

    @DeleteMapping("/{id}")
    public String removeCategoryById(@PathVariable Long id) {
        log.info("Удаление пользователя");
        return webClient
                .delete()
                .uri("/admin/categories/" + id)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}

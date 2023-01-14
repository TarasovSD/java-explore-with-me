package ru.practicum.explorewithme.publ.categories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.CategoryDto;
import ru.practicum.explorewithme.service.category.CategoryService;

import java.util.List;

@RestController
@RequestMapping(path = "/categories")
@Slf4j
public class CategoryPublicController {

    private final CategoryService categoryService;

    public CategoryPublicController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/{catId}")
    public CategoryDto findById(@PathVariable long catId) {
        log.info("Выполнен запрос категории с ID {}", catId);
        return categoryService.findById(catId);
    }

    @GetMapping()
    public List<CategoryDto> find(@RequestParam(required = false) Integer from,
                                  @RequestParam(required = false) Integer size) {
        log.info("Выполнен запрос всех категорий");
        int page = from / size;
        final PageRequest pageRequest = PageRequest.of(page, size);
        return categoryService.find(pageRequest);
    }
}

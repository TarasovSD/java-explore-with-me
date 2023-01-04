package ru.practicum.explorewithme.publ.categories;

import lombok.extern.slf4j.Slf4j;
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
    public CategoryDto findCategoryById(@PathVariable Long catId) {
        log.info("Выполнен запрос категории с ID: " + catId);
        return categoryService.findCategoryById(catId);
    }

    @GetMapping()
    public List<CategoryDto> findCategories() {
        log.info("Выполнен запрос всех категорий");
        return categoryService.findCategories();
    }
}

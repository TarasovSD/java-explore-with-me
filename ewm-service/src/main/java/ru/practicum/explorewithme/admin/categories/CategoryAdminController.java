package ru.practicum.explorewithme.admin.categories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.CategoryDto;
import ru.practicum.explorewithme.service.category.CategoryService;

@RestController
@RequestMapping(path = "/admin/categories")
@Slf4j
public class CategoryAdminController {

    private final CategoryService categoryService;

    public CategoryAdminController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping()
    public CategoryDto createCategory(@RequestBody CategoryDto categoryDto) {
        log.info("Категория создана");
        return categoryService.createCategory(categoryDto);
    }

    @PatchMapping()
    public CategoryDto updateCategory(@RequestBody CategoryDto categoryDto) {
        log.info("Категория id {} с обновлена", categoryDto.getId());
        return categoryService.updateCategory(categoryDto);
    }

    @DeleteMapping("/{id}")
    public void removeUserById(@PathVariable Long id) {
        log.info("Запрос на удаление категории с ID:" + id);
        categoryService.removeUserById(id);
    }
}

package ru.practicum.explorewithme.admin.categories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.CategoryDto;
import ru.practicum.explorewithme.service.category.CategoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/categories")
@Slf4j
public class CategoryAdminController {

    private final CategoryService categoryService;

    @PostMapping()
    public CategoryDto create(@RequestBody CategoryDto categoryDto) {
        log.info("Категория создана");
        return categoryService.create(categoryDto);
    }

    @PatchMapping()
    public CategoryDto update(@RequestBody CategoryDto categoryDto) {
        log.info("Категория id {} с обновлена", categoryDto.getId());
        return categoryService.update(categoryDto);
    }

    @DeleteMapping("/{id}")
    public void removeById(@PathVariable Long id) {
        log.info("Запрос на удаление категории с ID {}", id);
        categoryService.removeById(id);
    }
}

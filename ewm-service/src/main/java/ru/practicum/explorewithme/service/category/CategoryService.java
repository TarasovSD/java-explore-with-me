package ru.practicum.explorewithme.service.category;

import ru.practicum.explorewithme.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(CategoryDto categoryDto);

    CategoryDto updateCategory(CategoryDto categoryDto);

    void removeUserById(Long id);

    CategoryDto findCategoryById(Long id);

    List<CategoryDto> findCategories();

}

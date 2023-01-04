package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.CategoryDto;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.model.Category;

public class CategoryMapper {
    public static Category toCategory(CategoryDto categoryDto) {
        return new Category(categoryDto.getId(), categoryDto.getName());
    }

    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }

    public static EventFullDto.CategoryDtoForEvent toCategoryDtoForEvent(Category category) {
        return new EventFullDto.CategoryDtoForEvent(category.getId(), category.getName());
    }
}

package ru.practicum.explorewithme.service.category;

import org.springframework.data.domain.PageRequest;
import ru.practicum.explorewithme.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto create(CategoryDto categoryDto);

    CategoryDto update(CategoryDto categoryDto);

    void removeById(Long id);

    CategoryDto findById(long id);

    List<CategoryDto> find(PageRequest pageRequest);

}

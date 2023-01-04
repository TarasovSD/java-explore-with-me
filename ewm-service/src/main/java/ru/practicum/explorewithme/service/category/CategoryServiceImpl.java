package ru.practicum.explorewithme.service.category;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.repository.CategoryRepository;
import ru.practicum.explorewithme.dto.CategoryDto;
import ru.practicum.explorewithme.model.Category;
import ru.practicum.explorewithme.exceptions.CategoryNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = CategoryMapper.toCategory(categoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(CategoryDto categoryDto) {
        Category categoryToUpdate = CategoryMapper.toCategory(categoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(categoryToUpdate));
    }

    @Override
    @Transactional
    public void removeUserById(Long id) {
        categoryRepository.delete(CategoryMapper.toCategory(findCategoryById(id)));
    }

    @Override
    public CategoryDto findCategoryById(Long id) {
        if (categoryRepository.findById(id).isEmpty()) {
            throw new CategoryNotFoundException("Категория не найдена");
        }
        return CategoryMapper.toCategoryDto(categoryRepository.findById(id).get());
    }

    @Override
    public List<CategoryDto> findCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDto> categoriesDto = new ArrayList<>();
        for (Category category : categories) {
            categoriesDto.add(CategoryMapper.toCategoryDto(category));
        }
        return categoriesDto;
    }
}

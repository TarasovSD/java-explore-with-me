package ru.practicum.explorewithme.service.category;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.CategoryDto;
import ru.practicum.explorewithme.exceptions.CategoryNotFoundException;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.model.Category;
import ru.practicum.explorewithme.repository.CategoryRepository;

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
    public CategoryDto create(CategoryDto categoryDto) {
        Category category = CategoryMapper.toCategory(categoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryDto categoryDto) {
        Category categoryToUpdate = CategoryMapper.toCategory(categoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(categoryToUpdate));
    }

    @Override
    @Transactional
    public void removeById(Long id) {
        categoryRepository.findById(id).ifPresent((c) -> categoryRepository.deleteById(c.getId()));
    }

    @Override
    public CategoryDto findById(long id) {
        Category category = categoryRepository.findById(id).orElseThrow(()
                -> new CategoryNotFoundException("Категория не найдена"));
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public List<CategoryDto> find(PageRequest pageRequest) {
        List<Category> categories = categoryRepository.findAll(pageRequest).getContent();
        List<CategoryDto> categoriesDto = new ArrayList<>();
        for (Category category : categories) {
            categoriesDto.add(CategoryMapper.toCategoryDto(category));
        }
        return categoriesDto;
    }
}

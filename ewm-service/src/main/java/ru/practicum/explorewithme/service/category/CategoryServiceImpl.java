package ru.practicum.explorewithme.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.CategoryDto;
import ru.practicum.explorewithme.exceptions.CategoryNotFoundException;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.model.Category;
import ru.practicum.explorewithme.repository.CategoryRepository;

import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryDto create(CategoryDto categoryDto) {
        Category category = CategoryMapper.toCategory(categoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryDto.getId()).orElseThrow(()
                -> new CategoryNotFoundException("Категория не найдена"));
        category.setName(categoryDto.getName());
        return CategoryMapper.toCategoryDto(category);
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
        return CategoryMapper.toCategoryDtos(categories);
    }
}

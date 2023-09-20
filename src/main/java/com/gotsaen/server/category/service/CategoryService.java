package com.gotsaen.server.category.service;

import com.gotsaen.server.category.dto.CategoryDto;
import com.gotsaen.server.category.entity.Category;
import com.gotsaen.server.category.repository.CategoryRepository;
import com.gotsaen.server.exception.BusinessLogicException;
import com.gotsaen.server.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    @Transactional(propagation = Propagation.REQUIRED)
    public void createCategory(CategoryDto requestBody) {
        Optional<Category> category = categoryRepository.findByCategoryName(requestBody.getCategoryName());
        if(category.isPresent())
            throw new BusinessLogicException(ExceptionCode.CATEGORY_EXISTS);

        Category savedCategory = new Category();
        savedCategory.setCategoryName(requestBody.getCategoryName());
        categoryRepository.save(savedCategory);
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CATEGORY_NOT_FOUND));

        categoryRepository.delete(category);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateCategory(Long categoryId, CategoryDto requestBody) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CATEGORY_NOT_FOUND));

        Optional.ofNullable(requestBody.getCategoryName())
                .ifPresent(categoryName -> category.setCategoryName(categoryName));

        categoryRepository.save(category);
    }
}

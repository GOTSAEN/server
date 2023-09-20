package com.gotsaen.server.category.controller;

import com.gotsaen.server.category.dto.CategoryDto;
import com.gotsaen.server.category.entity.Category;
import com.gotsaen.server.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/categories")
@Validated
@Slf4j
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity postCategory(@Valid @RequestBody CategoryDto requestBody){
        categoryService.createCategory(requestBody);

        return new ResponseEntity(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Category>> getCategories(){
        return new ResponseEntity<>(categoryService.getCategories(), HttpStatus.OK);
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity updateCategory(@PathVariable Long categoryId, @Valid @RequestBody CategoryDto requestBody) {
        categoryService.updateCategory(categoryId, requestBody);

        return new ResponseEntity<>(HttpStatus.OK);
    }
    @DeleteMapping("/{categoryId}")
    public ResponseEntity deleteCategory(@PathVariable Long categoryId){
        categoryService.deleteCategory(categoryId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

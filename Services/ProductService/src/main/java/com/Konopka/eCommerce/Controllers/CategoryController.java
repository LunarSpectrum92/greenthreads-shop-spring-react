package com.Konopka.eCommerce.Controllers;


import com.Konopka.eCommerce.DTO.CategoryDto;
import com.Konopka.eCommerce.Services.CategoryService;
import com.Konopka.eCommerce.models.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }


    @PostMapping("/category")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Integer> addCategory(@RequestBody CategoryDto categoryDto) {
        return categoryService.createCategory(categoryDto);
    }


    @GetMapping("/categories/parents")
    public ResponseEntity<List<CategoryDto>> findParrentCategories(@RequestParam Integer categoryId) {
        return categoryService.findParrentCategories(categoryId);
    }


    @DeleteMapping("/category")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Integer> deleteCategory(@RequestParam Integer categoryId) {
        return categoryService.DeleteCategory(categoryId);
    }


}

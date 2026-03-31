package com.Konopka.eCommerce.services;

import com.Konopka.eCommerce.DTO.CategoryDto;
import com.Konopka.eCommerce.DTO.CategoryDtoMapper;
import com.Konopka.eCommerce.Repository.CategoryRepo;
import com.Konopka.eCommerce.Services.CategoryService;
import com.Konopka.eCommerce.models.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepo categoryRepo;

    @Mock
    private CategoryDtoMapper categoryDtoMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void findById_CategoryExists_ReturnsOptionalDto() {
        Integer id = 1;
        Category category = Category.builder().categoryId(id).categoryName("Electronics").build();
        CategoryDto dto = new CategoryDto(id, "Electronics", null);

        when(categoryRepo.findById(id)).thenReturn(Optional.of(category));
        when(categoryDtoMapper.apply(category)).thenReturn(dto);

        Optional<CategoryDto> result = categoryService.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().categoryName()).isEqualTo("Electronics");
    }

    @Test
    void createCategory_AlreadyExists_ReturnsConflict() {
        CategoryDto dto = new CategoryDto(1, "Existing", null);
        when(categoryRepo.findById(1)).thenReturn(Optional.of(new Category()));

        ResponseEntity<Integer> response = categoryService.createCategory(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(categoryRepo, never()).save(any());
    }

    @Test
    void createCategory_NewCategory_ReturnsCreated() {
        CategoryDto dto = new CategoryDto(1, "New", null);
        when(categoryRepo.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<Integer> response = categoryService.createCategory(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(1);
        verify(categoryRepo).save(any(Category.class));
    }

    @Test
    void deleteCategory_IdNotExists_ReturnsNotFound() {
        Integer id = 99;
        when(categoryRepo.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<Integer> response = categoryService.DeleteCategory(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteCategory_IdExists_ReturnsAccepted() {
        Integer id = 1;
        when(categoryRepo.findById(id)).thenReturn(Optional.of(new Category()));

        ResponseEntity<Integer> response = categoryService.DeleteCategory(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        verify(categoryRepo).deleteById(id);
    }

    @Test
    void updateCategory_NotExists_ReturnsConflict() {
        CategoryDto dto = new CategoryDto(1, "Update", null);
        when(categoryRepo.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<CategoryDto> response = categoryService.updateCategory(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void findParrentCategories_HasParents_ReturnsList() {
        Category parent = Category.builder().categoryId(10).categoryName("Parent").build();
        Category child = Category.builder().categoryId(20).categoryName("Child").categoryParentId(parent).build();
        
        CategoryDto parentDto = new CategoryDto(10, "Parent", null);

        when(categoryRepo.findById(20)).thenReturn(Optional.of(child));
        when(categoryDtoMapper.apply(parent)).thenReturn(parentDto);

        ResponseEntity<List<CategoryDto>> response = categoryService.findParrentCategories(20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).categoryName()).isEqualTo("Parent");
    }

    @Test
    void findParrentCategories_NoParent_ReturnsNotFound() {
        Category category = Category.builder().categoryId(1).categoryName("Root").categoryParentId(null).build();
        when(categoryRepo.findById(1)).thenReturn(Optional.of(category));

        ResponseEntity<List<CategoryDto>> response = categoryService.findParrentCategories(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
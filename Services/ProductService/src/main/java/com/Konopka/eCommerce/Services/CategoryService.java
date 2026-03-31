package com.Konopka.eCommerce.Services;

import com.Konopka.eCommerce.DTO.CategoryDto;
import com.Konopka.eCommerce.DTO.CategoryDtoMapper;
import com.Konopka.eCommerce.models.Category;
import com.Konopka.eCommerce.Repository.CategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    CategoryRepo categoryRepo;
    CategoryDtoMapper categoryDtoMapper;


    @Autowired
    public CategoryService(CategoryRepo categoryRepo, CategoryDtoMapper categoryDtoMapper) {
        this.categoryRepo = categoryRepo;
        this.categoryDtoMapper = categoryDtoMapper;
    }


    public Optional<CategoryDto> findById(Integer id) {
        return Optional.of(categoryDtoMapper.apply(categoryRepo.findById(id).get()));
    }


    public List<Category> findAll() {
        return categoryRepo.findAll();
    }


    public ResponseEntity<Integer> createCategory(CategoryDto categoryDto) {
        if (categoryRepo.findById(categoryDto.categoryId()).isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }


        if (categoryDto.categoryParentId() != null) {
            Category category = Category.builder()
                    .categoryId(categoryDto.categoryId())
                    .categoryName(categoryDto.categoryName())
                    .categoryParentId(categoryRepo.findById(categoryDto.categoryParentId()).get())
                    .build();
            categoryRepo.save(category);
            return new ResponseEntity<>(category.getCategoryId(), HttpStatus.CREATED);
        }

        Category category = Category.builder()
                .categoryId(categoryDto.categoryId())
                .categoryName(categoryDto.categoryName())
                .build();

        categoryRepo.save(category);

        return new ResponseEntity<>(category.getCategoryId(), HttpStatus.CREATED);
    }

    public ResponseEntity<Integer> DeleteCategory(Integer categoryId) {
        if (categoryRepo.findById(categoryId).isPresent()) {
            categoryRepo.deleteById(categoryId);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    public ResponseEntity<CategoryDto> updateCategory(CategoryDto categoryDto) {
        if (categoryRepo.findById(categoryDto.categoryId()).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        Category category = Category.builder()
                .categoryId(categoryDto.categoryId())
                .categoryName(categoryDto.categoryName())
                .categoryParentId(categoryRepo.findById(categoryDto.categoryParentId()).get())
                .build();


        categoryRepo.save(category);

        return new ResponseEntity<>(
                categoryDto
                , HttpStatus.CONFLICT);
    }


    public ResponseEntity<List<CategoryDto>> findParrentCategories(Integer categoryId) {
        Optional<Category> category = categoryRepo.findById(categoryId);
        if (category.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<Category> parents = new ArrayList<>();
        parents.add(category.get().getCategoryParentId());

        int i = 0;
        if (parents.get(i) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Category parent = parents.get(i);
        while (parent.getCategoryParentId() != null) {
            System.out.println(parent);
            parent = parents.get(i);
            System.out.println(parent.getCategoryParentId());
            if (parent.getCategoryParentId() == null) {
                break;
            }
            parents.add(parent.getCategoryParentId());
            i++;
        }

        List<CategoryDto> ParentsToDto = parents.stream()
                .map((parent1) -> categoryDtoMapper.apply(parent1))
                .toList();
        return new ResponseEntity<>(ParentsToDto, HttpStatus.OK);

    }


}

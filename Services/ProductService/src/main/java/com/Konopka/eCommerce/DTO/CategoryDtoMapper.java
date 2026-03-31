package com.Konopka.eCommerce.DTO;

import com.Konopka.eCommerce.models.Category;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class CategoryDtoMapper implements Function<Category, CategoryDto> {


    @Override
    public CategoryDto apply(Category category) {
        Integer parentCategoryId = null;

        if (category.getCategoryParentId() != null) {
            parentCategoryId = category.getCategoryParentId().getCategoryId();
        }

        return new CategoryDto(
                category.getCategoryId(),
                category.getCategoryName(),
                parentCategoryId
        );
    }

}

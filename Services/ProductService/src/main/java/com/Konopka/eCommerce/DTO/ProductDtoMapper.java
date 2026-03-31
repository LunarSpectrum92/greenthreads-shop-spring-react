package com.Konopka.eCommerce.DTO;


import com.Konopka.eCommerce.models.Category;
import com.Konopka.eCommerce.models.Product;
import com.Konopka.eCommerce.Repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductDtoMapper implements Function<Product, ProductDto> {

    ProductRepo productRepo;

    @Autowired
    public ProductDtoMapper(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }


    @Override
    public ProductDto apply(Product product) {
        return new ProductDto(
                product.getProductId(),
                product.getProductName(),
                product.getDescription(),
                product.getBrand(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory().stream()
                        .map(
                                Category::getCategoryId
                        )
                        .collect(Collectors.toList()),
                product.getDiscount(),
                product.getPhotoIds()
        );
    }
}

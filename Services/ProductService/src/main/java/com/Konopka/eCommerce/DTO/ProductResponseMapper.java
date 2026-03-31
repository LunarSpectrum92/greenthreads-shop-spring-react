package com.Konopka.eCommerce.DTO;


import com.Konopka.eCommerce.models.Category;
import com.Konopka.eCommerce.models.PhotoFeign;
import com.Konopka.eCommerce.models.Product;
import com.Konopka.eCommerce.Repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductResponseMapper {

    ProductRepo productRepo;
    PhotoFeign photoFeign;


    @Autowired
    public ProductResponseMapper(ProductRepo productRepo, PhotoFeign photoFeign) {
        this.productRepo = productRepo;
        this.photoFeign = photoFeign;
    }

    public ProductResponse Map(Product product) {
        Set<String> paths = new HashSet<>();
        if (!product.getPhotoIds().isEmpty()) {
            paths = photoFeign.findPhotosByIds(product.getPhotoIds()).getBody();
        }

        return new ProductResponse(
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
                paths
        );
    }
}

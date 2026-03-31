package com.Konopka.eCommerce.DTO;

import java.util.List;

public record ProductDto(
        int productId,
        String productName,
        String description,
        String brand,
        Double price,
        Integer quantity,
        List<Integer> category,
        Integer discount,
        List<Integer> photoIds
) {
}

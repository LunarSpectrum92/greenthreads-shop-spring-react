package com.Konopka.eCommerce.services;

import com.Konopka.eCommerce.DTO.*;
import com.Konopka.eCommerce.Repository.CategoryRepo;
import com.Konopka.eCommerce.Repository.ProductRepo;
import com.Konopka.eCommerce.Services.ProductService;
import com.Konopka.eCommerce.models.PhotoFeign;
import com.Konopka.eCommerce.models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepo productRepo;
    @Mock private ProductDtoMapper productDtoMapper;
    @Mock private ProductResponseMapper productResponseMapper;
    @Mock private CategoryRepo categoryRepo;
    @Mock private PhotoFeign photoFeign;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setProductId(1);
        product.setQuantity(10);
        product.setProductName("Laptop");
        productDto = new ProductDto(1, "Laptop", "Desc", "Brand", 100.0, 10, List.of(1), 0, List.of());
    }


    @Test
    void getProductById_ProductExistsAndStockAvailable_ReturnsProductDto() {
        when(productRepo.findById(1)).thenReturn(Optional.of(product));
        when(productDtoMapper.apply(product)).thenReturn(productDto);

        Optional<ProductDto> result = productService.getProductById(1, 5);

        assertTrue(result.isPresent());
        assertEquals(5, product.getQuantity()); // 10 - 5
        verify(productRepo).save(product);
    }

    @Test
    void getProductById_ProductNotFound_ThrowsRuntimeException() {
        when(productRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getProductById(99, 1));
    }

    @Test
    void getProductById_InsufficientStock_ThrowsRuntimeException() {
        when(productRepo.findById(1)).thenReturn(Optional.of(product));

        assertThrows(RuntimeException.class, () -> productService.getProductById(1, 15));
    }


    @Test
    void createProduct_ProductAlreadyExists_ReturnsConflict() {
        when(productRepo.findById(productDto.productId())).thenReturn(Optional.of(product));

        ResponseEntity<Product> response = productService.createProduct(productDto, Collections.emptyList());

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void createProduct_NewProduct_ReturnsCreated() {
        when(productRepo.findById(productDto.productId())).thenReturn(Optional.empty());
        when(productRepo.save(any(Product.class))).thenReturn(product);

        ResponseEntity<Product> response = productService.createProduct(productDto, Collections.emptyList());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(productRepo).save(any(Product.class));
    }


    @Test
    void getProductsByCategory_CategoryDoesNotExist_ReturnsNotFound() {
        when(categoryRepo.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<List<ProductResponse>> response = productService.getProductsByCategory(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getProductsByCategory_NoProductsInCategory_ReturnsNotFound() {
        when(categoryRepo.findById(1)).thenReturn(Optional.of(new com.Konopka.eCommerce.models.Category()));
        when(productRepo.findAllByCategory(1)).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = productService.getProductsByCategory(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    void searchEngine_PromptMatchesProductName_ReturnsList() {
        product.setProductName("Gaming Mouse");
        when(productRepo.findAll()).thenReturn(List.of(product));
        when(productResponseMapper.Map(product)).thenReturn(mock(ProductResponse.class));

        ResponseEntity<List<ProductResponse>> response = productService.searchEngine("mouse");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
}
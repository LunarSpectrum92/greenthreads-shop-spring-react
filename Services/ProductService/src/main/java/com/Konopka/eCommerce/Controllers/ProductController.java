package com.Konopka.eCommerce.Controllers;


import com.Konopka.eCommerce.DTO.PhotoDto;
import com.Konopka.eCommerce.DTO.ProductDto;
import com.Konopka.eCommerce.DTO.ProductResponse;
import com.Konopka.eCommerce.models.PhotoFeign;
import com.Konopka.eCommerce.models.Product;
import com.Konopka.eCommerce.Services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    ProductService productService;
    PhotoFeign photoFeign;

    @Autowired
    public ProductController(ProductService productService, PhotoFeign photoFeign) {
        this.productService = productService;
        this.photoFeign = photoFeign;
    }

    //get all products
    @GetMapping("/products")
    public List<ProductResponse> GetProductsById(@RequestBody List<Integer> ids) {
        return productService.GetProductsById(ids);
    }

    //find product by id for order
    @GetMapping("/product")
    public Optional<ProductDto> findById(@RequestParam int id, @RequestParam int quantity) {
        return productService.getProductById(id, quantity);
    }


    @GetMapping("/prduct/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable int id) {
        return productService.findProductById(id);
    }

    //find product by name
    @GetMapping("/product/name")
    public Optional<ProductDto> findByName(@RequestParam String name) {
        return productService.GetProductsByName(name);
    }


    //create product
    @PostMapping(value = "/product", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Product> createProduct(
            @RequestPart("data") ProductDto productDto,
            @RequestPart("photoS") List<MultipartFile> photo
    ) {
        return productService.createProduct(productDto, photo);
    }

    //add product photo
    @PostMapping("/product/photos")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Set<Integer>> createPhotos(@RequestBody PhotoDto photoDto) {
        return productService.addPhotos(photoDto);
    }

    //get product photo
    @GetMapping("/product/photos")
    public ResponseEntity<Set<String>> getPhotos(@RequestParam List<Integer> ids) {
        return productService.getPhotos(ids);
    }

    //finding products by category
    @GetMapping("/product/category")
    public ResponseEntity<List<ProductResponse>> getProductsForCategoryAndSubcategories(@RequestParam int categoryId) {
        return productService.getProductsForCategoryAndSubcategories(categoryId);
    }

    // Get all products
    @GetMapping("/product/all")
    public List<ProductResponse> getAllProducts() {
        return productService.GetProducts();
    }

    //get brands
    @GetMapping("/products/brand")
    public Set<String> getProductsByBrand() {
        return productService.getBrands();
    }

    // Find products by brand
    @GetMapping("/product/brand")
    public ResponseEntity<List<ProductResponse>> findAllByBrand(@RequestParam String brand) {
        return productService.findAllByBrand(brand);
    }

    // Search products by prompt
    @GetMapping("/product/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String prompt) {
        return productService.searchEngine(prompt);
    }

    // Update product
    @PutMapping("/product/put")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<ProductDto> updateProduct(@RequestBody ProductDto productDto) {
        return productService.updateProduct(productDto);
    }

    // Delete product
    @DeleteMapping("/product/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Integer> deleteProduct(@PathVariable int id) {
        return productService.deleteProduct(id);
    }

    // Get products sorted by price ascending
    @GetMapping("/product/price/asc")
    public List<ProductResponse> getProductsByPriceAsc() {
        return productService.findAllByOrderByPriceAsc();
    }

    // Get products sorted by price descending
    @GetMapping("/product/price/desc")
    public List<ProductResponse> getProductsByPriceDesc() {
        return productService.findAllByOrderByPriceDesc();
    }

}

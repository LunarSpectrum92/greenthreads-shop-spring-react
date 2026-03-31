package com.Konopka.eCommerce.respository;

import com.Konopka.eCommerce.Repository.ProductRepo;
import com.Konopka.eCommerce.configurations.TestConfigurations;
import com.Konopka.eCommerce.models.Category;
import com.Konopka.eCommerce.models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfigurations.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ProductRepoTest {

    @Autowired
    private ProductRepo productRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category electronics;
    private Category laptops;
    private Category smartphones;

    @BeforeEach
    void setUp() {
        electronics = Category.builder()
                .categoryName("Electronics")
                .build();
        entityManager.persist(electronics);

        laptops = Category.builder()
                .categoryName("Laptops")
                .categoryParentId(electronics)
                .build();

        smartphones = Category.builder()
                .categoryName("Smartphones")
                .categoryParentId(electronics)
                .build();

        entityManager.persist(laptops);
        entityManager.persist(smartphones);

        Product macbook = Product.builder()
                .productName("MacBook Pro")
                .brand("Apple")
                .price(5999.99)
                .quantity(10)
                .discount(0)
                .category(List.of(laptops))
                .build();

        Product iphone = Product.builder()
                .productName("iPhone 15")
                .brand("Apple")
                .price(3999.99)
                .quantity(25)
                .discount(5)
                .category(List.of(smartphones))
                .build();

        Product universalCharger = Product.builder()
                .productName("Universal Charger")
                .brand("Anker")
                .price(149.99)
                .quantity(100)
                .discount(0)
                .category(List.of(laptops, smartphones))
                .build();

        Product hdmiCable = Product.builder()
                .productName("HDMI Cable")
                .brand("Ugreen")
                .price(49.99)
                .quantity(200)
                .discount(10)
                .category(List.of(electronics))
                .build();

        entityManager.persist(macbook);
        entityManager.persist(iphone);
        entityManager.persist(universalCharger);
        entityManager.persist(hdmiCable);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findAllByCategory_laptops_returnsOnlyDirectProducts() {
        List<Product> result = productRepository.findAllByCategory(laptops.getCategoryId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Product::getProductName)
                .containsExactlyInAnyOrder("MacBook Pro", "Universal Charger");
    }

    @Test
    void findAllByCategory_smartphones_returnsOnlyDirectProducts() {
        List<Product> result = productRepository.findAllByCategory(smartphones.getCategoryId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Product::getProductName)
                .containsExactlyInAnyOrder("iPhone 15", "Universal Charger");
    }

    @Test
    void findAllByCategory_rootElectronics_doesNotReturnProductsFromSubcategories() {
        List<Product> result = productRepository.findAllByCategory(electronics.getCategoryId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).isEqualTo("HDMI Cable");
    }

    @Test
    void findAllByCategory_emptyCategory_returnsEmptyList() {
        Category empty = Category.builder()
                .categoryName("Empty Category")
                .build();
        entityManager.persistAndFlush(empty);

        List<Product> result = productRepository.findAllByCategory(empty.getCategoryId());

        assertThat(result).isEmpty();
    }

    @Test
    void findAllProductsForCategoryAndSubcategories_rootElectronics_returnsAllProductsInTree() {
        List<Product> result = productRepository
                .findAllProductsForCategoryAndSubcategories(electronics.getCategoryId());

        assertThat(result).hasSize(4);
        assertThat(result)
                .extracting(Product::getProductName)
                .containsExactlyInAnyOrder("MacBook Pro", "iPhone 15", "Universal Charger", "HDMI Cable");
    }

    @Test
    void findAllProductsForCategoryAndSubcategories_leafCategory_returnsOnlyOwnProducts() {
        List<Product> result = productRepository
                .findAllProductsForCategoryAndSubcategories(laptops.getCategoryId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Product::getProductName)
                .containsExactlyInAnyOrder("MacBook Pro", "Universal Charger");
    }

    @Test
    void findAllProductsForCategoryAndSubcategories_productInMultipleCategories_isNotDuplicated() {
        List<Product> result = productRepository
                .findAllProductsForCategoryAndSubcategories(electronics.getCategoryId());

        long count = result.stream()
                .filter(p -> p.getProductName().equals("Universal Charger"))
                .count();

        assertThat(count).isEqualTo(1);
    }

    @Test
    void findAllProductsForCategoryAndSubcategories_emptyCategory_returnsEmptyList() {
        Category empty = Category.builder()
                .categoryName("Empty")
                .build();
        entityManager.persistAndFlush(empty);

        List<Product> result = productRepository
                .findAllProductsForCategoryAndSubcategories(empty.getCategoryId());

        assertThat(result).isEmpty();
    }
}
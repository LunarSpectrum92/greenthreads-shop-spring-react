package com.Konopka.eCommerce.respository;

import com.Konopka.eCommerce.Repository.CategoryRepo;
import com.Konopka.eCommerce.configurations.TestConfigurations;
import com.Konopka.eCommerce.models.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfigurations.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class CategoryRepoTest {


    @Autowired
    private CategoryRepo categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        Category electronics = Category.builder()
                .categoryName("Electronics")
                .build();

        entityManager.persist(electronics);

        Category laptops = Category.builder()
                .categoryName("Laptops")
                .categoryParentId(electronics)
                .build();

        Category smartphones = Category.builder()
                .categoryName("Smartphones")
                .categoryParentId(electronics)
                .build();

        entityManager.persist(laptops);
        entityManager.persist(smartphones);

        entityManager.flush();
        entityManager.clear();
    }


    @Test
    public void findAllRootCategories_findsCategoriesCorrectly_returnsListCategories() {
        List<Category> roots = categoryRepository.findAllRootCategories();

        assertThat(roots).hasSize(1);
        assertThat(roots.get(0).getCategoryName()).isEqualTo("Electronics");
    }

}

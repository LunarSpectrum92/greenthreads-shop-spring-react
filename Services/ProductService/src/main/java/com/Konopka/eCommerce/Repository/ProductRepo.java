package com.Konopka.eCommerce.Repository;


import com.Konopka.eCommerce.models.Category;
import com.Konopka.eCommerce.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Integer> {

    Optional<Product> findByproductName(String name);

    List<Product> findAllByOrderByPriceAsc();

    List<Product> findAllByOrderByPriceDesc();


    @NativeQuery("SELECT *\n" +
            "FROM product\n" +
            "NATURAL JOIN product_category\n" +
            "NATURAL JOIN category\n" +
            "WHERE category_id = :categoryId\n")
    List<Product> findAllByCategory(@Param("categoryId") Integer categoryId);

    List<Product> findAllByBrand(String brand);


    @NativeQuery("""            
             WITH RECURSIVE CategoryTree AS (
                 SELECT * FROM category WHERE category_id = :parentCategoryId
                 UNION ALL
                 SELECT c.* FROM category c
                 INNER JOIN CategoryTree ct ON c.category_parent_id = ct.category_id
             )
             SELECT DISTINCT p.* FROM product p
             JOIN product_category pc ON p.product_id = pc.product_id
             JOIN CategoryTree ct ON pc.category_id = ct.category_id;
            """
    )
    List<Product> findAllProductsForCategoryAndSubcategories(@Param("parentCategoryId") int parentCategoryId);


}

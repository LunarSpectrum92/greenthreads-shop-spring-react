package com.Konopka.eCommerce.Repository;

import com.Konopka.eCommerce.models.Category;
import com.Konopka.eCommerce.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Integer> {

    @Query("SELECT c FROM Category c WHERE c.categoryParentId IS NULL")
    List<Category> findAllRootCategories();


}

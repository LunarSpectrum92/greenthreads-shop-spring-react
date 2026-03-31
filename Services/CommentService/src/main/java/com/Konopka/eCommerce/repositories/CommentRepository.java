package com.Konopka.eCommerce.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Konopka.eCommerce.models.Comment;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    List<Comment> findAllByKeycloakId(String id);

    List<Comment> findAllByProductId(Integer id);

}

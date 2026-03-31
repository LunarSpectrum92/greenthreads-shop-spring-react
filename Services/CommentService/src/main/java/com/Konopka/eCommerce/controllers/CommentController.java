package com.Konopka.eCommerce.controllers;


import com.Konopka.eCommerce.DTO.CommentDTO;
import com.Konopka.eCommerce.DTO.CommentRequest;
import com.Konopka.eCommerce.models.Comment;
import com.Konopka.eCommerce.services.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {


    CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }


    //create comment
    @PostMapping("/comment")
    public ResponseEntity<CommentDTO> addComment(@RequestBody CommentRequest comment, Authentication auth) {
        return commentService.createComment(comment, auth);
    }

    //delete comment
    @DeleteMapping("/comment")
    public ResponseEntity<Integer> deleteComment(@RequestBody Integer commentId, Authentication authentication) {
        return commentService.deleteComment(commentId, authentication);
    }

    //get comment by id
    @GetMapping("/comment/{commentId}")
    public ResponseEntity<Comment> getComment(@PathVariable Integer commentId) {
        return commentService.getCommentById(commentId);
    }

    //get comment by user id
    @GetMapping("/comment/user/{userId}")
    public ResponseEntity<List<Comment>> getCommentByUserId(@PathVariable String KeykloakId, Authentication auth) {
        return commentService.getAllCommentsByUser(KeykloakId, auth);
    }

    //getCommentsByProductId
    @GetMapping("/comment/product/{productId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByProductId(@PathVariable Integer productId) {
        return commentService.getAllCommentsByProduct(productId);
    }


}

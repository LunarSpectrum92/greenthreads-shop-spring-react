package com.Konopka.eCommerce.services;

import com.Konopka.eCommerce.DTO.CommentDTO;
import com.Konopka.eCommerce.DTO.CommentDtoMapper;
import com.Konopka.eCommerce.DTO.CommentRequest;
import com.Konopka.eCommerce.models.Comment;
import com.Konopka.eCommerce.repositories.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentDtoMapper commentDtoMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CommentService commentService;

    private Comment sampleComment;
    private CommentRequest sampleRequest;
    private CommentDTO sampleDTO;
    private Timestamp fixedTimestamp;

    @BeforeEach
    void setUp() {
        sampleRequest = new CommentRequest("great product", 5, "user-123", 101);
        sampleComment = Comment.builder()
                .commentBody("great product")
                .score(5)
                .keycloakId("user-123")
                .productId(101)
                .build();
        sampleDTO = new CommentDTO(
                1,
                "great product",
                fixedTimestamp,
                5,
                "user-123",
                101
        );
    }


    @Test
    void createComment_ShouldReturnCreated() {
        when(authentication.getName()).thenReturn("user-123");
        when(commentDtoMapper.commentDTOMapper(any(Comment.class))).thenReturn(sampleDTO);

        ResponseEntity<CommentDTO> response = commentService.createComment(sampleRequest, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(sampleDTO, response.getBody());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void deleteComment_UserDeletesOwnComment_ShouldReturnOk() {
        when(commentRepository.findById(1)).thenReturn(Optional.of(sampleComment));
        when(authentication.getName()).thenReturn("user-123");
        doReturn(Collections.emptyList()).when(authentication).getAuthorities();

        ResponseEntity<Integer> response = commentService.deleteComment(1, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(commentRepository).delete(sampleComment);
    }

    @Test
    void deleteComment_AdminDeletesAnyComment_ShouldReturnOk() {
        when(commentRepository.findById(1)).thenReturn(Optional.of(sampleComment));
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_Admin"))).when(authentication).getAuthorities();

        ResponseEntity<Integer> response = commentService.deleteComment(1, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(commentRepository).delete(sampleComment);
    }

    @Test
    void deleteComment_WrongUser_ShouldReturnForbidden() {
        sampleComment.setKeycloakId("inny-user");
        when(commentRepository.findById(1)).thenReturn(Optional.of(sampleComment));
        when(authentication.getName()).thenReturn("moje-id");
        doReturn(Collections.emptyList()).when(authentication).getAuthorities();

        ResponseEntity<Integer> response = commentService.deleteComment(1, authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_NotFound_ShouldReturn404() {
        when(commentRepository.findById(999)).thenReturn(Optional.empty());

        ResponseEntity<Integer> response = commentService.deleteComment(999, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


    @Test
    void getAllCommentsByProduct_ShouldReturnList() {
        when(commentRepository.findAllByProductId(101)).thenReturn(List.of(sampleComment));
        when(commentDtoMapper.commentDTOMapper(sampleComment)).thenReturn(sampleDTO);

        ResponseEntity<List<CommentDTO>> response = commentService.getAllCommentsByProduct(101);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getAllCommentsByProduct_Empty_ShouldReturnBadRequest() {
        when(commentRepository.findAllByProductId(101)).thenReturn(Collections.emptyList());

        ResponseEntity<List<CommentDTO>> response = commentService.getAllCommentsByProduct(101);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
package com.Konopka.eCommerce.DTO;


import com.Konopka.eCommerce.models.Comment;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;


@Component
public class CommentDtoMapper {

    public CommentDTO commentDTOMapper(Comment comment) {
        return new CommentDTO(
                comment.getCommentId(),
                comment.getCommentBody(),
                comment.getCommentDate(),
                comment.getScore(),
                comment.getKeycloakId(),
                comment.getProductId()
        );
    }


}

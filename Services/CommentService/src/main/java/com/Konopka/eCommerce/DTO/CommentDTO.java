package com.Konopka.eCommerce.DTO;

import java.sql.Timestamp;

public record CommentDTO(
        Integer commentId,
        String commentBody,
        Timestamp commentDate,
        Integer score,
        String keycloakId,
        Integer productId
) {
}

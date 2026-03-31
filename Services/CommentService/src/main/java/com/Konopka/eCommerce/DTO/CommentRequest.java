package com.Konopka.eCommerce.DTO;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;

import java.sql.Timestamp;

public record CommentRequest (
    String commentBody,
    Integer score,
    String keycloakId,
    Integer productId

) {
}

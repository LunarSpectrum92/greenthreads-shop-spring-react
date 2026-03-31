package com.Konopka.eCommerce.DTO;

import java.time.LocalDateTime;

public record ClientDto(
        Integer userId,
        String phone,
        String keycloakId,
        LocalDateTime createdAt,
        AddressDTO address,
        Integer photoId
) {
}

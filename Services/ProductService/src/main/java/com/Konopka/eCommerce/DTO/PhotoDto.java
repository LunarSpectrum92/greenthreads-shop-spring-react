package com.Konopka.eCommerce.DTO;


import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public record PhotoDto(
        Integer productId,
        Set<MultipartFile> photos
) {
}

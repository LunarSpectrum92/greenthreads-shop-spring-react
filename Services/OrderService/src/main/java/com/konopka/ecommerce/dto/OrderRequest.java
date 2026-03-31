package com.Konopka.eCommerce.DTO;


import com.Konopka.eCommerce.models.PaymentMethod;

import java.util.List;

public record OrderRequest(
        String clientId,
        List<OrderProductDto> orderProductsList
) {
}

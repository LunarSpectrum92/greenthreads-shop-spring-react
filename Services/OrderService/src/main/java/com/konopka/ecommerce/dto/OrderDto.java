package com.Konopka.eCommerce.DTO;


import com.Konopka.eCommerce.models.PaymentMethod;
import com.Konopka.eCommerce.models.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.List;

public record OrderDto(
        Integer orderId,

        BigDecimal totalAmount,

        LocalDateTime orderDate,

        String clientId,

        List<OrderProductDto> orderProductsList,

        Status status
) {
}

package com.Konopka.eCommerce.DTO;


import com.Konopka.eCommerce.models.Order;
import com.Konopka.eCommerce.models.OrderProduct;

import java.util.List;
import java.util.stream.Collectors;

public class OrderDtoMapper {

    public static OrderDto toDto(Order order) {

        return new OrderDto(
                order.getOrderId(),
                order.getTotalAmount(),
                order.getOrderDate(),
                order.getClientId(),
                mapOrderProductsToDto(order.getOrderProductsList()),
                order.getStatus()
        );
    }

    public static Order toEntity(OrderDto orderDto) {
        Order order = new Order();
        order.setTotalAmount(orderDto.totalAmount());
        order.setOrderDate(orderDto.orderDate());
        order.setClientId(orderDto.clientId());
        order.setOrderProductsList(mapOrderProductsToEntity(orderDto.orderProductsList()));
        return order;
    }

    public static List<OrderProductDto> mapOrderProductsToDto(List<OrderProduct> orderProducts) {
        return orderProducts.stream()
                .map(orderProduct -> {
                    System.out.println(orderProduct);
                    return new OrderProductDto(
                            orderProduct.getProductId(),
                            orderProduct.getQuantity(),
                            orderProduct.getPrice()
                    );

                })
                .collect(Collectors.toList());
    }

    public static List<OrderProduct> mapOrderProductsToEntity(List<OrderProductDto> orderProductDtos) {
        return orderProductDtos.stream()
                .map(orderProductDto -> {
                    OrderProduct orderProduct = new OrderProduct();
                    orderProduct.setProductId(orderProductDto.productId());
                    orderProduct.setQuantity(orderProductDto.quantity());
                    orderProduct.setPrice(orderProductDto.price());
                    return orderProduct;
                })
                .collect(Collectors.toList());
    }


    public static OrderProduct mapOrderProductToEntity(OrderProductDto orderProductDtos) {
        return OrderProduct.builder()
                .productId(orderProductDtos.productId())
                .quantity(orderProductDtos.quantity())
                .price(orderProductDtos.price())
                .build();
    }




}

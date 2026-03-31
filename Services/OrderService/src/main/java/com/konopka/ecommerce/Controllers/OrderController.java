package com.Konopka.eCommerce.Controllers;


import com.Konopka.eCommerce.DTO.OrderDto;
import com.Konopka.eCommerce.DTO.OrderRequest;
import com.Konopka.eCommerce.models.ClientFeign;
import com.Konopka.eCommerce.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {


    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    //create order
    @PostMapping("/order")
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderRequest orderRequest, Authentication authentication) {
        return orderService.createOrder(orderRequest, authentication);
    }

    //getorder
    @GetMapping("/order/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Integer orderId, Authentication authentication) {
        return orderService.getOrder(orderId, authentication);
    }

    //getallorders
    @GetMapping("/orders")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return orderService.getAllOrders();
    }

    //getOrdersByClientId
    @GetMapping("/order/client/{clientId}")
    public ResponseEntity<List<OrderDto>> getOrdersByClientId(@PathVariable String clientId, Authentication authentication) {
        return orderService.getOrdersByClientId(clientId, authentication);
    }


}

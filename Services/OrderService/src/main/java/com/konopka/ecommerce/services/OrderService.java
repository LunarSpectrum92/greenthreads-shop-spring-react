package com.Konopka.eCommerce.services;


import com.Konopka.eCommerce.DTO.*;
import com.Konopka.eCommerce.kafka.OrderMessageProducer;
import com.Konopka.eCommerce.models.*;
import com.Konopka.eCommerce.repositories.OrderProductRepository;
import com.Konopka.eCommerce.repositories.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class OrderService {

    private OrderRepository orderRepository;
    private OrderProductRepository orderProductRepository;
    private OrderMessageProducer orderMessageProducer;
    private ClientFeign clientFeign;
    private ProductFeign productFeign;

    @Autowired
    public OrderService(OrderRepository orderRepository, ClientFeign clientFeign, ProductFeign productFeign, OrderProductRepository orderProductRepository, OrderMessageProducer orderMessageProducer) {
        this.orderRepository = orderRepository;
        this.clientFeign = clientFeign;
        this.productFeign = productFeign;
        this.orderProductRepository = orderProductRepository;
        this.orderMessageProducer = orderMessageProducer;
    }

    @Transactional
    public ResponseEntity<OrderDto> createOrder(OrderRequest orderRequest, Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Admin"));

        if (!isAdmin) {
            if (!orderRequest.clientId().equals(authentication.getName())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        if (orderRequest == null || orderRequest.orderProductsList() == null || orderRequest.orderProductsList().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }


        ResponseEntity<ClientDto> client = clientFeign.getClientByKeycloakId(orderRequest.clientId());
        if (!client.hasBody()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        BigDecimal amount = BigDecimal.ZERO;


        List<OrderProduct> productDtos = new ArrayList<>();
        for (OrderProductDto orderProductDto : orderRequest.orderProductsList()) {
            var product = productFeign.findById(orderProductDto.productId(), orderProductDto.quantity());
            if (product.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Error-Message", "Product not found").build();
            }

            amount = amount.add(orderProductDto.price());
            System.out.println(amount);

            productDtos.add(OrderDtoMapper.mapOrderProductToEntity(orderProductDto));

        }

        //create order
        Order order = Order.builder().totalAmount(amount).clientId(String.valueOf(client.getBody().userId())).orderProductsList(OrderDtoMapper.mapOrderProductsToEntity(orderRequest.orderProductsList()))
                //.paymentMethod(orderRequest.paymentMethod())
                .status(Status.WAITING_FOR_PAYMENT).build();


        try {
            Order savedOrder = orderRepository.save(order);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        for (OrderProduct op : productDtos) {
            op.setOrder(order);
            try {
                orderProductRepository.save(op);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }


        //send message to payment
        orderMessageProducer.produceOrderMessage(OrderDtoMapper.toDto(order));

        return new ResponseEntity<>(OrderDtoMapper.toDto(order), HttpStatus.CREATED);
    }


    //getorder
    public ResponseEntity<OrderDto> getOrder(Integer orderId, Authentication authentication) {
        Optional<Order> order = orderRepository.findById(orderId);

        if (order.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        boolean allowed = checkIfAllowed(Integer.valueOf(order.get().getClientId()), authentication);

        if (!allowed) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(OrderDtoMapper.toDto(order.get()));
    }

    //getallorders
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        System.out.println("Liczba zamówień: " + orders.size());

        orders.forEach(order -> log.info("Order ID: {}", order.getOrderProductsList()));

        List<OrderDto> orderDtos = orders.stream().sorted(Comparator.comparing(Order::getOrderDate)).map(OrderDtoMapper::toDto).toList();

        return new ResponseEntity<>(orderDtos, HttpStatus.OK);
    }

    //getOrdersByClientId
    public ResponseEntity<List<OrderDto>> getOrdersByClientId(String clientId, Authentication authentication) {
        List<Order> orders = orderRepository.findAllByClientId(clientId);
        if (orders.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        boolean allowed = checkIfAllowed(Integer.valueOf(clientId), authentication);

        if (!allowed) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<OrderDto> orderDtos = orders.stream().sorted(Comparator.comparing(Order::getOrderDate)).map(OrderDtoMapper::toDto).toList();
        return new ResponseEntity<>(orderDtos, HttpStatus.OK);
    }


    public boolean checkIfAllowed(Integer clientId, Authentication authentication) {
        ResponseEntity<ClientDto> client = clientFeign.getClient(Integer.valueOf(clientId));
        if (client.getStatusCode() != HttpStatus.OK || client.getBody() == null) {
            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_Admin"));

        boolean isOwner = client.getBody().keycloakId().equals(authentication.getName());

        if (!isAdmin && !isOwner) {
            return false;
        }

        return true;
    }

}

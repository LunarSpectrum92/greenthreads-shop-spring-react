package com.Konopka.eCommerce.kafka;


import com.Konopka.eCommerce.DTO.PaymentDto;
import com.Konopka.eCommerce.repositories.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderMessageConsumer {

    OrderRepository orderRepository;

    @Autowired
    public OrderMessageConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    @KafkaListener(topics = "PaymentToOrder", groupId = "payment-group")
    public void consumePayment(PaymentDto payment) {
        var order = orderRepository.findById(payment.orderId());
        if (order.isEmpty()) {
            log.error("Order not found for orderId: {}", payment.orderId());
            throw new RuntimeException("Order not found");
        }


        order.get().setStatus(payment.status());

        orderRepository.save(order.get());

        log.info("Order with id {} updated with status {}", payment.orderId(), payment.status());
    }


}

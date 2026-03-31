package com.Konopka.eCommerce.services;


import com.Konopka.eCommerce.DTO.PaymentDto;
import com.Konopka.eCommerce.DTO.PaymentMapper;
import com.Konopka.eCommerce.DTO.PaymentRequest;
import com.Konopka.eCommerce.kafka.PaymentProducer;
import com.Konopka.eCommerce.models.Payment;
import com.Konopka.eCommerce.models.Status;
import com.Konopka.eCommerce.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class PaymentService {

    PaymentRepository paymentRepository;
    PaymentProducer paymentProducer;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, PaymentProducer paymentProducer) {
        this.paymentProducer = paymentProducer;
        this.paymentRepository = paymentRepository;
    }


    public ResponseEntity<Integer> createPayment(PaymentDto paymentDto) {
        Payment payment = PaymentMapper.toPayment(paymentDto);
        paymentRepository.save(payment);
        return new ResponseEntity<>(payment.getPaymentId(), HttpStatus.OK);
    }


    public ResponseEntity<Status> payForOrder(PaymentRequest paymentRequest) {
        Optional<Payment> payment = paymentRepository.findByOrderId(Long.valueOf(paymentRequest.orderId()));
        if (payment.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        payment.get().setCustomerId(paymentRequest.customerId());
        payment.get().setPaymentMethod(paymentRequest.paymentMethod());

        if (payment.get().getAmount().compareTo(paymentRequest.amount()) != 0) {
            payment.get().setStatus(Status.REJECTED);
            paymentRepository.save(payment.get());
            paymentProducer.produceOrderMessage(PaymentMapper.toPaymentDto(payment.get()));
            return new ResponseEntity<>(Status.REJECTED, HttpStatus.BAD_REQUEST);
        }

        payment.get().setStatus(Status.SUCCEEDED);
        paymentRepository.save(payment.get());
        paymentProducer.produceOrderMessage(PaymentMapper.toPaymentDto(payment.get()));

        return new ResponseEntity<>(Status.SUCCEEDED, HttpStatus.OK);
    }


    public ResponseEntity<Integer> getPaymentById(Long id) {
        Optional<Payment> payment = paymentRepository.findById(id);
        return payment.map(value -> new ResponseEntity<>(value.getPaymentId(), HttpStatus.OK)).orElseGet(() -> ResponseEntity.notFound().build());
    }


}
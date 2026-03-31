package com.Konopka.eCommerce.controllers;


import com.Konopka.eCommerce.DTO.PaymentRequest;

import com.Konopka.eCommerce.models.Status;
import com.Konopka.eCommerce.services.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    PaymentService paymentService;


    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    @GetMapping("/payment/{id}")
    public ResponseEntity<Integer> getPayment(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }


    @PostMapping("/payment")
    public ResponseEntity<Status> payForPayment(@RequestBody PaymentRequest paymentRequest) {
        return paymentService.payForOrder(paymentRequest);
    }


}

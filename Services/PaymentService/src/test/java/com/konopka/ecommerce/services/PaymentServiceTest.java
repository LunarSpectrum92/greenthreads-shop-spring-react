package com.konopka.ecommerce.services;


import com.Konopka.eCommerce.DTO.PaymentDto;
import com.Konopka.eCommerce.DTO.PaymentRequest;
import com.Konopka.eCommerce.kafka.PaymentMethods;
import com.Konopka.eCommerce.kafka.PaymentProducer;
import com.Konopka.eCommerce.models.Payment;
import com.Konopka.eCommerce.models.PaymentMethod;
import com.Konopka.eCommerce.models.Status;
import com.Konopka.eCommerce.repositories.PaymentRepository;
import com.Konopka.eCommerce.services.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;
    @Mock
    PaymentProducer paymentProducer;

    @InjectMocks
    PaymentService paymentService;

    @Test
    public void createPayment_paymentCreatedSuccessfully_returnResponseEntity() {
        PaymentDto paymentDto = new PaymentDto(
                1,
                1,
                new BigDecimal("9.5"),
                PaymentMethod.BLIK,
                Status.WAITING_FOR_PAYMENT,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(paymentRepository.save(any(Payment.class))).thenReturn(Payment.builder().paymentId(3).build());

        ResponseEntity<Integer> responseEntity = paymentService.createPayment(paymentDto);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(null);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void payForOrder_orderNotFound_returnsNotFound() {
        PaymentRequest request = new PaymentRequest(1, 100, BigDecimal.TEN, PaymentMethod.BLIK);
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        ResponseEntity<Status> response = paymentService.payForOrder(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void payForOrder_wrongAmount_returnsRejectedAndCallsKafka() {
        PaymentRequest request = new PaymentRequest(1, 100, new BigDecimal("50.00"), PaymentMethod.BLIK);
        Payment existingPayment = new Payment();
        existingPayment.setAmount(new BigDecimal("100.00"));

        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(existingPayment));

        ResponseEntity<Status> response = paymentService.payForOrder(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Status.REJECTED, response.getBody());
        verify(paymentRepository).save(existingPayment);
        verify(paymentProducer).produceOrderMessage(any());
        assertEquals(Status.REJECTED, existingPayment.getStatus());
    }

    @Test
    void payForOrder_correctData_returnsSucceededAndCallsKafka() {
        BigDecimal amount = new BigDecimal("100.00");
        PaymentRequest request = new PaymentRequest(1, 100, amount, PaymentMethod.BLIK);
        Payment existingPayment = new Payment();
        existingPayment.setAmount(amount);

        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(existingPayment));

        ResponseEntity<Status> response = paymentService.payForOrder(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Status.SUCCEEDED, response.getBody());
        verify(paymentRepository).save(existingPayment);
        verify(paymentProducer).produceOrderMessage(any());
        assertEquals(Status.SUCCEEDED, existingPayment.getStatus());
    }


    @Test
    void getPaymentById_paymentExists_returnsIdAndOk() {
        Long id = 10L;
        Payment payment = new Payment();
        payment.setPaymentId(123);
        when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));

        ResponseEntity<Integer> response = paymentService.getPaymentById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(123, response.getBody());
    }

    @Test
    void getPaymentById_paymentDoesNotExist_returnsNotFound() {
        Long id = 10L;
        when(paymentRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<Integer> response = paymentService.getPaymentById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


}

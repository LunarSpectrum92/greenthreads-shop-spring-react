package com.Konopka.eCommerce.kafka;


import com.Konopka.eCommerce.DTO.PaymentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentDto> kafkaTemplate;


    public PaymentProducer(KafkaTemplate<String, PaymentDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public PaymentDto producePaymentMessage(PaymentDto paymentDto) {
        sendMessage("PaymentNotification", paymentDto);
        return paymentDto;
    }

    public PaymentDto produceOrderMessage(PaymentDto paymentDto) {
        sendMessage("PaymentToOrder", paymentDto);
        return paymentDto;
    }

    private void sendMessage(String topic, PaymentDto message) {
        try {
            log.info("Sending message to Kafka topic {}: {}", topic, message);
            kafkaTemplate.send(topic, message).get();
            log.info("Message successfully sent to Kafka topic {}", topic);
        } catch (Exception e) {
            log.error("Failed to send message to Kafka topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Error while sending message to Kafka", e);
        }
    }


}

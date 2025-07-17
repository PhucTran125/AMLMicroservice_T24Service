package com.vpbankhackathon.t24_service.pubsub.producers;

import com.vpbankhackathon.t24_service.models.entities.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TransactionProducer {
    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.producer.topic.transaction}")
    private String topicName;

    public void sendMessage(Transaction request) {
        try {
            System.out.println("Attempting to send message to topic: " + topicName);

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(topicName, request);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    System.out.println("Sent message for transaction=[" + request.getId() +
                            "] with offset=[" + result.getRecordMetadata().offset() + "]");
                } else {
                    System.err.println("Unable to send message for transaction=[" +
                            request.getId() + "] due to : " + ex.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("Error in sendMessage: " + e.getMessage());
            throw new RuntimeException("Failed to send message to Kafka", e);
        }
    }
}

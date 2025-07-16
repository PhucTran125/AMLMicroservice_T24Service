package com.vpbankhackathon.t24_service.pubsub.consumers;

import com.vpbankhackathon.t24_service.models.dtos.T24AMLResult;
import com.vpbankhackathon.t24_service.services.DataSourceAndIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class T24AMLResultConsumer {
    @Autowired
    DataSourceAndIntegrationService service;

    @KafkaListener(topics = "t24-aml-result", groupId = "t24-aml-result-group")
    public void consumeMessage(T24AMLResult result) {
        try {
            // Process the AML result message
            service.processAMLResult(result);
            // Here you would typically parse the message and perform necessary actions
        } catch (Exception e) {
            System.err.println("Error processing T24 AML result: " + e.getMessage());
            throw new RuntimeException("Failed to process T24 AML result", e);
        }
    }
}

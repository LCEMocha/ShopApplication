package com.shop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void produceMessage(String topic, String payload) {
        //log.info("Producer TOPIC : " + topic);
        //log.info("Producer PAYLOAD : " + payload);
        kafkaTemplate.send(topic, payload);
    }
}

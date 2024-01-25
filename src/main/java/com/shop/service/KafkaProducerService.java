package com.shop.service;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class KafkaProducerService implements Runnable {
    private final KafkaProducer<String, String> producer;
    private final String topic;

    public KafkaProducerService(Properties props, String topic) {
        this.producer = new KafkaProducer<>(props);
        this.topic = topic;
    }

    @Override
    public void run() {
        // 채팅 메시지 전송
        // 리스트나 큐 사용
    }

    // 채팅 메시지 추가
    public void sendMessage(String message) {
        producer.send(new ProducerRecord<>(topic, message));
    }

    public void close() {
        producer.close();
    }
}
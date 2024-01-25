package com.shop.service;

import java.util.Properties;

public class KafkaChatTest {
    public static void main(String[] args) {
        String topic = "testTopic";

        // Kafka 설정
        Properties producerProps = new Properties(); // Producer 설정
        producerProps.put("bootstrap.servers", "localhost:9092");
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer"); // 메시지 키의 직렬화 방법
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer"); // 메시지 값의 직렬화 방법
        producerProps.put("acks", "all"); // 메시지 전송에 대한 확인 설정
        producerProps.put("retries", 0); // 메시지 전송 실패 시 재시도 횟수
        producerProps.put("linger.ms", 1); // 메시지를 보내기 전 대기하는 시간

        Properties consumerProps = new Properties(); // Consumer 설정
        consumerProps.put("bootstrap.servers", "localhost:9092");
        consumerProps.put("group.id", "test-consumer-group"); // Consumer 그룹 ID
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"); // 메시지 키의 역직렬화 방법
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"); // 메시지 값의 역직렬화 방법
        consumerProps.put("auto.offset.reset", "earliest"); // 오프셋 리셋 정책
        consumerProps.put("enable.auto.commit", true); // 오프셋 자동 커밋 여부

        // Producer와 Consumer 스레드 실행
        KafkaProducerService producerRunnable = new KafkaProducerService(producerProps, topic);
        Thread producerThread = new Thread(producerRunnable);
        producerThread.start();

        KafkaConsumerService consumerRunnable = new KafkaConsumerService(consumerProps, topic);
        Thread consumerThread = new Thread(consumerRunnable);
        consumerThread.start();

        producerRunnable.sendMessage("Hello world!");

        producerRunnable.close();
        consumerRunnable.close();
        producerThread.interrupt();
        consumerThread.interrupt();
    }
}

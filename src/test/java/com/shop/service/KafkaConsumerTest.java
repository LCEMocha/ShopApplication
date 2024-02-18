package com.shop.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class KafkaConsumerTest {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerTest.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicReference<String> receivedMessage = new AtomicReference<>();

    @Test
    public void testReceiveMessage() throws Exception {
        String testMessage = "T메시지";
        String topicName = "kafkaTest";

        // 메시지 발신 시 내용 출력
        kafkaTemplate.send(topicName, testMessage);
        // logger.info("Send message: {}", testMessage);
        System.out.println("Send message: " + testMessage);

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Message not received");

        assertEquals(testMessage, receivedMessage.get());
    }

    @KafkaListener(topics = "kafkaTest", groupId = "testGroup")
    public void listen(ConsumerRecord<String, String> record) {

        receivedMessage.set(record.value());
        // 수신 메시지 내용 출력
        // logger.info("Received message: {}", record.value());
        System.out.println("Received message: " + record.value());
        latch.countDown(); // 메시지 수신 시 래치를 감소시켜 테스트 메소드가 진행되도록 함
    }
}
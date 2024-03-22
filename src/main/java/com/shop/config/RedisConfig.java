package com.shop.config;

import com.shop.service.CouponWorker;
import io.lettuce.core.api.StatefulRedisConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import io.lettuce.core.RedisClient;


@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    /**
     * 내장 혹은 외부의 Redis를 연결
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * RedisConnection에서 넘겨준 byte 값 객체 직렬화
     */
    @Bean
    public RedisTemplate<?,?> redisTemplate(){
        RedisTemplate<byte[], byte[]> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, String> statefulRedisConnection() {
        RedisClient redisClient = RedisClient.create("redis://" + host + ":" + port);
        return redisClient.connect();
    }

    @Bean
        // Redis 커넥션 팩토리를 사용하여 메시지 리스너 등록, CouponIssuance 채널 구독하도록 설정
    RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory, CouponMessageListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listener, new ChannelTopic("CouponIssuance"));
        return container;
    }

    @Component
    // CouponIssuance 채널에서 메시지 받아 처리
    public class CouponMessageListener implements MessageListener {

        private final CouponWorker couponWorker;

        @Autowired
        public CouponMessageListener(CouponWorker couponWorker) {
            this.couponWorker = couponWorker;
        }

        @Override
        public void onMessage(Message message, byte[] pattern) {
            handleCouponRequest(new String(message.getBody()));
        }

        private void handleCouponRequest(String serialNumber) {
            System.out.println("handleCouponRequest 실행");
            String couponName = "할인쿠폰T";
            couponWorker.checkAndIssueCoupon(serialNumber, couponName);
        }
    }
}
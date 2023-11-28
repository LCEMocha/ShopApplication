package com.shop.config;

import com.shop.service.CouponWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;


@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Bean
    RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory, CouponMessageListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listener, new ChannelTopic("CouponIssuance"));
        return container;
    }

    @Component
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
            couponWorker.checkAndIssueCoupon(serialNumber);
        }
    }
}
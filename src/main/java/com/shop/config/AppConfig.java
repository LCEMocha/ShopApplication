package com.shop.config;

import com.shop.service.CouponIssuedResultHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public CouponIssuedResultHolder couponIssuedResultHolder() {
        return new CouponIssuedResultHolder();
    }
}

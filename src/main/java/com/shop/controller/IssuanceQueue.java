package com.shop.controller;

import com.shop.config.SecurityUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.security.Principal;
import java.time.Instant;

@RestController
public class IssuanceQueue {

    private RedisCommands<String, String> redisCommands;
    private final Instant startTime;
    private final CouponController couponController;

    public IssuanceQueue(StatefulRedisConnection<String, String> connection, CouponController couponController) {
        this.redisCommands = connection.sync();
        this.couponController = couponController;
        this.startTime = Instant.now();
    }

    @PostMapping("/request")
    public void addRequest(Principal principal) {
        // 점수 부여 기준
        long score = Instant.now().getEpochSecond() - startTime.getEpochSecond();
        String email = SecurityUtils.getEmailFromPrincipal(principal);
        redisCommands.zadd("eventQueue", score, email);
    }

    @GetMapping("/rank/{email}")
    public Long getCurrentRank(@PathVariable String email) {
        return redisCommands.zrank("eventQueue", email);
    }

    @Scheduled(fixedDelay = 5000)
    public void processQueue() {
        long size = 200;
        var userIds = redisCommands.zrange("eventQueue", 0, size - 1);
        userIds.forEach(couponController::requestCoupon);
        redisCommands.zremrangebyrank("eventQueue", 0, size - 1);
    }
}

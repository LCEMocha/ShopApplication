package com.shop.controller;

import com.shop.config.DistributedLock;
import com.shop.config.SecurityUtils;
import com.shop.service.CouponWorker;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.Principal;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/coupon")
public class CouponController {

    private final Object lock = new Object();

    private static final Logger log = LoggerFactory.getLogger(CouponController.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CouponWorker couponWorker;

    @Autowired
    private ApplicationContext applicationContext;

    public ResponseEntity<Object> requestCoupon(String email) {
        try {
            CouponWorker couponWorker = applicationContext.getBean(CouponWorker.class);

            //String email = SecurityUtils.getEmailFromPrincipal(principal);

            // Redis Set에서 일련번호를 랜덤하게 선택
            String serialNumber = selectRandomSerialNumber();

            // 사용 가능한 일련번호가 없을 경우 새 일련번호 생성
            if (serialNumber == null) {
                serialNumber = couponWorker.generateSerialNumber();
                redisTemplate.opsForSet().add("available_serial_numbers", serialNumber); // 새 일련번호를 Redis Set에 추가
                log.info("serial number 생성: {}", serialNumber);
            } else {
                log.info("serial number 선택: {}", serialNumber);
            }

            redisTemplate.convertAndSend("CouponIssuance", serialNumber);
            // 고객 쿠폰 발급 데이터 RPush
            redisTemplate.opsForList().rightPush("CouponStore:" + serialNumber, email);
            log.info("쿠폰 요청 및 일련번호 저장됨: {}", serialNumber);
            return ResponseEntity.ok("쿠폰 발급 요청 성공");
        } catch (Exception e) {
            log.error("쿠폰발급 요청 중 에러 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("쿠폰 발급 요청 실패");
        }

    }

    private String selectRandomSerialNumber() {
        // Redis Set에서 일련번호를 랜덤하게 하나 선택
        return redisTemplate.opsForSet().randomMember("available_serial_numbers");
    }
}

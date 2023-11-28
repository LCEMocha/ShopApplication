package com.shop.controller;

import com.shop.config.SecurityUtils;
import com.shop.service.CouponWorker;
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


@RestController
@RequestMapping("/coupon")
public class CouponController {

    private static final Logger log = LoggerFactory.getLogger(CouponController.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CouponWorker couponWorker;

    @PostMapping("/request")
    public ResponseEntity<Object> requestCoupon(Principal principal) {
        try {
            String email = SecurityUtils.getEmailFromPrincipal(principal);

            // Redis Set에서 일련번호를 랜덤하게 선택
            String serialNumber = selectRandomSerialNumber();

            // 사용 가능한 일련번호가 없을 경우 새 일련번호 생성
            if (serialNumber == null) {
                serialNumber = couponWorker.generateSerialNumber();
                redisTemplate.opsForSet().add("available_serial_numbers", serialNumber); // 새 일련번호를 Redis Set에 추가
                log.info("New serial number generated: {}", serialNumber);
            } else {
                log.info("Serial number selected: {}", serialNumber);
            }

            redisTemplate.convertAndSend("CouponIssuance", serialNumber);
            // 고객 쿠폰 발급 데이터 RPush
            redisTemplate.opsForList().rightPush("CouponStore:" + serialNumber, email);
            log.info("Coupon request published to channel and saved to list with serial number: {}", serialNumber);
            return ResponseEntity.ok("쿠폰 발급 요청 성공");
        } catch (Exception e) {
            log.error("Error during coupon request: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("쿠폰 발급 요청 실패");
        }
    }

    private String selectRandomSerialNumber() {
        // Redis Set에서 일련번호를 랜덤하게 하나 선택
        return redisTemplate.opsForSet().randomMember("available_serial_numbers");
    }
}

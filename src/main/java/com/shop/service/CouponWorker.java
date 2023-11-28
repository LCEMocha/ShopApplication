package com.shop.service;

import com.shop.entity.Member;
import com.shop.repository.CouponRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.util.Random;
import com.shop.entity.Coupon;
import com.shop.repository.MemberRepository;
import java.util.Calendar;
import java.util.Set;


@Service
public class CouponWorker {

    private static final Logger log = LoggerFactory.getLogger(CouponWorker.class);
    private final String serialNumber;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    public CouponWorker(RedisTemplate<String, String> redisTemplate) {
        this.serialNumber = generateSerialNumber();
        this.redisTemplate = redisTemplate;

        // 자신의 일련번호를 redis에 추가
        this.redisTemplate.opsForSet().add("available_serial_numbers", this.serialNumber);
        System.out.println("일련번호 redis에 추가 완료");
    }

    public String generateSerialNumber() {
        // 100000 ~ 999999 범위의 랜덤 숫자 생성
        int number = 100000 + new Random().nextInt(900000);
        return Integer.toString(number);
    }

    @Scheduled(fixedDelay = 4000) // 4초 주기
    public void scheduledCheckAndIssueCoupons() {
        Set<String> serialNumbers = redisTemplate.opsForSet().members("available_serial_numbers");
        if (serialNumbers != null) {
            for (String serialNumber : serialNumbers) {
                if (redisTemplate.opsForList().size("CouponStore:" + serialNumber) > 0) {
                    checkAndIssueCoupon(serialNumber);
                }
            }
        }
    }

    @Transactional
    public void checkAndIssueCoupon(String serialNumber) {
        log.info("checkAndIssueCoupon 메서드 호출됨.");
        try {
            System.out.println("개수 :" + redisTemplate.opsForList().size("CouponStore:" + serialNumber));
            if (redisTemplate.opsForList().size("CouponStore:" + serialNumber) > 0) {
                String couponData = redisTemplate.opsForList().leftPop("CouponStore:" + serialNumber);

                // 회원 정보 조회
                Member member = memberRepository.findByEmail(couponData);
                System.out.println("email 서치 중: " + couponData);
                if (member == null) {
                    log.error("No member found with email: {}", couponData);
                    return;
                }

                // 쿠폰 생성 및 저장 로직
                Coupon coupon = new Coupon();
                coupon.setMember(member);
                coupon.setCouponCode(generateCouponCode());
                coupon.setIssuedDate(new Date());
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, 14);
                coupon.setExpirationDate(calendar.getTime());
                coupon.setCouponStatus("unUsed");  // 하드코딩 하는 부분
                coupon.setCouponValue(2000.0);     // 하드코딩 하는 부분
                coupon.setCouponName("Event");     // 하드코딩 하는 부분

                //쿠폰을 DB에 저장하는 로직 추가
                couponRepository.save(coupon);

                // 일련번호를 Redis Set에서 삭제
                redisTemplate.opsForSet().remove("available_serial_numbers", CouponWorker.this.serialNumber);
            }
        } catch (Exception e) {
            log.error("checkAndIssueCoupon 메서드 실행 중 에러 발생", e);
        }
    }

    private String generateCouponCode() {
        StringBuilder code = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) { // 문자
                code.append((char) (rand.nextInt(26) + 'A'));
            } else { // 숫자
                code.append(rand.nextInt(10));
            }
        }
        return code.toString();
    }
}


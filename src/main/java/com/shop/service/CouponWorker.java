package com.shop.service;

import com.shop.config.DistributedLock;
import com.shop.entity.CouponAvailable;
import com.shop.entity.Member;
import com.shop.repository.CouponAvailableRepository;
import com.shop.repository.CouponRepository;
import jakarta.transaction.Transactional;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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
import java.util.concurrent.TimeUnit;
import org.springframework.context.ApplicationEventPublisher;


@Service
@Scope("prototype")
public class CouponWorker {

    private static final Logger log = LoggerFactory.getLogger(CouponWorker.class);
    private final String serialNumber;
    private final ApplicationEventPublisher eventPublisher;
    private RedissonClient redissonClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final CouponAvailableRepository couponAvailableRepository;
    private final CouponDecreaseService couponDecreaseService;

    @Autowired
    public CouponWorker(RedissonClient redissonClient, RedisTemplate<String, String> redisTemplate,
                        MemberRepository memberRepository, CouponRepository couponRepository,
                        CouponAvailableRepository couponAvailableRepository, CouponDecreaseService couponDecreaseService,
                        ApplicationEventPublisher eventPublisher) {

        this.serialNumber = generateSerialNumber();
        this.redissonClient = redissonClient;
        this.redisTemplate = redisTemplate;
        this.memberRepository = memberRepository;
        this.couponRepository = couponRepository;
        this.couponAvailableRepository = couponAvailableRepository;
        this.couponDecreaseService = couponDecreaseService;
        this.eventPublisher = eventPublisher;

        // 자신의 일련번호를 redis에 추가
        this.redisTemplate.opsForSet().add("available_serial_numbers", this.serialNumber);
        System.out.println("일련번호 redis에 추가 완료");
    }

    public String generateSerialNumber() {
        // 1000000 ~ 9999999 범위의 랜덤 숫자 생성
        int number = 1000000 + new Random().nextInt(9000000);
        return Integer.toString(number);
    }

    @Scheduled(fixedDelay = 4000) // 4초 주기
    public void scheduledCheckAndIssueCoupons() {
        Set<String> serialNumbers = redisTemplate.opsForSet().members("available_serial_numbers");
        if (serialNumbers != null) {
            for (String serialNumber : serialNumbers) {
                if (redisTemplate.opsForList().size("CouponStore:" + serialNumber) > 0) {
                    String couponName = "할인쿠폰T";
                    checkAndIssueCoupon(serialNumber, couponName);
                }
            }
        }
    }

    public void checkAndIssueCoupon(String serialNumber, String couponName) {
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

                // couponAvailable에 발급요청된 쿠폰정보가 있는지 찾기
                CouponAvailable couponAvailable = couponAvailableRepository.findByName(couponName)
                        .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

                // 쿠폰 재고 감소 시도
                boolean decreaseSuccessful = couponDecreaseService.couponDecrease(couponAvailable);
                if (decreaseSuccessful) {
                    // 쿠폰 발급
                    Coupon coupon = createAndSaveCoupon(member, couponName);

                    // 쿠폰 발급 이벤트 발행 (쿠폰이 정상적으로 생성된 경우에만)
                    if (coupon != null) {
                        redisTemplate.opsForSet().remove("available_serial_numbers", this.serialNumber);
                        CouponIssuedEvent event = new CouponIssuedEvent(this, coupon);
                        eventPublisher.publishEvent(event);
                    }
                }
            }
        } catch (Exception e) {
            log.error("checkAndIssueCoupon 메서드 실행 중 에러 발생", e);
        }
    }

    @Transactional
    @DistributedLock(key = "'CouponIssueLock-' + #serialNumber", timeUnit = TimeUnit.SECONDS, waitTime = 5L, leaseTime = 3L)
    public Coupon createAndSaveCoupon(Member member, String couponName) {
        CouponAvailable couponAvailable = couponAvailableRepository.findByName(couponName)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 쿠폰을 찾을 수 없습니다."));

        Coupon coupon = new Coupon();
        coupon.setMember(member);
        coupon.setCouponCode(generateCouponCode());
        coupon.setIssuedDate(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 14);
        coupon.setExpirationDate(calendar.getTime());
        coupon.setCouponStatus("unUsed");
        coupon.setCouponAvailable(couponAvailable);
        coupon.setCouponName(couponAvailable.getName());
        coupon.setDiscountAmount(couponAvailable.getDiscountAmount());
        coupon.setDiscountPercentage(couponAvailable.getDiscountPercentage());
        coupon.setDiscountType(couponAvailable.getDiscountType());

        return couponRepository.save(coupon);

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
package com.shop.service;

import com.shop.controller.CouponController;
import com.shop.entity.CouponAvailable;
import com.shop.entity.Member;
import com.shop.repository.CouponAvailableRepository;
import com.shop.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;
import java.security.Principal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
public class CouponWorkerTest {

    @Autowired
    private CouponController couponController;

    @Autowired
    CouponAvailableRepository couponAvailableRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CouponIssuedEventListener couponIssuedEventListener;

    CouponAvailable couponAvailable;
    private CountDownLatch doneLatch;

    private static final Logger logger = LoggerFactory.getLogger(CouponWorkerTest.class);

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        couponAvailable = couponAvailableRepository.findByName("할인쿠폰T")
                .orElseThrow(() -> new IllegalArgumentException("해당 쿠폰을 찾을 수 없습니다."));

        /**
        couponAvailable = new CouponAvailable("할인쿠폰T", 100L, null, 20D, "percentage");
        couponAvailableRepository.save(couponAvailable);


        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        // 회원100명만들기
        for (int i = 0; i < 100; i++) {
            MemberFormDto memberFormDto = new MemberFormDto();
            String uniqueEmail = "test" + i + "@example.com";
            memberFormDto.setName("TestName" + i);
            memberFormDto.setEmail(uniqueEmail);
            memberFormDto.setAddress("TestAddress" + i);
            memberFormDto.setPassword("TestPassword" + i);

            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberRepository.save(member);
        }
         **/

    }


    /**
     * Feature: 쿠폰 차감 동시성 테스트
     * Background
     *     Given 할인쿠폰T 라는 이름의 쿠폰 100장이 등록되어 있음
     * <p>
     * Scenario: 100장의 쿠폰을 100명의 사용자가 동시에 접근해 발급 요청함
     *           Lock의 이름은 쿠폰명으로 설정함
     * <p>
     * Then 사용자들의 요청만큼 정확히 쿠폰의 개수가 차감되어야 함
     */

    @Test
    @Rollback(true)
    void 쿠폰차감_분산락_적용_동시성100명_테스트() throws InterruptedException {
        int numberOfThreads = 100;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);
        CouponIssuedResultHolder status = new CouponIssuedResultHolder();

        List<Member> testMembers = memberRepository.findAll()
                .stream()
                .filter(member -> member.getEmail().startsWith("test"))
                .toList();

        assertThat(testMembers).hasSize(numberOfThreads);

        for (Member member : testMembers) {
            service.execute(() -> {
                Principal mockPrincipal = () -> member.getEmail();
                readyLatch.countDown();
                try {
                    startLatch.await();
                    couponController.requestCoupon(mockPrincipal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();

        Thread.sleep(15000);

        CouponAvailable persistCoupon = couponAvailableRepository.findById(couponAvailable.getId())
                .orElseThrow(IllegalArgumentException::new);

        entityManager.refresh(persistCoupon);
        assertThat(persistCoupon.getAvailableStock()).isZero();
        System.out.println("잔여 쿠폰 개수 = " + persistCoupon.getAvailableStock());
    }

    @AfterEach
    void tearDown() {
        // 테스트 종료 후 Redis 데이터베이스의 모든 키 삭제
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }
}

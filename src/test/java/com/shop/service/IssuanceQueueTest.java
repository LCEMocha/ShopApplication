package com.shop.service;

import com.shop.controller.CouponController;
import com.shop.controller.IssuanceQueue;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
//@Transactional
public class IssuanceQueueTest {

    //@Autowired
    //private CouponController couponController;

    @Autowired
    private IssuanceQueue issuanceQueue;

    @Autowired
    CouponAvailableRepository couponAvailableRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    CouponAvailable couponAvailable;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        couponAvailable = couponAvailableRepository.findByName("할인쿠폰T")
                .orElseThrow(() -> new IllegalArgumentException("해당 쿠폰을 찾을 수 없습니다."));

    }

    @Test
    //@Rollback(true)
    void 부하분산_테스트() throws InterruptedException {
        int numberOfThreads = 1000;
        int batchSize = 100;
        ExecutorService service = Executors.newFixedThreadPool(batchSize);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads / batchSize);

        List<Member> testMembers = memberRepository.findAll()
                .stream()
                .filter(member -> member.getEmail().startsWith("test"))
                .toList();

        assertThat(testMembers).hasSize(100);

        List<Member> duplicatedTestMembers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            duplicatedTestMembers.addAll(testMembers);
        }

        assertThat(duplicatedTestMembers).hasSize(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i += batchSize) {
            CountDownLatch startLatch = new CountDownLatch(1);
            for (int j = 0; j < batchSize; j++) {
                Member member = duplicatedTestMembers.get(i + j);
                service.execute(() -> {
                    Principal mockPrincipal = () -> member.getEmail();
                    try {
                        startLatch.await();
                        issuanceQueue.addRequest(mockPrincipal);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            // 이 그룹의 스레드가 모두 시작할 준비가 되면, 동시에 시작
            startLatch.countDown();

            // 다음 배치를 처리하기 전에 잠시 대기 (IssuanceQueue의 처리 간격과 일치)
            Thread.sleep(3000);

            // 이 배치의 처리 완료
            endLatch.countDown();
        }

        endLatch.await();
        service.shutdown();

        CouponAvailable persistCoupon = couponAvailableRepository.findById(couponAvailable.getId())
                .orElseThrow(IllegalArgumentException::new);

        entityManager.clear();
        System.out.println(persistCoupon.getAvailableStock());
        assertThat(persistCoupon.getAvailableStock()).isZero();
        System.out.println("잔여 쿠폰 개수 = " + persistCoupon.getAvailableStock());
    }

    @AfterEach
    void tearDown() {
        // 테스트 종료 후 Redis 데이터베이스의 모든 키 삭제
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }
}

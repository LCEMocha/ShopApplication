package com.shop.config;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

// AOP(Aspect-Oriented Programming) 구현. 분산락을 획득하는 로직

/*
@Aspect는 횡단 관심사(cross-cutting concerns)를 모듈화. 애플리케이션 전반에 걸친 공통 기능을 중앙에서 관리할 수 있음
joinPoint에 어떤 advice가 적용될지, 그 advice가 어떤 순서로 적용될지를 정의
 ex) 각 메서드의 시작과 종료 시점에 로그를 남기는 기능을 추가하고자 할 때, 코드를 메서드마다 추가하는 것은 비효율적.
     이때, @Aspect를 사용하여 로깅 기능을 구현한 Aspect 클래스를 만들 수 있다
 */
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAop {

    private static final String REDISSON_LOCK_PREFIX = "LOCK:";   // Redisson 락 키의 접두사

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;
    final Logger log = (Logger) LoggerFactory.getLogger(DistributedLockAop.class);

    /*
    @Around를 사용하여 ProceedingJoinPoint.proceed() 메서드 호출로 대상 메서드의 실행을 직접 제어할 수 있음
    Advisor는 Aspect의 구체적인 구현. "어떤 일을 할 것인가"와 "그 일을 어디에 적용할 것인가"
     */
    @Around("@annotation(distributedLock)")  // joinPoint. 특정 어노테이션(distributedLock)이 붙은 메서드를 대상으로 advice 적용
    public Object lock(final ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 메서드 이름을 기반으로 lock 키 생성
        String key = REDISSON_LOCK_PREFIX + method.getName();

        RLock rLock = redissonClient.getLock(key);

        try {
            // 설정된 대기 시간, 보유 시간, 시간 단위를 사용하여 락 시도
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!available) {
                return false;
            }

            // 락을 성공적으로 얻은 경우 비즈니스 로직 수행
            return aopForTransaction.proceed(joinPoint);  // proceed 호출 기준으로 사전/사후처리
        } catch (InterruptedException e) {
            throw new InterruptedException();
        } finally {
            try {
                rLock.unlock();  // 락 해제
            } catch (IllegalMonitorStateException e) {
                log.info("Redisson Lock Already UnLock - serviceName: {}, key: {}",
                        method.getName(), key);
            }
        }
    }
}

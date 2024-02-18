package com.shop.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AopForTransaction {

    @Transactional(propagation = Propagation.REQUIRES_NEW)  // 새로운 트랜잭션 시작
    public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();  // 실제 타겟 메서드 실행
    }
}

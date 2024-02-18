package com.shop.service;

import com.shop.config.DistributedLock;
import com.shop.entity.Coupon;
import com.shop.entity.CouponAvailable;
import com.shop.repository.CouponAvailableRepository;
import com.shop.service.CouponIssuedEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CouponDecreaseService {
    private final CouponAvailableRepository couponAvailableRepository;
    private final EntityManager entityManager;

    private static final int MAX_RETRY = 5;

    @DistributedLock(key = "'CountDecreaseLock-' + #serialNumber", timeUnit = TimeUnit.SECONDS, waitTime = 5L, leaseTime = 3L)
    public boolean couponDecrease(CouponAvailable couponAvailable) {
        try {
            couponAvailable = couponAvailableRepository.findById(couponAvailable.getId())
                    .orElseThrow(() -> new IllegalStateException("Coupon not found"));

            couponAvailable.decrease();
            couponAvailableRepository.save(couponAvailable);
            System.out.println(couponAvailable.getAvailableStock());
            return true;

        } catch (Exception e) {
            System.out.println("Error in couponDecrease: " + e.getMessage());
            return false;
        }
    }
}
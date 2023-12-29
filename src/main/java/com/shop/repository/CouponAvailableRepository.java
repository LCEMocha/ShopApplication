package com.shop.repository;

import com.shop.entity.CouponAvailable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CouponAvailableRepository extends JpaRepository<CouponAvailable, Long> {

    Optional<CouponAvailable> findById(Long couponId);

    Optional<CouponAvailable> findByName(String couponName);
}

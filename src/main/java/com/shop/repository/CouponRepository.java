package com.shop.repository;

import com.shop.entity.Coupon;
import com.shop.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    long countByMember(Member member);
}
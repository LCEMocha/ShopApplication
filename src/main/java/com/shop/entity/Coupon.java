package com.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.util.Date;

@Entity
@Table(name = "coupons")
@Getter
@Setter
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // LAZY 로딩 설정
    @JoinColumn(name = "coupon_available_id") // 외래키가 될 컬럼
    private CouponAvailable couponAvailable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String couponCode;
    private Date expirationDate;
    private String couponStatus;
    private Date issuedDate;
    private Date usedDate;
    private String couponName;

    // 할인 금액
    private Double discountAmount;

    // 할인 퍼센테이지
    private Double discountPercentage;

    // 할인 타입 (예: "amount", "percentage")
    private String discountType;

}


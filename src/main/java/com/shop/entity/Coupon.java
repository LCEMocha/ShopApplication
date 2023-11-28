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
    @JoinColumn(name = "member_id") // 외래키가 될 컬럼
    private Member member;

    private String couponCode;
    private Date expirationDate;
    private Double couponValue;
    private String couponStatus;
    private Date issuedDate;
    private Date usedDate;
    private String couponName;

}


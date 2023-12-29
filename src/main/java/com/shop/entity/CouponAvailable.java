package com.shop.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponAvailable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // 사용 가능한 재고수량
    private Long availableStock;

    // 할인 금액 (정액 할인인 경우)
    private Double discountAmount;

    // 할인 퍼센테이지 (정률 할인인 경우)
    private Double discountPercentage;

    // 할인 타입 (금액 할인인지 퍼센테이지 할인인지 나타내는 플래그)
    private String discountType; // "amount" 또는 "percentage"

    public CouponAvailable(String name, Long availableStock, Double discountAmount, Double discountPercentage, String discountType) {
        this.name = name;
        this.availableStock = availableStock;
        this.discountAmount = discountAmount;
        this.discountPercentage = discountPercentage;
        this.discountType = discountType;
    }

    public void decrease() {
        validateStockCount();
        this.availableStock -= 1;
    }

    private void validateStockCount() {
        if (availableStock < 1) {
            throw new IllegalArgumentException();
        }
    }

    @OneToMany(mappedBy = "couponAvailable")
    private List<Coupon> coupons;
}

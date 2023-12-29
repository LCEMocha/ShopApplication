package com.shop.service;

import com.shop.entity.Coupon;
import org.springframework.context.ApplicationEvent;

public class CouponIssuedEvent extends ApplicationEvent {

    private final Coupon coupon;

    public CouponIssuedEvent(Object source, Coupon coupon) {
        super(source);
        this.coupon = coupon;
    }

    public Coupon getCoupon() {
        return coupon;
    }
}
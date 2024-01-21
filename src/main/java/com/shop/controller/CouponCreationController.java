package com.shop.controller;

import com.shop.service.CouponCreationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/coupons")
public class CouponCreationController {

    private final CouponCreationService couponCreationService;

    @Autowired
    public CouponCreationController(CouponCreationService couponCreationService) {
        this.couponCreationService = couponCreationService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCoupon(String name, Long availableStock, Double discountAmount, Double discountPercentage, String discountType) {
        couponCreationService.createAndSaveCoupon(name, availableStock, discountAmount, discountPercentage, discountType);
        return ResponseEntity.ok("쿠폰 발급 요청함");
    }

}
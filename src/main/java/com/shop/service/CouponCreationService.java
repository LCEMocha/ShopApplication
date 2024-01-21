package com.shop.service;

import com.shop.entity.CouponAvailable;
import com.shop.repository.CouponAvailableRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CouponCreationService {
    private final CouponAvailableRepository couponAvailableRepository;

    @Autowired
    public CouponCreationService(CouponAvailableRepository couponAvailableRepository) {
        this.couponAvailableRepository = couponAvailableRepository;
    }

    /**
     * 새로운 쿠폰을 생성하고 저장한다.
     *
     * @param name 쿠폰의 이름
     * @param availableStock 쿠폰의 수량
     * @param discountAmount 할인 금액
     * @param discountPercentage 할인 퍼센테이지
     * @param discountType 할인 타입 ("amount" 또는 "percentage")
     * @return 생성된 CouponAvailable 객체
     */
    public CouponAvailable createAndSaveCoupon(String name, Long availableStock, Double discountAmount, Double discountPercentage, String discountType) {
        CouponAvailable newCoupon = new CouponAvailable(name, availableStock, discountAmount, discountPercentage, discountType);
        return couponAvailableRepository.save(newCoupon);
    }

    /**
    @PostConstruct
    public void init() {
        createAndSaveHardcodedCoupon();
    }
    **/

    public CouponAvailable createAndSaveHardcodedCoupon() {
        // 하드코딩된 값으로 쿠폰 생성
        String name = "할인쿠폰T";
        Long availableStock = 100L;
        Double discountAmount = 10000.0;
        Double discountPercentage = null;
        String discountType = "amount";

        return createAndSaveCoupon(name, availableStock, discountAmount, discountPercentage, discountType);
    }
}

package com.shop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CouponIssuedEventListener {
    private static final Logger logger = LoggerFactory.getLogger(CouponIssuedEventListener.class);

    private final CouponIssuedResultHolder resultHolder;

    public CouponIssuedEventListener(CouponIssuedResultHolder resultHolder) {
        this.resultHolder = resultHolder;
    }

    @EventListener
    public void onCouponIssued(CouponIssuedEvent event) {
        logger.info("발행이벤트 수신함");
        resultHolder.setIssued(true);
    }
}
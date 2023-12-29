package com.shop.service;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

@Component
public class CouponIssuedResultHolder {
    private AtomicBoolean issued = new AtomicBoolean(false);

    public void setIssued(boolean value) {
        issued.set(value);
    }

    public boolean isIssued() {
        return issued.get();
    }
}

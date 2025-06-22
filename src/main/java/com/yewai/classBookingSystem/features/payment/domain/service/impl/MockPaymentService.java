package com.yewai.classBookingSystem.features.payment.domain.service.impl;

import com.yewai.classBookingSystem.features.payment.domain.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

@Service
public class MockPaymentService implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(MockPaymentService.class);
    private final Random random = new Random();
    @Override
    public boolean addPaymentCard(Map<String, String> cardDetails) {
        if (random.nextInt(100) < 5) {
            log.error("MOCK PAYMENT FAILED: Failed to add payment card. Details: {}", cardDetails);
            return false;
        }
        log.info("MOCK PAYMENT SUCCESS: Successfully added payment card. Details: {}", cardDetails);
        return true;
    }

    @Override
    public boolean chargePayment(BigDecimal amount, String paymentRef) {
        if (random.nextInt(100) < 5) {
            log.error("MOCK PAYMENT FAILED: Failed to charge {} for reference {}", amount, paymentRef);
            return false;
        }
        log.info("MOCK PAYMENT SUCCESS: Charged {} for reference {}", amount, paymentRef);
        return true;
    }
}

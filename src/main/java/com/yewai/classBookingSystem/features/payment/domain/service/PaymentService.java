package com.yewai.classBookingSystem.features.payment.domain.service;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentService {

    public boolean addPaymentCard(Map<String, String> cardDetails);

    public boolean chargePayment(BigDecimal amount, String paymentRef);
}

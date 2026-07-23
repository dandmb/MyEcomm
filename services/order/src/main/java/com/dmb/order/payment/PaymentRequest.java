package com.dmb.order.payment;


import com.dmb.order.customer.CustomerResponse;
import com.dmb.order.order.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
    BigDecimal amount,
    PaymentMethod paymentMethod,
    Integer orderId,
    String orderReference,
    CustomerResponse customer
) {
}

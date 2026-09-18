package com.fundflow.payment;

import com.fundflow.entity.TxnStatus;

/**
 * Outcome of a payment attempt, returned by any PaymentService implementation.
 * mockReference stands in for whatever reference id a real gateway would return
 * (e.g. Razorpay's payment_id) - keeping this shape means a future real
 * implementation slots in without changing callers.
 */
public record PaymentResult(TxnStatus status, String reference) {
}

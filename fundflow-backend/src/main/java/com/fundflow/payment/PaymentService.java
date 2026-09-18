package com.fundflow.payment;

import com.fundflow.entity.Donation;

/**
 * Abstraction over "how a donation actually gets paid for".
 * Only MockPaymentService exists right now. A future RazorpayPaymentService
 * would implement this same interface so DonationService never has to change -
 * only which bean gets wired in.
 */
public interface PaymentService {

    /**
     * Attempts to charge the given donation via the given method
     * (e.g. "MOCK_CARD", "MOCK_UPI") and returns the outcome.
     * Implementations must NOT move real money in this project phase.
     */
    PaymentResult processPayment(Donation donation, String method);
}

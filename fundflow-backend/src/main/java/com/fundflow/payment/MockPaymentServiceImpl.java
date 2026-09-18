package com.fundflow.payment;

import com.fundflow.entity.Donation;
import com.fundflow.entity.TxnStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulated payment gateway. No real money moves and no external network
 * call is made - this only exists so the full donate -> pay -> result flow
 * can be built and demoed end to end before a real gateway is added.
 *
 * Outcome is weighted to feel realistic for a demo: mostly SUCCESS, with an
 * occasional FAILED or PENDING so the UI/notification paths for all three
 * states are actually exercised.
 */
@Service
public class MockPaymentServiceImpl implements PaymentService {

    private static final int SUCCESS_WEIGHT = 85;
    private static final int FAILED_WEIGHT = 10;
    // remaining 5% -> PENDING

    @Override
    public PaymentResult processPayment(Donation donation, String method) {
        int roll = ThreadLocalRandom.current().nextInt(100);
        TxnStatus status;
        if (roll < SUCCESS_WEIGHT) {
            status = TxnStatus.SUCCESS;
        } else if (roll < SUCCESS_WEIGHT + FAILED_WEIGHT) {
            status = TxnStatus.FAILED;
        } else {
            status = TxnStatus.PENDING;
        }

        String reference = "MOCK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        return new PaymentResult(status, reference);
    }
}

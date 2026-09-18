package com.fundflow.payment;

import com.fundflow.entity.Campaign;
import com.fundflow.entity.Donation;
import org.junit.jupiter.api.RepeatedTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MockPaymentServiceImplTest {

    private final MockPaymentServiceImpl paymentService = new MockPaymentServiceImpl();

    @RepeatedTest(20) // outcome is randomized, so run several times
    void processPayment_alwaysReturnsAValidResultWithReference() {
        Donation donation = Donation.builder()
                .id(1L)
                .campaign(Campaign.builder().id(1L).build())
                .amount(BigDecimal.TEN)
                .build();

        PaymentResult result = paymentService.processPayment(donation, "MOCK_CARD");

        assertThat(result.status()).isNotNull();
        assertThat(result.reference()).startsWith("MOCK-");
    }
}

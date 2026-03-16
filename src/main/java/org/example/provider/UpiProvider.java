package org.example.provider;

import org.example.model.PaymentRequest;
import org.example.model.PaymentResponse;
import org.example.model.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Simulates UPI payment processing.
 *
 * In a real system, this would call NPCI's UPI API.
 * Here we simulate with a 98% success rate.
 *
 * Best for: INR payments up to ₹1,00,000 (UPI transaction limit).
 */
@Component
public class UpiProvider implements PaymentProvider {

    private static final double SUCCESS_RATE = 0.98;
    private static final double UPI_MAX_AMOUNT = 100000.0; // ₹1 lakh limit

    private final Random random = new Random();

    @Override
    public PaymentResponse process(PaymentRequest request, String transactionId) {
        boolean success = random.nextDouble() < SUCCESS_RATE;

        if (success) {
            return new PaymentResponse(
                transactionId,
                PaymentStatus.SUCCESS,
                getName(),
                request.getAmount(),
                request.getCurrency(),
                "Payment processed via UPI successfully",
                false
            );
        } else {
            return new PaymentResponse(
                transactionId,
                PaymentStatus.FAILED,
                getName(),
                request.getAmount(),
                request.getCurrency(),
                "UPI transaction declined — please retry",
                false
            );
        }
    }

    @Override
    public boolean supports(String currency, double amount) {
        // UPI supports INR only, and has a ₹1 lakh per transaction limit
        return "INR".equalsIgnoreCase(currency) && amount <= UPI_MAX_AMOUNT;
    }

    @Override
    public String getName() {
        return "UPI";
    }
}

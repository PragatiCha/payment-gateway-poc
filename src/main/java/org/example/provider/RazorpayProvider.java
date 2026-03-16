package org.example.provider;

import org.example.model.PaymentRequest;
import org.example.model.PaymentResponse;
import org.example.model.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Simulates Razorpay payment processing.
 *
 * In a real system, this would call Razorpay's REST API using your API key.
 * Best for: INR payments above UPI's ₹1 lakh limit, or when you need
 * card + netbanking + UPI all in one provider.
 *
 * Success rate: 90%
 */
@Component
public class RazorpayProvider implements PaymentProvider {

    private static final double SUCCESS_RATE = 0.90;

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
                "Payment processed via Razorpay successfully",
                false
            );
        } else {
            return new PaymentResponse(
                transactionId,
                PaymentStatus.FAILED,
                getName(),
                request.getAmount(),
                request.getCurrency(),
                "Razorpay payment failed — bank declined the transaction",
                false
            );
        }
    }

    @Override
    public boolean supports(String currency, double amount) {
        // Razorpay handles all INR amounts (no upper limit like UPI)
        return "INR".equalsIgnoreCase(currency);
    }

    @Override
    public String getName() {
        return "Razorpay";
    }
}

package org.example.provider;

import org.example.model.PaymentRequest;
import org.example.model.PaymentResponse;
import org.example.model.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Simulates Stripe payment processing.
 *
 * In a real system, this would use the Stripe Java SDK:
 *   PaymentIntent.create(params)
 *
 * Best for: USD and EUR international payments.
 * Success rate: 95%
 */
@Component
public class StripeProvider implements PaymentProvider {

    private static final double SUCCESS_RATE = 0.95;

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
                "Payment processed via Stripe successfully",
                false
            );
        } else {
            return new PaymentResponse(
                transactionId,
                PaymentStatus.FAILED,
                getName(),
                request.getAmount(),
                request.getCurrency(),
                "Stripe payment failed — card declined",
                false
            );
        }
    }

    @Override
    public boolean supports(String currency, double amount) {
        // Stripe handles USD and EUR
        return "USD".equalsIgnoreCase(currency) || "EUR".equalsIgnoreCase(currency);
    }

    @Override
    public String getName() {
        return "Stripe";
    }
}

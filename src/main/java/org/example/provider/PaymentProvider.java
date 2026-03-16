package org.example.provider;

import org.example.model.PaymentRequest;
import org.example.model.PaymentResponse;

/**
 * This is the KEY design pattern here — the Strategy Pattern.
 *
 * Every payment provider (Stripe, Razorpay, UPI) implements this same interface.
 * This means our gateway doesn't care WHICH provider it talks to —
 * it just calls process() and gets a response back.
 *
 * Interview talking point:
 * "I used the Strategy Pattern so adding a new provider (e.g. PayPal)
 *  means just creating a new class — no changes to existing code."
 * That's the Open/Closed Principle from SOLID.
 */
public interface PaymentProvider {

    /**
     * Process a payment request and return a response.
     * @param request  the payment details
     * @param transactionId  unique ID we generated for this transaction
     */
    PaymentResponse process(PaymentRequest request, String transactionId);

    /**
     * Which currencies does this provider support?
     * Used by the router to decide which provider to pick.
     */
    boolean supports(String currency, double amount);

    /**
     * Human-readable name of this provider.
     */
    String getName();
}

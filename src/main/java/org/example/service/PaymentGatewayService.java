package org.example.service;

import org.example.model.PaymentRequest;
import org.example.model.PaymentResponse;
import org.example.provider.*;
import org.example.repository.PaymentRepository;
import org.example.router.PaymentRouter;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * The heart of the payment gateway.
 *
 * This service orchestrates the full payment flow:
 *
 *  Step 1 — Check idempotency
 *            If we've seen this key before → return cached response immediately
 *            (This prevents duplicate charges on network retries)
 *
 *  Step 2 — Route to the right provider
 *            Based on currency and amount, pick UPI / Razorpay / Stripe
 *
 *  Step 3 — Process the payment
 *            Call the chosen provider's process() method
 *
 *  Step 4 — Save and return
 *            Store the result so future duplicate requests get the cached response
 */
@Service
public class PaymentGatewayService {

    private final PaymentRouter router;
    private final PaymentRepository repository;

    public PaymentGatewayService(PaymentRouter router, PaymentRepository repository) {
        this.router = router;
        this.repository = repository;
    }

    /**
     * Process a payment request.
     *
     * @param request         the payment details (amount, currency)
     * @param idempotencyKey  unique key from the client — same key = same response
     */
    public PaymentResponse processPayment(PaymentRequest request, String idempotencyKey) {

        // ── Step 1: Idempotency Check ─────────────────────────────────────────
        // Has this exact request been made before?
        Optional<PaymentResponse> existingResponse = repository.findByIdempotencyKey(idempotencyKey);

        if (existingResponse.isPresent()) {
            // We've seen this key before — return the SAME response, don't charge again
            // We also mark it as fromCache=true so the client knows it was a duplicate
            System.out.println("Duplicate request detected for key: " + idempotencyKey + " — returning cached response");
            return buildCachedResponse(existingResponse.get());
        }

        // ── Step 2: Route to the right provider ───────────────────────────────
        PaymentProvider provider = router.route(request.getCurrency(), request.getAmount());
        System.out.println("Routing to provider: " + provider.getName());

        // ── Step 3: Generate a unique transaction ID ───────────────────────────
        // This is what the client uses to track this specific payment
        String transactionId = "txn_" + UUID.randomUUID().toString().substring(0, 8);

        // ── Step 4: Process the payment ───────────────────────────────────────
        PaymentResponse response = provider.process(request, transactionId);
        System.out.println("Payment result: " + response.getStatus() + " via " + response.getProvider());

        // ── Step 5: Save result ────────────────────────────────────────────────
        // Now if the same idempotencyKey comes again, we return this stored response
        repository.save(idempotencyKey, response);

        return response;
    }

    /**
     * Look up a payment by its transaction ID.
     * Used for the GET /payments/{id} endpoint.
     */
    public Optional<PaymentResponse> getPaymentStatus(String transactionId) {
        return repository.findByTransactionId(transactionId);
    }

    /**
     * Build a cached response — same data, but fromCache=true.
     * This tells the client: "we got your duplicate request, here's the original result."
     */
    private PaymentResponse buildCachedResponse(PaymentResponse original) {
        return new PaymentResponse(
            original.getTransactionId(),
            original.getStatus(),
            original.getProvider(),
            original.getAmount(),
            original.getCurrency(),
            original.getMessage() + " [duplicate request — cached response returned]",
            true  // ← fromCache flag
        );
    }
}

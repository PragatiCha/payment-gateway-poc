package org.example.repository;

import org.example.model.PaymentResponse;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory store — acts as our "database" for this POC.
 *
 * Two maps are maintained:
 *
 * 1. idempotencyStore  →  idempotencyKey  : PaymentResponse
 *    Checks if we've already processed this exact request.
 *    If yes → return cached response, skip re-processing.
 *
 * 2. transactionStore  →  transactionId   : PaymentResponse
 *    Lets clients look up payment status via GET /payments/{transactionId}
 *
 * In production:
 *   idempotencyStore → Redis (fast lookup + TTL so keys expire after 24 hrs)
 *   transactionStore → PostgreSQL (durable, queryable, audit trail)
 */
@Component
public class PaymentRepository {

    private final Map<String, PaymentResponse> idempotencyStore = new HashMap<>();
    private final Map<String, PaymentResponse> transactionStore = new HashMap<>();

    /** Check if we've already processed a request with this idempotency key */
    public Optional<PaymentResponse> findByIdempotencyKey(String idempotencyKey) {
        return Optional.ofNullable(idempotencyStore.get(idempotencyKey));
    }

    /** Look up a payment by its transaction ID */
    public Optional<PaymentResponse> findByTransactionId(String transactionId) {
        return Optional.ofNullable(transactionStore.get(transactionId));
    }

    /** Save against both keys so future lookups work from either direction */
    public void save(String idempotencyKey, PaymentResponse response) {
        idempotencyStore.put(idempotencyKey, response);
        transactionStore.put(response.getTransactionId(), response);
    }
}

package org.example.model;

/**
 * What we return to the client after processing.
 *
 * Example response:
 * {
 *   "transactionId": "txn_a1b2c3",
 *   "status": "SUCCESS",
 *   "provider": "UPI",
 *   "amount": 500.0,
 *   "currency": "INR",
 *   "message": "Payment processed via UPI successfully",
 *   "fromCache": false
 * }
 *
 * fromCache = true means this was a duplicate request — idempotency kicked in.
 */
public class PaymentResponse {

    private String transactionId;
    private PaymentStatus status;
    private String provider;       // "UPI", "Razorpay", "Stripe"
    private double amount;
    private String currency;
    private String message;
    private boolean fromCache;     // true = duplicate request, cached response returned

    public PaymentResponse(String transactionId, PaymentStatus status, String provider,
                           double amount, String currency, String message, boolean fromCache) {
        this.transactionId = transactionId;
        this.status = status;
        this.provider = provider;
        this.amount = amount;
        this.currency = currency;
        this.message = message;
        this.fromCache = fromCache;
    }

    public String getTransactionId() { return transactionId; }
    public PaymentStatus getStatus() { return status; }
    public String getProvider() { return provider; }
    public double getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getMessage() { return message; }
    public boolean isFromCache() { return fromCache; }
}

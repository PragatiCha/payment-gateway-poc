package org.example.model;

/**
 * What the client sends in the POST /payments request body.
 *
 * Example JSON:
 * {
 *   "amount": 500.0,
 *   "currency": "INR",
 *   "description": "Order #1234"
 * }
 *
 * Note: idempotencyKey comes as a REQUEST HEADER, not in the body.
 * Header name: Idempotency-Key
 */
public class PaymentRequest {

    private double amount;
    private String currency;      // "INR", "USD", "EUR"
    private String description;

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

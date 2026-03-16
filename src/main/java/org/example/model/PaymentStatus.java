package org.example.model;

/**
 * The 3 states a payment can be in.
 *
 * INITIATED → request received, not yet sent to provider
 * SUCCESS   → provider accepted the payment
 * FAILED    → provider rejected or an error occurred
 */
public enum PaymentStatus {
    INITIATED,
    SUCCESS,
    FAILED
}

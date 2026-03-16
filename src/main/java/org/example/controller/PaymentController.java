package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.PaymentRequest;
import org.example.model.PaymentResponse;
import org.example.service.PaymentGatewayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API — two endpoints:
 *
 *   POST /payments          → initiate a payment
 *   GET  /payments/{id}     → check payment status
 */
@RestController
@RequestMapping("/payments")
@Tag(name = "Payment Gateway", description = "Initiate payments and check status")
public class PaymentController {

    private final PaymentGatewayService gatewayService;

    public PaymentController(PaymentGatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @PostMapping
    @Operation(
            summary = "Initiate a payment",
            description = "Routes to UPI (INR ≤ ₹1L), Razorpay (INR > ₹1L), or Stripe (USD/EUR). " +
                    "Send the same Idempotency-Key twice to see duplicate prevention in action."
    )
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestBody PaymentRequest request,
            @Parameter(description = "Unique key to prevent duplicate charges. E.g: order-001")
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        if (request.getAmount() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getCurrency() == null || request.getCurrency().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        PaymentResponse response = gatewayService.processPayment(request, idempotencyKey);

        // 201 Created for new payments, 200 OK for cached/duplicate responses
        HttpStatus status = response.isFromCache() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{transactionId}")
    @Operation(
            summary = "Get payment status",
            description = "Look up a payment by the transactionId returned from POST /payments"
    )
    public ResponseEntity<PaymentResponse> getPaymentStatus(
            @Parameter(description = "Transaction ID returned from POST /payments. E.g: txn_a1b2c3")
            @PathVariable String transactionId) {

        return gatewayService.getPaymentStatus(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
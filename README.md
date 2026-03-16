# Payment Gateway POC 🏦

> Part of Pragati Chaturvedi's 30-Day Fintech Engineering Learning Journey
> Day 01–03 concepts: Payment Rails + Idempotency + Status Tracking

---

## What This Does

A runnable Spring Boot app that simulates a payment gateway with:
- **3 payment providers** — UPI, Razorpay, Stripe (simulated, no real API keys needed)
- **Smart routing** — picks the right provider based on currency and amount
- **Idempotency** — same request key always returns same result, no duplicate charges
- **Status tracking** — look up any payment by its transaction ID

---

## How to Run

```bash
# 1. Clone and go into the project
cd payment-gateway-poc

# 2. Run it
./gradlew bootRun

# Server starts at http://localhost:8080
```

---

## Try It Out (copy-paste these curl commands)

### 1. Make a UPI payment (INR, small amount)
```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: order-001" \
  -d '{"amount": 500, "currency": "INR", "description": "Coffee order"}'
```
Expected: routes to **UPI**, returns `transactionId`

---

### 2. Make a Razorpay payment (INR, large amount)
```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: order-002" \
  -d '{"amount": 150000, "currency": "INR", "description": "Laptop purchase"}'
```
Expected: routes to **Razorpay** (amount > ₹1 lakh UPI limit)

---

### 3. Make a Stripe payment (USD)
```bash
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: order-003" \
  -d '{"amount": 99, "currency": "USD", "description": "SaaS subscription"}'
```
Expected: routes to **Stripe**

---

### 4. Test idempotency — run the SAME request twice
```bash
# Run this twice with the SAME Idempotency-Key
curl -X POST http://localhost:8080/payments \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: order-004" \
  -d '{"amount": 200, "currency": "INR"}'

# Second call returns the SAME transactionId with "fromCache": true
# No double charge!
```

---

### 5. Check payment status
```bash
# Replace txn_xxxxxxxx with the transactionId from step 1
curl http://localhost:8080/payments/txn_xxxxxxxx
```

---

## Routing Logic

| Currency | Amount        | Provider   |
|----------|---------------|------------|
| INR      | ≤ ₹1,00,000   | UPI        |
| INR      | > ₹1,00,000   | Razorpay   |
| USD      | any           | Stripe     |
| EUR      | any           | Stripe     |

---

## Key Design Decisions (for interviews)

**Why the Strategy Pattern for providers?**
Each provider implements the same `PaymentProvider` interface.
Adding PayPal tomorrow = create one new class, zero changes to existing code.
That's the Open/Closed Principle.

**Why is idempotency key in the header, not the body?**
Same as Stripe's design — the key is about the *request*, not the payment itself.
It's a transport concern, not a business concern.

**Why two separate maps in the repository?**
`idempotencyStore` → fast deduplication lookup by client key
`transactionStore` → status lookup by our internal transaction ID
In production: Redis for idempotency (with TTL), PostgreSQL for transactions.

**What would you add next?**
- Retry logic: if UPI fails, fall back to Razorpay automatically
- Kafka: publish a `payment.processed` event after every transaction
- Webhook: notify the merchant asynchronously when payment settles

---

## Project Structure

```
src/main/java/com/pragati/gateway/
├── PaymentGatewayApplication.java   ← Spring Boot entry point
├── controller/
│   └── PaymentController.java       ← REST endpoints (POST + GET)
├── model/
│   ├── PaymentRequest.java          ← What client sends
│   ├── PaymentResponse.java         ← What we return
│   └── PaymentStatus.java           ← INITIATED / SUCCESS / FAILED
├── provider/
│   ├── PaymentProvider.java         ← Interface (Strategy Pattern)
│   ├── UpiProvider.java             ← INR small payments
│   ├── RazorpayProvider.java        ← INR large payments
│   └── StripeProvider.java          ← USD / EUR payments
├── service/
│   ├── PaymentGatewayService.java   ← Core logic: idempotency + route + process
│   └── PaymentRouter.java           ← Picks the right provider
└── repository/
    └── PaymentRepository.java       ← In-memory store (HashMap)
```

package org.example.router;

import org.example.provider.PaymentProvider;
import org.example.provider.RazorpayProvider;
import org.example.provider.StripeProvider;
import org.example.provider.UpiProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The PaymentRouter decides WHICH provider handles a payment.
 *
 * Routing logic (simple rules for the POC):
 *
 *   INR + amount <= ₹1,00,000  →  UPI   (cheapest, fastest for small INR)
 *   INR + amount >  ₹1,00,000  →  Razorpay  (handles large INR amounts)
 *   USD or EUR                 →  Stripe  (international currencies)
 *
 * Interview talking point:
 * "In production, routing can be more sophisticated — least-cost routing,
 *  provider health checks, A/B testing between providers, or even ML-based
 *  routing to maximise authorization rates."
 *
 * Spring automatically injects ALL classes that implement PaymentProvider
 * into the 'providers' list. So if we add PayPal tomorrow, it just works.
 */
@Component
public class PaymentRouter {

    private final List<PaymentProvider> providers;

    // Spring injects UpiProvider, RazorpayProvider, StripeProvider automatically
    public PaymentRouter(UpiProvider upi, RazorpayProvider razorpay, StripeProvider stripe) {
        this.providers = List.of(upi, razorpay, stripe);
    }

    /**
     * Find the right provider for this currency and amount.
     * We go through the list and pick the first one that supports it.
     *
     * UPI is registered first (lowest cost), so it gets priority for INR.
     */
    public PaymentProvider route(String currency, double amount) {

        return providers.stream()
                .filter(provider -> provider.supports(currency, amount))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                    "No payment provider found for currency: " + currency + ", amount: " + amount
                ));
    }
}

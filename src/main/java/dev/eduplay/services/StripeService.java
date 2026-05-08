package dev.eduplay.services;

import com.stripe.Stripe;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Service to manage Stripe configuration and initialization.
 */
public class StripeService {

    private static StripeService instance;
    private final String apiKey;

    public StripeService() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        
        // Search for STRIPE_SECRET_KEY in .env or environment variables
        this.apiKey = dotenv.get("STRIPE_SECRET_KEY");
        
        if (apiKey != null && !apiKey.isEmpty()) {
            Stripe.apiKey = apiKey;
            System.out.println("[StripeService] Stripe initialized with key from .env");
        } else {
            System.err.println("[StripeService] WARNING: STRIPE_SECRET_KEY not found in .env");
        }
    }

    public static StripeService getInstance() {
        if (instance == null) instance = new StripeService();
        return instance;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    public String getApiKey() {
        return apiKey;
    }
}

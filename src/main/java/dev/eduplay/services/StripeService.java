package dev.eduplay.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Minimal Stripe helper that creates a Checkout Session using the secret key
 * read from environment variables or a project .env file. It does a simple
 * HTTP POST to Stripe and returns the session URL which is opened in the
 * system browser.
 *
 * IMPORTANT: keep the secret key out of source control. Set STRIPE_SECRET_KEY
 * in your environment or create a local .env file with STRIPE_SECRET_KEY=sk_test_...
 */
public class StripeService {

    private static final String STRIPE_API = "https://api.stripe.com/v1/checkout/sessions";
    private final String secretKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public StripeService() {
        this.secretKey = resolveSecretKey();
    }

    private String resolveSecretKey() {
        String k = System.getenv("STRIPE_SECRET_KEY");
        if (k != null && !k.isBlank()) {
            k = k.trim();
            // Reject obvious placeholders or invalid keys (e.g. 'your_test...here')
            if (k.matches("(?i)^sk_(test|live)_[A-Za-z0-9_]{10,}$")) return k;
            System.out.println("[StripeService] STRIPE_SECRET_KEY present but does not look like a valid key; ignoring.");
        }
        // try loading .env in project root
        try {
            File env = new File(System.getProperty("user.dir"), ".env");
            if (env.exists()) {
                List<String> lines = Files.readAllLines(env.toPath(), StandardCharsets.UTF_8);
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
                    String[] parts = line.split("=", 2);
                    String name = parts[0].trim();
                    String val = parts[1].trim();
                    if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                        val = val.substring(1, val.length() - 1);
                    }
                    if ("STRIPE_SECRET_KEY".equals(name)) {
                        val = val.trim();
                        if (val.matches("(?i)^sk_(test|live)_[A-Za-z0-9_]{10,}$")) return val;
                        System.out.println("[StripeService] .env contains STRIPE_SECRET_KEY but it does not look valid; ignoring.");
                    }
                }
            }
        } catch (IOException ignored) {}
        return null;
    }

    public boolean isConfigured() { return secretKey != null && !secretKey.isBlank(); }

    /**
     * Create a Stripe Checkout session and open the returned URL in the system browser.
     * price in euros (use totalPrice as euros). Returns the session URL or throws.
     */
    public String createCheckoutAndOpen(String productName, double totalPrice, int quantity) throws Exception {
        if (!isConfigured()) throw new IllegalStateException("Stripe secret key not configured (STRIPE_SECRET_KEY)");
        List<String> parts = new ArrayList<>();
        // helper to add key=value form-encoded
        var add = (FormAdder) (k, v) -> parts.add(encode(k) + "=" + encode(v));

        add.add("mode", "payment");
        add.add("payment_method_types[]", "card");
        add.add("success_url", "https://example.com/success?session_id={CHECKOUT_SESSION_ID}");
        add.add("cancel_url", "https://example.com/cancel");

        add.add("line_items[0][price_data][currency]", "eur");
        add.add("line_items[0][price_data][product_data][name]", productName);
        long amountCents = Math.round(totalPrice * 100.0);
        add.add("line_items[0][price_data][unit_amount]", String.valueOf(amountCents));
        add.add("line_items[0][quantity]", String.valueOf(quantity));

        String body = String.join("&", parts);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(STRIPE_API))
                .header("Authorization", "Bearer " + secretKey)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        int code = resp.statusCode();
        String respBody = resp.body() != null ? resp.body() : "";
        if (code >= 200 && code < 300) {
            JsonNode node = mapper.readTree(respBody);
            String url = node.path("url").asText(null);
            if (url != null) {
                try { Desktop.getDesktop().browse(new URI(url)); } catch (Exception ignored) {}
                return url;
            }
            throw new RuntimeException("Stripe response did not contain a URL: " + respBody);
        } else {
            // Try to provide a clearer message for authentication issues
            if (code == 401 || code == 402) {
                throw new RuntimeException("Stripe API authentication error (" + code + "). Vérifiez votre STRIPE_SECRET_KEY. Réponse: " + respBody);
            }
            throw new RuntimeException("Stripe API error (" + code + "): " + respBody);
        }
    }

    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    @FunctionalInterface
    private interface FormAdder { void add(String k, String v); }

}


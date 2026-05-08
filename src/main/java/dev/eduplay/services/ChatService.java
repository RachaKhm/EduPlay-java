package dev.eduplay.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Simple ChatService: if OPENAI_API_KEY is set (env or .env) it forwards to OpenAI Chat API.
 * Otherwise returns simple fallback answers.
 */
public class ChatService {

    private static final String OPENAI_API = "https://api.openai.com/v1/chat/completions";
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;

    public ChatService() {
        this.apiKey = resolveApiKey();
    }

    private String resolveApiKey() {
        String k = System.getenv("OPENAI_API_KEY");
        if (k != null && !k.isBlank()) return k.trim();
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
                    if ("OPENAI_API_KEY".equals(name)) return val.replace("\"", "").trim();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public boolean isConfigured() { return apiKey != null && !apiKey.isBlank(); }

    public String ask(String question) throws Exception {
        if (!isConfigured()) return fallbackAnswer(question);

        // Build simple chat request (gpt-3.5-turbo)
        String payload = mapper.createObjectNode()
                .put("model", "gpt-3.5-turbo")
                .set("messages", mapper.createArrayNode()
                        .add(mapper.createObjectNode().put("role", "system").put("content", "You are a helpful assistant for parents using EduPlay."))
                        .add(mapper.createObjectNode().put("role", "user").put("content", question)))
                .toString();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_API))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            JsonNode root = mapper.readTree(resp.body());
            JsonNode msg = root.path("choices").get(0).path("message").path("content");
            return msg.asText("Désolé, je n'ai pas de réponse.");
        } else {
            throw new RuntimeException("OpenAI API error (" + resp.statusCode() + "): " + resp.body());
        }
    }

    private String fallbackAnswer(String question) {
        String q = question.toLowerCase();
        if (q.contains("commande") || q.contains("paiement") || q.contains("payer")) {
            return "Pour payer une commande, allez dans Mes commandes et cliquez sur le bouton 'Payer'. Si vous avez besoin d'aide, précisez le numéro de commande.";
        }
        if (q.contains("livre") || q.contains("biblioth")) {
            return "Pour demander un livre, utilisez la section Demandes de livres dans la barre latérale.";
        }
        return "Désolé, je n'ai pas compris. Pouvez-vous reformuler ?";
    }
}


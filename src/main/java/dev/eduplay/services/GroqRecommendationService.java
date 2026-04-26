package dev.eduplay.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroqRecommendationService {

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final URI endpoint;

    public GroqRecommendationService(File configFile) throws IOException {
        Objects.requireNonNull(configFile, "configFile is required");
        Properties cfg = new Properties();
        try (FileInputStream in = new FileInputStream(configFile)) {
            cfg.load(in);
        }
        this.apiKey = require(cfg, "groq.apiKey");
        this.model = cfg.getProperty("groq.model", "llama-3.1-8b-instant").trim();
        this.endpoint = URI.create(cfg.getProperty("groq.endpoint", "https://api.groq.com/openai/v1/chat/completions").trim());
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(12))
                .build();
    }

    public String recommendSeances(String context) throws IOException, InterruptedException {
        String payload = """
                {
                  "model": "%s",
                  "temperature": 0.4,
                  "messages": [
                    {
                      "role": "system",
                      "content": "Tu es un assistant EduPlay pour parents. Réponds en français clair et court."
                    },
                    {
                      "role": "user",
                      "content": "%s"
                    }
                  ]
                }
                """.formatted(escapeJson(model), escapeJson(context));

        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(25))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Groq API error (" + response.statusCode() + "): " + response.body());
        }

        String content = extractAssistantContent(response.body());
        if (content == null || content.isBlank()) {
            throw new IOException("Réponse Groq invalide (content vide).");
        }
        return content.trim();
    }

    private static String extractAssistantContent(String json) {
        Pattern p = Pattern.compile("\"content\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher m = p.matcher(json);
        while (m.find()) {
            String raw = m.group(1);
            String text = unescapeJson(raw);
            if (text != null && !text.isBlank()) return text;
        }
        return null;
    }

    private static String unescapeJson(String s) {
        String out = s;
        out = out.replace("\\n", "\n");
        out = out.replace("\\r", "\r");
        out = out.replace("\\t", "\t");
        out = out.replace("\\\"", "\"");
        out = out.replace("\\\\", "\\");
        return out;
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String require(Properties cfg, String key) {
        String value = cfg.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing Groq setting: " + key);
        }
        return value.trim();
    }
}

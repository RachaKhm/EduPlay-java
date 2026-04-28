package dev.eduplay.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class QuizService {

    private static final String API_KEY = System.getenv("GROQ_API_KEY") != null ? System.getenv("GROQ_API_KEY") : "VOTRE_CLE_API_ICI";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public String generateQuizJson(String bookText) {
        try {
            String systemPrompt = "Tu es un créateur de quiz éducatifs pour enfants. " +
                    "Lis l'histoire et génère un quiz de 10 questions à choix multiples. " +
                    "Tu DOIS répondre EXCLUSIVEMENT avec un tableau JSON valide. Ne dis RIEN d'autre avant ou après le JSON. " +
                    "Format attendu:\\n" +
                    "[\\n" +
                    "  {\\n" +
                    "    \\\"question\\\": \\\"Quelle est la couleur du chat ?\\\",\\n" +
                    "    \\\"options\\\": [\\\"Rouge\\\", \\\"Bleu\\\", \\\"Noir\\\"],\\n" +
                    "    \\\"answer\\\": \\\"Noir\\\"\\n" +
                    "  }\\n" +
                    "]";

            // Limiter la taille du texte pour ne pas dépasser la limite de tokens de l'API (12000 TPM)
            String truncatedText = bookText;
            if (truncatedText.length() > 10000) {
                truncatedText = truncatedText.substring(0, 10000);
            }
            String userMessage = "Voici l'histoire :\\n" + escapeJson(truncatedText);

            String jsonBody = "{"
                    + "\"model\": \"llama-3.3-70b-versatile\","
                    + "\"messages\": ["
                    + "  {\"role\": \"system\", \"content\": \"" + systemPrompt + "\"},"
                    + "  {\"role\": \"user\", \"content\": \"" + userMessage + "\"}"
                    + "],"
                    + "\"temperature\": 0.3,"
                    + "\"max_tokens\": 2048"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(45))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                System.err.println("[QuizService] Erreur API : " + response.body());
                return "ERROR:" + response.statusCode() + " " + response.body();
            }

            return extractContent(response.body());

        } catch (Exception e) {
            e.printStackTrace();
            return "EXCEPTION:" + e.getMessage();
        }
    }

    private String extractContent(String jsonBody) {
        try {
            int messageIndex = jsonBody.indexOf("\"message\"");
            if (messageIndex == -1) return null;
            
            int contentIndex = jsonBody.indexOf("\"content\"", messageIndex);
            if (contentIndex != -1) {
                int startIndex = jsonBody.indexOf("\"", contentIndex + 9);
                if (startIndex != -1) {
                    startIndex++; // skip quote
                    int endIndex = startIndex;
                    while (endIndex < jsonBody.length()) {
                        if (jsonBody.charAt(endIndex) == '"' && jsonBody.charAt(endIndex - 1) != '\\') {
                            break;
                        }
                        endIndex++;
                    }
                    
                    String content = jsonBody.substring(startIndex, endIndex);
                    
                    // Unescape JSON string
                    content = content.replace("\\n", "\n")
                                     .replace("\\\"", "\"")
                                     .replace("\\\\", "\\")
                                     .replace("\\r", "")
                                     .replace("\\t", "\t");
                    return content;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        // Supprimer les caractères de contrôle invisibles sauf ceux gérés ci-dessous
        s = s.replaceAll("[\\x00-\\x07\\x0B\\x0E-\\x1F]", "");
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

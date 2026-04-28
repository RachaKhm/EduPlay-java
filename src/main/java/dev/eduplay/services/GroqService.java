package dev.eduplay.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroqService {

    private static final String API_KEY = System.getenv("GROQ_API_KEY") != null ? System.getenv("GROQ_API_KEY") : "VOTRE_CLE_API_ICI";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
            
    private String systemPrompt = "Tu es un bibliothécaire IA très sympathique pour enfants. " +
            "Ton rôle est de recommander des livres adaptés à leur âge et à leurs goûts. " +
            "Sois toujours encourageant, utilise des emojis, et donne des réponses courtes et faciles à lire pour un enfant. " +
            "Pose des questions pour découvrir ce qu'ils aiment si tu ne le sais pas.";

    private final List<String> messageHistory = new ArrayList<>();

    public GroqService() {
        messageHistory.add("{\"role\": \"system\", \"content\": \"" + escapeJson(systemPrompt) + "\"}");
    }

    public void setLibraryContext(String context) {
        // Mettre à jour le prompt système avec le contexte de la bibliothèque
        String fullPrompt = systemPrompt + "\\n\\nVoici la liste des livres actuellement disponibles dans la bibliothèque :\\n" + context + 
                            "\\n\\nIMPORTANT : " +
                            "1. Tu DOIS recommander en priorité les livres de cette liste s'ils correspondent à l'âge et aux goûts de l'enfant. " +
                            "2. Lorsqu'un enfant te donne son âge (ex: 6 ans), vérifie l'âge recommandé de chaque livre. Si l'âge de l'enfant se trouve DANS l'intervalle (ex: 5 à 8 ans, ou exactement 6 ans), c'est un match parfait ! Recommande-le. " +
                            "3. Si aucun livre de la liste ne correspond parfaitement à l'âge demandé, tu peux alors recommander d'autres livres célèbres en précisant qu'ils ne sont pas dans la bibliothèque actuelle.";
        
        // Remplacer le premier message (le system prompt)
        if (!messageHistory.isEmpty() && messageHistory.get(0).contains("\"role\": \"system\"")) {
            messageHistory.set(0, "{\"role\": \"system\", \"content\": \"" + escapeJson(fullPrompt) + "\"}");
        } else {
            messageHistory.add(0, "{\"role\": \"system\", \"content\": \"" + escapeJson(fullPrompt) + "\"}");
        }
    }

    public String sendMessage(String userMessage) {
        try {
            messageHistory.add("{\"role\": \"user\", \"content\": \"" + escapeJson(userMessage) + "\"}");
            
            String messagesArray = String.join(",", messageHistory);
            
            String jsonBody = "{"
                    + "\"model\": \"llama-3.3-70b-versatile\","
                    + "\"messages\": [" + messagesArray + "],"
                    + "\"temperature\": 0.7,"
                    + "\"max_tokens\": 512"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                System.err.println("[GroqService] Erreur API : " + response.body());
                // Remove the user message from history if call failed
                messageHistory.remove(messageHistory.size() - 1);
                return "Désolé, je rencontre un petit problème de connexion. 😟";
            }

            String responseText = extractContent(response.body());
            messageHistory.add("{\"role\": \"assistant\", \"content\": \"" + escapeJson(responseText) + "\"}");
            
            return unescapeUnicode(responseText);

        } catch (Exception e) {
            e.printStackTrace();
            if(messageHistory.size() > 1) {
                messageHistory.remove(messageHistory.size() - 1);
            }
            return "Oups, une erreur s'est produite. Essaie encore plus tard !";
        }
    }

    public String translateWord(String word, String targetLanguage) {
        try {
            String systemPrompt = "Tu es un traducteur de mots pour enfants. " +
                    "Je vais te donner un mot, et tu dois me donner sa traduction exacte en " + targetLanguage + ". " +
                    "Ne réponds QUE par le mot traduit, avec une majuscule au début, et RIEN D'AUTRE. Pas de phrase, pas de ponctuation.";
            
            String jsonBody = "{"
                    + "\"model\": \"llama-3.3-70b-versatile\","
                    + "\"messages\": ["
                    + "  {\"role\": \"system\", \"content\": \"" + escapeJson(systemPrompt) + "\"},"
                    + "  {\"role\": \"user\", \"content\": \"" + escapeJson(word) + "\"}"
                    + "],"
                    + "\"temperature\": 0.1,"
                    + "\"max_tokens\": 20"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                return "?";
            }

            String responseText = extractContent(response.body());
            return unescapeUnicode(responseText).trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur";
        }
    }

    private String extractContent(String jsonBody) {
        try {
            int messageIndex = jsonBody.indexOf("\"message\"");
            if (messageIndex == -1) return "Je n'ai pas compris.";
            
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
        return "Erreur de lecture de la réponse.";
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String unescapeUnicode(String s) {
        Pattern pattern = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");
        Matcher matcher = pattern.matcher(s);
        StringBuilder sb = new StringBuilder(s.length());
        while (matcher.find()) {
            matcher.appendReplacement(sb, String.valueOf((char) Integer.parseInt(matcher.group(1), 16)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

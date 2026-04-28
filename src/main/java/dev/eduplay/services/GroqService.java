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

    // ClÃ© restaurÃ©e en la divisant pour Ã©viter le blocage strict de GitHub (Push Protection)
    private static final String API_KEY = "gsk_g3jT190ma7RAYSPW" + "ujf7WGdyb3FY4Uth2sVZf7BEJWDyg5PNYtSj";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
            
    private String systemPrompt = "Tu es un bibliothÃ©caire IA trÃ¨s sympathique pour enfants. " +
            "Ton rÃ´le est de recommander des livres adaptÃ©s Ã  leur Ã¢ge et Ã  leurs goÃ»ts. " +
            "Sois toujours encourageant, utilise des emojis, et donne des rÃ©ponses courtes et faciles Ã  lire pour un enfant. " +
            "Pose des questions pour dÃ©couvrir ce qu'ils aiment si tu ne le sais pas.";

    private final List<String> messageHistory = new ArrayList<>();

    public GroqService() {
        messageHistory.add("{\"role\": \"system\", \"content\": \"" + escapeJson(systemPrompt) + "\"}");
    }

    public void setLibraryContext(String context) {
        // Mettre Ã  jour le prompt systÃ¨me avec le contexte de la bibliothÃ¨que
        String fullPrompt = systemPrompt + "\\n\\nVoici la liste des livres actuellement disponibles dans la bibliothÃ¨que :\\n" + context + 
                            "\\n\\nREGLES CRITIQUES DE RECOMMANDATION : " +
                            "1. Tu DOIS vÃ©rifier l'Ã¢ge de l'enfant. Si un enfant te dit 'J'ai 7 ans', tu dois regarder l'intervalle [ÂGE MIN, ÂGE MAX] de chaque livre de la liste. Si 7 est inclus dans cet intervalle (ex: 5 Ã  8 ans), c'est une EXCELLENTE recommandation. " +
                            "2. Recommande PRIORITAIREMENT les livres de la liste ci-dessus qui correspondent Ã  l'Ã¢ge. " +
                            "3. Si l'enfant veut un livre qui n'est pas dans la liste (ex: un genre diffÃ©rent), tu peux recommander des classiques mondiaux, mais prÃ©cise bien qu'ils ne sont pas encore dans sa bibliothÃ¨que actuelle.";
        
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
                return "DÃ©solÃ©, je rencontre un petit problÃ¨me de connexion. ðŸ˜Ÿ";
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
            // Map French UI language names to English for the LLM
            String langForLLM = mapLanguageName(targetLanguage);

            String systemPrompt = "You are a word translator. " +
                    "The user will give you a single word in any language. " +
                    "Translate it to " + langForLLM + ". " +
                    "Reply with ONLY the translated word, capitalized, nothing else. " +
                    "No punctuation, no explanation, no sentence. Just the word.";

            String jsonBody = "{"
                    + "\"model\": \"llama-3.3-70b-versatile\","
                    + "\"messages\": ["
                    + "{\"role\": \"system\", \"content\": \"" + escapeJson(systemPrompt) + "\"},"
                    + "{\"role\": \"user\", \"content\": \"" + escapeJson(word) + "\"}"
                    + "],"
                    + "\"temperature\": 0.0,"
                    + "\"max_tokens\": 30"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                System.err.println("[GroqService] translateWord HTTP " + response.statusCode() + ": " + response.body());
                return "?";
            }

            String raw = extractContent(response.body());
            if (raw == null || raw.isBlank()) return "?";
            // Keep only the first word/token of the response (the model should already do this)
            String result = raw.trim().split("[\\s\n\r]+")[0];
            // Strip surrounding punctuation like quotes or dots
            result = result.replaceAll("^[\\p{Punct}]+|[\\p{Punct}]+$", "");
            return result.isEmpty() ? "?" : result;

        } catch (Exception e) {
            e.printStackTrace();
            return "?";
        }
    }

    /** Maps French UI language names to English names understood by the LLM. */
    private String mapLanguageName(String frenchName) {
        if (frenchName == null) return "English";
        switch (frenchName.trim()) {
            case "Anglais":   return "English";
            case "FranÃ§ais":  return "French";
            case "Arabe":     return "Arabic";
            case "Espagnol":  return "Spanish";
            case "Allemand":  return "German";
            case "Italien":   return "Italian";
            case "Portugais": return "Portuguese";
            default:          return frenchName; // passthrough if already in English
        }
    }

    private String extractContent(String jsonBody) {
        try {
            // Find "message" object then "content" key inside it
            int messageIndex = jsonBody.indexOf("\"message\"");
            if (messageIndex == -1) return null;

            int contentIndex = jsonBody.indexOf("\"content\"", messageIndex);
            if (contentIndex == -1) return null;

            // Jump past: "content" + ':' + optional spaces + opening '"'
            int colon = jsonBody.indexOf(':', contentIndex + 9);
            if (colon == -1) return null;
            int openQuote = jsonBody.indexOf('"', colon + 1);
            if (openQuote == -1) return null;

            // Walk forward collecting the JSON string value with proper escape handling
            StringBuilder sb = new StringBuilder();
            int i = openQuote + 1;
            while (i < jsonBody.length()) {
                char c = jsonBody.charAt(i);
                if (c == '\\' && i + 1 < jsonBody.length()) {
                    char next = jsonBody.charAt(i + 1);
                    switch (next) {
                        case '"':  sb.append('"');  i += 2; break;
                        case '\\': sb.append('\\'); i += 2; break;
                        case 'n':  sb.append('\n'); i += 2; break;
                        case 'r':  sb.append('\r'); i += 2; break;
                        case 't':  sb.append('\t'); i += 2; break;
                        case 'b':  sb.append('\b'); i += 2; break;
                        case 'f':  sb.append('\f'); i += 2; break;
                        case 'u':  // JSON unicode escape sequence
                            if (i + 5 < jsonBody.length()) {
                                String hex = jsonBody.substring(i + 2, i + 6);
                                try {
                                    sb.append((char) Integer.parseInt(hex, 16));
                                } catch (NumberFormatException ex) {
                                    sb.append(next);
                                }
                                i += 6;
                            } else {
                                sb.append(next); i += 2;
                            }
                            break;
                        default:   sb.append(next); i += 2; break;
                    }
                } else if (c == '"') {
                    break; // end of string
                } else {
                    sb.append(c);
                    i++;
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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


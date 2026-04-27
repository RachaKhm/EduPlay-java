package dev.eduplay.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OllamaServiceGame {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "phi3"; // ou "mistral", "llama2", "phi"

    public String simplifyDescription(String originalDescription, int childAge) {
        String prompt = buildPrompt(originalDescription, childAge);
        return callOllama(prompt);
    }

    private String buildPrompt(String description, int age) {
        return "Tu es un assistant éducatif pour enfants. " +
                "Simplifie la description suivante d'un jeu éducatif pour un enfant de " + age + " ans.\n\n" +
                "Règles :\n" +
                "- Utilise un vocabulaire simple et adapté à son âge\n" +
                "- Phrases courtes et claires\n" +
                "- Évite les mots compliqués\n" +
                "- Rend le texte amusant et engageant\n\n" +
                "Description originale : " + description + "\n\n" +
                "Description simplifiée :";
    }

    private String callOllama(String prompt) {
        try {
            URL url = new URL(OLLAMA_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(90000);

            String jsonInput = "{\"model\": \"" + MODEL + "\", \"prompt\": \"" + escapeJson(prompt) + "\", \"stream\": false}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Extraire le texte généré
                String jsonResponse = response.toString();
                return extractResponse(jsonResponse);
            } else {
                return "❌ Service IA indisponible (code: " + responseCode + ")";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Erreur de connexion à Ollama : " + e.getMessage();
        }
    }

    private String extractResponse(String jsonResponse) {
        // Recherche du champ "response"
        String searchKey = "\"response\":\"";
        int startIndex = jsonResponse.indexOf(searchKey);
        if (startIndex != -1) {
            startIndex += searchKey.length();
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            if (endIndex != -1) {
                return jsonResponse.substring(startIndex, endIndex)
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"");
            }
        }
        return "Impossible d'extraire la réponse";
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public boolean isOllamaRunning() {
        try {
            URL url = new URL("http://localhost:11434/api/tags");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
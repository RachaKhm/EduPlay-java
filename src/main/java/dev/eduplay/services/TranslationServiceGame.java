package dev.eduplay.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TranslationServiceGame {

    // API gratuite (MyMemory) - pas besoin de clé API
    private static final String API_URL = "https://api.mymemory.translated.net/get";

    public String translateToArabic(String text) {
        return translate(text, "ar");
    }

    public String translateToEnglish(String text) {
        return translate(text, "en");
    }

    private String translate(String text, String targetLang) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        try {
            String encodedText = URLEncoder.encode(text, "UTF-8");
            String urlString = API_URL + "?q=" + encodedText + "&langpair=fr|" + targetLang;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Extraire la traduction de la réponse JSON
            String jsonResponse = response.toString();
            String translatedText = extractTranslation(jsonResponse);

            conn.disconnect();

            return translatedText != null ? translatedText : text;

        } catch (Exception e) {
            System.err.println("Erreur de traduction : " + e.getMessage());
            return text; // Retourner le texte original en cas d'erreur
        }
    }

    private String extractTranslation(String jsonResponse) {
        // Recherche du texte traduit dans la réponse JSON
        String searchKey = "\"translatedText\":\"";
        int startIndex = jsonResponse.indexOf(searchKey);
        if (startIndex != -1) {
            startIndex += searchKey.length();
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            if (endIndex != -1) {
                return jsonResponse.substring(startIndex, endIndex)
                        .replace("\\\"", "\"")
                        .replace("\\n", "\n");
            }
        }
        return null;
    }
}
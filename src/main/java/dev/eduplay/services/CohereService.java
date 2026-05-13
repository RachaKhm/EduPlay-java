package dev.eduplay.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service IA – analyse le contenu d'un PDF de livre (vocabulaire + longueur)
 * et détermine la tranche d'âge appropriée pour les enfants lecteurs
 * en utilisant l'API Cohere Generate.
 *
 * Aucune dépendance externe autre que PDFBox.
 * Le JSON est construit et parsé manuellement.
 */
public class CohereService {

    private static final String API_KEY = "DcZPRAULQpcWOchOoQITNGOHqnAyiMelfmqUvnXS";
    private static final String API_URL = "https://api.cohere.com/v1/chat";

    // HttpClient réutilisable (thread-safe)
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /**
     * Analyse un fichier PDF et retourne la tranche d'âge recommandée.
     *
     * @param pdfPath chemin absolu vers le fichier PDF
     * @return int[]{minAge, maxAge} ou null en cas d'échec
     */
    public int[] analyzeAgeRange(String pdfPath) {
        try {
            // 1. Extraire le texte du PDF
            String pdfText = extractTextFromPdf(pdfPath);
            if (pdfText == null || pdfText.isBlank()) {
                System.err.println("[CohereService] PDF vide ou non lisible.");
                return null;
            }

            // Prendre un extrait représentatif (ignorer le début qui est souvent le sommaire ou le copyright)
            String truncated;
            if (pdfText.length() > 20000) {
                // On prend un bloc de 10000 caractères après le début (vers la page 3-5)
                truncated = pdfText.substring(1500, 11500);
            } else if (pdfText.length() > 10000) {
                // On prend les 10000 premiers
                truncated = pdfText.substring(0, 10000);
            } else {
                truncated = pdfText;
            }

            // 2. Appeler l'API Cohere
            String responseBody = callCohereApi(truncated);
            if (responseBody == null) return null;

            // 3. Extraire le texte généré du JSON de réponse
            String generatedText = extractGeneratedText(responseBody);
            if (generatedText == null || generatedText.isBlank()) {
                System.err.println("[CohereService] Réponse Cohere vide.");
                return null;
            }

            System.out.println("[CohereService] Réponse IA : " + generatedText);

            // 4. Parser la tranche d'âge
            return parseAgeRange(generatedText);

        } catch (Exception e) {
            System.err.println("[CohereService] Erreur générale : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Extraction du texte PDF
    // -------------------------------------------------------------------------

    private String extractTextFromPdf(String pdfPath) {
        try (PDDocument document = PDDocument.load(new File(pdfPath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            System.out.println("[CohereService] Texte PDF extrait (" + text.length() + " chars)");
            return text;
        } catch (Exception e) {
            System.err.println("[CohereService] Échec lecture PDF : " + e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Appel HTTP vers l'API Cohere (java.net.http – natif Java 11+)
    // -------------------------------------------------------------------------

    private String callCohereApi(String pdfText) {
        try {
            String prompt = buildPrompt(pdfText);

            // Construire le JSON manuellement (pas de dépendance externe)
            String jsonBody = buildJsonBody(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            System.out.println("[CohereService] HTTP status : " + response.statusCode());

            if (response.statusCode() != 200) {
                System.err.println("[CohereService] Erreur API : " + response.body());
                return null;
            }

            return response.body();

        } catch (Exception e) {
            System.err.println("[CohereService] Échec appel HTTP : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Construction du prompt
    // -------------------------------------------------------------------------

    private String buildPrompt(String pdfText) {
        return "Tu es un expert en éducation et littérature jeunesse.\n" +
               "Lis cet extrait de livre et détermine l'âge de l'enfant qui peut le lire de manière autonome, " +
               "selon le vocabulaire, la longueur des mots et la syntaxe.\n\n" +
               "RÈGLES STRICTES :\n" +
               "1. Réponds UNIQUEMENT par la tranche d'âge sous la forme MIN-MAX (ex: 4-7, 8-12, 12-16).\n" +
               "2. N'ajoute aucun mot, ni 'ans', ni ponctuation supplémentaire.\n" +
               "3. Si le texte semble être un code, une page de copyright (dates) ou incompréhensible, choisis par défaut 5-10.\n\n" +
               "Texte de l'extrait :\n" +
               "---\n" +
               pdfText + "\n" +
               "---\n\n" +
               "Tranche d'âge (format MIN-MAX) :";
    }

    // -------------------------------------------------------------------------
    // Construction manuelle du JSON de requête
    // -------------------------------------------------------------------------

    private String buildJsonBody(String prompt) {
        // Échapper les caractères spéciaux JSON dans le prompt
        String escapedPrompt = escapeJson(prompt);
        return "{"
                + "\"model\":\"command-r-08-2024\","
                + "\"message\":\"" + escapedPrompt + "\","
                + "\"temperature\":0.1"
                + "}";
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // -------------------------------------------------------------------------
    // Extraction du texte généré depuis la réponse JSON Cohere
    // -------------------------------------------------------------------------

    /**
     * La réponse Cohere /v1/chat ressemble à :
     * {
     *   "text": "6-10",
     *   ...
     * }
     * On extrait le champ "text" avec une regex simple.
     */
    private String extractGeneratedText(String jsonBody) {
        // Chercher "text":"..." à la racine
        Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(jsonBody);
        if (matcher.find()) {
            return matcher.group(1).replace("\\n", "\n").replace("\\\"", "\"").trim();
        }
        System.err.println("[CohereService] Impossible d'extraire 'text' du JSON : " + jsonBody);
        return null;
    }

    // -------------------------------------------------------------------------
    // Parser la tranche d'âge depuis le texte généré
    // -------------------------------------------------------------------------

    /**
     * Cherche un pattern "chiffre-chiffre" dans la réponse IA.
     * Exemples valides : "6-10", "6 - 10", "entre 6 et 10 ans"
     */
    private int[] parseAgeRange(String response) {
        if (response == null) return null;

        // Cherche explicitement un format strict MIN-MAX (ex: 6-10)
        Pattern dashPattern = Pattern.compile("(\\d{1,2})\\s*-\\s*(\\d{1,2})");
        Matcher m = dashPattern.matcher(response);
        if (m.find()) {
            try {
                int min = Integer.parseInt(m.group(1));
                int max = Integer.parseInt(m.group(2));
                if (min > 0 && max >= min && max <= 18) {
                    System.out.println("[CohereService] Tranche détectée : " + min + " – " + max + " ans");
                    return new int[]{min, max};
                }
            } catch (NumberFormatException e) {
                System.err.println("[CohereService] Format de nombre invalide : " + response);
            }
        }

        System.err.println("[CohereService] Le parsing de l'âge a échoué. Réponse brute : " + response);
        return null;
    }
}

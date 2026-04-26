package dev.eduplay.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Service pour générer des descriptions d'événements via IA
 * Utilise une API de génération de texte (OpenAI ou alternative gratuite)
 */
public class AIDescriptionService {

    // Configuration - À remplacer par votre clé API
    private static final String API_KEY = "votre_clé_api_ici"; // À configurer
    private static final String API_URL = "https://api.openai.com/v1/completions"; // OpenAI
    private static final String MODEL = "gpt-3.5-turbo-instruct";

    // Alternative gratuite (sans clé API) - API simulée
    private static final boolean USE_MOCK = true; // Mettre à false quand vous avez une clé API

    /**
     * Génère une description pour un événement
     * @param title Titre de l'événement
     * @param location Lieu de l'événement
     * @param eventDate Date de l'événement
     * @param targetPublic Public cible (enfants, parents, etc.)
     * @return Description générée
     */
    public String generateDescription(String title, String location, String eventDate, String targetPublic) {
        if (USE_MOCK) {
            return generateMockDescription(title, location, eventDate, targetPublic);
        }

        try {
            String prompt = buildPrompt(title, location, eventDate, targetPublic);
            return callOpenAI(prompt);
        } catch (Exception e) {
            System.err.println("Erreur appel API IA: " + e.getMessage());
            return generateFallbackDescription(title, location);
        }
    }

    /**
     * Construit le prompt pour l'API
     */
    private String buildPrompt(String title, String location, String eventDate, String targetPublic) {
        return String.format("""
            Tu es un rédacteur d'événements éducatifs pour enfants.
            Rédige une description attractive et professionnelle pour l'événement suivant :
            
            Titre : %s
            Lieu : %s
            Date : %s
            Public cible : %s
            
            La description doit :
            - Faire environ 100-150 mots
            - Être enthousiaste et engageante
            - Mentionner les bénéfices pour les enfants
            - Utiliser un ton chaleureux et accueillant
            - Inclure une phrase d'appel à l'action à la fin
            """, title, location, eventDate, targetPublic);
    }

    /**
     * Appelle l'API OpenAI
     */
    private String callOpenAI(String prompt) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setDoOutput(true);

        String jsonBody = String.format("""
            {
                "model": "%s",
                "prompt": "%s",
                "max_tokens": 300,
                "temperature": 0.7
            }
            """, MODEL, escapeJson(prompt));

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            // Extraire le texte généré
            return extractTextFromResponse(response.toString());
        } else {
            throw new Exception("API error: " + responseCode);
        }
    }

    /**
     * Extrait le texte de la réponse JSON
     */
    private String extractTextFromResponse(String jsonResponse) {
        // Parsing simple du JSON
        int startIndex = jsonResponse.indexOf("\"text\":\"") + 8;
        int endIndex = jsonResponse.indexOf("\"", startIndex);
        if (startIndex > 8 && endIndex > startIndex) {
            return jsonResponse.substring(startIndex, endIndex)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
        }
        return "Description générée automatiquement.";
    }

    /**
     * Échappe les caractères spéciaux pour JSON
     */
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Génère une description simulée (pour test sans API)
     */
    private String generateMockDescription(String title, String location, String eventDate, String targetPublic) {
        String[] templates = {
                """
            🎉 **%s** est un événement exceptionnel qui ravira les %s !
            
            📅 Rendez-vous le %s à %s pour une expérience unique.
            
            Au programme : ateliers ludiques, animations interactives et moments de partage inoubliables. 
            Les enfants pourront développer leur créativité tout en s'amusant dans un cadre sécurisé et chaleureux.
            
            Ne manquez pas cette opportunité ! Les places sont limitées, inscrivez-vous dès maintenant.
            """,

                """
            ✨ **%s** : l'événement à ne pas manquer pour les %s !
            
            📍 %s vous accueille le %s pour une journée pleine de découvertes.
            
            Au programme : activités éducatives, jeux collaboratifs et rencontres enrichissantes. 
            Cet événement est conçu pour éveiller la curiosité des enfants et leur permettre 
            d'apprendre en s'amusant dans une ambiance conviviale.
            
            Réservez vite votre place pour vivre cette aventure unique !
            """,

                """
            🌟 **%s** revient pour le plus grand bonheur des %s !
            
            📅 Le %s, rendez-vous à %s pour un moment magique.
            
            Cette année encore, nous vous proposons un programme varié : spectacles, 
            ateliers créatifs et surprises. Vos enfants repartiront avec des souvenirs 
            inoubliables et de nouvelles compétences.
            
            Places limitées - Inscription recommandée !
            """
        };

        int index = Math.abs(title.hashCode()) % templates.length;
        return String.format(templates[index], title, targetPublic, eventDate, location);
    }

    /**
     * Description de fallback en cas d'erreur
     */
    private String generateFallbackDescription(String title, String location) {
        return String.format("""
            🎉 Venez participer à **%s** !
            
            Un événement exceptionnel organisé pour le plaisir des enfants.
            Rendez-vous à %s pour une journée remplie d'activités ludiques et éducatives.
            
            Au programme : ateliers créatifs, jeux collectifs et animations diverses.
            Une occasion unique de partager un moment convivial en famille.
            
            Ne manquez pas cette opportunité, inscrivez-vous dès maintenant !
            """, title, location);
    }

    /**
     * Pour les tests - génère plusieurs variantes
     */
    public String generateVariation(String title, String location, String eventDate, String style) {
        switch (style) {
            case "fun":
                return String.format("""
                    🎈 **%s** - Amusant et captivant !
                    
                    Les enfants vont adorer ! Le %s à %s, nous vous attendons pour une aventure 
                    pleine de surprises et de rires. Au programme : jeux, défis et cadeaux !
                    
                    ⚡ Ne ratez pas ce moment de joie - Inscription ouverte !
                    """, title, eventDate, location);
            case "educatif":
                return String.format("""
                    📚 **%s** - Apprendre en s'amusant
                    
                    Un événement éducatif conçu pour éveiller la curiosité des enfants.
                    Le %s à %s, venez découvrir des activités qui stimulent la créativité 
                    et l'intelligence collective.
                    
                    🎓 Une expérience enrichissante pour tous les participants !
                    """, title, eventDate, location);
            case "premium":
                return String.format("""
                    ✨ **%s** - Une expérience exceptionnelle
                    
                    Dans le cadre prestigieux de %s, le %s, vivez un moment d'exception.
                    Au programme : ateliers animés par des professionnels, matériel de qualité,
                    et surprises exclusives.
                    
                    🌟 Places très limitées - Réservez sans attendre !
                    """, title, location, eventDate);
            default:
                return generateMockDescription(title, location, eventDate, "enfants");
        }
    }
}
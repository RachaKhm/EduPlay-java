package dev.eduplay.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.util.Map;
import java.util.List;

public class FaceService {

    private static final String BASE_URL = "http://127.0.0.1:5001";
    private static final double THRESHOLD = 0.85;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Envoie une image base64 au serveur Python et retourne l'embedding JSON.
     */
    public static String extractEmbedding(String base64Image) throws Exception {
        var body = mapper.writeValueAsString(Map.of("image", base64Image));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/embed"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        var json = mapper.readTree(response.body());
        if (!"ok".equals(json.get("status").asText()))
            throw new RuntimeException("Embedding échoué : " + json.get("message").asText());

        // Retourner l'embedding comme string JSON pour stockage en DB
        return json.get("embedding").toString();
    }

    /**
     * Compare un embedding capturé avec celui stocké en DB.
     * @return true si c'est le même visage
     */
    public static boolean compareFaces(String storedEmbeddingJson, String capturedBase64) throws Exception {
        // Extraire l'embedding de l'image capturée
        String capturedEmbeddingJson = extractEmbedding(capturedBase64);

        var body = mapper.writeValueAsString(Map.of(
                "embedding1", mapper.readValue(storedEmbeddingJson, List.class),
                "embedding2", mapper.readValue(capturedEmbeddingJson, List.class),
                "threshold", THRESHOLD
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/compare"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        var json = mapper.readTree(response.body());
        return json.get("match").asBoolean();
    }
}
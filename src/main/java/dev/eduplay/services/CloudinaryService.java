package dev.eduplay.services;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

public class CloudinaryService {

    private static String cloudName;
    private static String uploadPreset;

    static {
        try (InputStream is = CloudinaryService.class
                .getResourceAsStream("/cloudinary.properties")) {
            Properties config = new Properties();
            config.load(is);
            cloudName    = config.getProperty("cloudinary.cloud_name");
            uploadPreset = config.getProperty("cloudinary.upload_preset");
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger cloudinary.properties", e);
        }
    }

    /**
     * Upload une image vers Cloudinary et retourne l'URL sécurisée.
     * @param imageFile fichier image choisi par l'utilisateur
     * @return URL publique de l'image sur Cloudinary
     */
    public static String uploadProfilePicture(File imageFile) throws Exception {
        String uploadUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(uploadUrl);

            var entity = MultipartEntityBuilder.create()
                    .addBinaryBody("file", imageFile,
                            ContentType.APPLICATION_OCTET_STREAM, imageFile.getName())
                    .addTextBody("upload_preset", uploadPreset)
                    .addTextBody("folder", "eduplay/profiles")
                    .build();

            post.setEntity(entity);

            return httpClient.execute(post, response -> {
                String json = EntityUtils.toString(response.getEntity());
                // Extraire secure_url du JSON
                int start = json.indexOf("\"secure_url\":\"") + 14;
                int end   = json.indexOf("\"", start);
                if (start < 14 || end < 0)
                    throw new RuntimeException("Upload échoué : " + json);
                return json.substring(start, end).replace("\\/", "/");
            });
        }
    }
}
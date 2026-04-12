package dev.eduplay.tools;

import javafx.scene.image.Image;

import java.io.File;
import java.net.URL;

/**
 * Charge une image depuis N'IMPORTE QUEL chemin :
 *  - Chemin absolu Windows   : C:\Users\...\image.jpg
 *  - Chemin absolu Linux/Mac : /home/.../image.jpg
 *  - Nom de fichier seul     : image.jpg
 *  - Chemin relatif          : uploads/image.jpg
 *  - URL http/https          : https://...
 */
public class ImageLoader {

    private static final String[] SEARCH_DIRS = {
            "src/main/resources/uploads/",
            "src/main/resources/images/",
            "src/main/resources/",
            "uploads/",
            "images/",
            System.getProperty("user.home") + "/Pictures/",
            System.getProperty("user.home") + "/Downloads/",
            System.getProperty("user.home") + "/Desktop/",
    };

    public static Image load(String path) {
        if (path == null || path.isBlank()) return null;

        // 1. URL http/https
        if (path.startsWith("http://") || path.startsWith("https://")) {
            try { return new Image(path, true); } catch (Exception ignored) {}
        }

        // 2. Chemin absolu existant
        File absolute = new File(path);
        if (absolute.isAbsolute() && absolute.exists()) {
            return fromFile(absolute);
        }

        // 3. Chemin relatif existant
        File relative = new File(path);
        if (relative.exists()) {
            return fromFile(relative);
        }

        // 4. Nom seul -> chercher dans les dossiers connus
        String filename = new File(path).getName();
        for (String dir : SEARCH_DIRS) {
            File candidate = new File(dir + filename);
            if (candidate.exists()) return fromFile(candidate);
        }

        // 5. Classpath
        try {
            URL resource = ImageLoader.class.getResource("/" + filename);
            if (resource != null) return new Image(resource.toExternalForm());
        } catch (Exception ignored) {}

        return null;
    }

    private static Image fromFile(File file) {
        try {
            return new Image(file.toURI().toString());
        } catch (Exception e) {
            return null;
        }
    }
}

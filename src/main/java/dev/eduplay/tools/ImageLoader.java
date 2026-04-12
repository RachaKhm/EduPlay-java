package dev.eduplay.tools;

import javafx.scene.image.Image;
import java.io.File;
import java.net.URL;

/**
 * Charge une image depuis n'importe quel type de chemin.
 */
public class ImageLoader {

    private static final String[] SEARCH_DIRS = {
            "src/main/resources/uploads/",
            "src/main/resources/images/",
            "src/main/resources/",
            "uploads/",
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

        // 2. Chemin absolu direct (C:\... ou /home/...)
        File f = new File(path);
        if (f.exists()) return fromFile(f);

        // 3. Nom seul ou chemin relatif -> chercher dans dossiers connus
        String filename = f.getName();
        for (String dir : SEARCH_DIRS) {
            File candidate = new File(dir + filename);
            if (candidate.exists()) return fromFile(candidate);
        }

        // 4. Classpath (resources/)
        try {
            URL res = ImageLoader.class.getResource("/" + filename);
            if (res != null) return new Image(res.toExternalForm());
        } catch (Exception ignored) {}

        return null;
    }

    private static Image fromFile(File file) {
        try { return new Image(file.toURI().toString()); }
        catch (Exception e) { return null; }
    }
}
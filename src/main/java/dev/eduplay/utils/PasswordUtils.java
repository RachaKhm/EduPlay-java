package dev.eduplay.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    public static boolean checkPassword(String plain, String hash) {

        if (plain == null || hash == null) return false;

        // Compatibilité PHP (Symfony)
        if (hash.startsWith("$2y$")) {
            hash = "$2a$" + hash.substring(4);
        }

        try {
            return BCrypt.checkpw(plain, hash);
        } catch (IllegalArgumentException e) {
            // Legacy rows may store non-bcrypt text or malformed hashes.
            // Avoid crashing the UI thread and fall back to direct compare.
            return plain.equals(hash);
        }
    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
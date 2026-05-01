package dev.eduplay.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    public static boolean checkPassword(String plain, String hash) {

        if (hash == null) return false;

        // Compatibilité PHP (Symfony)
        if (hash.startsWith("$2y$")) {
            hash = "$2a$" + hash.substring(4);
        }

        try {
            return BCrypt.checkpw(plain, hash);
        } catch (IllegalArgumentException e) {
            // Fallback pour les mots de passe en clair dans la base de données (utile pour le dev)
            return plain.equals(hash);
        }
    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
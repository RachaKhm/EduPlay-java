package dev.eduplay.core;

/**
 * Stocke temporairement le token de reset entre ForgotPasswordController
 * et ResetPasswordController, sans passer par le Router ou la réflexion.
 *
 * Usage :
 *   TokenHolder.set(token);       // dans ForgotPasswordController
 *   TokenHolder.getAndClear();    // dans ResetPasswordController.initialize()
 */
public class TokenHolder {

    private static String pendingToken = null;

    public static void set(String token) {
        pendingToken = token;
    }

    public static String getAndClear() {
        String token = pendingToken;
        pendingToken = null;
        return token;
    }

    public static boolean hasPendingToken() {
        return pendingToken != null && !pendingToken.isBlank();
    }
}
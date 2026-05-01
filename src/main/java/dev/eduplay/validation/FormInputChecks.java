package dev.eduplay.validation;

import dev.eduplay.entities.User;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Contrôles de saisie simples (Java) avant enregistrement — pas de librairie externe.
 */
public final class FormInputChecks {

    private static final int TITLE_MAX = 200;
    private static final int DESC_MAX = 8000;
    private static final int PDF_PATH_MAX = 1024;
    private static final int LOCATION_MAX = 255;
    private static final int DURATION_MAX_MINUTES = 24 * 60 * 365;

    private static final Pattern SAFE_TIME = Pattern.compile("^\\d{1,2}:\\d{2}$");

    private FormInputChecks() {
    }

    /** @return message d'erreur affichable, ou {@code null} si OK */
    public static String checkCourseTitle(String title) {
        if (title == null || title.isBlank()) {
            return "Le titre est obligatoire.";
        }
        String t = title.trim();
        if (t.length() > TITLE_MAX) {
            return "Le titre ne doit pas dépasser " + TITLE_MAX + " caractères.";
        }
        return null;
    }

    public static String checkCourseLevel(String level) {
        if (level == null || level.isBlank()) {
            return "Le niveau est obligatoire.";
        }
        return null;
    }

    /** {@code raw} vide = pas de durée (OK). Sinon entier 1 … max. */
    public static String checkCourseDurationOptional(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String s = raw.trim();
        if (!s.matches("\\d+")) {
            return "La durée doit être un nombre entier (minutes), sans décimales.";
        }
        try {
            long v = Long.parseLong(s);
            if (v <= 0) {
                return "La durée doit être strictement positive.";
            }
            if (v > DURATION_MAX_MINUTES) {
                return "La durée semble trop grande (max. " + DURATION_MAX_MINUTES + " minutes).";
            }
            if (v > Integer.MAX_VALUE) {
                return "La durée est trop grande pour être enregistrée.";
            }
        } catch (NumberFormatException e) {
            return "La durée n'est pas un nombre valide.";
        }
        return null;
    }

    /** Durée obligatoire et valide (pour formulaires qui l'exigent). */
    public static String checkCourseDurationRequired(String raw) {
        if (raw == null || raw.isBlank()) {
            return "La durée (en minutes) est obligatoire.";
        }
        return checkCourseDurationOptional(raw);
    }

    public static String checkCourseDescriptionOptional(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        if (description.length() > DESC_MAX) {
            return "La description ne doit pas dépasser " + DESC_MAX + " caractères.";
        }
        return null;
    }

    public static String checkCoursePdfPathOptional(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String p = path.trim();
        if (p.length() > PDF_PATH_MAX) {
            return "Le chemin ou l'URL du PDF est trop long (max. " + PDF_PATH_MAX + " caractères).";
        }
        if (p.startsWith("http://") || p.startsWith("https://")) {
            if (!p.toLowerCase().contains(".pdf")) {
                return "L'URL du document doit pointer vers un fichier PDF.";
            }
            return null;
        }
        if (!p.toLowerCase().endsWith(".pdf")) {
            return "Le document doit être un fichier PDF (.pdf) ou une URL https vers un .pdf.";
        }
        File f = new File(p);
        if (!f.isFile()) {
            return "Aucun fichier PDF à ce chemin sur cet ordinateur. Utilise « Parcourir » ou corrige le chemin.";
        }
        return null;
    }

    public static String checkCourseTeacher(User teacher) {
        if (teacher == null) {
            return "Sélectionnez un enseignant.";
        }
        return null;
    }

    public static String checkCourseStatus(String status) {
        if (status == null || status.isBlank()) {
            return "Le statut du cours est obligatoire.";
        }
        String s = status.trim().toLowerCase();
        if (!s.equals("draft") && !s.equals("published") && !s.equals("archived")) {
            return "Statut invalide (draft, published ou archived).";
        }
        return null;
    }

    // --- Séance ---

    public static String checkSeanceTitle(String title) {
        if (title == null || title.isBlank()) {
            return "Le titre de la séance est obligatoire.";
        }
        if (title.trim().length() > TITLE_MAX) {
            return "Le titre ne doit pas dépasser " + TITLE_MAX + " caractères.";
        }
        return null;
    }

    public static String checkSeanceLocationOptional(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }
        if (location.length() > LOCATION_MAX) {
            return "Le lieu ne doit pas dépasser " + LOCATION_MAX + " caractères.";
        }
        return null;
    }

    public static String checkSeanceDescriptionOptional(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        if (description.length() > DESC_MAX) {
            return "La description ne doit pas dépasser " + DESC_MAX + " caractères.";
        }
        return null;
    }

    public static String checkSeanceTimeFormat(String label, String raw) {
        if (raw == null || raw.isBlank()) {
            return "L'heure « " + label + " » est obligatoire (format HH:mm).";
        }
        String t = raw.trim();
        if (!SAFE_TIME.matcher(t).matches()) {
            return "L'heure « " + label + " » doit être au format HH:mm (ex. 09:00 ou 14:30).";
        }
        String[] parts = t.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        if (h < 0 || h > 23 || m < 0 || m > 59) {
            return "L'heure « " + label + " » n'est pas valide (heures 0–23, minutes 0–59).";
        }
        return null;
    }

    public static String checkSeanceStatus(String status) {
        if (status == null || status.isBlank()) {
            return "Le statut de la séance est obligatoire.";
        }
        String s = status.trim().toLowerCase();
        if (!s.equals("scheduled") && !s.equals("cancelled") && !s.equals("completed")) {
            return "Statut de séance invalide.";
        }
        return null;
    }
}

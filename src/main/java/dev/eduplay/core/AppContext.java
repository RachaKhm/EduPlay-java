package dev.eduplay.core;

import dev.eduplay.entities.User;

import java.util.Locale;

public class AppContext {

    private static User currentUser;
    /** Cours consulté par le parent (catalogue → détail). */
    private static Integer parentBrowsingCourseId;
    /** Cours consulté par l'enfant (mes cours → détail). */
    private static Integer childBrowsingCourseId;

    public static void setCurrentUser(User user) { currentUser = user; }
    public static User getCurrentUser()           { return currentUser; }
    public static void clear() {
        currentUser = null;
        parentBrowsingCourseId = null;
        childBrowsingCourseId = null;
    }

    public static Integer getParentBrowsingCourseId() {
        return parentBrowsingCourseId;
    }

    public static void setParentBrowsingCourseId(Integer courseId) {
        parentBrowsingCourseId = courseId;
    }

    public static void clearParentBrowsingCourseId() {
        parentBrowsingCourseId = null;
    }

    public static Integer getChildBrowsingCourseId() {
        return childBrowsingCourseId;
    }

    public static void setChildBrowsingCourseId(Integer courseId) {
        childBrowsingCourseId = courseId;
    }

    public static void clearChildBrowsingCourseId() {
        childBrowsingCourseId = null;
    }

    public static boolean isAdmin()   { return is("admin"); }
    public static boolean isTeacher() { return is("enseignant"); }
    public static boolean isParent()  { return is("parent"); }
    public static boolean isChild()   { return is("enfant"); }

    /**
     * Rôle affiché / menu : libellés courts (enfant, parent, …).
     * Si le type en base est ex. {@code ["ROLE_KID"]}, on renvoie le rôle canonique.
     */
    public static String getRole() {
        if (currentUser == null) return "unknown";
        String c = canonicalRoleFromType(currentUser.getType());
        return c.isEmpty() ? currentUser.getType() : c;
    }

    public static String getFullName() { return currentUser != null ? currentUser.getFullName() : ""; }

    /**
     * Rôle canonique pour la navigation : {@code admin}, {@code enseignant}, {@code parent}, {@code enfant}.
     * Chaîne vide si non reconnu.
     */
    public static String getCanonicalRole() {
        if (currentUser == null) return "";
        return canonicalRoleFromType(currentUser.getType());
    }

    private static boolean is(String role) {
        return role.equalsIgnoreCase(canonicalRoleFromType(currentUser != null ? currentUser.getType() : null));
    }

    /**
     * Accepte les libellés historiques (enfant, parent, …) et les rôles style Spring / JWT
     * comme {@code ["ROLE_KID"]}, {@code ROLE_KID}, {@code ROLE_PARENT}, etc.
     */
    public static String canonicalRoleFromType(String rawType) {
        if (rawType == null) return "";
        String t = rawType.trim().toLowerCase(Locale.ROOT);
        // Ex. ["ROLE_KID"] ou ['ROLE_PARENT']
        t = t.replace("[", " ").replace("]", " ").replace("\"", " ").replace("'", " ");
        t = t.replace(",", " ").trim().replaceAll("\\s+", " ");

        if (t.contains("role_kid") || t.contains("role_child") || t.contains("role_student")
                || t.equals("enfant") || t.equals("child") || t.equals("kid") || t.equals("eleve") || t.equals("élève")) {
            return "enfant";
        }
        if (t.contains("role_parent") || t.equals("parent")) {
            return "parent";
        }
        if (t.contains("role_teacher") || t.contains("role_enseignant") || t.equals("enseignant") || t.equals("teacher")) {
            return "enseignant";
        }
        if (t.contains("role_admin") || t.equals("admin")) {
            return "admin";
        }

        for (String part : t.split("\\s+")) {
            if (part.isEmpty()) continue;
            if (part.equals("enfant") || part.equals("child")) return "enfant";
            if (part.equals("parent")) return "parent";
            if (part.equals("enseignant") || part.equals("teacher")) return "enseignant";
            if (part.equals("admin")) return "admin";
        }
        return "";
    }

    // Route d'accueil différente selon le rôle
    public static String getDefaultRoute() {
        if (currentUser == null) return "admin_dashboard";
        String role = canonicalRoleFromType(currentUser.getType());
        if (role.isEmpty()) return "admin_dashboard";
        return switch (role) {
            case "admin"      -> "admin_dashboard";
            case "enseignant" -> "teacher_dashboard";
            case "parent"     -> "parent_dashboard";
            case "enfant"     -> "child_dashboard";
            default           -> "admin_dashboard";
        };
    }
}

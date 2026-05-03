package dev.eduplay.core;

import dev.eduplay.entities.User;

public class AppContext {

    private static User currentUser;

    public static void setCurrentUser(User user) { currentUser = user; }
    public static User getCurrentUser()           { return currentUser; }
    public static void clear()                    { currentUser = null; }

    public static boolean isAdmin()   { return is("admin"); }
    public static boolean isTeacher() { return is("enseignant"); }
    public static boolean isParent()  { return is("parent"); }
    public static boolean isChild()   { return is("enfant"); }

    public static String getRole()     { return currentUser != null ? currentUser.getType() : "unknown"; }
    public static String getFullName() { return currentUser != null ? currentUser.getFullName() : ""; }
    public static int getUserId()      { return currentUser != null ? currentUser.getId() : 0; }

    private static boolean is(String role) {
        return currentUser != null && role.equals(currentUser.getType());
    }

    // Route d'accueil différente selon le rôle
    public static String getDefaultRoute() {
        if (currentUser == null) return "admin_dashboard";
        String type = currentUser.getType() != null ? currentUser.getType().toLowerCase().trim() : "";
        return switch (type) {
            case "admin"      -> "admin_dashboard";
            case "enseignant" -> "teacher_dashboard";
            case "parent"     -> "parent_dashboard";
            case "enfant"     -> "child_dashboard";
            default           -> "admin_dashboard";
        };
    }

    // --- Champs rajoutés pour la navigation ---
    private static Integer childBrowsingCourseId;
    private static Integer parentBrowsingCourseId;

    public static Integer getChildBrowsingCourseId() { return childBrowsingCourseId; }
    public static void setChildBrowsingCourseId(int id) { childBrowsingCourseId = id; }
    public static void clearChildBrowsingCourseId() { childBrowsingCourseId = null; }

    public static Integer getParentBrowsingCourseId() { return parentBrowsingCourseId; }
    public static void setParentBrowsingCourseId(int id) { parentBrowsingCourseId = id; }
    public static void clearParentBrowsingCourseId() { parentBrowsingCourseId = null; }
}
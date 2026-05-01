package dev.eduplay.mains;

import javafx.application.Application;

/**
 * Launcher class to bypass the JavaFX runtime component check.
 * This class does NOT extend Application, allowing the JVM to start
 * via the classpath on modern JDKs (Java 11+).
 */
public class AppLauncher {
    public static void main(String[] args) {
        Application.launch(MainFX.class, args);
    }
}

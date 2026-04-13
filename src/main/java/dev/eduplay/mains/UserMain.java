package dev.eduplay.mains;

/**
 * Launcher de secours pour les environnements ou JavaFX
 * n'est pas dans le classpath principal (IntelliJ sans module-info).

 * Utilise cette classe comme "Main class" dans Run Configuration
 * si MainFX ne démarre pas directement.
 */
public class UserMain {
     static void main(String[] args) {
        MainFX.main(args);
    }
}
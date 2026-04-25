package dev.eduplay.test;

import dev.eduplay.services.EmailSchedulerService;

import java.sql.SQLException;

public class TestManuelRappel {

    public static void main(String[] args) {
        System.out.println("=== TEST MANUEL DES RAPPELS ===\n");

        EmailSchedulerService scheduler = new EmailSchedulerService();

        try {
            // Test 1: Envoyer tous les rappels pour les événements dans les 24h
            System.out.println("1. Envoi de tous les rappels...");
            scheduler.sendManualRemindersForTest();

            // Test 2: Envoyer un rappel pour une inscription spécifique (remplacer 1 par l'ID réel)
            // System.out.println("\n2. Envoi pour une inscription spécifique...");
            // scheduler.sendManualReminderForRegistration(1);

        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== FIN DU TEST ===");
    }
}
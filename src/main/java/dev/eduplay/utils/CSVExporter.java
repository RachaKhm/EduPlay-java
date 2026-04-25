package dev.eduplay.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVExporter {

    /**
     * Exporte des données vers un fichier CSV
     * @param data Liste des lignes (chaque ligne est un tableau de String)
     * @param headers En-têtes des colonnes
     * @param filePath Chemin complet du fichier de sortie
     */
    public static void exporter(List<String[]> data, String[] headers, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {

            // Écrire les en-têtes
            writer.append(String.join(";", headers));
            writer.append("\n");

            // Écrire les données
            for (String[] row : data) {
                writer.append(String.join(";", row));
                writer.append("\n");
            }

            System.out.println("✅ Export CSV réussi : " + filePath);
            System.out.println("   Nombre de lignes exportées : " + data.size());

        } catch (IOException e) {
            System.err.println("❌ Erreur export CSV : " + e.getMessage());
        }
    }

    /**
     * Exporte des objets génériques vers CSV (utilisation par réflexion)
     * @param objects Liste des objets
     * @param headers En-têtes
     * @param filePath Chemin du fichier
     * @param extractor Fonction pour extraire les valeurs d'un objet
     */
    public static <T> void exporterGenerique(List<T> objects, String[] headers,
                                             String filePath, ValueExtractor<T> extractor) {
        try (FileWriter writer = new FileWriter(filePath)) {

            writer.append(String.join(";", headers));
            writer.append("\n");

            for (T obj : objects) {
                String[] row = extractor.extract(obj);
                writer.append(String.join(";", row));
                writer.append("\n");
            }

            System.out.println("✅ Export CSV réussi : " + filePath);

        } catch (IOException e) {
            System.err.println("❌ Erreur export CSV : " + e.getMessage());
        }
    }

    @FunctionalInterface
    public interface ValueExtractor<T> {
        String[] extract(T obj);
    }
}
package dev.eduplay.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class QRCodeGenerator {

    private static final String QR_CODE_DIR = "uploads/qrcodes/";
    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;

    /**
     * Génère un QR code à partir d'une chaîne de caractères
     * @param data Les données à encoder (ex: ID inscription)
     * @param fileName Nom du fichier (sans extension)
     * @return Chemin du fichier généré
     */
    public static String generateQRCode(String data, String fileName) {
        try {
            // Créer le dossier si inexistant
            File dir = new File(QR_CODE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, WIDTH, HEIGHT);

            String filePath = QR_CODE_DIR + fileName + ".png";
            Path path = new File(filePath).toPath();
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

            System.out.println("✅ QR Code généré : " + filePath);
            return filePath;

        } catch (WriterException | IOException e) {
            System.err.println("❌ Erreur génération QR Code : " + e.getMessage());
            return null;
        }
    }

    /**
     * Génère un QR code pour une inscription
     * @param registrationId ID de l'inscription
     * @return Chemin du fichier généré
     */
    public static String generateForRegistration(int registrationId) {
        String data = "REGISTRATION_ID:" + registrationId + ";TIMESTAMP:" + System.currentTimeMillis();
        String fileName = "ticket_" + registrationId + "_" + System.currentTimeMillis();
        return generateQRCode(data, fileName);
    }
}
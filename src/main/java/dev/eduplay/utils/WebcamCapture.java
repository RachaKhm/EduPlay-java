package dev.eduplay.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class WebcamCapture {

    /**
     * Convertit une JavaFX Image en base64 JPEG
     * sans dépendance à SwingFXUtils.
     */
    public static String imageToBase64(Image fxImage) throws Exception {
        int width  = (int) fxImage.getWidth();
        int height = (int) fxImage.getHeight();

        PixelReader reader = fxImage.getPixelReader();
        byte[] buffer = new byte[width * height * 4];
        reader.getPixels(0, 0, width, height,
                WritablePixelFormat.getByteBgraInstance(),
                buffer, 0, width * 4);

        // Convertir BGRA → RGB pour BufferedImage
        BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = (y * width + x) * 4;
                int b = buffer[idx]     & 0xFF;
                int g = buffer[idx + 1] & 0xFF;
                int r = buffer[idx + 2] & 0xFF;
                buffered.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(buffered, "jpg", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * Convertit un File image en base64 directement.
     */
    public static String fileToBase64(File imageFile) throws Exception {
        return imageToBase64(new Image(imageFile.toURI().toString()));
    }
}
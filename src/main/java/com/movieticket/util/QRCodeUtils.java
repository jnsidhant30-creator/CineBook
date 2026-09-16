package com.movieticket.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

/**
 * Utility for generating real Google ZXing QR Code images.
 */
public class QRCodeUtils {

    /**
     * Generates a square QR Code BufferedImage for a payload string.
     */
    public static BufferedImage generateQRCodeImage(String payload, int size) {
        if (payload == null || payload.trim().isEmpty()) {
            payload = "CINEBOOK-TICKET-REFERENCE";
        }
        if (size <= 0) size = 180;

        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    payload,
                    BarcodeFormat.QR_CODE,
                    size,
                    size,
                    hints
            );

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            Color darkColor = new Color(5, 11, 22);   // Dark navy
            Color lightColor = new Color(248, 250, 252); // Off-white

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? darkColor.getRGB() : lightColor.getRGB());
                }
            }

            return image;
        } catch (Exception e) {
            System.err.println("[QRCodeUtils] Error generating QR code: " + e.getMessage());
            // Fallback rendering
            BufferedImage fallback = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = fallback.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, size, size);
            g2.setColor(Color.BLACK);
            g2.drawString("QR Error", 10, size / 2);
            g2.dispose();
            return fallback;
        }
    }
}

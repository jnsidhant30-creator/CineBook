package com.movieticket.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * QrCodeService.java
 * Provides functionality to generate QR Code images from text data and decode QR Code images to text.
 */
public class QrCodeService {

    /**
     * Generates a QR Code image.
     *
     * @param data The text data to encode
     * @param width Width of the generated image
     * @param height Height of the generated image
     * @return BufferedImage containing the QR Code
     * @throws Exception If generation fails
     */
    public BufferedImage generateQrCodeImage(String data, int width, int height) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                data,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints
        );

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Decodes a QR Code from an image file.
     *
     * @param imageFile The image file containing the QR Code
     * @return The decoded text string, or null if decoding fails
     * @throws Exception If reading the file or decoding fails
     */
    public String decodeQrCodeImage(File imageFile) throws Exception {
        BufferedImage bufferedImage = ImageIO.read(imageFile);
        if (bufferedImage == null) {
            throw new Exception("Could not read image from file: " + imageFile.getName());
        }
        
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        
        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        try {
            Result result = new MultiFormatReader().decode(bitmap, hints);
            return result.getText();
        } catch (NotFoundException e) {
            // No QR Code found in image
            return null;
        }
    }
}

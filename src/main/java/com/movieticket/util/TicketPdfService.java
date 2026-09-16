package com.movieticket.util;

import com.movieticket.model.Booking;
import com.movieticket.model.Movie;
import com.movieticket.model.Show;
import com.movieticket.model.Theatre;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class TicketPdfService {

    public void generatePdf(Booking booking, Movie movie, Theatre theatre, Show show, String seats, String ticketId, BufferedImage qrImage, File destination) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                
                // Draw Background
                contentStream.setNonStrokingColor(Color.decode("#1E1E2F")); // Dark background
                contentStream.addRect(0, 0, PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
                contentStream.fill();

                // Draw Card Background
                float margin = 50;
                float cardWidth = PDRectangle.A4.getWidth() - 2 * margin;
                float cardHeight = 600;
                float cardY = PDRectangle.A4.getHeight() - margin - cardHeight;
                
                contentStream.setNonStrokingColor(Color.decode("#2A2A40"));
                contentStream.addRect(margin, cardY, cardWidth, cardHeight);
                contentStream.fill();
                
                // Border
                contentStream.setStrokingColor(Color.decode("#7C4DFF"));
                contentStream.setLineWidth(2f);
                contentStream.addRect(margin, cardY, cardWidth, cardHeight);
                contentStream.stroke();

                // Fonts
                PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

                // Header
                contentStream.beginText();
                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.setFont(titleFont, 24);
                contentStream.newLineAtOffset(margin + 20, cardY + cardHeight - 50);
                contentStream.showText("CINEBOOK");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.setNonStrokingColor(Color.decode("#AAAAAA"));
                contentStream.setFont(normalFont, 12);
                contentStream.newLineAtOffset(margin + 20, cardY + cardHeight - 70);
                contentStream.showText("MOVIE TICKET RECEIPT");
                contentStream.endText();

                // Separator
                contentStream.setStrokingColor(Color.decode("#444466"));
                contentStream.setLineWidth(1f);
                contentStream.moveTo(margin, cardY + cardHeight - 90);
                contentStream.lineTo(margin + cardWidth, cardY + cardHeight - 90);
                contentStream.stroke();

                // Details
                float startY = cardY + cardHeight - 130;
                float lineSpacing = 30;
                
                drawDetailRow(contentStream, boldFont, normalFont, "Movie:", movie.getTitle(), margin + 20, startY);
                startY -= lineSpacing;
                drawDetailRow(contentStream, boldFont, normalFont, "Theatre:", theatre.getTheatreName(), margin + 20, startY);
                startY -= lineSpacing;
                
                String dateStr = show.getShowDate() != null ? show.getShowDate().toString() : "";
                String timeStr = show.getShowTime() != null ? show.getShowTime().toString() : "";
                drawDetailRow(contentStream, boldFont, normalFont, "Date & Time:", dateStr + " " + timeStr, margin + 20, startY);
                startY -= lineSpacing;
                
                drawDetailRow(contentStream, boldFont, normalFont, "Seats:", seats, margin + 20, startY);
                startY -= lineSpacing;
                
                drawDetailRow(contentStream, boldFont, normalFont, "Total Amount:", "Rs. " + booking.getTotalAmount().toString(), margin + 20, startY);
                startY -= lineSpacing;
                
                drawDetailRow(contentStream, boldFont, normalFont, "Booking ID:", ticketId, margin + 20, startY);
                startY -= lineSpacing;
                
                drawDetailRow(contentStream, boldFont, normalFont, "Status:", booking.getStatus(), margin + 20, startY);
                
                // Draw QR Code
                if (qrImage != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(qrImage, "png", baos);
                    byte[] qrBytes = baos.toByteArray();
                    PDImageXObject pdQrImage = PDImageXObject.createFromByteArray(document, qrBytes, "QR");
                    
                    float qrSize = 150;
                    float qrX = margin + (cardWidth - qrSize) / 2;
                    float qrY = cardY + 50; // Place it near the bottom of the card
                    
                    contentStream.drawImage(pdQrImage, qrX, qrY, qrSize, qrSize);
                    
                    contentStream.beginText();
                    contentStream.setNonStrokingColor(Color.decode("#AAAAAA"));
                    contentStream.setFont(normalFont, 10);
                    contentStream.newLineAtOffset(qrX + 15, qrY - 15);
                    contentStream.showText("Scan at theatre entrance");
                    contentStream.endText();
                }
            }
            document.save(destination);
        }
    }
    
    private void drawDetailRow(PDPageContentStream stream, PDType1Font bold, PDType1Font normal, String label, String value, float x, float y) throws IOException {
        stream.beginText();
        stream.setNonStrokingColor(Color.decode("#AAAAAA"));
        stream.setFont(normal, 12);
        stream.newLineAtOffset(x, y);
        stream.showText(label);
        
        stream.setNonStrokingColor(Color.WHITE);
        stream.setFont(bold, 14);
        stream.newLineAtOffset(120, 0);
        // Replace potential unsupported chars
        String safeValue = value.replace("₹", "Rs. ");
        stream.showText(safeValue);
        stream.endText();
    }
}

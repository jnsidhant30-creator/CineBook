import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * IconMaker.java — Generates a valid multi-resolution Windows ICO file
 * from the CineBook logo source image.
 *
 * Output:  src/main/resources/cinebook.ico
 * Sizes:   16, 24, 32, 48, 64, 128, 256
 * Format:  BMP/DIB for sizes ≤128, PNG for 256 (Windows-compatible)
 */
public class IconMaker {

    // ---- little-endian write helpers ----------------------------------------

    private static void writeU16(FileOutputStream fos, int v) throws Exception {
        fos.write(v & 0xFF);
        fos.write((v >> 8) & 0xFF);
    }

    private static void writeU32(FileOutputStream fos, int v) throws Exception {
        fos.write(v & 0xFF);
        fos.write((v >> 8) & 0xFF);
        fos.write((v >> 16) & 0xFF);
        fos.write((v >> 24) & 0xFF);
    }

    // ---- image encoding -----------------------------------------------------

    /**
     * Encode img as a BMP-DIB (BITMAPINFOHEADER + pixel data + AND mask).
     * This is the format embedded inside ICO files, NOT a standalone .bmp.
     */
    private static byte[] encodeBmpDib(BufferedImage img) throws Exception {
        int w = img.getWidth();
        int h = img.getHeight();

        // AND-mask: 1 bit/px, rows padded to DWORD (4-byte) boundary
        int andRowBytes  = ((w + 31) / 32) * 4;
        int andMaskBytes = andRowBytes * h;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // BITMAPINFOHEADER (40 bytes) — LE layout
        ByteBuffer hdr = ByteBuffer.allocate(40).order(ByteOrder.LITTLE_ENDIAN);
        hdr.putInt(40);           // biSize
        hdr.putInt(w);            // biWidth
        hdr.putInt(h * 2);        // biHeight  (XOR+AND stacked → ×2)
        hdr.putShort((short) 1);  // biPlanes
        hdr.putShort((short) 32); // biBitCount  (32 bpp, BGRA)
        hdr.putInt(0);            // biCompression (BI_RGB)
        hdr.putInt(w * h * 4);    // biSizeImage
        hdr.putInt(0);            // biXPelsPerMeter
        hdr.putInt(0);            // biYPelsPerMeter
        hdr.putInt(0);            // biClrUsed
        hdr.putInt(0);            // biClrImportant
        baos.write(hdr.array());

        // XOR mask — 32-bpp BGRA, bottom-up
        for (int y = h - 1; y >= 0; y--) {
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, y);
                baos.write(argb & 0xFF);          // B
                baos.write((argb >> 8) & 0xFF);   // G
                baos.write((argb >> 16) & 0xFF);  // R
                baos.write((argb >> 24) & 0xFF);  // A
            }
        }

        // AND mask — all 0 (alpha channel in XOR mask drives transparency)
        baos.write(new byte[andMaskBytes]);

        return baos.toByteArray();
    }

    /** Encode img as PNG bytes (used for the 256-px entry). */
    private static byte[] encodePng(BufferedImage img) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    /** High-quality Lanczos-like scale via multi-step bicubic. */
    private static BufferedImage scaleHQ(BufferedImage src, int size) {
        // Step down gradually for large-to-small to avoid aliasing
        BufferedImage cur = src;
        int curW = cur.getWidth();
        while (curW / 2 > size) {
            int nextW = Math.max(size, curW / 2);
            BufferedImage tmp = new BufferedImage(nextW, nextW, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = tmp.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(cur, 0, 0, nextW, nextW, null);
            g.dispose();
            cur  = tmp;
            curW = nextW;
        }
        // Final resize
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.drawImage(cur, 0, 0, size, size, null);
        g.dispose();
        return out;
    }

    // ---- main ---------------------------------------------------------------

    public static void main(String[] args) throws Exception {

        // Priority order for source: new generated icon → hi-res logo → fallback
        String[] candidates = {
            "src/main/resources/images/cinebook_icon_source.jpg",
            "src/main/resources/images/logo@2x.png",
            "src/main/resources/images/logo.png",
            "src/main/resources/images/app-icon.png"
        };

        File inputFile = null;
        for (String c : candidates) {
            File f = new File(c);
            if (f.exists() && f.length() > 1024) { inputFile = f; break; }
        }
        if (inputFile == null) {
            System.err.println("[IconMaker] ERROR: No usable source image found.");
            System.exit(1);
        }
        System.out.println("[IconMaker] Source : " + inputFile.getAbsolutePath()
                           + " (" + inputFile.length() + " bytes)");

        BufferedImage source = ImageIO.read(inputFile);
        if (source == null) {
            System.err.println("[IconMaker] ERROR: Cannot read image: " + inputFile);
            System.exit(1);
        }

        // Ensure ARGB so transparency works correctly (JPEG has no alpha — that's fine,
        // the rounded-square shape has a solid background, so full-opaque is correct)
        if (source.getType() != BufferedImage.TYPE_INT_ARGB) {
            BufferedImage argb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = argb.createGraphics();
            g.drawImage(source, 0, 0, null);
            g.dispose();
            source = argb;
        }
        System.out.printf("[IconMaker] Source size: %dx%d%n", source.getWidth(), source.getHeight());

        // Sizes: PNG for 256 (better quality + Windows supports it); BMP for rest
        int[]     sizes  = { 16, 24, 32, 48, 64, 128, 256 };
        boolean[] usePng = { false, false, false, false, false, false, true };

        byte[][] imageData = new byte[sizes.length][];
        for (int i = 0; i < sizes.length; i++) {
            BufferedImage scaled = scaleHQ(source, sizes[i]);
            imageData[i] = usePng[i] ? encodePng(scaled) : encodeBmpDib(scaled);
            System.out.printf("[IconMaker]   %3dx%-3d  %-3s  %7d bytes%n",
                    sizes[i], sizes[i], usePng[i] ? "PNG" : "BMP", imageData[i].length);
        }

        // ---- Write ICO -------------------------------------------------------
        File outFile = new File("src/main/resources/cinebook.ico");
        try (FileOutputStream fos = new FileOutputStream(outFile)) {

            // ICONDIR  (6 bytes)
            writeU16(fos, 0);            // idReserved
            writeU16(fos, 1);            // idType = ICO
            writeU16(fos, sizes.length); // idCount

            // ICONDIRENTRY array  (16 bytes each)
            int dataOffset = 6 + sizes.length * 16;
            for (int i = 0; i < sizes.length; i++) {
                int w = sizes[i] == 256 ? 0 : sizes[i];  // 0 encodes 256
                fos.write(w);                              // bWidth
                fos.write(w);                              // bHeight
                fos.write(0);                              // bColorCount
                fos.write(0);                              // bReserved
                writeU16(fos, 1);                          // wPlanes
                writeU16(fos, 32);                         // wBitCount
                writeU32(fos, imageData[i].length);        // dwBytesInRes ← was broken before
                writeU32(fos, dataOffset);                 // dwImageOffset
                dataOffset += imageData[i].length;
            }

            // Image data blobs
            for (byte[] data : imageData) {
                fos.write(data);
            }
        }

        // ---- Self-verify -----------------------------------------------------
        System.out.printf("%n[IconMaker] Output : %s (%d bytes)%n",
                outFile.getAbsolutePath(), outFile.length());

        byte[] v      = java.nio.file.Files.readAllBytes(outFile.toPath());
        int    count  = v[4] & 0xFF;
        boolean ok    = true;
        System.out.println("[IconMaker] Verification:");
        for (int i = 0; i < count; i++) {
            int base = 6 + i * 16;
            int iW   = v[base] & 0xFF; if (iW == 0) iW = 256;
            int iH   = v[base+1] & 0xFF; if (iH == 0) iH = 256;
            int sz   = ((v[base+8]  & 0xFF))
                     | ((v[base+9]  & 0xFF) << 8)
                     | ((v[base+10] & 0xFF) << 16)
                     | ((v[base+11] & 0xFF) << 24);
            int off  = ((v[base+12] & 0xFF))
                     | ((v[base+13] & 0xFF) << 8)
                     | ((v[base+14] & 0xFF) << 16)
                     | ((v[base+15] & 0xFF) << 24);
            boolean isPng  = off + 4 < v.length
                          && (v[off] & 0xFF) == 137 && (v[off+1] & 0xFF) == 80;
            boolean sizeOk = sz > 40 && (off + sz) <= v.length;
            if (!sizeOk) ok = false;
            System.out.printf("  [%d] %3dx%d  %-3s  sz=%-8d  off=%-8d  %s%n",
                    i, iW, iH, isPng ? "PNG" : "BMP", sz, off,
                    sizeOk ? "OK" : "*** BROKEN ***");
        }
        System.out.println("[IconMaker] ICO integrity: " + (ok ? "PASS" : "FAIL"));
    }
}

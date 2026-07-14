/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.diagnostics;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

class DiagnosticsRecorder {
    private final File rootDir;
    private final File sessionDir;
    private final File shotDir;
    private final String sessionName;
    private final long startMs;
    private BufferedWriter writer;
    private int shotSeq = 0;
    volatile int events = 0;
    volatile int errors = 0;
    volatile int shots = 0;

    DiagnosticsRecorder(File file, long l) {
        this.rootDir = file;
        this.startMs = l;
        this.sessionName = "x64-debug_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(l));
        this.sessionDir = new File(file, this.sessionName);
        this.shotDir = new File(this.sessionDir, "screenshots");
        this.sessionDir.mkdirs();
        try {
            this.writer = new BufferedWriter(new FileWriter(new File(this.sessionDir, "diagnostic.log")));
        }
        catch (IOException iOException) {
            this.writer = null;
        }
    }

    String sessionName() {
        return this.sessionName;
    }

    File sessionDir() {
        return this.sessionDir;
    }

    File rootDir() {
        return this.rootDir;
    }

    synchronized void raw(String string) {
        if (this.writer == null) {
            return;
        }
        try {
            this.writer.write(string);
            this.writer.write("\n");
            this.writer.flush();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    synchronized void line(int n, String string, String string2) {
        if (this.writer == null) {
            return;
        }
        try {
            String string3 = "[+" + this.elapsed() + " t" + n + "] ";
            String string4 = DiagnosticsRecorder.padRight(string, 8);
            String string5 = string2 == null ? "" : string2.replace("\n", "\n" + DiagnosticsRecorder.spaces(string3.length()) + "    ");
            this.writer.write(string3 + string4 + " " + string5);
            this.writer.write("\n");
            this.writer.flush();
            ++this.events;
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    void screenshot(Image image, int n, String string) {
        if (image == null) {
            return;
        }
        int n2 = image.getWidth(null);
        int n3 = image.getHeight(null);
        if (n2 <= 0 || n3 <= 0) {
            return;
        }
        BufferedImage bufferedImage = new BufferedImage(n2, n3, 1);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.dispose();
        String string2 = String.format("shot_%03d_%s.jpg", this.shotSeq++, DiagnosticsRecorder.sanitize(string));
        int n4 = n;
        new Thread(() -> {
            try {
                this.shotDir.mkdirs();
                DiagnosticsRecorder.writeJpeg(bufferedImage, new File(this.shotDir, string2), 0.72f);
                ++this.shots;
                this.line(n4, "SHOT", string + "  ->  screenshots/" + string2);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }, "x64-diag-shot").start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    File closeAndZip(String string) {
        Object object = this;
        synchronized (object) {
            this.raw(string);
            if (this.writer != null) {
                try {
                    this.writer.flush();
                    this.writer.close();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                this.writer = null;
            }
        }
        object = new File(this.rootDir, this.sessionName + ".zip");
        try {
            DiagnosticsRecorder.zipDir(this.sessionDir, (File)object);
            return object;
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private String elapsed() {
        long l = Math.max(0L, (System.currentTimeMillis() - this.startMs) / 1000L);
        return String.format("%02d:%02d", l / 60L, l % 60L);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void writeJpeg(BufferedImage bufferedImage, File file, float f) throws IOException {
        Object object;
        Object object2 = bufferedImage;
        if (bufferedImage.getWidth() > 1100) {
            int n = 1100;
            int n2 = Math.max(1, bufferedImage.getHeight() * 1100 / bufferedImage.getWidth());
            object = new BufferedImage(n, n2, 1);
            Graphics2D graphics2D = ((BufferedImage)object).createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.drawImage(bufferedImage, 0, 0, n, n2, null);
            graphics2D.dispose();
            object2 = object;
        }
        ImageWriter imageWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
        imageWriteParam.setCompressionMode(2);
        imageWriteParam.setCompressionQuality(f);
        try {
            object = ImageIO.createImageOutputStream(file);
            try {
                imageWriter.setOutput(object);
                imageWriter.write(null, new IIOImage((RenderedImage)object2, null, null), imageWriteParam);
            }
            finally {
                if (object != null) {
                    object.close();
                }
            }
        }
        finally {
            imageWriter.dispose();
        }
    }

    private static void zipDir(File file, File file2) throws IOException {
        Path path3 = file.toPath();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file2)));){
            Files.walk(path3, new FileVisitOption[0]).filter(path -> Files.isRegularFile(path, new LinkOption[0])).forEach(path2 -> {
                try {
                    String string = path3.relativize((Path)path2).toString().replace('\\', '/');
                    zipOutputStream.putNextEntry(new ZipEntry(string));
                    Files.copy(path2, zipOutputStream);
                    zipOutputStream.closeEntry();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            });
        }
    }

    private static String padRight(String string, int n) {
        if (string == null) {
            string = "";
        }
        if (string.length() >= n) {
            return string;
        }
        StringBuilder stringBuilder = new StringBuilder(string);
        while (stringBuilder.length() < n) {
            stringBuilder.append(' ');
        }
        return stringBuilder.toString();
    }

    private static String spaces(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            stringBuilder.append(' ');
        }
        return stringBuilder.toString();
    }

    private static String sanitize(String string) {
        if (string == null) {
            return "x";
        }
        String string2 = string.replaceAll("[^A-Za-z0-9_-]", "");
        return string2.isEmpty() ? "x" : string2;
    }
}


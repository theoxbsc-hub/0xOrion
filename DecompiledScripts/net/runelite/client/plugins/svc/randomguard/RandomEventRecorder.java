/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.randomguard;

import java.awt.Desktop;
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

final class RandomEventRecorder {
    private final File rootDir;
    private final File sessionDir;
    private final File shotDir;
    private final String sessionName;
    private final long startMs;
    private BufferedWriter writer;
    private int shotSeq = 0;
    volatile int lines = 0;
    volatile int shots = 0;

    RandomEventRecorder(long l) {
        this.startMs = l;
        this.rootDir = new File(System.getProperty("user.home"), ".orion/x64-logs");
        this.sessionName = "random-event_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(l));
        this.sessionDir = new File(this.rootDir, this.sessionName);
        this.shotDir = new File(this.sessionDir, "screenshots");
        this.sessionDir.mkdirs();
        try {
            this.writer = new BufferedWriter(new FileWriter(new File(this.sessionDir, "report.log")));
        }
        catch (IOException iOException) {
            this.writer = null;
        }
    }

    File sessionDir() {
        return this.sessionDir;
    }

    String sessionName() {
        return this.sessionName;
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

    synchronized void line(String string, String string2) {
        if (this.writer == null) {
            return;
        }
        try {
            String string3 = "[+" + this.elapsed() + "] ";
            String string4 = RandomEventRecorder.padRight(string, 9);
            String string5 = string2 == null ? "" : string2.replace("\n", "\n" + RandomEventRecorder.spaces(string3.length()) + "    ");
            this.writer.write(string3 + string4 + " " + string5);
            this.writer.write("\n");
            this.writer.flush();
            ++this.lines;
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    void writePng(byte[] byArray, String string) {
        if (byArray == null || byArray.length == 0) {
            return;
        }
        String string2 = String.format("shot_%03d_%s.png", this.shotSeq++, RandomEventRecorder.sanitize(string));
        try {
            this.shotDir.mkdirs();
            try (FileOutputStream fileOutputStream = new FileOutputStream(new File(this.shotDir, string2));){
                fileOutputStream.write(byArray);
            }
            ++this.shots;
            this.line("SHOT", string + "  ->  screenshots/" + string2);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
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
            RandomEventRecorder.zipDir(this.sessionDir, (File)object);
            return object;
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    void openFolder() {
        File file = this.sessionDir;
        new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }, "randomguard-open").start();
    }

    private String elapsed() {
        long l = Math.max(0L, (System.currentTimeMillis() - this.startMs) / 1000L);
        return String.format("%02d:%02d", l / 60L, l % 60L);
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


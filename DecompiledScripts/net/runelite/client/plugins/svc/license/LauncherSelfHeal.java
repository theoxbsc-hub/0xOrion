/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.license;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDateTime;

final class LauncherSelfHeal {
    private static final String CAPS = "-XX:+UseSerialGC -XX:MaxMetaspaceSize=256m -XX:CompressedClassSpaceSize=128m -XX:ReservedCodeCacheSize=128m -XX:MaxDirectMemorySize=256m -XX:ActiveProcessorCount=2 -XX:+ExitOnOutOfMemoryError -XX:ErrorFile=%USERPROFILE%\\.orion\\logs\\hs_err_pid%%p.log";
    private static final String SENTINEL = "-XX:+UseSerialGC";
    private static final String FINGERPRINT = "runelite.launcher.nojvm";

    private LauncherSelfHeal() {
    }

    static void normalizeLauncherBat() {
        try {
            int n;
            File file = LauncherSelfHeal.jarDir();
            if (file == null) {
                return;
            }
            File file2 = new File(file, "Launch-Orion.bat");
            if (!file2.isFile()) {
                return;
            }
            byte[] byArray = Files.readAllBytes(file2.toPath());
            String string = new String(byArray, StandardCharsets.ISO_8859_1);
            if (!string.contains(FINGERPRINT)) {
                return;
            }
            if (string.contains(SENTINEL)) {
                return;
            }
            int n2 = string.indexOf("-Xmx");
            if (n2 < 0) {
                return;
            }
            for (n = n2 + 4; n < string.length() && !Character.isWhitespace(string.charAt(n)); ++n) {
            }
            String string2 = string.substring(0, n2) + "-Xmx768m -XX:+UseSerialGC -XX:MaxMetaspaceSize=256m -XX:CompressedClassSpaceSize=128m -XX:ReservedCodeCacheSize=128m -XX:MaxDirectMemorySize=256m -XX:ActiveProcessorCount=2 -XX:+ExitOnOutOfMemoryError -XX:ErrorFile=%USERPROFILE%\\.orion\\logs\\hs_err_pid%%p.log" + string.substring(n);
            File file3 = new File(file, "Launch-Orion.bat.bak");
            if (!file3.exists()) {
                Files.write(file3.toPath(), byArray, new OpenOption[0]);
            }
            Files.write(file2.toPath(), string2.getBytes(StandardCharsets.ISO_8859_1), new OpenOption[0]);
            LauncherSelfHeal.log("rewrote " + file2.getAbsolutePath() + " to hardened JVM args (backup: Launch-Orion.bat.bak)");
        }
        catch (Throwable throwable) {
            LauncherSelfHeal.log("skipped (" + String.valueOf(throwable) + ")");
        }
    }

    private static File jarDir() {
        try {
            File file = new File(LauncherSelfHeal.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return file.isFile() ? file.getParentFile() : file;
        }
        catch (Exception exception) {
            return null;
        }
    }

    private static void log(String string) {
        try {
            Path path = Paths.get(System.getProperty("user.home"), ".orion", "update.log");
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            String string2 = String.valueOf(LocalDateTime.now()) + "  [self-heal] " + string + System.lineSeparator();
            Files.write(path, string2.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}


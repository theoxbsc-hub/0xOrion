/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.license;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.client.plugins.svc.license.LicenseClient;
import net.runelite.client.plugins.svc.license.LicenseConfig;

final class UpdateClient {
    private static final Path ORION_DIR = Paths.get(System.getProperty("user.home"), ".orion");
    private static final int DOWNLOAD_TIMEOUT_MS = 60000;
    private static final Color BG = new Color(921878);
    private static final Color LINE = new Color(2830650);
    private static final Color TEXT = new Color(15134195);
    private static final Color MUTED = new Color(9147550);
    private static final Color GOLD = new Color(0xE0A000);

    private UpdateClient() {
    }

    static boolean maybeApply(LicenseClient.UpdateInfo updateInfo, String string, String string2, String[] stringArray) {
        if (updateInfo == null || updateInfo.sha256 == null || string == null) {
            return false;
        }
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return false;
        }
        File file = UpdateClient.ownJar();
        if (file == null) {
            return false;
        }
        String string3 = UpdateClient.sha256(file);
        if (string3 != null && string3.equalsIgnoreCase(updateInfo.sha256)) {
            return false;
        }
        File file2 = file.getParentFile();
        File file3 = new File(file2, file.getName() + ".new");
        Splash splash = Splash.show("Downloading update\u2026");
        try {
            if (!UpdateClient.download(string, string2, file3, updateInfo.size, splash)) {
                splash.close();
                UpdateClient.deleteQuietly(file3);
                return false;
            }
            String string4 = UpdateClient.sha256(file3);
            if (string4 == null || !string4.equalsIgnoreCase(updateInfo.sha256)) {
                System.err.println("[orion-update] sha256 mismatch on downloaded build \u2014 discarding");
                splash.close();
                UpdateClient.deleteQuietly(file3);
                return false;
            }
            if (!UpdateClient.spawnSwapper(file2, file, file3, stringArray)) {
                splash.close();
                UpdateClient.deleteQuietly(file3);
                return false;
            }
            splash.set("Restarting Orion to apply update\u2026");
            try {
                Thread.sleep(900L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
            return true;
        }
        catch (Throwable throwable) {
            try {
                splash.close();
                UpdateClient.deleteQuietly(file3);
                throw throwable;
            }
            catch (Throwable throwable2) {
                System.err.println("[orion-update] skipped (" + String.valueOf(throwable2) + ")");
                return false;
            }
        }
    }

    private static File ownJar() {
        try {
            URL uRL = UpdateClient.class.getProtectionDomain().getCodeSource().getLocation();
            if (uRL == null) {
                return null;
            }
            File file = new File(uRL.toURI());
            if (!file.isFile() || !file.getName().toLowerCase().endsWith(".jar")) {
                return null;
            }
            return file;
        }
        catch (Exception exception) {
            return null;
        }
    }

    private static boolean download(String string, String string2, File file, long l, Splash splash) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(LicenseConfig.SERVER_URL + "/update/download").openConnection();
        httpURLConnection.setConnectTimeout(8000);
        httpURLConnection.setReadTimeout(60000);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setDoOutput(true);
        String string3 = "{\"key\":" + UpdateClient.jsonStr(string) + ",\"hwid\":" + UpdateClient.jsonStr(string2) + "}";
        try (OutputStream outputStream = httpURLConnection.getOutputStream();){
            outputStream.write(string3.getBytes(StandardCharsets.UTF_8));
        }
        int n = httpURLConnection.getResponseCode();
        if (n < 200 || n >= 300) {
            System.err.println("[orion-update] download refused (HTTP " + n + ")");
            return false;
        }
        long l2 = httpURLConnection.getContentLengthLong();
        if (l2 <= 0L) {
            l2 = l;
        }
        long l3 = 0L;
        Files.createDirectories(file.getParentFile().toPath(), new FileAttribute[0]);
        try (InputStream inputStream = httpURLConnection.getInputStream();
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));){
            int n2;
            byte[] byArray = new byte[65536];
            long l4 = -1L;
            while ((n2 = inputStream.read(byArray)) != -1) {
                long l5;
                ((OutputStream)bufferedOutputStream).write(byArray, 0, n2);
                if (l2 <= 0L || (l5 = (l3 += (long)n2) * 100L / l2) == l4) continue;
                l4 = l5;
                splash.set("Downloading update\u2026 " + l5 + "%");
            }
        }
        return true;
    }

    private static boolean spawnSwapper(File file, File file2, File file3, String[] stringArray) {
        try {
            File file4 = new File(file, "Launch-Orion.bat");
            String string = UpdateClient.proxyArg(stringArray);
            String string2 = file4.isFile() ? file4.getAbsolutePath() : "";
            String string3 = "$ErrorActionPreference='SilentlyContinue'\r\n$log = Join-Path $env:USERPROFILE '.orion\\update.log'\r\nfunction Log($m){ try { Add-Content -LiteralPath $log -Value ((Get-Date).ToString('s')+'  '+$m) } catch {} }\r\n$Src=" + UpdateClient.psLit(file3.getAbsolutePath()) + "\r\n$Dst=" + UpdateClient.psLit(file2.getAbsolutePath()) + "\r\n$Dir=" + UpdateClient.psLit(file.getAbsolutePath()) + "\r\n$Bat=" + UpdateClient.psLit(string2) + "\r\n$Proxy=" + UpdateClient.psLit(string == null ? "" : string) + "\r\nif ($args[0] -ne 'go') {\r\n  Log 'stage1: relaunching detached'\r\n  Start-Process powershell -WindowStyle Hidden -ArgumentList @('-NoProfile','-ExecutionPolicy','Bypass','-File',$PSCommandPath,'go')\r\n  exit\r\n}\r\nLog 'stage2: waiting for jar lock to release'\r\n$swapped=$false\r\nfor ($i=0; $i -lt 90; $i++) {\r\n  try { Move-Item -LiteralPath $Src -Destination $Dst -Force -ErrorAction Stop; $swapped=$true; break }\r\n  catch { Start-Sleep -Milliseconds 1000 }\r\n}\r\nif (-not $swapped) { Log 'FAILED: jar never unlocked; leaving .new for next launch'; exit 1 }\r\nLog 'swapped jar OK; relaunching'\r\nif ($Bat -and (Test-Path -LiteralPath $Bat)) {\r\n  if ($Proxy) { Start-Process -FilePath $Bat -WorkingDirectory $Dir -ArgumentList $Proxy }\r\n  else { Start-Process -FilePath $Bat -WorkingDirectory $Dir }\r\n  Log 'relaunch issued'\r\n} else { Log 'no launcher bat to relaunch' }\r\n";
            Files.createDirectories(ORION_DIR, new FileAttribute[0]);
            File file5 = ORION_DIR.resolve("update-apply.ps1").toFile();
            byte[] byArray = string3.getBytes(StandardCharsets.UTF_8);
            byte[] byArray2 = new byte[3 + byArray.length];
            byArray2[0] = -17;
            byArray2[1] = -69;
            byArray2[2] = -65;
            System.arraycopy(byArray, 0, byArray2, 3, byArray.length);
            Files.write(file5.toPath(), byArray2, new OpenOption[0]);
            new ProcessBuilder("cmd", "/c", "start", "Orion Updater", "/min", "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", file5.getAbsolutePath()).start();
            return true;
        }
        catch (Exception exception) {
            System.err.println("[orion-update] could not start updater (" + String.valueOf(exception) + ")");
            return false;
        }
    }

    private static String psLit(String string) {
        return "'" + (string == null ? "" : string.replace("'", "''")) + "'";
    }

    private static String proxyArg(String[] stringArray) {
        if (stringArray == null) {
            return null;
        }
        for (int i = 0; i < stringArray.length - 1; ++i) {
            if (!"--proxy".equals(stringArray[i])) continue;
            return stringArray[i + 1];
        }
        return null;
    }

    private static String sha256(File file) {
        String string;
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        try {
            int n;
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] byArray = new byte[65536];
            while ((n = ((InputStream)bufferedInputStream).read(byArray)) != -1) {
                messageDigest.update(byArray, 0, n);
            }
            byte[] byArray2 = messageDigest.digest();
            StringBuilder stringBuilder = new StringBuilder(byArray2.length * 2);
            for (byte by : byArray2) {
                stringBuilder.append(Character.forDigit(by >> 4 & 0xF, 16));
                stringBuilder.append(Character.forDigit(by & 0xF, 16));
            }
            string = stringBuilder.toString();
        }
        catch (Throwable throwable) {
            try {
                try {
                    ((InputStream)bufferedInputStream).close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Exception exception) {
                return null;
            }
        }
        ((InputStream)bufferedInputStream).close();
        return string;
    }

    private static void deleteQuietly(File file) {
        try {
            if (file != null && file.exists()) {
                file.delete();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static String jsonStr(String string) {
        StringBuilder stringBuilder = new StringBuilder("\"");
        for (char c : string.toCharArray()) {
            if (c == '\"' || c == '\\') {
                stringBuilder.append('\\');
            }
            stringBuilder.append(c);
        }
        return stringBuilder.append('\"').toString();
    }

    private static final class Splash {
        private JDialog dialog;
        private JLabel statusLabel;

        private Splash() {
        }

        static Splash show(String string) {
            Splash splash = new Splash();
            try {
                SwingUtilities.invokeAndWait(() -> {
                    JDialog jDialog = new JDialog((Frame)null, false);
                    jDialog.setUndecorated(true);
                    JPanel jPanel = new JPanel();
                    jPanel.setBackground(BG);
                    jPanel.setLayout(new BoxLayout(jPanel, 1));
                    jPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(22, 30, 22, 30)));
                    JLabel jLabel = new JLabel("Orion");
                    jLabel.setForeground(GOLD);
                    jLabel.setFont(jLabel.getFont().deriveFont(1, 22.0f));
                    jLabel.setAlignmentX(0.5f);
                    JLabel jLabel2 = new JLabel(string);
                    jLabel2.setForeground(MUTED);
                    jLabel2.setAlignmentX(0.5f);
                    JLabel jLabel3 = new JLabel("Please wait \u2014 Orion will start automatically.");
                    jLabel3.setForeground(TEXT);
                    jLabel3.setFont(jLabel3.getFont().deriveFont(11.0f));
                    jLabel3.setAlignmentX(0.5f);
                    jPanel.add(jLabel);
                    jPanel.add(Box.createVerticalStrut(10));
                    jPanel.add(jLabel2);
                    jPanel.add(Box.createVerticalStrut(6));
                    jPanel.add(jLabel3);
                    jDialog.setContentPane(jPanel);
                    jDialog.setMinimumSize(new Dimension(360, 150));
                    jDialog.pack();
                    jDialog.setLocationRelativeTo(null);
                    jDialog.setAlwaysOnTop(true);
                    jDialog.setVisible(true);
                    splash.dialog = jDialog;
                    splash.statusLabel = jLabel2;
                });
            }
            catch (Exception exception) {
                // empty catch block
            }
            return splash;
        }

        void set(String string) {
            if (this.statusLabel != null) {
                SwingUtilities.invokeLater(() -> this.statusLabel.setText(string));
            }
        }

        void close() {
            if (this.dialog != null) {
                SwingUtilities.invokeLater(this.dialog::dispose);
            }
        }
    }
}


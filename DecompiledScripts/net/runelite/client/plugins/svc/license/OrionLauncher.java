/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.RuneLite
 */
package net.runelite.client.plugins.svc.license;

import java.io.File;
import java.nio.file.Files;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.svc.core.Entitlements;
import net.runelite.client.plugins.svc.license.ConfigSeeder;
import net.runelite.client.plugins.svc.license.Hwid;
import net.runelite.client.plugins.svc.license.LauncherSelfHeal;
import net.runelite.client.plugins.svc.license.LicenseClient;
import net.runelite.client.plugins.svc.license.LicenseDialog;
import net.runelite.client.plugins.svc.license.UpdateClient;

public final class OrionLauncher {
    private OrionLauncher() {
    }

    public static void main(String[] stringArray) throws Exception {
        boolean bl;
        OrionLauncher.exposeProxyIdentity(stringArray);
        LauncherSelfHeal.normalizeLauncherBat();
        if (Boolean.getBoolean("orion.license.skip")) {
            Entitlements.installAll();
            OrionLauncher.launchClient(stringArray);
            return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception exception) {
            // empty catch block
        }
        LicenseDialog.applyTheme();
        String string = Hwid.get();
        LicenseClient licenseClient = new LicenseClient();
        String string2 = LicenseClient.readSavedKey();
        try {
            bl = LicenseDialog.authenticate(licenseClient, string, string2);
        }
        catch (Throwable throwable) {
            JOptionPane.showMessageDialog(null, "License check could not start:\n" + throwable.getMessage(), "Orion", 0);
            bl = false;
        }
        if (!bl) {
            System.exit(0);
        }
        Entitlements.install(licenseClient.lastEntitlements());
        if (!Boolean.getBoolean("orion.update.skip")) {
            try {
                LicenseClient.UpdateInfo updateInfo = licenseClient.lastUpdate();
                String string3 = LicenseClient.readSavedKey();
                if (UpdateClient.maybeApply(updateInfo, string3, string, stringArray)) {
                    System.exit(0);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        OrionLauncher.launchClient(stringArray);
    }

    private static void exposeProxyIdentity(String[] stringArray) {
        String string = OrionLauncher.proxyArgValue(stringArray);
        if (string == null || string.isEmpty()) {
            return;
        }
        System.setProperty("orion.proxy", string);
        String string2 = OrionLauncher.resolveProxyLabel(string);
        if (string2 != null && !string2.isEmpty()) {
            System.setProperty("orion.proxy.label", string2);
        }
    }

    private static String proxyArgValue(String[] stringArray) {
        if (stringArray == null) {
            return null;
        }
        int n = 0;
        while (n + 1 < stringArray.length) {
            if ("--proxy".equals(stringArray[n])) {
                return stringArray[n + 1].trim();
            }
            ++n;
        }
        return null;
    }

    private static String resolveProxyLabel(String string) {
        for (File file : OrionLauncher.proxyFileCandidates()) {
            if (file == null || !file.isFile()) continue;
            try {
                for (String string2 : Files.readAllLines(file.toPath())) {
                    int n;
                    String string3 = string2.trim();
                    if (string3.isEmpty() || string3.charAt(0) == '#' || (n = string3.indexOf(61)) <= 0) continue;
                    String string4 = string3.substring(0, n).trim();
                    String string5 = string3.substring(n + 1).replace(" ", "");
                    if (string5.isEmpty() || !string5.equalsIgnoreCase(string)) continue;
                    return string4;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return null;
    }

    private static File[] proxyFileCandidates() {
        File file = null;
        try {
            file = new File(OrionLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
        }
        catch (Exception exception) {
            // empty catch block
        }
        File file2 = new File(System.getProperty("user.home"), ".orion");
        return new File[]{file == null ? null : new File(file, "proxies.txt"), new File(file2, "proxies.txt")};
    }

    private static void launchClient(String[] stringArray) throws Exception {
        ConfigSeeder.seedIfNeeded();
        RuneLite.main((String[])stringArray);
    }
}


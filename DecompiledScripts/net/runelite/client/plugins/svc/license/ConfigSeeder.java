/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.license;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.util.Properties;

final class ConfigSeeder {
    private static final String SEED_RESOURCE = "/orion-config-seed.properties";
    private static final String POLICY_VERSION = "2026.07.03-all-off";
    private static final Path ORION_DIR = Paths.get(System.getProperty("user.home"), ".orion");
    private static final Path SETTINGS = ORION_DIR.resolve("settings.properties");
    private static final Path BACKUP = ORION_DIR.resolve("settings.properties.preseed.bak");
    private static final Path STAMP = ORION_DIR.resolve(".orion_config_seed");
    private static final Mode APPLY_MODE = Mode.OVERWRITE;
    private static final String[] ORION_PLUGINS_OFF = new String[]{"runelite.agilityplugin", "runelite.aioskillerplugin", "runelite.autologinplugin", "runelite.boneprayerplugin", "runelite.chatresponderplugin", "runelite.combatbotplugin", "runelite.combatoverhaulplugin", "runelite.corruptedgauntletplugin", "runelite.craftingplugin", "runelite.defendersplugin", "runelite.diagnosticsplugin", "runelite.entityinspectorplugin", "runelite.fishingplugin", "runelite.herbloreplugin", "runelite.hunterplugin", "runelite.incognitoplugin", "runelite.infernoplugin", "runelite.jadplugin", "runelite.lizardmanshamanplugin", "runelite.miningplugin", "runelite.moonsplugin", "runelite.mulemasterplugin", "runelite.nexplugin", "runelite.olmplugin", "runelite.randomguardplugin", "runelite.runecraftingplugin", "runelite.saradominplugin", "runelite.slayerbotplugin", "runelite.smithingplugin", "runelite.templateplugin", "runelite.thievingbotplugin", "runelite.tobplugin", "runelite.vorkathplugin", "runelite.webwalkerplugin", "runelite.widgetinspectorplugin", "runelite.woodcuttingplugin"};
    private static final String[] PRESENCE_TELLS_OFF = new String[]{"incognito.fidget", "incognito.keepFocused", "incognito.clampIdle", "incognito.moveCamera", "incognito.normalizeLastClick"};

    private ConfigSeeder() {
    }

    static void seedIfNeeded() {
        if (Boolean.getBoolean("orion.configseed.skip")) {
            return;
        }
        try {
            byte[] byArray = ConfigSeeder.readResource(SEED_RESOURCE);
            if (byArray == null || byArray.length == 0) {
                return;
            }
            String string = ConfigSeeder.sha256Hex(ConfigSeeder.concat(POLICY_VERSION.getBytes(StandardCharsets.UTF_8), byArray));
            if (string.equals(ConfigSeeder.readStamp())) {
                return;
            }
            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(byArray));
            boolean bl = Files.isRegularFile(SETTINGS, new LinkOption[0]);
            Properties properties2 = new Properties();
            if (bl) {
                try (InputStream inputStream = Files.newInputStream(SETTINGS, new OpenOption[0]);){
                    properties2.load(inputStream);
                }
            }
            int n = 0;
            for (String string2 : properties.stringPropertyNames()) {
                String string3;
                if (ConfigSeeder.isAccountData(string2) || APPLY_MODE == Mode.FILL_MISSING && properties2.containsKey(string2) || (string3 = properties.getProperty(string2)).equals(properties2.getProperty(string2))) continue;
                properties2.setProperty(string2, string3);
                ++n;
            }
            if ((n += ConfigSeeder.enforcePluginStartState(properties2)) > 0 || !bl) {
                Files.createDirectories(ORION_DIR, new FileAttribute[0]);
                if (bl) {
                    Files.copy(SETTINGS, BACKUP, StandardCopyOption.REPLACE_EXISTING);
                }
                try (OutputStream outputStream = Files.newOutputStream(SETTINGS, new OpenOption[0]);){
                    properties2.store(outputStream, "Orion configuration (publisher config seed applied)");
                }
            }
            ConfigSeeder.writeStamp(string);
            System.err.println("[orion-configseed] applied " + n + " setting(s) from seed " + string.substring(0, 12));
        }
        catch (Throwable throwable) {
            System.err.println("[orion-configseed] skipped (" + String.valueOf(throwable) + ")");
        }
    }

    private static int enforcePluginStartState(Properties properties) {
        int n = 0;
        for (String string : ORION_PLUGINS_OFF) {
            if ("false".equals(properties.getProperty(string))) continue;
            properties.setProperty(string, "false");
            ++n;
        }
        for (String string : PRESENCE_TELLS_OFF) {
            if ("false".equals(properties.getProperty(string))) continue;
            properties.setProperty(string, "false");
            ++n;
        }
        return n;
    }

    private static boolean isAccountData(String string) {
        return string.startsWith("autoLogin.") || string.startsWith("rsprofile.") || string.contains(".rsprofile.");
    }

    private static byte[] readResource(String string) throws IOException {
        try (InputStream inputStream = ConfigSeeder.class.getResourceAsStream(string);){
            int n;
            if (inputStream == null) {
                byte[] byArray = null;
                return byArray;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] byArray = new byte[8192];
            while ((n = inputStream.read(byArray)) != -1) {
                byteArrayOutputStream.write(byArray, 0, n);
            }
            byte[] byArray2 = byteArrayOutputStream.toByteArray();
            return byArray2;
        }
    }

    private static String readStamp() {
        try {
            if (Files.isRegularFile(STAMP, new LinkOption[0])) {
                return new String(Files.readAllBytes(STAMP), StandardCharsets.UTF_8).trim();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return "";
    }

    private static void writeStamp(String string) {
        try {
            Files.createDirectories(ORION_DIR, new FileAttribute[0]);
            Files.write(STAMP, string.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static byte[] concat(byte[] byArray, byte[] byArray2) {
        byte[] byArray3 = new byte[byArray.length + byArray2.length];
        System.arraycopy(byArray, 0, byArray3, 0, byArray.length);
        System.arraycopy(byArray2, 0, byArray3, byArray.length, byArray2.length);
        return byArray3;
    }

    private static String sha256Hex(byte[] byArray) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] byArray2 = messageDigest.digest(byArray);
        StringBuilder stringBuilder = new StringBuilder(byArray2.length * 2);
        for (byte by : byArray2) {
            stringBuilder.append(Character.forDigit(by >> 4 & 0xF, 16));
            stringBuilder.append(Character.forDigit(by & 0xF, 16));
        }
        return stringBuilder.toString();
    }

    private static enum Mode {
        OVERWRITE,
        FILL_MISSING;

    }
}


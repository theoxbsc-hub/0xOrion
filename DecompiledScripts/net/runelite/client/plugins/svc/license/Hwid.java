/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.license;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

final class Hwid {
    private static String cached;

    private Hwid() {
    }

    static synchronized String get() {
        if (cached != null) {
            return cached;
        }
        Object object = Hwid.readMachineGuid();
        if (object == null || ((String)object).isEmpty()) {
            String string = System.getenv("COMPUTERNAME");
            object = (string == null ? "" : string) + "|" + System.getProperty("user.name", "") + "|" + System.getProperty("os.name", "") + "|" + System.getProperty("os.arch", "");
        }
        object = (String)object + "|" + System.getProperty("user.name", "");
        cached = Hwid.sha256Hex((String)object);
        return cached;
    }

    private static String readMachineGuid() {
        try {
            Process process = new ProcessBuilder("reg", "query", "HKLM\\SOFTWARE\\Microsoft\\Cryptography", "/v", "MachineGuid").redirectErrorStream(true).start();
            String string = null;
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));){
                String string2;
                while ((string2 = bufferedReader.readLine()) != null) {
                    int n = string2.indexOf("REG_SZ");
                    if (n < 0) continue;
                    string = string2.substring(n + "REG_SZ".length()).trim();
                }
            }
            process.waitFor();
            return string;
        }
        catch (Exception exception) {
            return null;
        }
    }

    private static String sha256Hex(String string) {
        try {
            byte[] byArray = MessageDigest.getInstance("SHA-256").digest(string.getBytes(StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder(byArray.length * 2);
            for (byte by : byArray) {
                stringBuilder.append(Character.forDigit(by >> 4 & 0xF, 16));
                stringBuilder.append(Character.forDigit(by & 0xF, 16));
            }
            return stringBuilder.substring(0, 32).toUpperCase();
        }
        catch (Exception exception) {
            return Integer.toHexString(string.hashCode());
        }
    }
}


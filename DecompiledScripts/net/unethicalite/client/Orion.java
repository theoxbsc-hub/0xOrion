/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.RuneLite
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import net.runelite.client.RuneLite;
import net.unethicalite.client.minimal.MinimalClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Orion {
    private static final Logger log = LoggerFactory.getLogger(Orion.class);
    public static final File CLIENT_DIR = new File(System.getProperty("user.home"), ".orion");

    public static void main(String[] args) throws Exception {
        if (Arrays.asList(args).contains("--minimal")) {
            System.setProperty("unethicalite.minimal", "true");
            MinimalClient.main(args);
        } else {
            RuneLite.main((String[])args);
        }
    }

    private static String getCacheDir() {
        String dir = System.getProperty("unethicalite.cache-dir");
        if (dir != null) {
            return dir;
        }
        return ".orion";
    }

    public static File getCacheDirectory() {
        String dir = Orion.getCacheDir();
        if (Objects.equals(dir, ".orion")) {
            return new File(CLIENT_DIR, "jagexcache");
        }
        File cacheDirs = new File(CLIENT_DIR, "custom-cache");
        return new File(cacheDirs, dir);
    }
}


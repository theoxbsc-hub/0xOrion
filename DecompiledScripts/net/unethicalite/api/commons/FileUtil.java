/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.reflect.TypeToken
 *  net.runelite.client.RuneLite
 *  net.runelite.client.plugins.Plugin
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.api.commons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
    private static final File DATA_DIR = new File(RuneLite.RUNELITE_DIR, "data");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void serialize(Plugin plugin, String fileName, Serializable data) {
        FileUtil.serialize(plugin.getClass().getSimpleName(), fileName, data);
    }

    public static void serialize(String dirName, String fileName, Serializable data) {
        try (FileOutputStream fos = new FileOutputStream(new File(FileUtil.getDirectory(dirName), fileName));
             ObjectOutputStream oos = new ObjectOutputStream(fos);){
            oos.writeObject(data);
            oos.flush();
        }
        catch (IOException e) {
            log.error("Error serializing file: {}", (Object)e.getMessage());
        }
    }

    public static <T> T deserialize(Plugin plugin, String fileName) {
        return FileUtil.deserialize(plugin.getClass().getSimpleName(), fileName);
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static <T> T deserialize(String dirName, String fileName) {
        try (FileInputStream fis = new FileInputStream(new File(FileUtil.getDirectory(dirName), fileName));){
            Object object;
            try (ObjectInputStream ois = new ObjectInputStream(fis);){
                object = ois.readObject();
            }
            return (T)object;
        }
        catch (IOException | ClassNotFoundException e) {
            log.error("Error deserializing file: {}", (Object)e.getMessage());
            return null;
        }
    }

    public static void writeJson(Plugin plugin, String fileName, Object data) {
        FileUtil.writeJson(plugin.getClass().getSimpleName(), fileName, data);
    }

    public static void writeJson(String dirName, String fileName, Object data) {
        try (FileWriter writer = new FileWriter(new File(FileUtil.getDirectory(dirName), fileName));){
            GSON.toJson(data, (Appendable)writer);
            writer.flush();
        }
        catch (IOException e) {
            log.error("Error writing json: {}", (Object)e.getMessage());
        }
    }

    public static <T> T readJson(Plugin plugin, String fileName, Class<T> type) {
        return FileUtil.readJson(plugin.getClass().getSimpleName(), fileName, type);
    }

    public static <T> T readJson(String dirName, String fileName, Class<T> type) {
        return FileUtil.readJson(dirName, fileName, TypeToken.get(type));
    }

    public static <T> T readJson(Plugin plugin, String fileName, TypeToken<T> type) {
        return FileUtil.readJson(plugin.getClass().getSimpleName(), fileName, type);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static <T> T readJson(String dirName, String fileName, TypeToken<T> type) {
        try (FileReader reader = new FileReader(new File(FileUtil.getDirectory(dirName), fileName));){
            Object object = GSON.fromJson((Reader)reader, type.getType());
            return (T)object;
        }
        catch (IOException e) {
            log.error("Error reading json: {}", (Object)e.getMessage());
            return null;
        }
    }

    public static boolean exists(Plugin plugin, String fileName) {
        return FileUtil.exists(plugin.getClass().getSimpleName(), fileName);
    }

    public static boolean exists(String dirName, String fileName) {
        File pluginDir = new File(DATA_DIR, dirName);
        return new File(pluginDir, fileName).exists();
    }

    public static File getPluginDirectory(Plugin plugin) {
        return FileUtil.getDirectory(plugin.getClass().getSimpleName());
    }

    public static File getDirectory(String dirName) {
        File dir = new File(DATA_DIR, dirName);
        dir.mkdirs();
        return dir;
    }
}


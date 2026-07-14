/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.api.commons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import net.unethicalite.client.config.UnethicaliteProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);
    private static final Gson GSON = new GsonBuilder().create();

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static <T> T readJson(String url, Class<T> expectedType) {
        try {
            URL httpUrl = new URL(url);
            URLConnection conn = httpUrl.openConnection();
            conn.setConnectTimeout(UnethicaliteProperties.getConnectionTimeout());
            conn.setReadTimeout(UnethicaliteProperties.getReadTimeout());
            try (InputStream stream = conn.getInputStream();){
                Object object = GSON.fromJson(new String(stream.readAllBytes()), expectedType);
                return (T)object;
            }
            catch (IOException e) {
                log.error("Failed to read data", (Throwable)e);
                return null;
            }
        }
        catch (IOException e) {
            log.error("Failed to connect to url", (Throwable)e);
            return null;
        }
    }

    public static byte[] readBytes(String url) {
        byte[] byArray;
        block11: {
            URL httpUrl = new URL(url);
            URLConnection conn = httpUrl.openConnection();
            conn.setConnectTimeout(UnethicaliteProperties.getConnectionTimeout());
            conn.setReadTimeout(UnethicaliteProperties.getReadTimeout());
            InputStream stream = conn.getInputStream();
            try {
                byArray = stream.readAllBytes();
                if (stream == null) break block11;
            }
            catch (Throwable throwable) {
                try {
                    try {
                        if (stream != null) {
                            try {
                                stream.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (IOException e) {
                        log.error("Failed to read data", (Throwable)e);
                        return null;
                    }
                }
                catch (IOException e) {
                    log.error("Failed to connect to url", (Throwable)e);
                    return null;
                }
            }
            stream.close();
        }
        return byArray;
    }
}


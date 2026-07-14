/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.client.config;

import java.io.IOException;
import java.util.Properties;

public class UnethicaliteProperties {
    private static final String API_URL;
    private static final int CONNECTION_TIMEOUT;
    private static final int READ_TIMEOUT;
    private static final Properties properties;

    public static String getApiUrl() {
        return API_URL;
    }

    public static int getConnectionTimeout() {
        return CONNECTION_TIMEOUT;
    }

    public static int getReadTimeout() {
        return READ_TIMEOUT;
    }

    public static Properties getProperties() {
        return properties;
    }

    static {
        properties = new Properties();
        try {
            properties.load(UnethicaliteProperties.class.getResourceAsStream("unethicalite.properties"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        API_URL = properties.getProperty("unethicalite.api.url");
        CONNECTION_TIMEOUT = Integer.parseInt(properties.getProperty("unethicalite.api.connection-timeout"));
        READ_TIMEOUT = Integer.parseInt(properties.getProperty("unethicalite.api.read-timeout"));
    }
}


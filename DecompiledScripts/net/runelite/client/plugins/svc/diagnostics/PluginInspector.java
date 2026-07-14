/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginManager
 */
package net.runelite.client.plugins.svc.diagnostics;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginManager;
import net.unethicalite.api.plugins.Plugins;

final class PluginInspector {
    private static final String ORION_PKG = "net.runelite.client.plugins.svc";
    private static final String[] STATUS_FIELDS = new String[]{"status", "state", "phase", "currentAction", "action"};
    private static final String[] SENSITIVE = new String[]{"pin", "pass", "password", "secret", "token", "apikey", "api key", "auth", "license", "hwid", "mac", "username", "account", "email", "credential"};

    private PluginInspector() {
    }

    static List<Plugin> enabledOrionPlugins(Plugin plugin) {
        ArrayList<Plugin> arrayList = new ArrayList<Plugin>();
        try {
            PluginManager pluginManager = Plugins.getPluginManager();
            if (pluginManager == null) {
                return arrayList;
            }
            for (Plugin plugin2 : pluginManager.getPlugins()) {
                if (plugin2 == null || plugin2 == plugin || !plugin2.getClass().getName().startsWith(ORION_PKG) || !pluginManager.isPluginEnabled(plugin2)) continue;
                arrayList.add(plugin2);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        arrayList.sort(Comparator.comparing(PluginInspector::displayName));
        return arrayList;
    }

    static String displayName(Plugin plugin) {
        String string;
        try {
            string = plugin.getName();
        }
        catch (Throwable throwable) {
            return plugin.getClass().getSimpleName();
        }
        if (string == null) {
            return plugin.getClass().getSimpleName();
        }
        return string.replaceAll("<[^>]*>", "").replace("\u25cf", "").trim();
    }

    static String readStatus(Plugin plugin) {
        try {
            String string;
            Method method = plugin.getClass().getMethod("getStatus", new Class[0]);
            Object object = method.invoke(plugin, new Object[0]);
            if (object != null && !(string = object.toString().trim()).isEmpty()) {
                return string;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        for (String string : STATUS_FIELDS) {
            try {
                String string2;
                Field field = plugin.getClass().getDeclaredField(string);
                field.setAccessible(true);
                Object object = field.get(plugin);
                if (object == null || (string2 = object.toString().trim()).isEmpty()) continue;
                return string2;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    static String dumpConfig(Plugin plugin) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Object object;
            PluginManager pluginManager = Plugins.getPluginManager();
            if (pluginManager == null) {
                return "   (config unavailable)";
            }
            Config config = pluginManager.getPluginConfigProxy(plugin);
            if (config == null) {
                return "   (no config)";
            }
            Class<?> clazz = PluginInspector.configInterface(config);
            if (clazz == null) {
                return "   (no config interface)";
            }
            String string = "";
            ConfigGroup configGroup = clazz.getAnnotation(ConfigGroup.class);
            if (configGroup != null) {
                string = configGroup.value();
            }
            ArrayList<Method> arrayList = new ArrayList<Method>();
            for (Method object2 : clazz.getMethods()) {
                object = object2.getAnnotation(ConfigItem.class);
                if (object == null || object2.getParameterCount() != 0 || object.hidden() || object.secret()) continue;
                arrayList.add(object2);
            }
            arrayList.sort(Comparator.comparingInt(method -> method.getAnnotation(ConfigItem.class).position()));
            if (!string.isEmpty()) {
                stringBuilder.append("   [group: ").append(string).append("]\n");
            }
            if (arrayList.isEmpty()) {
                stringBuilder.append("   (no settings)");
                return stringBuilder.toString();
            }
            for (Method method2 : arrayList) {
                ConfigItem configItem = method2.getAnnotation(ConfigItem.class);
                String string2 = configItem.keyName();
                if (PluginInspector.isSensitive(string2) || PluginInspector.isSensitive(configItem.name())) {
                    object = "***";
                } else {
                    try {
                        Object object2 = method2.invoke(config, new Object[0]);
                        object = String.valueOf(object2);
                    }
                    catch (Throwable throwable) {
                        object = "<unreadable>";
                    }
                }
                stringBuilder.append(String.format("   %-28s = %s%n", string2, object));
            }
        }
        catch (Throwable throwable) {
            stringBuilder.append("   (config read failed: ").append(throwable.getClass().getSimpleName()).append(")");
        }
        return stringBuilder.toString();
    }

    private static Class<?> configInterface(Config config) {
        for (Class<?> clazz : config.getClass().getInterfaces()) {
            if (!Config.class.isAssignableFrom(clazz) || clazz == Config.class) continue;
            return clazz;
        }
        return null;
    }

    private static boolean isSensitive(String string) {
        if (string == null) {
            return false;
        }
        String string2 = string.toLowerCase();
        for (String string3 : SENSITIVE) {
            if (!string2.contains(string3)) continue;
            return true;
        }
        return false;
    }
}


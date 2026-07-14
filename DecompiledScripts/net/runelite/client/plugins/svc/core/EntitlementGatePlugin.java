/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  net.runelite.client.Notifier
 *  net.runelite.client.callback.ClientThread
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.events.PluginChanged
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.plugins.PluginManager
 */
package net.runelite.client.plugins.svc.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.svc.core.Entitlements;

@PluginDescriptor(name="x64 Entitlement Gate", description="Support plugin \u2014 locks scripts not included in your license. Leave enabled.", tags={"orion", "x64", "core", "license"}, enabledByDefault=true, hidden=true)
public class EntitlementGatePlugin
extends Plugin {
    private static final String ORION_PACKAGE = ".plugins.svc.";
    private static final String DOT = "\u25cf";
    private static final String LOCK = "\ud83d\udd12 ";
    @Inject
    private PluginManager pluginManager;
    @Inject
    private ClientThread clientThread;
    @Inject
    private Notifier notifier;

    protected void startUp() throws Exception {
        super.startUp();
        ArrayList<Plugin> arrayList = new ArrayList<Plugin>();
        for (Plugin plugin : this.pluginManager.getPlugins()) {
            if (!this.isLocked(plugin)) continue;
            arrayList.add(plugin);
        }
        for (Plugin plugin : arrayList) {
            try {
                this.pluginManager.setPluginEnabled(plugin, false);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            this.applyHidden(plugin);
        }
        this.clientThread.invokeLater(() -> {
            for (Plugin plugin : arrayList) {
                this.disable(plugin);
            }
        });
    }

    @Subscribe
    public void onPluginChanged(PluginChanged pluginChanged) {
        if (pluginChanged == null || !pluginChanged.isLoaded()) {
            return;
        }
        Plugin plugin = pluginChanged.getPlugin();
        if (!this.isLocked(plugin)) {
            return;
        }
        this.clientThread.invokeLater(() -> this.disable(plugin));
        this.notifyLocked(plugin);
    }

    private boolean isLocked(Plugin plugin) {
        if (plugin == null || plugin == this) {
            return false;
        }
        Class<?> clazz = plugin.getClass();
        if (!clazz.getName().contains(ORION_PACKAGE)) {
            return false;
        }
        PluginDescriptor pluginDescriptor = clazz.getAnnotation(PluginDescriptor.class);
        if (pluginDescriptor == null || pluginDescriptor.hidden()) {
            return false;
        }
        return !Entitlements.isAllowed(clazz.getSimpleName());
    }

    private void disable(Plugin plugin) {
        try {
            this.pluginManager.setPluginEnabled(plugin, false);
            this.pluginManager.stopPlugin(plugin);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void notifyLocked(Plugin plugin) {
        try {
            this.notifier.notify("\"" + EntitlementGatePlugin.stripHtml(plugin.getName()) + "\" isn't included in your license. Contact x64 to unlock it.");
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void applyHidden(Plugin plugin) {
        try {
            PluginDescriptor pluginDescriptor = plugin.getClass().getAnnotation(PluginDescriptor.class);
            if (pluginDescriptor == null || pluginDescriptor.hidden()) {
                return;
            }
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(pluginDescriptor);
            Field field = invocationHandler.getClass().getDeclaredField("memberValues");
            field.setAccessible(true);
            Map map = (Map)field.get(invocationHandler);
            map.put("hidden", Boolean.TRUE);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static String stripHtml(String string) {
        if (string == null) {
            return "";
        }
        return string.replaceAll("<[^>]*>", "").replace(DOT, "").replace(LOCK, "").trim();
    }
}


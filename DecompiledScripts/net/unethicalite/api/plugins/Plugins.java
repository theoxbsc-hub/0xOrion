/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Singleton
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginInstantiationException
 *  net.runelite.client.plugins.PluginManager
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.api.plugins;

import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.unethicalite.client.Static;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Plugins {
    private static final Logger log = LoggerFactory.getLogger(Plugins.class);

    public static PluginManager getPluginManager() {
        return Static.getPluginManager();
    }

    public static boolean isEnabled(Plugin plugin) {
        return Plugins.getPluginManager().isPluginEnabled(plugin);
    }

    public static boolean isEnabled(String configGroup) {
        return (Boolean)Static.getConfigManager().getConfiguration("runelite", configGroup, Boolean.class);
    }

    public static boolean stopPlugin(Plugin plugin) {
        try {
            assert (SwingUtilities.isEventDispatchThread());
            Plugins.getPluginManager().remove(plugin);
            Plugins.getPluginManager().setPluginEnabled(plugin, false);
            return Plugins.getPluginManager().stopPlugin(plugin);
        }
        catch (PluginInstantiationException e) {
            log.error("Failed to stop plugin {}, error: {}", (Object)plugin.getClass().getSimpleName(), (Object)e);
            return false;
        }
    }

    public static boolean startPlugin(Plugin plugin) {
        try {
            Plugins.getPluginManager().setPluginEnabled(plugin, true);
            return Plugins.getPluginManager().startPlugin(plugin);
        }
        catch (PluginInstantiationException e) {
            log.error("Failed to start plugin {}, error: {}", (Object)plugin.getClass().getSimpleName(), (Object)e);
            return false;
        }
    }
}


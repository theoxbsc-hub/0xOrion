/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.plugins.Plugin
 */
package net.unethicalite.api.plugins;

import net.runelite.client.config.Config;
import net.runelite.client.plugins.Plugin;

public abstract class SettingsPlugin
extends Plugin {
    public abstract Config getConfig();

    public abstract String getPluginName();

    public abstract String getPluginDescription();

    public abstract String[] getPluginTags();
}


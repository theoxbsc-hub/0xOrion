/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.plugins.PluginManager
 *  net.runelite.client.util.Text
 */
package net.runelite.client.plugins.svc.core;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.util.Text;

@PluginDescriptor(name="x64 Core", description="Support plugin \u2014 pins the x64 scripts to the top of the plugin list. Leave enabled.", tags={"orion", "x64", "core"}, enabledByDefault=true, hidden=true)
public class PinManagerPlugin
extends Plugin {
    private static final String RUNELITE_GROUP = "runelite";
    private static final String PINNED_KEY = "pinnedPlugins";
    private static final String ORION_PACKAGE = ".plugins.svc.";
    @Inject
    private ConfigManager configManager;
    @Inject
    private PluginManager pluginManager;

    protected void startUp() throws Exception {
        super.startUp();
        this.pinOrionPlugins();
    }

    private void pinOrionPlugins() {
        String string = this.configManager.getConfiguration(RUNELITE_GROUP, PINNED_KEY);
        List list = string == null ? List.of() : Text.fromCSV((String)string);
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>();
        for (Object object : this.pluginManager.getPlugins()) {
            PluginDescriptor pluginDescriptor;
            Object object2;
            if (object == null || !((Class)(object2 = object.getClass())).getName().contains(ORION_PACKAGE) || (pluginDescriptor = ((Class)object2).getAnnotation(PluginDescriptor.class)) == null || pluginDescriptor.hidden()) continue;
            linkedHashSet.add(object.getName());
        }
        LinkedHashSet linkedHashSet2 = new LinkedHashSet();
        for (Object object2 : list) {
            if (((String)object2).contains("x64 ") && !linkedHashSet.contains(object2)) continue;
            linkedHashSet2.add(object2);
        }
        linkedHashSet2.addAll(linkedHashSet);
        if (!new LinkedHashSet(list).equals(linkedHashSet2)) {
            this.configManager.setConfiguration(RUNELITE_GROUP, PINNED_KEY, Text.toCSV(new ArrayList(linkedHashSet2)));
        }
    }
}


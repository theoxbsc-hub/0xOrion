/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc.foundation;

import com.google.inject.Binder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.foundation.WidgetInspectorConfig;
import net.runelite.client.plugins.svc.foundation.WidgetInspectorOverlay;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(name="<html><!--4--><nobr><font color='#4FA8FF'>\u25cf x64 Widget Inspector</font></nobr></html>", description="Shows open interface IDs and child widget details \u2014 use this to find widget IDs for bot configs", tags={"orion", "widget", "inspector", "debug", "ids"}, enabledByDefault=false)
public class WidgetInspectorPlugin
extends Plugin {
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private WidgetInspectorConfig config;
    private WidgetInspectorOverlay overlay;
    private MouseAdapter mouseAdapter;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(WidgetInspectorConfig.class).toInstance((Object)((WidgetInspectorConfig)configManager.getConfig(WidgetInspectorConfig.class)));
    }

    protected void startUp() {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        this.overlay = new WidgetInspectorOverlay(this.client, this.config, configManager);
        this.overlayManager.add((Overlay)this.overlay);
        this.mouseAdapter = new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (WidgetInspectorPlugin.this.overlay != null) {
                    WidgetInspectorPlugin.this.overlay.handleMouseClicked(mouseEvent.getX(), mouseEvent.getY());
                }
            }
        };
        try {
            this.client.getCanvas().addMouseListener(this.mouseAdapter);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    protected void shutDown() {
        try {
            this.client.getCanvas().removeMouseListener(this.mouseAdapter);
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.overlayManager.remove((Overlay)this.overlay);
        this.overlay = null;
        this.mouseAdapter = null;
    }
}


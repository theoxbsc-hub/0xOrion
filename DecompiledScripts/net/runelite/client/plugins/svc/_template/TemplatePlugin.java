/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.ItemComposition
 *  net.runelite.api.Player
 *  net.runelite.client.RuneLite
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.runelite.client.plugins.svc._template;

import com.google.inject.Binder;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.Player;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc._template.TemplateConfig;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(name="My Script", description="What it does", tags={"orion", "custom"}, enabledByDefault=false, hidden=true)
public class TemplatePlugin
extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private TemplateConfig config;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(TemplateConfig.class).toInstance((Object)((TemplateConfig)configManager.getConfig(TemplateConfig.class)));
    }

    @Override
    protected void onStart() throws Exception {
    }

    @Override
    protected void onStop() throws Exception {
    }

    @Override
    protected int loop() {
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return 1000;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return 600;
        }
        return 600;
    }

    private String itemName(int n) {
        ItemComposition itemComposition = this.client.getItemDefinition(n);
        return itemComposition != null ? itemComposition.getName() : "";
    }
}


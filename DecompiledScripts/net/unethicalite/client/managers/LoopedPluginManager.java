/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Provider
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.events.PluginChanged
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.config.PluginConfigurationDescriptor
 *  net.runelite.client.plugins.config.PluginListPanel
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.managers;

import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.config.PluginConfigurationDescriptor;
import net.runelite.client.plugins.config.PluginListPanel;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.plugins.SettingsPlugin;
import net.unethicalite.api.plugins.Task;
import net.unethicalite.api.plugins.TaskPlugin;
import net.unethicalite.client.Static;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LoopedPluginManager {
    private static final Logger log = LoggerFactory.getLogger(LoopedPluginManager.class);
    private final EventBus eventBus;
    @Inject
    private ConfigManager configManager;
    @Inject
    private Provider<PluginListPanel> pluginListPanelProvider;
    @Inject
    private Client client;
    private LoopedPlugin loopedPlugin;
    private Thread currentLoop;

    @Inject
    LoopedPluginManager(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.register((Object)this);
    }

    public void unregister() {
        if (!this.isPluginRegistered()) {
            return;
        }
        this.loopedPlugin.stop();
        if (this.loopedPlugin instanceof Script) {
            Script script = (Script)this.loopedPlugin;
            script.onStop();
            script.getPaint().clear();
        }
        if (this.loopedPlugin instanceof TaskPlugin) {
            for (Task task : ((TaskPlugin)this.loopedPlugin).getTasks()) {
                if (!task.subscribe()) continue;
                this.eventBus.unregister((Object)task);
            }
        }
        this.currentLoop = null;
        this.loopedPlugin = null;
        this.client.setQueuedMenu(null);
    }

    public void submit(Plugin plugin) {
        if (!(plugin instanceof LoopedPlugin)) {
            log.error("Only LoopedPlugins may be submitted to the LoopedPluginManager");
            return;
        }
        if (this.currentLoop != null && this.currentLoop.isAlive()) {
            log.debug("A plugin is already running, stopping it");
            Executors.newSingleThreadExecutor().execute(this::unregister);
        }
        log.debug("Registering {} as a LoopedPlugin", (Object)plugin.getClass().getSimpleName());
        this.loopedPlugin = (LoopedPlugin)plugin;
        if (this.loopedPlugin instanceof TaskPlugin) {
            for (Task task : ((TaskPlugin)this.loopedPlugin).getTasks()) {
                if (!task.subscribe()) continue;
                this.eventBus.register((Object)task);
            }
        }
        if (plugin instanceof Script) {
            Script script = (Script)plugin;
            script.getPaint().clear();
            script.onStart(Static.getScriptArgs());
        }
        this.currentLoop = new Thread(this.loopedPlugin);
        this.currentLoop.start();
    }

    public boolean isPluginRegistered() {
        return this.currentLoop != null && this.currentLoop.isAlive() && this.loopedPlugin != null && this.loopedPlugin.isRunning();
    }

    @Subscribe
    private void onPluginChanged(PluginChanged pluginChanged) {
        Plugin plugin = pluginChanged.getPlugin();
        if (pluginChanged.isLoaded()) {
            if (plugin instanceof SettingsPlugin) {
                SettingsPlugin settingsPlugin = (SettingsPlugin)plugin;
                PluginListPanel pluginListPanel = (PluginListPanel)this.pluginListPanelProvider.get();
                pluginListPanel.addFakePlugin(new PluginConfigurationDescriptor[]{new PluginConfigurationDescriptor(settingsPlugin.getPluginName(), settingsPlugin.getPluginDescription(), settingsPlugin.getPluginTags(), settingsPlugin.getConfig(), this.configManager.getConfigDescriptor(settingsPlugin.getConfig()))});
                pluginListPanel.rebuildPluginList();
                return;
            }
            if (plugin instanceof LoopedPlugin) {
                this.submit(plugin);
            }
        } else if (plugin instanceof LoopedPlugin && plugin == this.loopedPlugin) {
            this.unregister();
        }
    }
}


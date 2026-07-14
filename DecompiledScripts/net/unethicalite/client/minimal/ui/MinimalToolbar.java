/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.miginfocom.swing.MigLayout
 *  net.runelite.api.Client
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.config.RuneLiteConfig
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.events.ConfigChanged
 *  net.runelite.client.plugins.config.PluginConfigurationDescriptor
 *  net.runelite.client.ui.ColorScheme
 *  net.runelite.client.ui.components.colorpicker.ColorPickerManager
 */
package net.unethicalite.client.minimal.ui;

import java.awt.LayoutManager;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import net.runelite.api.Client;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.config.PluginConfigurationDescriptor;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.client.config.UnethicaliteConfig;
import net.unethicalite.client.devtools.EntityRenderer;
import net.unethicalite.client.devtools.scriptinspector.ScriptInspector;
import net.unethicalite.client.devtools.varinspector.VarInspector;
import net.unethicalite.client.devtools.widgetinspector.WidgetInspector;
import net.unethicalite.client.managers.MinimalFpsManager;
import net.unethicalite.client.managers.MinimalPluginManager;
import net.unethicalite.client.minimal.config.DisableRenderCallbacks;
import net.unethicalite.client.minimal.config.MinimalConfigPanel;
import net.unethicalite.client.minimal.plugins.MinimalPluginChanged;
import net.unethicalite.client.minimal.plugins.MinimalPluginState;
import net.unethicalite.client.minimal.ui.MinimalPluginsPanel;

@Singleton
public class MinimalToolbar
extends JMenuBar {
    private static final DisableRenderCallbacks DISABLE_RENDERING = new DisableRenderCallbacks();
    private final VarInspector varInspector;
    private final WidgetInspector widgetInspector;
    private final ScriptInspector scriptInspector;
    private final EntityRenderer entityRenderer;
    private final MinimalPluginManager minimalPluginManager;
    private final MinimalPluginsPanel minimalPluginsPanel;
    private final ConfigManager configManager;
    private final EventBus eventBus;
    private final UnethicaliteConfig unethicaliteConfig;
    private final RuneLiteConfig runeLiteConfig;
    private final Client client;
    private final MinimalFpsManager minimalFpsManager;
    private final ColorPickerManager colorPickerManager;
    private JMenuItem pluginConfig;
    private JMenuItem stopPlugin;
    private JMenuItem pauseScript;
    private JMenuItem restartPlugin;
    private JRadioButton rendering;
    private MinimalConfigPanel unethicaliteConfigPanel;
    private MinimalConfigPanel clientConfigPanel;

    @Inject
    public MinimalToolbar(VarInspector varInspector, WidgetInspector widgetInspector, ScriptInspector scriptInspector, EntityRenderer entityRenderer, MinimalPluginManager minimalPluginManager, MinimalPluginsPanel minimalPluginsPanel, ConfigManager configManager, EventBus eventBus, UnethicaliteConfig unethicaliteConfig, RuneLiteConfig runeLiteConfig, Client client, MinimalFpsManager minimalFpsManager, ColorPickerManager colorPickerManager) {
        this.varInspector = varInspector;
        this.widgetInspector = widgetInspector;
        this.scriptInspector = scriptInspector;
        this.entityRenderer = entityRenderer;
        this.minimalPluginManager = minimalPluginManager;
        this.minimalPluginsPanel = minimalPluginsPanel;
        this.configManager = configManager;
        this.eventBus = eventBus;
        this.unethicaliteConfig = unethicaliteConfig;
        this.runeLiteConfig = runeLiteConfig;
        this.client = client;
        this.minimalFpsManager = minimalFpsManager;
        this.colorPickerManager = colorPickerManager;
    }

    public void init() {
        this.configManager.setDefaultConfiguration((Config)this.unethicaliteConfig, false);
        this.configManager.setDefaultConfiguration((Config)this.runeLiteConfig, false);
        PluginConfigurationDescriptor minimal = new PluginConfigurationDescriptor("Unethicalite", "Unethicalite settings", new String[0], (Config)this.unethicaliteConfig, this.configManager.getConfigDescriptor((Config)this.unethicaliteConfig));
        this.unethicaliteConfigPanel = new MinimalConfigPanel(this.configManager, this.colorPickerManager, this.eventBus);
        this.unethicaliteConfigPanel.init(minimal);
        PluginConfigurationDescriptor cl = new PluginConfigurationDescriptor("Client", "Client settings", new String[0], (Config)this.runeLiteConfig, this.configManager.getConfigDescriptor((Config)this.runeLiteConfig));
        this.clientConfigPanel = new MinimalConfigPanel(this.configManager, this.colorPickerManager, this.eventBus);
        this.clientConfigPanel.init(cl);
        JFrame interactWindow = new JFrame();
        interactWindow.setVisible(false);
        interactWindow.getContentPane().setBackground(ColorScheme.DARK_GRAY_COLOR);
        interactWindow.setAlwaysOnTop(true);
        interactWindow.setLayout((LayoutManager)new MigLayout());
        interactWindow.setDefaultCloseOperation(2);
        interactWindow.pack();
        SwingUtilities.invokeLater(() -> {
            JMenuItem scripts = new JMenuItem("Plugins");
            scripts.addActionListener(e -> this.minimalPluginsPanel.open());
            scripts.setMaximumSize(scripts.getPreferredSize());
            this.add(scripts);
            JMenu settingsMenu = new JMenu("Settings");
            JMenuItem unethicaliteSettings = new JMenuItem("Unethicalite Settings");
            unethicaliteSettings.addActionListener(e -> this.unethicaliteConfigPanel.open());
            settingsMenu.add(unethicaliteSettings);
            JMenuItem interactSettings = new JMenuItem("Interaction Settings");
            interactSettings.addActionListener(e -> interactWindow.setVisible(!interactWindow.isVisible()));
            settingsMenu.add(interactSettings);
            JMenuItem clientSettings = new JMenuItem("Client Settings");
            clientSettings.addActionListener(e -> this.clientConfigPanel.open());
            settingsMenu.add(clientSettings);
            this.add(settingsMenu);
            this.rendering = new JRadioButton("Toggle rendering");
            this.rendering.addActionListener(e -> this.configManager.setConfiguration("unethicalite", "renderOff", (Object)this.rendering.isSelected()));
            JMenu debug = new JMenu("Debug");
            this.rendering.setSelected((Boolean)this.configManager.getConfiguration("unethicalite", "renderOff", Boolean.class));
            this.add(this.rendering);
            JRadioButton mouseDebug = new JRadioButton("Mouse debug");
            mouseDebug.addActionListener(e -> this.configManager.setConfiguration("unethicalite", "drawMouse", (Object)mouseDebug.isSelected()));
            debug.add(mouseDebug);
            JRadioButton menuActionDebug = new JRadioButton("MenuAction debug");
            menuActionDebug.addActionListener(e -> this.configManager.setConfiguration("unethicalite", "debugMenuAction", (Object)menuActionDebug.isSelected()));
            debug.add(menuActionDebug);
            JRadioButton dialogDebug = new JRadioButton("Dialog debug");
            dialogDebug.addActionListener(e -> this.configManager.setConfiguration("unethicalite", "debugDialogs", (Object)dialogDebug.isSelected()));
            debug.add(dialogDebug);
            JRadioButton collisionDebug = new JRadioButton("Collision map");
            collisionDebug.addActionListener(e -> this.entityRenderer.setCollisionMap(!this.entityRenderer.isCollisionMap()));
            debug.add(collisionDebug);
            JRadioButton pathDebug = new JRadioButton("Draw path");
            pathDebug.addActionListener(e -> this.entityRenderer.setPath(!this.entityRenderer.isPath()));
            debug.add(pathDebug);
            this.add(debug);
            JMenu developer = new JMenu("Developer");
            JMenuItem varInspectorItem = new JMenuItem("Var Inspector");
            varInspectorItem.addActionListener(e -> this.varInspector.open());
            developer.add(varInspectorItem);
            JMenuItem widgetInspectorItem = new JMenuItem("Widget Inspector");
            widgetInspectorItem.addActionListener(e -> this.widgetInspector.open());
            developer.add(widgetInspectorItem);
            JMenuItem scriptInspectorItem = new JMenuItem("Script Inspector");
            scriptInspectorItem.addActionListener(e -> this.scriptInspector.open());
            developer.add(scriptInspectorItem);
            JRadioButton gameObjectsBtn = new JRadioButton("Game Objects");
            gameObjectsBtn.addActionListener(e -> this.entityRenderer.setGameObjects(!this.entityRenderer.isGameObjects()));
            JRadioButton wallObjectsBtn = new JRadioButton("Wall Objects");
            wallObjectsBtn.addActionListener(e -> this.entityRenderer.setWallObjects(!this.entityRenderer.isWallObjects()));
            JRadioButton decorativeObjectsBtn = new JRadioButton("Decorative Objects");
            decorativeObjectsBtn.addActionListener(e -> this.entityRenderer.setDecorativeObjects(!this.entityRenderer.isDecorativeObjects()));
            JRadioButton groundObjectsBtn = new JRadioButton("Ground Objects");
            groundObjectsBtn.addActionListener(e -> this.entityRenderer.setGroundObjects(!this.entityRenderer.isGroundObjects()));
            JRadioButton npcsBtn = new JRadioButton("NPCs");
            npcsBtn.addActionListener(e -> this.entityRenderer.setNpcs(!this.entityRenderer.isNpcs()));
            JRadioButton playersBtn = new JRadioButton("Players");
            playersBtn.addActionListener(e -> this.entityRenderer.setPlayers(!this.entityRenderer.isPlayers()));
            JRadioButton tileItemsBtn = new JRadioButton("Tile Items");
            tileItemsBtn.addActionListener(e -> this.entityRenderer.setTileItems(!this.entityRenderer.isTileItems()));
            JRadioButton inventoryBtn = new JRadioButton("Inventory");
            inventoryBtn.addActionListener(e -> this.entityRenderer.setInventory(!this.entityRenderer.isInventory()));
            JRadioButton projectilesBtn = new JRadioButton("Projectiles");
            projectilesBtn.addActionListener(e -> this.entityRenderer.setProjectiles(!this.entityRenderer.isProjectiles()));
            JRadioButton tileLocationBtn = new JRadioButton("Tile Location");
            tileLocationBtn.addActionListener(e -> this.entityRenderer.setTileLocation(!this.entityRenderer.isTileLocation()));
            developer.add(gameObjectsBtn);
            developer.add(wallObjectsBtn);
            developer.add(decorativeObjectsBtn);
            developer.add(groundObjectsBtn);
            developer.add(npcsBtn);
            developer.add(playersBtn);
            developer.add(tileItemsBtn);
            developer.add(inventoryBtn);
            developer.add(projectilesBtn);
            developer.add(tileLocationBtn);
            this.add(developer);
            this.pluginConfig = new JMenuItem("Plugin config");
            this.pluginConfig.addActionListener(e -> {
                if (this.minimalPluginManager.getPlugin() != null && this.minimalPluginManager.getConfig() != null) {
                    MinimalConfigPanel panel = new MinimalConfigPanel(this.configManager, this.colorPickerManager, this.eventBus);
                    panel.init(new PluginConfigurationDescriptor(this.minimalPluginManager.getPlugin().getName(), "", new String[0], this.minimalPluginManager.getConfig(), this.configManager.getConfigDescriptor(this.minimalPluginManager.getConfig())));
                    panel.open();
                }
            });
            this.pluginConfig.setVisible(false);
            this.pluginConfig.setMaximumSize(this.pluginConfig.getPreferredSize());
            this.add(this.pluginConfig);
            this.pauseScript = new JMenuItem("Pause script");
            this.pauseScript.addActionListener(e -> {
                if (this.minimalPluginManager.getPlugin() != null) {
                    this.minimalPluginManager.pauseScript();
                }
            });
            this.pauseScript.setVisible(false);
            this.pauseScript.setMaximumSize(this.pauseScript.getPreferredSize());
            this.add(this.pauseScript);
            this.stopPlugin = new JMenuItem("Stop script");
            this.stopPlugin.addActionListener(e -> {
                if (this.minimalPluginManager.getPlugin() != null) {
                    this.minimalPluginManager.stopPlugin();
                }
            });
            this.stopPlugin.setVisible(false);
            this.stopPlugin.setMaximumSize(this.stopPlugin.getPreferredSize());
            this.add(this.stopPlugin);
            this.restartPlugin = new JMenuItem("Restart script");
            this.restartPlugin.addActionListener(e -> {
                if (this.minimalPluginManager.getPlugin() != null) {
                    this.minimalPluginManager.restartPlugin();
                }
            });
            this.restartPlugin.setVisible(false);
            this.restartPlugin.setMaximumSize(this.restartPlugin.getPreferredSize());
            this.add(this.restartPlugin);
        });
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals("unethicalite")) {
            return;
        }
        switch (event.getKey()) {
            case "renderOff": {
                if (this.rendering == null) break;
                SwingUtilities.invokeLater(() -> this.rendering.setSelected(Boolean.parseBoolean(event.getNewValue())));
                break;
            }
            case "fpsLimit": {
                this.minimalFpsManager.reloadConfig(this.unethicaliteConfig.fpsLimit());
            }
        }
    }

    @Subscribe
    private void onMinimalPluginChanged(MinimalPluginChanged event) {
        this.pluginConfig.setVisible(event.getState() == MinimalPluginState.STARTED || event.getState() == MinimalPluginState.PAUSED);
        this.stopPlugin.setVisible(event.getState() == MinimalPluginState.STARTED || event.getState() == MinimalPluginState.PAUSED);
        this.pauseScript.setVisible(event.getPlugin() instanceof Script && (event.getState() == MinimalPluginState.STARTED || event.getState() == MinimalPluginState.PAUSED));
        this.restartPlugin.setVisible(event.getState() == MinimalPluginState.STARTED || event.getState() == MinimalPluginState.PAUSED);
        if (event.getState() == MinimalPluginState.STARTED) {
            this.pluginConfig.setText(event.getPlugin().getName());
        }
        if (event.getState() == MinimalPluginState.PAUSED) {
            this.pauseScript.setText("Resume script");
        } else {
            this.pauseScript.setText("Pause script");
        }
    }
}


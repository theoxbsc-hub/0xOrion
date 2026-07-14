/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Named
 *  joptsimple.ArgumentAcceptingOptionSpec
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.hooks.DrawCallbacks
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.events.ConfigChanged
 *  net.runelite.client.plugins.PluginDescriptor
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.managers;

import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.hooks.DrawCallbacks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.account.GameAccount;
import net.unethicalite.api.game.Game;
import net.unethicalite.client.Static;
import net.unethicalite.client.managers.MinimalPluginManager;
import net.unethicalite.client.minimal.config.DisableRenderCallbacks;
import net.unethicalite.client.minimal.plugins.PluginEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsManager {
    private static final Logger log = LoggerFactory.getLogger(SettingsManager.class);
    private static final DisableRenderCallbacks DISABLE_RENDERING = new DisableRenderCallbacks();
    private final Client client;
    private final OptionSet clientArgs;
    private final ConfigManager configManager;
    private boolean initializedArgs;

    @Inject
    public SettingsManager(Client client, @Named(value="clientArgs") OptionSet clientArgs, EventBus eventBus, ConfigManager configManager) {
        this.client = client;
        this.clientArgs = clientArgs;
        this.configManager = configManager;
        eventBus.register((Object)this);
    }

    public static OptionSet parseArgs(OptionParser parser, String ... args) {
        ArgumentAcceptingOptionSpec accInfo = parser.accepts("account").withRequiredArg().ofType(String.class);
        ArgumentAcceptingOptionSpec cacheDirInfo = parser.accepts("cache-dir").withOptionalArg().ofType(String.class);
        parser.accepts("minimal");
        parser.accepts("norender");
        parser.accepts("script").withRequiredArg().ofType(String.class);
        parser.accepts("scriptArgs").withRequiredArg().ofType(String.class);
        OptionSet options = parser.parse(args);
        if (options.has("account")) {
            String[] details = ((String)options.valueOf((OptionSpec)accInfo)).split(":");
            GameAccount gameAccount = new GameAccount(details[0], details[1]);
            if (details.length >= 3) {
                gameAccount.setAuth(details[2]);
            }
            Game.setGameAccount(gameAccount);
        }
        if (options.has("scriptArgs")) {
            Static.setScriptArgs(((String)options.valueOf("scriptArgs")).split(","));
        }
        if (options.has("cache-dir")) {
            String cacheDir = (String)options.valueOf((OptionSpec)cacheDirInfo);
            if (cacheDir != null) {
                System.setProperty("unethicalite.cache-dir", cacheDir);
            } else {
                GameAccount acc = Game.getGameAccount();
                if (acc != null) {
                    System.setProperty("unethicalite.cache-dir", acc.getUsername());
                } else {
                    System.setProperty("unethicalite.cache-dir", UUID.randomUUID().toString());
                }
            }
        }
        return options;
    }

    public static void quickLaunch(MinimalPluginManager minimalPluginManager, OptionSet options) {
        if (options.has("script")) {
            String script = (String)options.valueOf("script");
            PluginEntry quickStartScript = minimalPluginManager.loadPlugins().stream().filter(x -> x.getScriptClass().getAnnotation(PluginDescriptor.class).name().equals(script)).findFirst().orElse(null);
            if (quickStartScript == null || !quickStartScript.isScript()) {
                return;
            }
            minimalPluginManager.startPlugin(quickStartScript);
        }
    }

    private void initArgs() {
        if (this.clientArgs.has("norender")) {
            this.configManager.setConfiguration("unethicalite", "renderOff", (Object)true);
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged e) {
        if (e.getGameState() == GameState.LOGIN_SCREEN && !this.initializedArgs) {
            this.initArgs();
            this.initializedArgs = true;
        }
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals("unethicalite")) {
            return;
        }
        if ("renderOff".equals(event.getKey())) {
            boolean enabled = Boolean.parseBoolean(event.getNewValue());
            this.client.setLowCpu(enabled);
            if (enabled) {
                this.client.setDrawCallbacks((DrawCallbacks)DISABLE_RENDERING);
            } else {
                this.client.setDrawCallbacks(null);
            }
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Key
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.World
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.game.WorldService
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.PluginDescriptor
 *  net.runelite.client.plugins.PluginInstantiationException
 *  net.runelite.client.plugins.PluginManager
 *  net.runelite.client.util.WorldUtil
 *  net.runelite.http.api.worlds.World
 *  net.runelite.http.api.worlds.WorldResult
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.managers;

import com.google.inject.Key;
import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import net.unethicalite.api.plugins.Plugins;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.client.config.UnethicaliteConfig;
import net.unethicalite.client.minimal.MinimalClient;
import net.unethicalite.client.minimal.plugins.MinimalClassLoader;
import net.unethicalite.client.minimal.plugins.MinimalPluginChanged;
import net.unethicalite.client.minimal.plugins.MinimalPluginState;
import net.unethicalite.client.minimal.plugins.PluginEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MinimalPluginManager {
    private static final Logger log = LoggerFactory.getLogger(MinimalPluginManager.class);
    private static final File PLUGINS_DIR = new File(MinimalClient.CLIENT_DIR, "plugins");
    @Inject
    private UnethicaliteConfig minimalConfig;
    @Inject
    private ExecutorService executorService;
    @Inject
    private Client client;
    @Inject
    private PluginManager pluginManager;
    @Inject
    private ConfigManager configManager;
    @Inject
    private WorldService worldService;
    private PluginEntry pluginEntry = null;
    private Plugin plugin = null;
    private Config config = null;
    private boolean worldSet;

    public List<PluginEntry> loadPlugins() {
        return this.loadPlugins(PLUGINS_DIR);
    }

    public List<PluginEntry> loadPlugins(File dir) {
        ArrayList<PluginEntry> plugins = new ArrayList<PluginEntry>();
        try {
            File[] files = dir.listFiles();
            if (files == null) {
                return plugins;
            }
            for (File file : files) {
                if (file.isDirectory() || !file.getName().endsWith(".jar")) continue;
                try (JarFile jar = new JarFile(file);
                     MinimalClassLoader ucl = new MinimalClassLoader(new URL[]{file.toURI().toURL()});){
                    Enumeration<JarEntry> elems = jar.entries();
                    while (elems.hasMoreElements()) {
                        JarEntry entry = elems.nextElement();
                        if (!entry.getName().endsWith(".class")) continue;
                        String name = entry.getName();
                        name = name.substring(0, name.length() - ".class".length()).replace('/', '.');
                        try {
                            Class<?> clazz = ucl.loadClass(name);
                            if (!Plugin.class.isAssignableFrom(clazz) || Modifier.isAbstract(clazz.getModifiers()) || clazz.getAnnotation(PluginDescriptor.class) == null) continue;
                            Class<?> scriptClass = clazz;
                            plugins.add(new PluginEntry(scriptClass, ((PluginDescriptor[])scriptClass.getAnnotationsByType(PluginDescriptor.class))[0], jar.getManifest()));
                        }
                        catch (Exception | NoClassDefFoundError e) {
                            log.error("Failed to load class: {}", (Object)name, (Object)e);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return plugins;
    }

    public void startPlugin(PluginEntry entry) {
        try {
            this.plugin = this.pluginManager.loadPlugins(List.of(entry.getScriptClass()), null).stream().findFirst().orElse(null);
            if (this.plugin == null) {
                return;
            }
            log.debug("Loaded plugin: {}", (Object)this.plugin.getName());
            if (!Plugins.startPlugin(this.plugin)) {
                return;
            }
            this.pluginEntry = entry;
            for (Key key : this.plugin.getInjector().getBindings().keySet()) {
                Class type = key.getTypeLiteral().getRawType();
                if (!Config.class.isAssignableFrom(type) || !type.getPackageName().startsWith(this.plugin.getClass().getPackageName())) continue;
                this.config = (Config)this.plugin.getInjector().getInstance(key);
                this.configManager.setDefaultConfiguration(this.config, false);
            }
            this.client.getCallbacks().post((Object)new MinimalPluginChanged(this.plugin, MinimalPluginState.STARTED));
        }
        catch (PluginInstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopPlugin() {
        if (!Plugins.stopPlugin(this.plugin)) {
            return;
        }
        this.client.getCallbacks().post((Object)new MinimalPluginChanged(this.plugin, MinimalPluginState.STOPPED));
        this.plugin = null;
        this.pluginEntry = null;
        this.config = null;
    }

    public void restartPlugin() {
        String currentPlugin = this.pluginEntry.toString();
        this.stopPlugin();
        List<PluginEntry> plugins = this.loadPlugins();
        PluginEntry newEntry = plugins.stream().filter(e -> e.toString().equals(currentPlugin)).findFirst().orElse(null);
        if (newEntry == null) {
            return;
        }
        this.startPlugin(newEntry);
    }

    public void pauseScript() {
        if (this.plugin == null || !(this.plugin instanceof Script)) {
            return;
        }
        ((Script)this.plugin).pauseScript();
    }

    public boolean isScriptRunning() {
        return this.plugin != null && this.plugin instanceof Script;
    }

    @Subscribe
    private void onMinimalPluginChanged(MinimalPluginChanged e) {
        log.info("Minimal Plugin state changed: {} [{}]", (Object)e.getPlugin().getName(), (Object)e.getState());
        if (e.getState() == MinimalPluginState.RESTARTING) {
            this.executorService.execute(this::restartPlugin);
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged e) {
        if (this.worldSet || e.getGameState() != GameState.LOGIN_SCREEN) {
            return;
        }
        Optional<Integer> worldArg = Optional.ofNullable(System.getProperty("cli.world")).map(Integer::parseInt);
        worldArg.ifPresent(this::setWorld);
        this.worldSet = true;
    }

    private void setWorld(int cliWorld) {
        int correctedWorld;
        int n = correctedWorld = cliWorld < 300 ? cliWorld + 300 : cliWorld;
        if (correctedWorld <= 300 || this.client.getWorld() == correctedWorld) {
            return;
        }
        WorldResult worldResult = this.worldService.getWorlds();
        if (worldResult == null) {
            log.warn("Failed to lookup worlds.");
            return;
        }
        World world = worldResult.findWorld(correctedWorld);
        if (world != null) {
            net.runelite.api.World rsWorld = this.client.createWorld();
            rsWorld.setActivity(world.getActivity());
            rsWorld.setAddress(world.getAddress());
            rsWorld.setId(world.getId());
            rsWorld.setPlayerCount(world.getPlayers());
            rsWorld.setLocation(world.getLocation());
            rsWorld.setTypes(WorldUtil.toWorldTypes((EnumSet)world.getTypes()));
            this.client.changeWorld(rsWorld);
            log.debug("Applied new world {}", (Object)correctedWorld);
        } else {
            log.warn("World {} not found.", (Object)correctedWorld);
        }
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    public Config getConfig() {
        return this.config;
    }
}


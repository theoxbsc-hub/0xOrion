/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  com.google.common.util.concurrent.ThreadFactoryBuilder
 *  com.google.gson.Gson
 *  com.google.inject.AbstractModule
 *  com.google.inject.Provides
 *  com.google.inject.name.Names
 *  com.openosrs.client.config.OpenOSRSConfig
 *  javax.annotation.Nullable
 *  javax.inject.Named
 *  javax.inject.Singleton
 *  joptsimple.OptionSet
 *  net.runelite.api.Client
 *  net.runelite.api.hooks.Callbacks
 *  net.runelite.api.packets.ClientPacket
 *  net.runelite.api.packets.ServerPacket
 *  net.runelite.client.NonScheduledExecutorServiceExceptionLogger
 *  net.runelite.client.RuneLiteProperties
 *  net.runelite.client.chat.ChatMessageManager
 *  net.runelite.client.config.ChatColorConfig
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.config.RuneLiteConfig
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.game.ItemManager
 *  net.runelite.client.menus.MenuManager
 *  net.runelite.client.task.Scheduler
 *  net.runelite.client.util.DeferredEventBus
 *  net.runelite.client.util.ExecutorServiceExceptionLogger
 *  net.runelite.http.api.RuneLiteAPI
 *  okhttp3.HttpUrl
 *  okhttp3.OkHttpClient
 */
package net.unethicalite.client.minimal;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.openosrs.client.config.OpenOSRSConfig;
import java.applet.Applet;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;
import joptsimple.OptionSet;
import net.runelite.api.Client;
import net.runelite.api.hooks.Callbacks;
import net.runelite.api.packets.ClientPacket;
import net.runelite.api.packets.ServerPacket;
import net.runelite.client.NonScheduledExecutorServiceExceptionLogger;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ChatColorConfig;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.task.Scheduler;
import net.runelite.client.util.DeferredEventBus;
import net.runelite.client.util.ExecutorServiceExceptionLogger;
import net.runelite.http.api.RuneLiteAPI;
import net.unethicalite.api.movement.pathfinder.GlobalCollisionMap;
import net.unethicalite.client.Static;
import net.unethicalite.client.config.UnethicaliteConfig;
import net.unethicalite.client.config.UnethicaliteProperties;
import net.unethicalite.client.minimal.MinimalHooks;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

@Singleton
public class MinimalModule
extends AbstractModule {
    private final boolean developerMode;
    private final OkHttpClient okHttpClient;
    private final Supplier<Applet> clientLoader;
    private final File config;
    private final OptionSet optionSet;

    protected void configure() {
        Properties properties = RuneLiteProperties.getProperties();
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            this.bindConstant().annotatedWith((Annotation)Names.named((String)key)).to(value);
        }
        this.bind(OptionSet.class).annotatedWith((Annotation)Names.named((String)"clientArgs")).toInstance((Object)this.optionSet);
        this.bindConstant().annotatedWith((Annotation)Names.named((String)"developerMode")).to(this.developerMode);
        this.bindConstant().annotatedWith((Annotation)Names.named((String)"safeMode")).to(false);
        this.bind(File.class).annotatedWith((Annotation)Names.named((String)"config")).toInstance((Object)this.config);
        this.bind(ScheduledExecutorService.class).toInstance((Object)new ExecutorServiceExceptionLogger(Executors.newSingleThreadScheduledExecutor()));
        this.bind(OkHttpClient.class).toInstance((Object)this.okHttpClient);
        this.bind(MenuManager.class);
        this.bind(ChatMessageManager.class);
        this.bind(ItemManager.class);
        this.bind(Scheduler.class);
        this.bind(Gson.class).toInstance((Object)RuneLiteAPI.GSON);
        this.bind(Callbacks.class).to(MinimalHooks.class);
        this.bind(EventBus.class).toInstance((Object)new EventBus());
        this.bind(EventBus.class).annotatedWith((Annotation)Names.named((String)"Deferred EventBus")).to(DeferredEventBus.class);
        this.requestStaticInjection(new Class[]{Static.class});
        Properties unethicaliteProperties = UnethicaliteProperties.getProperties();
        for (String key : unethicaliteProperties.stringPropertyNames()) {
            String value = unethicaliteProperties.getProperty(key);
            this.bindConstant().annotatedWith((Annotation)Names.named((String)key)).to(value);
        }
    }

    @Provides
    @Singleton
    Applet provideApplet() {
        return this.clientLoader.get();
    }

    @Provides
    @Singleton
    Client provideClient(@Nullable Applet applet) {
        return applet instanceof Client ? (Client)applet : null;
    }

    @Provides
    @Singleton
    RuneLiteConfig provideConfig(ConfigManager configManager) {
        return (RuneLiteConfig)configManager.getConfig(RuneLiteConfig.class);
    }

    @Provides
    @Singleton
    ChatColorConfig provideChatColorConfig(ConfigManager configManager) {
        return (ChatColorConfig)configManager.getConfig(ChatColorConfig.class);
    }

    @Provides
    @Named(value="runelite.api.base")
    HttpUrl provideApiBase(@Named(value="runelite.api.base") String s) {
        String prop = System.getProperty("runelite.http-service.url");
        return HttpUrl.get((String)(Strings.isNullOrEmpty((String)prop) ? s : prop));
    }

    @Provides
    @Named(value="runelite.session")
    HttpUrl provideSession(@Named(value="runelite.session") String s) {
        String prop = System.getProperty("runelite.session.url");
        return HttpUrl.get((String)(Strings.isNullOrEmpty((String)prop) ? s : prop));
    }

    @Provides
    @Named(value="runelite.static.base")
    HttpUrl provideStaticBase(@Named(value="runelite.static.base") String s) {
        String prop = System.getProperty("runelite.static.url");
        return HttpUrl.get((String)(Strings.isNullOrEmpty((String)prop) ? s : prop));
    }

    @Provides
    @Singleton
    OpenOSRSConfig provideOpenOSRSConfig(ConfigManager configManager) {
        return (OpenOSRSConfig)configManager.getConfig(OpenOSRSConfig.class);
    }

    @Provides
    @Singleton
    ExecutorService provideExecutorService() {
        int poolSize = 2 * Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(poolSize, poolSize, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactoryBuilder().setNameFormat("worker-%d").build());
        executor.allowCoreThreadTimeOut(true);
        return new NonScheduledExecutorServiceExceptionLogger((ExecutorService)executor);
    }

    @Provides
    @Singleton
    @Nullable
    ClientPacket provideClientPacket(@Nullable Client client) {
        assert (client != null);
        return client.createClientPacket(-1, -1);
    }

    @Provides
    @Singleton
    @Nullable
    ServerPacket provideServerPacket(@Nullable Client client) {
        assert (client != null);
        return client.createServerPacket(-1, -1);
    }

    @Provides
    @Singleton
    GlobalCollisionMap provideGlobalCollisionMap(@Named(value="unethicalite.api.url") String apiUrl) throws IOException {
        return GlobalCollisionMap.fetchFromUrl(apiUrl + "/regions");
    }

    @Provides
    @Singleton
    UnethicaliteConfig provideUnethicaliteConfig(ConfigManager configManager) {
        return (UnethicaliteConfig)configManager.getConfig(UnethicaliteConfig.class);
    }

    public MinimalModule(boolean developerMode, OkHttpClient okHttpClient, Supplier<Applet> clientLoader, File config, OptionSet optionSet) {
        this.developerMode = developerMode;
        this.okHttpClient = okHttpClient;
        this.clientLoader = clientLoader;
        this.config = config;
        this.optionSet = optionSet;
    }
}


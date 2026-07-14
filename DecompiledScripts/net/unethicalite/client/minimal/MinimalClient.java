/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  ch.qos.logback.classic.Level
 *  ch.qos.logback.classic.Logger
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.inject.Guice
 *  com.google.inject.Inject
 *  com.google.inject.Injector
 *  com.google.inject.Module
 *  com.openosrs.client.OpenOSRS
 *  javax.annotation.Nullable
 *  javax.inject.Provider
 *  javax.inject.Singleton
 *  joptsimple.ArgumentAcceptingOptionSpec
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  joptsimple.ValueConversionException
 *  joptsimple.ValueConverter
 *  net.runelite.api.Client
 *  net.runelite.api.Constants
 *  net.runelite.client.ClassPreloader
 *  net.runelite.client.ClientSessionManager
 *  net.runelite.client.RuneLite
 *  net.runelite.client.RuneLiteProperties
 *  net.runelite.client.RuntimeConfigLoader
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.game.WorldService
 *  net.runelite.client.rs.ClientLoader
 *  net.runelite.client.rs.ClientUpdateCheckMode
 *  net.runelite.client.ui.DrawManager
 *  net.runelite.client.ui.FatalErrorDialog
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 *  net.runelite.client.ui.overlay.tooltip.TooltipOverlay
 *  net.runelite.http.api.RuneLiteAPI
 *  okhttp3.Cache
 *  okhttp3.OkHttpClient
 *  okhttp3.OkHttpClient$Builder
 *  okhttp3.Request
 *  okhttp3.Response
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.minimal;

import ch.qos.logback.classic.Level;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.openosrs.client.OpenOSRS;
import java.applet.Applet;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.SwingUtilities;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.client.ClassPreloader;
import net.runelite.client.ClientSessionManager;
import net.runelite.client.RuneLite;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.RuntimeConfigLoader;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.WorldService;
import net.runelite.client.rs.ClientLoader;
import net.runelite.client.rs.ClientUpdateCheckMode;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.FatalErrorDialog;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.tooltip.TooltipOverlay;
import net.runelite.http.api.RuneLiteAPI;
import net.unethicalite.client.Orion;
import net.unethicalite.client.config.UnethicaliteConfig;
import net.unethicalite.client.managers.MinimalFpsManager;
import net.unethicalite.client.managers.MinimalPluginManager;
import net.unethicalite.client.managers.SettingsManager;
import net.unethicalite.client.minimal.MinimalModule;
import net.unethicalite.client.minimal.ui.MinimalToolbar;
import net.unethicalite.client.minimal.ui.MinimalUI;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MinimalClient {
    private static final Logger log = LoggerFactory.getLogger(MinimalClient.class);
    public static final File CLIENT_DIR = Orion.CLIENT_DIR;
    public static final File CACHE_DIR = new File(CLIENT_DIR, "cache");
    public static final File LOGS_DIR = new File(CLIENT_DIR, "logs");
    public static final File DEFAULT_CONFIG_FILE = new File(CLIENT_DIR, "settings.properties");
    public static final File DATA_DIR = new File(CLIENT_DIR, "data");
    public static final File SCRIPTS_DIR = new File(CLIENT_DIR, "scripts");
    private static final int MAX_OKHTTP_CACHE_SIZE = 0x1400000;
    @Inject
    private EventBus eventBus;
    @Inject
    private ConfigManager configManager;
    @Inject
    private MinimalUI minimalUI;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private Provider<TooltipOverlay> tooltipOverlay;
    @Inject
    @Nullable
    private Client client;
    @Inject
    private MinimalToolbar minimalToolbar;
    @Inject
    @Nullable
    private Applet applet;
    @Inject
    private MinimalPluginManager minimalPluginManager;
    @Inject
    private DrawManager drawManager;
    @Inject
    private MinimalFpsManager minimalFpsManager;
    @Inject
    private UnethicaliteConfig minimalConfig;
    @Inject
    private WorldService worldService;
    @Inject
    private ClientSessionManager clientSessionManager;

    public static void main(String[] args) throws Exception {
        OkHttpClient okHttpClient;
        Locale.setDefault(Locale.ENGLISH);
        OptionParser parser = new OptionParser();
        parser.accepts("developer-mode", "Enable developer tools");
        parser.accepts("debug", "Show extra debugging output");
        parser.accepts("insecure-skip-tls-verification", "Disables TLS verification");
        parser.accepts("jav_config", "jav_config url").withRequiredArg().defaultsTo((Object)RuneLiteProperties.getJavConfig(), (Object[])new String[0]);
        ArgumentAcceptingOptionSpec proxyInfo = parser.accepts("proxy").withRequiredArg().ofType(String.class);
        ArgumentAcceptingOptionSpec worldInfo = parser.accepts("world").withRequiredArg().ofType(Integer.class);
        ArgumentAcceptingOptionSpec configfile = parser.accepts("runelite", "Use a specified config file").withRequiredArg().withValuesConvertedBy((ValueConverter)new ConfigFileConverter()).defaultsTo((Object)DEFAULT_CONFIG_FILE, (Object[])new File[0]);
        OptionSet options = SettingsManager.parseArgs(parser, args);
        if (options.has("debug")) {
            ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger((String)"ROOT");
            logger.setLevel(Level.DEBUG);
        }
        if (options.has("proxy")) {
            String[] proxy = ((String)options.valueOf((OptionSpec)proxyInfo)).split(":");
            if (proxy.length >= 2) {
                System.setProperty("socksProxyHost", proxy[0]);
                System.setProperty("socksProxyPort", proxy[1]);
            }
            if (proxy.length >= 4) {
                System.setProperty("java.net.socks.username", proxy[2]);
                System.setProperty("java.net.socks.password", proxy[3]);
                final String user = proxy[2];
                final char[] pass = proxy[3].toCharArray();
                Authenticator.setDefault(new Authenticator(){
                    private final PasswordAuthentication auth;
                    {
                        this.auth = new PasswordAuthentication(user, pass);
                    }

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return this.auth;
                    }
                });
            }
        }
        if (options.has("world")) {
            int world = (Integer)options.valueOf((OptionSpec)worldInfo);
            System.setProperty("cli.world", String.valueOf(world));
        }
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            log.error("Uncaught exception:", throwable);
            if (throwable instanceof AbstractMethodError) {
                log.error("Classes are out of date; Build with maven again.");
            }
        });
        OpenOSRS.preload();
        RuneLiteAPI.CLIENT = okHttpClient = MinimalClient.buildHttpClient(options.has("insecure-skip-tls-verification"));
        try {
            RuntimeConfigLoader runtimeConfigLoader = new RuntimeConfigLoader(okHttpClient);
            ClientLoader clientLoader = new ClientLoader(okHttpClient, ClientUpdateCheckMode.AUTO, runtimeConfigLoader, (String)options.valueOf("jav_config"));
            new Thread(() -> {
                clientLoader.get();
                ClassPreloader.preload();
            }, "Preloader").start();
            log.info("Orion {} (RuneLite version {}, launcher version {}) starting up, args: {}", new Object[]{OpenOSRS.SYSTEM_VERSION, RuneLiteProperties.getVersion() == null ? "unknown" : RuneLiteProperties.getVersion(), RuneLiteProperties.getLauncherVersion(), args.length == 0 ? "none" : String.join((CharSequence)" ", args)});
            long start = System.currentTimeMillis();
            RuneLite.setInjector((Injector)Guice.createInjector((Module[])new Module[]{new MinimalModule(options.has("developer-mode"), okHttpClient, (Supplier<Applet>)clientLoader, (File)options.valueOf((OptionSpec)configfile), options)}));
            ((MinimalClient)RuneLite.getInjector().getInstance(MinimalClient.class)).start(options);
            long end = System.currentTimeMillis();
            RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
            long uptime = rb.getUptime();
            log.info("Client initialization took {}ms. Uptime: {}ms", (Object)(end - start), (Object)uptime);
        }
        catch (Exception e) {
            log.error("Failure during startup", (Throwable)e);
            SwingUtilities.invokeLater(() -> new FatalErrorDialog("Orion has encountered an unexpected error during startup.").open());
        }
    }

    @VisibleForTesting
    static OkHttpClient buildHttpClient(boolean insecureSkipTlsVerification) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder().pingInterval(30L, TimeUnit.SECONDS).addNetworkInterceptor(chain -> {
            Request userAgentRequest = chain.request().newBuilder().header("User-Agent", RuneLite.USER_AGENT).build();
            return chain.proceed(userAgentRequest);
        }).cache(new Cache(new File(CACHE_DIR, "okhttp"), 0x1400000L)).addNetworkInterceptor(chain -> {
            Response res = chain.proceed(chain.request());
            if (res.code() >= 400 && "GET".equals(res.request().method())) {
                res = res.newBuilder().header("Cache-Control", "no-store").build();
            }
            return res;
        });
        if (insecureSkipTlsVerification || RuneLiteProperties.isInsecureSkipTlsVerification()) {
            MinimalClient.setupInsecureTrustManager(builder);
        }
        return builder.build();
    }

    private static void setupInsecureTrustManager(OkHttpClient.Builder okHttpClientBuilder) {
        try {
            X509TrustManager trustManager = new X509TrustManager(){

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            okHttpClientBuilder.sslSocketFactory(sc.getSocketFactory(), trustManager);
        }
        catch (KeyManagementException | NoSuchAlgorithmException ex) {
            log.warn("unable to setup insecure trust manager", (Throwable)ex);
        }
    }

    private static void copyJagexCache() {
        Path from = Paths.get(System.getProperty("user.home"), "jagexcache");
        Path to = Paths.get(Orion.getCacheDirectory().getAbsolutePath(), "jagexcache");
        if (Files.exists(to, new LinkOption[0]) || !Files.exists(from, new LinkOption[0])) {
            return;
        }
        log.info("Copying jagexcache from {} to {}", (Object)from, (Object)to);
        try (Stream<Path> stream = Files.walk(from, new FileVisitOption[0]);){
            stream.forEach(source -> {
                try {
                    Files.copy(source, to.resolve(from.relativize((Path)source)), StandardCopyOption.COPY_ATTRIBUTES);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        catch (Exception e) {
            log.warn("unable to copy jagexcache", (Throwable)e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void start(OptionSet options) throws Exception {
        boolean isOutdated;
        boolean bl = isOutdated = this.client == null;
        if (!isOutdated) {
            RuneLite.getInjector().injectMembers((Object)this.client);
        }
        if (this.applet != null) {
            MinimalClient.copyJagexCache();
            this.applet.setSize(Constants.GAME_FIXED_SIZE);
            String oldHome = System.setProperty("user.home", Orion.getCacheDirectory().getAbsolutePath());
            try {
                this.applet.init();
            }
            finally {
                System.setProperty("user.home", oldHome);
            }
            this.applet.start();
        }
        this.configManager.load();
        this.minimalToolbar.init();
        this.drawManager.registerEveryFrameListener((Runnable)this.minimalFpsManager);
        this.minimalFpsManager.reloadConfig(this.minimalConfig.fpsLimit());
        this.eventBus.register((Object)this.minimalToolbar);
        this.eventBus.register((Object)this.minimalPluginManager);
        this.clientSessionManager.start();
        this.eventBus.register((Object)this.clientSessionManager);
        this.minimalUI.init();
        this.eventBus.register((Object)this.minimalUI);
        this.eventBus.register((Object)this.overlayManager);
        this.eventBus.register((Object)this.configManager);
        this.overlayManager.add((Overlay)this.tooltipOverlay.get());
        this.minimalUI.show();
        if (options.has("script")) {
            SettingsManager.quickLaunch(this.minimalPluginManager, options);
        }
    }

    static {
        System.setProperty("sun.java2d.uiScale", "1.0");
        String launcherVersion = System.getProperty("launcher.version");
        System.setProperty("runelite.launcher.version", launcherVersion == null ? "unknown" : launcherVersion);
        CLIENT_DIR.mkdirs();
        SCRIPTS_DIR.mkdirs();
        DATA_DIR.mkdirs();
    }

    private static class ConfigFileConverter
    implements ValueConverter<File> {
        private ConfigFileConverter() {
        }

        public File convert(String fileName) {
            File file = Paths.get(fileName, new String[0]).isAbsolute() || fileName.startsWith("./") || fileName.startsWith(".\\") ? new File(fileName) : new File(CLIENT_DIR, fileName);
            if (!(!file.exists() || file.isFile() && file.canWrite())) {
                throw new ValueConversionException(String.format("File %s is not accessible", file.getAbsolutePath()));
            }
            return file;
        }

        public Class<? extends File> valueType() {
            return File.class;
        }

        public String valuePattern() {
            return null;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Binder
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.client.RuneLite
 *  net.runelite.client.callback.ClientThread
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.plugins.PluginDescriptor
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.runelite.client.plugins.svc.autologin;

import com.google.inject.Binder;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.autologin.AutoLoginConfig;
import net.runelite.client.plugins.svc.autologin.ProfileSlot;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.plugins.svc.core.ShieldLoginGate;
import net.unethicalite.api.input.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(name="<html><!--1--><nobr><font color='#FFB000'>\u25cf x64 Auto Login</font></nobr></html>", description="Automatically re-logs in when disconnected", tags={"orion", "login", "auto", "proxy"}, enabledByDefault=false)
public class AutoLoginPlugin
extends OrionLoopedPlugin {
    private static final Logger log = LoggerFactory.getLogger(AutoLoginPlugin.class);
    private static final int IDX_LANDING = 0;
    private static final int IDX_CREDENTIALS = 2;
    @Inject
    private Client client;
    @Inject
    private AutoLoginConfig config;
    @Inject
    private ClientThread clientThread;
    @Inject
    private ConfigManager configManager;
    private long loginScreenSeenAt = 0L;
    private ProfileSlot boundProfile;

    public void configure(Binder binder) {
        ConfigManager configManager = (ConfigManager)RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(AutoLoginConfig.class).toInstance((Object)((AutoLoginConfig)configManager.getConfig(AutoLoginConfig.class)));
    }

    @Override
    protected void onStart() {
        this.bindLaunchProxyToProfile();
    }

    private void bindLaunchProxyToProfile() {
        this.boundProfile = null;
        if (!this.config.bindProfileToProxy()) {
            return;
        }
        String string = AutoLoginPlugin.trimToNull(System.getProperty("orion.proxy.label"));
        String string2 = AutoLoginPlugin.trimToNull(System.getProperty("orion.proxy"));
        if (string == null && string2 == null) {
            return;
        }
        ProfileSlot profileSlot = this.resolveProfileForProxy(string, string2);
        if (profileSlot == null) {
            log.info("Auto Login: launch proxy '{}' is not assigned to any profile; using {}", (Object)(string != null ? string : string2), (Object)this.config.activeProfile());
            return;
        }
        this.boundProfile = profileSlot;
        if (this.config.activeProfile() != profileSlot) {
            this.configManager.setConfiguration("autoLogin", "activeProfile", (Object)profileSlot);
        }
        log.info("Auto Login: bound launch proxy '{}' -> {}", (Object)(string != null ? string : string2), (Object)profileSlot);
    }

    private ProfileSlot resolveProfileForProxy(String string, String string2) {
        String string3 = AutoLoginPlugin.hostOf(string2);
        for (ProfileSlot profileSlot : ProfileSlot.values()) {
            String string4 = AutoLoginPlugin.trimToNull(this.proxyOf(profileSlot));
            if (string4 == null || !(string != null && string4.equalsIgnoreCase(string) || string2 != null && string4.equalsIgnoreCase(string2)) && (string3 == null || !string4.equalsIgnoreCase(string3))) continue;
            return profileSlot;
        }
        return null;
    }

    private static String hostOf(String string) {
        if (string == null) {
            return null;
        }
        int n = string.indexOf(58);
        String string2 = (n > 0 ? string.substring(0, n) : string).trim();
        return string2.isEmpty() ? null : string2;
    }

    @Override
    protected int loop() {
        long l;
        if (this.client.getGameState() != GameState.LOGIN_SCREEN) {
            this.loginScreenSeenAt = 0L;
            return 1000;
        }
        if (ShieldLoginGate.isBlocked()) {
            this.loginScreenSeenAt = 0L;
            long l2 = ShieldLoginGate.remainingMs();
            return (int)Math.max(1000L, Math.min(l2, 5000L));
        }
        String string = this.activeUsername();
        String string2 = this.activePassword();
        string = string == null ? "" : string.trim();
        String string3 = string2 = string2 == null ? "" : string2;
        if (string.isEmpty() || string2.isEmpty()) {
            return 1500;
        }
        if (AutoLoginPlugin.looksRejected(this.client.getLoginMessage())) {
            return 30000;
        }
        long l3 = System.currentTimeMillis();
        if (this.loginScreenSeenAt == 0L) {
            this.loginScreenSeenAt = l3;
        }
        if ((l = (long)this.config.loginDelay() * 1000L - (l3 - this.loginScreenSeenAt)) > 0L) {
            return (int)Math.max(50L, Math.min(l, 500L));
        }
        this.submitLogin(string, string2);
        return 5000;
    }

    private void submitLogin(String string, String string2) {
        this.clientThread.invokeLater(() -> {
            if (this.client.getGameState() != GameState.LOGIN_SCREEN) {
                return;
            }
            if (this.client.getLoginIndex() == 0) {
                this.client.setLoginIndex(2);
            }
            this.client.setUsername(string);
            this.client.setPassword(string2);
            Keyboard.sendEnter();
        });
    }

    private ProfileSlot effectiveProfile() {
        if (this.config.bindProfileToProxy() && this.boundProfile != null) {
            return this.boundProfile;
        }
        return this.config.activeProfile();
    }

    private String activeUsername() {
        return this.usernameOf(this.effectiveProfile());
    }

    private String activePassword() {
        return this.passwordOf(this.effectiveProfile());
    }

    private String usernameOf(ProfileSlot profileSlot) {
        switch (profileSlot) {
            case PROFILE_2: {
                return this.config.profile2Username();
            }
            case PROFILE_3: {
                return this.config.profile3Username();
            }
            case PROFILE_4: {
                return this.config.profile4Username();
            }
            case PROFILE_5: {
                return this.config.profile5Username();
            }
            case PROFILE_6: {
                return this.config.profile6Username();
            }
            case PROFILE_7: {
                return this.config.profile7Username();
            }
            case PROFILE_8: {
                return this.config.profile8Username();
            }
            case PROFILE_9: {
                return this.config.profile9Username();
            }
            case PROFILE_10: {
                return this.config.profile10Username();
            }
            case PROFILE_11: {
                return this.config.profile11Username();
            }
            case PROFILE_12: {
                return this.config.profile12Username();
            }
            case PROFILE_13: {
                return this.config.profile13Username();
            }
            case PROFILE_14: {
                return this.config.profile14Username();
            }
            case PROFILE_15: {
                return this.config.profile15Username();
            }
            case PROFILE_16: {
                return this.config.profile16Username();
            }
            case PROFILE_17: {
                return this.config.profile17Username();
            }
            case PROFILE_18: {
                return this.config.profile18Username();
            }
            case PROFILE_19: {
                return this.config.profile19Username();
            }
            case PROFILE_20: {
                return this.config.profile20Username();
            }
        }
        return this.config.profile1Username();
    }

    private String passwordOf(ProfileSlot profileSlot) {
        switch (profileSlot) {
            case PROFILE_2: {
                return this.config.profile2Password();
            }
            case PROFILE_3: {
                return this.config.profile3Password();
            }
            case PROFILE_4: {
                return this.config.profile4Password();
            }
            case PROFILE_5: {
                return this.config.profile5Password();
            }
            case PROFILE_6: {
                return this.config.profile6Password();
            }
            case PROFILE_7: {
                return this.config.profile7Password();
            }
            case PROFILE_8: {
                return this.config.profile8Password();
            }
            case PROFILE_9: {
                return this.config.profile9Password();
            }
            case PROFILE_10: {
                return this.config.profile10Password();
            }
            case PROFILE_11: {
                return this.config.profile11Password();
            }
            case PROFILE_12: {
                return this.config.profile12Password();
            }
            case PROFILE_13: {
                return this.config.profile13Password();
            }
            case PROFILE_14: {
                return this.config.profile14Password();
            }
            case PROFILE_15: {
                return this.config.profile15Password();
            }
            case PROFILE_16: {
                return this.config.profile16Password();
            }
            case PROFILE_17: {
                return this.config.profile17Password();
            }
            case PROFILE_18: {
                return this.config.profile18Password();
            }
            case PROFILE_19: {
                return this.config.profile19Password();
            }
            case PROFILE_20: {
                return this.config.profile20Password();
            }
        }
        return this.config.profile1Password();
    }

    private String proxyOf(ProfileSlot profileSlot) {
        switch (profileSlot) {
            case PROFILE_2: {
                return this.config.profile2Proxy();
            }
            case PROFILE_3: {
                return this.config.profile3Proxy();
            }
            case PROFILE_4: {
                return this.config.profile4Proxy();
            }
            case PROFILE_5: {
                return this.config.profile5Proxy();
            }
            case PROFILE_6: {
                return this.config.profile6Proxy();
            }
            case PROFILE_7: {
                return this.config.profile7Proxy();
            }
            case PROFILE_8: {
                return this.config.profile8Proxy();
            }
            case PROFILE_9: {
                return this.config.profile9Proxy();
            }
            case PROFILE_10: {
                return this.config.profile10Proxy();
            }
            case PROFILE_11: {
                return this.config.profile11Proxy();
            }
            case PROFILE_12: {
                return this.config.profile12Proxy();
            }
            case PROFILE_13: {
                return this.config.profile13Proxy();
            }
            case PROFILE_14: {
                return this.config.profile14Proxy();
            }
            case PROFILE_15: {
                return this.config.profile15Proxy();
            }
            case PROFILE_16: {
                return this.config.profile16Proxy();
            }
            case PROFILE_17: {
                return this.config.profile17Proxy();
            }
            case PROFILE_18: {
                return this.config.profile18Proxy();
            }
            case PROFILE_19: {
                return this.config.profile19Proxy();
            }
            case PROFILE_20: {
                return this.config.profile20Proxy();
            }
        }
        return this.config.profile1Proxy();
    }

    private static String trimToNull(String string) {
        if (string == null) {
            return null;
        }
        return (string = string.trim()).isEmpty() ? null : string;
    }

    private static boolean looksRejected(String string) {
        if (string == null) {
            return false;
        }
        String string2 = string.toLowerCase();
        return string2.contains("invalid") || string2.contains("incorrect") || string2.contains("disabled") || string2.contains("banned") || string2.contains("not a member") || string2.contains("suspici");
    }
}


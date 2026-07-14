package net.runelite.client.plugins.ox.autologin;

import com.google.inject.Binder;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.ox.core.OxConsole;
import net.runelite.client.plugins.ox.core.OxLoginGate;
import net.runelite.client.plugins.ox.core.OxLoopedPlugin;
import net.runelite.client.plugins.ox.core.OxPluginRegistry;
import net.unethicalite.api.input.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
    name = "<html><nobr><font color='#B44DFF'>0xAutoLogin</font></nobr></html>",
    description = "Logs in with the account selected in Launch-0x (username/password). Re-logs after disconnect.",
    tags = {"0x", "login", "auto", "account"},
    enabledByDefault = true
)
public class AutoLoginPlugin extends OxLoopedPlugin {
    private static final Logger log = LoggerFactory.getLogger(AutoLoginPlugin.class);

    @Inject
    private Client client;
    @Inject
    private AutoLoginConfig config;
    @Inject
    private ClientThread clientThread;

    private long loginScreenSeenAt = 0L;
    private boolean loggedMissingCreds = false;
    private boolean loggedGateLift = false;

    @Override
    public void configure(Binder binder) {
        ConfigManager cm = RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(AutoLoginConfig.class).toInstance(cm.getConfig(AutoLoginConfig.class));
    }

    @Override
    protected void onStart() {
        String user = resolveUsername();
        OxPluginRegistry.register("0xAutoLogin");
        if (user.isEmpty()) {
            OxConsole.warn("AutoLogin", "no username - set Launch-0x account or fallback config");
        } else {
            OxConsole.info("AutoLogin", "ready  user=" + user);
        }
    }

    @Override
    protected int loop() {
        if (OxLoginGate.isBlocked()) {
            this.loginScreenSeenAt = 0L;
            this.loggedGateLift = false;
            long left = OxLoginGate.remainingMs();
            return (int) Math.max(1000L, Math.min(left, 5000L));
        }
        if (!this.loggedGateLift && !OxLoginGate.getReason().isEmpty()) {
            this.loggedGateLift = true;
            OxConsole.info("AutoLogin", "staff gate cleared (" + OxLoginGate.getReason() + ")");
        }

        if (this.client.getGameState() != GameState.LOGIN_SCREEN) {
            this.loginScreenSeenAt = 0L;
            return 1000;
        }

        String username = resolveUsername();
        String password = resolvePassword();
        if (username.isEmpty() || password.isEmpty()) {
            if (!this.loggedMissingCreds) {
                OxConsole.warn("AutoLogin", "waiting for credentials");
                this.loggedMissingCreds = true;
            }
            return 1500;
        }
        this.loggedMissingCreds = false;

        if (looksRejected(this.client.getLoginMessage())) {
            return 30000;
        }

        long now = System.currentTimeMillis();
        if (this.loginScreenSeenAt == 0L) {
            this.loginScreenSeenAt = now;
        }
        long waitMs = (long) this.config.loginDelay() * 1000L - (now - this.loginScreenSeenAt);
        if (waitMs > 0L) {
            return (int) Math.max(50L, Math.min(waitMs, 500L));
        }

        submitLogin(username, password);
        return 5000;
    }

    private void submitLogin(String username, String password) {
        this.clientThread.invokeLater(() -> {
            if (this.client.getGameState() != GameState.LOGIN_SCREEN) {
                return;
            }
            if (this.client.getLoginIndex() == 0) {
                this.client.setLoginIndex(2);
            }
            this.client.setUsername(username);
            this.client.setPassword(password);
            Keyboard.sendEnter();
            OxConsole.info("AutoLogin", "submitted login for " + username);
        });
    }

    private String resolveUsername() {
        if (this.config.useLaunchAccount()) {
            String fromLaunch = prop("ox.username");
            if (!fromLaunch.isEmpty()) {
                return fromLaunch;
            }
        }
        return trim(this.config.fallbackUsername());
    }

    private String resolvePassword() {
        if (this.config.useLaunchAccount()) {
            String fromLaunch = System.getProperty("ox.password", "");
            if (fromLaunch != null && !fromLaunch.isEmpty()) {
                return fromLaunch;
            }
        }
        String fallback = this.config.fallbackPassword();
        return fallback == null ? "" : fallback;
    }

    private static String prop(String key) {
        return trim(System.getProperty(key, ""));
    }

    private static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static boolean looksRejected(String message) {
        if (message == null) {
            return false;
        }
        String lower = message.toLowerCase();
        return lower.contains("invalid")
            || lower.contains("incorrect")
            || lower.contains("disabled")
            || lower.contains("banned")
            || lower.contains("not a member")
            || lower.contains("suspici");
    }
}

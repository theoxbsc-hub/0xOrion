package net.runelite.client.plugins.ox.notify;

import com.google.inject.Binder;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.ox.core.OxConsole;
import net.runelite.client.plugins.ox.core.OxNotify;
import net.runelite.client.plugins.ox.core.OxPluginRegistry;
import net.runelite.client.plugins.ox.core.OxScriptActivity;
import net.runelite.client.plugins.ox.info.OxRuntimeInfo;
import net.unethicalite.api.entities.Players;

@PluginDescriptor(
    name = "<html><nobr><font color='#B44DFF'>0xNotify</font></nobr></html>",
    description = "Discord webhook alerts: staff logout, disconnect, script breaks. Paste webhook URL in config.",
    tags = {"0x", "discord", "webhook", "notify", "alert", "staff"},
    enabledByDefault = false
)
public class DiscordNotifyPlugin extends Plugin implements OxNotify.Sink {
    @Inject
    private Client client;
    @Inject
    private DiscordNotifyConfig config;
    @Inject
    private ConfigManager configManager;

    private DiscordWebhookClient webhook;
    private GameState lastState = GameState.UNKNOWN;

    @Override
    public void configure(Binder binder) {
        ConfigManager cm = RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(DiscordNotifyConfig.class).toInstance(cm.getConfig(DiscordNotifyConfig.class));
    }

    @Override
    protected void startUp() {
        this.webhook = new DiscordWebhookClient();
        OxNotify.setSink(this);
        OxPluginRegistry.register("0xNotify");
        OxConsole.info("Notify", "Discord webhook sink armed"
            + (isUrlConfigured() ? "" : " (paste webhook URL in config)"));
    }

    @Override
    protected void shutDown() {
        OxNotify.clearSink(this);
        if (this.webhook != null) {
            this.webhook.shutdown();
            this.webhook = null;
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event == null || !"oxDiscordNotify".equals(event.getGroup())) {
            return;
        }
        if ("sendTestPing".equals(event.getKey()) && this.config.sendTestPing()) {
            OxNotify.test("manual test from config");
            try {
                this.configManager.setConfiguration("oxDiscordNotify", "sendTestPing", "false");
            } catch (Throwable ignored) {
                // ignore
            }
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event == null) {
            return;
        }
        GameState next = event.getGameState();
        GameState prev = this.lastState;
        this.lastState = next;

        if (prev == GameState.LOGGED_IN && next == GameState.LOGIN_SCREEN) {
            if (this.config.notifyLogout()) {
                OxNotify.loggedOut("returned to login screen");
            }
        } else if (next == GameState.LOGGED_IN && prev != GameState.LOGGED_IN) {
            if (this.config.notifyLogin()) {
                OxNotify.loggedIn(accountLabel());
            }
        }
    }

    @Override
    public void onNotify(OxNotify.Kind kind, String title, String detail) {
        if (!allows(kind) || this.webhook == null || !isUrlConfigured()) {
            return;
        }
        this.webhook.sendEmbed(
            this.config.webhookUrl().trim(),
            this.config.botName(),
            kind,
            title,
            detail,
            accountLabel(),
            locationLabel(),
            scriptsLabel(),
            this.config.minIntervalMs()
        );
    }

    private boolean allows(OxNotify.Kind kind) {
        switch (kind) {
            case STAFF_ALERT:
                return this.config.notifyStaffAlert();
            case LOGGED_OUT:
                return this.config.notifyLogout();
            case LOGGED_IN:
                return this.config.notifyLogin();
            case SCRIPT_BREAK:
                return this.config.notifyScriptBreak();
            case SCRIPT_STOP:
                return this.config.notifyScriptStop();
            case TEST:
            case INFO:
                return true;
            default:
                return true;
        }
    }

    private boolean isUrlConfigured() {
        try {
            String url = this.config.webhookUrl();
            return url != null && url.trim().startsWith("https://discord.com/api/webhooks/");
        } catch (Throwable t) {
            return false;
        }
    }

    private static String accountLabel() {
        try {
            return OxRuntimeInfo.read().getDisplayName();
        } catch (Throwable t) {
            return System.getProperty("ox.displayName", "-");
        }
    }

    private String locationLabel() {
        try {
            if (this.client.getGameState() != GameState.LOGGED_IN) {
                return "login screen";
            }
            WorldPoint wp = Players.getLocal() != null ? Players.getLocal().getWorldLocation() : null;
            if (wp == null) {
                return "-";
            }
            return wp.getX() + "," + wp.getY() + " P" + wp.getPlane();
        } catch (Throwable t) {
            return "-";
        }
    }

    private static String scriptsLabel() {
        try {
            Map<String, OxScriptActivity.Snapshot> all = OxScriptActivity.all();
            if (all.isEmpty()) {
                return "(none)";
            }
            StringBuilder sb = new StringBuilder();
            for (OxScriptActivity.Snapshot s : all.values()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(s.displayName);
                String st = s.stats != null ? s.stats.get("State") : null;
                if (st != null && !st.isEmpty()) {
                    sb.append(" [").append(st).append("]");
                }
            }
            return sb.toString();
        } catch (Throwable t) {
            return "-";
        }
    }
}

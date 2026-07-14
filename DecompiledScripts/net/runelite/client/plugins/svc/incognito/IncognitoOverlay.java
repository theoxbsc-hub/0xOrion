/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.client.ui.overlay.OverlayPanel
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.components.LineComponent
 *  net.runelite.client.ui.overlay.components.TitleComponent
 */
package net.runelite.client.plugins.svc.incognito;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.client.plugins.svc.incognito.IncognitoConfig;
import net.runelite.client.plugins.svc.incognito.IncognitoPlugin;
import net.runelite.client.plugins.svc.incognito.PlayerWatcher;
import net.runelite.client.plugins.svc.incognito.ShieldReaction;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class IncognitoOverlay
extends OverlayPanel {
    private static final Color SHIELD_GREEN = new Color(0, 200, 80);
    private static final Color ACTIVE_COLOR = new Color(80, 220, 120);
    private static final Color FAILED_COLOR = new Color(255, 80, 80);
    private static final Color WARN_COLOR = new Color(255, 200, 60);
    private static final Color LABEL_COLOR = new Color(200, 200, 200);
    private static final Color DIM_COLOR = new Color(140, 140, 140);
    private final Client client;
    private final IncognitoPlugin plugin;
    private final IncognitoConfig config;

    IncognitoOverlay(Client client, IncognitoPlugin incognitoPlugin, IncognitoConfig incognitoConfig) {
        this.client = client;
        this.plugin = incognitoPlugin;
        this.config = incognitoConfig;
        this.setPosition(OverlayPosition.TOP_LEFT);
        this.setPreferredSize(new Dimension(260, 220));
    }

    public Dimension render(Graphics2D graphics2D) {
        if (!this.config.showOverlay()) {
            return null;
        }
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("INCOGNITO SHIELD").color(SHIELD_GREEN).build());
        this.addLine("Pre-Launch:", this.plugin.isPrelaunchActive() ? "ACTIVE" : "NOT RUNNING", LABEL_COLOR, this.plugin.isPrelaunchActive() ? ACTIVE_COLOR : FAILED_COLOR);
        this.addSpacer();
        this.addLine("--- SYSTEM PROPS ---", "", DIM_COLOR, DIM_COLOR);
        this.addLine("Username:", System.getProperty("user.name", "?"), LABEL_COLOR, ACTIVE_COLOR);
        this.addLine("Hostname:", this.plugin.getFakeHostname().isEmpty() ? "?" : this.plugin.getFakeHostname(), LABEL_COLOR, ACTIVE_COLOR);
        this.addLine("OS:", System.getProperty("os.name", "?") + " " + System.getProperty("os.version", ""), LABEL_COLOR, ACTIVE_COLOR);
        this.addLine("Java:", System.getProperty("java.version", "?"), LABEL_COLOR, ACTIVE_COLOR);
        this.addSpacer();
        this.addLine("--- GAME LOGIN (server sees) ---", "", DIM_COLOR, DIM_COLOR);
        boolean bl = this.plugin.isMacSpoofOk();
        this.addLine("MAC (live):", (String)(bl ? this.plugin.getLiveMac() : this.plugin.getLiveMac() + " LEAK!"), LABEL_COLOR, bl ? ACTIVE_COLOR : FAILED_COLOR);
        this.addLine("Java vendor:", this.plugin.getLiveJavaVendor(), LABEL_COLOR, ACTIVE_COLOR);
        this.addLine("Java ver:", this.plugin.getLiveJavaVersion(), LABEL_COLOR, ACTIVE_COLOR);
        this.addLine("Game UID:", this.plugin.getGameUid(), LABEL_COLOR, ACTIVE_COLOR);
        String string = this.plugin.getLiveCacheDir();
        this.addLine("Cache dir:", string, LABEL_COLOR, string != null && string.startsWith("orion-") ? ACTIVE_COLOR : WARN_COLOR);
        this.addLine("Revision:", this.plugin.getGameRevision() + " (shared)", LABEL_COLOR, WARN_COLOR);
        this.addSpacer();
        long l = this.plugin.getUptimeSeconds();
        String string2 = l < 60L ? l + "s" : (l < 3600L ? l / 60L + "m " + l % 60L + "s" : l / 3600L + "h " + l % 3600L / 60L + "m");
        this.addLine("Uptime:", string2, LABEL_COLOR, DIM_COLOR);
        this.renderPresence();
        this.renderPlayerWatch();
        return super.render(graphics2D);
    }

    private void renderPresence() {
        if (!this.config.showPresenceStats() || !this.plugin.isPresenceActive()) {
            return;
        }
        this.addSpacer();
        this.addLine("--- PRESENCE ---", "", DIM_COLOR, DIM_COLOR);
        boolean bl = this.plugin.getPresenceFocused();
        this.addLine("Focus:", this.config.keepFocused() ? (bl ? "true (forced)" : "true") : (bl ? "true" : "FALSE"), LABEL_COLOR, bl ? ACTIVE_COLOR : FAILED_COLOR);
        int n = this.plugin.getPresenceMouseIdle();
        int n2 = this.plugin.getPresenceKbIdle();
        int n3 = this.config.idleCeilingTicks();
        this.addLine("Mouse idle:", n < 0 ? "?" : String.valueOf(n), LABEL_COLOR, this.config.clampIdle() && n > n3 ? WARN_COLOR : ACTIVE_COLOR);
        this.addLine("Kbd idle:", n2 < 0 ? "?" : String.valueOf(n2), LABEL_COLOR, this.config.clampIdle() && n2 > n3 ? WARN_COLOR : ACTIVE_COLOR);
        int n4 = this.plugin.getPresenceRecorderSpread();
        this.addLine("Recorder:", (String)(n4 < 0 ? "?" : (n4 <= 2 ? "FLAT (" + n4 + ")" : "varied (" + n4 + "px)")), LABEL_COLOR, n4 <= 2 ? FAILED_COLOR : ACTIVE_COLOR);
        String string = (this.config.clampIdle() ? "clamp " : "") + (this.config.fidget() ? "move " : "") + (this.config.normalizeLastClick() ? "click " : "") + (this.config.moveCamera() ? "cam" : "");
        this.addLine("Anti-idle:", string.trim().isEmpty() ? "focus only" : string.trim(), LABEL_COLOR, ACTIVE_COLOR);
        this.addLine("Moves/Corr:", this.plugin.getPresenceFidgets() + " / " + this.plugin.getPresenceCorrections(), LABEL_COLOR, DIM_COLOR);
    }

    private void renderPlayerWatch() {
        String string;
        boolean bl;
        this.addSpacer();
        this.addLine("--- PLAYER WATCH ---", "", DIM_COLOR, DIM_COLOR);
        int n = this.plugin.getWatchedCount();
        boolean bl2 = this.config.panicEnabled();
        boolean bl3 = n > 0 || bl2;
        boolean bl4 = this.config.playerWatchEnabled();
        if (!bl3) {
            this.addLine("Watcher:", "OFF (no list/panic)", LABEL_COLOR, DIM_COLOR);
            return;
        }
        this.addLine("Alerting:", "ON (" + n + " names" + (bl2 ? " + panic" : "") + ")", LABEL_COLOR, ACTIVE_COLOR);
        boolean bl5 = bl = this.plugin.getReactionMode() == ShieldReaction.TELEPORT_HOME;
        this.addLine("Reaction:", bl4 ? (bl ? "Teleport home" : "Log out") : "OFF (alert only)", LABEL_COLOR, bl4 ? (bl ? ACTIVE_COLOR : WARN_COLOR) : DIM_COLOR);
        this.addLine("Alert:", this.plugin.isWebhookConfigured() ? "Admin webhook" : "WEBHOOK NOT SET", LABEL_COLOR, this.plugin.isWebhookConfigured() ? ACTIVE_COLOR : FAILED_COLOR);
        long l = this.plugin.getCooldownSeconds();
        long l2 = this.plugin.getStayLoggedOutSeconds();
        if (this.plugin.isAwaitingShot()) {
            this.addLine("Status:", "TRIGGERED \u2014 reacting", LABEL_COLOR, FAILED_COLOR);
        } else if (l2 > 0L) {
            this.addLine("Status:", "logged out " + IncognitoOverlay.formatDuration(l2), LABEL_COLOR, FAILED_COLOR);
        } else if (l > 0L) {
            this.addLine("Status:", "cooldown " + l + "s", LABEL_COLOR, WARN_COLOR);
        } else {
            this.addLine("Status:", "ARMED", LABEL_COLOR, SHIELD_GREEN);
        }
        if (this.config.panicEnabled()) {
            this.addLine("Panic:", "<=" + this.config.panicDistance() + " tiles", LABEL_COLOR, WARN_COLOR);
        }
        if (this.config.ignoreZonesEnabled()) {
            int n2 = this.plugin.getIgnoreZoneCount();
            this.addLine("Ignore zones:", (String)(n2 == 0 ? "none valid" : n2 + " active"), LABEL_COLOR, n2 == 0 ? WARN_COLOR : ACTIVE_COLOR);
        }
        if ((string = this.plugin.getLastDetectionText()) != null) {
            this.addLine("Last:", string, LABEL_COLOR, FAILED_COLOR);
        }
        if (this.config.showWatchOverlay()) {
            List<PlayerWatcher.NearbyPlayer> list = this.plugin.getNearbyPlayers(6);
            this.addLine("Nearby:", list.isEmpty() ? "none" : String.valueOf(list.size()), LABEL_COLOR, DIM_COLOR);
            for (PlayerWatcher.NearbyPlayer nearbyPlayer : list) {
                this.addLine((nearbyPlayer.watched ? "\u2605 " : "  ") + nearbyPlayer.name, nearbyPlayer.distance + "t", nearbyPlayer.watched ? FAILED_COLOR : LABEL_COLOR, DIM_COLOR);
            }
        }
    }

    private void addLine(String string, String string2, Color color, Color color2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string).leftColor(color).right(string2).rightColor(color2).build());
    }

    private void addSpacer() {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(" ").right(" ").build());
    }

    private static String formatDuration(long l) {
        if (l < 60L) {
            return l + "s";
        }
        long l2 = l / 60L;
        long l3 = l % 60L;
        return l2 + "m " + l3 + "s";
    }
}


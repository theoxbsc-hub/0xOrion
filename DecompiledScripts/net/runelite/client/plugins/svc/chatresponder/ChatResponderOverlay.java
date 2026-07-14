/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.client.ui.overlay.OverlayPanel
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.components.LineComponent
 *  net.runelite.client.ui.overlay.components.TitleComponent
 */
package net.runelite.client.plugins.svc.chatresponder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.plugins.svc.chatresponder.ChatResponderConfig;
import net.runelite.client.plugins.svc.chatresponder.ChatResponderPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class ChatResponderOverlay
extends OverlayPanel {
    private static final Color HEADER = new Color(255, 176, 0);
    private static final Color SUB = new Color(158, 198, 255);
    private final Client client;
    private final ChatResponderPlugin plugin;
    private final ChatResponderConfig config;

    ChatResponderOverlay(Client client, ChatResponderPlugin chatResponderPlugin, ChatResponderConfig chatResponderConfig) {
        this.client = client;
        this.plugin = chatResponderPlugin;
        this.config = chatResponderConfig;
        this.setPosition(OverlayPosition.TOP_LEFT);
        this.setPreferredSize(new Dimension(235, 180));
    }

    public Dimension render(Graphics2D graphics2D) {
        if (!this.config.showOverlay() || this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("x64 Chat Responder").color(HEADER).build());
        boolean bl = this.plugin.isPublicEnabled();
        boolean bl2 = this.plugin.isPmEnabled();
        if (bl) {
            this.addHeader("\u2014 Public \u2014");
            this.addLine("Mode", this.config.responseMode().toString());
            this.addLine("Status", this.plugin.getPublicStatus());
            this.addLine("Replies", String.valueOf(this.plugin.getPublicRepliesSent()));
            this.addLine("Ignored", String.valueOf(this.plugin.getPublicIgnored()));
            this.addWrapped("Heard", this.plugin.getPublicLastDetected());
            this.addWrapped("Said", this.plugin.getPublicLastReply());
        }
        if (bl2) {
            this.addHeader("\u2014 Private \u2014");
            this.addLine("Scope", this.config.replyScope().toString());
            this.addLine("Status", this.plugin.getPmStatus());
            this.addLine("Replies", String.valueOf(this.plugin.getPmRepliesSent()));
            this.addLine("Ignored", String.valueOf(this.plugin.getPmIgnored()));
            this.addWrapped("Heard", this.plugin.getPmLastDetected());
            this.addWrapped("Said", this.plugin.getPmLastReply());
        }
        if (!bl && !bl2) {
            this.addLine("Status", "Both pipelines off");
        }
        if (this.config.webhookEnabled() || this.config.pmWebhookEnabled()) {
            this.addLine("Webhooks", String.valueOf(this.plugin.getWebhooksSent()));
        }
        return super.render(graphics2D);
    }

    private void addHeader(String string) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string).leftColor(SUB).build());
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2 == null ? "-" : string2).build());
    }

    private void addWrapped(String string, String string2) {
        Object object;
        Object object2 = object = string2 == null || string2.isEmpty() ? "-" : string2;
        if (((String)object).length() > 28) {
            object = ((String)object).substring(0, 27) + "\u2026";
        }
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right((String)object).rightColor(Color.LIGHT_GRAY).build());
    }
}


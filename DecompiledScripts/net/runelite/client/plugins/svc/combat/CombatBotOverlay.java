/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.client.ui.overlay.OverlayPanel
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.components.LineComponent
 *  net.runelite.client.ui.overlay.components.TitleComponent
 */
package net.runelite.client.plugins.svc.combat;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.combat.AioCombatConfig;
import net.runelite.client.plugins.svc.combat.CombatBotPlugin;
import net.runelite.client.plugins.svc.combat.PrayerFlickMode;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class CombatBotOverlay
extends OverlayPanel {
    private final Client client;
    private final CombatBotPlugin plugin;
    private final AioCombatConfig config;

    CombatBotOverlay(Client client, CombatBotPlugin combatBotPlugin, AioCombatConfig aioCombatConfig) {
        this.client = client;
        this.plugin = combatBotPlugin;
        this.config = aioCombatConfig;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setPreferredSize(new Dimension(200, 120));
    }

    public Dimension render(Graphics2D graphics2D) {
        int n;
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Orion Combat Bot").color(Color.GREEN).build());
        if (this.config.gemstoneCrabMode()) {
            this.addLine("Mode", "Gemstone Crab");
        }
        this.addLine("Status", this.plugin.getStatus());
        this.addLine("Kills", String.valueOf(this.plugin.getKillCount()));
        Player player = this.client.getLocalPlayer();
        if (player != null) {
            n = this.client.getBoostedSkillLevel(Skill.HITPOINTS);
            int n2 = this.client.getRealSkillLevel(Skill.HITPOINTS);
            int n3 = this.client.getBoostedSkillLevel(Skill.PRAYER);
            this.addLine("HP", n + "/" + n2);
            this.addLine("Prayer", "" + n3);
            this.addLine("Run", this.client.getEnergy() / 100 + "%");
        }
        if (this.config.enablePrayer() && this.config.flickMode() != PrayerFlickMode.ALWAYS_ON) {
            this.addLine("Flick", this.config.flickMode().toString());
        }
        if (this.config.enableStyleSwap() && (n = this.plugin.getSecondsUntilStyleSwap(this.config)) >= 0) {
            this.addLine("Style swap", n / 60 + "m " + n % 60 + "s");
        }
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2).build());
    }
}


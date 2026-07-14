/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Skill
 *  net.runelite.client.ui.overlay.OverlayPanel
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.components.LineComponent
 *  net.runelite.client.ui.overlay.components.TitleComponent
 */
package net.runelite.client.plugins.svc.runecrafting;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.runecrafting.RunecraftingPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class RunecraftingOverlay
extends OverlayPanel {
    private final Client client;
    private final RunecraftingPlugin plugin;

    RunecraftingOverlay(Client client, RunecraftingPlugin runecraftingPlugin) {
        this.client = client;
        this.plugin = runecraftingPlugin;
        this.setPosition(OverlayPosition.BOTTOM_LEFT);
        this.setPreferredSize(new Dimension(220, 130));
    }

    public Dimension render(Graphics2D graphics2D) {
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Orion Runecrafting").color(Color.CYAN).build());
        this.addLine("Status", this.plugin.getStatus());
        this.addLine("State", this.plugin.getStateName());
        this.addLine("Runs", String.valueOf(this.plugin.getRunsCompleted()));
        this.addLine("RC level", String.valueOf(this.client.getRealSkillLevel(Skill.RUNECRAFT)));
        this.addLine("XP gained", String.valueOf(this.plugin.getXpGained()));
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string + ":").right(string2).build());
    }
}


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
package net.runelite.client.plugins.svc.combatoverhaul;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.plugins.svc.combatoverhaul.CombatOverhaulConfig;
import net.runelite.client.plugins.svc.combatoverhaul.CombatOverhaulPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class CombatOverhaulOverlay
extends OverlayPanel {
    private static final int BUTTON_STRIP_H = 16;
    private final Client client;
    private final CombatOverhaulPlugin plugin;
    private final CombatOverhaulConfig config;

    CombatOverhaulOverlay(Client client, CombatOverhaulPlugin combatOverhaulPlugin, CombatOverhaulConfig combatOverhaulConfig) {
        this.client = client;
        this.plugin = combatOverhaulPlugin;
        this.config = combatOverhaulConfig;
        this.setPosition(OverlayPosition.TOP_LEFT);
        this.setPreferredSize(new Dimension(205, 160));
    }

    public Dimension render(Graphics2D graphics2D) {
        if (!this.config.showOverlay()) {
            return null;
        }
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            return null;
        }
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Combat Overhaul").color(new Color(255, 70, 70)).build());
        this.addLine("Gear Switcher", this.config.gearSwitcherEnabled() ? "ON" : "OFF");
        this.addLine("Status", this.plugin.getStatus());
        String string = this.plugin.getProgress();
        if (!string.isEmpty()) {
            this.addLine("Progress", string);
        }
        this.addLine("Capture", this.plugin.getCapturePresetName());
        int n = this.plugin.getSpecEnergyPercent();
        if (n >= 0) {
            this.addLine("Spec", n + "%");
        }
        this.addLine("Last", this.plugin.getLastSwitch());
        for (int i = 1; i <= this.plugin.presetCount(); ++i) {
            int n2 = this.plugin.presetItemCount(i);
            String string2 = this.plugin.presetHotkeyText(i);
            if (n2 == 0 && "-".equals(string2)) continue;
            this.addLine(this.plugin.presetName(i), string2 + " (" + n2 + ")");
        }
        this.getPanelComponent().getChildren().add(LineComponent.builder().left("[ Snapshot ->").right(this.plugin.getCapturePresetName() + " ]").leftColor(Color.CYAN).rightColor(Color.CYAN).build());
        return super.render(graphics2D);
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string).right(string2).build());
    }

    boolean handleMouseClicked(int n, int n2) {
        if (!this.config.showOverlay() || !this.config.gearSwitcherEnabled()) {
            return false;
        }
        Rectangle rectangle = this.getBounds();
        if (rectangle == null || rectangle.width == 0 || rectangle.height == 0) {
            return false;
        }
        Rectangle rectangle2 = new Rectangle(rectangle.x, rectangle.y + rectangle.height - 16, rectangle.width, 16);
        if (rectangle2.contains(n, n2)) {
            this.plugin.snapshotCapturePreset();
            return true;
        }
        return false;
    }
}


package net.runelite.client.plugins.ox.info;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.ox.core.OxPluginRegistry;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@PluginDescriptor(
    name = "<html><nobr><font color='#B44DFF'>\u25cf 0xINFO</font></nobr></html>",
    description = "Account identity, active script stats, and past session history (logs banked, trips, runtime)",
    tags = {"0x", "info", "account", "mac", "proxy", "identity", "history", "sessions"},
    enabledByDefault = true
)
public class InfoPlugin extends Plugin {
    @Inject
    private ClientToolbar clientToolbar;

    private OxInfoPanel panel;
    private NavigationButton navButton;

    @Override
    protected void startUp() {
        this.panel = new OxInfoPanel();
        this.navButton = NavigationButton.builder()
            .tooltip("0xINFO")
            .icon(createIcon())
            .priority(0)
            .panel(this.panel)
            .build();
        this.clientToolbar.addNavigation(this.navButton);
        this.panel.startAutoRefresh();
        OxPluginRegistry.register("0xINFO");
    }

    @Override
    protected void shutDown() {
        if (this.panel != null) {
            this.panel.stopAutoRefresh();
        }
        this.clientToolbar.removeNavigation(this.navButton);
        this.navButton = null;
        this.panel = null;
    }

    private static BufferedImage createIcon() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0xB44DFF));
        g.fillRoundRect(0, 0, 16, 16, 5, 5);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.drawString("i", 6, 12);
        g.dispose();
        return img;
    }
}

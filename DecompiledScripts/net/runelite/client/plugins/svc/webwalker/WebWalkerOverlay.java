/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Perspective
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPosition
 */
package net.runelite.client.plugins.svc.webwalker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.webwalker.WebWalkerPlugin;
import net.runelite.client.plugins.svc.webwalker.walker.OrionWebWalker;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class WebWalkerOverlay
extends Overlay {
    private static final Color PATH_COLOR = new Color(0, 200, 255, 140);
    private static final Color DEST_COLOR = new Color(0, 255, 120, 180);
    private final Client client;
    private final WebWalkerPlugin plugin;

    public WebWalkerOverlay(Client client, WebWalkerPlugin webWalkerPlugin) {
        this.client = client;
        this.plugin = webWalkerPlugin;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public Dimension render(Graphics2D graphics2D) {
        OrionWebWalker orionWebWalker = this.plugin.getWalker();
        if (orionWebWalker == null || this.client.getLocalPlayer() == null) {
            return null;
        }
        List<WorldPoint> list = orionWebWalker.getRemainingPath();
        int n = this.client.getPlane();
        int n2 = 0;
        for (WorldPoint worldPoint : list) {
            if (worldPoint.getPlane() != n || n2++ > 200) continue;
            this.drawTile(graphics2D, worldPoint, PATH_COLOR, 1);
        }
        WorldPoint worldPoint = this.plugin.getTarget();
        if (worldPoint != null && worldPoint.getPlane() == n) {
            this.drawTile(graphics2D, worldPoint, DEST_COLOR, 2);
        }
        return null;
    }

    private void drawTile(Graphics2D graphics2D, WorldPoint worldPoint, Color color, int n) {
        LocalPoint localPoint = LocalPoint.fromWorld((Client)this.client, (WorldPoint)worldPoint);
        if (localPoint == null) {
            return;
        }
        Polygon polygon = Perspective.getCanvasTilePoly((Client)this.client, (LocalPoint)localPoint);
        if (polygon == null) {
            return;
        }
        graphics2D.setColor(color);
        graphics2D.setStroke(new BasicStroke(n));
        graphics2D.drawPolygon(polygon);
        if (n > 1) {
            graphics2D.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
            graphics2D.fillPolygon(polygon);
        }
    }
}


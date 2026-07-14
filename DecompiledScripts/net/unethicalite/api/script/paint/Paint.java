/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Point
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayManager
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.OverlayPriority
 *  net.runelite.client.ui.overlay.OverlayUtil
 *  net.runelite.client.ui.overlay.RenderableEntity
 */
package net.unethicalite.api.script.paint;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Point;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.RenderableEntity;
import net.unethicalite.api.script.paint.DefaultPaint;
import net.unethicalite.client.devtools.EntityRenderer;
import net.unethicalite.client.managers.InputManager;
import net.unethicalite.client.minimal.ui.MinimalUI;

@Singleton
public class Paint
extends Overlay {
    private final List<RenderableEntity> overlays = new ArrayList<RenderableEntity>();
    private boolean enabled = false;
    @Inject
    private EntityRenderer entityRenderer;
    @Inject
    private ConfigManager configManager;
    @Inject
    private InputManager inputManager;
    public final DefaultPaint tracker = new DefaultPaint();

    @Inject
    public Paint(OverlayManager overlayManager) {
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.setPriority(OverlayPriority.LOW);
        overlayManager.add((Overlay)this);
    }

    public Dimension render(Graphics2D g) {
        Font font = g.getFont();
        if (MinimalUI.getFrame() != null) {
            if (((Boolean)this.configManager.getConfiguration("entityinspector", "drawMouse", Boolean.class)).booleanValue()) {
                g.setFont(new Font("Tahoma", 1, 18));
                OverlayUtil.renderTextLocation((Graphics2D)g, (Point)new Point(this.inputManager.getLastClickX() - g.getFont().getSize() / 3, this.inputManager.getLastClickY() + g.getFont().getSize() / 3), (String)"X", (Color)Color.WHITE);
                OverlayUtil.renderTextLocation((Graphics2D)g, (Point)new Point(this.inputManager.getLastMoveX() - g.getFont().getSize() / 3, this.inputManager.getLastMoveY() + g.getFont().getSize() / 3), (String)"X", (Color)Color.GREEN);
            }
            g.setColor(Color.WHITE);
            g.setFont(font);
            this.entityRenderer.render(g);
        }
        if (!this.enabled) {
            return null;
        }
        for (RenderableEntity renderableEntity : this.overlays) {
            renderableEntity.render(g);
        }
        return null;
    }

    public void submit(RenderableEntity p) {
        this.overlays.add(p);
    }

    public void clear() {
        this.overlays.clear();
        this.tracker.clear();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            this.submit(this.tracker.getTracker());
        }
    }
}


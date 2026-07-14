/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.api.MenuAction
 *  net.runelite.api.Point
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.managers.interaction;

import java.awt.Rectangle;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.api.MouseHandler;
import net.unethicalite.api.SceneEntity;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.events.MenuAutomated;
import net.unethicalite.api.exception.InteractionException;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.api.input.naturalmouse.NaturalMouse;
import net.unethicalite.api.packets.Packets;
import net.unethicalite.api.utils.CoordUtils;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.config.UnethicaliteConfig;
import net.unethicalite.client.managers.interaction.MouseBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class InteractionManager {
    private static final Logger log = LoggerFactory.getLogger(InteractionManager.class);
    private static final int MINIMAP_WIDTH = 250;
    private static final int MINIMAP_HEIGHT = 180;
    @Inject
    private NaturalMouse naturalMouse;
    @Inject
    private UnethicaliteConfig config;
    @Inject
    private Client client;

    @Inject
    InteractionManager(EventBus eventBus) {
        eventBus.register((Object)this);
    }

    @Subscribe
    public void onMenuAutomated(MenuAutomated event) {
        java.awt.Point clickPoint = this.getClickPoint(event);
        MouseHandler mouseHandler = this.client.getMouseHandler();
        try {
            switch (this.config.interactMethod()) {
                case MOUSE_EVENTS: {
                    Point newPoint;
                    SceneEntity entity = event.getEntity();
                    if (entity != null && this.config.mouseBehavior() == MouseBehavior.CLICKBOXES) {
                        clickPoint = entity.getClickPoint().getAwtPoint();
                    }
                    if (event.getOpcode() == MenuAction.WALK && this.clickOffScreen(clickPoint) && (newPoint = CoordUtils.localToMinimap(this.client, LocalPoint.fromScene((int)event.getParam0(), (int)event.getParam1()), 6400)) != null) {
                        clickPoint = newPoint.getAwtPoint();
                    }
                    if (this.config.naturalMouse()) {
                        this.naturalMouse.moveTo(clickPoint.x, clickPoint.y);
                    }
                    java.awt.Point finalClickPoint = clickPoint;
                    GameThread.invoke(() -> {
                        if (!this.config.naturalMouse()) {
                            mouseHandler.sendMovement(finalClickPoint.x, finalClickPoint.y);
                        }
                        this.client.setPendingAutomation(event);
                        this.setHoveredEntity(entity);
                        log.debug("Sending click to [{}, {}]", (Object)finalClickPoint.x, (Object)finalClickPoint.y);
                        mouseHandler.sendClick(finalClickPoint.x, finalClickPoint.y, 1);
                    });
                    Time.sleep(50, 100);
                    mouseHandler.sendRelease();
                    break;
                }
                case INVOKE: {
                    this.processAction(event, clickPoint.x, clickPoint.y);
                    break;
                }
                case PACKETS: {
                    GameThread.invoke(() -> {
                        try {
                            if (event.getOpcode() == MenuAction.CC_OP || event.getOpcode() == MenuAction.CC_OP_LOW_PRIORITY) {
                                Widget child;
                                int param0 = event.getParam0();
                                int param1 = event.getParam1();
                                int id = event.getIdentifier();
                                int itemId = event.getItemId();
                                if (itemId != -1) {
                                    this.client.invokeWidgetAction(id, param1, param0, itemId, "");
                                    return;
                                }
                                Widget widget = Widgets.fromId(event.getParam1());
                                if (widget == null) {
                                    return;
                                }
                                Widget widget2 = child = param0 == -1 ? null : widget.getChild(param0);
                                if (child == null) {
                                    this.client.invokeWidgetAction(id, param1, param0, -1, "");
                                    return;
                                }
                                this.client.invokeWidgetAction(id, param1, param0, child.getItemId(), "");
                            } else {
                                Packets.fromAutomatedMenu(event).send();
                            }
                        }
                        catch (InteractionException ex) {
                            log.error("Packet interaction failed, falling back to invoke", (Throwable)ex);
                            this.processAction(event, -1, -1);
                        }
                    });
                }
            }
        }
        catch (InteractionException ex) {
            log.error("Interaction failed: {}", (Object)ex.getMessage());
            this.client.setPendingAutomation(null);
        }
    }

    public void setHoveredEntity(SceneEntity entity) {
        if (entity != null) {
            long[] entitiesAtMouse = this.client.getEntitiesAtMouse();
            int count = this.client.getEntitiesAtMouseCount();
            if (count < 1000) {
                entitiesAtMouse[count] = entity.getTag();
                this.client.setEntitiesAtMouseCount(count + 1);
            }
        }
    }

    private void processAction(MenuAutomated entry, int x, int y) {
        GameThread.invoke(() -> this.client.invokeMenuAction(entry.getOption(), entry.getTarget(), entry.getIdentifier(), entry.getOpcode().getId(), entry.getParam0(), entry.getParam1(), x, y));
    }

    private java.awt.Point getClickPoint(MenuAutomated event) {
        java.awt.Point clickPoint;
        if (this.config.mouseBehavior() == MouseBehavior.OFF_SCREEN) {
            return new java.awt.Point(0, 0);
        }
        if (this.config.mouseBehavior() == MouseBehavior.MOUSE_POS) {
            return new java.awt.Point(this.client.getMouseHandler().getCurrentX(), this.client.getMouseHandler().getCurrentY());
        }
        if (event.getClickX() != -1 && event.getClickY() != -1 && this.config.mouseBehavior() == MouseBehavior.CLICKBOXES && !this.clickInsideMinimap(clickPoint = new java.awt.Point(event.getClickX(), event.getClickY()))) {
            return clickPoint;
        }
        Rectangle bounds = this.client.getCanvas().getBounds();
        java.awt.Point randomPoint = new java.awt.Point(Rand.nextInt(0, bounds.width), Rand.nextInt(0, bounds.height));
        if (this.clickInsideMinimap(randomPoint)) {
            return this.getClickPoint(event);
        }
        return randomPoint;
    }

    public boolean clickInsideMinimap(java.awt.Point point) {
        return this.getMinimap().contains(point);
    }

    private Rectangle getMinimap() {
        Widget minimap = Widgets.get(WidgetInfo.FIXED_VIEWPORT_MINIMAP_DRAW_AREA);
        if (Widgets.isVisible(minimap)) {
            return minimap.getBounds();
        }
        Widget minimap1 = Widgets.get(WidgetInfo.RESIZABLE_MINIMAP_DRAW_AREA);
        if (Widgets.isVisible(minimap1)) {
            return minimap1.getBounds();
        }
        Widget minimap2 = Widgets.get(WidgetInfo.RESIZABLE_MINIMAP_STONES_DRAW_AREA);
        if (Widgets.isVisible(minimap2)) {
            return minimap2.getBounds();
        }
        Rectangle bounds = this.client.getCanvas().getBounds();
        return new Rectangle(bounds.width - 250, 0, 250, 180);
    }

    private boolean clickOffScreen(java.awt.Point point) {
        return point.x < 0 || point.y < 0 || point.x > this.client.getViewportWidth() || point.y > this.client.getViewportHeight();
    }
}


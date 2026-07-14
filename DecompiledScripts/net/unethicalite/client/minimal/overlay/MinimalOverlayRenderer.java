/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.google.common.collect.ImmutableList
 *  com.google.common.primitives.Ints
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Point
 *  net.runelite.api.events.BeforeRender
 *  net.runelite.api.events.ClientTick
 *  net.runelite.api.events.FocusChanged
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.api.widgets.WidgetItem
 *  net.runelite.client.config.RuneLiteConfig
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.events.OverlayMenuClicked
 *  net.runelite.client.input.KeyListener
 *  net.runelite.client.input.KeyManager
 *  net.runelite.client.input.MouseAdapter
 *  net.runelite.client.input.MouseListener
 *  net.runelite.client.input.MouseManager
 *  net.runelite.client.ui.ClientUI
 *  net.runelite.client.ui.JagexColors
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayBounds
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayManager
 *  net.runelite.client.ui.overlay.OverlayMenuEntry
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.OverlayUtil
 *  net.runelite.client.ui.overlay.tooltip.Tooltip
 *  net.runelite.client.ui.overlay.tooltip.TooltipManager
 *  net.runelite.client.util.ColorUtil
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.slf4j.Marker
 *  org.slf4j.MarkerFactory
 */
package net.unethicalite.client.minimal.overlay;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Point;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayBounds;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Singleton
public class MinimalOverlayRenderer
extends MouseAdapter
implements KeyListener {
    private static final Logger log = LoggerFactory.getLogger(MinimalOverlayRenderer.class);
    private static final Marker DEDUPLICATE = MarkerFactory.getMarker((String)"DEDUPLICATE");
    private static final int BORDER = 5;
    private static final int BORDER_TOP = 20;
    private static final int PADDING = 2;
    private static final int OVERLAY_RESIZE_TOLERANCE = 5;
    private static final Dimension SNAP_CORNER_SIZE = new Dimension(80, 80);
    private static final Color SNAP_CORNER_COLOR = new Color(0, 255, 255, 50);
    private static final Color SNAP_CORNER_ACTIVE_COLOR = new Color(0, 255, 0, 100);
    private static final Color MOVING_OVERLAY_COLOR = new Color(255, 255, 0, 100);
    private static final Color MOVING_OVERLAY_ACTIVE_COLOR = new Color(255, 255, 0, 200);
    private static final Color MOVING_OVERLAY_TARGET_COLOR = Color.RED;
    private static final Color MOVING_OVERLAY_RESIZING_COLOR = new Color(255, 0, 255, 200);
    private final Client client;
    private final OverlayManager overlayManager;
    private final TooltipManager tooltipManager;
    private final RuneLiteConfig runeLiteConfig;
    private final EventBus eventBus;
    private final java.awt.Point overlayOffset = new java.awt.Point();
    private final java.awt.Point mousePosition = new java.awt.Point();
    private Overlay currentManagedOverlay;
    private Overlay dragTargetOverlay;
    private Rectangle currentManagedBounds;
    private boolean inOverlayManagingMode;
    private boolean inOverlayResizingMode;
    private boolean inOverlayDraggingMode;
    private boolean startedMovingOverlay;
    private Overlay hoveredOverlay;
    private Rectangle viewportBounds;
    private Rectangle chatboxBounds;
    private boolean chatboxHidden;
    private boolean isResizeable;
    private OverlayBounds emptySnapCorners;
    private OverlayBounds snapCorners;
    private Overlay focusedOverlay;
    private Overlay prevFocusedOverlay;

    @Inject
    private MinimalOverlayRenderer(Client client, OverlayManager overlayManager, RuneLiteConfig runeLiteConfig, MouseManager mouseManager, KeyManager keyManager, TooltipManager tooltipManager, ClientUI clientUI, EventBus eventBus) {
        this.client = client;
        this.overlayManager = overlayManager;
        this.runeLiteConfig = runeLiteConfig;
        this.tooltipManager = tooltipManager;
        this.eventBus = eventBus;
        keyManager.registerKeyListener((KeyListener)this);
        mouseManager.registerMouseListener((MouseListener)this);
        eventBus.register((Object)this);
    }

    @Subscribe
    public void onFocusChanged(FocusChanged event) {
        if (!event.isFocused()) {
            if (this.inOverlayManagingMode) {
                this.inOverlayManagingMode = false;
                this.resetOverlayManagementMode();
            }
            this.hoveredOverlay = null;
        }
    }

    @Subscribe
    protected void onClientTick(ClientTick t) {
        Overlay overlay = this.hoveredOverlay;
        if (overlay == null || this.client.isMenuOpen()) {
            return;
        }
        boolean shift = this.client.isKeyPressed(81);
        if (!shift) {
            return;
        }
        List menuEntries = overlay.getMenuEntries();
        if (menuEntries.isEmpty()) {
            return;
        }
        for (int i = menuEntries.size() - 1; i >= 0; --i) {
            OverlayMenuEntry overlayMenuEntry = (OverlayMenuEntry)menuEntries.get(i);
            this.client.createMenuEntry(-1).setOption(overlayMenuEntry.getOption()).setTarget(ColorUtil.wrapWithColorTag((String)overlayMenuEntry.getTarget(), (Color)JagexColors.MENU_TARGET)).setType(overlayMenuEntry.getMenuAction()).onClick(e -> this.eventBus.post((Object)new OverlayMenuClicked(overlayMenuEntry, overlay)));
        }
    }

    @Subscribe
    public void onBeforeRender(BeforeRender event) {
        this.hoveredOverlay = null;
        if (this.focusedOverlay == null && this.prevFocusedOverlay != null) {
            this.prevFocusedOverlay.onMouseExit();
        }
        this.prevFocusedOverlay = this.focusedOverlay;
        this.focusedOverlay = null;
        if (this.client.getGameState() == GameState.LOGGED_IN) {
            if (this.shouldInvalidateBounds()) {
                this.emptySnapCorners = this.buildSnapCorners();
            }
            this.snapCorners = new OverlayBounds(this.emptySnapCorners);
        }
    }

    public void renderOverlayLayer(Graphics2D graphics, OverlayLayer layer) {
        Collection overlays = this.overlayManager.getLayer(layer);
        this.renderOverlays(graphics, overlays, layer);
    }

    public void renderAfterInterface(Graphics2D graphics, int interfaceId, Collection<WidgetItem> widgetItems) {
        Collection overlays = this.overlayManager.getForInterface(interfaceId);
        this.overlayManager.setWidgetItems(widgetItems);
        this.renderOverlays(graphics, overlays, OverlayLayer.ABOVE_WIDGETS);
        this.overlayManager.setWidgetItems(Collections.emptyList());
    }

    public void renderAfterLayer(Graphics2D graphics, Widget layer, Collection<WidgetItem> widgetItems) {
        Collection overlays = this.overlayManager.getForLayer(layer.getId());
        this.overlayManager.setWidgetItems(widgetItems);
        this.renderOverlays(graphics, overlays, OverlayLayer.ABOVE_WIDGETS);
        this.overlayManager.setWidgetItems(Collections.emptyList());
    }

    private void renderOverlays(Graphics2D graphics, Collection<Overlay> overlays, OverlayLayer layer) {
        if (overlays == null || overlays.isEmpty() || this.client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        OverlayUtil.setGraphicProperties((Graphics2D)graphics);
        if (this.inOverlayDraggingMode && layer == OverlayLayer.UNDER_WIDGETS && this.currentManagedOverlay != null && this.currentManagedOverlay.getPosition() != OverlayPosition.DETACHED) {
            OverlayBounds translatedSnapCorners = this.snapCorners.translated(-MinimalOverlayRenderer.SNAP_CORNER_SIZE.width, -MinimalOverlayRenderer.SNAP_CORNER_SIZE.height);
            Color previous = graphics.getColor();
            for (Rectangle corner : translatedSnapCorners.getBounds()) {
                graphics.setColor(corner.contains(this.mousePosition) ? SNAP_CORNER_ACTIVE_COLOR : SNAP_CORNER_COLOR);
                graphics.fill(corner);
            }
            graphics.setColor(previous);
        }
        Point mouseCanvasPosition = this.client.getMouseCanvasPosition();
        java.awt.Point mouse = new java.awt.Point(mouseCanvasPosition.getX(), mouseCanvasPosition.getY());
        AffineTransform transform = graphics.getTransform();
        Stroke stroke = graphics.getStroke();
        Composite composite = graphics.getComposite();
        Paint paint = graphics.getPaint();
        RenderingHints renderingHints = graphics.getRenderingHints();
        Color background = graphics.getBackground();
        for (Overlay overlay : overlays) {
            java.awt.Point location;
            OverlayPosition overlayPosition = this.getCorrectedOverlayPosition(overlay);
            if (overlayPosition == OverlayPosition.DYNAMIC || overlayPosition == OverlayPosition.TOOLTIP) {
                this.safeRender(this.client, overlay, layer, graphics, new java.awt.Point());
                graphics.setTransform(transform);
                graphics.setStroke(stroke);
                graphics.setComposite(composite);
                graphics.setPaint(paint);
                graphics.setRenderingHints(renderingHints);
                graphics.setBackground(background);
                continue;
            }
            Rectangle bounds = overlay.getBounds();
            Dimension dimension = bounds.getSize();
            java.awt.Point preferredLocation = overlay.getPreferredLocation();
            Rectangle snapCorner = null;
            if (overlayPosition != OverlayPosition.DETACHED && (preferredLocation == null || overlay.getPreferredPosition() != null)) {
                snapCorner = this.snapCorners.forPosition(overlayPosition);
                java.awt.Point translation = OverlayUtil.transformPosition((OverlayPosition)overlayPosition, (Dimension)dimension);
                int destX = snapCorner.x + translation.x;
                int destY = snapCorner.y + translation.y;
                location = this.clampOverlayLocation(destX, destY, dimension.width, dimension.height, overlay);
            } else {
                location = preferredLocation != null ? preferredLocation : bounds.getLocation();
                location = this.clampOverlayLocation(location.x, location.y, dimension.width, dimension.height, overlay);
            }
            if (overlay.getPreferredSize() != null) {
                bounds.setSize(overlay.getPreferredSize());
            }
            this.safeRender(this.client, overlay, layer, graphics, location);
            if (snapCorner != null && bounds.width + bounds.height > 0) {
                OverlayUtil.shiftSnapCorner((OverlayPosition)overlayPosition, (Rectangle)snapCorner, (Rectangle)bounds, (int)2);
            }
            graphics.setTransform(transform);
            graphics.setStroke(stroke);
            graphics.setComposite(composite);
            graphics.setPaint(paint);
            graphics.setRenderingHints(renderingHints);
            graphics.setBackground(background);
            if (bounds.isEmpty()) continue;
            if (this.inOverlayManagingMode) {
                Color boundsColor;
                if (this.inOverlayResizingMode && this.currentManagedOverlay == overlay) {
                    boundsColor = MOVING_OVERLAY_RESIZING_COLOR;
                } else if (this.inOverlayDraggingMode && this.currentManagedOverlay == overlay) {
                    boundsColor = MOVING_OVERLAY_ACTIVE_COLOR;
                } else if (this.inOverlayDraggingMode && overlay.isDragTargetable() && this.currentManagedOverlay.isDragTargetable() && this.currentManagedOverlay.getBounds().intersects(bounds)) {
                    boundsColor = MOVING_OVERLAY_TARGET_COLOR;
                    assert (this.currentManagedOverlay != overlay);
                    this.dragTargetOverlay = overlay;
                } else {
                    boundsColor = MOVING_OVERLAY_COLOR;
                }
                graphics.setColor(boundsColor);
                graphics.draw(bounds);
                graphics.setPaint(paint);
            }
            if (this.client.isMenuOpen() || this.client.getSpellSelected() || !bounds.contains(mouse)) continue;
            this.hoveredOverlay = overlay;
            if (this.inOverlayManagingMode) {
                String tooltipText = overlay.getPlugin() == null ? overlay.getName() : overlay.getPlugin().getName();
                this.tooltipManager.add(new Tooltip(tooltipText));
            }
            if (this.focusedOverlay == null) {
                this.focusedOverlay = overlay;
                if (this.focusedOverlay != this.prevFocusedOverlay) {
                    if (this.prevFocusedOverlay != null) {
                        this.prevFocusedOverlay.onMouseExit();
                    }
                    overlay.onMouseEnter();
                }
            }
            overlay.onMouseOver();
        }
    }

    public MouseEvent mousePressed(MouseEvent mouseEvent) {
        if (!this.inOverlayManagingMode) {
            return mouseEvent;
        }
        java.awt.Point mousePoint = mouseEvent.getPoint();
        this.mousePosition.setLocation(mousePoint);
        this.currentManagedOverlay = this.findMangedOverlay(mousePoint);
        if (this.currentManagedOverlay == null) {
            return mouseEvent;
        }
        if (SwingUtilities.isRightMouseButton(mouseEvent)) {
            if (this.currentManagedOverlay.isResettable()) {
                this.overlayManager.resetOverlay(this.currentManagedOverlay);
            }
        } else if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
            java.awt.Point offset = new java.awt.Point(mousePoint.x, mousePoint.y);
            offset.translate(-this.currentManagedOverlay.getBounds().x, -this.currentManagedOverlay.getBounds().y);
            this.overlayOffset.setLocation(offset);
            this.inOverlayResizingMode = this.currentManagedOverlay != null && this.currentManagedOverlay.isResizable();
            this.inOverlayDraggingMode = !this.inOverlayResizingMode;
            this.startedMovingOverlay = true;
            this.currentManagedBounds = new Rectangle(this.currentManagedOverlay.getBounds());
        } else {
            return mouseEvent;
        }
        mouseEvent.consume();
        return mouseEvent;
    }

    public MouseEvent mouseMoved(MouseEvent mouseEvent) {
        if (!this.inOverlayManagingMode) {
            return mouseEvent;
        }
        java.awt.Point mousePoint = mouseEvent.getPoint();
        this.mousePosition.setLocation(mousePoint);
        if (!this.inOverlayResizingMode && !this.inOverlayDraggingMode) {
            this.currentManagedOverlay = this.findMangedOverlay(mousePoint);
        }
        if (this.currentManagedOverlay == null || !this.currentManagedOverlay.isResizable()) {
            return mouseEvent;
        }
        Rectangle toleranceRect = new Rectangle(this.currentManagedOverlay.getBounds());
        toleranceRect.grow(-5, -5);
        return mouseEvent;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Overlay findMangedOverlay(java.awt.Point mousePoint) {
        OverlayManager overlayManager = this.overlayManager;
        synchronized (overlayManager) {
            ImmutableList layerOrder = ImmutableList.of((Object)OverlayLayer.UNDER_WIDGETS, (Object)OverlayLayer.MANUAL, (Object)OverlayLayer.ABOVE_WIDGETS, (Object)OverlayLayer.ALWAYS_ON_TOP);
            return this.overlayManager.getOverlays().stream().filter(arg_0 -> MinimalOverlayRenderer.lambda$findMangedOverlay$1((List)layerOrder, arg_0)).filter(c -> c.getPosition() != OverlayPosition.DYNAMIC && c.getPosition() != OverlayPosition.TOOLTIP).sorted(Comparator.comparingInt(arg_0 -> MinimalOverlayRenderer.lambda$findMangedOverlay$3((List)layerOrder, arg_0)).thenComparing(OverlayManager.OVERLAY_COMPARATOR).reversed()).filter(o -> o.getBounds().contains(mousePoint)).findFirst().orElse(null);
        }
    }

    public MouseEvent mouseDragged(MouseEvent mouseEvent) {
        Rectangle canvasRect;
        if (!this.inOverlayManagingMode) {
            return mouseEvent;
        }
        java.awt.Point p = mouseEvent.getPoint();
        this.mousePosition.setLocation(p);
        if (this.currentManagedOverlay == null) {
            return mouseEvent;
        }
        if (this.dragTargetOverlay != null && !this.currentManagedOverlay.getBounds().intersects(this.dragTargetOverlay.getBounds())) {
            this.dragTargetOverlay = null;
        }
        if (!(canvasRect = new Rectangle(this.client.getRealDimensions())).contains(p)) {
            return mouseEvent;
        }
        if (this.inOverlayResizingMode) {
            int originalX = this.currentManagedBounds.x;
            int originalY = this.currentManagedBounds.y;
            int x = originalX;
            int y = originalY;
            int width = this.currentManagedBounds.width;
            int height = this.currentManagedBounds.height;
            int minOverlaySize = this.currentManagedOverlay.getMinimumSize();
            int widthOverflow = Math.max(0, minOverlaySize - width);
            int heightOverflow = Math.max(0, minOverlaySize - height);
            int dx = x - originalX;
            int dy = y - originalY;
            if (widthOverflow > 0) {
                width = minOverlaySize;
                if (dx > 0) {
                    x -= widthOverflow;
                }
            }
            if (heightOverflow > 0) {
                height = minOverlaySize;
                if (dy > 0) {
                    y -= heightOverflow;
                }
            }
            this.currentManagedBounds.setRect(x, y, width, height);
            this.currentManagedOverlay.setPreferredSize(new Dimension(this.currentManagedBounds.width, this.currentManagedBounds.height));
            if (this.currentManagedOverlay.getPreferredLocation() != null) {
                this.currentManagedOverlay.setPreferredLocation(this.currentManagedBounds.getLocation());
            }
        } else if (this.inOverlayDraggingMode) {
            java.awt.Point overlayPosition = new java.awt.Point(p);
            overlayPosition.translate(-this.overlayOffset.x, -this.overlayOffset.y);
            Rectangle overlayBounds = this.currentManagedOverlay.getBounds();
            overlayPosition = this.clampOverlayLocation(overlayPosition.x, overlayPosition.y, overlayBounds.width, overlayBounds.height, this.currentManagedOverlay);
            this.currentManagedOverlay.setPreferredPosition(null);
            this.currentManagedOverlay.setPreferredLocation(overlayPosition);
        } else {
            return mouseEvent;
        }
        if (this.startedMovingOverlay) {
            this.overlayManager.rebuildOverlayLayers();
            this.startedMovingOverlay = false;
        }
        mouseEvent.consume();
        return mouseEvent;
    }

    public MouseEvent mouseReleased(MouseEvent mouseEvent) {
        if (!this.inOverlayManagingMode || this.currentManagedOverlay == null || !this.inOverlayDraggingMode && !this.inOverlayResizingMode) {
            return mouseEvent;
        }
        this.mousePosition.setLocation(-1, -1);
        if (this.dragTargetOverlay != null && this.dragTargetOverlay.onDrag(this.currentManagedOverlay)) {
            mouseEvent.consume();
            this.resetOverlayManagementMode();
            return mouseEvent;
        }
        if (this.currentManagedOverlay.getPosition() != OverlayPosition.DETACHED && this.inOverlayDraggingMode) {
            OverlayBounds snapCorners = this.emptySnapCorners.translated(-MinimalOverlayRenderer.SNAP_CORNER_SIZE.width, -MinimalOverlayRenderer.SNAP_CORNER_SIZE.height);
            for (Rectangle snapCorner : snapCorners.getBounds()) {
                if (!snapCorner.contains(mouseEvent.getPoint())) continue;
                OverlayPosition position = snapCorners.fromBounds(snapCorner);
                if (position == this.getCorrectedOverlayPosition(this.currentManagedOverlay)) {
                    position = null;
                }
                this.currentManagedOverlay.setPreferredPosition(position);
                this.currentManagedOverlay.setPreferredLocation(null);
                this.currentManagedOverlay.revalidate();
                break;
            }
        }
        this.overlayManager.saveOverlay(this.currentManagedOverlay);
        this.resetOverlayManagementMode();
        mouseEvent.consume();
        return mouseEvent;
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.isAltDown()) {
            this.inOverlayManagingMode = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        if (!e.isAltDown() && this.inOverlayManagingMode) {
            this.inOverlayManagingMode = false;
            this.resetOverlayManagementMode();
        }
    }

    private void safeRender(Client client, Overlay overlay, OverlayLayer layer, Graphics2D graphics, java.awt.Point point) {
        Dimension overlayDimension;
        if (!(this.isResizeable || layer != OverlayLayer.ABOVE_SCENE && layer != OverlayLayer.UNDER_WIDGETS)) {
            graphics.setClip(client.getViewportXOffset(), client.getViewportYOffset(), client.getViewportWidth(), client.getViewportHeight());
        } else {
            graphics.setClip(0, 0, client.getCanvasWidth(), client.getCanvasHeight());
        }
        OverlayPosition position = overlay.getPosition();
        if (position == OverlayPosition.DYNAMIC || position == OverlayPosition.DETACHED) {
            graphics.setFont(this.runeLiteConfig.fontType().getFont());
        } else if (position == OverlayPosition.TOOLTIP) {
            graphics.setFont(this.runeLiteConfig.tooltipFontType().getFont());
        } else {
            graphics.setFont(this.runeLiteConfig.interfaceFontType().getFont());
        }
        graphics.translate(point.x, point.y);
        overlay.getBounds().setLocation(point);
        try {
            overlayDimension = overlay.render(graphics);
        }
        catch (Exception ex) {
            log.warn(DEDUPLICATE, "Error during overlay rendering", (Throwable)ex);
            return;
        }
        Dimension dimension = (Dimension)MoreObjects.firstNonNull((Object)overlayDimension, (Object)new Dimension());
        overlay.getBounds().setSize(dimension);
    }

    private OverlayPosition getCorrectedOverlayPosition(Overlay overlay) {
        OverlayPosition overlayPosition = overlay.getPosition();
        if (overlay.getPreferredPosition() != null) {
            overlayPosition = overlay.getPreferredPosition();
        }
        if (!this.isResizeable) {
            switch (overlayPosition) {
                case CANVAS_TOP_RIGHT: {
                    overlayPosition = OverlayPosition.TOP_RIGHT;
                    break;
                }
                case ABOVE_CHATBOX_RIGHT: {
                    overlayPosition = OverlayPosition.BOTTOM_RIGHT;
                }
            }
        }
        return overlayPosition;
    }

    private void resetOverlayManagementMode() {
        this.inOverlayResizingMode = false;
        this.inOverlayDraggingMode = false;
        this.currentManagedOverlay = null;
        this.dragTargetOverlay = null;
        this.currentManagedBounds = null;
    }

    private boolean shouldInvalidateBounds() {
        boolean viewportChanged;
        Widget viewportWidget;
        boolean chatboxHiddenChanged;
        boolean chatboxBoundsChanged;
        Widget chatbox = this.client.getWidget(WidgetInfo.CHATBOX);
        boolean resizeableChanged = this.isResizeable != this.client.isResized();
        boolean changed = false;
        if (resizeableChanged) {
            this.isResizeable = this.client.isResized();
            changed = true;
        }
        boolean bl = chatboxBoundsChanged = chatbox == null || !chatbox.getBounds().equals(this.chatboxBounds);
        if (chatboxBoundsChanged) {
            this.chatboxBounds = chatbox != null ? chatbox.getBounds() : new Rectangle();
            changed = true;
        }
        boolean bl2 = chatboxHiddenChanged = this.chatboxHidden != (chatbox == null || chatbox.isHidden());
        if (chatboxHiddenChanged) {
            this.chatboxHidden = chatbox == null || chatbox.isHidden();
            changed = true;
        }
        Rectangle viewport = (viewportWidget = this.getViewportLayer()) != null ? viewportWidget.getBounds() : new Rectangle();
        boolean bl3 = viewportChanged = !viewport.equals(this.viewportBounds);
        if (viewportChanged) {
            this.viewportBounds = viewport;
            changed = true;
        }
        return changed;
    }

    private Widget getViewportLayer() {
        if (this.client.isResized()) {
            if (this.client.getVar(4607) == 1) {
                return this.client.getWidget(WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE);
            }
            return this.client.getWidget(WidgetInfo.RESIZABLE_VIEWPORT_OLD_SCHOOL_BOX);
        }
        return this.client.getWidget(WidgetInfo.FIXED_VIEWPORT);
    }

    private OverlayBounds buildSnapCorners() {
        java.awt.Point topLeftPoint = new java.awt.Point(this.viewportBounds.x + 5, this.viewportBounds.y + 20);
        java.awt.Point topCenterPoint = new java.awt.Point(this.viewportBounds.x + this.viewportBounds.width / 2, this.viewportBounds.y + 5);
        java.awt.Point topRightPoint = new java.awt.Point(this.viewportBounds.x + this.viewportBounds.width - 5, topCenterPoint.y);
        java.awt.Point bottomLeftPoint = new java.awt.Point(topLeftPoint.x, this.viewportBounds.y + this.viewportBounds.height - 5);
        java.awt.Point bottomRightPoint = new java.awt.Point(topRightPoint.x, bottomLeftPoint.y);
        if (this.isResizeable && this.chatboxHidden) {
            bottomLeftPoint.y += this.chatboxBounds.height;
        }
        java.awt.Point rightChatboxPoint = this.isResizeable ? new java.awt.Point(this.viewportBounds.x + this.chatboxBounds.width - 5, bottomLeftPoint.y) : bottomRightPoint;
        java.awt.Point canvasTopRightPoint = this.isResizeable ? new java.awt.Point((int)this.client.getRealDimensions().getWidth(), 0) : topRightPoint;
        return new OverlayBounds(new Rectangle(topLeftPoint, SNAP_CORNER_SIZE), new Rectangle(topCenterPoint, SNAP_CORNER_SIZE), new Rectangle(topRightPoint, SNAP_CORNER_SIZE), new Rectangle(bottomLeftPoint, SNAP_CORNER_SIZE), new Rectangle(bottomRightPoint, SNAP_CORNER_SIZE), new Rectangle(rightChatboxPoint, SNAP_CORNER_SIZE), new Rectangle(canvasTopRightPoint, SNAP_CORNER_SIZE));
    }

    private java.awt.Point clampOverlayLocation(int overlayX, int overlayY, int overlayWidth, int overlayHeight, Overlay overlay) {
        Rectangle parentBounds = overlay.getParentBounds();
        if (parentBounds == null || parentBounds.isEmpty()) {
            Dimension dim = this.client.getRealDimensions();
            parentBounds = new Rectangle(0, 0, dim.width, dim.height);
        }
        return new java.awt.Point(Ints.constrainToRange((int)overlayX, (int)parentBounds.x, (int)Math.max(parentBounds.x, parentBounds.x + parentBounds.width - overlayWidth)), Ints.constrainToRange((int)overlayY, (int)parentBounds.y, (int)Math.max(parentBounds.y, parentBounds.y + parentBounds.height - overlayHeight)));
    }

    private static /* synthetic */ int lambda$findMangedOverlay$3(List layerOrder, Overlay c) {
        return layerOrder.indexOf(c.getLayer());
    }

    private static /* synthetic */ boolean lambda$findMangedOverlay$1(List layerOrder, Overlay c) {
        return layerOrder.contains(c.getLayer());
    }
}


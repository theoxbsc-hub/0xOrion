/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.BufferProvider
 *  net.runelite.api.Client
 *  net.runelite.api.MainBufferProvider
 *  net.runelite.api.Renderable
 *  net.runelite.api.Skill
 *  net.runelite.api.events.BeforeMenuRender
 *  net.runelite.api.events.BeforeRender
 *  net.runelite.api.events.FakeXpDrop
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.events.GameTick
 *  net.runelite.api.events.ScriptCallbackEvent
 *  net.runelite.api.hooks.Callbacks
 *  net.runelite.api.hooks.DrawCallbacks
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.api.widgets.WidgetItem
 *  net.runelite.api.worldmap.WorldMap
 *  net.runelite.api.worldmap.WorldMapRenderer
 *  net.runelite.client.callback.ClientThread
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.input.KeyManager
 *  net.runelite.client.input.MouseManager
 *  net.runelite.client.task.Scheduler
 *  net.runelite.client.ui.DrawManager
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.util.DeferredEventBus
 *  net.runelite.client.util.RSTimeUnit
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.minimal;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.BufferProvider;
import net.runelite.api.Client;
import net.runelite.api.MainBufferProvider;
import net.runelite.api.Renderable;
import net.runelite.api.Skill;
import net.runelite.api.events.BeforeMenuRender;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.FakeXpDrop;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.hooks.Callbacks;
import net.runelite.api.hooks.DrawCallbacks;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.api.worldmap.WorldMap;
import net.runelite.api.worldmap.WorldMapRenderer;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.task.Scheduler;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.util.DeferredEventBus;
import net.runelite.client.util.RSTimeUnit;
import net.unethicalite.client.minimal.overlay.MinimalOverlayRenderer;
import net.unethicalite.client.minimal.ui.MinimalUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MinimalHooks
implements Callbacks {
    private static final Logger log = LoggerFactory.getLogger(MinimalHooks.class);
    private static final long CHECK = RSTimeUnit.GAME_TICKS.getDuration().toNanos();
    private static final GameTick GAME_TICK = new GameTick();
    private static final BeforeRender BEFORE_RENDER = new BeforeRender();
    private static Client client;
    private static MainBufferProvider lastMainBufferProvider;
    private static Graphics2D lastGraphics;
    private final MinimalOverlayRenderer renderer;
    private final EventBus eventBus;
    private final DeferredEventBus deferredEventBus;
    private final Scheduler scheduler;
    private final MouseManager mouseManager;
    private final KeyManager keyManager;
    private final ClientThread clientThread;
    private final DrawManager drawManager;
    private final MinimalUI minimalUI;
    private final List<RenderableDrawListener> renderableDrawListeners = new ArrayList<RenderableDrawListener>();
    private Dimension lastStretchedDimensions;
    private VolatileImage stretchedImage;
    private Graphics2D stretchedGraphics;
    private long lastCheck;
    private boolean ignoreNextNpcUpdate;
    private boolean shouldProcessGameTick;

    @Inject
    private MinimalHooks(Client client, MinimalOverlayRenderer renderer, EventBus eventBus, DeferredEventBus deferredEventBus, Scheduler scheduler, MouseManager mouseManager, KeyManager keyManager, ClientThread clientThread, DrawManager drawManager, MinimalUI minimalUI) {
        MinimalHooks.client = client;
        this.renderer = renderer;
        this.eventBus = eventBus;
        this.deferredEventBus = deferredEventBus;
        this.scheduler = scheduler;
        this.mouseManager = mouseManager;
        this.keyManager = keyManager;
        this.clientThread = clientThread;
        this.drawManager = drawManager;
        this.minimalUI = minimalUI;
        eventBus.register((Object)this);
    }

    private static Graphics2D getGraphics(MainBufferProvider mainBufferProvider) {
        if (lastGraphics == null || lastMainBufferProvider != mainBufferProvider) {
            if (lastGraphics != null) {
                log.debug("Graphics reset!");
                lastGraphics.dispose();
            }
            lastMainBufferProvider = mainBufferProvider;
            lastGraphics = (Graphics2D)mainBufferProvider.getImage().getGraphics();
        }
        return lastGraphics;
    }

    private static Image copy(Image src) {
        int width = src.getWidth(null);
        int height = src.getHeight(null);
        BufferedImage image = new BufferedImage(width, height, 1);
        Graphics graphics = image.getGraphics();
        graphics.drawImage(src, 0, 0, width, height, null);
        graphics.dispose();
        return image;
    }

    public static void clearColorBuffer(int x, int y, int width, int height, int color) {
        BufferProvider bp = client.getBufferProvider();
        int canvasWidth = bp.getWidth();
        int[] pixels = bp.getPixels();
        int pixelPos = y * canvasWidth + x;
        int pixelJump = canvasWidth - width;
        for (int cy = y; cy < y + height; ++cy) {
            for (int cx = x; cx < x + width; ++cx) {
                pixels[pixelPos++] = 0;
            }
            pixelPos += pixelJump;
        }
    }

    public static void renderDraw(Renderable renderable, int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z, long hash) {
        DrawCallbacks drawCallbacks = client.getDrawCallbacks();
        if (drawCallbacks != null) {
            drawCallbacks.draw(renderable, orientation, pitchSin, pitchCos, yawSin, yawCos, x, y, z, hash);
        } else {
            renderable.draw(orientation, pitchSin, pitchCos, yawSin, yawCos, x, y, z, hash);
        }
    }

    public static boolean drawMenu() {
        BeforeMenuRender event = new BeforeMenuRender();
        client.getCallbacks().post((Object)event);
        return event.isConsumed();
    }

    public void post(Object event) {
        this.eventBus.post(event);
    }

    public void postDeferred(Object event) {
        this.deferredEventBus.post(event);
    }

    public void tick() {
        if (this.shouldProcessGameTick) {
            this.shouldProcessGameTick = false;
            this.deferredEventBus.replay();
            this.eventBus.post((Object)GAME_TICK);
            int tick = client.getTickCount();
            client.setTickCount(tick + 1);
        }
        this.clientThread.invoke();
        long now = System.nanoTime();
        if (now - this.lastCheck < CHECK) {
            return;
        }
        this.lastCheck = now;
        try {
            this.scheduler.tick();
            this.checkWorldMap();
        }
        catch (Exception ex) {
            log.warn("error during main loop tasks", (Throwable)ex);
        }
    }

    public void frame() {
        this.eventBus.post((Object)BEFORE_RENDER);
    }

    private void checkWorldMap() {
        Widget widget = client.getWidget(WidgetInfo.WORLD_MAP_VIEW);
        if (widget != null) {
            return;
        }
        WorldMap worldMap = client.getWorldMap();
        if (worldMap == null) {
            return;
        }
        WorldMapRenderer manager = worldMap.getWorldMapRenderer();
        if (manager != null && manager.isLoaded()) {
            log.debug("World map was closed, reinitializing");
            worldMap.initializeWorldMap(worldMap.getWorldMapData());
        }
    }

    public MouseEvent mousePressed(MouseEvent mouseEvent) {
        return this.mouseManager.processMousePressed(mouseEvent);
    }

    public MouseEvent mouseReleased(MouseEvent mouseEvent) {
        return this.mouseManager.processMouseReleased(mouseEvent);
    }

    public MouseEvent mouseClicked(MouseEvent mouseEvent) {
        return this.mouseManager.processMouseClicked(mouseEvent);
    }

    public MouseEvent mouseEntered(MouseEvent mouseEvent) {
        return this.mouseManager.processMouseEntered(mouseEvent);
    }

    public MouseEvent mouseExited(MouseEvent mouseEvent) {
        return this.mouseManager.processMouseExited(mouseEvent);
    }

    public MouseEvent mouseDragged(MouseEvent mouseEvent) {
        return this.mouseManager.processMouseDragged(mouseEvent);
    }

    public MouseEvent mouseMoved(MouseEvent mouseEvent) {
        return this.mouseManager.processMouseMoved(mouseEvent);
    }

    public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event) {
        return this.mouseManager.processMouseWheelMoved(event);
    }

    public void keyPressed(KeyEvent keyEvent) {
        this.keyManager.processKeyPressed(keyEvent);
    }

    public void keyReleased(KeyEvent keyEvent) {
        this.keyManager.processKeyReleased(keyEvent);
    }

    public void keyTyped(KeyEvent keyEvent) {
        this.keyManager.processKeyTyped(keyEvent);
    }

    public void registerRenderableDrawListener(RenderableDrawListener listener) {
        this.renderableDrawListeners.add(listener);
    }

    public void unregisterRenderableDrawListener(RenderableDrawListener listener) {
        this.renderableDrawListeners.remove(listener);
    }

    public boolean draw(Renderable renderable, boolean drawingUi) {
        try {
            for (RenderableDrawListener renderableDrawListener : this.renderableDrawListeners) {
                if (renderableDrawListener.draw(renderable, drawingUi)) continue;
                return false;
            }
        }
        catch (Exception ex) {
            log.error("exception from renderable draw listener", (Throwable)ex);
        }
        return true;
    }

    public void draw(MainBufferProvider mainBufferProvider, Graphics graphics, int x, int y) {
        Image finalImage;
        if (graphics == null) {
            return;
        }
        Graphics2D graphics2d = MinimalHooks.getGraphics(mainBufferProvider);
        try {
            this.renderer.renderOverlayLayer(graphics2d, OverlayLayer.ALWAYS_ON_TOP);
        }
        catch (Exception ex) {
            log.warn("Error during overlay rendering", (Throwable)ex);
        }
        if (client.isGpu()) {
            return;
        }
        Image image = mainBufferProvider.getImage();
        if (client.isStretchedEnabled()) {
            GraphicsConfiguration gc = this.minimalUI.getGraphicsConfiguration();
            Dimension stretchedDimensions = client.getStretchedDimensions();
            if (this.lastStretchedDimensions == null || !this.lastStretchedDimensions.equals(stretchedDimensions) || this.stretchedImage != null && this.stretchedImage.validate(gc) == 2) {
                this.stretchedImage = gc.createCompatibleVolatileImage(stretchedDimensions.width, stretchedDimensions.height);
                if (this.stretchedGraphics != null) {
                    this.stretchedGraphics.dispose();
                }
                this.stretchedGraphics = (Graphics2D)this.stretchedImage.getGraphics();
                this.lastStretchedDimensions = stretchedDimensions;
                graphics.setColor(Color.BLACK);
                graphics.fillRect(0, 0, client.getCanvas().getWidth(), client.getCanvas().getHeight());
            }
            this.stretchedGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, client.isStretchedFast() ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR : RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            this.stretchedGraphics.drawImage(image, 0, 0, stretchedDimensions.width, stretchedDimensions.height, null);
            finalImage = this.stretchedImage;
        } else {
            finalImage = image;
        }
        graphics.drawImage(finalImage, 0, 0, client.getCanvas());
        this.drawManager.processDrawComplete(() -> MinimalHooks.copy(finalImage));
    }

    public void drawScene() {
        MainBufferProvider bufferProvider = (MainBufferProvider)client.getBufferProvider();
        Graphics2D graphics2d = MinimalHooks.getGraphics(bufferProvider);
        try {
            this.renderer.renderOverlayLayer(graphics2d, OverlayLayer.ABOVE_SCENE);
        }
        catch (Exception ex) {
            log.warn("Error during overlay rendering", (Throwable)ex);
        }
    }

    public void drawAboveOverheads() {
        MainBufferProvider bufferProvider = (MainBufferProvider)client.getBufferProvider();
        Graphics2D graphics2d = MinimalHooks.getGraphics(bufferProvider);
        try {
            this.renderer.renderOverlayLayer(graphics2d, OverlayLayer.UNDER_WIDGETS);
        }
        catch (Exception ex) {
            log.warn("Error during overlay rendering", (Throwable)ex);
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        switch (gameStateChanged.getGameState()) {
            case LOGGING_IN: 
            case HOPPING: {
                this.ignoreNextNpcUpdate = true;
            }
        }
    }

    public void updateNpcs() {
        if (this.ignoreNextNpcUpdate) {
            this.ignoreNextNpcUpdate = false;
            log.debug("Skipping login updateNpc");
        } else {
            this.shouldProcessGameTick = true;
        }
        this.deferredEventBus.replay();
    }

    public void drawInterface(int interfaceId, List<WidgetItem> widgetItems) {
        MainBufferProvider bufferProvider = (MainBufferProvider)client.getBufferProvider();
        Graphics2D graphics2d = MinimalHooks.getGraphics(bufferProvider);
        try {
            this.renderer.renderAfterInterface(graphics2d, interfaceId, widgetItems);
        }
        catch (Exception ex) {
            log.warn("Error during overlay rendering", (Throwable)ex);
        }
    }

    public void drawLayer(Widget layer, List<WidgetItem> widgetItems) {
        MainBufferProvider bufferProvider = (MainBufferProvider)client.getBufferProvider();
        Graphics2D graphics2d = MinimalHooks.getGraphics(bufferProvider);
        try {
            this.renderer.renderAfterLayer(graphics2d, layer, widgetItems);
        }
        catch (Exception ex) {
            log.warn("Error during overlay rendering", (Throwable)ex);
        }
    }

    @Subscribe
    public void onScriptCallbackEvent(ScriptCallbackEvent scriptCallbackEvent) {
        if (!scriptCallbackEvent.getEventName().equals("fakeXpDrop")) {
            return;
        }
        int[] intStack = client.getIntStack();
        int intStackSize = client.getIntStackSize();
        int statId = intStack[intStackSize - 2];
        int xp = intStack[intStackSize - 1];
        Skill skill = Skill.values()[statId];
        FakeXpDrop fakeXpDrop = new FakeXpDrop(skill, xp);
        this.eventBus.post((Object)fakeXpDrop);
    }

    @FunctionalInterface
    public static interface RenderableDrawListener {
        public boolean draw(Renderable var1, boolean var2);
    }
}


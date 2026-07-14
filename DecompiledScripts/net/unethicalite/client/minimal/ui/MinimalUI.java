/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  javax.annotation.Nullable
 *  javax.inject.Inject
 *  javax.inject.Provider
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Player
 *  net.runelite.api.Point
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.client.RuneLiteProperties
 *  net.runelite.client.callback.ClientThread
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.config.RuneLiteConfig
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.events.ClientShutdown
 *  net.runelite.client.events.ConfigChanged
 *  net.runelite.client.ui.ClientPanel
 *  net.runelite.client.ui.ClientUI
 *  net.runelite.client.ui.ContainableFrame
 *  net.runelite.client.ui.ContainableFrame$Mode
 *  net.runelite.client.util.ImageUtil
 *  net.runelite.client.util.OSType
 *  net.runelite.client.util.OSXUtil
 *  net.runelite.client.util.Text
 *  net.runelite.client.util.WinUtil
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.minimal.ui;

import com.google.common.base.Strings;
import java.applet.Applet;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.RuneLiteProperties;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.ClientPanel;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.ContainableFrame;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.OSType;
import net.runelite.client.util.OSXUtil;
import net.runelite.client.util.Text;
import net.runelite.client.util.WinUtil;
import net.unethicalite.client.config.UnethicaliteConfig;
import net.unethicalite.client.minimal.ui.MinimalToolbar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MinimalUI {
    private static final Logger log = LoggerFactory.getLogger(MinimalUI.class);
    public static final BufferedImage ICON = ImageUtil.loadImageResource(ClientUI.class, (String)"/openosrs.png");
    private static final String CONFIG_GROUP = "runelite";
    private static final String PLUS_CONFIG_GROUP = "runelite";
    private static final String CONFIG_CLIENT_BOUNDS = "clientBounds";
    private static final String CONFIG_CLIENT_MAXIMIZED = "clientMaximized";
    private static final String CONFIG_OPACITY = "enableOpacity";
    private static final String CONFIG_OPACITY_AMOUNT = "opacityPercentage";
    private static final int CLIENT_WELL_HIDDEN_MARGIN = 160;
    private static final int CLIENT_WELL_HIDDEN_MARGIN_TOP = 10;
    public static boolean allowInput = false;
    public static ContainableFrame frame;
    private final RuneLiteConfig config;
    private final Applet client;
    private final ConfigManager configManager;
    private final Provider<ClientThread> clientThreadProvider;
    private final EventBus eventBus;
    private JPanel container;
    private Dimension lastClientSize;
    private Field opacityField;
    private Field peerField;
    private Method setOpacityMethod;
    private Cursor defaultCursor;
    @Inject
    private MinimalToolbar minimalToolbar;
    @Inject
    private UnethicaliteConfig minimalConfig;

    @Inject
    private MinimalUI(RuneLiteConfig config, @Nullable Applet client, ConfigManager configManager, Provider<ClientThread> clientThreadProvider, EventBus eventbus) {
        this.config = config;
        this.client = client;
        this.configManager = configManager;
        this.clientThreadProvider = clientThreadProvider;
        this.eventBus = eventbus;
    }

    public static int getX() {
        return frame.getX();
    }

    public static int getY() {
        return frame.getY();
    }

    public static void setupDefaults() {
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setInitialDelay(300);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        System.setProperty("jgoodies.popupDropShadowEnabled", "false");
        System.setProperty("sun.awt.noerasebackground", "true");
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals("runelite") && (!event.getGroup().equals("runelite") || !event.getKey().equals(CONFIG_OPACITY)) && !event.getKey().equals(CONFIG_OPACITY_AMOUNT) || event.getKey().equals(CONFIG_CLIENT_MAXIMIZED) || event.getKey().equals(CONFIG_CLIENT_BOUNDS)) {
            return;
        }
        SwingUtilities.invokeLater(() -> this.updateFrameConfig(event.getKey().equals("lockWindowSize")));
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() != GameState.LOGGED_IN || !(this.client instanceof Client) || !this.config.usernameInTitle()) {
            return;
        }
        Client client = (Client)this.client;
        ClientThread clientThread = (ClientThread)this.clientThreadProvider.get();
        clientThread.invokeLater(() -> {
            if (client.getGameState() != GameState.LOGGED_IN) {
                return true;
            }
            Player player = client.getLocalPlayer();
            if (player == null) {
                return false;
            }
            String name = player.getName();
            if (Strings.isNullOrEmpty((String)name)) {
                return false;
            }
            frame.setTitle(this.buildTitleWithPlayer(name));
            return true;
        });
    }

    public void init() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MinimalUI.setupDefaults();
            frame = new ContainableFrame();
            OSXUtil.tryEnableFullscreen((JFrame)frame);
            frame.setTitle(RuneLiteProperties.getTitle());
            frame.setIconImage((Image)ICON);
            frame.getLayeredPane().setCursor(Cursor.getDefaultCursor());
            frame.setResizable(true);
            this.container = new JPanel();
            this.container.setLayout(new BoxLayout(this.container, 0));
            this.container.add((Component)new ClientPanel(this.client));
            frame.setJMenuBar((JMenuBar)this.minimalToolbar);
            frame.add((Component)this.container);
            frame.addWindowListener((WindowListener)new WindowAdapter(){

                @Override
                public void windowClosing(WindowEvent e) {
                    MinimalUI.this.shutdownClient();
                }
            });
            frame.setUndecorated(false);
            this.updateFrameConfig(true);
        });
    }

    private void shutdownClient() {
        log.debug("Shutting down client!");
        this.saveClientBoundsConfig();
        ClientShutdown csev = new ClientShutdown();
        this.eventBus.post((Object)csev);
        new Thread(() -> {
            if (this.client != null) {
                int clientShutdownWaitMS;
                if (this.client instanceof Client) {
                    ((Client)this.client).stopNow();
                    clientShutdownWaitMS = 1000;
                } else {
                    this.client.stop();
                    frame.setVisible(false);
                    clientShutdownWaitMS = 6000;
                }
                try {
                    Thread.sleep(clientShutdownWaitMS);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
            }
            System.exit(0);
        }, "Unethicalite Shutdown").start();
    }

    public void show() {
        SwingUtilities.invokeLater(() -> {
            Rectangle clientBounds;
            frame.pack();
            frame.revalidateMinimumSize();
            if (this.config.rememberScreenBounds()) {
                try {
                    clientBounds = (Rectangle)this.configManager.getConfiguration("runelite", CONFIG_CLIENT_BOUNDS, Rectangle.class);
                    if (clientBounds != null) {
                        frame.revalidateMinimumSize();
                        frame.setLocation(clientBounds.getLocation());
                    } else {
                        frame.setLocationRelativeTo((Component)frame.getOwner());
                    }
                    if (this.configManager.getConfiguration("runelite", CONFIG_CLIENT_MAXIMIZED) != null) {
                        frame.setExtendedState(6);
                    }
                }
                catch (Exception ex) {
                    log.warn("Failed to set window bounds", (Throwable)ex);
                    frame.setLocationRelativeTo((Component)frame.getOwner());
                }
            } else {
                frame.setLocationRelativeTo((Component)frame.getOwner());
            }
            clientBounds = frame.getBounds();
            Rectangle screenBounds = frame.getGraphicsConfiguration().getBounds();
            if ((double)(clientBounds.x + clientBounds.width - 160) < screenBounds.getX() || (double)(clientBounds.x + 160) > screenBounds.getX() + screenBounds.getWidth() || (double)(clientBounds.y + 10) < screenBounds.getY() || (double)(clientBounds.y + 160) > screenBounds.getY() + screenBounds.getHeight()) {
                frame.setLocationRelativeTo((Component)frame.getOwner());
            }
            frame.setVisible(true);
            if (this.minimalConfig.minimized()) {
                frame.setState(1);
            } else {
                frame.toFront();
                this.requestFocus();
                this.giveClientFocus();
            }
        });
        if (this.client != null && !(this.client instanceof Client)) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog((Component)frame, "Orion has not yet been updated to work with the latest\ngame update, it will work with reduced functionality until then.", "RuneLite is outdated", 1));
        }
    }

    public void paint(Graphics graphics) {
        assert (SwingUtilities.isEventDispatchThread()) : "paint must be called on EDT";
        frame.paint(graphics);
    }

    public int getWidth() {
        return frame.getWidth();
    }

    public int getHeight() {
        return frame.getHeight();
    }

    public boolean isFocused() {
        return frame.isFocused();
    }

    public void requestFocus() {
        if (OSType.getOSType() == OSType.Windows && !frame.isFocused()) {
            SwingUtilities.invokeLater(() -> {
                if ((frame.getExtendedState() & 6) == 6) {
                    frame.setExtendedState(1);
                    frame.setExtendedState(6);
                } else {
                    int x = MinimalUI.frame.getLocation().x;
                    int y = MinimalUI.frame.getLocation().y;
                    int width = frame.getWidth();
                    int height = frame.getHeight();
                    frame.setExtendedState(1);
                    frame.setExtendedState(0);
                    frame.setLocation(x, y);
                    frame.setSize(width, height);
                }
            });
        }
        frame.requestFocus();
        this.giveClientFocus();
    }

    public Point getCanvasOffset() {
        Canvas canvas;
        if (this.client instanceof Client && (canvas = ((Client)this.client).getCanvas()) != null) {
            java.awt.Point point = SwingUtilities.convertPoint(canvas, 0, 0, (Component)frame);
            return new Point(point.x, point.y);
        }
        return new Point(0, 0);
    }

    public GraphicsConfiguration getGraphicsConfiguration() {
        return frame.getGraphicsConfiguration();
    }

    private void giveClientFocus() {
        if (this.client instanceof Client) {
            Canvas c = ((Client)this.client).getCanvas();
            if (c != null) {
                c.requestFocusInWindow();
            }
        } else if (this.client != null) {
            this.client.requestFocusInWindow();
        }
    }

    private String buildTitleWithPlayer(String rawName) {
        String sanitized = Text.removeTags((String)Strings.nullToEmpty((String)rawName)).replace('\u00a0', ' ').trim();
        String baseTitle = RuneLiteProperties.getTitle();
        return sanitized.isEmpty() ? baseTitle : baseTitle + " - " + sanitized;
    }

    private void updateFrameConfig(boolean updateResizable) {
        int height;
        Boolean opacity;
        if (frame == null) {
            return;
        }
        if (this.config.usernameInTitle() && this.client instanceof Client) {
            Player player = ((Client)this.client).getLocalPlayer();
            if (player != null && player.getName() != null) {
                frame.setTitle(this.buildTitleWithPlayer(player.getName()));
            }
        } else {
            frame.setTitle(RuneLiteProperties.getTitle());
        }
        if (frame.isAlwaysOnTopSupported()) {
            frame.setAlwaysOnTop(this.config.gameAlwaysOnTop());
        }
        if (updateResizable) {
            frame.setResizable(!this.config.lockWindowSize());
        }
        frame.setExpandResizeType(this.config.automaticResizeType());
        ContainableFrame.Mode containMode = this.config.containInScreen();
        if (containMode == ContainableFrame.Mode.ALWAYS) {
            containMode = ContainableFrame.Mode.RESIZING;
        }
        frame.setContainedInScreen(containMode);
        if (!this.config.rememberScreenBounds()) {
            this.configManager.unsetConfiguration("runelite", CONFIG_CLIENT_MAXIMIZED);
            this.configManager.unsetConfiguration("runelite", CONFIG_CLIENT_BOUNDS);
        }
        if ((opacity = (Boolean)this.configManager.getConfiguration("runelite", CONFIG_OPACITY, Boolean.TYPE)) != null && opacity.booleanValue()) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            if (gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
                this.setOpacity();
            } else {
                log.warn("Opacity isn't supported on your system!");
                this.configManager.setConfiguration("runelite", CONFIG_OPACITY, (Object)false);
            }
        } else if (frame.getOpacity() != 1.0f) {
            frame.setOpacity(1.0f);
        }
        if (this.client == null) {
            return;
        }
        int width = Math.max(Math.min(this.config.gameSize().width, 7680), 765);
        Dimension size = new Dimension(width, height = Math.max(Math.min(this.config.gameSize().height, 2160), 503));
        if (!size.equals(this.lastClientSize)) {
            this.lastClientSize = size;
            this.client.setSize(size);
            this.client.setPreferredSize(size);
            this.client.getParent().setPreferredSize(size);
            this.client.getParent().setSize(size);
            if (frame.isVisible()) {
                frame.pack();
            }
        }
    }

    private void saveClientBoundsConfig() {
        if ((frame.getExtendedState() & 6) != 0) {
            this.configManager.setConfiguration("runelite", CONFIG_CLIENT_MAXIMIZED, (Object)true);
        } else {
            Rectangle bounds = frame.getBounds();
            this.configManager.unsetConfiguration("runelite", CONFIG_CLIENT_MAXIMIZED);
            this.configManager.setConfiguration("runelite", CONFIG_CLIENT_BOUNDS, (Object)bounds);
        }
    }

    private void setOpacity() {
        SwingUtilities.invokeLater(() -> {
            try {
                if (this.opacityField == null) {
                    this.opacityField = Window.class.getDeclaredField("opacity");
                    this.opacityField.setAccessible(true);
                }
                if (this.peerField == null) {
                    this.peerField = Component.class.getDeclaredField("peer");
                    this.peerField.setAccessible(true);
                }
                if (this.setOpacityMethod == null) {
                    this.setOpacityMethod = Class.forName("java.awt.peer.WindowPeer").getDeclaredMethod("setOpacity", Float.TYPE);
                }
                float opacity = Float.parseFloat(this.configManager.getConfiguration("runelite", CONFIG_OPACITY_AMOUNT)) / 100.0f;
                assert (opacity > 0.0f && opacity <= 1.0f) : "I don't know who you are, I don't know why you tried, and I don't know how you tried, but this is NOT what you're supposed to do and you should honestly feel terrible about what you did, so I want you to take a nice long amount of time to think about what you just tried to do so you are not gonna do this in the future.";
                this.opacityField.setFloat(frame, opacity);
                this.setOpacityMethod.invoke(this.peerField.get(frame), Float.valueOf(opacity));
            }
            catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException | NullPointerException | InvocationTargetException exception) {
                // empty catch block
            }
        });
    }

    public void resetCursor() {
        if (this.container == null) {
            return;
        }
        this.defaultCursor = null;
        this.container.setCursor(Cursor.getDefaultCursor());
    }

    public void setCursor(BufferedImage image, String name) {
        Cursor cursorAwt;
        if (this.container == null) {
            return;
        }
        java.awt.Point hotspot = new java.awt.Point(0, 0);
        this.defaultCursor = cursorAwt = Toolkit.getDefaultToolkit().createCustomCursor(image, hotspot, name);
        this.setCursor(cursorAwt);
    }

    public void setCursor(Cursor cursor) {
        this.container.setCursor(cursor);
    }

    public void forceFocus() {
        switch (OSType.getOSType()) {
            case MacOS: {
                OSXUtil.requestForeground();
                break;
            }
            case Windows: {
                WinUtil.requestForeground((Frame)frame);
                break;
            }
            default: {
                frame.requestFocus();
            }
        }
        this.giveClientFocus();
    }

    public Cursor getCurrentCursor() {
        return this.container.getCursor();
    }

    public Cursor getDefaultCursor() {
        return this.defaultCursor != null ? this.defaultCursor : Cursor.getDefaultCursor();
    }

    public void setTitle(String title) {
        frame.setTitle(title);
    }

    public static ContainableFrame getFrame() {
        return frame;
    }
}


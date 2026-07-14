/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.input.MouseListener
 *  net.runelite.client.input.MouseManager
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.managers;

import java.awt.event.MouseEvent;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.unethicalite.api.events.MouseAutomated;
import net.unethicalite.client.config.UnethicaliteConfig;
import net.unethicalite.client.managers.LoopedPluginManager;
import net.unethicalite.client.managers.MinimalPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class InputManager
implements MouseListener {
    private static final Logger log = LoggerFactory.getLogger(InputManager.class);
    private final Client client;
    private final MinimalPluginManager minimalPluginManager;
    private final LoopedPluginManager loopedPluginManager;
    private final UnethicaliteConfig interactionConfig;
    private int lastClickX = -1;
    private int lastClickY = -1;
    private int lastMoveX = -1;
    private int lastMoveY = -1;

    @Inject
    public InputManager(MinimalPluginManager minimalPluginManager, LoopedPluginManager loopedPluginManager, MouseManager manager, EventBus eventBus, Client client, UnethicaliteConfig interactionConfig) {
        this.minimalPluginManager = minimalPluginManager;
        this.loopedPluginManager = loopedPluginManager;
        this.client = client;
        this.interactionConfig = interactionConfig;
        eventBus.register((Object)this);
        manager.registerMouseListener((MouseListener)this);
    }

    public MouseEvent mouseClicked(MouseEvent mouseEvent) {
        this.checkIfAutomated(mouseEvent);
        this.setLastClick(mouseEvent.getX(), mouseEvent.getY());
        return mouseEvent;
    }

    public MouseEvent mousePressed(MouseEvent mouseEvent) {
        this.checkIfAutomated(mouseEvent);
        this.setLastClick(mouseEvent.getX(), mouseEvent.getY());
        return mouseEvent;
    }

    public MouseEvent mouseReleased(MouseEvent mouseEvent) {
        this.checkIfAutomated(mouseEvent);
        this.setLastClick(mouseEvent.getX(), mouseEvent.getY());
        return mouseEvent;
    }

    public MouseEvent mouseEntered(MouseEvent mouseEvent) {
        this.checkIfAutomated(mouseEvent);
        this.setLastMove(mouseEvent.getX(), mouseEvent.getY());
        return mouseEvent;
    }

    public MouseEvent mouseExited(MouseEvent mouseEvent) {
        this.checkIfAutomated(mouseEvent);
        this.setLastMove(mouseEvent.getX(), mouseEvent.getY());
        return mouseEvent;
    }

    public MouseEvent mouseDragged(MouseEvent mouseEvent) {
        this.checkIfAutomated(mouseEvent);
        this.setLastMove(mouseEvent.getX(), mouseEvent.getY());
        return mouseEvent;
    }

    public MouseEvent mouseMoved(MouseEvent mouseEvent) {
        this.checkIfAutomated(mouseEvent);
        this.setLastMove(mouseEvent.getX(), mouseEvent.getY());
        return mouseEvent;
    }

    @Subscribe
    private void onMouseAutomated(MouseAutomated event) {
        switch (event.getEventType()) {
            case PRESS: 
            case RELEASE: {
                this.setLastClick(event.getX(), event.getY());
                break;
            }
            case EXIT: 
            case MOVE: {
                this.setLastMove(event.getX(), event.getY());
            }
        }
    }

    private void setLastClick(int x, int y) {
        this.lastClickX = x;
        this.lastClickY = y;
    }

    private void setLastMove(int x, int y) {
        this.lastMoveX = x;
        this.lastMoveY = y;
    }

    private void checkIfAutomated(MouseEvent mouseEvent) {
        if (!this.interactionConfig.disableMouse()) {
            return;
        }
        if ((this.loopedPluginManager.isPluginRegistered() || this.minimalPluginManager.isScriptRunning()) && mouseEvent.getSource() != "unethicalite") {
            mouseEvent.consume();
        }
    }

    public int getLastClickX() {
        return this.lastClickX;
    }

    public int getLastClickY() {
        return this.lastClickY;
    }

    public int getLastMoveX() {
        return this.lastMoveX;
    }

    public int getLastMoveY() {
        return this.lastMoveY;
    }
}


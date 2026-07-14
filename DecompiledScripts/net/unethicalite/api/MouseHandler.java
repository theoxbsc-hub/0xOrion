/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api;

import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public interface MouseHandler
extends MouseListener,
MouseMotionListener,
FocusListener {
    public int getCurrentX();

    public int getCurrentY();

    public long getCurrentMillis();

    public int getCurrentButton();

    public int getIdleCycles();

    public int getLastButton();

    public int getLastPressedX();

    public int getLastPressedY();

    public long getLastPressedMillis();

    public int getLastPendingButton();

    public void setMouseX(int var1);

    public void setMouseY(int var1);

    public void setLastMovedMillis(long var1);

    public void setCurrentButton(int var1);

    public void setIdleCycles(int var1);

    public void setLastButton(int var1);

    public void setLastPressedX(int var1);

    public void setLastPressedY(int var1);

    public void setLastPressedMillis(long var1);

    public void sendClick(int var1, int var2, int var3);

    public void sendRelease();

    public void sendExit();

    public void sendFocusLost();

    public void sendMovement(int var1, int var2);

    public void sendClickMouseEvent(int var1, int var2, int var3);

    default public void sendClickMouseEvent(int x, int y) {
        this.sendClickMouseEvent(x, y, 1);
    }

    public void sendMovementMouseEvent(int var1, int var2);
}


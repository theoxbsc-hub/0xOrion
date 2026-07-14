/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.api;

import java.awt.Dimension;

public interface SystemCalls {
    public long currentTimeMillis();

    public void sleep(long var1);

    public Dimension getScreenSize();

    public void setMousePosition(int var1, int var2);
}


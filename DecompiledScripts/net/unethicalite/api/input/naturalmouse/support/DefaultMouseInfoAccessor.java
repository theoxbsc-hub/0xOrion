/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.support;

import java.awt.MouseInfo;
import java.awt.Point;
import net.unethicalite.api.input.naturalmouse.api.MouseInfoAccessor;

public class DefaultMouseInfoAccessor
implements MouseInfoAccessor {
    @Override
    public Point getMousePosition() {
        return MouseInfo.getPointerInfo().getLocation();
    }
}


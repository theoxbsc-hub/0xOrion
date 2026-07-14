/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input.naturalmouse.support;

import java.awt.Dimension;
import java.awt.Point;
import net.unethicalite.api.input.naturalmouse.api.MouseInfoAccessor;
import net.unethicalite.api.input.naturalmouse.api.SystemCalls;
import net.unethicalite.api.input.naturalmouse.support.DefaultMouseMotionNature;

public class ScreenAdjustedNature
extends DefaultMouseMotionNature {
    private final Point offset;
    private final Dimension screenSize;

    public ScreenAdjustedNature(int x, int y, int x2, int y2) {
        this(new Dimension(x2 - x, y2 - y), new Point(x, y));
        if (y2 <= y || x2 <= x) {
            throw new IllegalArgumentException("Invalid range " + x + " " + y + " " + x2 + " " + y2);
        }
    }

    public ScreenAdjustedNature(Dimension screenSize, Point mouseOffset) {
        this.screenSize = screenSize;
        this.offset = mouseOffset;
    }

    @Override
    public void setMouseInfo(MouseInfoAccessor mouseInfo) {
        super.setMouseInfo(new ProxyMouseInfo(mouseInfo));
    }

    @Override
    public void setSystemCalls(SystemCalls systemCalls) {
        super.setSystemCalls(new ProxySystemCalls(systemCalls));
    }

    private class ProxySystemCalls
    implements SystemCalls {
        private final SystemCalls underlying;

        public ProxySystemCalls(SystemCalls underlying) {
            this.underlying = underlying;
        }

        @Override
        public long currentTimeMillis() {
            return this.underlying.currentTimeMillis();
        }

        @Override
        public void sleep(long time) {
            this.underlying.sleep(time);
        }

        @Override
        public Dimension getScreenSize() {
            return ScreenAdjustedNature.this.screenSize;
        }

        @Override
        public void setMousePosition(int x, int y) {
            this.underlying.setMousePosition(x + ScreenAdjustedNature.this.offset.x, y + ScreenAdjustedNature.this.offset.y);
        }
    }

    private class ProxyMouseInfo
    implements MouseInfoAccessor {
        private final MouseInfoAccessor underlying;
        private final Point p = new Point();

        public ProxyMouseInfo(MouseInfoAccessor underlying) {
            this.underlying = underlying;
        }

        @Override
        public Point getMousePosition() {
            Point realPointer = this.underlying.getMousePosition();
            this.p.setLocation(realPointer.x - ScreenAdjustedNature.this.offset.x, realPointer.y - ScreenAdjustedNature.this.offset.y);
            return this.p;
        }
    }
}


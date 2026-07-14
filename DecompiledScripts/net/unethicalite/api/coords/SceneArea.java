/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.coords;

import net.unethicalite.api.coords.ScenePoint;

public final class SceneArea {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int plane;

    public boolean contains(ScenePoint point) {
        if (point.getPlane() != this.plane) {
            return false;
        }
        return point.getX() >= this.x && point.getX() <= this.x + this.width && point.getY() >= this.y && point.getY() <= this.y + this.height;
    }

    public SceneArea(int x, int y, int width, int height, int plane) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.plane = plane;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getPlane() {
        return this.plane;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SceneArea)) {
            return false;
        }
        SceneArea other = (SceneArea)o;
        if (this.getX() != other.getX()) {
            return false;
        }
        if (this.getY() != other.getY()) {
            return false;
        }
        if (this.getWidth() != other.getWidth()) {
            return false;
        }
        if (this.getHeight() != other.getHeight()) {
            return false;
        }
        return this.getPlane() == other.getPlane();
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getX();
        result = result * 59 + this.getY();
        result = result * 59 + this.getWidth();
        result = result * 59 + this.getHeight();
        result = result * 59 + this.getPlane();
        return result;
    }

    public String toString() {
        return "SceneArea(x=" + this.getX() + ", y=" + this.getY() + ", width=" + this.getWidth() + ", height=" + this.getHeight() + ", plane=" + this.getPlane() + ")";
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.events;

public final class PlaneChanged {
    private final int plane;

    public PlaneChanged(int plane) {
        this.plane = plane;
    }

    public int getPlane() {
        return this.plane;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PlaneChanged)) {
            return false;
        }
        PlaneChanged other = (PlaneChanged)o;
        return this.getPlane() == other.getPlane();
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getPlane();
        return result;
    }

    public String toString() {
        return "PlaneChanged(plane=" + this.getPlane() + ")";
    }
}


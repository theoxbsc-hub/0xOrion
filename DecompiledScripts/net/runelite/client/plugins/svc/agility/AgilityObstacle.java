/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.agility;

import net.runelite.api.coords.WorldPoint;

public class AgilityObstacle {
    private static final int[] NO_ANIMS = new int[0];
    private final int[] ids;
    private final WorldPoint anchor;
    private final WorldPoint stand;
    private final String label;
    private final int[] anims;

    public AgilityObstacle(WorldPoint worldPoint, WorldPoint worldPoint2, String string, int n, int ... nArray) {
        this(worldPoint, worldPoint2, string, new int[]{n}, nArray);
    }

    public AgilityObstacle(WorldPoint worldPoint, WorldPoint worldPoint2, String string, int[] nArray, int[] nArray2) {
        if (nArray == null || nArray.length == 0) {
            throw new IllegalArgumentException("obstacle needs at least one id");
        }
        this.anchor = worldPoint;
        this.stand = worldPoint2;
        this.label = string;
        this.ids = nArray;
        this.anims = nArray2 != null ? nArray2 : NO_ANIMS;
    }

    public int[] ids() {
        return this.ids;
    }

    public int id() {
        return this.ids[0];
    }

    public WorldPoint anchor() {
        return this.anchor;
    }

    public WorldPoint stand() {
        return this.stand;
    }

    public String label() {
        return this.label;
    }

    public int[] anims() {
        return this.anims;
    }

    public boolean producesAnim() {
        return this.anims.length > 0;
    }

    public boolean matchesId(int n) {
        for (int n2 : this.ids) {
            if (n2 != n) continue;
            return true;
        }
        return false;
    }

    public boolean matchesAnim(int n) {
        if (n == -1) {
            return false;
        }
        for (int n2 : this.anims) {
            if (n2 != n) continue;
            return true;
        }
        return false;
    }
}


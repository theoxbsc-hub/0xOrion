/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.randomguard;

import java.util.List;
import net.runelite.api.coords.WorldPoint;

final class NpcInfo {
    final int id;
    final String name;
    final List<String> actions;
    final WorldPoint location;
    final int distance;
    final int animation;

    NpcInfo(int n, String string, List<String> list, WorldPoint worldPoint, int n2, int n3) {
        this.id = n;
        this.name = string == null ? "?" : string;
        this.actions = list;
        this.location = worldPoint;
        this.distance = n2;
        this.animation = n3;
    }

    String tag() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.id).append(':').append(this.name);
        if (this.location != null) {
            stringBuilder.append(" @").append(this.location.getX()).append(',').append(this.location.getY()).append(',').append(this.location.getPlane());
        }
        stringBuilder.append(" d=").append(this.distance);
        if (this.animation != -1) {
            stringBuilder.append(" anim=").append(this.animation);
        }
        stringBuilder.append(" opts=").append(this.actions);
        return stringBuilder.toString();
    }

    String optionsCsv() {
        return this.actions.isEmpty() ? "(none)" : String.join((CharSequence)", ", this.actions);
    }
}


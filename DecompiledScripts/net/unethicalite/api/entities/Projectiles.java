/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Actor
 *  net.runelite.api.Client
 *  net.runelite.api.Projectile
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.entities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.entities.Players;
import net.unethicalite.client.Static;

public class Projectiles {
    private Projectiles() {
    }

    public static List<Projectile> getAll(Predicate<Projectile> filter) {
        ArrayList<Projectile> out = new ArrayList<Projectile>();
        Static.getClient().getProjectiles().forEach(p -> {
            if (filter.test((Projectile)p)) {
                out.add((Projectile)p);
            }
        });
        return out;
    }

    public static List<Projectile> getAll(int ... ids) {
        return Projectiles.getAll(Predicates.ids(ids));
    }

    public static Projectile getNearest(Predicate<Projectile> filter) {
        return Projectiles.getAll(filter).stream().min(Comparator.comparingInt(p -> WorldPoint.fromLocal((Client)Static.getClient(), (int)((int)p.getX()), (int)((int)p.getY()), (int)Static.getClient().getPlane()).distanceTo(Players.getLocal().getWorldLocation()))).orElse(null);
    }

    public static Projectile getNearest(int ... ids) {
        return Projectiles.getNearest(Predicates.ids(ids));
    }

    public static Projectile getNearest(Actor target) {
        return Projectiles.getNearest((Projectile x) -> x.getInteracting() != null && x.getInteracting().equals(target));
    }

    public static Projectile getNearest(WorldPoint startPoint) {
        LocalPoint localPoint = LocalPoint.fromWorld((Client)Static.getClient(), (WorldPoint)startPoint);
        if (localPoint == null) {
            return null;
        }
        return Projectiles.getNearest((Projectile x) -> x.getX1() == localPoint.getX() && x.getY1() == localPoint.getY());
    }
}


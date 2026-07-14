/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Player
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.entities;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.entities.Entities;
import net.unethicalite.api.query.entities.PlayerQuery;
import net.unethicalite.client.Static;

public class Players
extends Entities<Player> {
    private static final Players PLAYERS = new Players();

    private Players() {
    }

    @Override
    protected List<Player> all(Predicate<? super Player> filter) {
        return Static.getClient().getPlayers().stream().filter(filter).collect(Collectors.toList());
    }

    public static PlayerQuery query() {
        return Players.query(Players::getAll);
    }

    public static PlayerQuery query(Supplier<List<Player>> supplier) {
        return new PlayerQuery(supplier);
    }

    public static List<Player> getAll() {
        return Players.getAll((Player player) -> true);
    }

    public static List<Player> getAll(Predicate<Player> filter) {
        return PLAYERS.all((Predicate<? super Player>)filter);
    }

    public static List<Player> getAll(String ... names) {
        return PLAYERS.all(names);
    }

    public static Player getNearest(Predicate<Player> filter) {
        return Players.getNearest(Players.getLocal().getWorldLocation(), filter);
    }

    public static Player getNearest(String ... names) {
        return Players.getNearest(Players.getLocal().getWorldLocation(), names);
    }

    public static Player getNearest(WorldPoint to, Predicate<Player> filter) {
        return PLAYERS.nearest(to, filter);
    }

    public static Player getNearest(WorldPoint to, String ... names) {
        return (Player)PLAYERS.nearest(to, names);
    }

    public static Player getHintArrowed() {
        return Static.getClient().getHintArrowPlayer();
    }

    public static Player getLocal() {
        Player local = Static.getClient().getLocalPlayer();
        if (local == null) {
            throw new IllegalStateException("Local player was null, are you logged in?");
        }
        return local;
    }
}


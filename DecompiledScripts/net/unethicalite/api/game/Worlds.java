/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.GameState
 *  net.runelite.api.MenuAction
 *  net.runelite.api.World
 *  net.runelite.api.WorldType
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.http.api.worlds.WorldResult
 *  net.runelite.http.api.worlds.WorldType
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.api.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.World;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.http.api.worlds.WorldResult;
import net.runelite.http.api.worlds.WorldType;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Worlds {
    private static final Logger log = LoggerFactory.getLogger(Worlds.class);

    private static List<World> lookup() {
        ArrayList<World> out = new ArrayList<World>();
        WorldResult lookup = Static.getWorldService().getWorlds();
        if (lookup == null) {
            return Collections.emptyList();
        }
        lookup.getWorlds().forEach(w -> {
            World world = Static.getClient().createWorld();
            world.setActivity(w.getActivity());
            world.setAddress(w.getAddress());
            world.setId(w.getId());
            world.setPlayerCount(w.getPlayers());
            world.setLocation(w.getLocation());
            EnumSet<net.runelite.api.WorldType> types = EnumSet.noneOf(net.runelite.api.WorldType.class);
            w.getTypes().stream().map(Worlds::toApiWorldType).forEach(types::add);
            world.setTypes(types);
            out.add(world);
        });
        Static.getClient().setWorldList(out.toArray(new World[0]));
        return out;
    }

    public static List<World> getAll(Predicate<World> filter) {
        List<World> loadedWorlds;
        ArrayList<World> out = new ArrayList<World>();
        try {
            World[] worlds = Static.getClient().getWorldList();
            if (worlds == null) {
                Worlds.loadWorlds();
                return out;
            }
            loadedWorlds = Arrays.asList(worlds);
        }
        catch (Exception e) {
            log.warn("Game couldn't load worlds, falling back to RuneLite API.");
            loadedWorlds = Worlds.lookup();
        }
        for (World world : loadedWorlds) {
            if (!filter.test(world)) continue;
            out.add(world);
        }
        return out;
    }

    public static World getFirst(Predicate<World> filter) {
        return Worlds.getAll(filter).stream().findFirst().orElse(null);
    }

    public static World getFirst(int id) {
        return Worlds.getFirst(x -> x.getId() == id);
    }

    public static World getRandom(Predicate<World> filter) {
        List<World> all = Worlds.getAll(filter);
        if (all.isEmpty()) {
            return null;
        }
        return all.get(Rand.nextInt(0, all.size()));
    }

    public static int getCurrentId() {
        return Static.getClient().getWorld();
    }

    public static void hopTo(World world) {
        Worlds.hopTo(world, false);
    }

    public static void hopTo(World world, boolean spam) {
        Widget rememberOption;
        if (!Worlds.isHopperOpen()) {
            Worlds.openHopper();
            Time.sleepUntil(Worlds::isHopperOpen, 3000);
        }
        if (Widgets.isVisible(rememberOption = (Widget)Dialog.getOptions().stream().filter(x -> x.getText().contains("Yes. In future, only warn about")).findFirst().orElse(null))) {
            Dialog.chooseOption(2);
            Time.sleepUntil(() -> Game.getState() == GameState.HOPPING, 3000);
            return;
        }
        log.debug("Hoping to world {}", (Object)world.getId());
        Static.getClient().interact(1, MenuAction.CC_OP.getId(), world.getId(), WidgetInfo.WORLD_SWITCHER_LIST.getId());
        if (!spam) {
            Time.sleepUntil(() -> Game.getState() == GameState.HOPPING, 3000);
        }
        if (Dialog.isViewingOptions()) {
            Dialog.chooseOption(2);
            Time.sleepUntil(() -> Game.getState() == GameState.HOPPING, 3000);
        }
    }

    public static World getCurrentWorld() {
        return Worlds.getFirst(Static.getClient().getWorld());
    }

    public static boolean inMembersWorld() {
        return Worlds.inMembersWorld(false);
    }

    public static boolean inMembersWorld(boolean useLookUp) {
        World currentWorld;
        if (useLookUp && (currentWorld = (World)Worlds.lookup().stream().filter(x -> x.getId() == Worlds.getCurrentId()).findFirst().orElse(null)) != null) {
            return currentWorld.isMembers();
        }
        return Static.getClient().isMembersWorld();
    }

    public static void loadWorlds() {
        if (Game.isOnLoginScreen()) {
            Worlds.openLobbyWorlds();
            Time.sleep(200L);
            Worlds.closeLobbyWorlds();
            return;
        }
        if (Game.isLoggedIn()) {
            Worlds.openHopper();
        }
    }

    public static void openHopper() {
        if (!Tabs.isOpen(Tab.LOG_OUT)) {
            Tabs.open(Tab.LOG_OUT);
        }
        Static.getClient().interact(1, MenuAction.CC_OP.getId(), -1, WidgetInfo.WORLD_SWITCHER_BUTTON.getId());
    }

    public static void openLobbyWorlds() {
        Static.getClient().loadWorlds();
        Static.getClient().setWorldSelectOpen(true);
    }

    public static void closeLobbyWorlds() {
        Static.getClient().setWorldSelectOpen(false);
    }

    public static boolean isHopperOpen() {
        return Widgets.isVisible(Widgets.get(WidgetInfo.WORLD_SWITCHER_LIST));
    }

    private static net.runelite.api.WorldType toApiWorldType(WorldType httpWorld) {
        if (httpWorld == WorldType.TOURNAMENT) {
            return net.runelite.api.WorldType.TOURNAMENT_WORLD;
        }
        return net.runelite.api.WorldType.valueOf((String)httpWorld.name());
    }
}


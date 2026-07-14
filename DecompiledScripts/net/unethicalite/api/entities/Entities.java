/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.MenuAction
 *  net.runelite.api.MenuEntry
 *  net.runelite.api.Tile
 *  net.runelite.api.coords.WorldPoint
 */
package net.unethicalite.api.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.SceneEntity;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.client.Static;

public abstract class Entities<T extends SceneEntity> {
    protected abstract List<T> all(Predicate<? super T> var1);

    protected List<T> all(String ... names) {
        return this.all(Predicates.names(names));
    }

    protected List<T> all(int ... ids) {
        return this.all(Predicates.ids(ids));
    }

    protected T nearest(WorldPoint to, Predicate<? super T> filter) {
        return (T)((SceneEntity)this.all((? super T x) -> x.getId() != -1 && filter.test(x)).stream().min(Comparator.comparingDouble(t -> t.getWorldLocation().distanceToHypotenuse(to))).orElse(null));
    }

    protected T nearest(WorldPoint to, String ... names) {
        return this.nearest(to, Predicates.names(names));
    }

    protected T nearest(WorldPoint to, int ... ids) {
        return this.nearest(to, Predicates.ids(ids));
    }

    public static List<? extends SceneEntity> getHoveredEntities() {
        MenuEntry[] menuEntries = Static.getClient().getMenuEntries();
        if (menuEntries.length == 0) {
            return Collections.emptyList();
        }
        ArrayList<Object> out = new ArrayList<Object>();
        block6: for (MenuEntry menuEntry : menuEntries) {
            MenuAction menuAction = menuEntry.getType();
            switch (menuAction) {
                case EXAMINE_OBJECT: 
                case ITEM_USE_ON_GAME_OBJECT: 
                case WIDGET_TARGET_ON_GAME_OBJECT: 
                case GAME_OBJECT_FIRST_OPTION: 
                case GAME_OBJECT_SECOND_OPTION: 
                case GAME_OBJECT_THIRD_OPTION: 
                case GAME_OBJECT_FOURTH_OPTION: 
                case GAME_OBJECT_FIFTH_OPTION: {
                    int x = menuEntry.getParam0();
                    int y = menuEntry.getParam1();
                    int id = menuEntry.getIdentifier();
                    Tile tile = Static.getClient().getScene().getTiles()[Static.getClient().getPlane()][x][y];
                    out.addAll(TileObjects.getAt(tile, id));
                    continue block6;
                }
                case EXAMINE_NPC: 
                case ITEM_USE_ON_NPC: 
                case WIDGET_TARGET_ON_NPC: 
                case NPC_FIRST_OPTION: 
                case NPC_SECOND_OPTION: 
                case NPC_THIRD_OPTION: 
                case NPC_FOURTH_OPTION: 
                case NPC_FIFTH_OPTION: {
                    int index = menuEntry.getIdentifier();
                    out.add(Static.getClient().getCachedNPCs()[index]);
                    continue block6;
                }
                case EXAMINE_ITEM_GROUND: 
                case ITEM_USE_ON_GROUND_ITEM: 
                case WIDGET_TARGET_ON_GROUND_ITEM: 
                case GROUND_ITEM_FIRST_OPTION: 
                case GROUND_ITEM_SECOND_OPTION: 
                case GROUND_ITEM_THIRD_OPTION: 
                case GROUND_ITEM_FOURTH_OPTION: 
                case GROUND_ITEM_FIFTH_OPTION: {
                    int x = menuEntry.getParam0();
                    int y = menuEntry.getParam1();
                    int id = menuEntry.getIdentifier();
                    Tile tile = Static.getClient().getScene().getTiles()[Static.getClient().getPlane()][x][y];
                    out.addAll(TileItems.getAt(tile, id));
                    continue block6;
                }
                case ITEM_USE_ON_PLAYER: 
                case WIDGET_TARGET_ON_PLAYER: 
                case PLAYER_FIRST_OPTION: 
                case PLAYER_SECOND_OPTION: 
                case PLAYER_THIRD_OPTION: 
                case PLAYER_FOURTH_OPTION: 
                case PLAYER_FIFTH_OPTION: 
                case PLAYER_SIXTH_OPTION: 
                case PLAYER_SEVENTH_OPTION: 
                case PLAYER_EIGHTH_OPTION: {
                    out.add(Static.getClient().getCachedPlayers()[menuEntry.getIdentifier()]);
                    continue block6;
                }
            }
        }
        return out;
    }
}


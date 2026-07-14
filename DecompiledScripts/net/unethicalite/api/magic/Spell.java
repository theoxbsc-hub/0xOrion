/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Item
 *  net.runelite.api.NPC
 *  net.runelite.api.Player
 *  net.runelite.api.TileItem
 *  net.runelite.api.TileObject
 *  net.runelite.api.widgets.WidgetInfo
 */
package net.unethicalite.api.magic;

import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.Interactable;
import net.unethicalite.api.magic.Magic;

public interface Spell {
    public int getLevel();

    public WidgetInfo getWidget();

    public boolean canCast();

    default public void cast() {
        Magic.cast(this);
    }

    default public void castOn(Interactable interactable) {
        if (interactable instanceof Item) {
            Magic.cast(this, (Item)interactable);
            return;
        }
        if (interactable instanceof NPC) {
            Magic.cast(this, (NPC)interactable);
            return;
        }
        if (interactable instanceof Player) {
            Magic.cast(this, (Player)interactable);
            return;
        }
        if (interactable instanceof TileItem) {
            Magic.cast(this, (TileItem)interactable);
            return;
        }
        if (interactable instanceof TileObject) {
            Magic.cast(this, (TileObject)interactable);
        }
    }
}


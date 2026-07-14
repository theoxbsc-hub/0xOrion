/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Actor
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.InventoryID
 *  net.runelite.api.Item
 *  net.runelite.api.ItemComposition
 *  net.runelite.api.ItemContainer
 *  net.runelite.api.NPC
 *  net.runelite.api.ObjectComposition
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.TileItem
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.client.ui.overlay.OverlayPanel
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.components.LineComponent
 *  net.runelite.client.ui.overlay.components.TitleComponent
 */
package net.runelite.client.plugins.svc.foundation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.util.Iterator;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.foundation.EntityInspectorPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;

class EntityInspectorOverlay
extends OverlayPanel {
    private final Client client;
    private final EntityInspectorPlugin plugin;

    EntityInspectorOverlay(Client client, EntityInspectorPlugin entityInspectorPlugin) {
        this.client = client;
        this.plugin = entityInspectorPlugin;
        this.setPosition(OverlayPosition.TOP_LEFT);
        this.setPreferredSize(new Dimension(300, 800));
    }

    /*
     * WARNING - void declaration
     */
    public Dimension render(Graphics2D graphics2D) {
        Object object;
        Object object2;
        Object object3;
        Object object4;
        Object object5;
        Object object6;
        if (!this.client.getGameState().equals((Object)GameState.LOGGED_IN)) {
            return null;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return null;
        }
        WorldPoint worldPoint = player.getWorldLocation();
        this.getPanelComponent().getChildren().add(TitleComponent.builder().text("Entity Inspector").color(Color.ORANGE).build());
        this.header("MOUSE");
        try {
            object6 = this.client.getCanvas();
            object5 = ((Component)object6).getLocationOnScreen();
            object4 = MouseInfo.getPointerInfo().getLocation();
            int n = ((Point)object4).x - ((Point)object5).x;
            int n2 = ((Point)object4).y - ((Point)object5).y;
            this.addLine("Canvas", n + ", " + n2);
        }
        catch (Exception exception) {
            this.addLine("Canvas", "unavailable");
        }
        object6 = this.plugin.atCursorTile;
        object5 = this.plugin.atCursorInfo;
        if (!((String)object6).isEmpty() || !((String)object5).isEmpty()) {
            this.header("AT CURSOR");
            if (!((String)object6).isEmpty()) {
                this.addLine("Tile", (String)object6);
            }
            if (!((String)object5).isEmpty()) {
                this.addLine("Entity", (String)object5);
            }
        }
        if (!((String)(object4 = this.plugin.lastExamineInfo)).isEmpty()) {
            this.header("LAST EXAMINED");
            this.addLine("", (String)object4);
        }
        this.header("PLAYER");
        this.addLine("Pos", worldPoint.getX() + ", " + worldPoint.getY() + " P" + worldPoint.getPlane());
        this.addLine("Region", String.valueOf(worldPoint.getRegionID()));
        this.addLine("HP", this.client.getBoostedSkillLevel(Skill.HITPOINTS) + "/" + this.client.getRealSkillLevel(Skill.HITPOINTS));
        this.addLine("Prayer", this.client.getBoostedSkillLevel(Skill.PRAYER) + "/" + this.client.getRealSkillLevel(Skill.PRAYER));
        this.addLine("Run", this.client.getEnergy() / 100 + "%");
        this.addLine("Spec", this.client.getVarpValue(300) / 10 + "%");
        this.addLine("Anim", String.valueOf(player.getAnimation()));
        Actor actor = player.getInteracting();
        if (actor instanceof NPC) {
            NPC nPC2 = (NPC)actor;
            object3 = nPC2.getName() != null ? nPC2.getName() : "?";
            this.header("COMBAT TARGET");
            this.addLineHighlight("Name", (String)object3);
            this.addLineHighlight("NPC ID", String.valueOf(nPC2.getId()));
            this.addLineHighlight("Anim ID", String.valueOf(nPC2.getAnimation()));
            this.addLine("HP ratio", String.valueOf(nPC2.getHealthRatio()));
            this.addLine("Interacting", nPC2.getInteracting() != null ? nPC2.getInteracting().getName() : "none");
            this.addLine("Distance", String.valueOf(nPC2.getWorldLocation().distanceTo(worldPoint)));
        } else {
            this.header("COMBAT TARGET");
            this.addLine("", "none");
        }
        this.header("INVENTORY");
        ItemContainer itemContainer = this.client.getItemContainer(InventoryID.INVENTORY);
        if (itemContainer != null) {
            for (Item object7 : itemContainer.getItems()) {
                if (object7.getId() == -1) continue;
                object2 = this.client.getItemDefinition(object7.getId());
                String n6 = object2 != null ? object2.getName() : "Unknown";
                this.addLine(n6, "ID:" + object7.getId() + " x" + object7.getQuantity());
            }
        }
        this.header("NEARBY NPCs (r=10)");
        object3 = NPCs.getAll(nPC -> nPC != null && nPC.getWorldLocation().distanceTo(worldPoint) <= 10);
        int n = 0;
        Object object8 = object3.iterator();
        while (object8.hasNext()) {
            NPC nPC2 = (NPC)object8.next();
            if (n++ >= 6) break;
            object2 = nPC2.getName() != null ? nPC2.getName() : "null";
            int n2 = nPC2.getWorldLocation().distanceTo(worldPoint);
            int n3 = nPC2.getAnimation();
            object = n3 >= 0 ? " anim:" + n3 : "";
            this.addLine((String)object2 + (String)object, "ID:" + nPC2.getId() + " d:" + n2);
        }
        this.header("NEARBY OBJECTS (r=5)");
        object8 = TileObjects.getAll(tileObject -> tileObject != null && tileObject.getWorldLocation().distanceTo(worldPoint) <= 5);
        boolean bl = false;
        object2 = object8.iterator();
        while (object2.hasNext()) {
            void var12_21;
            TileObject tileObject2 = (TileObject)object2.next();
            if (++var12_21 >= 6) break;
            ObjectComposition objectComposition = this.client.getObjectDefinition(tileObject2.getId());
            object = objectComposition != null ? objectComposition.getName() : "Unknown";
            this.addLine((String)object, "ID:" + tileObject2.getId());
        }
        this.header("GROUND ITEMS (r=5)");
        object2 = TileItems.getAll(tileItem -> tileItem != null && tileItem.getWorldLocation().distanceTo(worldPoint) <= 5);
        int n4 = 0;
        Iterator iterator = object2.iterator();
        while (iterator.hasNext()) {
            object = (TileItem)iterator.next();
            if (n4++ >= 6) break;
            ItemComposition itemComposition = this.client.getItemDefinition(object.getId());
            String string = itemComposition != null ? itemComposition.getName() : "Unknown";
            this.addLine(string, "ID:" + object.getId() + " x" + object.getQuantity());
        }
        return super.render(graphics2D);
    }

    private void header(String string) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left("--- " + string + " ---").leftColor(Color.YELLOW).build());
    }

    private void addLine(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string).right(string2).build());
    }

    private void addLineHighlight(String string, String string2) {
        this.getPanelComponent().getChildren().add(LineComponent.builder().left(string).right(string2).rightColor(Color.CYAN).build());
    }
}


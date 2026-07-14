package net.runelite.client.plugins.ox.debug;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.util.List;
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
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;

class DebugOverlay extends OverlayPanel {
    private static final Color PURPLE = new Color(0xB4, 0x4D, 0xFF);

    private final Client client;
    private final DebugPlugin plugin;

    DebugOverlay(Client client, DebugPlugin plugin) {
        this.client = client;
        this.plugin = plugin;
        this.setPosition(OverlayPosition.TOP_LEFT);
        this.setPreferredSize(new Dimension(300, 800));
    }

    @Override
    public Dimension render(Graphics2D g) {
        if (!this.client.getGameState().equals(GameState.LOGGED_IN)) {
            return null;
        }
        Player player = this.client.getLocalPlayer();
        if (player == null) {
            return null;
        }
        WorldPoint loc = player.getWorldLocation();

        this.getPanelComponent().getChildren().add(
            TitleComponent.builder().text("0xDEBUG").color(PURPLE).build()
        );

        this.header("MOUSE");
        try {
            Component canvas = this.client.getCanvas();
            Point canvasOrigin = canvas.getLocationOnScreen();
            Point mouse = MouseInfo.getPointerInfo().getLocation();
            int cx = mouse.x - canvasOrigin.x;
            int cy = mouse.y - canvasOrigin.y;
            this.addLine("Canvas", cx + ", " + cy);
        } catch (Exception e) {
            this.addLine("Canvas", "unavailable");
        }

        if (!this.plugin.atCursorTile.isEmpty() || !this.plugin.atCursorInfo.isEmpty()) {
            this.header("AT CURSOR");
            if (!this.plugin.atCursorTile.isEmpty()) {
                this.addLine("Tile", this.plugin.atCursorTile);
            }
            if (!this.plugin.atCursorInfo.isEmpty()) {
                this.addLine("Entity", this.plugin.atCursorInfo);
            }
        }
        if (!this.plugin.lastExamineInfo.isEmpty()) {
            this.header("LAST EXAMINED");
            this.addLine("", this.plugin.lastExamineInfo);
        }

        this.header("PLAYER");
        this.addLine("Pos", loc.getX() + ", " + loc.getY() + " P" + loc.getPlane());
        this.addLine("Region", String.valueOf(loc.getRegionID()));
        this.addLine("HP", this.client.getBoostedSkillLevel(Skill.HITPOINTS)
            + "/" + this.client.getRealSkillLevel(Skill.HITPOINTS));
        this.addLine("Prayer", this.client.getBoostedSkillLevel(Skill.PRAYER)
            + "/" + this.client.getRealSkillLevel(Skill.PRAYER));
        this.addLine("Run", this.client.getEnergy() / 100 + "%");
        this.addLine("Spec", this.client.getVarpValue(300) / 10 + "%");
        this.addLine("Anim", String.valueOf(player.getAnimation()));

        Actor interacting = player.getInteracting();
        this.header("COMBAT TARGET");
        if (interacting instanceof NPC) {
            NPC npc = (NPC) interacting;
            String name = npc.getName() != null ? npc.getName() : "?";
            this.addLineHighlight("Name", name);
            this.addLineHighlight("NPC ID", String.valueOf(npc.getId()));
            this.addLineHighlight("Anim ID", String.valueOf(npc.getAnimation()));
            this.addLine("HP ratio", String.valueOf(npc.getHealthRatio()));
            this.addLine("Interacting",
                npc.getInteracting() != null ? npc.getInteracting().getName() : "none");
            this.addLine("Distance", String.valueOf(npc.getWorldLocation().distanceTo(loc)));
        } else {
            this.addLine("", "none");
        }

        this.header("INVENTORY");
        ItemContainer inv = this.client.getItemContainer(InventoryID.INVENTORY);
        if (inv != null) {
            for (Item item : inv.getItems()) {
                if (item.getId() == -1) {
                    continue;
                }
                ItemComposition def = this.client.getItemDefinition(item.getId());
                String name = def != null ? def.getName() : "Unknown";
                this.addLine(name, "ID:" + item.getId() + " x" + item.getQuantity());
            }
        }

        this.header("NEARBY NPCs (r=10)");
        List<NPC> npcs = NPCs.getAll(n -> n != null && n.getWorldLocation().distanceTo(loc) <= 10);
        int npcCount = 0;
        for (NPC npc : npcs) {
            if (npcCount++ >= 6) {
                break;
            }
            String name = npc.getName() != null ? npc.getName() : "null";
            int dist = npc.getWorldLocation().distanceTo(loc);
            int anim = npc.getAnimation();
            String animBit = anim >= 0 ? " anim:" + anim : "";
            this.addLine(name + animBit, "ID:" + npc.getId() + " d:" + dist);
        }

        this.header("NEARBY OBJECTS (r=5)");
        List<TileObject> objects = TileObjects.getAll(
            o -> o != null && o.getWorldLocation().distanceTo(loc) <= 5
        );
        int objCount = 0;
        for (TileObject obj : objects) {
            if (objCount++ >= 6) {
                break;
            }
            ObjectComposition def = this.client.getObjectDefinition(obj.getId());
            String name = def != null ? def.getName() : "Unknown";
            this.addLine(name, "ID:" + obj.getId());
        }

        this.header("GROUND ITEMS (r=5)");
        List<TileItem> ground = TileItems.getAll(
            i -> i != null && i.getWorldLocation().distanceTo(loc) <= 5
        );
        int itemCount = 0;
        for (TileItem tileItem : ground) {
            if (itemCount++ >= 6) {
                break;
            }
            ItemComposition def = this.client.getItemDefinition(tileItem.getId());
            String name = def != null ? def.getName() : "Unknown";
            this.addLine(name, "ID:" + tileItem.getId() + " x" + tileItem.getQuantity());
        }

        return super.render(g);
    }

    private void header(String title) {
        this.getPanelComponent().getChildren().add(
            LineComponent.builder().left("--- " + title + " ---").leftColor(PURPLE).build()
        );
    }

    private void addLine(String left, String right) {
        this.getPanelComponent().getChildren().add(
            LineComponent.builder().left(left).right(right).build()
        );
    }

    private void addLineHighlight(String left, String right) {
        this.getPanelComponent().getChildren().add(
            LineComponent.builder().left(left).right(right).rightColor(Color.CYAN).build()
        );
    }
}

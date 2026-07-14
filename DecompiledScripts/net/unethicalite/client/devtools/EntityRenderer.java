/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Actor
 *  net.runelite.api.Client
 *  net.runelite.api.DecorativeObject
 *  net.runelite.api.DynamicObject
 *  net.runelite.api.GameObject
 *  net.runelite.api.GroundObject
 *  net.runelite.api.ItemLayer
 *  net.runelite.api.NPC
 *  net.runelite.api.Perspective
 *  net.runelite.api.Player
 *  net.runelite.api.Point
 *  net.runelite.api.Projectile
 *  net.runelite.api.Renderable
 *  net.runelite.api.Tile
 *  net.runelite.api.TileItem
 *  net.runelite.api.TileObject
 *  net.runelite.api.WallObject
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.api.widgets.WidgetItem
 *  net.runelite.api.worldmap.WorldMap
 *  net.runelite.api.worldmap.WorldMapData
 *  net.runelite.client.ui.FontManager
 *  net.runelite.client.ui.overlay.OverlayUtil
 *  net.runelite.client.ui.overlay.tooltip.Tooltip
 *  net.runelite.client.ui.overlay.tooltip.TooltipManager
 */
package net.unethicalite.client.devtools;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.DynamicObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.ItemLayer;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.api.Renderable;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.api.worldmap.WorldMap;
import net.runelite.api.worldmap.WorldMapData;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.unethicalite.api.SceneEntity;
import net.unethicalite.api.coords.RegionPoint;
import net.unethicalite.api.coords.ScenePoint;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.Projectiles;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.api.utils.DrawUtils;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

@Singleton
public class EntityRenderer {
    private static final Font FONT = FontManager.getRunescapeFont().deriveFont(1, 16.0f);
    private static final Color RED = new Color(221, 44, 0);
    private static final Color GREEN = new Color(0, 200, 83);
    private static final Color TURQOISE = new Color(0, 200, 157);
    private static final Color ORANGE = new Color(255, 109, 0);
    private static final Color YELLOW = new Color(255, 214, 0);
    private static final Color CYAN = new Color(0, 184, 212);
    private static final Color BLUE = new Color(41, 98, 255);
    private static final Color DEEP_PURPLE = new Color(98, 0, 234);
    private static final Color PURPLE = new Color(170, 0, 255);
    private static final Color GRAY = new Color(158, 158, 158);
    private final Client client;
    private final TooltipManager tooltipManager;
    private boolean groundObjects;
    private boolean wallObjects;
    private boolean decorativeObjects;
    private boolean gameObjects;
    private boolean graphicsObjects;
    private boolean inventory;
    private boolean npcs;
    private boolean players;
    private boolean tileItems;
    private boolean projectiles;
    private boolean tileLocation;
    private boolean collisionMap;
    private boolean path;
    private boolean ids = true;
    private boolean names = true;
    private boolean indexes = true;
    private boolean animations = true;
    private boolean graphics = true;
    private boolean actions = true;
    private boolean worldLocations = true;
    private boolean quantities = true;
    private boolean trueWorldLocations = true;
    private List<WorldPoint> currentPath = null;

    @Inject
    public EntityRenderer(Client client, TooltipManager tooltipManager) {
        this.client = client;
        this.tooltipManager = tooltipManager;
    }

    public void render(Graphics2D g) {
        g.setFont(FONT);
        Point mouse = this.client.getMouseCanvasPosition();
        if (this.gameObjects || this.groundObjects || this.wallObjects || this.decorativeObjects) {
            this.renderTileObjects(g, mouse);
        }
        if (this.tileItems) {
            this.renderTileItems(g, mouse);
        }
        if (this.npcs) {
            this.renderNpcs(g, mouse);
        }
        if (this.players) {
            this.renderPlayers(g, mouse);
        }
        if (this.projectiles) {
            this.renderProjectiles(g);
        }
        if (this.graphicsObjects) {
            this.renderGraphicsObjects(g);
        }
        if (this.inventory) {
            this.renderInventory(g);
        }
        Tile hoveredTile = Tiles.getHoveredTile();
        if (this.tileLocation) {
            this.renderTileTooltip(g, hoveredTile);
        }
        if (hoveredTile == null) {
            return;
        }
        if (this.path && this.currentPath != null) {
            DrawUtils.drawPath(g, this.currentPath);
        }
        if (this.collisionMap) {
            DrawUtils.drawCollisions(g);
        }
    }

    public void renderPlayers(Graphics2D graphics, Point point) {
        Player local = Players.getLocal();
        List<Player> players = Players.getAll(x -> !x.equals(local));
        for (Player p : players) {
            if (p.getConvexHull() == null || !p.getConvexHull().contains(point.getX(), point.getY())) continue;
            graphics.setColor(BLUE);
            graphics.draw(p.getConvexHull());
            OverlayUtil.renderActorOverlay((Graphics2D)graphics, (Actor)p, (String)"", (Color)BLUE);
            this.tooltipManager.add(new Tooltip(this.createInfo((SceneEntity)p)));
        }
        graphics.setColor(CYAN);
        OverlayUtil.renderActorParagraph((Graphics2D)graphics, (Actor)local, (String)this.createInfo((SceneEntity)local), (Color)CYAN);
        this.renderPlayerWireframe(graphics, local, CYAN);
    }

    public void renderNpcs(Graphics2D graphics, Point point) {
        List<NPC> npcs = NPCs.getAll(x -> true);
        for (NPC npc : npcs) {
            Color color = npc.getCombatLevel() > 1 ? YELLOW : ORANGE;
            graphics.setColor(color);
            if (npc.getConvexHull() == null || !npc.getConvexHull().contains(point.getX(), point.getY())) continue;
            graphics.draw(npc.getConvexHull());
            this.tooltipManager.add(new Tooltip(this.createInfo((SceneEntity)npc)));
        }
    }

    public void renderTileObjects(Graphics2D graphics, Point mouse) {
        List<TileObject> tileObjects = TileObjects.getSurrounding(Players.getLocal().getWorldLocation(), 35, x -> true);
        for (SceneEntity sceneEntity : tileObjects) {
            if (sceneEntity instanceof GameObject && this.gameObjects) {
                this.renderGameObjects(graphics, (GameObject)sceneEntity, mouse);
                continue;
            }
            if (sceneEntity instanceof WallObject && this.wallObjects) {
                this.renderWallObject(graphics, (WallObject)sceneEntity, mouse);
                continue;
            }
            if (sceneEntity instanceof GroundObject && this.groundObjects) {
                this.renderGroundObject(graphics, (GroundObject)sceneEntity, mouse);
                continue;
            }
            if (!(sceneEntity instanceof DecorativeObject) || !this.decorativeObjects) continue;
            this.renderDecorObject(graphics, (DecorativeObject)sceneEntity, mouse);
        }
    }

    public void renderTileTooltip(Graphics2D graphics, Tile tile) {
        WorldMap worldMap = this.client.getWorldMap();
        Widget widget = Widgets.get(WidgetInfo.WORLD_MAP_VIEW);
        if (worldMap != null && Widgets.isVisible(widget)) {
            WorldPoint origin;
            WorldPoint worldMapMouse = worldMap.getMouseLocation();
            WorldMapData worldMapData = worldMap.getWorldMapData();
            if (worldMapMouse != null && worldMapData != null && (origin = worldMapData.getOrigin()) != null) {
                String tooltip = String.format("World map location: %d, %d, %d</br>Region ID: %d location: %d, %d</br>Map origin: %d, %d %d</br>", worldMapMouse.getX(), worldMapMouse.getY(), worldMapMouse.getPlane(), worldMapMouse.getRegionID(), worldMapMouse.getRegionX(), worldMapMouse.getRegionY(), origin.getX(), origin.getY(), origin.getPlane());
                this.tooltipManager.add(new Tooltip(tooltip));
                return;
            }
        }
        if (tile == null) {
            return;
        }
        Polygon poly = Perspective.getCanvasTilePoly((Client)this.client, (LocalPoint)tile.getLocalLocation());
        if (poly != null && poly.contains(this.client.getMouseCanvasPosition().getX(), this.client.getMouseCanvasPosition().getY())) {
            String tooltip;
            WorldPoint worldLocation = tile.getWorldLocation();
            ScenePoint scenePoint = ScenePoint.fromWorld(worldLocation);
            if (this.trueWorldLocations && Static.getClient().isInInstancedRegion()) {
                WorldPoint trueWorldPoint = WorldPoint.fromLocalInstance((Client)Static.getClient(), (LocalPoint)LocalPoint.fromWorld((Client)Static.getClient(), (WorldPoint)worldLocation));
                tooltip = String.format("World location: %d, %d, %d</br>Region ID: %d location: %d, %d</br>Scene location: %d, %d</br>True location: %d, %d, %d</br>", worldLocation.getX(), worldLocation.getY(), worldLocation.getPlane(), worldLocation.getRegionID(), worldLocation.getRegionX(), worldLocation.getRegionY(), scenePoint.getX(), scenePoint.getY(), trueWorldPoint.getX(), trueWorldPoint.getY(), trueWorldPoint.getPlane());
            } else {
                tooltip = String.format("World location: %d, %d, %d</br>Region ID: %d location: %d, %d</br>Scene location: %d, %d</br>", worldLocation.getX(), worldLocation.getY(), worldLocation.getPlane(), worldLocation.getRegionID(), worldLocation.getRegionX(), worldLocation.getRegionY(), scenePoint.getX(), scenePoint.getY());
            }
            this.tooltipManager.add(new Tooltip(tooltip));
            OverlayUtil.renderPolygon((Graphics2D)graphics, (Shape)poly, (Color)GREEN);
        }
    }

    public void renderTileItems(Graphics2D graphics, Point point) {
        List<TileItem> tileItems = TileItems.getSurrounding(Players.getLocal().getWorldLocation(), 35, x -> true);
        for (TileItem tileItem : tileItems) {
            ItemLayer tileItemPile = tileItem.getTile().getItemLayer();
            if (tileItemPile == null || tileItemPile.getCanvasTilePoly() == null || !tileItemPile.getCanvasTilePoly().contains(point.getX(), point.getY())) continue;
            Renderable current = tileItemPile.getBottom();
            while (current instanceof TileItem) {
                OverlayUtil.renderTileOverlay((Graphics2D)graphics, (TileObject)tileItemPile, (String)"", (Color)RED);
                this.tooltipManager.add(new Tooltip(this.createInfo((SceneEntity)((TileItem)current))));
                current = current.getNext();
            }
        }
    }

    public void renderGameObjects(Graphics2D graphics, GameObject go, Point point) {
        if (go == null) {
            return;
        }
        Shape hull = go.getConvexHull();
        if (hull == null) {
            return;
        }
        if (!hull.contains(point.getX(), point.getY())) {
            return;
        }
        Renderable entity = go.getRenderable();
        Color color = entity instanceof DynamicObject ? TURQOISE : GREEN;
        graphics.setColor(color);
        graphics.draw(hull);
        OverlayUtil.renderTileOverlay((Graphics2D)graphics, (TileObject)go, (String)"", (Color)color);
        this.tooltipManager.add(new Tooltip(this.createInfo((SceneEntity)go)));
    }

    public void renderGroundObject(Graphics2D graphics, GroundObject gr, Point point) {
        if (gr == null) {
            return;
        }
        Shape hull = gr.getConvexHull();
        if (hull == null) {
            return;
        }
        if (!hull.contains(point.getX(), point.getY())) {
            return;
        }
        OverlayUtil.renderTileOverlay((Graphics2D)graphics, (TileObject)gr, (String)"", (Color)PURPLE);
        this.tooltipManager.add(new Tooltip(this.createInfo((SceneEntity)gr)));
    }

    public void renderWallObject(Graphics2D graphics, WallObject w, Point point) {
        if (w == null) {
            return;
        }
        Shape hull = w.getConvexHull();
        if (hull == null) {
            return;
        }
        if (!hull.contains(point.getX(), point.getY())) {
            return;
        }
        OverlayUtil.renderTileOverlay((Graphics2D)graphics, (TileObject)w, (String)"", (Color)GRAY);
        this.tooltipManager.add(new Tooltip(this.createInfo((SceneEntity)w)));
    }

    public void renderDecorObject(Graphics2D graphics, DecorativeObject deo, Point point) {
        if (deo == null) {
            return;
        }
        Shape hull = deo.getConvexHull();
        if (hull == null) {
            return;
        }
        if (!hull.contains(point.getX(), point.getY())) {
            return;
        }
        graphics.draw(hull);
        hull = deo.getConvexHull2();
        if (hull != null) {
            graphics.draw(hull);
        }
        OverlayUtil.renderTileOverlay((Graphics2D)graphics, (TileObject)deo, (String)"", (Color)DEEP_PURPLE);
        this.tooltipManager.add(new Tooltip(this.createInfo((SceneEntity)deo)));
    }

    public void renderInventory(Graphics2D graphics) {
        Widget inventoryWidget = Widgets.get(WidgetInfo.INVENTORY);
        if (!Widgets.isVisible(inventoryWidget)) {
            return;
        }
        for (WidgetItem item : inventoryWidget.getWidgetItems()) {
            Rectangle slotBounds = item.getCanvasBounds();
            String idText = "" + item.getId();
            FontMetrics fm = graphics.getFontMetrics();
            Rectangle2D textBounds = fm.getStringBounds(idText, graphics);
            int textX = (int)(slotBounds.getX() + slotBounds.getWidth() / 2.0 - textBounds.getWidth() / 2.0);
            int textY = (int)(slotBounds.getY() + slotBounds.getHeight() / 2.0 + textBounds.getHeight() / 2.0);
            graphics.setColor(new Color(255, 255, 255, 65));
            graphics.fill(slotBounds);
            graphics.setColor(Color.BLACK);
            graphics.drawString(idText, textX + 1, textY + 1);
            graphics.setColor(YELLOW);
            graphics.drawString(idText, textX, textY);
        }
    }

    public void renderProjectiles(Graphics2D graphics) {
        List<Projectile> projectiles = Projectiles.getAll(new int[0]);
        for (Projectile projectile : projectiles) {
            int originY;
            int originX = projectile.getX1();
            LocalPoint tilePoint = new LocalPoint(originX, originY = projectile.getY1());
            Polygon poly = Perspective.getCanvasTilePoly((Client)this.client, (LocalPoint)tilePoint);
            if (poly != null) {
                OverlayUtil.renderPolygon((Graphics2D)graphics, (Shape)poly, (Color)Color.RED);
            }
            int projectileId = projectile.getId();
            Actor projectileInteracting = projectile.getInteracting();
            Object infoString = "";
            infoString = projectileInteracting == null ? (String)infoString + "AoE" : (String)infoString + "Targeted (T: " + projectileInteracting.getName() + ")";
            infoString = (String)infoString + " (ID: " + projectileId + ")";
            if (projectileInteracting != null) {
                OverlayUtil.renderActorOverlay((Graphics2D)graphics, (Actor)projectile.getInteracting(), (String)infoString, (Color)Color.RED);
                continue;
            }
            LocalPoint projectilePoint = new LocalPoint((int)projectile.getX(), (int)projectile.getY());
            Point textLocation = Perspective.getCanvasTextLocation((Client)this.client, (Graphics2D)graphics, (LocalPoint)projectilePoint, (String)infoString, (int)0);
            if (textLocation == null) continue;
            OverlayUtil.renderTextLocation((Graphics2D)graphics, (Point)textLocation, (String)infoString, (Color)Color.RED);
        }
    }

    public void renderGraphicsObjects(Graphics2D graphics) {
        this.client.getGraphicsObjects().forEach(graphicsObject -> {
            String infoString;
            Point textLocation;
            LocalPoint lp = graphicsObject.getLocation();
            Polygon poly = Perspective.getCanvasTilePoly((Client)this.client, (LocalPoint)lp);
            if (poly != null) {
                OverlayUtil.renderPolygon((Graphics2D)graphics, (Shape)poly, (Color)Color.MAGENTA);
            }
            if ((textLocation = Perspective.getCanvasTextLocation((Client)this.client, (Graphics2D)graphics, (LocalPoint)lp, (String)(infoString = "(ID: " + graphicsObject.getId() + ")"), (int)0)) != null) {
                OverlayUtil.renderTextLocation((Graphics2D)graphics, (Point)textLocation, (String)infoString, (Color)Color.WHITE);
            }
        });
    }

    public void renderPlayerWireframe(Graphics2D graphics, Player player, Color color) {
        Polygon[] polys = player.getPolygons();
        if (polys == null) {
            return;
        }
        graphics.setColor(color);
        for (Polygon p : polys) {
            graphics.drawPolygon(p);
        }
    }

    public String createInfo(SceneEntity interactable) {
        StringBuilder sb = new StringBuilder();
        if (interactable instanceof Actor) {
            if (this.indexes) {
                if (interactable instanceof Player) {
                    sb.append("Index: ").append(((Player)interactable).getIndex()).append("</br>");
                }
                if (interactable instanceof NPC) {
                    sb.append("Index: ").append(((NPC)interactable).getIndex()).append("</br>");
                }
            }
            this.appendCommonFields(sb, interactable);
            if (this.animations) {
                sb.append("Animations: ").append(((Actor)interactable).getAnimation()).append("</br>");
            }
            if (this.graphics) {
                sb.append("Graphic: ").append(((Actor)interactable).getGraphic()).append("</br>");
            }
            return sb.toString();
        }
        if (interactable instanceof TileObject) {
            if (this.ids) {
                sb.append("Base ID: ").append(interactable.getId()).append("</br>");
                sb.append("Actual ID: ").append(((TileObject)interactable).getActualId()).append("</br>");
            }
            this.appendCommonFields(sb, interactable);
            if (this.animations && interactable instanceof GameObject && ((GameObject)interactable).getRenderable() instanceof DynamicObject) {
                sb.append("Animations: ").append(((DynamicObject)((GameObject)interactable).getRenderable()).getAnimationID()).append("</br>");
            }
            return sb.toString();
        }
        if (interactable instanceof TileItem) {
            if (this.ids) {
                sb.append("ID: ").append(interactable.getId()).append("</br>");
            }
            if (this.quantities) {
                sb.append("Quantity: ").append(((TileItem)interactable).getQuantity()).append("</br>");
            }
            this.appendCommonFields(sb, interactable);
            return sb.toString();
        }
        return sb.toString();
    }

    private void appendCommonFields(StringBuilder sb, SceneEntity interactable) {
        if (interactable instanceof Actor) {
            if (interactable instanceof NPC && this.ids) {
                sb.append("ID: ").append(interactable.getId()).append("</br>");
            }
            if (this.names) {
                sb.append("Name: ").append(interactable.getName()).append("</br>");
            }
            if (this.actions) {
                sb.append("Actions: ").append(Arrays.toString(interactable.getActions())).append("</br>");
            }
            if (this.worldLocations) {
                WorldPoint location = interactable.getWorldLocation();
                sb.append("Location: ").append(location).append("</br>");
                sb.append("Region: ").append(RegionPoint.fromWorld(location)).append("</br>");
                sb.append("Scene: ").append(ScenePoint.fromWorld(location)).append("</br>");
            }
            if (interactable instanceof NPC) {
                sb.append("Transformvarbit: ").append(((NPC)interactable).getComposition().getTransformVarbit()).append("</br>");
                sb.append("Transformvarp: ").append(((NPC)interactable).getComposition().getTransformVarp()).append("</br>");
            }
            return;
        }
        if (interactable instanceof TileObject) {
            if (this.names) {
                sb.append("Name: ").append(interactable.getName()).append("</br>");
            }
            if (this.actions) {
                sb.append("Actions: ").append(Arrays.toString(interactable.getActions())).append("</br>");
            }
            if (this.worldLocations) {
                WorldPoint location = interactable.getWorldLocation();
                sb.append("Location: ").append(location).append("</br>");
                sb.append("Region: ").append(RegionPoint.fromWorld(location)).append("</br>");
                sb.append("Scene: ").append(ScenePoint.fromWorld(location)).append("</br>");
            }
            sb.append("Transformvarbit: ").append(this.client.getObjectDefinition(interactable.getId()).getTransformVarbit()).append("</br>");
            sb.append("Transformvarp: ").append(this.client.getObjectDefinition(interactable.getId()).getVarPlayerId()).append("</br>");
            return;
        }
        if (interactable instanceof TileItem) {
            if (this.names) {
                sb.append("Name: ").append(interactable.getName()).append("</br>");
            }
            if (this.actions) {
                sb.append("Actions: ").append(Arrays.toString(interactable.getActions())).append("</br>");
            }
            if (this.worldLocations) {
                WorldPoint location = interactable.getWorldLocation();
                sb.append("Location: ").append(location).append("</br>");
                sb.append("Region: ").append(RegionPoint.fromWorld(location)).append("</br>");
                sb.append("Scene: ").append(ScenePoint.fromWorld(location)).append("</br>");
            }
        }
    }

    public boolean isGroundObjects() {
        return this.groundObjects;
    }

    public void setGroundObjects(boolean groundObjects) {
        this.groundObjects = groundObjects;
    }

    public boolean isWallObjects() {
        return this.wallObjects;
    }

    public void setWallObjects(boolean wallObjects) {
        this.wallObjects = wallObjects;
    }

    public boolean isDecorativeObjects() {
        return this.decorativeObjects;
    }

    public void setDecorativeObjects(boolean decorativeObjects) {
        this.decorativeObjects = decorativeObjects;
    }

    public boolean isGameObjects() {
        return this.gameObjects;
    }

    public void setGameObjects(boolean gameObjects) {
        this.gameObjects = gameObjects;
    }

    public boolean isGraphicsObjects() {
        return this.graphicsObjects;
    }

    public void setGraphicsObjects(boolean graphicsObjects) {
        this.graphicsObjects = graphicsObjects;
    }

    public boolean isInventory() {
        return this.inventory;
    }

    public void setInventory(boolean inventory) {
        this.inventory = inventory;
    }

    public boolean isNpcs() {
        return this.npcs;
    }

    public void setNpcs(boolean npcs) {
        this.npcs = npcs;
    }

    public boolean isPlayers() {
        return this.players;
    }

    public void setPlayers(boolean players) {
        this.players = players;
    }

    public boolean isTileItems() {
        return this.tileItems;
    }

    public void setTileItems(boolean tileItems) {
        this.tileItems = tileItems;
    }

    public boolean isProjectiles() {
        return this.projectiles;
    }

    public void setProjectiles(boolean projectiles) {
        this.projectiles = projectiles;
    }

    public boolean isTileLocation() {
        return this.tileLocation;
    }

    public void setTileLocation(boolean tileLocation) {
        this.tileLocation = tileLocation;
    }

    public boolean isCollisionMap() {
        return this.collisionMap;
    }

    public void setCollisionMap(boolean collisionMap) {
        this.collisionMap = collisionMap;
    }

    public boolean isPath() {
        return this.path;
    }

    public void setPath(boolean path) {
        this.path = path;
    }

    public boolean isIds() {
        return this.ids;
    }

    public void setIds(boolean ids) {
        this.ids = ids;
    }

    public boolean isNames() {
        return this.names;
    }

    public void setNames(boolean names) {
        this.names = names;
    }

    public boolean isIndexes() {
        return this.indexes;
    }

    public void setIndexes(boolean indexes) {
        this.indexes = indexes;
    }

    public boolean isAnimations() {
        return this.animations;
    }

    public void setAnimations(boolean animations) {
        this.animations = animations;
    }

    public boolean isGraphics() {
        return this.graphics;
    }

    public void setGraphics(boolean graphics) {
        this.graphics = graphics;
    }

    public boolean isActions() {
        return this.actions;
    }

    public void setActions(boolean actions) {
        this.actions = actions;
    }

    public boolean isWorldLocations() {
        return this.worldLocations;
    }

    public void setWorldLocations(boolean worldLocations) {
        this.worldLocations = worldLocations;
    }

    public boolean isQuantities() {
        return this.quantities;
    }

    public void setQuantities(boolean quantities) {
        this.quantities = quantities;
    }

    public boolean isTrueWorldLocations() {
        return this.trueWorldLocations;
    }

    public void setTrueWorldLocations(boolean trueWorldLocations) {
        this.trueWorldLocations = trueWorldLocations;
    }

    public void setCurrentPath(List<WorldPoint> currentPath) {
        this.currentPath = currentPath;
    }
}


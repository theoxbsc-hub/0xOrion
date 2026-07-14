/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Multimap
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.api.IndexDataBase
 *  net.runelite.api.Item
 *  net.runelite.api.ItemContainer
 *  net.runelite.api.NPC
 *  net.runelite.api.NPCComposition
 *  net.runelite.api.ObjectComposition
 *  net.runelite.api.PlayerComposition
 *  net.runelite.api.TileObject
 *  net.runelite.api.VarbitComposition
 *  net.runelite.api.events.DecorativeObjectDespawned
 *  net.runelite.api.events.DecorativeObjectSpawned
 *  net.runelite.api.events.GameObjectDespawned
 *  net.runelite.api.events.GameObjectSpawned
 *  net.runelite.api.events.GameStateChanged
 *  net.runelite.api.events.GroundObjectDespawned
 *  net.runelite.api.events.GroundObjectSpawned
 *  net.runelite.api.events.ItemContainerChanged
 *  net.runelite.api.events.ItemSpawned
 *  net.runelite.api.events.PlayerSpawned
 *  net.runelite.api.events.VarbitChanged
 *  net.runelite.api.events.WallObjectDespawned
 *  net.runelite.api.events.WallObjectSpawned
 *  net.runelite.api.events.WidgetClosed
 *  net.runelite.api.events.WidgetHiddenChanged
 *  net.runelite.api.events.WidgetLoaded
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.client.callback.ClientThread
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.managers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Iterator;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.IndexDataBase;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.ObjectComposition;
import net.runelite.api.PlayerComposition;
import net.runelite.api.TileObject;
import net.runelite.api.VarbitComposition;
import net.runelite.api.events.DecorativeObjectDespawned;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetHiddenChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.api.events.NPCCompositionChanged;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DefinitionManager {
    private static final Logger log = LoggerFactory.getLogger(DefinitionManager.class);
    private static final Multimap<Integer, Integer> VARS = HashMultimap.create();
    private static final Multimap<Integer, Integer> VARBIT_TO_ENTITYID = HashMultimap.create();
    private static final Multimap<Integer, Integer> VARP_TO_ENTITYID = HashMultimap.create();
    private static final Multimap<Integer, TileObject> TRANSFORMING_OBJECTS = HashMultimap.create();
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;

    @Inject
    DefinitionManager(EventBus eventBus) {
        eventBus.register((Object)this);
    }

    public void init() {
        this.clientThread.invoke(() -> {
            int[] varbitIds;
            IndexDataBase indexVarbits = this.client.getIndexConfig();
            for (int id : varbitIds = indexVarbits.getFileIds(14)) {
                VarbitComposition varbit = this.client.getVarbit(id);
                if (varbit == null) continue;
                VARS.put((Object)varbit.getIndex(), (Object)id);
            }
        });
    }

    @Subscribe(priority=2.14748365E9f)
    private void onNpcCompositionChanged(NPCCompositionChanged event) {
        NPC npc = Static.getClient().getCachedNPCs()[event.getNpcIndex()];
        if (npc == null) {
            return;
        }
        NPCComposition composition = npc.getComposition();
        if (composition != null && composition.getConfigs() != null) {
            if (composition.getTransformVarbit() != -1) {
                VARBIT_TO_ENTITYID.put((Object)composition.getTransformVarbit(), (Object)event.getNpcIndex());
            } else if (composition.getTransformVarp() != -1) {
                VARP_TO_ENTITYID.put((Object)composition.getTransformVarp(), (Object)event.getNpcIndex());
            }
            npc.setTransformedComposition(composition.transform());
        }
    }

    @Subscribe(priority=2.14748365E9f)
    private void onSpawn(GameObjectSpawned event) {
        this.checkTransformObject((TileObject)event.getGameObject());
    }

    @Subscribe(priority=2.14748365E9f)
    private void onDespawn(GameObjectDespawned event) {
        TRANSFORMING_OBJECTS.remove((Object)event.getGameObject().getId(), (Object)event.getGameObject());
    }

    @Subscribe(priority=2.14748365E9f)
    private void onSpawn(WallObjectSpawned event) {
        this.checkTransformObject((TileObject)event.getWallObject());
    }

    @Subscribe(priority=2.14748365E9f)
    private void onChange(WallObjectSpawned event) {
        this.checkTransformObject((TileObject)event.getWallObject());
    }

    @Subscribe(priority=2.14748365E9f)
    private void onDespawn(WallObjectDespawned event) {
        TRANSFORMING_OBJECTS.remove((Object)event.getWallObject().getId(), (Object)event.getWallObject());
    }

    @Subscribe(priority=2.14748365E9f)
    private void onSpawn(DecorativeObjectSpawned event) {
        this.checkTransformObject((TileObject)event.getDecorativeObject());
    }

    @Subscribe(priority=2.14748365E9f)
    private void onDespawn(DecorativeObjectDespawned event) {
        TRANSFORMING_OBJECTS.remove((Object)event.getDecorativeObject().getId(), (Object)event.getDecorativeObject());
    }

    @Subscribe(priority=2.14748365E9f)
    private void onSpawn(GroundObjectSpawned event) {
        this.checkTransformObject((TileObject)event.getGroundObject());
    }

    @Subscribe(priority=2.14748365E9f)
    private void onDespawn(GroundObjectDespawned event) {
        TRANSFORMING_OBJECTS.remove((Object)event.getGroundObject().getId(), (Object)event.getGroundObject());
    }

    @Subscribe(priority=2.14748365E9f)
    private void onItemSpawn(ItemSpawned event) {
        this.client.cacheItem(event.getItem().getId(), this.client.getItemDefinition(event.getItem().getId()));
    }

    @Subscribe(priority=2.14748365E9f)
    private void onItemContainerChanged(ItemContainerChanged event) {
        ItemContainer container = event.getItemContainer();
        if (container == null) {
            return;
        }
        for (Item item : container.getItems()) {
            if (item == null) continue;
            this.client.cacheItem(item.getId(), this.client.getItemDefinition(item.getId()));
        }
    }

    @Subscribe(priority=2.14748365E9f)
    private void onPlayerSpawned(PlayerSpawned event) {
        PlayerComposition playerComposition = event.getPlayer().getPlayerComposition();
        if (playerComposition == null) {
            return;
        }
        for (int equipmentId : playerComposition.getEquipmentIds()) {
            if (equipmentId < 512) continue;
            this.client.cacheItem(equipmentId, this.client.getItemDefinition(equipmentId));
        }
    }

    @Subscribe(priority=2.14748365E9f)
    private void onWidgetHiddenChanged(WidgetHiddenChanged event) {
        this.checkWidget(event.getWidget());
    }

    @Subscribe(priority=2.14748365E9f)
    private void onWidgetLoaded(WidgetLoaded event) {
        for (Widget widget : Widgets.get(event.getGroupId())) {
            this.checkWidget(widget);
        }
    }

    @Subscribe(priority=2.14748365E9f)
    private void onWidgetClosed(WidgetClosed event) {
        for (Widget widget : Widgets.get(event.getGroupId())) {
            this.checkWidget(widget);
        }
    }

    @Subscribe(priority=2.14748365E9f)
    private void onGameStateChanged(GameStateChanged gameStateChanged) {
        switch (gameStateChanged.getGameState()) {
            case LOGIN_SCREEN: 
            case HOPPING: {
                this.client.clearItemCache();
            }
        }
    }

    @Subscribe(priority=2.14748365E9f)
    private void onVarbitChanged(VarbitChanged e) {
        if (VARS.isEmpty()) {
            this.init();
        }
        Collection changedVarbits = VARS.get((Object)e.getIndex());
        Iterator iterator = changedVarbits.iterator();
        while (iterator.hasNext()) {
            int varId = (Integer)iterator.next();
            if (!VARBIT_TO_ENTITYID.containsKey((Object)varId)) continue;
            int configValue = Vars.getBit(varId);
            Collection entityIds = VARBIT_TO_ENTITYID.get((Object)varId);
            this.checkEntityTransforms(configValue, entityIds);
        }
        int varp = e.getIndex();
        if (VARP_TO_ENTITYID.containsKey((Object)varp)) {
            int configValue = Vars.getVarp(varp);
            Collection entityIds = VARP_TO_ENTITYID.get((Object)varp);
            this.checkEntityTransforms(configValue, entityIds);
        }
    }

    private void checkEntityTransforms(int configValue, Collection<Integer> entityIds) {
        for (Integer entityId : entityIds) {
            NPC npc;
            if (entityId < this.client.getCachedNPCs().length && (npc = this.client.getCachedNPCs()[entityId]) != null && npc.getComposition() != null) {
                NPCComposition transformed;
                NPCComposition current = npc.getTransformedComposition();
                if (current == (transformed = npc.getComposition().transform())) continue;
                npc.setTransformedComposition(transformed);
                if (configValue == 0) {
                    log.debug("NPC {} reverted to default state", (Object)entityId);
                    continue;
                }
                log.debug("NPC {} transformed", (Object)entityId);
                continue;
            }
            ObjectComposition objectComposition = this.client.getObjectDefinition(entityId.intValue());
            Collection cachedObjects = TRANSFORMING_OBJECTS.get((Object)entityId);
            for (TileObject cachedObject : cachedObjects) {
                ObjectComposition transformed;
                ObjectComposition current;
                if (objectComposition == null || cachedObject == null || (current = cachedObject.getTransformedComposition()) == (transformed = objectComposition.getImpostor())) continue;
                cachedObject.setTransformedComposition(transformed);
                if (configValue == 0) {
                    String name = transformed != null ? transformed.getName() : current.getName();
                    log.debug("[{}] reverted to default state", (Object)name);
                    continue;
                }
                if (transformed == null) {
                    log.debug("[{}: {}] transformed into [{}: {}]", new Object[]{objectComposition.getId(), objectComposition.getName(), null, null});
                    continue;
                }
                log.debug("[{}: {}] transformed into [{}: {}]", new Object[]{objectComposition.getId(), objectComposition.getName(), transformed.getId(), transformed.getName()});
            }
        }
    }

    private void checkWidget(Widget widget) {
        if (widget == null) {
            return;
        }
        boolean hidden = widget.isHidden();
        widget.setVisible(!hidden);
        if (!hidden) {
            int itemId = widget.getItemId();
            log.trace("Widget {}, {} is now visible", (Object)WidgetInfo.TO_GROUP((int)widget.getId()), (Object)WidgetInfo.TO_CHILD((int)widget.getId()));
            if (itemId != -1) {
                log.trace("Caching item {} from widget", (Object)itemId);
                this.client.cacheItem(itemId, this.client.getItemDefinition(itemId));
            }
        }
        this.checkWidgetChildren(widget.getDynamicChildren());
        this.checkWidgetChildren(widget.getStaticChildren());
        this.checkWidgetChildren(widget.getNestedChildren());
    }

    private void checkWidgetChildren(Widget[] widgets) {
        if (widgets == null) {
            return;
        }
        for (Widget widget : widgets) {
            this.checkWidget(widget);
        }
    }

    private void checkTransformObject(TileObject object) {
        if (object == null) {
            return;
        }
        ObjectComposition composition = this.client.getObjectDefinition(object.getId());
        if (composition == null) {
            return;
        }
        if (composition.getImpostorIds() != null) {
            if (composition.getTransformVarbit() != -1) {
                VARBIT_TO_ENTITYID.put((Object)composition.getTransformVarbit(), (Object)object.getId());
            } else if (composition.getVarPlayerId() != -1) {
                VARP_TO_ENTITYID.put((Object)composition.getVarPlayerId(), (Object)object.getId());
            }
            TRANSFORMING_OBJECTS.put((Object)object.getId(), (Object)object);
            object.setTransformedComposition(composition.getImpostor());
        } else {
            object.setTransformedComposition(composition);
        }
    }
}


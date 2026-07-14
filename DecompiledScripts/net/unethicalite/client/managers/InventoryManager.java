/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.api.InventoryID
 *  net.runelite.api.Item
 *  net.runelite.api.events.ItemContainerChanged
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 */
package net.unethicalite.client.managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
public class InventoryManager {
    private static final Map<Integer, Item[]> cachedContainers = new ConcurrentHashMap<Integer, Item[]>();
    @Inject
    private Client client;

    @Inject
    InventoryManager(EventBus eventBus) {
        eventBus.register((Object)this);
    }

    @Subscribe(priority=2.14748365E9f)
    private void onItemContainerChanged(ItemContainerChanged e) {
        cachedContainers.put(e.getContainerId(), e.getItemContainer().getItems());
        if (e.getContainerId() == InventoryID.INVENTORY.getId()) {
            this.client.runScript(new Object[]{6009, 0x950000, 28, 1, -1});
        }
    }

    public static Map<Integer, Item[]> getCachedContainers() {
        return cachedContainers;
    }
}


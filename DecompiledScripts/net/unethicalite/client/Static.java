/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.api.packets.ClientPacket
 *  net.runelite.api.packets.ServerPacket
 *  net.runelite.client.callback.ClientThread
 *  net.runelite.client.chat.ChatMessageManager
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.game.ItemManager
 *  net.runelite.client.game.SpriteManager
 *  net.runelite.client.game.WorldService
 *  net.runelite.client.input.KeyManager
 *  net.runelite.client.plugins.PluginManager
 *  net.runelite.client.ui.ClientToolbar
 *  net.runelite.client.ui.overlay.OverlayManager
 */
package net.unethicalite.client;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.packets.ClientPacket;
import net.runelite.api.packets.ServerPacket;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.WorldService;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.movement.pathfinder.CollisionMap;
import net.unethicalite.api.movement.pathfinder.GlobalCollisionMap;
import net.unethicalite.api.script.paint.Paint;
import net.unethicalite.client.config.UnethicaliteConfig;
import net.unethicalite.client.devtools.EntityRenderer;
import net.unethicalite.client.managers.DefinitionManager;
import net.unethicalite.client.managers.InventoryManager;
import net.unethicalite.client.managers.NeverLogManager;
import net.unethicalite.client.managers.QuestManager;
import net.unethicalite.client.managers.RegionManager;
import net.unethicalite.client.managers.SettingsManager;
import net.unethicalite.client.managers.interaction.InteractionManager;

@Singleton
public class Static {
    private static String[] scriptArgs = new String[0];
    @Inject
    private static EventBus eventBus;
    @Inject
    private static ClientThread clientThread;
    @Inject
    private static Client client;
    @Inject
    private static ItemManager itemManager;
    @Inject
    private static WorldService worldService;
    @Inject
    private static ChatMessageManager chatMessageManager;
    @Inject
    private static ClientPacket clientPacket;
    @Inject
    private static ServerPacket serverPacket;
    @Inject
    private static GlobalCollisionMap globalCollisionMap;
    @Inject
    private static PluginManager pluginManager;
    @Inject
    private static ConfigManager configManager;
    @Inject
    private static KeyManager keyManager;
    @Inject
    private static DefinitionManager definitionManager;
    @Inject
    private static InteractionManager interactionManager;
    @Inject
    private static Paint paint;
    @Inject
    private static UnethicaliteConfig unethicaliteConfig;
    @Inject
    private static NeverLogManager neverLogManager;
    @Inject
    private static EntityRenderer entityRenderer;
    @Inject
    private static OverlayManager overlayManager;
    @Inject
    private static ClientToolbar clientToolbar;
    @Inject
    private static RegionManager regionManager;
    @Inject
    private static InventoryManager inventoryManager;
    @Inject
    private static QuestManager questManager;
    @Inject
    private static SpriteManager spriteManager;
    @Inject
    private static SettingsManager settingsManager;

    public static void setScriptArgs(String[] scriptArgs) {
        Static.scriptArgs = scriptArgs;
    }

    public static CollisionMap getGlobalCollisionMap() {
        return Static.getClient().isInInstancedRegion() ? globalCollisionMap.withLocalCollisions() : globalCollisionMap;
    }

    public static String[] getScriptArgs() {
        return scriptArgs;
    }

    public static EventBus getEventBus() {
        return eventBus;
    }

    public static ClientThread getClientThread() {
        return clientThread;
    }

    public static Client getClient() {
        return client;
    }

    public static ItemManager getItemManager() {
        return itemManager;
    }

    public static WorldService getWorldService() {
        return worldService;
    }

    public static ChatMessageManager getChatMessageManager() {
        return chatMessageManager;
    }

    public static ClientPacket getClientPacket() {
        return clientPacket;
    }

    public static ServerPacket getServerPacket() {
        return serverPacket;
    }

    public static PluginManager getPluginManager() {
        return pluginManager;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static KeyManager getKeyManager() {
        return keyManager;
    }

    public static DefinitionManager getDefinitionManager() {
        return definitionManager;
    }

    public static InteractionManager getInteractionManager() {
        return interactionManager;
    }

    public static Paint getPaint() {
        return paint;
    }

    public static UnethicaliteConfig getUnethicaliteConfig() {
        return unethicaliteConfig;
    }

    public static NeverLogManager getNeverLogManager() {
        return neverLogManager;
    }

    public static EntityRenderer getEntityRenderer() {
        return entityRenderer;
    }

    public static OverlayManager getOverlayManager() {
        return overlayManager;
    }

    public static ClientToolbar getClientToolbar() {
        return clientToolbar;
    }

    public static RegionManager getRegionManager() {
        return regionManager;
    }

    public static InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public static QuestManager getQuestManager() {
        return questManager;
    }

    public static SpriteManager getSpriteManager() {
        return spriteManager;
    }
}


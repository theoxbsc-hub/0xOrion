/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.TileItem
 *  net.runelite.api.packets.ClientPacket
 *  net.runelite.api.packets.PacketBufferNode
 */
package net.unethicalite.api.packets;

import net.runelite.api.Client;
import net.runelite.api.TileItem;
import net.runelite.api.packets.ClientPacket;
import net.runelite.api.packets.PacketBufferNode;
import net.unethicalite.api.game.Game;
import net.unethicalite.client.Static;

public class GroundItemPackets {
    public static void groundItemFirstOption(TileItem item, boolean ctrlDown) {
        GroundItemPackets.queueGroundItemAction1Packet(item.getId(), item.getWorldLocation().getX(), item.getWorldLocation().getY(), ctrlDown);
    }

    public static void groundItemSecondOption(TileItem item, boolean ctrlDown) {
        GroundItemPackets.queueGroundItemAction2Packet(item.getId(), item.getWorldLocation().getX(), item.getWorldLocation().getY(), ctrlDown);
    }

    public static void groundItemThirdOption(TileItem item, boolean ctrlDown) {
        GroundItemPackets.queueGroundItemAction3Packet(item.getId(), item.getWorldLocation().getX(), item.getWorldLocation().getY(), ctrlDown);
    }

    public static void groundItemFourthOption(TileItem item, boolean ctrlDown) {
        GroundItemPackets.queueGroundItemAction4Packet(item.getId(), item.getWorldLocation().getX(), item.getWorldLocation().getY(), ctrlDown);
    }

    public static void groundItemFifthOption(TileItem item, boolean ctrlDown) {
        GroundItemPackets.queueGroundItemAction5Packet(item.getId(), item.getWorldLocation().getX(), item.getWorldLocation().getY(), ctrlDown);
    }

    public static void groundItemAction(TileItem item, String action, boolean ctrlDown) {
        int index = item.getActionIndex(action);
        switch (index) {
            case 0: {
                GroundItemPackets.groundItemFirstOption(item, ctrlDown);
                break;
            }
            case 1: {
                GroundItemPackets.groundItemSecondOption(item, ctrlDown);
                break;
            }
            case 2: {
                GroundItemPackets.groundItemThirdOption(item, ctrlDown);
                break;
            }
            case 3: {
                GroundItemPackets.groundItemFourthOption(item, ctrlDown);
                break;
            }
            case 4: {
                GroundItemPackets.groundItemFifthOption(item, ctrlDown);
            }
        }
    }

    public static void queueItemUseOnGroundObjectPacket(int groundItemId, int worldPointX, int worldPointY, int itemSlot, int itemId, int itemWidgetId, boolean ctrlDown) {
        GroundItemPackets.createItemOnGroundItem(groundItemId, worldPointX, worldPointY, itemSlot, itemId, itemWidgetId, ctrlDown).send();
    }

    public static void queueSpellOnGroundObjectPacket(int groundItemId, int worldPointX, int worldPointY, int spellWidgetId, boolean ctrlDown) {
        GroundItemPackets.createSpellOnGroundItem(groundItemId, worldPointX, worldPointY, spellWidgetId, ctrlDown).send();
    }

    public static void queueGroundItemAction1Packet(int groundItemId, int worldPointX, int worldPointY, boolean ctrlDown) {
        GroundItemPackets.createFirstAction(groundItemId, worldPointX, worldPointY, ctrlDown).send();
    }

    public static void queueGroundItemAction2Packet(int groundItemId, int worldPointX, int worldPointY, boolean ctrlDown) {
        GroundItemPackets.createSecondAction(groundItemId, worldPointX, worldPointY, ctrlDown).send();
    }

    public static void queueGroundItemAction3Packet(int groundItemId, int worldPointX, int worldPointY, boolean ctrlDown) {
        GroundItemPackets.createThirdAction(groundItemId, worldPointX, worldPointY, ctrlDown).send();
    }

    public static void queueGroundItemAction4Packet(int groundItemId, int worldPointX, int worldPointY, boolean ctrlDown) {
        GroundItemPackets.createFourthAction(groundItemId, worldPointX, worldPointY, ctrlDown).send();
    }

    public static void queueGroundItemAction5Packet(int groundItemId, int worldPointX, int worldPointY, boolean ctrlDown) {
        GroundItemPackets.createFifthAction(groundItemId, worldPointX, worldPointY, ctrlDown).send();
    }

    public static PacketBufferNode createItemOnGroundItem(int groundItemId, int worldPointX, int worldPointY, int itemSlot, int itemId, int itemWidgetId, boolean ctrlDown) {
        return GroundItemPackets.createWidgetOnGroundItem(groundItemId, worldPointX, worldPointY, itemSlot, itemId, itemWidgetId, ctrlDown);
    }

    public static PacketBufferNode createWidgetOnGroundItem(int groundItemId, int worldPointX, int worldPointY, int sourceSlot, int sourceItemId, int sourceWidgetId, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPOBJT(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeShort(worldPointX);
        packetBufferNode.getPacketBuffer().writeShort(groundItemId);
        packetBufferNode.getPacketBuffer().writeByteSub(ctrlDown ? 1 : 0);
        packetBufferNode.getPacketBuffer().writeShortAdd(sourceItemId);
        packetBufferNode.getPacketBuffer().writeShortLE(sourceSlot);
        packetBufferNode.getPacketBuffer().writeIntME(sourceWidgetId);
        packetBufferNode.getPacketBuffer().writeShortAddLE(worldPointY);
        return packetBufferNode;
    }

    public static PacketBufferNode createSpellOnGroundItem(int groundItemId, int worldPointX, int worldPointY, int spellWidgetId, boolean ctrlDown) {
        return GroundItemPackets.createWidgetOnGroundItem(groundItemId, worldPointX, worldPointY, -1, -1, spellWidgetId, ctrlDown);
    }

    public static PacketBufferNode createFirstAction(int groundItemId, int worldPointX, int worldPointY, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPOBJ1(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeShortAdd(worldPointY);
        packetBufferNode.getPacketBuffer().writeByteAdd(ctrlDown ? 1 : 0);
        packetBufferNode.getPacketBuffer().writeShortAddLE(worldPointX);
        packetBufferNode.getPacketBuffer().writeShortLE(groundItemId);
        return packetBufferNode;
    }

    public static PacketBufferNode createSecondAction(int groundItemId, int worldPointX, int worldPointY, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPOBJ2(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeShortAddLE(groundItemId);
        packetBufferNode.getPacketBuffer().writeShortAdd(worldPointY);
        packetBufferNode.getPacketBuffer().writeByteSub(ctrlDown ? 1 : 0);
        packetBufferNode.getPacketBuffer().writeShortLE(worldPointX);
        return packetBufferNode;
    }

    public static PacketBufferNode createThirdAction(int groundItemId, int worldPointX, int worldPointY, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPOBJ3(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeShortLE(worldPointX);
        packetBufferNode.getPacketBuffer().writeShortAddLE(worldPointY);
        packetBufferNode.getPacketBuffer().writeByteAdd(ctrlDown ? 1 : 0);
        packetBufferNode.getPacketBuffer().writeShortAdd(groundItemId);
        return packetBufferNode;
    }

    public static PacketBufferNode createFourthAction(int groundItemId, int worldPointX, int worldPointY, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPOBJ4(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeShortAddLE(groundItemId);
        packetBufferNode.getPacketBuffer().writeByteNeg(ctrlDown ? 1 : 0);
        packetBufferNode.getPacketBuffer().writeShortAdd(worldPointX);
        packetBufferNode.getPacketBuffer().writeShortAddLE(worldPointY);
        return packetBufferNode;
    }

    public static PacketBufferNode createFifthAction(int groundItemId, int worldPointX, int worldPointY, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPOBJ5(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeShortLE(worldPointY);
        packetBufferNode.getPacketBuffer().writeShort(groundItemId);
        packetBufferNode.getPacketBuffer().writeByteNeg(ctrlDown ? 1 : 0);
        packetBufferNode.getPacketBuffer().writeShortAddLE(worldPointX);
        return packetBufferNode;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Item
 *  net.runelite.api.Point
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.packets.ClientPacket
 *  net.runelite.api.packets.PacketBufferNode
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.api.packets;

import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.packets.ClientPacket;
import net.runelite.api.packets.PacketBufferNode;
import net.unethicalite.api.game.Game;
import net.unethicalite.client.Static;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectPackets {
    private static final Logger log = LoggerFactory.getLogger(ObjectPackets.class);

    public static void tileObjectFirstOption(TileObject object, boolean ctrlDown) {
        Point p = object.menuPoint();
        LocalPoint lp = new LocalPoint(p.getX(), p.getY());
        WorldPoint wp = WorldPoint.fromScene((Client)Static.getClient(), (int)lp.getX(), (int)lp.getY(), (int)object.getPlane());
        ObjectPackets.queueTileObjectAction1Packet(object.getId(), wp.getX(), wp.getY(), ctrlDown);
    }

    public static void tileObjectSecondOption(TileObject object, boolean ctrlDown) {
        Point p = object.menuPoint();
        LocalPoint lp = new LocalPoint(p.getX(), p.getY());
        WorldPoint wp = WorldPoint.fromScene((Client)Static.getClient(), (int)lp.getX(), (int)lp.getY(), (int)object.getPlane());
        ObjectPackets.queueTileObjectAction2Packet(object.getId(), wp.getX(), wp.getY(), ctrlDown);
    }

    public static void tileObjectThirdOption(TileObject object, boolean ctrlDown) {
        Point p = object.menuPoint();
        LocalPoint lp = new LocalPoint(p.getX(), p.getY());
        WorldPoint wp = WorldPoint.fromScene((Client)Static.getClient(), (int)lp.getX(), (int)lp.getY(), (int)object.getPlane());
        ObjectPackets.queueTileObjectAction3Packet(object.getId(), wp.getX(), wp.getY(), ctrlDown);
    }

    public static void tileObjectFourthOption(TileObject object, boolean ctrlDown) {
        Point p = object.menuPoint();
        LocalPoint lp = new LocalPoint(p.getX(), p.getY());
        WorldPoint wp = WorldPoint.fromScene((Client)Static.getClient(), (int)lp.getX(), (int)lp.getY(), (int)object.getPlane());
        ObjectPackets.queueTileObjectAction4Packet(object.getId(), wp.getX(), wp.getY(), ctrlDown);
    }

    public static void tileObjectFifthOption(TileObject object, boolean ctrlDown) {
        Point p = object.menuPoint();
        LocalPoint lp = new LocalPoint(p.getX(), p.getY());
        WorldPoint wp = WorldPoint.fromScene((Client)Static.getClient(), (int)lp.getX(), (int)lp.getY(), (int)object.getPlane());
        ObjectPackets.queueTileObjectAction5Packet(object.getId(), wp.getX(), wp.getY(), ctrlDown);
    }

    public static void tileObjectAction(TileObject object, String action, boolean ctrlDown) {
        int index = object.getActionIndex(action);
        switch (index) {
            case 0: {
                ObjectPackets.tileObjectFirstOption(object, ctrlDown);
                break;
            }
            case 1: {
                ObjectPackets.tileObjectSecondOption(object, ctrlDown);
                break;
            }
            case 2: {
                ObjectPackets.tileObjectThirdOption(object, ctrlDown);
                break;
            }
            case 3: {
                ObjectPackets.tileObjectFourthOption(object, ctrlDown);
                break;
            }
            case 4: {
                ObjectPackets.tileObjectFifthOption(object, ctrlDown);
            }
        }
    }

    public static void useItemOnTileObject(Item item, TileObject object) {
        Point p = object.menuPoint();
        LocalPoint lp = new LocalPoint(p.getX(), p.getY());
        WorldPoint wp = WorldPoint.fromScene((Client)Static.getClient(), (int)lp.getX(), (int)lp.getY(), (int)object.getPlane());
        ObjectPackets.queueItemUseOnTileObjectPacket(object.getId(), wp.getX(), wp.getY(), item.getSlot(), item.getId(), item.getWidgetId(), false);
    }

    public static void queueItemUseOnTileObjectPacket(int objectId, int worldPointX, int worldPointY, int itemSlot, int itemId, int itemWidgetId, boolean ctrlDown) {
        ObjectPackets.createItemOnObjectPacket(objectId, worldPointX, worldPointY, itemSlot, itemId, itemWidgetId, ctrlDown).send();
    }

    public static void queueSpellOnTileObjectPacket(int objectId, int worldPointX, int worldPointY, int spellWidgetId, boolean ctrlDown) {
        ObjectPackets.createSpellOnObjectPacket(objectId, worldPointX, worldPointY, spellWidgetId, ctrlDown).send();
    }

    public static void queueTileObjectAction1Packet(int objectId, int worldPointX, int worldPointY, boolean ctrlDown) {
        ObjectPackets.createObjectFirstActionPacket(objectId, worldPointX, worldPointY, ctrlDown).send();
    }

    public static void queueTileObjectAction2Packet(int objectId, int worldPointX, int worldPointY, boolean ctrlDown) {
        ObjectPackets.createObjectSecondActionPacket(objectId, worldPointX, worldPointY, ctrlDown).send();
    }

    public static void queueTileObjectAction3Packet(int objectId, int worldPointX, int worldPointY, boolean ctrlDown) {
        ObjectPackets.createObjectThirdActionPacket(objectId, worldPointX, worldPointY, ctrlDown).send();
    }

    public static void queueTileObjectAction4Packet(int objectId, int worldPointX, int worldPointY, boolean ctrlDown) {
        ObjectPackets.createObjectFourthActionPacket(objectId, worldPointX, worldPointY, ctrlDown).send();
    }

    public static void queueTileObjectAction5Packet(int objectId, int worldPointX, int worldPointY, boolean ctrlDown) {
        ObjectPackets.createObjectFifthActionPacket(objectId, worldPointX, worldPointY, ctrlDown).send();
    }

    public static PacketBufferNode createItemOnObjectPacket(int objectId, int worldPointX, int worldPointY, int itemSlot, int itemId, int itemWidgetId, boolean ctrlDown) {
        return ObjectPackets.createWidgetOnObjectPacket(objectId, worldPointX, worldPointY, itemSlot, itemId, itemWidgetId, ctrlDown);
    }

    public static PacketBufferNode createWidgetOnObjectPacket(int objectId, int worldPointX, int worldPointY, int sourceSlot, int sourceItemId, int sourceWidgetId, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPLOCT(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeShortLE(worldPointY);
        packetBufferNode.getPacketBuffer().writeShortAddLE(sourceItemId);
        packetBufferNode.getPacketBuffer().writeIntIME(sourceWidgetId);
        packetBufferNode.getPacketBuffer().writeByte(ctrlDown ? 1 : 0);
        packetBufferNode.getPacketBuffer().writeShort(worldPointX);
        packetBufferNode.getPacketBuffer().writeShortLE(objectId);
        packetBufferNode.getPacketBuffer().writeShort(sourceSlot);
        return packetBufferNode;
    }

    public static PacketBufferNode createSpellOnObjectPacket(int objectId, int worldPointX, int worldPointY, int spellWidgetId, boolean ctrlDown) {
        return ObjectPackets.createWidgetOnObjectPacket(objectId, worldPointX, worldPointY, -1, -1, spellWidgetId, ctrlDown);
    }

    public static PacketBufferNode createObjectFirstActionPacket(int objectId, int worldPointX, int worldPointY, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPLOC1(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeByteAdd(ctrlDown ? 1 : 0);
        packetBufferNode.getPacketBuffer().writeShortAdd(worldPointX);
        packetBufferNode.getPacketBuffer().writeShortLE(objectId);
        packetBufferNode.getPacketBuffer().writeShort(worldPointY);
        return packetBufferNode;
    }

    public static PacketBufferNode createObjectSecondActionPacket(int objectId, int worldPointX, int worldPointY, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPLOC2(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeShortAdd(objectId);
        packetBufferNode.getPacketBuffer().writeShortAdd(worldPointX);
        packetBufferNode.getPacketBuffer().writeShort(worldPointY);
        packetBufferNode.getPacketBuffer().writeByteNeg(ctrlDown ? 1 : 0);
        return packetBufferNode;
    }

    public static PacketBufferNode createObjectThirdActionPacket(int objectId, int worldPointX, int worldPointY, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPLOC3(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeByteAdd(ctrlDown ? 1 : 0);
        packetBufferNode.getPacketBuffer().writeShortAddLE(worldPointY);
        packetBufferNode.getPacketBuffer().writeShortAdd(worldPointX);
        packetBufferNode.getPacketBuffer().writeShortLE(objectId);
        return packetBufferNode;
    }

    public static PacketBufferNode createObjectFourthActionPacket(int objectId, int worldPointX, int worldPointY, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPLOC4(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeShortAdd(worldPointX);
        packetBufferNode.getPacketBuffer().writeShort(worldPointY);
        packetBufferNode.getPacketBuffer().writeShort(objectId);
        packetBufferNode.getPacketBuffer().writeByteSub(ctrlDown ? 1 : 0);
        return packetBufferNode;
    }

    public static PacketBufferNode createObjectFifthActionPacket(int objectId, int worldPointX, int worldPointY, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPLOC5(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeShortLE(worldPointY);
        packetBufferNode.getPacketBuffer().writeShortLE(worldPointX);
        packetBufferNode.getPacketBuffer().writeShortAdd(objectId);
        packetBufferNode.getPacketBuffer().writeByteNeg(ctrlDown ? 1 : 0);
        return packetBufferNode;
    }
}


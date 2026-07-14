/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.NPC
 *  net.runelite.api.packets.ClientPacket
 *  net.runelite.api.packets.PacketBufferNode
 */
package net.unethicalite.api.packets;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.packets.ClientPacket;
import net.runelite.api.packets.PacketBufferNode;
import net.unethicalite.api.game.Game;
import net.unethicalite.client.Static;

public class NPCPackets {
    public static void npcFirstOption(NPC npc, boolean ctrlDown) {
        NPCPackets.queueNPCAction1Packet(npc.getIndex(), ctrlDown);
    }

    public static void npcSecondOption(NPC npc, boolean ctrlDown) {
        NPCPackets.queueNPCAction2Packet(npc.getIndex(), ctrlDown);
    }

    public static void npcThirdOption(NPC npc, boolean ctrlDown) {
        NPCPackets.queueNPCAction3Packet(npc.getIndex(), ctrlDown);
    }

    public static void npcFourthOption(NPC npc, boolean ctrlDown) {
        NPCPackets.queueNPCAction4Packet(npc.getIndex(), ctrlDown);
    }

    public static void npcFifthOption(NPC npc, boolean ctrlDown) {
        NPCPackets.queueNPCAction5Packet(npc.getIndex(), ctrlDown);
    }

    public static void npcAction(NPC npc, String action, boolean ctrlDown) {
        int index = npc.getActionIndex(action);
        switch (index) {
            case 0: {
                NPCPackets.npcFirstOption(npc, ctrlDown);
                break;
            }
            case 1: {
                NPCPackets.npcSecondOption(npc, ctrlDown);
                break;
            }
            case 2: {
                NPCPackets.npcThirdOption(npc, ctrlDown);
                break;
            }
            case 3: {
                NPCPackets.npcFourthOption(npc, ctrlDown);
                break;
            }
            case 4: {
                NPCPackets.npcFifthOption(npc, ctrlDown);
            }
        }
    }

    public static void spellOnNpc(int widgetId, NPC npc, boolean b) {
        NPCPackets.queueSpellOnNpcPacket(npc.getIndex(), widgetId, b);
    }

    public static void queueItemOnNpcPacket(int npcIndex, int itemWidgetId, int itemId, int itemSlot, boolean ctrlDown) {
        NPCPackets.createItemOnNpcPacket(npcIndex, itemWidgetId, itemId, itemSlot, ctrlDown).send();
    }

    public static void queueSpellOnNpcPacket(int npcIndex, int spellWidgetId, boolean ctrlDown) {
        NPCPackets.createSpellOnNpcPacket(npcIndex, spellWidgetId, ctrlDown).send();
    }

    public static void queueNPCAction1Packet(int npcIndex, boolean ctrlDown) {
        NPCPackets.createNpcFirstActionPacket(npcIndex, ctrlDown).send();
    }

    public static void queueNPCAction2Packet(int npcIndex, boolean ctrlDown) {
        NPCPackets.createNpcSecondActionPacket(npcIndex, ctrlDown).send();
    }

    public static void queueNPCAction3Packet(int npcIndex, boolean ctrlDown) {
        NPCPackets.createNpcThirdActionPacket(npcIndex, ctrlDown).send();
    }

    public static void queueNPCAction4Packet(int npcIndex, boolean ctrlDown) {
        NPCPackets.createNpcFourthActionPacket(npcIndex, ctrlDown).send();
    }

    public static void queueNPCAction5Packet(int npcIndex, boolean ctrlDown) {
        NPCPackets.createNpcFifthActionPacket(npcIndex, ctrlDown).send();
    }

    public static PacketBufferNode createItemOnNpcPacket(int npcIndex, int itemWidgetId, int itemId, int itemSlot, boolean ctrlDown) {
        return NPCPackets.createWidgetOnNpc(npcIndex, itemWidgetId, itemId, itemSlot, ctrlDown);
    }

    public static PacketBufferNode createWidgetOnNpc(int npcIndex, int sourceWidgetId, int sourceItemId, int sourceSlot, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPNPCT(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeIntIME(sourceWidgetId);
        packetBufferNode.getPacketBuffer().writeShortLE(sourceSlot);
        packetBufferNode.getPacketBuffer().writeShortAdd(sourceItemId);
        packetBufferNode.getPacketBuffer().writeByteSub(ctrlDown ? 1 : 0);
        packetBufferNode.getPacketBuffer().writeShortAdd(npcIndex);
        return packetBufferNode;
    }

    public static PacketBufferNode createSpellOnNpcPacket(int npcIndex, int spellWidgetId, boolean ctrlDown) {
        return NPCPackets.createWidgetOnNpc(npcIndex, spellWidgetId, -1, -1, ctrlDown);
    }

    public static PacketBufferNode createNpcFirstActionPacket(int npcIndex, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPNPC1(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeByteSub(ctrlDown ? 1 : 0);
        packetBufferNode.getPacketBuffer().writeShortAdd(npcIndex);
        return packetBufferNode;
    }

    public static PacketBufferNode createNpcSecondActionPacket(int npcIndex, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPNPC2(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeByte(ctrlDown ? 1 : 0);
        packetBufferNode.getPacketBuffer().writeShortAddLE(npcIndex);
        return packetBufferNode;
    }

    public static PacketBufferNode createNpcThirdActionPacket(int npcIndex, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPNPC3(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeByteAdd(ctrlDown ? 1 : 0);
        packetBufferNode.getPacketBuffer().writeShort(npcIndex);
        return packetBufferNode;
    }

    public static PacketBufferNode createNpcFourthActionPacket(int npcIndex, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPNPC4(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeByte(ctrlDown ? 1 : 0);
        packetBufferNode.getPacketBuffer().writeShortLE(npcIndex);
        return packetBufferNode;
    }

    public static PacketBufferNode createNpcFifthActionPacket(int npcIndex, boolean ctrlDown) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.OPNPC5(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeShortAdd(npcIndex);
        packetBufferNode.getPacketBuffer().writeByteNeg(ctrlDown ? 1 : 0);
        return packetBufferNode;
    }
}


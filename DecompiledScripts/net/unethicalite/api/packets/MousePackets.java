/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.packets.ClientPacket
 *  net.runelite.api.packets.PacketBufferNode
 */
package net.unethicalite.api.packets;

import net.runelite.api.Client;
import net.runelite.api.packets.ClientPacket;
import net.runelite.api.packets.PacketBufferNode;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.client.Static;

public class MousePackets {
    public static void queueClickPacket(int x, int y) {
        Static.getClient().setMouseLastPressedMillis(System.currentTimeMillis());
        int mousePressedTime = (int)(Static.getClient().getMouseLastPressedMillis() - Static.getClient().getClientMouseLastPressedMillis());
        if (mousePressedTime < 0) {
            mousePressedTime = 0;
        }
        if (mousePressedTime > Short.MAX_VALUE) {
            mousePressedTime = Short.MAX_VALUE;
        }
        Static.getClient().setClientMouseLastPressedMillis(Static.getClient().getMouseLastPressedMillis());
        int mouseInfo = (mousePressedTime << 1) + 1;
        MousePackets.queueClickPacket(mouseInfo, x, y);
    }

    public static void queueClickPacket(int mouseInfo, int x, int y) {
        GameThread.invoke(() -> MousePackets.createClickPacket(mouseInfo, x, y).send());
    }

    public static void queueClickPacket() {
        MousePackets.queueClickPacket(0, 0);
    }

    public static PacketBufferNode createClickPacket(int mouseInfo, int x, int y) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.EVENT_MOUSE_CLICK(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeShort(mouseInfo);
        packetBufferNode.getPacketBuffer().writeShort(x);
        packetBufferNode.getPacketBuffer().writeShort(y);
        return packetBufferNode;
    }
}


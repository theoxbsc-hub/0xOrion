/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.packets.PacketBuffer
 *  net.runelite.api.packets.ServerPacket
 */
package net.unethicalite.api.events;

import java.util.Arrays;
import net.runelite.api.packets.PacketBuffer;
import net.runelite.api.packets.ServerPacket;
import net.unethicalite.api.events.PacketSent;

public class ServerPacketReceived {
    private ServerPacket serverPacket;
    private boolean consumed;
    private PacketBuffer packetBuffer;
    private int length;

    public String hexDump() {
        byte[] payload = Arrays.copyOfRange(this.packetBuffer.getPayload(), 0, this.length);
        return "\n" + PacketSent.hexDump(payload, 0, payload.length);
    }

    public ServerPacket getServerPacket() {
        return this.serverPacket;
    }

    public boolean isConsumed() {
        return this.consumed;
    }

    public PacketBuffer getPacketBuffer() {
        return this.packetBuffer;
    }

    public int getLength() {
        return this.length;
    }

    public void setServerPacket(ServerPacket serverPacket) {
        this.serverPacket = serverPacket;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    public void setPacketBuffer(PacketBuffer packetBuffer) {
        this.packetBuffer = packetBuffer;
    }

    public void setLength(int length) {
        this.length = length;
    }
}


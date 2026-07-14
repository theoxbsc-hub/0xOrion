/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.packets.PacketBufferNode
 */
package net.unethicalite.api.events;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import net.runelite.api.packets.PacketBufferNode;

public class PacketSent {
    private final PacketBufferNode packetBufferNode;

    public String hexDump() {
        byte[] payload = Arrays.copyOfRange(this.packetBufferNode.getPacketBuffer().getPayload(), 1, this.packetBufferNode.getPacketBuffer().getOffset());
        return "\n" + PacketSent.hexDump(payload, 0, this.getPacketBufferNode().getPacketBuffer().getOffset());
    }

    public static String hexDump(byte[] array, int offset, int length) {
        int width = 16;
        StringBuilder builder = new StringBuilder();
        builder.append(Arrays.toString(array) + "\n");
        for (int rowOffset = offset; rowOffset < offset + length; rowOffset += 16) {
            builder.append(String.format("%06d     ", rowOffset));
            for (int index = 0; index < 16; ++index) {
                if (rowOffset + index < array.length) {
                    builder.append(String.format("%02X ", array[rowOffset + index]));
                    continue;
                }
                builder.append("   ");
            }
            if (rowOffset < array.length) {
                int asciiWidth = Math.min(16, array.length - rowOffset);
                builder.append("     ");
                builder.append(new String(array, rowOffset, asciiWidth, StandardCharsets.UTF_8).replaceAll("\r\n", " ").replaceAll("\n", " "));
            }
            builder.append(String.format("%n", new Object[0]));
        }
        return builder.toString();
    }

    public PacketSent(PacketBufferNode packetBufferNode) {
        this.packetBufferNode = packetBufferNode;
    }

    public PacketBufferNode getPacketBufferNode() {
        return this.packetBufferNode;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof PacketSent)) {
            return false;
        }
        PacketSent other = (PacketSent)o;
        if (!other.canEqual(this)) {
            return false;
        }
        PacketBufferNode this$packetBufferNode = this.getPacketBufferNode();
        PacketBufferNode other$packetBufferNode = other.getPacketBufferNode();
        return !(this$packetBufferNode == null ? other$packetBufferNode != null : !this$packetBufferNode.equals(other$packetBufferNode));
    }

    protected boolean canEqual(Object other) {
        return other instanceof PacketSent;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        PacketBufferNode $packetBufferNode = this.getPacketBufferNode();
        result = result * 59 + ($packetBufferNode == null ? 43 : $packetBufferNode.hashCode());
        return result;
    }

    public String toString() {
        return "PacketSent(packetBufferNode=" + String.valueOf(this.getPacketBufferNode()) + ")";
    }
}


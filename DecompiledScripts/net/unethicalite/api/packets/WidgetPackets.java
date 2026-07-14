/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Item
 *  net.runelite.api.packets.ClientPacket
 *  net.runelite.api.packets.PacketBufferNode
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 */
package net.unethicalite.api.packets;

import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.packets.ClientPacket;
import net.runelite.api.packets.PacketBufferNode;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.game.Game;
import net.unethicalite.client.Static;

public class WidgetPackets {
    public static void widgetFirstOption(Widget widget) {
        WidgetPackets.queueWidgetAction1Packet(widget.getId(), widget.getItemId(), widget.getIndex());
    }

    public static void widgetSecondOption(Widget widget) {
        WidgetPackets.queueWidgetAction2Packet(widget.getId(), widget.getItemId(), widget.getIndex());
    }

    public static void widgetThirdOption(Widget widget) {
        WidgetPackets.queueWidgetAction3Packet(widget.getId(), widget.getItemId(), widget.getIndex());
    }

    public static void widgetFourthOption(Widget widget) {
        WidgetPackets.queueWidgetAction4Packet(widget.getId(), widget.getItemId(), widget.getIndex());
    }

    public static void widgetFifthOption(Widget widget) {
        WidgetPackets.queueWidgetAction5Packet(widget.getId(), widget.getItemId(), widget.getIndex());
    }

    public static void widgetSixthOption(Widget widget) {
        WidgetPackets.queueWidgetAction6Packet(widget.getId(), widget.getItemId(), widget.getIndex());
    }

    public static void widgetSeventhOption(Widget widget) {
        WidgetPackets.queueWidgetAction7Packet(widget.getId(), widget.getItemId(), widget.getIndex());
    }

    public static void widgetEighthOption(Widget widget) {
        WidgetPackets.queueWidgetAction8Packet(widget.getId(), widget.getItemId(), widget.getIndex());
    }

    public static void widgetNinthOption(Widget widget) {
        WidgetPackets.queueWidgetAction9Packet(widget.getId(), widget.getItemId(), widget.getIndex());
    }

    public static void widgetTenthOption(Widget widget) {
        WidgetPackets.queueWidgetAction10Packet(widget.getId(), widget.getItemId(), widget.getIndex());
    }

    public static void widgetItemAction(WidgetInfo container, Item item, String action) {
        int index = item.getActionIndex(action);
        WidgetPackets.widgetItemAction(container, item, index);
    }

    public static void widgetItemAction(WidgetInfo container, Item item, int index) {
        int widgetPackedId = container.getPackedId();
        int itemId = item.getId();
        int itemSlot = item.getSlot();
        switch (index) {
            case 0: {
                WidgetPackets.queueWidgetAction1Packet(widgetPackedId, itemId, itemSlot);
                break;
            }
            case 1: {
                WidgetPackets.queueWidgetAction2Packet(widgetPackedId, itemId, itemSlot);
                break;
            }
            case 2: {
                WidgetPackets.queueWidgetAction3Packet(widgetPackedId, itemId, itemSlot);
                break;
            }
            case 3: {
                WidgetPackets.queueWidgetAction4Packet(widgetPackedId, itemId, itemSlot);
                break;
            }
            case 4: {
                WidgetPackets.queueWidgetAction5Packet(widgetPackedId, itemId, itemSlot);
                break;
            }
            case 5: {
                WidgetPackets.queueWidgetAction6Packet(widgetPackedId, itemId, itemSlot);
                break;
            }
            case 6: {
                WidgetPackets.queueWidgetAction7Packet(widgetPackedId, itemId, itemSlot);
                break;
            }
            case 7: {
                WidgetPackets.queueWidgetAction8Packet(widgetPackedId, itemId, itemSlot);
                break;
            }
            case 8: {
                WidgetPackets.queueWidgetAction9Packet(widgetPackedId, itemId, itemSlot);
                break;
            }
            case 9: {
                WidgetPackets.queueWidgetAction10Packet(widgetPackedId, itemId, itemSlot);
            }
        }
    }

    public static void widgetAction(Widget widget, String action) {
        int index = widget.getActionIndex(action);
        switch (index) {
            case 0: {
                WidgetPackets.widgetFirstOption(widget);
                break;
            }
            case 1: {
                WidgetPackets.widgetSecondOption(widget);
                break;
            }
            case 2: {
                WidgetPackets.widgetThirdOption(widget);
                break;
            }
            case 3: {
                WidgetPackets.widgetFourthOption(widget);
                break;
            }
            case 4: {
                WidgetPackets.widgetFifthOption(widget);
                break;
            }
            case 5: {
                WidgetPackets.widgetSixthOption(widget);
                break;
            }
            case 6: {
                WidgetPackets.widgetSeventhOption(widget);
                break;
            }
            case 7: {
                WidgetPackets.widgetEighthOption(widget);
                break;
            }
            case 8: {
                WidgetPackets.widgetNinthOption(widget);
                break;
            }
            case 9: {
                WidgetPackets.widgetTenthOption(widget);
            }
        }
    }

    public static void queueWidgetAction1Packet(int widgetId, int itemId, int childId) {
        WidgetPackets.createFirstAction(widgetId, itemId, childId).send();
    }

    public static void queueWidgetAction2Packet(int widgetId, int itemId, int childId) {
        WidgetPackets.createSecondAction(widgetId, itemId, childId).send();
    }

    public static void queueWidgetAction3Packet(int widgetId, int itemId, int childId) {
        WidgetPackets.createThirdAction(widgetId, itemId, childId).send();
    }

    public static void queueWidgetAction4Packet(int widgetId, int itemId, int childId) {
        WidgetPackets.createFourthAction(widgetId, itemId, childId).send();
    }

    public static void queueWidgetAction5Packet(int widgetId, int itemId, int childId) {
        WidgetPackets.createFifthAction(widgetId, itemId, childId).send();
    }

    public static void queueWidgetAction6Packet(int widgetId, int itemId, int childId) {
        WidgetPackets.createSixthAction(widgetId, itemId, childId).send();
    }

    public static void queueWidgetAction7Packet(int widgetId, int itemId, int childId) {
        WidgetPackets.createSeventhAction(widgetId, itemId, childId).send();
    }

    public static void queueWidgetAction8Packet(int widgetId, int itemId, int childId) {
        WidgetPackets.createEighthAction(widgetId, itemId, childId).send();
    }

    public static void queueWidgetAction9Packet(int widgetId, int itemId, int childId) {
        WidgetPackets.createNinthAction(widgetId, itemId, childId).send();
    }

    public static void queueWidgetAction10Packet(int widgetId, int itemId, int childId) {
        WidgetPackets.createTenthAction(widgetId, itemId, childId).send();
    }

    public static void queueResumePauseWidgetPacket(int widgetId, int childId) {
        WidgetPackets.createContinuePacket(widgetId, childId).send();
    }

    public static void queueWidgetOnWidgetPacket(int sourceWidgetId, int sourceSlot, int sourceItemId, int destinationWidgetId, int destinationSlot, int destinationItemId) {
        WidgetPackets.createWidgetOnWidget(sourceWidgetId, sourceSlot, sourceItemId, destinationWidgetId, destinationSlot, destinationItemId).send();
    }

    public static void queueResumeObjectPacket(int itemId) {
        WidgetPackets.createContinueObjectPacket(itemId).send();
    }

    public static void queueWidgetTypePacket(int widgetId) {
        WidgetPackets.createWidgetTypePacket(widgetId).send();
    }

    public static PacketBufferNode createType1Action(int widgetId) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packet = client.preparePacket(clientPacket.IF_BUTTON10(), client.getPacketWriter().getIsaacCipher());
        packet.getPacketBuffer().writeInt(widgetId);
        return packet;
    }

    public static PacketBufferNode createDefaultAction(int type, int widgetPackedId, int itemId, int itemSlot) {
        switch (type) {
            case 1: {
                return WidgetPackets.createFirstAction(widgetPackedId, itemId, itemSlot);
            }
            case 2: {
                return WidgetPackets.createSecondAction(widgetPackedId, itemId, itemSlot);
            }
            case 3: {
                return WidgetPackets.createThirdAction(widgetPackedId, itemId, itemSlot);
            }
            case 4: {
                return WidgetPackets.createFourthAction(widgetPackedId, itemId, itemSlot);
            }
            case 5: {
                return WidgetPackets.createFifthAction(widgetPackedId, itemId, itemSlot);
            }
            case 6: {
                return WidgetPackets.createSixthAction(widgetPackedId, itemId, itemSlot);
            }
            case 7: {
                return WidgetPackets.createSeventhAction(widgetPackedId, itemId, itemSlot);
            }
            case 8: {
                return WidgetPackets.createEighthAction(widgetPackedId, itemId, itemSlot);
            }
            case 9: {
                return WidgetPackets.createNinthAction(widgetPackedId, itemId, itemSlot);
            }
            case 10: {
                return WidgetPackets.createTenthAction(widgetPackedId, itemId, itemSlot);
            }
        }
        throw new IllegalArgumentException("Invalid widget action type: " + type);
    }

    public static PacketBufferNode createFirstAction(int widgetId, int itemId, int childId) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.IF_BUTTON1(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeInt(widgetId);
        packetBufferNode.getPacketBuffer().writeShort(childId);
        packetBufferNode.getPacketBuffer().writeShort(itemId);
        return packetBufferNode;
    }

    public static PacketBufferNode createSecondAction(int widgetId, int itemId, int childId) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.IF_BUTTON2(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeInt(widgetId);
        packetBufferNode.getPacketBuffer().writeShort(childId);
        packetBufferNode.getPacketBuffer().writeShort(itemId);
        return packetBufferNode;
    }

    public static PacketBufferNode createThirdAction(int widgetId, int itemId, int childId) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.IF_BUTTON3(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeInt(widgetId);
        packetBufferNode.getPacketBuffer().writeShort(childId);
        packetBufferNode.getPacketBuffer().writeShort(itemId);
        return packetBufferNode;
    }

    public static PacketBufferNode createFourthAction(int widgetId, int itemId, int childId) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.IF_BUTTON4(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeInt(widgetId);
        packetBufferNode.getPacketBuffer().writeShort(childId);
        packetBufferNode.getPacketBuffer().writeShort(itemId);
        return packetBufferNode;
    }

    public static PacketBufferNode createFifthAction(int widgetId, int itemId, int childId) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.IF_BUTTON5(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeInt(widgetId);
        packetBufferNode.getPacketBuffer().writeShort(childId);
        packetBufferNode.getPacketBuffer().writeShort(itemId);
        return packetBufferNode;
    }

    public static PacketBufferNode createSixthAction(int widgetId, int itemId, int childId) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.IF_BUTTON6(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeInt(widgetId);
        packetBufferNode.getPacketBuffer().writeShort(childId);
        packetBufferNode.getPacketBuffer().writeShort(itemId);
        return packetBufferNode;
    }

    public static PacketBufferNode createSeventhAction(int widgetId, int itemId, int childId) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.IF_BUTTON7(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeInt(widgetId);
        packetBufferNode.getPacketBuffer().writeShort(childId);
        packetBufferNode.getPacketBuffer().writeShort(itemId);
        return packetBufferNode;
    }

    public static PacketBufferNode createEighthAction(int widgetId, int itemId, int childId) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.IF_BUTTON8(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeInt(widgetId);
        packetBufferNode.getPacketBuffer().writeShort(childId);
        packetBufferNode.getPacketBuffer().writeShort(itemId);
        return packetBufferNode;
    }

    public static PacketBufferNode createNinthAction(int widgetId, int itemId, int childId) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.IF_BUTTON9(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeInt(widgetId);
        packetBufferNode.getPacketBuffer().writeShort(childId);
        packetBufferNode.getPacketBuffer().writeShort(itemId);
        return packetBufferNode;
    }

    public static PacketBufferNode createTenthAction(int widgetId, int itemId, int childId) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.IF_BUTTON10(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeInt(widgetId);
        packetBufferNode.getPacketBuffer().writeShort(childId);
        packetBufferNode.getPacketBuffer().writeShort(itemId);
        return packetBufferNode;
    }

    public static PacketBufferNode createWidgetOnWidget(int sourceWidgetId, int sourceSlot, int sourceItemId, int itemId, int destinationSlot, int widgetId) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.IF_BUTTONT(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeShortLE(destinationSlot);
        packetBufferNode.getPacketBuffer().writeShortAddLE(sourceItemId);
        packetBufferNode.getPacketBuffer().writeIntIME(sourceWidgetId);
        packetBufferNode.getPacketBuffer().writeShortLE(itemId);
        packetBufferNode.getPacketBuffer().writeShortAdd(sourceSlot);
        packetBufferNode.getPacketBuffer().writeIntIME(widgetId);
        return packetBufferNode;
    }

    public static PacketBufferNode createContinuePacket(int widgetId, int childId) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.RESUME_PAUSEBUTTON(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeShort(childId);
        packetBufferNode.getPacketBuffer().writeInt(widgetId);
        return packetBufferNode;
    }

    public static PacketBufferNode createContinueObjectPacket(int itemId) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.RESUME_OBJDIALOG(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeShort(itemId);
        return packetBufferNode;
    }

    public static PacketBufferNode createWidgetTypePacket(int widgetId) {
        Client client = Static.getClient();
        ClientPacket clientPacket = Game.getClientPacket();
        PacketBufferNode packetBufferNode = Static.getClient().preparePacket(clientPacket.WIDGET_TYPE(), client.getPacketWriter().getIsaacCipher());
        packetBufferNode.getPacketBuffer().writeInt(widgetId);
        return packetBufferNode;
    }
}


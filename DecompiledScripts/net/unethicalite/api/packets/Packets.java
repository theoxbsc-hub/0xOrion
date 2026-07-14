/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.MenuAction
 *  net.runelite.api.packets.ClientPacket
 *  net.runelite.api.packets.PacketBufferNode
 *  net.runelite.api.packets.PacketWriter
 *  net.runelite.api.widgets.Widget
 */
package net.unethicalite.api.packets;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.packets.ClientPacket;
import net.runelite.api.packets.PacketBufferNode;
import net.runelite.api.packets.PacketWriter;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.events.MenuAutomated;
import net.unethicalite.api.exception.InteractionException;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.api.packets.GroundItemPackets;
import net.unethicalite.api.packets.ItemPackets;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.NPCPackets;
import net.unethicalite.api.packets.ObjectPackets;
import net.unethicalite.api.packets.PlayerPackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

public class Packets {
    public static void queuePacket(ClientPacket clientPacket, Object ... data) {
        PacketWriter writer = Static.getClient().getPacketWriter();
        PacketBufferNode packet = Static.getClient().preparePacket(clientPacket, writer.getIsaacCipher());
        for (Object o : data) {
            if (o instanceof Byte) {
                packet.getPacketBuffer().writeByte(((Integer)o).intValue());
                continue;
            }
            if (o instanceof Short) {
                packet.getPacketBuffer().writeShort(((Integer)o).intValue());
                continue;
            }
            if (o instanceof Integer) {
                packet.getPacketBuffer().writeInt(((Integer)o).intValue());
                continue;
            }
            if (o instanceof Long) {
                packet.getPacketBuffer().writeLong(((Long)o).longValue());
                continue;
            }
            if (o instanceof String) {
                packet.getPacketBuffer().writeStringCp1252NullTerminated((String)o);
                continue;
            }
            return;
        }
        packet.send();
        writer.queuePacket(packet);
    }

    public static void queuePacket(PacketBufferNode packet) {
        GameThread.invoke(() -> Static.getClient().getPacketWriter().queuePacket(packet));
    }

    public static PacketBufferNode fromAutomatedMenu(MenuAutomated menu) {
        MenuAction opcode = menu.getOpcode();
        Client client = Static.getClient();
        int id = menu.getIdentifier();
        int param0 = menu.getParam0();
        int param1 = menu.getParam1();
        int selectedWidgetItemId = client.getSelectedSpellItemId();
        int selectedWidgetSlot = client.getSelectedSpellChildIndex();
        int selectedWidget = client.getSelectedSpellWidget();
        int selectedSpellWidget = client.getSelectedSpellWidget();
        switch (opcode) {
            case ITEM_USE_ON_GAME_OBJECT: 
            case WIDGET_TARGET_ON_GAME_OBJECT: {
                return ObjectPackets.createWidgetOnObjectPacket(id, param0 + client.getBaseX(), param1 + client.getBaseY(), selectedWidgetSlot, selectedWidgetItemId, selectedWidget, false);
            }
            case GAME_OBJECT_FIRST_OPTION: {
                return ObjectPackets.createObjectFirstActionPacket(id, param0 + client.getBaseX(), param1 + client.getBaseY(), false);
            }
            case GAME_OBJECT_SECOND_OPTION: {
                return ObjectPackets.createObjectSecondActionPacket(id, param0 + client.getBaseX(), param1 + client.getBaseY(), false);
            }
            case GAME_OBJECT_THIRD_OPTION: {
                return ObjectPackets.createObjectThirdActionPacket(id, param0 + client.getBaseX(), param1 + client.getBaseY(), false);
            }
            case GAME_OBJECT_FOURTH_OPTION: {
                return ObjectPackets.createObjectFourthActionPacket(id, param0 + client.getBaseX(), param1 + client.getBaseY(), false);
            }
            case GAME_OBJECT_FIFTH_OPTION: {
                return ObjectPackets.createObjectFifthActionPacket(id, param0 + client.getBaseX(), param1 + client.getBaseY(), false);
            }
            case ITEM_USE_ON_NPC: 
            case WIDGET_TARGET_ON_NPC: {
                return NPCPackets.createWidgetOnNpc(id, selectedWidget, selectedWidgetItemId, selectedWidgetSlot, false);
            }
            case NPC_FIRST_OPTION: {
                return NPCPackets.createNpcFirstActionPacket(id, false);
            }
            case NPC_SECOND_OPTION: {
                return NPCPackets.createNpcSecondActionPacket(id, false);
            }
            case NPC_THIRD_OPTION: {
                return NPCPackets.createNpcThirdActionPacket(id, false);
            }
            case NPC_FOURTH_OPTION: {
                return NPCPackets.createNpcFourthActionPacket(id, false);
            }
            case NPC_FIFTH_OPTION: {
                return NPCPackets.createNpcFifthActionPacket(id, false);
            }
            case ITEM_USE_ON_PLAYER: 
            case WIDGET_TARGET_ON_PLAYER: {
                return PlayerPackets.createWidgetOnPlayer(id, selectedWidgetItemId, selectedWidgetSlot, selectedWidget, false);
            }
            case PLAYER_FIRST_OPTION: {
                return PlayerPackets.createFirstAction(id, false);
            }
            case PLAYER_SECOND_OPTION: {
                return PlayerPackets.createSecondAction(id, false);
            }
            case PLAYER_THIRD_OPTION: {
                return PlayerPackets.createThirdAction(id, false);
            }
            case PLAYER_FOURTH_OPTION: {
                return PlayerPackets.createFourthAction(id, false);
            }
            case PLAYER_FIFTH_OPTION: {
                return PlayerPackets.createFifthAction(id, false);
            }
            case PLAYER_SIXTH_OPTION: {
                return PlayerPackets.createSixthAction(id, false);
            }
            case PLAYER_SEVENTH_OPTION: {
                return PlayerPackets.createSeventhAction(id, false);
            }
            case PLAYER_EIGHTH_OPTION: {
                return PlayerPackets.createEighthAction(id, false);
            }
            case ITEM_USE_ON_GROUND_ITEM: 
            case WIDGET_TARGET_ON_GROUND_ITEM: {
                return GroundItemPackets.createWidgetOnGroundItem(id, param0 + client.getBaseX(), param1 + client.getBaseY(), selectedWidgetSlot, selectedWidgetItemId, selectedWidget, false);
            }
            case GROUND_ITEM_FIRST_OPTION: {
                return GroundItemPackets.createFirstAction(id, param0 + client.getBaseX(), param1 + client.getBaseY(), false);
            }
            case GROUND_ITEM_SECOND_OPTION: {
                return GroundItemPackets.createSecondAction(id, param0 + client.getBaseX(), param1 + client.getBaseY(), false);
            }
            case GROUND_ITEM_THIRD_OPTION: {
                return GroundItemPackets.createThirdAction(id, param0 + client.getBaseX(), param1 + client.getBaseY(), false);
            }
            case GROUND_ITEM_FOURTH_OPTION: {
                return GroundItemPackets.createFourthAction(id, param0 + client.getBaseX(), param1 + client.getBaseY(), false);
            }
            case GROUND_ITEM_FIFTH_OPTION: {
                return GroundItemPackets.createFifthAction(id, param0 + client.getBaseX(), param1 + client.getBaseY(), false);
            }
            case ITEM_USE_ON_ITEM: 
            case WIDGET_TARGET_ON_WIDGET: {
                Widget targetParent = Widgets.fromId(param1);
                assert (targetParent != null);
                Widget targetChild = targetParent.getChild(param0);
                int childItemId = -1;
                if (targetChild != null) {
                    childItemId = targetChild.getItemId();
                }
                Widget source = client.getWidget(selectedWidget);
                assert (source != null);
                if (source.getType() == 5) {
                    selectedWidgetSlot = -1;
                    selectedWidgetItemId = -1;
                }
                return WidgetPackets.createWidgetOnWidget(selectedWidget, selectedWidgetSlot, selectedWidgetItemId, childItemId, param0, param1);
            }
            case ITEM_FIRST_OPTION: {
                return ItemPackets.createFirstAction(param1, id, param0);
            }
            case ITEM_SECOND_OPTION: {
                return ItemPackets.createSecondAction(param1, id, param0);
            }
            case ITEM_THIRD_OPTION: {
                return ItemPackets.createThirdAction(param1, id, param0);
            }
            case ITEM_FOURTH_OPTION: {
                return ItemPackets.createFourthAction(param1, id, param0);
            }
            case ITEM_FIFTH_OPTION: {
                return ItemPackets.createFifthAction(param1, id, param0);
            }
            case WIDGET_FIRST_OPTION: {
                return WidgetPackets.createFirstAction(param1, -1, param0);
            }
            case WIDGET_SECOND_OPTION: {
                return WidgetPackets.createSecondAction(param1, -1, param0);
            }
            case WIDGET_THIRD_OPTION: {
                return WidgetPackets.createThirdAction(param1, -1, param0);
            }
            case WIDGET_FOURTH_OPTION: {
                return WidgetPackets.createFourthAction(param1, -1, param0);
            }
            case WIDGET_FIFTH_OPTION: {
                return WidgetPackets.createFifthAction(param1, -1, param0);
            }
            case WIDGET_TYPE_1: {
                return WidgetPackets.createType1Action(param1);
            }
            case WIDGET_CONTINUE: {
                return WidgetPackets.createContinuePacket(param1, param0);
            }
            case WALK: {
                client.setDestinationX(param0);
                client.setDestinationY(param1);
                return MovementPackets.createMovement(param0 + client.getBaseX(), param1 + client.getBaseY(), false);
            }
        }
        throw new InteractionException("Couldn't parse packet from opcode: " + String.valueOf(opcode));
    }
}


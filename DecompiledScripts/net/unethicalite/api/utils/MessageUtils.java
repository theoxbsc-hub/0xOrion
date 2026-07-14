/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.ChatMessageType
 *  net.runelite.client.chat.ChatColorType
 *  net.runelite.client.chat.ChatMessageBuilder
 *  net.runelite.client.chat.QueuedMessage
 */
package net.unethicalite.api.utils;

import net.runelite.api.ChatMessageType;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.QueuedMessage;
import net.unethicalite.client.Static;

public class MessageUtils {
    public static void addMessage(String message, ChatColorType colorType, ChatMessageType messageType) {
        String chatMessage = new ChatMessageBuilder().append(colorType).append(message).build();
        Static.getChatMessageManager().queue(QueuedMessage.builder().type(messageType).runeLiteFormattedMessage(chatMessage).build());
    }

    public static void addMessage(String message, ChatMessageType messageType) {
        MessageUtils.addMessage(message, ChatColorType.HIGHLIGHT, messageType);
    }

    public static void addMessage(String message, ChatColorType colorType) {
        MessageUtils.addMessage(message, colorType, ChatMessageType.CONSOLE);
    }

    public static void addMessage(String message) {
        MessageUtils.addMessage(message, ChatColorType.HIGHLIGHT, ChatMessageType.CONSOLE);
    }
}


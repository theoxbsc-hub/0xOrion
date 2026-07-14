/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.chatresponder;

final class IncomingMessage {
    final String sender;
    final String text;
    final long ts;
    final boolean pm;

    IncomingMessage(String string, String string2, long l, boolean bl) {
        this.sender = string;
        this.text = string2;
        this.ts = l;
        this.pm = bl;
    }
}


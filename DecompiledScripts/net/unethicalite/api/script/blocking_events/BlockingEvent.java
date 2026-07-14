/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.script.blocking_events;

public abstract class BlockingEvent {
    public abstract boolean validate();

    public abstract int loop();
}


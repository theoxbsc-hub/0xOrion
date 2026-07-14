/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api;

public interface PluginTask {
    public boolean validate();

    public int execute();

    default public boolean blocking() {
        return true;
    }
}


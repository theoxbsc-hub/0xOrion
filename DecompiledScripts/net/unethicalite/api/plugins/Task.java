/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.plugins;

public interface Task {
    public boolean validate();

    public int execute();

    default public boolean isBlocking() {
        return true;
    }

    default public boolean subscribe() {
        return false;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.plugins;

import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.plugins.Task;

public abstract class TaskPlugin
extends LoopedPlugin {
    private String currentTask = null;

    public abstract Task[] getTasks();

    @Override
    protected int loop() {
        for (Task task : this.getTasks()) {
            if (!task.validate()) continue;
            this.currentTask = task.getClass().getSimpleName();
            int delay = task.execute();
            if (!task.isBlocking()) continue;
            return delay;
        }
        return 1000;
    }

    public String getCurrentTask() {
        return this.currentTask;
    }
}


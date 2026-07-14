package net.runelite.client.plugins.ox.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/** Collects enabled 0x plugins and prints a one-shot console list. */
public final class OxPluginRegistry {
    private static final CopyOnWriteArrayList<String> ENABLED = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean SCHEDULED = new AtomicBoolean(false);
    private static final AtomicBoolean PRINTED = new AtomicBoolean(false);

    private OxPluginRegistry() {
    }

    public static void register(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return;
        }
        if (!ENABLED.contains(displayName)) {
            ENABLED.add(displayName);
        }
        if (SCHEDULED.compareAndSet(false, true)) {
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(2500L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                printSummary();
            }, "ox-plugin-summary");
            t.setDaemon(true);
            t.start();
        }
    }

    private static void printSummary() {
        if (!PRINTED.compareAndSet(false, true)) {
            return;
        }
        List<String> copy = new ArrayList<>(ENABLED);
        OxConsole.plugins(copy.toArray(new String[0]));
    }
}

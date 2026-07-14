package net.runelite.client.plugins.ox.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Live registry of which 0x scripts are running and their current stats (for 0xINFO).
 */
public final class OxScriptActivity {
    private static final ConcurrentHashMap<String, Snapshot> LIVE = new ConcurrentHashMap<>();

    private OxScriptActivity() {
    }

    public static void update(String scriptId, String displayName, Map<String, String> stats) {
        if (scriptId == null || scriptId.isEmpty()) {
            return;
        }
        LinkedHashMap<String, String> copy = new LinkedHashMap<>();
        if (stats != null) {
            copy.putAll(stats);
        }
        LIVE.put(scriptId, new Snapshot(
            scriptId,
            displayName == null || displayName.isEmpty() ? scriptId : displayName,
            System.currentTimeMillis(),
            Collections.unmodifiableMap(copy)
        ));
    }

    public static void clear(String scriptId) {
        if (scriptId != null) {
            Snapshot removed = LIVE.remove(scriptId);
            if (removed != null) {
                try {
                    OxNotify.scriptStop(removed.displayName, "session ended");
                } catch (Throwable ignored) {
                    // never break script teardown
                }
            }
        }
    }

    public static Map<String, Snapshot> all() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(LIVE));
    }

    public static Snapshot primary() {
        Snapshot first = null;
        for (Snapshot s : LIVE.values()) {
            if (first == null) {
                first = s;
            }
            // Prefer bots over info panels
            if (s.scriptId.startsWith("wc") || s.displayName.toLowerCase().contains("bot")) {
                return s;
            }
        }
        return first;
    }

    public static final class Snapshot {
        public final String scriptId;
        public final String displayName;
        public final long updatedMs;
        public final Map<String, String> stats;

        Snapshot(String scriptId, String displayName, long updatedMs, Map<String, String> stats) {
            this.scriptId = scriptId;
            this.displayName = displayName;
            this.updatedMs = updatedMs;
            this.stats = stats;
        }
    }
}

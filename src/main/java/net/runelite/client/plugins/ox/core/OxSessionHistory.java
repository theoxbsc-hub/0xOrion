package net.runelite.client.plugins.ox.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Persists finished script sessions under ~/.oxrl./stats/sessions.json
 */
public final class OxSessionHistory {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<Session>>() {
    }.getType();
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final int MAX_STORED = 80;

    private OxSessionHistory() {
    }

    public static void save(Session session) {
        if (session == null) {
            return;
        }
        LOCK.lock();
        try {
            List<Session> all = new ArrayList<>(loadUnlocked());
            // Replace in-progress entry with same id if present
            all.removeIf(s -> session.id != null && session.id.equals(s.id));
            all.add(0, session);
            while (all.size() > MAX_STORED) {
                all.remove(all.size() - 1);
            }
            writeUnlocked(all);
        } finally {
            LOCK.unlock();
        }
    }

    public static List<Session> recent(int limit) {
        LOCK.lock();
        try {
            List<Session> all = loadUnlocked();
            if (all.isEmpty() || limit <= 0) {
                return Collections.emptyList();
            }
            return new ArrayList<>(all.subList(0, Math.min(limit, all.size())));
        } finally {
            LOCK.unlock();
        }
    }

    public static List<Session> recentForAccount(String accountId, int limit) {
        LOCK.lock();
        try {
            List<Session> all = loadUnlocked();
            ArrayList<Session> filtered = new ArrayList<>();
            for (Session s : all) {
                if (accountId == null || accountId.isEmpty()
                    || accountId.equals(s.accountId)
                    || accountId.equalsIgnoreCase(s.accountName)) {
                    filtered.add(s);
                    if (filtered.size() >= limit) {
                        break;
                    }
                }
            }
            return filtered;
        } finally {
            LOCK.unlock();
        }
    }

    private static List<Session> loadUnlocked() {
        Path path = historyPath();
        if (!Files.isRegularFile(path)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            List<Session> list = GSON.fromJson(reader, LIST_TYPE);
            return list == null ? new ArrayList<>() : new ArrayList<>(list);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static void writeUnlocked(List<Session> sessions) {
        Path path = historyPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(sessions, LIST_TYPE, writer);
            }
        } catch (IOException ignored) {
            // history is best-effort
        }
    }

    private static Path historyPath() {
        String home = System.getProperty("user.home", ".");
        return Paths.get(home, ".oxrl.", "stats", "sessions.json");
    }

    public static final class Session {
        public String id;
        public String script;
        public String accountId;
        public String accountName;
        public long startMs;
        public long endMs;
        public int logsBanked;
        public int logsDropped;
        public int bankTrips;
        public int steals;
        public int sells;
        public int xpGained;
        public int barsSmelted;
        public int itemsSmithed;
        public int itemsGathered;
        public int tradesAccepted;
        public String tree;
        public String note;

        public Session() {
        }

        public long durationMs() {
            long end = endMs > 0L ? endMs : System.currentTimeMillis();
            return Math.max(0L, end - startMs);
        }

        public String summaryLine() {
            StringBuilder sb = new StringBuilder();
            sb.append(script == null ? "Script" : script);
            sb.append(" · ").append(formatDuration(durationMs()));
            String key = script == null ? "" : script.toLowerCase();
            boolean thieve = key.contains("thieve") || key.contains("thiev");
            boolean wood = key.contains("wc") || key.contains("wood");
            boolean mine = key.contains("mine") || key.contains("mining");
            boolean fish = key.contains("fish");
            boolean smith = key.contains("smith");
            boolean mule = key.contains("mule");
            if (thieve) {
                if (steals > 0) {
                    sb.append(" · Steals ").append(steals);
                }
                if (sells > 0) {
                    sb.append(" · Sells ").append(sells);
                }
            } else if (wood) {
                if (logsBanked > 0) {
                    sb.append(" · Banked ").append(logsBanked);
                }
                if (logsDropped > 0) {
                    sb.append(" · Dropped ").append(logsDropped);
                }
                if (bankTrips > 0) {
                    sb.append(" · Trips ").append(bankTrips);
                }
            } else if (mine) {
                if (bankTrips > 0) {
                    sb.append(" · Trips ").append(bankTrips);
                }
                if (xpGained > 0) {
                    sb.append(" · XP ").append(xpGained);
                }
                if (itemsGathered > 0) {
                    sb.append(" · Ores ").append(itemsGathered);
                }
                if (barsSmelted > 0) {
                    sb.append(" · Bars ").append(barsSmelted);
                }
            } else if (fish) {
                if (bankTrips > 0) {
                    sb.append(" · Deposits ").append(bankTrips);
                }
                if (itemsGathered > 0) {
                    sb.append(" · Fish ").append(itemsGathered);
                }
            } else if (smith) {
                if (barsSmelted > 0) {
                    sb.append(" · Bars ").append(barsSmelted);
                }
                if (itemsSmithed > 0) {
                    sb.append(" · Items ").append(itemsSmithed);
                }
                if (bankTrips > 0) {
                    sb.append(" · Trips ").append(bankTrips);
                }
                if (xpGained > 0) {
                    sb.append(" · XP ").append(xpGained);
                }
            } else if (mule) {
                if (tradesAccepted > 0) {
                    sb.append(" · Trades ").append(tradesAccepted);
                }
                if (bankTrips > 0) {
                    sb.append(" · Banked ").append(bankTrips);
                }
                if (itemsGathered > 0) {
                    sb.append(" · Items ").append(itemsGathered);
                }
            } else {
                if (steals > 0) {
                    sb.append(" · Steals ").append(steals);
                }
                if (sells > 0) {
                    sb.append(" · Sells ").append(sells);
                }
                if (logsBanked > 0) {
                    sb.append(" · Banked ").append(logsBanked);
                }
                if (logsDropped > 0) {
                    sb.append(" · Dropped ").append(logsDropped);
                }
                if (bankTrips > 0) {
                    sb.append(" · Trips ").append(bankTrips);
                }
                if (xpGained > 0) {
                    sb.append(" · XP ").append(xpGained);
                }
                if (barsSmelted > 0) {
                    sb.append(" · Bars ").append(barsSmelted);
                }
                if (itemsSmithed > 0) {
                    sb.append(" · Items ").append(itemsSmithed);
                }
                if (itemsGathered > 0) {
                    sb.append(" · Gathered ").append(itemsGathered);
                }
            }
            if (tree != null && !tree.isEmpty() && !"—".equals(tree)) {
                sb.append(" · ").append(tree);
            }
            return sb.toString();
        }

        public String metaLine() {
            String who = accountName != null && !accountName.isEmpty() ? accountName
                : (accountId != null && !accountId.isEmpty() ? accountId : "unknown");
            return who + " · " + formatWhen(startMs);
        }

        private static String formatDuration(long ms) {
            long sec = ms / 1000L;
            long h = sec / 3600L;
            long m = (sec % 3600L) / 60L;
            long s = sec % 60L;
            if (h > 0L) {
                return h + "h " + m + "m";
            }
            if (m > 0L) {
                return m + "m " + s + "s";
            }
            return s + "s";
        }

        private static String formatWhen(long ms) {
            try {
                java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("MMM d HH:mm", Locale.UK);
                return fmt.format(new java.util.Date(ms));
            } catch (Exception e) {
                return String.valueOf(ms);
            }
        }
    }
}

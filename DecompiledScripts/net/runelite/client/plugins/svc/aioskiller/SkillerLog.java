/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.aioskiller;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SkillerLog {
    private final long sessionStartMs;
    private final Map<String, Issue> issues = new LinkedHashMap<String, Issue>();
    private boolean finished = false;
    private volatile int errorCount = 0;
    private volatile String lastSummary = "";
    private volatile Kind lastKind = null;

    public SkillerLog(long l) {
        this.sessionStartMs = l;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void record(Kind kind, String string, String string2, String string3, String string4) {
        if (string3 == null || string3.isEmpty()) {
            return;
        }
        long l = System.currentTimeMillis();
        String string5 = String.valueOf((Object)kind) + "|" + SkillerLog.safe(string) + "|" + string3;
        Map<String, Issue> map = this.issues;
        synchronized (map) {
            Issue issue = this.issues.get(string5);
            if (issue == null) {
                this.issues.put(string5, new Issue(kind, SkillerLog.safe(string), SkillerLog.safe(string2), string3, string4, l));
            } else {
                issue.lastMs = l;
                ++issue.count;
            }
            this.errorCount = this.issues.size();
        }
        this.lastSummary = string3;
        this.lastKind = kind;
    }

    public boolean hasErrors() {
        return this.errorCount > 0;
    }

    public int errorCount() {
        return this.errorCount;
    }

    public String lastSummary() {
        return this.lastSummary;
    }

    public Kind lastKind() {
        return this.lastKind;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public List<Issue> snapshot() {
        Map<String, Issue> map = this.issues;
        synchronized (map) {
            return new ArrayList<Issue>(this.issues.values());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public File finish(boolean bl) {
        Object object = this.issues;
        synchronized (object) {
            if (this.finished) {
                return null;
            }
            this.finished = true;
            if (this.issues.isEmpty()) {
                return null;
            }
        }
        try {
            object = new File(System.getProperty("user.home"), ".orion/x64-logs");
            ((File)object).mkdirs();
            String string = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File file = new File((File)object, "aioskiller-report_" + string + ".log");
            this.write(file);
            if (bl) {
                this.open(file);
            }
            return file;
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private void write(File file) throws Exception {
        List<Issue> list = this.snapshot();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));){
            String string = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            long l = System.currentTimeMillis() - this.sessionStartMs;
            bufferedWriter.write("===============================================================\n");
            bufferedWriter.write("  x64 AIO Skiller \u2014 Run Report\n");
            bufferedWriter.write("  Generated:      " + string + "\n");
            bufferedWriter.write("  Session length: " + SkillerLog.duration(l) + "\n");
            bufferedWriter.write("  Issues found:   " + list.size() + "\n");
            bufferedWriter.write("===============================================================\n\n");
            bufferedWriter.write("This report was generated because the bot ran into problems during\n");
            bufferedWriter.write("the run. Each entry explains what happened, when, and how to fix it.\n");
            bufferedWriter.write("A clean run produces no report at all.\n\n");
            int n = 1;
            for (Issue issue : list) {
                bufferedWriter.write("---------------------------------------------------------------\n");
                bufferedWriter.write(" [" + n + "] " + issue.kind.title + " \u2014 " + issue.skill + "\n");
                bufferedWriter.write("---------------------------------------------------------------\n");
                bufferedWriter.write(" When:    +" + SkillerLog.duration(issue.firstMs - this.sessionStartMs) + " into the run (" + SkillerLog.clock(issue.firstMs) + ")\n");
                if (issue.count > 1) {
                    bufferedWriter.write(" Repeats: " + issue.count + " times, last at +" + SkillerLog.duration(issue.lastMs - this.sessionStartMs) + " (" + SkillerLog.clock(issue.lastMs) + ")\n");
                }
                bufferedWriter.write(" Phase:   " + issue.phase + "\n");
                bufferedWriter.write(" What:    " + issue.summary + "\n");
                if (!issue.detail.isEmpty()) {
                    bufferedWriter.write(" Context: " + issue.detail + "\n");
                }
                bufferedWriter.write(" Meaning: " + SkillerLog.indent(SkillerLog.meaning(issue.kind)) + "\n");
                bufferedWriter.write(" Fix:     " + SkillerLog.indent(SkillerLog.fix(issue.kind, issue.skill)) + "\n\n");
                ++n;
            }
            bufferedWriter.write("===============================================================\n");
            bufferedWriter.write(" End of report. Send this file to support if you need help.\n");
            bufferedWriter.write("===============================================================\n");
            bufferedWriter.flush();
        }
    }

    private void open(File file) {
        new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }, "x64-skiller-report-open").start();
    }

    private static String meaning(Kind kind) {
        switch (kind.ordinal()) {
            case 0: {
                return "The bot could not start or continue this skill because a required condition was missing (no materials, no tool, or nowhere to train), so it skipped the skill and moved on to the next one.";
            }
            case 1: {
                return "The bot stayed on this skill but gained no XP for several minutes \u2014 it was repeating the same action without making progress. This usually means a wrong NPC/object id, a blocked or unreachable path, or an interaction the server silently ignored (a stuck loop).";
            }
            case 2: {
                return "An unexpected internal error (exception) was thrown while running this skill. The bot caught it and kept going, but the action that threw it did not complete.";
            }
        }
        return "The bot reported a problem while running this skill.";
    }

    private static String fix(Kind kind, String string) {
        switch (kind.ordinal()) {
            case 0: {
                return "Check this skill's requirements: make any feeder skill run first (e.g. Mining before Smithing so the bank has ore), stock the bank with the needed materials/tools, or disable the skill if you can't supply it.";
            }
            case 1: {
                return "Verify the " + string + " training spot is correct and reachable: confirm the target NPC/object/rock ids, that the character can path to them, and that nothing (a wall, another player, an empty bank) is blocking the loop. Re-run the Entity Inspector at the spot if ids look wrong.";
            }
            case 2: {
                return "Send this report so the exception text + phase can be traced to the exact code path. The 'Context' line above holds the error message.";
            }
        }
        return "Send this report so we can investigate.";
    }

    private static String indent(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        int n = 0;
        for (String string2 : string.split(" ")) {
            if (n > 0 && n + string2.length() + 1 > 56) {
                stringBuilder.append("\n          ");
                n = 0;
            } else if (n > 0) {
                stringBuilder.append(' ');
                ++n;
            }
            stringBuilder.append(string2);
            n += string2.length();
        }
        return stringBuilder.toString();
    }

    private static String clock(long l) {
        return new SimpleDateFormat("HH:mm:ss").format(new Date(l));
    }

    private static String duration(long l) {
        if (l < 0L) {
            l = 0L;
        }
        long l2 = l / 1000L;
        long l3 = l2 / 3600L;
        long l4 = l2 % 3600L / 60L;
        long l5 = l2 % 60L;
        if (l3 > 0L) {
            return l3 + "h " + l4 + "m " + l5 + "s";
        }
        if (l4 > 0L) {
            return l4 + "m " + l5 + "s";
        }
        return l5 + "s";
    }

    private static String safe(String string) {
        return string == null || string.isEmpty() ? "\u2014" : string;
    }

    public static enum Kind {
        SKILL_SKIPPED("SKILL SKIPPED"),
        NO_PROGRESS("NO PROGRESS (possible stuck loop)"),
        EXCEPTION("INTERNAL ERROR"),
        PROBLEM("PROBLEM");

        final String title;

        private Kind(String string2) {
            this.title = string2;
        }
    }

    public static final class Issue {
        public final Kind kind;
        public final String skill;
        public final String phase;
        public final String summary;
        public final String detail;
        public final long firstMs;
        public long lastMs;
        public int count;

        Issue(Kind kind, String string, String string2, String string3, String string4, long l) {
            this.kind = kind;
            this.skill = string;
            this.phase = string2;
            this.summary = string3;
            this.detail = string4 == null ? "" : string4;
            this.firstMs = l;
            this.lastMs = l;
            this.count = 1;
        }
    }
}


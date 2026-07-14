/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.herblore;

import java.util.function.Predicate;

final class ItemMatcher {
    private final String key;
    private final Predicate<String> test;

    private ItemMatcher(String string, Predicate<String> predicate) {
        this.key = string;
        this.test = predicate;
    }

    String key() {
        return this.key;
    }

    boolean matches(String string) {
        return this.test.test(ItemMatcher.normalize(string));
    }

    static ItemMatcher grimy(String string) {
        String string3 = ItemMatcher.normalize(string);
        return new ItemMatcher("grimy:" + string3, string2 -> string2.contains("grimy") && string2.contains(string3));
    }

    static ItemMatcher clean(String string) {
        String string3 = ItemMatcher.normalize(string);
        return new ItemMatcher("clean:" + string3, string2 -> string2.contains(string3) && !string2.contains("grimy") && !string2.contains("unf") && !string2.contains("potion") && !string2.contains("vial"));
    }

    static ItemMatcher unf(String string) {
        String string3 = ItemMatcher.normalize(string);
        return new ItemMatcher("unf:" + string3, string2 -> string2.contains(string3) && string2.contains("unf"));
    }

    static ItemMatcher named(String string2) {
        String string3 = ItemMatcher.normalize(string2);
        String[] stringArray = string3.split(" ");
        return new ItemMatcher("name:" + string3, string -> {
            for (String string2 : stringArray) {
                if (string2.isEmpty() || ItemMatcher.containsToken(string, string2)) continue;
                return false;
            }
            return true;
        });
    }

    private static boolean containsToken(String string, String string2) {
        for (String string3 : string.split(" ")) {
            if (!string3.equals(string2) && !string3.startsWith(string2) && !string2.startsWith(string3)) continue;
            return true;
        }
        return false;
    }

    static String normalize(String string) {
        if (string == null) {
            return "";
        }
        String string2 = string.toLowerCase().replace('\u2019', '\'').replace('`', '\'');
        string2 = string2.replaceAll("[^a-z0-9 ]", " ");
        return string2.replaceAll("\\s+", " ").trim();
    }
}


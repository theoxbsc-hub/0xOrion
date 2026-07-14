/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.slayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SlayerNames {
    private static final Set<String> QUALIFIERS = new HashSet<String>(Arrays.asList("undead", "fossil", "island"));
    private static final List<Set<String>> SPELLING_VARIANTS = Arrays.asList(new HashSet<String>(Arrays.asList("wrath", "wraith")));

    private SlayerNames() {
    }

    public static boolean matches(String string, String string2) {
        if (string == null || string2 == null) {
            return false;
        }
        List<String> list = SlayerNames.significantWords(string);
        List<String> list2 = SlayerNames.significantWords(string2);
        if (list.isEmpty() || list2.isEmpty()) {
            return false;
        }
        return SlayerNames.phraseContains(list, list2) || SlayerNames.phraseContains(list2, list);
    }

    public static String core(String string) {
        List<String> list = SlayerNames.significantWords(string);
        if (list.isEmpty()) {
            list = SlayerNames.words(string);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); ++i) {
            String string2;
            String string3 = string2 = i == list.size() - 1 ? SlayerNames.singular(list.get(i)) : list.get(i);
            if (stringBuilder.length() > 0) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(string2);
        }
        return stringBuilder.toString();
    }

    private static List<String> significantWords(String string) {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string2 : SlayerNames.words(string)) {
            if (QUALIFIERS.contains(string2)) continue;
            arrayList.add(string2);
        }
        return arrayList;
    }

    private static List<String> words(String string) {
        ArrayList<String> arrayList = new ArrayList<String>();
        if (string == null) {
            return arrayList;
        }
        for (String string2 : string.toLowerCase().trim().split("\\s+")) {
            if (string2.isEmpty()) continue;
            arrayList.add(string2);
        }
        return arrayList;
    }

    private static boolean phraseContains(List<String> list, List<String> list2) {
        if (list2.isEmpty() || list.size() < list2.size()) {
            return false;
        }
        int n = 0;
        while (n + list2.size() <= list.size()) {
            boolean bl = true;
            for (int i = 0; i < list2.size(); ++i) {
                if (SlayerNames.wordMatches(list.get(n + i), list2.get(i))) continue;
                bl = false;
                break;
            }
            if (bl) {
                return true;
            }
            ++n;
        }
        return false;
    }

    private static boolean wordMatches(String string, String string2) {
        if (string.equals(string2)) {
            return true;
        }
        Set<String> set = SlayerNames.matchForms(string);
        for (String string3 : SlayerNames.matchForms(string2)) {
            if (!set.contains(string3)) continue;
            return true;
        }
        return false;
    }

    private static Set<String> matchForms(String string) {
        Set<String> set = SlayerNames.singularForms(string);
        HashSet<String> hashSet = new HashSet<String>(set);
        for (String string2 : set) {
            for (Set<String> set2 : SPELLING_VARIANTS) {
                if (!set2.contains(string2)) continue;
                hashSet.addAll(set2);
            }
        }
        return hashSet;
    }

    private static Set<String> singularForms(String string) {
        HashSet<String> hashSet = new HashSet<String>();
        hashSet.add(string);
        int n = string.length();
        if (n > 3 && string.endsWith("men")) {
            hashSet.add(string.substring(0, n - 3) + "man");
        }
        if (n > 4 && string.endsWith("ies")) {
            hashSet.add(string.substring(0, n - 3) + "y");
            hashSet.add(string.substring(0, n - 1));
        }
        if (n > 3 && string.endsWith("es")) {
            hashSet.add(string.substring(0, n - 2));
        }
        if (n > 3 && string.endsWith("s")) {
            hashSet.add(string.substring(0, n - 1));
        }
        return hashSet;
    }

    private static String singular(String string) {
        int n = string.length();
        if (n > 3 && string.endsWith("men")) {
            return string.substring(0, n - 3) + "man";
        }
        if (n > 4 && string.endsWith("ies")) {
            return string.substring(0, n - 1);
        }
        if (n > 3 && string.endsWith("s")) {
            return string.substring(0, n - 1);
        }
        return string;
    }
}


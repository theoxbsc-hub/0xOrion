/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.chatresponder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

final class TypoGenerator {
    private static final Map<Character, char[]> NEIGHBOURS = new HashMap<Character, char[]>();

    private TypoGenerator() {
    }

    private static void put(char c, String string) {
        NEIGHBOURS.put(Character.valueOf(c), string.toCharArray());
    }

    static Result apply(String string, int n, ThreadLocalRandom threadLocalRandom) {
        String string2;
        int n2;
        if (string == null || string.isEmpty() || n <= 0 || threadLocalRandom.nextInt(100) >= n) {
            return new Result(string, null);
        }
        CharSequence[] charSequenceArray = string.split(" ", -1);
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        for (n2 = 0; n2 < charSequenceArray.length; ++n2) {
            string2 = charSequenceArray[n2];
            if (string2.length() < 3 || !TypoGenerator.isMostlyAlpha(string2)) continue;
            arrayList.add(n2);
        }
        if (arrayList.isEmpty()) {
            return new Result(string, null);
        }
        n2 = (Integer)arrayList.get(threadLocalRandom.nextInt(arrayList.size()));
        string2 = charSequenceArray[n2];
        String string3 = TypoGenerator.mangle(string2, threadLocalRandom);
        if (string3.equals(string2)) {
            return new Result(string, null);
        }
        charSequenceArray[n2] = string3;
        return new Result(String.join((CharSequence)" ", charSequenceArray), string2);
    }

    private static String mangle(String string, ThreadLocalRandom threadLocalRandom) {
        switch (threadLocalRandom.nextInt(4)) {
            case 0: {
                return TypoGenerator.substituteNeighbour(string, threadLocalRandom);
            }
            case 1: {
                return TypoGenerator.dropChar(string, threadLocalRandom);
            }
            case 2: {
                return TypoGenerator.doubleChar(string, threadLocalRandom);
            }
        }
        return TypoGenerator.transpose(string, threadLocalRandom);
    }

    private static String substituteNeighbour(String string, ThreadLocalRandom threadLocalRandom) {
        char[] cArray = string.toCharArray();
        for (int i = 0; i < cArray.length; ++i) {
            int n = threadLocalRandom.nextInt(cArray.length);
            char c = Character.toLowerCase(cArray[n]);
            char[] cArray2 = NEIGHBOURS.get(Character.valueOf(c));
            if (cArray2 == null) continue;
            char c2 = cArray2[threadLocalRandom.nextInt(cArray2.length)];
            cArray[n] = Character.isUpperCase(cArray[n]) ? Character.toUpperCase(c2) : c2;
            return new String(cArray);
        }
        return string;
    }

    private static String dropChar(String string, ThreadLocalRandom threadLocalRandom) {
        int n = 1 + threadLocalRandom.nextInt(string.length() - 1);
        return string.substring(0, n) + string.substring(n + 1);
    }

    private static String doubleChar(String string, ThreadLocalRandom threadLocalRandom) {
        int n = threadLocalRandom.nextInt(string.length());
        return string.substring(0, n) + string.charAt(n) + string.substring(n);
    }

    private static String transpose(String string, ThreadLocalRandom threadLocalRandom) {
        if (string.length() < 2) {
            return string;
        }
        int n = threadLocalRandom.nextInt(string.length() - 1);
        char[] cArray = string.toCharArray();
        char c = cArray[n];
        cArray[n] = cArray[n + 1];
        cArray[n + 1] = c;
        return new String(cArray);
    }

    private static boolean isMostlyAlpha(String string) {
        int n = 0;
        for (int i = 0; i < string.length(); ++i) {
            if (!Character.isLetter(string.charAt(i))) continue;
            ++n;
        }
        return n >= string.length() - 1;
    }

    static {
        TypoGenerator.put('q', "was");
        TypoGenerator.put('w', "qeasd");
        TypoGenerator.put('e', "wrsdf");
        TypoGenerator.put('r', "etdfg");
        TypoGenerator.put('t', "ryfgh");
        TypoGenerator.put('y', "tughj");
        TypoGenerator.put('u', "yihjk");
        TypoGenerator.put('i', "uojkl");
        TypoGenerator.put('o', "ipkl");
        TypoGenerator.put('p', "ol");
        TypoGenerator.put('a', "qwsz");
        TypoGenerator.put('s', "qweadzx");
        TypoGenerator.put('d', "wersfxc");
        TypoGenerator.put('f', "ertdgcv");
        TypoGenerator.put('g', "rtyfhvb");
        TypoGenerator.put('h', "tyugjbn");
        TypoGenerator.put('j', "yuihknm");
        TypoGenerator.put('k', "uiojlm");
        TypoGenerator.put('l', "iopk");
        TypoGenerator.put('z', "asx");
        TypoGenerator.put('x', "zsdc");
        TypoGenerator.put('c', "xdfv");
        TypoGenerator.put('v', "cfgb");
        TypoGenerator.put('b', "vghn");
        TypoGenerator.put('n', "bhjm");
        TypoGenerator.put('m', "njk");
    }

    static final class Result {
        final String text;
        final String correctedWord;

        Result(String string, String string2) {
            this.text = string;
            this.correctedWord = string2;
        }
    }
}


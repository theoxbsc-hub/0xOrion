/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.chatresponder;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import net.runelite.client.plugins.svc.chatresponder.ChatResponderConfig;
import net.runelite.client.plugins.svc.chatresponder.Intent;
import net.runelite.client.plugins.svc.chatresponder.Persona;

class ResponseEngine {
    private static final String[] GREETING = new String[]{"hey", "yo", "hi", "sup", "heya", "yo wsg", "ello", "wsp", "hey hru", "ayy", "heyy"};
    private static final String[] THANKS = new String[]{"np", "no worries", "anytime", "yw", "ofc", "gl", "cheers", "all good", "gg", "ez"};
    private static final String[] FAREWELL = new String[]{"cya", "later", "gn", "take care", "bye", "ttyl", "peace", "see ya", "o/"};
    private static final String[] TRADE = new String[]{"not selling sry", "nah im good", "what u offering", "how much", "ill think about it", "not rn sry", "maybe later", "im not buying atm", "im poor lol"};
    private static final String[] BOT_ACCUSATION = new String[]{"lol no", "what no", "im not a bot lmao", "bro what", "??", "why u askin that", "nah just grinding", "u good?", "lmao no", "no u", "report me then i guess", "im just built different", "huh", "ok weirdo", "and u are?"};
    private static final String[] QUESTION = new String[]{"idk", "not sure tbh", "maybe", "prob", "dunno", "could be", "ask someone else lol", "no idea", "hmm good q", "ya prob"};
    private static final String[] FALLBACK = new String[]{"lol", "ya", "true", "fair", "lmao", "fr", "oh word", "ikr", "yeah", "haha", "mhm", "nice", "bet", "w", "real", "facts", "ngl", "lowkey"};
    private static final String[] PM_OPENER = new String[]{"?", "??", "woah", "huh", "hm?", "wdym", "who dis", "do i know u", "and?", "what", "sup", "yo", "u good?", "wym", "??who", "new acc who dis"};
    private static final String[] PM_GREETING = new String[]{"yo", "hey", "sup", "ayy", "wsg", "heyy", "yo wsg", "hi", "ello", "wsp"};
    private static final String[] PM_THANKS = new String[]{"np", "yw", "ofc", "anytime", "gg", "ez", "all good", "no worries"};
    private static final String[] PM_FAREWELL = new String[]{"cya", "later", "gn", "peace", "ttyl", "o/", "bye", "take care"};
    private static final String[] PM_TRADE = new String[]{"not selling sry", "wdym how much", "nah im good", "what u offering", "ill think bout it", "not rn", "maybe later", "im poor lol"};
    private static final String[] PM_BOT_ACCUSATION = new String[]{"lol no", "bro what", "??", "im not a bot lmao", "nah just grinding", "u good?", "and u are?", "report me then", "im built different", "huh", "ok weirdo"};
    private static final String[] PM_QUESTION = new String[]{"idk", "not sure tbh", "maybe", "prob", "dunno", "no idea", "ask someone else lol", "hmm", "ya prob", "could be"};
    private static final String[] PM_FALLBACK = new String[]{"lol", "ya", "true", "fr", "lmao", "ikr", "oh word", "mhm", "fair", "haha", "nice", "bet", "w", "real", "facts", "ngl", "lowkey"};
    private static final String[] BOT_WORDS = new String[]{"bot", "botting", "botter", "macro", "macroing", "macroer", "scripted", "scripting", "script", "autoclick", "autoclicker", "autoclicking", "cheat", "cheating", "cheater", "rwt", "afk", "captcha"};
    private static final String[] BOT_PHRASES = new String[]{"are you a bot", "are u a bot", "is this a bot", "ur a bot", "your a bot", "you are a bot", "u a bot", "u r a bot", "you a bot", "ru a bot", "r u a bot", "report you", "report u", "reporting you", "reporting u", "prove your human", "prove ur human", "prove you are human", "afk check", "say something", "you botting", "u botting", "are you botting", "are u botting"};
    private static final String[] SKILLTEST_WORDS = new String[]{"say", "type", "repeat", "spell", "prove"};
    private static final String[] LOCATION_PROBES = new String[]{"what world", "which world", "where are you", "where r u", "where u at", "what wave"};
    private static final String[] GREETING_WORDS = new String[]{"hi", "hey", "hello", "yo", "sup", "wsg", "wsp", "heya", "hru", "wuu2", "wyd", "howdy", "ello", "hiya"};
    private static final String[] THANKS_WORDS = new String[]{"ty", "thanks", "thank", "thx", "tysm", "tyvm", "nice one", "gz", "gratz", "grats", "congrats", "gg", "pog", "ez"};
    private static final String[] FAREWELL_WORDS = new String[]{"bye", "cya", "gtg", "gn", "goodnight", "good night", "logging", "log off", "ttyl", "see ya", "later", "cyaa"};
    private static final String[] TRADE_WORDS = new String[]{"price", "pc", "selling", "buying", "buy", "sell", "worth", "how much", "trade", "offer", "wts", "wtb", "gp for"};
    private static final String[] QUESTION_WORDS = new String[]{"how", "what", "why", "where", "when", "who", "which", "can you", "do you", "are you", "is it"};
    private static final Pattern MATH = Pattern.compile("\\d+\\s*[+\\-*x\u00d7/]\\s*\\d+");
    private static final Pattern HTML = Pattern.compile("<[^>]*>");
    private static final Pattern REPEATS = Pattern.compile("(.)\\1{2,}");
    private static final String[][] ABBREV = new String[][]{{"you", "u"}, {"your", "ur"}, {"youre", "ur"}, {"you're", "ur"}, {"are", "r"}, {"please", "pls"}, {"people", "ppl"}, {"though", "tho"}, {"because", "bc"}, {"okay", "ok"}, {"really", "rly"}, {"to be honest", "tbh"}};
    private String lastResponse = "";

    ResponseEngine() {
    }

    static String normalise(String string) {
        if (string == null) {
            return "";
        }
        String string2 = HTML.matcher(string).replaceAll(" ").toLowerCase().trim();
        string2 = REPEATS.matcher(string2).replaceAll("$1$1");
        return string2.replaceAll("\\s+", " ");
    }

    boolean isInterrogation(String string) {
        if (ResponseEngine.containsPhrase(string, BOT_PHRASES) || ResponseEngine.containsPhrase(string, LOCATION_PROBES)) {
            return true;
        }
        if (ResponseEngine.hasAnyWord(string, BOT_WORDS)) {
            return true;
        }
        if (MATH.matcher(string).find()) {
            return true;
        }
        return ResponseEngine.hasAnyWord(string, SKILLTEST_WORDS) && string.split(" ").length <= 6;
    }

    Intent classify(String string, ChatResponderConfig chatResponderConfig) {
        return this.classify(string, chatResponderConfig.handleInterrogation());
    }

    Intent classify(String string, boolean bl) {
        String string2 = ResponseEngine.normalise(string);
        if (bl && this.isInterrogation(string2)) {
            return Intent.BOT_ACCUSATION;
        }
        if (ResponseEngine.hasAnyWord(string2, GREETING_WORDS)) {
            return Intent.GREETING;
        }
        if (ResponseEngine.hasAnyWord(string2, THANKS_WORDS)) {
            return Intent.THANKS;
        }
        if (ResponseEngine.hasAnyWord(string2, FAREWELL_WORDS) || ResponseEngine.containsPhrase(string2, FAREWELL_WORDS)) {
            return Intent.FAREWELL;
        }
        if (ResponseEngine.containsPhrase(string2, TRADE_WORDS) || ResponseEngine.hasAnyWord(string2, TRADE_WORDS)) {
            return Intent.TRADE;
        }
        if (string.contains("?") || ResponseEngine.startsWithAny(string2, QUESTION_WORDS)) {
            return Intent.QUESTION;
        }
        return Intent.FALLBACK;
    }

    String pickResponse(Intent intent, ChatResponderConfig chatResponderConfig, ThreadLocalRandom threadLocalRandom) {
        return this.pickResponse(ResponseEngine.poolFor(intent, chatResponderConfig), threadLocalRandom);
    }

    String pickResponse(String[] stringArray, ThreadLocalRandom threadLocalRandom) {
        if (stringArray == null || stringArray.length == 0) {
            stringArray = FALLBACK;
        }
        String string = stringArray[threadLocalRandom.nextInt(stringArray.length)];
        for (int i = 0; i < 3 && string.equals(this.lastResponse) && stringArray.length > 1; ++i) {
            string = stringArray[threadLocalRandom.nextInt(stringArray.length)];
        }
        this.lastResponse = string;
        return string;
    }

    String applyPersona(String string, ChatResponderConfig chatResponderConfig, ThreadLocalRandom threadLocalRandom) {
        return this.applyPersona(string, chatResponderConfig.forceLowercase(), chatResponderConfig.useAbbreviations(), chatResponderConfig.persona(), threadLocalRandom);
    }

    String applyPersona(String string, boolean bl, boolean bl2, Persona persona, ThreadLocalRandom threadLocalRandom) {
        Object object = string;
        if (bl) {
            object = ((String)object).toLowerCase();
        }
        if (bl2) {
            for (String[] stringArray : ABBREV) {
                object = ((String)object).replaceAll("(?i)\\b" + Pattern.quote(stringArray[0]) + "\\b", stringArray[1]);
            }
        }
        if (persona == Persona.SLANG && threadLocalRandom.nextInt(100) < 12 && !((String)object).contains("lol") && !((String)object).contains("lmao")) {
            object = (String)object + (threadLocalRandom.nextBoolean() ? " lol" : " lmao");
        }
        return ((String)object).trim();
    }

    private static String[] poolFor(Intent intent, ChatResponderConfig chatResponderConfig) {
        String[] stringArray;
        String string;
        switch (intent) {
            case GREETING: {
                string = chatResponderConfig.greetingResponses();
                stringArray = GREETING;
                break;
            }
            case THANKS: {
                string = chatResponderConfig.thanksResponses();
                stringArray = THANKS;
                break;
            }
            case FAREWELL: {
                string = chatResponderConfig.farewellResponses();
                stringArray = FAREWELL;
                break;
            }
            case TRADE: {
                string = chatResponderConfig.tradeResponses();
                stringArray = TRADE;
                break;
            }
            case BOT_ACCUSATION: {
                string = chatResponderConfig.botAccusationResponses();
                stringArray = BOT_ACCUSATION;
                break;
            }
            case QUESTION: {
                string = chatResponderConfig.questionResponses();
                stringArray = QUESTION;
                break;
            }
            default: {
                string = chatResponderConfig.fallbackResponses();
                stringArray = FALLBACK;
            }
        }
        String[] stringArray2 = ResponseEngine.splitPipe(string);
        return stringArray2.length > 0 ? stringArray2 : stringArray;
    }

    String[] pmPoolFor(Intent intent, ChatResponderConfig chatResponderConfig) {
        String[] stringArray;
        String string;
        switch (intent) {
            case GREETING: {
                string = chatResponderConfig.pmGreetingResponses();
                stringArray = PM_GREETING;
                break;
            }
            case THANKS: {
                string = chatResponderConfig.pmThanksResponses();
                stringArray = PM_THANKS;
                break;
            }
            case FAREWELL: {
                string = chatResponderConfig.pmFarewellResponses();
                stringArray = PM_FAREWELL;
                break;
            }
            case TRADE: {
                string = chatResponderConfig.pmTradeResponses();
                stringArray = PM_TRADE;
                break;
            }
            case BOT_ACCUSATION: {
                string = chatResponderConfig.pmBotAccusationResponses();
                stringArray = PM_BOT_ACCUSATION;
                break;
            }
            case QUESTION: {
                string = chatResponderConfig.pmQuestionResponses();
                stringArray = PM_QUESTION;
                break;
            }
            default: {
                string = chatResponderConfig.pmFallbackResponses();
                stringArray = PM_FALLBACK;
            }
        }
        String[] stringArray2 = ResponseEngine.splitPipe(string);
        return stringArray2.length > 0 ? stringArray2 : stringArray;
    }

    String[] pmOpenerPool(ChatResponderConfig chatResponderConfig) {
        String[] stringArray = ResponseEngine.splitPipe(chatResponderConfig.firstContactResponses());
        return stringArray.length > 0 ? stringArray : PM_OPENER;
    }

    static String[] splitPipe(String string) {
        if (string == null || string.trim().isEmpty()) {
            return new String[0];
        }
        String[] stringArray = string.split("\\|");
        int n = 0;
        for (String string2 : stringArray) {
            if (string2.trim().isEmpty()) continue;
            ++n;
        }
        String[] stringArray2 = new String[n];
        int n2 = 0;
        for (String string3 : stringArray) {
            if (string3.trim().isEmpty()) continue;
            stringArray2[n2++] = string3.trim();
        }
        return stringArray2;
    }

    private static boolean hasAnyWord(String string, String[] stringArray) {
        for (String string2 : stringArray) {
            if (!(string2.indexOf(32) >= 0 ? string.contains(string2) : ResponseEngine.hasWord(string, string2))) continue;
            return true;
        }
        return false;
    }

    private static boolean hasWord(String string, String string2) {
        int n = 0;
        int n2;
        while ((n2 = string.indexOf(string2, n)) >= 0) {
            boolean bl;
            boolean bl2 = n2 == 0 || !Character.isLetterOrDigit(string.charAt(n2 - 1));
            int n3 = n2 + string2.length();
            boolean bl3 = bl = n3 >= string.length() || !Character.isLetterOrDigit(string.charAt(n3));
            if (bl2 && bl) {
                return true;
            }
            n = n2 + 1;
        }
        return false;
    }

    private static boolean containsPhrase(String string, String[] stringArray) {
        for (String string2 : stringArray) {
            if (!string.contains(string2)) continue;
            return true;
        }
        return false;
    }

    private static boolean startsWithAny(String string, String[] stringArray) {
        for (String string2 : stringArray) {
            if (!string.startsWith(string2 + " ") && !string.equals(string2)) continue;
            return true;
        }
        return false;
    }
}


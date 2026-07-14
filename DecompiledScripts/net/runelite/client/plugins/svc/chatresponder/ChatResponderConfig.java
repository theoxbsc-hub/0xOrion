/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 *  net.runelite.client.config.Range
 *  net.runelite.client.config.Units
 */
package net.runelite.client.plugins.svc.chatresponder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.plugins.svc.chatresponder.Persona;
import net.runelite.client.plugins.svc.chatresponder.PmReplyScope;
import net.runelite.client.plugins.svc.chatresponder.ResponseMode;

@ConfigGroup(value="chatResponder")
public interface ChatResponderConfig
extends Config {
    @ConfigSection(name="General", description="Who the bot replies to", position=0, closedByDefault=false)
    public static final String generalSection = "general";
    @ConfigSection(name="Behaviour & Rate", description="How often and how fast it replies", position=1, closedByDefault=false)
    public static final String behaviourSection = "behaviour";
    @ConfigSection(name="Persona & Typos", description="Voice and human typing tells", position=2, closedByDefault=false)
    public static final String personaSection = "persona";
    @ConfigSection(name="Anti-Detection", description="Handling bot traps & interrogation", position=3, closedByDefault=false)
    public static final String antiSection = "anti";
    @ConfigSection(name="Custom Responses", description="Override reply pools. Separate lines with | (pipe). Blank = built-in.", position=4, closedByDefault=true)
    public static final String responsesSection = "responses";
    @ConfigSection(name="Filters", description="Who/what to never reply to", position=5, closedByDefault=true)
    public static final String filtersSection = "filters";
    @ConfigSection(name="Discord Webhook", description="Alerts for detected chat & replies. Posts to the shared admin channel (the same one the Incognito Shield uses) \u2014 no URL to configure.", position=6, closedByDefault=true)
    public static final String webhookSection = "webhook";
    @ConfigSection(name="PM \u2014 General", description="Private-message auto-reply", position=7, closedByDefault=false)
    public static final String pmGeneralSection = "pmGeneral";
    @ConfigSection(name="PM \u2014 Behaviour & Rate", description="How often and how fast it replies to PMs", position=8, closedByDefault=false)
    public static final String pmBehaviourSection = "pmBehaviour";
    @ConfigSection(name="PM \u2014 Persona & Typos", description="Voice and human typing tells for PMs", position=9, closedByDefault=false)
    public static final String pmPersonaSection = "pmPersona";
    @ConfigSection(name="PM \u2014 Anti-Detection", description="Handling bot traps & interrogation in PMs", position=10, closedByDefault=false)
    public static final String pmAntiSection = "pmAnti";
    @ConfigSection(name="PM \u2014 First Contact", description="Lazy opener replies to a player's first PM", position=11, closedByDefault=false)
    public static final String pmOpenerSection = "pmOpener";
    @ConfigSection(name="PM \u2014 Custom Responses", description="Override PM reply pools. Separate lines with | (pipe). Blank = built-in.", position=12, closedByDefault=true)
    public static final String pmResponsesSection = "pmResponses";
    @ConfigSection(name="PM \u2014 Filters", description="Who/what to never reply to in PMs", position=13, closedByDefault=true)
    public static final String pmFiltersSection = "pmFilters";
    @ConfigSection(name="PM \u2014 Discord Webhook", description="Alerts for detected PMs & replies. Posts to the shared admin channel (the same one the Incognito Shield uses) \u2014 no URL to configure.", position=14, closedByDefault=true)
    public static final String pmWebhookSection = "pmWebhook";

    @ConfigItem(keyName="showOverlay", name="Show Status Overlay", section="general", position=-2, description="Show the on-screen status/debug panel (mode, counters, last heard/said). Off by default so nothing draws over the game \u2014 turn on only when you want to watch the bot.")
    default public boolean showOverlay() {
        return false;
    }

    @ConfigItem(keyName="enablePublicReplies", name="Enable Public Replies", section="general", position=-1, description="Master switch for the public-chat responder. Off = ignore public chat (private-message replies, configured separately, are unaffected).")
    default public boolean enablePublicReplies() {
        return true;
    }

    @ConfigItem(keyName="responseMode", name="Respond To", section="general", position=0, description="Name only = reply only when your name is mentioned (to that player). Nearby = reply to players within the radius below. All = reply to any public chat.")
    default public ResponseMode responseMode() {
        return ResponseMode.NEARBY;
    }

    @ConfigItem(keyName="nearbyRadius", name="Nearby Radius", section="general", position=1, description="Tiles. In Nearby mode only reply to senders within this many tiles of you.")
    @Range(min=1, max=15)
    default public int nearbyRadius() {
        return 8;
    }

    @ConfigItem(keyName="prefixSenderName", name="Prefix Sender Name", section="general", position=2, description="Start the reply with the sender's name (e.g. \"Zezima lol\") so it reads as aimed at them.")
    default public boolean prefixSenderName() {
        return false;
    }

    @ConfigItem(keyName="ignoreAutoLoginAccounts", name="Ignore Own Accounts", section="general", position=3, description="Never reply (public OR private) to any player whose name matches one of the account usernames in the Auto Login plugin's fields. Stops your own bot fleet from replying to each other in an endless loop when several run this responder.")
    default public boolean ignoreAutoLoginAccounts() {
        return true;
    }

    @ConfigItem(keyName="responseProbability", name="Reply Chance", section="behaviour", position=0, description="Chance to reply to an eligible message. Lower = more human (humans ignore lots of chat).")
    @Range(min=1, max=100)
    @Units(value="%")
    default public int responseProbability() {
        return 70;
    }

    @ConfigItem(keyName="randomIgnoreChance", name="Random Ignore Chance", section="behaviour", position=1, description="Extra chance to silently skip an otherwise-eligible message, for unpredictability.")
    @Range(min=0, max=100)
    @Units(value="%")
    default public int randomIgnoreChance() {
        return 15;
    }

    @ConfigItem(keyName="minReactionMs", name="Min Reaction (ms)", section="behaviour", position=2, description="Minimum 'noticed the message' delay before typing starts.")
    @Range(min=0, max=15000)
    default public int minReactionMs() {
        return 800;
    }

    @ConfigItem(keyName="maxReactionMs", name="Max Reaction (ms)", section="behaviour", position=3, description="Maximum 'noticed the message' delay before typing starts.")
    @Range(min=0, max=20000)
    default public int maxReactionMs() {
        return 2500;
    }

    @ConfigItem(keyName="perCharTypingMs", name="Per-Char Typing (ms)", section="behaviour", position=4, description="Added delay per character of the reply, simulating typing speed.")
    @Range(min=0, max=400)
    default public int perCharTypingMs() {
        return 70;
    }

    @ConfigItem(keyName="perPlayerCooldownSec", name="Per-Player Cooldown (s)", section="behaviour", position=5, description="Won't reply to the same player again until this many seconds pass. Note: the follow-up window below lets a live conversation bypass this cooldown.")
    @Range(min=0, max=600)
    default public int perPlayerCooldownSec() {
        return 20;
    }

    @ConfigItem(keyName="followUpWindow", name="Follow-up Replies", section="behaviour", position=6, description="After replying to a player, keep replying to their next N messages \u2014 ignoring the per-player cooldown AND the reply-chance dice \u2014 so a back-and-forth conversation actually flows. Once the window is used up, the cooldown applies again. 0 = off (one reply, then cooldown).")
    @Range(min=0, max=10)
    default public int followUpWindow() {
        return 3;
    }

    @ConfigItem(keyName="maxRepliesPerMinute", name="Max Replies / Min", section="behaviour", position=7, description="Global cap on replies per minute. Keeps you from spamming a busy area.")
    @Range(min=1, max=60)
    default public int maxRepliesPerMinute() {
        return 8;
    }

    @ConfigItem(keyName="maxResponseLen", name="Max Reply Length", section="behaviour", position=8, description="Replies longer than this (after persona/typos) are trimmed at a word boundary.")
    @Range(min=10, max=200)
    default public int maxResponseLen() {
        return 80;
    }

    @ConfigItem(keyName="persona", name="Persona", section="persona", position=0, description="Voice used for replies. Slang is the most human in OSRS/RSPS chat.")
    default public Persona persona() {
        return Persona.SLANG;
    }

    @ConfigItem(keyName="typoChance", name="Typo Chance", section="persona", position=1, description="Chance a reply contains a realistic typo (fat-finger, dropped/doubled letter, transpose).")
    @Range(min=0, max=100)
    @Units(value="%")
    default public int typoChance() {
        return 15;
    }

    @ConfigItem(keyName="correctionChance", name="Correction Chance", section="persona", position=2, description="When a typo happens, chance to follow up with a \"*word\" correction (very human).")
    @Range(min=0, max=100)
    @Units(value="%")
    default public int correctionChance() {
        return 30;
    }

    @ConfigItem(keyName="forceLowercase", name="Force Lowercase", section="persona", position=3, description="Send replies in lowercase, like most casual chat.")
    default public boolean forceLowercase() {
        return true;
    }

    @ConfigItem(keyName="useAbbreviations", name="Use Abbreviations", section="persona", position=4, description="Shorten common words (you->u, your->ur, are->r, please->pls ...).")
    default public boolean useAbbreviations() {
        return true;
    }

    @ConfigItem(keyName="handleInterrogation", name="Deflect Bot Traps", section="anti", position=0, description="Detect 'are u a bot', 'u botting?', 'say X', math challenges etc. and reply with a human deflection. The reply is always from a fixed pool \u2014 incoming text is never echoed.")
    default public boolean handleInterrogation() {
        return true;
    }

    @ConfigItem(keyName="answerMathQuestions", name="Answer Math Challenges", section="anti", position=1, description="OFF (recommended): treat 'what's 9+10' as a trap and deflect instead of answering. ON would answer literally, which reads as botty.")
    default public boolean answerMathQuestions() {
        return false;
    }

    @ConfigItem(keyName="goQuietAfterInterrogations", name="Go Quiet After N Probes", section="anti", position=2, description="If one player keeps interrogating you this many times, stop replying to them for a while (a real person gets annoyed and ignores them).")
    @Range(min=1, max=20)
    default public int goQuietAfterInterrogations() {
        return 3;
    }

    @ConfigItem(keyName="quietDurationSec", name="Quiet Duration (s)", section="anti", position=3, description="How long to stay silent toward a player after they trip the probe limit.")
    @Range(min=5, max=1800)
    default public int quietDurationSec() {
        return 90;
    }

    @ConfigItem(keyName="greetingResponses", name="Greetings", section="responses", position=0, description="Replies to hi/hey/yo/sup. Pipe-separated, e.g. hey|yo|sup")
    default public String greetingResponses() {
        return "";
    }

    @ConfigItem(keyName="questionResponses", name="Questions", section="responses", position=1, description="Replies to general questions. Pipe-separated.")
    default public String questionResponses() {
        return "";
    }

    @ConfigItem(keyName="thanksResponses", name="Thanks / Congrats", section="responses", position=2, description="Replies to ty/thanks/gz. Pipe-separated.")
    default public String thanksResponses() {
        return "";
    }

    @ConfigItem(keyName="farewellResponses", name="Farewells", section="responses", position=3, description="Replies to bye/cya/gtg. Pipe-separated.")
    default public String farewellResponses() {
        return "";
    }

    @ConfigItem(keyName="tradeResponses", name="Trade / Price", section="responses", position=4, description="Replies to price checks / buy / sell. Pipe-separated.")
    default public String tradeResponses() {
        return "";
    }

    @ConfigItem(keyName="botAccusationResponses", name="Bot-Trap Deflections", section="responses", position=5, description="Replies to bot accusations & skill-tests. Pipe-separated.")
    default public String botAccusationResponses() {
        return "";
    }

    @ConfigItem(keyName="fallbackResponses", name="Fallback / Filler", section="responses", position=6, description="Catch-all replies when nothing else matches. Pipe-separated.")
    default public String fallbackResponses() {
        return "";
    }

    @ConfigItem(keyName="playerWhitelist", name="Only These Players", section="filters", position=0, description="If set, ONLY reply to these players. Pipe-separated names. Blank = anyone.")
    default public String playerWhitelist() {
        return "";
    }

    @ConfigItem(keyName="playerBlacklist", name="Never These Players", section="filters", position=1, description="Never reply to these players. Pipe-separated names.")
    default public String playerBlacklist() {
        return "";
    }

    @ConfigItem(keyName="wordBlacklist", name="Skip If Contains", section="filters", position=2, description="Skip any message containing one of these words/phrases. Pipe-separated.")
    default public String wordBlacklist() {
        return "";
    }

    @ConfigItem(keyName="webhookEnabled", name="Enable Webhook", section="webhook", position=0, description="Send Discord alerts describing detected chat and the bot's replies to the shared admin channel.")
    default public boolean webhookEnabled() {
        return false;
    }

    @ConfigItem(keyName="richAlerts", name="Screenshot Alerts (PM + Watched)", section="webhook", position=1, description="ALWAYS post a Discord alert \u2014 with a game screenshot AND the recent chatbox \u2014 whenever the bot replies to a private message, OR replies to a player on the Incognito plugin's \"Watched Players\" list. Fires to the shared admin channel independently of the toggles above and the PM webhook section (so you never miss those two cases). Turn off to disable it.")
    default public boolean richAlertsEnabled() {
        return true;
    }

    @ConfigItem(keyName="logResponded", name="Log Replies", section="webhook", position=3, description="Post an alert each time the bot replies (includes the detected message and the reply).")
    default public boolean logResponded() {
        return true;
    }

    @ConfigItem(keyName="logDetected", name="Log Ignored Detections", section="webhook", position=4, description="Also post when an eligible message was detected but NOT replied to (with the reason).")
    default public boolean logDetected() {
        return false;
    }

    @ConfigItem(keyName="minWebhookGapMs", name="Min Gap Between Posts (ms)", section="webhook", position=5, description="Minimum spacing between webhook posts so chat bursts don't hit Discord's rate limit.")
    @Range(min=0, max=10000)
    default public int minWebhookGapMs() {
        return 1500;
    }

    @ConfigItem(keyName="enablePmReplies", name="Enable PM Replies", section="pmGeneral", position=0, description="Master switch for the private-message responder. Off = ignore all PMs.")
    default public boolean enablePmReplies() {
        return false;
    }

    @ConfigItem(keyName="pmReplyScope", name="Reply To", section="pmGeneral", position=1, description="All = reply to any PM (still honouring blacklists and an optional whitelist). Whitelist only = reply only to names on the whitelist below.")
    default public PmReplyScope replyScope() {
        return PmReplyScope.ALL;
    }

    @ConfigItem(keyName="pmPrefixSenderName", name="Prefix Sender Name", section="pmGeneral", position=2, description="Start the reply with the sender's name. Usually OFF for PMs (it's already 1:1).")
    default public boolean pmPrefixSenderName() {
        return false;
    }

    @ConfigItem(keyName="pmResponseProbability", name="Reply Chance", section="pmBehaviour", position=0, description="Chance to reply to an eligible PM. Lower = more human (people don't always answer).")
    @Range(min=1, max=100)
    @Units(value="%")
    default public int pmResponseProbability() {
        return 85;
    }

    @ConfigItem(keyName="pmRandomIgnoreChance", name="Random Ignore Chance", section="pmBehaviour", position=1, description="Extra chance to silently skip an otherwise-eligible PM, for unpredictability.")
    @Range(min=0, max=100)
    @Units(value="%")
    default public int pmRandomIgnoreChance() {
        return 8;
    }

    @ConfigItem(keyName="pmMinReactionMs", name="Min Reaction (ms)", section="pmBehaviour", position=2, description="Minimum 'noticed the PM' delay before typing starts (read time).")
    @Range(min=0, max=15000)
    default public int pmMinReactionMs() {
        return 1200;
    }

    @ConfigItem(keyName="pmMaxReactionMs", name="Max Reaction (ms)", section="pmBehaviour", position=3, description="Maximum 'noticed the PM' delay before typing starts (read time).")
    @Range(min=0, max=20000)
    default public int pmMaxReactionMs() {
        return 4000;
    }

    @ConfigItem(keyName="pmPerCharTypingMs", name="Per-Char Typing (ms)", section="pmBehaviour", position=4, description="Added delay per character of the reply, simulating typing speed.")
    @Range(min=0, max=400)
    default public int pmPerCharTypingMs() {
        return 75;
    }

    @ConfigItem(keyName="pmPerPlayerCooldownSec", name="Per-Player Cooldown (s)", section="pmBehaviour", position=5, description="Won't reply to the same player again until this many seconds pass. The follow-up window below lets a live conversation bypass this.")
    @Range(min=0, max=600)
    default public int pmPerPlayerCooldownSec() {
        return 8;
    }

    @ConfigItem(keyName="pmFollowUpWindow", name="Follow-up Replies", section="pmBehaviour", position=6, description="After replying to a player, keep replying to their next N PMs \u2014 ignoring the per-player cooldown AND the reply-chance dice \u2014 so a conversation flows. 0 = off.")
    @Range(min=0, max=15)
    default public int pmFollowUpWindow() {
        return 6;
    }

    @ConfigItem(keyName="pmMaxRepliesPerMinute", name="Max Replies / Min", section="pmBehaviour", position=7, description="Global cap on PM replies per minute.")
    @Range(min=1, max=60)
    default public int pmMaxRepliesPerMinute() {
        return 10;
    }

    @ConfigItem(keyName="pmMaxResponseLen", name="Max Reply Length", section="pmBehaviour", position=8, description="Replies longer than this (after persona/typos) are trimmed at a word boundary.")
    @Range(min=10, max=200)
    default public int pmMaxResponseLen() {
        return 80;
    }

    @ConfigItem(keyName="pmPersona", name="Persona", section="pmPersona", position=0, description="Voice used for replies. Slang is the most human in OSRS/RSPS chat.")
    default public Persona pmPersona() {
        return Persona.SLANG;
    }

    @ConfigItem(keyName="pmTypoChance", name="Typo Chance", section="pmPersona", position=1, description="Chance a reply contains a realistic typo (fat-finger, dropped/doubled letter, transpose).")
    @Range(min=0, max=100)
    @Units(value="%")
    default public int pmTypoChance() {
        return 15;
    }

    @ConfigItem(keyName="pmCorrectionChance", name="Correction Chance", section="pmPersona", position=2, description="When a typo happens, chance to follow up with a \"*word\" correction (very human).")
    @Range(min=0, max=100)
    @Units(value="%")
    default public int pmCorrectionChance() {
        return 30;
    }

    @ConfigItem(keyName="pmForceLowercase", name="Force Lowercase", section="pmPersona", position=3, description="Send replies in lowercase, like most casual chat.")
    default public boolean pmForceLowercase() {
        return true;
    }

    @ConfigItem(keyName="pmUseAbbreviations", name="Use Abbreviations", section="pmPersona", position=4, description="Shorten common words (you->u, your->ur, are->r, please->pls ...).")
    default public boolean pmUseAbbreviations() {
        return true;
    }

    @ConfigItem(keyName="pmHandleInterrogation", name="Deflect Bot Traps", section="pmAnti", position=0, description="Detect 'are u a bot', 'say X', math challenges etc. and reply with a human deflection. The reply is always from a fixed pool \u2014 incoming text is never echoed.")
    default public boolean pmHandleInterrogation() {
        return true;
    }

    @ConfigItem(keyName="pmGoQuietAfterInterrogations", name="Go Quiet After N Probes", section="pmAnti", position=1, description="If one player keeps interrogating you this many times, stop replying to them for a while.")
    @Range(min=1, max=20)
    default public int pmGoQuietAfterInterrogations() {
        return 3;
    }

    @ConfigItem(keyName="pmQuietDurationSec", name="Quiet Duration (s)", section="pmAnti", position=2, description="How long to stay silent toward a player after they trip the probe limit.")
    @Range(min=5, max=1800)
    default public int pmQuietDurationSec() {
        return 120;
    }

    @ConfigItem(keyName="pmEnableOpeners", name="Use Openers", section="pmOpener", position=0, description="On the very first PM from a player (this session) reply with a short low-effort opener like \"?\", \"woah\" or \"who dis\" instead of a normal reply.")
    default public boolean enableOpeners() {
        return true;
    }

    @ConfigItem(keyName="pmFirstContactResponses", name="Opener Lines", section="pmOpener", position=1, description="Opener pool for a player's first PM. Pipe-separated, e.g. ?|woah|who dis. Blank = built-in.")
    default public String firstContactResponses() {
        return "";
    }

    @ConfigItem(keyName="pmGreetingResponses", name="Greetings", section="pmResponses", position=0, description="Replies to hi/hey/yo/sup. Pipe-separated.")
    default public String pmGreetingResponses() {
        return "";
    }

    @ConfigItem(keyName="pmQuestionResponses", name="Questions", section="pmResponses", position=1, description="Replies to general questions. Pipe-separated.")
    default public String pmQuestionResponses() {
        return "";
    }

    @ConfigItem(keyName="pmThanksResponses", name="Thanks / Congrats", section="pmResponses", position=2, description="Replies to ty/thanks/gz. Pipe-separated.")
    default public String pmThanksResponses() {
        return "";
    }

    @ConfigItem(keyName="pmFarewellResponses", name="Farewells", section="pmResponses", position=3, description="Replies to bye/cya/gtg. Pipe-separated.")
    default public String pmFarewellResponses() {
        return "";
    }

    @ConfigItem(keyName="pmTradeResponses", name="Trade / Price", section="pmResponses", position=4, description="Replies to price checks / buy / sell. Pipe-separated.")
    default public String pmTradeResponses() {
        return "";
    }

    @ConfigItem(keyName="pmBotAccusationResponses", name="Bot-Trap Deflections", section="pmResponses", position=5, description="Replies to bot accusations & skill-tests. Pipe-separated.")
    default public String pmBotAccusationResponses() {
        return "";
    }

    @ConfigItem(keyName="pmFallbackResponses", name="Fallback / Filler", section="pmResponses", position=6, description="Catch-all replies when nothing else matches. Pipe-separated.")
    default public String pmFallbackResponses() {
        return "";
    }

    @ConfigItem(keyName="pmPlayerWhitelist", name="Only These Players", section="pmFilters", position=0, description="In 'Whitelist only' mode, ONLY reply to these names. In 'All' mode, if set, still restricts replies to these names. Pipe-separated. Blank = anyone.")
    default public String pmPlayerWhitelist() {
        return "";
    }

    @ConfigItem(keyName="pmPlayerBlacklist", name="Never These Players", section="pmFilters", position=1, description="Never reply to PMs from these players. Pipe-separated names.")
    default public String pmPlayerBlacklist() {
        return "";
    }

    @ConfigItem(keyName="pmWordBlacklist", name="Skip If Contains", section="pmFilters", position=2, description="Skip any PM containing one of these words/phrases. Pipe-separated.")
    default public String pmWordBlacklist() {
        return "";
    }

    @ConfigItem(keyName="pmWebhookEnabled", name="Enable Webhook", section="pmWebhook", position=0, description="Send Discord alerts describing detected PMs and the bot's replies to the shared admin channel.")
    default public boolean pmWebhookEnabled() {
        return false;
    }

    @ConfigItem(keyName="pmLogResponded", name="Log Replies", section="pmWebhook", position=3, description="Post an alert each time the bot replies to a PM (includes the PM and the reply).")
    default public boolean pmLogResponded() {
        return true;
    }

    @ConfigItem(keyName="pmLogDetected", name="Log Ignored Detections", section="pmWebhook", position=4, description="Also post when an eligible PM was detected but NOT replied to (with the reason).")
    default public boolean pmLogDetected() {
        return false;
    }

    @ConfigItem(keyName="pmMinWebhookGapMs", name="Min Gap Between Posts (ms)", section="pmWebhook", position=5, description="Minimum spacing between webhook posts so PM bursts don't hit Discord's rate limit.")
    @Range(min=0, max=10000)
    default public int pmMinWebhookGapMs() {
        return 1500;
    }
}


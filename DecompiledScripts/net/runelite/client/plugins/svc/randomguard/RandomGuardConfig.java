/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigSection
 *  net.runelite.client.config.Range
 */
package net.runelite.client.plugins.svc.randomguard;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(value="orionRandomGuard")
public interface RandomGuardConfig
extends Config {
    public static final String DETECT = "detect";
    public static final String RESPOND = "respond";
    public static final String DIAG = "diag";
    public static final String ALERT = "alert";
    @ConfigSection(name="Detection", description="What chat lines count as a random event, and how to find the event NPC.", position=0)
    public static final String detectSection = "detect";
    @ConfigSection(name="Response", description="What the guard does the moment it sees a random event.", position=1)
    public static final String respondSection = "respond";
    @ConfigSection(name="Diagnostic", description="The 1-minute recording captured for each random event.", position=2)
    public static final String diagSection = "diag";
    @ConfigSection(name="Alert", description="Discord alert sent to the admin webhook on detection.", position=3)
    public static final String alertSection = "alert";

    @ConfigItem(keyName="triggerPhrases", name="Trigger phrases", description="Comma-separated, case-insensitive substrings. If a server chat line contains ANY of these, the guard treats it as a random event and reacts. Default catches the 'speak to the servant \u2026 complete the random event' anti-bot line.", section="detect", position=1)
    default public String triggerPhrases() {
        return "to complete the random event,speak to the servant,random event";
    }

    @ConfigItem(keyName="eventNpcNames", name="Event NPC name keywords", description="Comma-separated, case-insensitive name substrings used to pick the random-event NPC out of everything nearby (e.g. 'servant'). If none match, the guard falls back to the nearest NPC that has a Talk-to/Dismiss option, then to the nearest NPC.", section="detect", position=2)
    default public String eventNpcNames() {
        return "servant";
    }

    @ConfigItem(keyName="scanRange", name="Scan range (tiles)", description="How far around you to scan for the event NPC and to list nearby NPCs in the report.", section="detect", position=3)
    @Range(min=1, max=25)
    default public int scanRange() {
        return 15;
    }

    @ConfigItem(keyName="reArmSeconds", name="Re-arm cooldown (sec)", description="Minimum gap between two handlings, so the same event's repeated chat spam can't restart the guard while it is still working.", section="detect", position=4)
    @Range(min=5, max=600)
    default public int reArmSeconds() {
        return 45;
    }

    @ConfigItem(keyName="pauseOtherBots", name="Pause other bots", description="While handling the event, raise a flag that makes the AIO Skiller (and any bot that honours it) idle, so it stops repeating the action that an ignored random event makes obvious.", section="respond", position=0)
    default public boolean pauseOtherBots() {
        return true;
    }

    @ConfigItem(keyName="interactWithNpc", name="Talk to the event NPC", description="Best-effort: walk to and interact with the event NPC (e.g. the servant) to try to clear the event. Simple 'talk to complete' randoms resolve; complex ones are still recorded for you.", section="respond", position=1)
    default public boolean interactWithNpc() {
        return true;
    }

    @ConfigItem(keyName="talkAction", name="Preferred NPC option", description="The right-click option to use on the event NPC (e.g. 'Talk-to'). If the NPC doesn't have it, the guard uses the NPC's first available option instead.", section="respond", position=2)
    default public String talkAction() {
        return "Talk-to";
    }

    @ConfigItem(keyName="progressDialogue", name="Auto-progress dialogue", description="While the event dialogue is open, press space to continue and pick the first option, to push through a simple talk-to-complete random event.", section="respond", position=3)
    default public boolean progressDialogue() {
        return true;
    }

    @ConfigItem(keyName="forceRealInput", name="Handle as real mouse input", description="Force the client's interaction pipeline to real mouse events (NaturalMouse move + click) while handling the event, so the servant interaction itself isn't a bare injected packet. Restored to your previous setting when handling finishes.", section="respond", position=4)
    default public boolean forceRealInput() {
        return true;
    }

    @ConfigItem(keyName="runDiagnostic", name="Record a diagnostic", description="On detection, record a focused report to .orion/x64-logs/ \u2014 every nearby NPC, the exact options you can take on each (e.g. the servant), surrounding objects, chat and screenshots.", section="diag", position=0)
    default public boolean runDiagnostic() {
        return true;
    }

    @ConfigItem(keyName="diagnosticSeconds", name="Record for (seconds)", description="How long to keep recording after the random event appears, before stopping and saving the report.", section="diag", position=1)
    @Range(min=15, max=300)
    default public int diagnosticSeconds() {
        return 60;
    }

    @ConfigItem(keyName="captureScreenshots", name="Include screenshots", description="Save periodic screenshots into the report (and one marked-up shot of the event NPC).", section="diag", position=2)
    default public boolean captureScreenshots() {
        return true;
    }

    @ConfigItem(keyName="openReport", name="Open report when done", description="Pop open the report folder when the 1-minute recording finishes, so you can review the random event visually.", section="diag", position=3)
    default public boolean openReport() {
        return true;
    }

    @ConfigItem(keyName="sendWebhook", name="Send Discord alert", description="Post an alert (account, world, event NPC id + options, location, nearby NPCs) with a screenshot to the hardcoded admin Discord channel the instant a random event is detected.", section="alert", position=0)
    default public boolean sendWebhook() {
        return true;
    }

    @ConfigItem(keyName="showOverlay", name="Show overlay", description="Show the on-screen Random Event Guard status panel.", section="alert", position=1)
    default public boolean showOverlay() {
        return true;
    }
}


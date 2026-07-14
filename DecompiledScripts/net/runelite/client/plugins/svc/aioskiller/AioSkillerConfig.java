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
package net.runelite.client.plugins.svc.aioskiller;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.plugins.svc.aioskiller.InputMode;
import net.runelite.client.plugins.svc.aioskiller.OrderMode;
import net.runelite.client.plugins.svc.mining.MiningOre;

@ConfigGroup(value="orionAioSkiller")
public interface AioSkillerConfig
extends Config {
    @ConfigSection(name="Overlay", description="On-screen status panel", position=0)
    public static final String overlaySection = "overlay";
    @ConfigSection(name="AFK all skills", description="Master switch: train every AFK-capable skill via the server ::afk command instead of the normal methods", position=1)
    public static final String afkSection = "afk";
    @ConfigSection(name="Event Watcher", description="Interrupt skilling to fight server world/vote bosses when their spawn is announced", position=2)
    public static final String eventSection = "event";
    @ConfigSection(name="Gear preferences", description="How the bot gears up for Combat training and event-boss fights. Either let it auto-equip the best gear you own ('Equip highest gear I own'), or capture your own fixed loadouts: run the AIO Skiller, equip the gear you want in-game, then tick a 'Capture equipped gear' box \u2014 it snapshots everything you're wearing and unticks itself.", position=3)
    public static final String gearSection = "gear";
    @ConfigSection(name="Order", description="Which order skills are trained in", position=4)
    public static final String orderSection = "order";
    @ConfigSection(name="Breaks", description="Take periodic breaks: bank, log out, stay offline for a spell, then let Auto Login bring the account back and resume skilling", position=5)
    public static final String breaksSection = "breaks";
    @ConfigSection(name="Skills", description="Enable each skill, set its target level, and (where available) choose its AFK method", position=6)
    public static final String skillsSection = "skills";
    @ConfigSection(name="Mining", description="Mining-specific preferences", position=7)
    public static final String miningSection = "mining";
    @ConfigSection(name="Run report", description="What happens when a run ends with problems", position=8)
    public static final String reportSection = "report";

    @ConfigItem(keyName="showOverlay", name="Show overlay", description="Show the AIO Skiller status panel", section="overlay", position=0)
    default public boolean showOverlay() {
        return true;
    }

    @ConfigItem(keyName="inputMode", name="Input mode", description="How the WHOLE AIO Skiller (skilling, banking, walking, AFK, combat) delivers actions. MOUSE MOVEMENTS = the client's real mouse pipeline (genuine NaturalMouse cursor move + click inside each target); the server gets a full mouse-movement stream. PACKETS = a lighter, faster packet delivery that is still NOT movement-less: before each packet the bot dispatches a brisk synthetic cursor approach and stamps the click with a fresh, re-sampled coordinate, so actions always carry mouse telemetry and never paint a one-pixel heatmap. Both are built to be human-indistinguishable at the packet level \u2014 pick the delivery style you prefer. Applies while running; restored on stop.", section="overlay", position=1)
    default public InputMode inputMode() {
        return InputMode.MOUSE_MOVEMENTS;
    }

    @ConfigItem(keyName="afkAllSkills", name="AFK all skills", description="Switch the WHOLE bot to AFK training. Instead of the normal glider + engine methods it types ::afk, clicks the skill in the AFK menu and trains its AFK object non-stop. AFK-capable skills: Combat, Magic, Mining, Smithing, Hunter, Fishing, Ranged, Herblore, Farming \u2014 each still uses its enable toggle and target level from the Skills section. Agility, Thieving, Woodcutting and Crafting have no AFK method and are skipped while this is on. Leave this OFF to choose per-skill (each dual-method skill has its own 'train via AFK' toggle).", section="afk", position=0)
    default public boolean afkAllSkills() {
        return false;
    }

    @ConfigItem(keyName="eventWatcherEnabled", name="Watch for boss events", description="Watch chat for world-boss (::wb) and vote-boss (::vb) spawn announcements. When one fires, pause the current skill, make sure a melee weapon is wielded (banking for the best gear owned if the weapon slot is empty), teleport to the arena with that command and kill the boss, then ::home and resume skilling. On death, no-boss-found or a timeout it simply ::home's and carries on.", section="event", position=0)
    default public boolean eventWatcherEnabled() {
        return true;
    }

    @ConfigItem(keyName="watchVoteBoss", name="  Vote boss (::vb)", description="React to the vote-boss spawn announcement (\"The vote boss has spawned! Use ::vb \u2026\").", section="event", position=1)
    default public boolean watchVoteBoss() {
        return true;
    }

    @ConfigItem(keyName="watchWorldBoss", name="  World boss (::wb)", description="React to the world-boss spawn announcement (\"\u2026 Use ::wb \u2026\").", section="event", position=2)
    default public boolean watchWorldBoss() {
        return true;
    }

    @ConfigItem(keyName="autoBestGear", name="Equip highest gear I own", description="When ON, Combat training and event-boss fights automatically withdraw and equip the BEST melee gear you own that your stats allow \u2014 scanning your worn gear, inventory and bank and picking, per slot, the highest tier you meet the OSRS level requirement to wield (e.g. a banked rune scimitar at 40 Attack, a dragon scimitar at 60, an abyssal whip at 70; matching rune armour at 40 Defence, dragon pieces at 60). Already-wearing something? It only swaps when the bank holds a higher tier. It re-checks and upgrades as you level. Owning nothing better, it keeps the built-in kit. This OVERRIDES the captured loadouts below, and (for Combat) the dragon-scimitar auto-upgrade and 'Use my combat gear'. Ignored by the 'Gemstone crab from lvl 1' and AFK combat methods, which manage their own gear. The gear ids it considers are written to the client log.", section="gear", position=0)
    default public boolean autoBestGear() {
        return false;
    }

    @ConfigItem(keyName="eventGearOverride", name="Use my event-boss gear", description="When ON (and 'Equip highest gear I own' is OFF), the Event Watcher gears up with the loadout captured below instead of the best melee kit it can find. It banks, withdraws and equips exactly these items (pieces already worn are left on) before teleporting to the boss. Capture the loadout first with the box below.", section="gear", position=1)
    default public boolean eventGearOverride() {
        return false;
    }

    @ConfigItem(keyName="captureEventGear", name="  Capture equipped gear \u2192 event boss", description="Tick this (with the AIO Skiller enabled and your gear worn) to snapshot everything you currently have equipped into the event-boss loadout. The box unticks itself once captured.", section="gear", position=2)
    default public boolean captureEventGear() {
        return false;
    }

    @ConfigItem(keyName="eventGearIds", name="  Event-boss item IDs", description="The captured event-boss loadout (comma-separated item IDs). Filled in by the capture box above; you can also edit it by hand.", section="gear", position=3)
    default public String eventGearIds() {
        return "";
    }

    @ConfigItem(keyName="combatGearOverride", name="Use my combat gear", description="When ON (and 'Equip highest gear I own' is OFF), Combat training (the rock-crab method) withdraws and equips the loadout captured below instead of the built-in rock-crab set. The dragon-scimitar auto-upgrade is skipped while this is on (you manage your own weapon). No effect on the 'Gemstone crab from lvl 1' or AFK combat methods. Capture the loadout first with the box below.", section="gear", position=4)
    default public boolean combatGearOverride() {
        return false;
    }

    @ConfigItem(keyName="captureCombatGear", name="  Capture equipped gear \u2192 combat", description="Tick this (with the AIO Skiller enabled and your gear worn) to snapshot everything you currently have equipped into the combat-training loadout. The box unticks itself once captured.", section="gear", position=5)
    default public boolean captureCombatGear() {
        return false;
    }

    @ConfigItem(keyName="combatGearIds", name="  Combat item IDs", description="The captured combat-training loadout (comma-separated item IDs). Filled in by the capture box above; you can also edit it by hand.", section="gear", position=6)
    default public String combatGearIds() {
        return "";
    }

    @ConfigItem(keyName="orderMode", name="Order mode", description="RANDOM picks any enabled, unfinished skill each time. FIXED follows the list below.", section="order", position=0)
    default public OrderMode orderMode() {
        return OrderMode.RANDOM;
    }

    @ConfigItem(keyName="fixedOrder", name="Fixed order", description="Comma-separated skill order, used only when Order mode = FIXED. Keys: combat, magic, mining, smithing, hunter, fishing, ranged, agility, thieving, woodcutting, crafting, herblore, farming. Dependencies are enforced regardless of this list when both run their normal method: Smithing runs after Mining (needs banked ore) and Crafting runs after Combat (needs gems from the gemstone crab). Fishing and Ranged only train via AFK.", section="order", position=1)
    default public String fixedOrder() {
        return "combat,magic,mining,smithing,hunter,agility,thieving,woodcutting,crafting,herblore,farming,fishing,ranged";
    }

    @ConfigItem(keyName="timeLimitEnabled", name="Switch skills on a timer", description="Instead of training each skill all the way to its target before moving on, train each selected skill for a random length of time (rolled between the min/max below), then rotate to a different skill. Skills are never marked done by the timer \u2014 they keep their place in the rotation until they hit their target level. Great for a natural, varied session. Leave OFF to train one skill at a time to completion.", section="order", position=2)
    default public boolean timeLimitEnabled() {
        return false;
    }

    @Range(min=1, max=600)
    @ConfigItem(keyName="timeLimitMinMinutes", name="  Min minutes per skill", description="Lower bound (minutes) of the random per-skill training slice. Used only when 'Switch skills on a timer' is on.", section="order", position=3)
    default public int timeLimitMinMinutes() {
        return 15;
    }

    @Range(min=1, max=600)
    @ConfigItem(keyName="timeLimitMaxMinutes", name="  Max minutes per skill", description="Upper bound (minutes) of the random per-skill training slice. Used only when 'Switch skills on a timer' is on. If set below the min it is treated as equal to the min.", section="order", position=4)
    default public int timeLimitMaxMinutes() {
        return 45;
    }

    @ConfigItem(keyName="breaksEnabled", name="Take breaks", description="Periodically pause skilling for a random break. When a break comes due the bot banks its inventory at home, logs out (when 'Log out on break' is on) and stays offline for a random spell, then resumes from a clean slate. IMPORTANT: for a logged-out account to come back online you must ALSO run the x64 Auto Login plugin with this account's credentials \u2014 a break arms the same 'stay logged out' gate the Incognito Shield uses, and Auto Login logs back in the moment it lifts. Leave OFF for non-stop training.", section="breaks", position=0)
    default public boolean breaksEnabled() {
        return false;
    }

    @Range(min=1, max=1000)
    @ConfigItem(keyName="breakEveryMinMinutes", name="  Break every (min)", description="Lower bound (minutes) of how long to skill between breaks. The actual gap before each break is rolled randomly between this and the max.", section="breaks", position=1)
    default public int breakEveryMinMinutes() {
        return 45;
    }

    @Range(min=1, max=1000)
    @ConfigItem(keyName="breakEveryMaxMinutes", name="  Break every (max)", description="Upper bound (minutes) of the gap between breaks. If set below the min it is treated as equal to the min.", section="breaks", position=2)
    default public int breakEveryMaxMinutes() {
        return 90;
    }

    @Range(min=1, max=600)
    @ConfigItem(keyName="breakForMinMinutes", name="  Break for (min)", description="Lower bound (minutes) of how long each break lasts. The actual break length is rolled randomly between this and the max.", section="breaks", position=3)
    default public int breakForMinMinutes() {
        return 5;
    }

    @Range(min=1, max=600)
    @ConfigItem(keyName="breakForMaxMinutes", name="  Break for (max)", description="Upper bound (minutes) of each break. If set below the min it is treated as equal to the min.", section="breaks", position=4)
    default public int breakForMaxMinutes() {
        return 20;
    }

    @ConfigItem(keyName="breakLogout", name="  Log out on break", description="Log fully out for the duration of each break and let Auto Login relog when it ends (recommended \u2014 an idle, still-logged-in character is an obvious bot tell). Requires the x64 Auto Login plugin to be enabled with this account's credentials. Turn OFF to instead just stand idle in-world for the break without logging out (no Auto Login needed, but the account stays online the whole time).", section="breaks", position=5)
    default public boolean breakLogout() {
        return true;
    }

    @ConfigItem(keyName="combatEnabled", name="Combat", description="Train Attack/Strength/Defence (lowest first, in 20-level intervals)", section="skills", position=0)
    default public boolean combatEnabled() {
        return true;
    }

    @Range(min=1, max=99)
    @ConfigItem(keyName="combatTarget", name="  Combat target", description="Stop when the LOWEST of Attack/Strength/Defence reaches this level", section="skills", position=1)
    default public int combatTarget() {
        return 60;
    }

    @ConfigItem(keyName="combatAfk", name="  Combat \u2014 train via AFK", description="Train Combat at the ::afk melee dummy instead of on crabs. Either way the bot trains the LOWEST of Attack/Strength/Defence first and commits a 20-level band to it before rotating to the next lowest, up to the target. Overridden by the master 'AFK all skills' switch.", section="skills", position=2)
    default public boolean combatAfk() {
        return false;
    }

    @ConfigItem(keyName="gemstoneFromLevel1", name="  Gemstone crab from lvl 1", description="Train Combat at the gemstone crab from level 1 instead of rock crabs. First secures the kit: withdraws the level-1 Recruit sword from the bank, and buys the Rune scimitar, Dragon scimitar and rune armour (helm/platebody/platelegs, Climbing boots) at the warrior shop, grinding Thieving for the gp first if short. Teleports in, crawls the cave to a live crab, attacks it, and crawls to the next chamber when it dies. Trains the shared-XP Lunge (Controlled) style so Attack/Strength/Defence level together. Wields the Recruit sword until 40 Attack, the rune scimitar at 40 Attack and the dragon scimitar at 60 Attack, and dons the rune armour at 40 Defence. Ignored when Combat trains via AFK.", section="skills", position=3)
    default public boolean gemstoneFromLevel1() {
        return false;
    }

    @ConfigItem(keyName="magicEnabled", name="Magic", description="Train Magic (auto-cast staff at rock crabs)", section="skills", position=5)
    default public boolean magicEnabled() {
        return false;
    }

    @Range(min=1, max=99)
    @ConfigItem(keyName="magicTarget", name="  Magic target", description="Stop training Magic at this level", section="skills", position=6)
    default public int magicTarget() {
        return 60;
    }

    @ConfigItem(keyName="magicAfk", name="  Magic \u2014 train via AFK", description="Train Magic at the ::afk magic dummy instead of rock crabs. Overridden by the master 'AFK all skills' switch.", section="skills", position=7)
    default public boolean magicAfk() {
        return false;
    }

    @ConfigItem(keyName="miningEnabled", name="Mining", description="Train Mining", section="skills", position=8)
    default public boolean miningEnabled() {
        return true;
    }

    @Range(min=1, max=99)
    @ConfigItem(keyName="miningTarget", name="  Mining target", description="Stop training Mining at this level", section="skills", position=9)
    default public int miningTarget() {
        return 60;
    }

    @ConfigItem(keyName="miningAfk", name="  Mining \u2014 train via AFK", description="Train Mining at the ::afk gem rock instead of the ore pit. Overridden by the master 'AFK all skills' switch. Note: AFK mining banks no smithing ore, so the Smithing-prep mining only runs when Mining uses the normal method.", section="skills", position=10)
    default public boolean miningAfk() {
        return false;
    }

    @ConfigItem(keyName="smithingEnabled", name="Smithing", description="Train Smithing (uses banked ores)", section="skills", position=11)
    default public boolean smithingEnabled() {
        return true;
    }

    @Range(min=1, max=99)
    @ConfigItem(keyName="smithingTarget", name="  Smithing target", description="Stop training Smithing at this level", section="skills", position=12)
    default public int smithingTarget() {
        return 60;
    }

    @ConfigItem(keyName="smithingAfk", name="  Smithing \u2014 train via AFK", description="Train Smithing at the ::afk furnace instead of the home furnace/anvil. Overridden by the master 'AFK all skills' switch.", section="skills", position=13)
    default public boolean smithingAfk() {
        return false;
    }

    @ConfigItem(keyName="hunterEnabled", name="Hunter", description="Train Hunter", section="skills", position=14)
    default public boolean hunterEnabled() {
        return true;
    }

    @Range(min=1, max=99)
    @ConfigItem(keyName="hunterTarget", name="  Hunter target", description="Stop training Hunter at this level", section="skills", position=15)
    default public int hunterTarget() {
        return 60;
    }

    @ConfigItem(keyName="hunterAfk", name="  Hunter \u2014 train via AFK", description="Train Hunter at the ::afk spot instead of laying traps. Overridden by the master 'AFK all skills' switch.", section="skills", position=16)
    default public boolean hunterAfk() {
        return false;
    }

    @ConfigItem(keyName="fishingEnabled", name="Fishing (AFK)", description="Train Fishing. Fishing has only an AFK method, so it always trains at the ::afk fishing spot.", section="skills", position=17)
    default public boolean fishingEnabled() {
        return true;
    }

    @Range(min=1, max=99)
    @ConfigItem(keyName="fishingTarget", name="  Fishing target", description="Stop training Fishing at this level", section="skills", position=18)
    default public int fishingTarget() {
        return 60;
    }

    @ConfigItem(keyName="rangedEnabled", name="Ranged (AFK)", description="Train Ranged. Ranged has only an AFK method, so it always trains at the ::afk ranged dummy.", section="skills", position=19)
    default public boolean rangedEnabled() {
        return true;
    }

    @Range(min=1, max=99)
    @ConfigItem(keyName="rangedTarget", name="  Ranged target", description="Stop training Ranged at this level", section="skills", position=20)
    default public int rangedTarget() {
        return 60;
    }

    @ConfigItem(keyName="agilityEnabled", name="Agility", description="Train Agility (no AFK method)", section="skills", position=21)
    default public boolean agilityEnabled() {
        return true;
    }

    @Range(min=1, max=99)
    @ConfigItem(keyName="agilityTarget", name="  Agility target", description="Stop training Agility at this level", section="skills", position=22)
    default public int agilityTarget() {
        return 60;
    }

    @ConfigItem(keyName="thievingEnabled", name="Thieving", description="Train Thieving (no AFK method)", section="skills", position=23)
    default public boolean thievingEnabled() {
        return true;
    }

    @Range(min=1, max=99)
    @ConfigItem(keyName="thievingTarget", name="  Thieving target", description="Stop training Thieving at this level", section="skills", position=24)
    default public int thievingTarget() {
        return 60;
    }

    @ConfigItem(keyName="woodcuttingEnabled", name="Woodcutting", description="Train Woodcutting (no AFK method)", section="skills", position=25)
    default public boolean woodcuttingEnabled() {
        return true;
    }

    @Range(min=1, max=99)
    @ConfigItem(keyName="woodcuttingTarget", name="  Woodcutting target", description="Stop training Woodcutting at this level", section="skills", position=26)
    default public int woodcuttingTarget() {
        return 60;
    }

    @ConfigItem(keyName="craftingEnabled", name="Crafting", description="Train Crafting (cuts banked gems; no AFK method)", section="skills", position=27)
    default public boolean craftingEnabled() {
        return true;
    }

    @Range(min=1, max=99)
    @ConfigItem(keyName="craftingTarget", name="  Crafting target", description="Stop training Crafting at this level", section="skills", position=28)
    default public int craftingTarget() {
        return 60;
    }

    @ConfigItem(keyName="herbloreEnabled", name="Herblore (AFK)", description="Train Herblore (AFK at the cauldron via ::afk)", section="skills", position=29)
    default public boolean herbloreEnabled() {
        return true;
    }

    @Range(min=1, max=99)
    @ConfigItem(keyName="herbloreTarget", name="  Herblore target", description="Stop training Herblore at this level", section="skills", position=30)
    default public int herbloreTarget() {
        return 60;
    }

    @ConfigItem(keyName="farmingEnabled", name="Farming (AFK)", description="Train Farming (AFK at the enchanted bush via ::afk)", section="skills", position=31)
    default public boolean farmingEnabled() {
        return true;
    }

    @Range(min=1, max=99)
    @ConfigItem(keyName="farmingTarget", name="  Farming target", description="Stop training Farming at this level", section="skills", position=32)
    default public int farmingTarget() {
        return 60;
    }

    @ConfigItem(keyName="miningBankOre", name="Ore to mine", description="Which ore to power-mine and bank at the ore pit (normal method). ANY = highest-tier rock available. While Smithing still has levels to gain, Mining first banks the smithing-prep ore quotas (extra mithril + coal) before switching to this choice.", section="mining", position=0)
    default public MiningOre miningBankOre() {
        return MiningOre.ANY;
    }

    @ConfigItem(keyName="openReportOnProblem", name="Open report when a run has problems", description="When a run ends after hitting problems (a skipped skill, a stuck loop, an internal error), pop the report open in your text editor. The report is ALWAYS saved to .orion/x64-logs either way \u2014 turn this OFF (default) to stop Notepad windows appearing, especially when running unattended or multi-boxing.", section="report", position=0)
    default public boolean openReportOnProblem() {
        return false;
    }
}


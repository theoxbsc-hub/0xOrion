/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.client.config.Config
 *  net.runelite.client.config.ConfigGroup
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.Range
 */
package net.runelite.client.plugins.svc.diagnostics;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(value="x64diagnostics")
public interface DiagnosticsConfig
extends Config {
    @ConfigItem(keyName="screenshots", name="Include screenshots", description="Save a few screenshots so we can see what was on your screen when the problem happened. Turn off if you'd rather not include images.", position=0)
    default public boolean screenshots() {
        return true;
    }

    @ConfigItem(keyName="verbose", name="Verbose (full detail)", description="Capture extra detail (every game tick + more log messages). Makes a bigger file \u2014 only turn this on if we ask you to.", position=1)
    default public boolean verbose() {
        return false;
    }

    @ConfigItem(keyName="openFolder", name="Open folder when I stop", description="When you turn this plugin OFF, pop open the folder containing your saved report so you can send the .zip to us.", position=2)
    default public boolean openFolder() {
        return true;
    }

    @ConfigItem(keyName="showOverlay", name="Show recording overlay", description="Show the on-screen \"RECORDING\" indicator while the report is being captured.", position=3)
    default public boolean showOverlay() {
        return true;
    }

    @ConfigItem(keyName="captureEnvironment", name="Capture environment (entities/objects)", description="Log the IDs of nearby NPCs (with pet/size flags), ground objects on/around your tile, active graphics and projectiles \u2014 and dump them whenever you take a notable hit. This is how we identify damage sources like lava-floor tiles or tornado NPCs. Recommended ON for bug reports.", position=4)
    default public boolean captureEnvironment() {
        return true;
    }

    @ConfigItem(keyName="damageDumpThreshold", name="Damage dump threshold (HP)", description="When your HP drops by at least this much in one tick, dump the full surroundings (object/graphics/projectile/NPC IDs) so we can pin what hit you. Lower = more captures.", position=5)
    @Range(min=1, max=50)
    default public int damageDumpThreshold() {
        return 5;
    }

    @ConfigItem(keyName="environmentRange", name="Environment capture range (tiles)", description="How far around you to scan for NPCs/graphics when capturing the environment. Ground objects are always scanned on your tile and the 8 adjacent tiles.", position=6)
    @Range(min=3, max=20)
    default public int environmentRange() {
        return 12;
    }

    @ConfigItem(keyName="captureInterfaces", name="Capture interfaces (widgets/items)", description="Log the widget tree of any open interface (group/child IDs, item IDs+names, text, actions) and your inventory's item IDs whenever they change. This is how we read the IDs behind menus like the furnace 'Smelt-bars' / anvil 'Smith' interfaces. Recommended ON when we ask you to capture a menu.", position=7)
    default public boolean captureInterfaces() {
        return true;
    }

    @ConfigItem(keyName="captureClicks", name="Capture clicks (menu actions)", description="Log every menu option you click \u2014 the exact action ('Climb-up', 'Jump-across', 'Crawl-through'), the object/NPC/item ID it was on, and the world tile. This is how we read the precise obstacle order for an agility course: turn this ON, walk a full lap by hand (with the bot OFF), then send the report. Recommended ON for any 'wrong order / wrong object' bug.", position=8)
    default public boolean captureClicks() {
        return true;
    }
}


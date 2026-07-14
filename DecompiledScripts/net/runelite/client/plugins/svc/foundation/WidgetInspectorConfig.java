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
package net.runelite.client.plugins.svc.foundation;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(value="orionWidgetInspector")
public interface WidgetInspectorConfig
extends Config {
    @ConfigSection(name="Inspection Target", description="Which interface to drill into", position=0, closedByDefault=false)
    public static final String targetSection = "target";

    @ConfigItem(keyName="inspectId", name="Inspect Interface ID", description="Set this to any ID shown in 'Open IDs' to see all its child widgets. -1 = off.", section="target", position=0)
    default public int inspectId() {
        return -1;
    }

    @ConfigItem(keyName="showHidden", name="Show Hidden Children", description="Include hidden child widgets in the detail view", section="target", position=1)
    default public boolean showHidden() {
        return false;
    }

    @ConfigItem(keyName="maxChildren", name="Max Children Shown", description="Limit how many children are listed per interface (keep low to avoid overflow)", section="target", position=2)
    @Range(min=5, max=80)
    default public int maxChildren() {
        return 30;
    }

    @ConfigItem(keyName="maxScanId", name="Max Interface ID to Scan", description="Upper bound when scanning for open interfaces (600 covers all standard OSRS interfaces)", section="target", position=3)
    @Range(min=50, max=1000)
    default public int maxScanId() {
        return 600;
    }

    @ConfigItem(keyName="textOnly", name="Text Widgets Only (Detail View)", description="Only show child widgets that have text \u2014 makes it easy to find teleport buttons and menu entries without wading through blank slots", section="target", position=4)
    default public boolean textOnly() {
        return false;
    }
}


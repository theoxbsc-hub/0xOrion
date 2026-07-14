/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Prayer
 *  net.runelite.api.Skill
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 */
package net.unethicalite.api.widgets;

import java.util.Arrays;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.widgets.Widgets;

public class Prayers {
    public static boolean isEnabled(Prayer prayer) {
        return Vars.getBit(prayer.getVarbit()) == 1;
    }

    public static void toggle(Prayer prayer) {
        Widget widget = Widgets.get(prayer.getWidgetInfo());
        if (widget != null) {
            widget.interact(0);
        }
    }

    public static int getPoints() {
        return Skills.getBoostedLevel(Skill.PRAYER);
    }

    public static void toggleQuickPrayer(boolean enabled) {
        Widget widget = Widgets.get(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);
        if (widget != null) {
            widget.interact(enabled ? "Activate" : "Deactivate");
        }
    }

    public static boolean isQuickPrayerEnabled() {
        return Vars.getBit(4103) == 1;
    }

    public static boolean anyActive() {
        return Arrays.stream(Prayer.values()).anyMatch(Prayers::isEnabled);
    }

    public static void disableAll() {
        Arrays.stream(Prayer.values()).filter(Prayers::isEnabled).forEach(Prayers::toggle);
    }
}


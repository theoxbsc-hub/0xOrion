/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.GameState
 */
package net.unethicalite.api.widgets;

import java.util.Arrays;
import net.runelite.api.GameState;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.client.Static;

public class Tabs {
    public static void open(Tab tab) {
        if (Static.getClient() == null || Game.getState() != GameState.LOGGED_IN) {
            return;
        }
        GameThread.invoke(() -> Static.getClient().runScript(new Object[]{915, tab.getIndex()}));
    }

    @Deprecated
    public static void openInterface(Tab tab) {
        Tabs.open(tab);
    }

    public static boolean isOpen(Tab tab) {
        return Vars.getVarcInt(171) == Arrays.asList(Tab.values()).indexOf((Object)tab);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.MenuAction
 *  net.runelite.api.widgets.Widget
 */
package net.unethicalite.api.script.blocking_events;

import net.runelite.api.MenuAction;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.script.blocking_events.BlockingEvent;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

public class WelcomeScreenEvent
extends BlockingEvent {
    @Override
    public boolean validate() {
        return WelcomeScreenEvent.isWelcomeScreenOpen();
    }

    @Override
    public int loop() {
        Widget playButton = WelcomeScreenEvent.getPlayButton();
        if (playButton != null) {
            Static.getClient().interact(1, MenuAction.CC_OP.getId(), -1, playButton.getId());
        }
        return -1;
    }

    public static Widget getPlayButton() {
        Widget nu = Widgets.get(413, x -> x.hasAction(new String[]{"Play"}));
        if (Widgets.isVisible(nu)) {
            return nu;
        }
        Widget old = Widgets.get(378, x -> x.hasAction(new String[]{"Play"}));
        if (Widgets.isVisible(old)) {
            return old;
        }
        return null;
    }

    public static boolean isWelcomeScreenOpen() {
        return Widgets.isVisible(Widgets.get(378, x -> x.hasAction(new String[]{"Play"}))) || Widgets.isVisible(Widgets.get(413, x -> x.hasAction(new String[]{"Play"})));
    }
}


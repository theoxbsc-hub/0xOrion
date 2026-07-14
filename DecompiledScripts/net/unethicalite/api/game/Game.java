/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.GameState
 *  net.runelite.api.Player
 *  net.runelite.api.coords.LocalPoint
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.api.packets.ClientPacket
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 */
package net.unethicalite.api.game;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.packets.ClientPacket;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.account.GameAccount;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

public class Game {
    private static final int MEMBER_DAYS_VARP = 1780;
    private static final int CUTSCENE_VARBIT = 542;
    private static final String LOGOUT_ACTION = "Logout";
    private static GameAccount gameAccount = null;

    @Deprecated
    public static ClientPacket getClientPacket() {
        return Static.getClientPacket();
    }

    public static boolean isLoggedIn() {
        return Game.getState() == GameState.LOGGED_IN || Game.getState() == GameState.LOADING;
    }

    public static boolean isOnLoginScreen() {
        return Game.getState() == GameState.LOGIN_SCREEN || Game.getState() == GameState.LOGIN_SCREEN_AUTHENTICATOR || Game.getState() == GameState.LOGGING_IN;
    }

    public static GameState getState() {
        return Static.getClient().getGameState();
    }

    public static int getWildyLevel() {
        Widget wildyLevelWidget = Widgets.get(WidgetInfo.PVP_WILDERNESS_LEVEL);
        if (!Widgets.isVisible(wildyLevelWidget)) {
            return 0;
        }
        if (wildyLevelWidget.getText().contains("Guarded") || wildyLevelWidget.getText().contains("Protection")) {
            return 0;
        }
        if (wildyLevelWidget.getText().contains("Deadman")) {
            return Integer.MAX_VALUE;
        }
        String widgetText = wildyLevelWidget.getText();
        if (widgetText.equals("")) {
            return 0;
        }
        if (widgetText.equals("Level: --")) {
            Player local = Players.getLocal();
            int y = WorldPoint.fromLocal((Client)Static.getClient(), (LocalPoint)local.getLocalLocation()).getY();
            return 2 + (y - 3528) / 8;
        }
        String levelText = widgetText.contains("<br>") ? widgetText.substring(0, widgetText.indexOf("<br>")) : widgetText;
        return Integer.parseInt(levelText.replace("Level: ", ""));
    }

    public static int getMembershipDays() {
        return Vars.getVarp(1780);
    }

    public static boolean isInCutscene() {
        return Vars.getBit(542) > 0;
    }

    public static void logout() {
        Widget logOutHopper = Widgets.get(69, x -> x.hasAction(new String[]{LOGOUT_ACTION}));
        if (logOutHopper != null) {
            logOutHopper.interact(LOGOUT_ACTION);
            return;
        }
        Widget logOut = Widgets.get(182, x -> x.hasAction(new String[]{LOGOUT_ACTION}));
        if (logOut != null) {
            logOut.interact(LOGOUT_ACTION);
            return;
        }
        if (!Tabs.isOpen(Tab.LOG_OUT)) {
            Tabs.open(Tab.LOG_OUT);
        }
    }

    public static GameAccount getGameAccount() {
        return gameAccount;
    }

    public static void setGameAccount(GameAccount gameAccount) {
        Game.gameAccount = gameAccount;
    }
}


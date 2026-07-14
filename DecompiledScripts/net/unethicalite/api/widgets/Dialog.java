/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.DialogOption
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 */
package net.unethicalite.api.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.runelite.api.DialogOption;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.GrandExchange;
import net.unethicalite.api.packets.DialogPackets;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

public class Dialog {
    private static final Supplier<Widget> SPRITE_CONT = () -> Widgets.get(193, 0);
    private static final Supplier<Widget> SCROLL_BAR = () -> Widgets.get(162, 557);
    private static final Supplier<Widget> WEIRD_CONT = () -> Widgets.get(193, 3);
    private static final Supplier<Widget> WEIRD_CONT_2 = () -> Widgets.get(633, 0);
    private static final Supplier<Widget> NPC_CONT = () -> Widgets.get(231, 4);
    private static final Supplier<Widget> NPC_TEXT = () -> Widgets.get(231, 6);
    private static final Supplier<Widget> PLAYER_CONT = () -> Widgets.get(217, 3);
    private static final Supplier<Widget> PLAYER_NAME = () -> Widgets.get(217, 4);
    private static final Supplier<Widget> PLAYER_TEXT = () -> Widgets.get(217, 6);
    private static final Supplier<Widget> DEATH_CONT = () -> Widgets.get(663, 0);
    private static final Supplier<Widget> TUT_CONT = () -> Widgets.get(229, 2);
    private static final Supplier<Widget> OPTIONS = () -> Widgets.get(219, 1);

    public static void continueTutorial() {
        GameThread.invoke(() -> Static.getClient().runScript(new Object[]{299, 1, 1, 1}));
    }

    public static boolean isOpen() {
        return SCROLL_BAR.get() == null || !SCROLL_BAR.get().isVisible();
    }

    public static boolean canContinue() {
        return Dialog.canContinueNPC() || Dialog.canContinuePlayer() || Dialog.canContinueDeath() || Dialog.canSpriteContinue() || Dialog.canSprite2Continue() || Dialog.canContinue1() || Dialog.canContinue2() || Dialog.canContinueTutIsland() || Dialog.canContinueTutIsland2() || Dialog.canContinueTutIsland3() || Dialog.canLevelUpContinue();
    }

    public static boolean canLevelUpContinue() {
        return Widgets.isVisible(Widgets.get(WidgetInfo.LEVEL_UP_LEVEL));
    }

    public static boolean canSpriteContinue() {
        return Widgets.isVisible(SPRITE_CONT.get());
    }

    public static boolean canSprite2Continue() {
        return Widgets.isVisible(Widgets.get(WidgetInfo.DIALOG2_SPRITE_CONTINUE));
    }

    public static boolean canContinue1() {
        return Widgets.isVisible(WEIRD_CONT.get());
    }

    public static boolean canContinue2() {
        return Widgets.isVisible(WEIRD_CONT_2.get());
    }

    public static boolean canContinueNPC() {
        return Widgets.isVisible(NPC_CONT.get());
    }

    public static boolean canContinuePlayer() {
        return Widgets.isVisible(PLAYER_CONT.get());
    }

    public static boolean canContinueDeath() {
        Widget widget = DEATH_CONT.get();
        return widget != null && widget.isVisible() && widget.getChild(2) != null && !widget.getChild(2).isVisible();
    }

    public static boolean canContinueTutIsland() {
        return Widgets.isVisible(TUT_CONT.get());
    }

    public static boolean canContinueTutIsland2() {
        Widget widget = Widgets.get(WidgetInfo.DIALOG_SPRITE);
        return widget != null && widget.isVisible() && widget.getChild(2) != null && widget.getChild(2).isVisible();
    }

    public static boolean canContinueTutIsland3() {
        Widget widget = Widgets.get(WidgetInfo.CHATBOX_FULL_INPUT);
        return widget != null && widget.isVisible() && widget.getText().toLowerCase().contains("continue");
    }

    public static boolean isEnterInputOpen() {
        return Widgets.isVisible(Widgets.get(WidgetInfo.CHATBOX_FULL_INPUT)) && !GrandExchange.isSearchingItem();
    }

    public static void enterName(String input) {
        GameThread.invoke(() -> DialogPackets.sendNameInput(input));
    }

    public static void enterText(String input) {
        GameThread.invoke(() -> DialogPackets.sendTextInput(input));
    }

    public static void enterAmount(int input) {
        GameThread.invoke(() -> DialogPackets.sendNumberInput(input));
    }

    public static boolean isViewingOptions() {
        return !Dialog.getOptions().isEmpty();
    }

    public static void continueSpace() {
        if (Dialog.isOpen()) {
            Keyboard.sendSpace();
        }
    }

    public static boolean chooseOption(int index) {
        if (Dialog.isViewingOptions()) {
            Keyboard.type(index);
            return true;
        }
        return false;
    }

    public static boolean chooseOption(String ... options) {
        if (Dialog.isViewingOptions()) {
            for (int i = 0; i < Dialog.getOptions().size(); ++i) {
                Widget widget = Dialog.getOptions().get(i);
                for (String option : options) {
                    if (!widget.getText().contains(option)) continue;
                    return Dialog.chooseOption(i + 1);
                }
            }
        }
        return false;
    }

    public static List<Widget> getOptions() {
        Widget widget = OPTIONS.get();
        if (!Widgets.isVisible(widget)) {
            return Collections.emptyList();
        }
        ArrayList<Widget> out = new ArrayList<Widget>();
        Widget[] children = widget.getChildren();
        if (children == null) {
            return out;
        }
        for (int i = 1; i < children.length; ++i) {
            if (children[i].getText().isBlank()) continue;
            out.add(children[i]);
        }
        return out;
    }

    public static boolean hasOption(String option) {
        return Dialog.hasOption(Predicates.texts(option));
    }

    public static boolean hasOption(Predicate<String> option) {
        return Dialog.getOptions().stream().map(Widget::getText).filter(Objects::nonNull).anyMatch(option);
    }

    public static void invokeDialog(DialogOption ... dialogOption) {
        GameThread.invokeLater(() -> {
            for (DialogOption option : dialogOption) {
                Static.getClient().processDialog(option.getWidgetUid(), option.getMenuIndex());
            }
            return true;
        });
    }

    public static void invokeDialog(int widgetId, int menuIndex) {
        GameThread.invokeLater(() -> {
            Static.getClient().processDialog(widgetId, menuIndex);
            return true;
        });
    }

    public static void close() {
        GameThread.invoke(() -> Static.getClient().runScript(new Object[]{138}));
    }

    public static String getText() {
        Widget widget = null;
        if (Dialog.canContinueNPC()) {
            widget = NPC_TEXT.get();
        } else if (Dialog.canContinuePlayer()) {
            widget = PLAYER_TEXT.get();
        }
        return widget == null ? "" : widget.getText();
    }

    public static String getName() {
        Widget widget = null;
        if (Dialog.canContinueNPC()) {
            widget = NPC_CONT.get();
        } else if (Dialog.canContinuePlayer()) {
            widget = PLAYER_NAME.get();
        }
        return widget == null ? "" : widget.getText();
    }
}


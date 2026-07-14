/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.VarPlayer
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 */
package net.unethicalite.api.game;

import java.util.function.Supplier;
import net.runelite.api.VarPlayer;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;

public class GameSettings {

    public static enum Audio {
        MUSIC(() -> Widgets.get(116, 41), VarPlayer.MUSIC_VOLUME.getId()),
        EFFECTS(() -> Widgets.get(116, 55), VarPlayer.SOUND_EFFECT_VOLUME.getId()),
        AREA(() -> Widgets.get(116, 69), VarPlayer.AREA_EFFECT_VOLUME.getId());

        private final Supplier<Widget> widgetSupplier;
        private final int levelVarp;

        private Audio(Supplier<Widget> widgetSupplier, int levelVarp) {
            this.widgetSupplier = widgetSupplier;
            this.levelVarp = levelVarp;
        }

        public Supplier<Widget> getWidgetSupplier() {
            return this.widgetSupplier;
        }

        public int getLevel() {
            return Vars.getVarp(this.getLevelVarp());
        }

        public void setVolume(int level) {
            if (this.getLevel() != level) {
                Widget widget;
                if (!Tabs.isOpen(Tab.OPTIONS)) {
                    Tabs.open(Tab.OPTIONS);
                    Time.sleepUntil(() -> Tabs.isOpen(Tab.OPTIONS), 2000);
                }
                if ((widget = this.widgetSupplier.get()) != null && widget.getChild(level) != null) {
                    widget.getChild(level).interact(0);
                }
            }
        }

        public static boolean isFullMuted() {
            return MUSIC.getLevel() == 0 && AREA.getLevel() == 0 && EFFECTS.getLevel() == 0;
        }

        public static void muteAll() {
            for (Audio audio : Audio.values()) {
                audio.setVolume(0);
            }
        }

        public int getLevelVarp() {
            return this.levelVarp;
        }
    }

    public static enum Display {
        FIXED(() -> Widgets.get(WidgetInfo.FIXED_VIEWPORT)),
        RESIZABLE_MODERN(() -> Widgets.get(WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_MAGIC_TAB)),
        RESIZABLE_CLASSIC(() -> Widgets.get(WidgetInfo.RESIZABLE_VIEWPORT_INTERFACE_CONTAINER));

        private final Supplier<Widget> widgetSupplier;

        private Display(Supplier<Widget> widgetSupplier) {
            this.widgetSupplier = widgetSupplier;
        }

        public Supplier<Widget> getWidgetSupplier() {
            return this.widgetSupplier;
        }

        public static void setMode(Display displayMode) {
            if (!Tabs.isOpen(Tab.OPTIONS)) {
                Tabs.open(Tab.OPTIONS);
                Time.sleepUntil(() -> Tabs.isOpen(Tab.OPTIONS), 2000);
            }
            switch (displayMode) {
                case FIXED: {
                    GameThread.invoke(() -> Static.getClient().runScript(new Object[]{3998, 0}));
                    break;
                }
                case RESIZABLE_MODERN: {
                    GameThread.invoke(() -> Static.getClient().runScript(new Object[]{3998, 1}));
                    break;
                }
                case RESIZABLE_CLASSIC: {
                    GameThread.invoke(() -> {
                        Static.getClient().runScript(new Object[]{441, 7602188, 7602213, 7602207, 7602209, 7602214, 7602215, 0x740000});
                        Static.getClient().interact(1, 57, 2, 7602213);
                    });
                }
            }
        }

        public static Display getCurrentMode() {
            for (Display display : Display.values()) {
                Widget widget = display.getWidgetSupplier().get();
                if (!Widgets.isVisible(widget)) continue;
                return display;
            }
            return null;
        }
    }
}


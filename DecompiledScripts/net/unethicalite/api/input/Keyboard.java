/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.input;

import java.awt.Canvas;
import java.awt.event.KeyEvent;
import net.unethicalite.api.commons.Time;
import net.unethicalite.client.Static;

public class Keyboard {
    public static void pressed(int keyCode) {
        Keyboard.pressed(keyCode, '\uffff');
    }

    public static void pressed(int keyCode, char keyChar) {
        Canvas canvas = Static.getClient().getCanvas();
        long time = System.currentTimeMillis();
        KeyEvent event = new KeyEvent(canvas, 401, time, 0, keyCode, keyChar, 1);
        canvas.dispatchEvent(event);
    }

    public static void typed(int keyCode) {
        Keyboard.typed(keyCode, '\uffff');
    }

    public static void typed(int keyCode, char keyChar) {
        Canvas canvas = Static.getClient().getCanvas();
        long time = System.currentTimeMillis();
        KeyEvent event = new KeyEvent(canvas, 400, time, 0, keyCode, keyChar, 0);
        canvas.dispatchEvent(event);
    }

    public static void released(int keyCode) {
        Keyboard.released(keyCode, '\uffff');
    }

    public static void released(int keyCode, char keyChar) {
        Canvas canvas = Static.getClient().getCanvas();
        long time = System.currentTimeMillis();
        KeyEvent event = new KeyEvent(canvas, 402, time, 0, keyCode, keyChar, 1);
        canvas.dispatchEvent(event);
    }

    public static void type(char c) {
        Canvas canvas = Static.getClient().getCanvas();
        long time = System.currentTimeMillis();
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
        KeyEvent pressed = new KeyEvent(canvas, 401, time, 0, keyCode, c, 1);
        KeyEvent typed = new KeyEvent(canvas, 400, time, 0, 0, c, 0);
        canvas.dispatchEvent(pressed);
        canvas.dispatchEvent(typed);
        Time.sleep(10L);
        KeyEvent released = new KeyEvent(canvas, 402, System.currentTimeMillis(), 0, keyCode, c, 1);
        canvas.dispatchEvent(released);
    }

    public static void type(int number) {
        Keyboard.type(String.valueOf(number));
    }

    public static void type(String text) {
        Keyboard.type(text, false);
    }

    public static void type(String text, boolean sendEnter) {
        char[] chars;
        for (char c : chars = text.toCharArray()) {
            Keyboard.type(c);
        }
        if (sendEnter) {
            Keyboard.sendEnter();
        }
    }

    public static void sendEnter() {
        Keyboard.type('\n');
    }

    public static void sendSpace() {
        Keyboard.type(' ');
    }
}


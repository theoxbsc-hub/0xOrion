/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.MenuAction
 *  net.runelite.api.Point
 */
package net.unethicalite.api;

import java.util.Arrays;
import java.util.function.Predicate;
import net.runelite.api.MenuAction;
import net.runelite.api.Point;
import net.unethicalite.api.SceneEntity;
import net.unethicalite.api.commons.Predicates;
import net.unethicalite.api.events.MenuAutomated;

public interface Interactable {
    public Point getClickPoint();

    public String[] getActions();

    public int getActionOpcode(int var1);

    default public int getActionIndex(String action) {
        return Arrays.asList(this.getActions()).indexOf(action);
    }

    default public boolean hasAction(Predicate<String> filter) {
        String[] raw = this.getActions();
        if (raw == null) {
            return false;
        }
        return Arrays.stream(raw).anyMatch(filter);
    }

    default public boolean hasAction(String ... actions) {
        return this.hasAction(Predicates.texts(actions));
    }

    public MenuAutomated getMenu(int var1);

    public MenuAutomated getMenu(int var1, int var2);

    default public MenuAutomated getMenu(String action) {
        return this.getMenu(this.getActionIndex(action));
    }

    default public MenuAutomated getMenu(int identifier, int opcode, int param0, int param1) {
        return this.getMenu(identifier, opcode, param0, param1, -1);
    }

    default public MenuAutomated getMenu(int identifier, int opcode, int param0, int param1, int itemId) {
        MenuAutomated.MenuAutomatedBuilder builder = MenuAutomated.builder().identifier(identifier).opcode(MenuAction.of((int)opcode)).param0(param0).param1(param1).itemId(itemId);
        if (this instanceof SceneEntity) {
            builder.entity((SceneEntity)this);
        } else {
            Point clickPoint = this.getClickPoint();
            builder.clickX(clickPoint.getX()).clickY(clickPoint.getY());
        }
        return builder.build();
    }

    public void interact(int var1);

    public void interact(int var1, int var2);

    public void interact(int var1, int var2, int var3, int var4);

    default public void interact(Predicate<String> predicate) {
        String[] raw = this.getActions();
        if (raw == null) {
            return;
        }
        for (int i = 0; i < raw.length; ++i) {
            if (!predicate.test(raw[i])) continue;
            this.interact(i);
            return;
        }
    }

    default public void interact(String action) {
        if (this.getActions() == null) {
            return;
        }
        int index = this.getActionIndex(action);
        if (index == -1) {
            return;
        }
        this.interact(index);
    }

    default public void interact(String ... actions) {
        this.interact(Predicates.texts(actions));
    }
}


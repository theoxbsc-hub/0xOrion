/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.MenuAction
 */
package net.unethicalite.api.events;

import net.runelite.api.MenuAction;

public final class MenuActionProcessed {
    private final String option;
    private final String target;
    private final int identifier;
    private final MenuAction opcode;
    private final int param0;
    private final int param1;
    private final int clickX;
    private final int clickY;

    public MenuActionProcessed(String option, String target, int identifier, MenuAction opcode, int param0, int param1, int clickX, int clickY) {
        this.option = option;
        this.target = target;
        this.identifier = identifier;
        this.opcode = opcode;
        this.param0 = param0;
        this.param1 = param1;
        this.clickX = clickX;
        this.clickY = clickY;
    }

    public String getOption() {
        return this.option;
    }

    public String getTarget() {
        return this.target;
    }

    public int getIdentifier() {
        return this.identifier;
    }

    public MenuAction getOpcode() {
        return this.opcode;
    }

    public int getParam0() {
        return this.param0;
    }

    public int getParam1() {
        return this.param1;
    }

    public int getClickX() {
        return this.clickX;
    }

    public int getClickY() {
        return this.clickY;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MenuActionProcessed)) {
            return false;
        }
        MenuActionProcessed other = (MenuActionProcessed)o;
        if (this.getIdentifier() != other.getIdentifier()) {
            return false;
        }
        if (this.getParam0() != other.getParam0()) {
            return false;
        }
        if (this.getParam1() != other.getParam1()) {
            return false;
        }
        if (this.getClickX() != other.getClickX()) {
            return false;
        }
        if (this.getClickY() != other.getClickY()) {
            return false;
        }
        String this$option = this.getOption();
        String other$option = other.getOption();
        if (this$option == null ? other$option != null : !this$option.equals(other$option)) {
            return false;
        }
        String this$target = this.getTarget();
        String other$target = other.getTarget();
        if (this$target == null ? other$target != null : !this$target.equals(other$target)) {
            return false;
        }
        MenuAction this$opcode = this.getOpcode();
        MenuAction other$opcode = other.getOpcode();
        return !(this$opcode == null ? other$opcode != null : !this$opcode.equals(other$opcode));
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getIdentifier();
        result = result * 59 + this.getParam0();
        result = result * 59 + this.getParam1();
        result = result * 59 + this.getClickX();
        result = result * 59 + this.getClickY();
        String $option = this.getOption();
        result = result * 59 + ($option == null ? 43 : $option.hashCode());
        String $target = this.getTarget();
        result = result * 59 + ($target == null ? 43 : $target.hashCode());
        MenuAction $opcode = this.getOpcode();
        result = result * 59 + ($opcode == null ? 43 : $opcode.hashCode());
        return result;
    }

    public String toString() {
        return "MenuActionProcessed(option=" + this.getOption() + ", target=" + this.getTarget() + ", identifier=" + this.getIdentifier() + ", opcode=" + String.valueOf(this.getOpcode()) + ", param0=" + this.getParam0() + ", param1=" + this.getParam1() + ", clickX=" + this.getClickX() + ", clickY=" + this.getClickY() + ")";
    }
}


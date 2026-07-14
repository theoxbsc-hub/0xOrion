/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.MenuAction
 *  net.runelite.api.MenuEntry
 */
package net.unethicalite.api.events;

import java.util.function.Consumer;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.unethicalite.api.SceneEntity;

public class MenuAutomated {
    private String option;
    private String target;
    private int identifier;
    private MenuAction opcode;
    private int param0;
    private int param1;
    private int clickX;
    private int clickY;
    private int itemId;
    private SceneEntity entity;

    public MenuEntry toEntry(Client client, int idx) {
        return this.toEntry(client, idx, null);
    }

    public MenuEntry toEntry(Client client, int idx, Consumer<MenuEntry> consumer) {
        return this.toEntry(client, idx, this.option, this.target, consumer);
    }

    public MenuEntry toEntry(Client client, int idx, String option, String target) {
        return this.toEntry(client, idx, option, target, null);
    }

    public MenuEntry toEntry(Client client, int idx, String option, String target, Consumer<MenuEntry> consumer) {
        return client.createMenuEntry(idx).setOption(option).setTarget(target).setIdentifier(this.identifier).setType(this.opcode).setParam0(this.param0).setParam1(this.param1).onClick(consumer);
    }

    public MenuEntry toEntry(Client client) {
        return this.toEntry(client, null);
    }

    public MenuEntry toEntry(Client client, Consumer<MenuEntry> consumer) {
        return this.toEntry(client, this.option, this.target, consumer);
    }

    public MenuEntry toEntry(Client client, String option, String target) {
        return this.toEntry(client, option, target, null);
    }

    public MenuEntry toEntry(Client client, String option, String target, Consumer<MenuEntry> consumer) {
        return this.toEntry(client, -1, option, target, consumer);
    }

    public MenuEntry toBareEntry(Client client) {
        return client.createMenuEntry(this.option, this.target, this.identifier, this.opcode.getId(), this.param0, this.param1, this.itemId, false);
    }

    private static String $default$option() {
        return "Automated";
    }

    private static String $default$target() {
        return "";
    }

    private static int $default$clickX() {
        return -1;
    }

    private static int $default$clickY() {
        return -1;
    }

    private static int $default$itemId() {
        return -1;
    }

    MenuAutomated(String option, String target, int identifier, MenuAction opcode, int param0, int param1, int clickX, int clickY, int itemId, SceneEntity entity) {
        this.option = option;
        this.target = target;
        this.identifier = identifier;
        this.opcode = opcode;
        this.param0 = param0;
        this.param1 = param1;
        this.clickX = clickX;
        this.clickY = clickY;
        this.itemId = itemId;
        this.entity = entity;
    }

    public static MenuAutomatedBuilder builder() {
        return new MenuAutomatedBuilder();
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

    public int getItemId() {
        return this.itemId;
    }

    public SceneEntity getEntity() {
        return this.entity;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public void setOpcode(MenuAction opcode) {
        this.opcode = opcode;
    }

    public void setParam0(int param0) {
        this.param0 = param0;
    }

    public void setParam1(int param1) {
        this.param1 = param1;
    }

    public void setClickX(int clickX) {
        this.clickX = clickX;
    }

    public void setClickY(int clickY) {
        this.clickY = clickY;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setEntity(SceneEntity entity) {
        this.entity = entity;
    }

    public static class MenuAutomatedBuilder {
        private boolean option$set;
        private String option$value;
        private boolean target$set;
        private String target$value;
        private int identifier;
        private MenuAction opcode;
        private int param0;
        private int param1;
        private boolean clickX$set;
        private int clickX$value;
        private boolean clickY$set;
        private int clickY$value;
        private boolean itemId$set;
        private int itemId$value;
        private SceneEntity entity;

        MenuAutomatedBuilder() {
        }

        public MenuAutomatedBuilder option(String option) {
            this.option$value = option;
            this.option$set = true;
            return this;
        }

        public MenuAutomatedBuilder target(String target) {
            this.target$value = target;
            this.target$set = true;
            return this;
        }

        public MenuAutomatedBuilder identifier(int identifier) {
            this.identifier = identifier;
            return this;
        }

        public MenuAutomatedBuilder opcode(MenuAction opcode) {
            this.opcode = opcode;
            return this;
        }

        public MenuAutomatedBuilder param0(int param0) {
            this.param0 = param0;
            return this;
        }

        public MenuAutomatedBuilder param1(int param1) {
            this.param1 = param1;
            return this;
        }

        public MenuAutomatedBuilder clickX(int clickX) {
            this.clickX$value = clickX;
            this.clickX$set = true;
            return this;
        }

        public MenuAutomatedBuilder clickY(int clickY) {
            this.clickY$value = clickY;
            this.clickY$set = true;
            return this;
        }

        public MenuAutomatedBuilder itemId(int itemId) {
            this.itemId$value = itemId;
            this.itemId$set = true;
            return this;
        }

        public MenuAutomatedBuilder entity(SceneEntity entity) {
            this.entity = entity;
            return this;
        }

        public MenuAutomated build() {
            String option$value = this.option$value;
            if (!this.option$set) {
                option$value = MenuAutomated.$default$option();
            }
            String target$value = this.target$value;
            if (!this.target$set) {
                target$value = MenuAutomated.$default$target();
            }
            int clickX$value = this.clickX$value;
            if (!this.clickX$set) {
                clickX$value = MenuAutomated.$default$clickX();
            }
            int clickY$value = this.clickY$value;
            if (!this.clickY$set) {
                clickY$value = MenuAutomated.$default$clickY();
            }
            int itemId$value = this.itemId$value;
            if (!this.itemId$set) {
                itemId$value = MenuAutomated.$default$itemId();
            }
            return new MenuAutomated(option$value, target$value, this.identifier, this.opcode, this.param0, this.param1, clickX$value, clickY$value, itemId$value, this.entity);
        }

        public String toString() {
            return "MenuAutomated.MenuAutomatedBuilder(option$value=" + this.option$value + ", target$value=" + this.target$value + ", identifier=" + this.identifier + ", opcode=" + String.valueOf(this.opcode) + ", param0=" + this.param0 + ", param1=" + this.param1 + ", clickX$value=" + this.clickX$value + ", clickY$value=" + this.clickY$value + ", itemId$value=" + this.itemId$value + ", entity=" + String.valueOf(this.entity) + ")";
        }
    }
}


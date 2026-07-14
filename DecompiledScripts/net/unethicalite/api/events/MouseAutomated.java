/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.events;

public class MouseAutomated {
    private int x;
    private int y;
    private int button;
    private EventType eventType;

    MouseAutomated(int x, int y, int button, EventType eventType) {
        this.x = x;
        this.y = y;
        this.button = button;
        this.eventType = eventType;
    }

    public static MouseAutomatedBuilder builder() {
        return new MouseAutomatedBuilder();
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getButton() {
        return this.button;
    }

    public EventType getEventType() {
        return this.eventType;
    }

    public static class MouseAutomatedBuilder {
        private int x;
        private int y;
        private int button;
        private EventType eventType;

        MouseAutomatedBuilder() {
        }

        public MouseAutomatedBuilder x(int x) {
            this.x = x;
            return this;
        }

        public MouseAutomatedBuilder y(int y) {
            this.y = y;
            return this;
        }

        public MouseAutomatedBuilder button(int button) {
            this.button = button;
            return this;
        }

        public MouseAutomatedBuilder eventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public MouseAutomated build() {
            return new MouseAutomated(this.x, this.y, this.button, this.eventType);
        }

        public String toString() {
            return "MouseAutomated.MouseAutomatedBuilder(x=" + this.x + ", y=" + this.y + ", button=" + this.button + ", eventType=" + String.valueOf((Object)this.eventType) + ")";
        }
    }

    public static enum EventType {
        PRESS,
        MOVE,
        RELEASE,
        EXIT;

    }
}


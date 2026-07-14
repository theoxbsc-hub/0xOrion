/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.events;

public final class LobbyWorldSelectToggled {
    private final boolean opened;

    public LobbyWorldSelectToggled(boolean opened) {
        this.opened = opened;
    }

    public boolean isOpened() {
        return this.opened;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof LobbyWorldSelectToggled)) {
            return false;
        }
        LobbyWorldSelectToggled other = (LobbyWorldSelectToggled)o;
        return this.isOpened() == other.isOpened();
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isOpened() ? 79 : 97);
        return result;
    }

    public String toString() {
        return "LobbyWorldSelectToggled(opened=" + this.isOpened() + ")";
    }
}


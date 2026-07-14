/*
 * Decompiled with CFR 0.152.
 */
package net.unethicalite.api.events;

public final class LoginStateChanged {
    private final int index;

    public LoginStateChanged(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof LoginStateChanged)) {
            return false;
        }
        LoginStateChanged other = (LoginStateChanged)o;
        return this.getIndex() == other.getIndex();
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + this.getIndex();
        return result;
    }

    public String toString() {
        return "LoginStateChanged(index=" + this.getIndex() + ")";
    }
}


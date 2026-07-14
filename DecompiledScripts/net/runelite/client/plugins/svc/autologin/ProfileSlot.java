/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.autologin;

public enum ProfileSlot {
    PROFILE_1("Profile 1"),
    PROFILE_2("Profile 2"),
    PROFILE_3("Profile 3"),
    PROFILE_4("Profile 4"),
    PROFILE_5("Profile 5"),
    PROFILE_6("Profile 6"),
    PROFILE_7("Profile 7"),
    PROFILE_8("Profile 8"),
    PROFILE_9("Profile 9"),
    PROFILE_10("Profile 10"),
    PROFILE_11("Profile 11"),
    PROFILE_12("Profile 12"),
    PROFILE_13("Profile 13"),
    PROFILE_14("Profile 14"),
    PROFILE_15("Profile 15"),
    PROFILE_16("Profile 16"),
    PROFILE_17("Profile 17"),
    PROFILE_18("Profile 18"),
    PROFILE_19("Profile 19"),
    PROFILE_20("Profile 20");

    private final String label;

    private ProfileSlot(String string2) {
        this.label = string2;
    }

    public String toString() {
        return this.label;
    }
}


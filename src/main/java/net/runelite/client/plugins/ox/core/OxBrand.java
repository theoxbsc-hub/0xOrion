package net.runelite.client.plugins.ox.core;

/**
 * Shared branding for 0x plugins in the plugin list / overlays.
 */
public final class OxBrand {
    /** HTML purple label matching x64-style coloured plugin titles. */
    public static final String PURPLE = "#B44DFF";

    private OxBrand() {
    }

    public static String pluginName(String name) {
        return "<html><nobr><font color='" + PURPLE + "'>" + name + "</font></nobr></html>";
    }
}

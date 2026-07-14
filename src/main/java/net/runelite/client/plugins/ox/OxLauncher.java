package net.runelite.client.plugins.ox;

import net.runelite.client.RuneLite;
import net.runelite.client.plugins.ox.core.OxConsole;

/**
 * Own launcher — same game client stack as the Orion/x64 jar,
 * without the x64 licence dialog / entitlement gate.
 */
public final class OxLauncher {
    private OxLauncher() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("ox.client", "true");
        if (System.getProperty("runelite.title") == null) {
            System.setProperty("runelite.title", "0xOrion");
        }
        // Quiet agent chatter unless explicitly disabled
        if (System.getProperty("ox.quiet") == null) {
            System.setProperty("ox.quiet", "true");
        }
        OxConsole.banner();
        RuneLite.main(args);
    }
}

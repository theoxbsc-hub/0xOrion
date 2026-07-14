package net.runelite.client.plugins.ox.staffalert;

/**
 * Known Orion/AIO ::afk destination tiles - used when "Ignore at AFK zones" is on.
 */
final class AfkZones {
    /** Default staff watchlist (editable in config). */
    static final String DEFAULT_STAFF =
        "Wizard, Jesse, Lark, Harry, Atom, TinyCow, Dibbes, Chames, Keensoil, Smurfy, "
            + "Threads, Babyboy, TaylorSwift, At, Azazeal, Hold Alt F4, J Beezy";

    /** Centre tiles from x64 AioSkillerConstants AFK_*_DEST (not home - use Ignore at Home). */
    static final int[][] AFK_CENTRES = {
        {2563, 3289}, // Farming
        {2534, 3440}, // Herblore
        {2552, 3333}, // Fishing
        {2937, 3356}, // Hunter
        {2860, 3350}, // Mining
        {2860, 3360}, // Smithing
        {2860, 3370}, // Combat / mage / range dummy
    };

    /** ::home hub (centre + bank + nearby WC / glider). */
    static final int[][] HOME_CENTRES = {
        {2785, 3382}, // Home centre
        {2796, 3391}, // Home bank
        {2795, 3374}, // Gnome glider
        {2754, 3398}, // Local WC spot near home
    };

    static final int DEFAULT_RADIUS = 18;
    static final int DEFAULT_HOME_RADIUS = 25;

    private AfkZones() {
    }
}

/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.herblore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.runelite.client.plugins.svc.herblore.Ingredient;
import net.runelite.client.plugins.svc.herblore.ItemMatcher;
import net.runelite.client.plugins.svc.herblore.Recipe;
import net.runelite.client.plugins.svc.herblore.RecipeKind;

final class HerbloreConstants {
    static final int BANK_OBJECT_ID = 10083;
    static final String BANK_ACTION = "Use";
    static final int MAKE_INTERFACE_WAIT_MS = 900;
    static final int COMBINE_RETRY_MS = 2500;
    static final int COMBINE_SETTLE_MS = 300;
    static final int COMBINE_CONFIRM_MS = 1800;
    static final int SPACE_REPEAT_MS = 250;
    static final int BATCH_ACTIVE_MS = 1400;
    static final int MAX_COMBINE_RETRIES = 4;
    static final int BANK_OPEN_TIMEOUT_MS = 4000;
    static final int CLEAN_STEP_MS = 280;
    static final int STUCK_MS = 4000;
    static final int IDLE_RECHECK_MS = 15000;
    static final int ANTI_BAN_MS = 180;
    static final List<Recipe> FINISHED = new ArrayList<Recipe>();
    static final List<Recipe> UNF = new ArrayList<Recipe>();
    static final List<Recipe> CLEAN = new ArrayList<Recipe>();
    static final List<ItemMatcher> ALL_MATCHERS;

    private HerbloreConstants() {
    }

    private static void fin(String string, int n, double d, String string2, String string3) {
        FINISHED.add(new Recipe(RecipeKind.FINISHED, string, n, d, 1, n, new Ingredient(ItemMatcher.unf(string2), 1, string2 + " potion (unf)"), new Ingredient(ItemMatcher.named(string3), 1, string3)));
    }

    private static Recipe superCombat() {
        return new Recipe(RecipeKind.FINISHED, "Super combat potion", 90, 150.0, 3, 90, new Ingredient(ItemMatcher.named("Super attack"), 1, "Super attack(4)"), new Ingredient(ItemMatcher.named("Super strength"), 1, "Super strength(4)"), new Ingredient(ItemMatcher.named("Super defence"), 1, "Super defence(4)"), new Ingredient(ItemMatcher.clean("Torstol"), 1, "Torstol"));
    }

    private static void unf(String string, int n, String string2) {
        UNF.add(new Recipe(RecipeKind.UNF, string + " potion (unf)", 0, 0.0, 1, n, new Ingredient(ItemMatcher.clean(string), 1, string2), new Ingredient(ItemMatcher.named("Vial of water"), 1, "Vial of water")));
    }

    private static void clean(String string, int n, double d) {
        CLEAN.add(new Recipe(RecipeKind.CLEAN, string, n, d, 0, n, new Ingredient(ItemMatcher.grimy(string), 1, "Grimy " + string.toLowerCase())));
    }

    private static void addMatchers(Map<String, ItemMatcher> map, List<Recipe> list) {
        for (Recipe recipe : list) {
            for (Ingredient ingredient : recipe.inputs) {
                map.putIfAbsent(ingredient.matcher.key(), ingredient.matcher);
            }
        }
    }

    static {
        HerbloreConstants.fin("Attack potion", 3, 25.0, "Guam", "Eye of newt");
        HerbloreConstants.fin("Antipoison", 5, 37.5, "Marrentill", "Unicorn horn dust");
        HerbloreConstants.fin("Strength potion", 12, 50.0, "Tarromin", "Limpwurt root");
        HerbloreConstants.fin("Restore potion", 22, 62.5, "Harralander", "Red spiders' eggs");
        HerbloreConstants.fin("Energy potion", 26, 67.5, "Harralander", "Chocolate dust");
        HerbloreConstants.fin("Defence potion", 30, 75.0, "Ranarr", "White berries");
        HerbloreConstants.fin("Agility potion", 34, 80.0, "Toadflax", "Toad's legs");
        HerbloreConstants.fin("Combat potion", 36, 84.0, "Harralander", "Goat horn dust");
        HerbloreConstants.fin("Prayer potion", 38, 87.5, "Ranarr", "Snape grass");
        HerbloreConstants.fin("Super attack", 45, 100.0, "Irit", "Eye of newt");
        HerbloreConstants.fin("Superantipoison", 48, 106.3, "Irit", "Unicorn horn dust");
        HerbloreConstants.fin("Fishing potion", 50, 112.5, "Avantoe", "Snape grass");
        HerbloreConstants.fin("Super energy", 52, 117.5, "Avantoe", "Mort myre fungus");
        HerbloreConstants.fin("Hunter potion", 53, 120.0, "Avantoe", "Kebbit teeth dust");
        HerbloreConstants.fin("Super strength", 55, 125.0, "Kwuarm", "Limpwurt root");
        HerbloreConstants.fin("Super restore", 63, 142.5, "Snapdragon", "Red spiders' eggs");
        HerbloreConstants.fin("Super defence", 66, 150.0, "Cadantine", "White berries");
        HerbloreConstants.fin("Antifire potion", 69, 157.5, "Lantadyme", "Dragon scale dust");
        HerbloreConstants.fin("Ranging potion", 72, 162.5, "Dwarf weed", "Wine of zamorak");
        HerbloreConstants.fin("Magic potion", 76, 172.5, "Lantadyme", "Potato cactus");
        HerbloreConstants.fin("Saradomin brew", 81, 180.0, "Toadflax", "Crushed nest");
        FINISHED.add(HerbloreConstants.superCombat());
        HerbloreConstants.unf("Toadflax", 81, "Toadflax");
        HerbloreConstants.unf("Lantadyme", 76, "Lantadyme");
        HerbloreConstants.unf("Dwarf weed", 72, "Dwarf weed");
        HerbloreConstants.unf("Cadantine", 66, "Cadantine");
        HerbloreConstants.unf("Snapdragon", 63, "Snapdragon");
        HerbloreConstants.unf("Kwuarm", 55, "Kwuarm");
        HerbloreConstants.unf("Avantoe", 53, "Avantoe");
        HerbloreConstants.unf("Irit", 48, "Irit leaf");
        HerbloreConstants.unf("Ranarr", 38, "Ranarr weed");
        HerbloreConstants.unf("Harralander", 36, "Harralander");
        HerbloreConstants.unf("Tarromin", 12, "Tarromin");
        HerbloreConstants.unf("Marrentill", 5, "Marrentill");
        HerbloreConstants.unf("Guam", 3, "Guam leaf");
        HerbloreConstants.clean("Torstol", 75, 15.0);
        HerbloreConstants.clean("Dwarf weed", 70, 13.8);
        HerbloreConstants.clean("Lantadyme", 67, 13.1);
        HerbloreConstants.clean("Cadantine", 65, 12.5);
        HerbloreConstants.clean("Snapdragon", 59, 11.8);
        HerbloreConstants.clean("Kwuarm", 54, 11.3);
        HerbloreConstants.clean("Avantoe", 48, 10.0);
        HerbloreConstants.clean("Irit", 40, 8.8);
        HerbloreConstants.clean("Toadflax", 30, 8.0);
        HerbloreConstants.clean("Ranarr", 25, 7.5);
        HerbloreConstants.clean("Harralander", 20, 6.3);
        HerbloreConstants.clean("Tarromin", 11, 5.0);
        HerbloreConstants.clean("Marrentill", 5, 3.8);
        HerbloreConstants.clean("Guam", 3, 2.5);
        LinkedHashMap<String, ItemMatcher> linkedHashMap = new LinkedHashMap<String, ItemMatcher>();
        HerbloreConstants.addMatchers(linkedHashMap, FINISHED);
        HerbloreConstants.addMatchers(linkedHashMap, UNF);
        HerbloreConstants.addMatchers(linkedHashMap, CLEAN);
        ALL_MATCHERS = new ArrayList(linkedHashMap.values());
    }
}


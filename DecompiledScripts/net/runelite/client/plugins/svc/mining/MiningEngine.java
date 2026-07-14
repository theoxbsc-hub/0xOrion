/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Item
 *  net.runelite.api.Locatable
 *  net.runelite.api.ObjectComposition
 *  net.runelite.api.Player
 *  net.runelite.api.Skill
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.mining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.Locatable;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.core.HumanInput;
import net.runelite.client.plugins.svc.mining.MiningMode;
import net.runelite.client.plugins.svc.mining.MiningOre;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;

public final class MiningEngine {
    private final Client client;
    private final HumanInput input;
    private MiningState state = MiningState.MINING;
    private String status = "Starting";
    private int tripCount = 0;
    private int startXp = -1;
    private WorldPoint miningAnchor = null;
    private WorldPoint bankAnchor = null;
    private int lastMineTick = -100;
    private int bankClickTick = -100;
    private int stateEntryTick = 0;
    private WorldPoint lastClickedRock = null;
    private final Map<WorldPoint, Integer> depletedUntilTick = new HashMap<WorldPoint, Integer>();
    private static final int DEPLETED_BLACKLIST_TICKS = 6;
    private final Random selectRng = new Random();
    private long reactUntilMs = 0L;

    public MiningEngine(Client client) {
        this(client, null);
    }

    public MiningEngine(Client client, HumanInput humanInput) {
        this.client = client;
        this.input = humanInput;
    }

    public void reset() {
        this.state = MiningState.MINING;
        this.status = "Starting";
        this.tripCount = 0;
        this.startXp = -1;
        this.miningAnchor = null;
        this.bankAnchor = null;
        this.lastMineTick = -100;
        this.bankClickTick = -100;
        this.stateEntryTick = 0;
        this.lastClickedRock = null;
        this.depletedUntilTick.clear();
        this.reactUntilMs = 0L;
    }

    public int tick(Player player, Settings settings) {
        if (this.startXp < 0) {
            this.startXp = Skills.getExperience(Skill.MINING);
        }
        switch (this.state.ordinal()) {
            case 0: {
                return this.loopMining(player, settings);
            }
            case 1: {
                return this.loopWalkToBank(player, settings);
            }
            case 2: {
                return this.loopBanking(settings);
            }
            case 3: {
                return this.loopReturnToMine(player, settings);
            }
            case 4: {
                return this.loopDropping(settings);
            }
        }
        return 600;
    }

    public void onChatMessage(String string) {
        boolean bl;
        if (string == null) {
            return;
        }
        boolean bl2 = bl = string.contains("nothing interesting happens") || string.contains("there is no ore") || string.contains("rock contains no") || string.contains("no ore left") || string.contains("manage to mine all");
        if (bl && this.lastClickedRock != null) {
            this.depletedUntilTick.put(this.lastClickedRock, this.client.getTickCount() + 6);
            this.lastClickedRock = null;
        }
    }

    private int loopMining(Player player, Settings settings) {
        if (settings.bankAndHold) {
            if (this.hasDepositableItems(settings)) {
                this.status = "Goal reached \u2014 banking the last load";
                return this.enter(MiningState.WALK_TO_BANK, 400);
            }
            this.status = "DONE \u2014 bar goal gathered & banked";
            return 2500;
        }
        if (Inventory.getFreeSlots() == 0) {
            int n = this.humanWait();
            if (n > 0) {
                this.status = "Inventory full";
                return n;
            }
            this.clearReaction();
            if (settings.miningMode == MiningMode.DROP) {
                this.status = "Inventory full \u2014 dropping";
                return this.enter(MiningState.DROPPING, 300);
            }
            this.status = "Inventory full \u2014 heading to bank";
            return this.enter(MiningState.WALK_TO_BANK, 300);
        }
        if (player.getAnimation() != -1) {
            this.status = "Mining " + String.valueOf((Object)settings.ore);
            this.clearReaction();
            return 600;
        }
        if (Movement.isWalking()) {
            this.status = "Moving to rock";
            this.clearReaction();
            return 400;
        }
        if (this.client.getTickCount() - this.lastMineTick < 3) {
            return 400;
        }
        TileObject tileObject = this.findMineableRock(player.getWorldLocation(), settings);
        if (tileObject != null) {
            int n = this.humanWait();
            if (n > 0) {
                this.status = "Mining " + String.valueOf((Object)settings.ore);
                return n;
            }
            this.clearReaction();
            this.miningAnchor = player.getWorldLocation();
            this.status = "Mining " + String.valueOf((Object)settings.ore);
            this.lastClickedRock = tileObject.getWorldLocation();
            if (this.input != null) {
                this.input.interactObjectVia(tileObject, "Mine");
            } else {
                tileObject.interact("Mine");
            }
            this.lastMineTick = this.client.getTickCount();
            return Rand.nextInt(900, 1400);
        }
        this.clearReaction();
        if (!settings.ore.isAny() && this.currentMiningLevel() < settings.ore.getTier()) {
            this.status = "Mining " + this.currentMiningLevel() + " too low for " + String.valueOf((Object)settings.ore) + " (needs " + settings.ore.getTier() + ")";
            return 1500;
        }
        this.status = settings.ore.isAny() ? "No live rocks in range" : "No " + String.valueOf((Object)settings.ore) + " rocks in range";
        return 800;
    }

    private int loopWalkToBank(Player player, Settings settings) {
        if (Bank.isOpen()) {
            return this.enter(MiningState.BANKING, 300);
        }
        TileObject tileObject = this.findBankObject();
        if (tileObject != null) {
            this.bankAnchor = tileObject.getWorldLocation();
            int n = tileObject.getWorldLocation().distanceTo(player.getWorldLocation());
            if (n > 5) {
                this.status = "Walking to bank";
                if (this.input != null) {
                    this.input.walkToward(player.getWorldLocation(), tileObject.getWorldLocation());
                } else {
                    Movement.walkTo((Locatable)tileObject);
                }
                return 1200;
            }
            if (this.client.getTickCount() - this.bankClickTick < 5) {
                this.status = "Opening bank...";
                return 400;
            }
            this.status = "Opening bank";
            if (this.input != null) {
                this.input.interactObjectVia(tileObject, this.bankActionFor(tileObject));
            } else {
                tileObject.interact(this.bankActionFor(tileObject));
            }
            this.bankClickTick = this.client.getTickCount();
            return 1500;
        }
        if (this.bankAnchor != null) {
            this.status = "Walking to bank";
            if (this.input != null) {
                this.input.walkToward(player.getWorldLocation(), this.bankAnchor);
            } else {
                Movement.walkTo(this.bankAnchor);
            }
            return 1200;
        }
        this.status = "Bank 'Bank chest' not found";
        return 1000;
    }

    private int loopBanking(Settings settings) {
        if (!Bank.isOpen()) {
            return this.enter(MiningState.WALK_TO_BANK, 600);
        }
        if (Inventory.getFreeSlots() > 0 && !this.hasDepositableItems(settings)) {
            Bank.close();
            ++this.tripCount;
            this.status = "Banked (#" + this.tripCount + ")";
            boolean bl = settings.returnToMine && !settings.bankAndHold;
            return this.enter(bl ? MiningState.RETURN_TO_MINE : MiningState.MINING, 800);
        }
        Bank.depositAll(item -> item != null && !this.isKept(item.getName(), settings));
        this.status = "Depositing";
        return 700;
    }

    private int loopReturnToMine(Player player, Settings settings) {
        if (this.miningAnchor == null) {
            return this.enter(MiningState.MINING, 300);
        }
        WorldPoint worldPoint = player.getWorldLocation();
        if (worldPoint.distanceTo(this.miningAnchor) <= 3 || this.findMineableRock(worldPoint, settings) != null) {
            return this.enter(MiningState.MINING, 300);
        }
        this.status = "Returning to rocks";
        if (this.input != null) {
            this.input.walkToward(player.getWorldLocation(), this.miningAnchor);
        } else {
            Movement.walkTo(this.miningAnchor);
        }
        return 1200;
    }

    private int loopDropping(Settings settings) {
        int n;
        List<Item> list = Inventory.getAll(item -> this.shouldDrop((Item)item, settings));
        if (list.isEmpty()) {
            ++this.tripCount;
            this.status = "Dropped \u2014 resuming";
            this.clearReaction();
            return this.enter(MiningState.MINING, 300);
        }
        this.status = "Dropping (" + list.size() + ")";
        int n2 = this.humanWait();
        if (n2 > 0) {
            return n2;
        }
        this.clearReaction();
        if (this.input != null && list.size() > 2 && this.selectRng.nextDouble() < 0.08) {
            n = this.selectRng.nextInt(list.size() - 1);
            Collections.swap(list, n, n + 1);
        }
        n = this.input == null ? list.size() : Math.min(list.size(), 5 + this.selectRng.nextInt(5));
        double d = this.input == null ? 1.0 : this.input.profile().tempo();
        for (int i = 0; i < n; ++i) {
            int n3;
            Item item2 = list.get(i);
            if (this.input != null) {
                this.input.itemInteractVia(item2, "Drop");
            } else {
                item2.interact("Drop");
            }
            int n4 = n3 = this.input == null ? Rand.nextInt(70, 130) : (int)Math.round((double)(45 + this.selectRng.nextInt(130)) * d);
            if (this.input != null && this.selectRng.nextDouble() < 0.07) {
                n3 += 250 + this.selectRng.nextInt(600);
            }
            try {
                Thread.sleep(n3);
                continue;
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                return 300;
            }
        }
        return this.input == null ? 300 : 140 + this.selectRng.nextInt(180);
    }

    private TileObject findMineableRock(WorldPoint worldPoint, Settings settings) {
        List<Player> list;
        int n;
        String string;
        boolean bl = settings.matchRockByName;
        boolean bl2 = settings.ore.isAny();
        List<TileObject> list2 = TileObjects.getAll(arg_0 -> this.lambda$findMineableRock$0(worldPoint, bl, bl2, string = this.oreRockKeyword(settings), n = this.currentMiningLevel(), list = settings.avoidOtherPlayers ? this.otherPlayers() : null, arg_0));
        if (list2 == null || list2.isEmpty()) {
            return null;
        }
        int n2 = Integer.MIN_VALUE;
        for (TileObject object : list2) {
            int tileObject2 = bl2 && bl ? this.tierOfRockName(this.nameOf(object)) : settings.ore.getTier();
            if (tileObject2 <= n2) continue;
            n2 = tileObject2;
        }
        ArrayList arrayList = new ArrayList();
        for (TileObject tileObject2 : list2) {
            int n3 = bl2 && bl ? this.tierOfRockName(this.nameOf(tileObject2)) : settings.ore.getTier();
            if (n3 != n2) continue;
            arrayList.add(tileObject2);
        }
        arrayList.sort(Comparator.comparingInt(tileObject -> tileObject.getWorldLocation().distanceTo(worldPoint)));
        return this.pickWeighted(arrayList, worldPoint);
    }

    private TileObject pickWeighted(List<TileObject> list, WorldPoint worldPoint) {
        int n;
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() == 1 || this.input == null) {
            return list.get(0);
        }
        int n2 = list.get(0).getWorldLocation().distanceTo(worldPoint);
        for (n = 1; n < list.size() && n < 3 && list.get(n).getWorldLocation().distanceTo(worldPoint) <= n2 + 5; ++n) {
        }
        if (n == 1) {
            return list.get(0);
        }
        double d = 0.55 + 0.3 * MiningEngine.seedUnit(this.input.profile().seed(), 17);
        double d2 = this.selectRng.nextDouble();
        if (d2 < d) {
            return list.get(0);
        }
        if (n == 2 || d2 < d + (1.0 - d) * 0.72) {
            return list.get(1);
        }
        return list.get(2);
    }

    private static double seedUnit(long l, int n) {
        return (double)(l >>> n & 0xFFL) / 255.0;
    }

    private int humanWait() {
        long l;
        if (this.input == null) {
            return 0;
        }
        long l2 = System.currentTimeMillis();
        if (this.reactUntilMs <= 0L) {
            this.reactUntilMs = l2 + (long)this.input.profile().reactionDelay();
        }
        return (l = this.reactUntilMs - l2) > 0L ? (int)Math.max(60L, Math.min(240L, l)) : 0;
    }

    private void clearReaction() {
        this.reactUntilMs = 0L;
    }

    private String nameOf(TileObject tileObject) {
        ObjectComposition objectComposition = this.resolve(tileObject.getId());
        if (objectComposition == null) {
            return "";
        }
        String string = objectComposition.getName();
        return string == null ? "" : string;
    }

    private boolean nameContains(String string, String string2) {
        if (string2 == null || string2.isEmpty()) {
            return false;
        }
        return string != null && string.toLowerCase().contains(string2);
    }

    private String oreRockKeyword(Settings settings) {
        String string = settings.rockNameOverride;
        if (string != null && !string.trim().isEmpty()) {
            return string.trim().toLowerCase();
        }
        return settings.ore.getRockKeyword();
    }

    private boolean isDepletedName(String string) {
        if (string == null) {
            return false;
        }
        String string2 = string.trim().toLowerCase();
        if (string2.isEmpty()) {
            return false;
        }
        if (string2.equals("rocks") || string2.equals("rock")) {
            return true;
        }
        return string2.contains("depleted") || string2.contains("empty");
    }

    private int currentMiningLevel() {
        int n = Skills.getBoostedLevel(Skill.MINING);
        return n > 0 ? n : Skills.getLevel(Skill.MINING);
    }

    private int tierOfRockName(String string) {
        if (string == null || string.isEmpty()) {
            return 0;
        }
        String string2 = string.toLowerCase();
        int n = 0;
        for (MiningOre miningOre : MiningOre.values()) {
            if (miningOre.isAny() || !string2.contains(miningOre.getRockKeyword())) continue;
            n = Math.max(n, miningOre.getTier());
        }
        return n;
    }

    private boolean isBlacklisted(WorldPoint worldPoint) {
        Integer n = this.depletedUntilTick.get(worldPoint);
        if (n == null) {
            return false;
        }
        if (this.client.getTickCount() >= n) {
            this.depletedUntilTick.remove(worldPoint);
            return false;
        }
        return true;
    }

    private List<Player> otherPlayers() {
        Player player = this.client.getLocalPlayer();
        return Players.getAll(player2 -> player2 != null && player2 != player && player2.getWorldLocation() != null);
    }

    private boolean isRockTaken(WorldPoint worldPoint, List<Player> list) {
        for (Player player : list) {
            int n;
            int n2;
            WorldPoint worldPoint2 = player.getWorldLocation();
            if (worldPoint2 == null || worldPoint2.getPlane() != worldPoint.getPlane() || (n2 = Math.abs(worldPoint2.getX() - worldPoint.getX())) + (n = Math.abs(worldPoint2.getY() - worldPoint.getY())) > 1) continue;
            return true;
        }
        return false;
    }

    private TileObject findBankObject() {
        String string = "Bank chest".trim().toLowerCase();
        return TileObjects.getNearest(tileObject -> {
            if (tileObject == null) {
                return false;
            }
            ObjectComposition objectComposition = this.resolve(tileObject.getId());
            if (objectComposition == null) {
                return false;
            }
            String string2 = objectComposition.getName();
            if (string2 == null) {
                return false;
            }
            if (!string.isEmpty()) {
                return string2.toLowerCase().contains(string);
            }
            return this.actionMatches(objectComposition, "Bank") || this.actionMatches(objectComposition, "Use") || this.actionMatches(objectComposition, "Open");
        });
    }

    private String bankActionFor(TileObject tileObject) {
        String string = "".trim();
        if (!string.isEmpty()) {
            return string;
        }
        ObjectComposition objectComposition = this.resolve(tileObject.getId());
        if (objectComposition != null) {
            for (String string2 : new String[]{"Bank", "Use", "Open", "Deposit"}) {
                if (!this.actionMatches(objectComposition, string2)) continue;
                return string2;
            }
        }
        return "Bank";
    }

    private boolean hasAction(int n, String string) {
        return this.actionMatches(this.resolve(n), string);
    }

    private boolean actionMatches(ObjectComposition objectComposition, String string) {
        if (objectComposition == null) {
            return false;
        }
        String[] stringArray = objectComposition.getActions();
        if (stringArray == null) {
            return false;
        }
        for (String string2 : stringArray) {
            if (string2 == null || !string2.equalsIgnoreCase(string)) continue;
            return true;
        }
        return false;
    }

    private ObjectComposition resolve(int n) {
        ObjectComposition objectComposition = this.client.getObjectDefinition(n);
        if (objectComposition == null) {
            return null;
        }
        try {
            ObjectComposition objectComposition2;
            if (objectComposition.getImpostorIds() != null && (objectComposition2 = objectComposition.getImpostor()) != null) {
                return objectComposition2;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return objectComposition;
    }

    private boolean hasDepositableItems(Settings settings) {
        return Inventory.getFirst(item -> item != null && !this.isKept(item.getName(), settings)) != null;
    }

    private boolean shouldDrop(Item item, Settings settings) {
        if (item == null) {
            return false;
        }
        if (this.isKept(item.getName(), settings)) {
            return false;
        }
        return settings.ore.isAny() || this.matchesOre(item.getName(), settings);
    }

    private boolean matchesOre(String string, Settings settings) {
        if (string == null) {
            return false;
        }
        String string2 = string.toLowerCase();
        for (String string3 : settings.ore.getItemNames()) {
            if (!string2.contains(string3.toLowerCase())) continue;
            return true;
        }
        return false;
    }

    private boolean isKept(String string, Settings settings) {
        if (string == null) {
            return false;
        }
        String string2 = string.toLowerCase();
        for (String string3 : this.keepNames(settings)) {
            if (string3.isEmpty() || !string2.contains(string3.toLowerCase())) continue;
            return true;
        }
        return false;
    }

    private String[] keepNames(Settings settings) {
        String string = settings.keepItems;
        if (string == null || string.trim().isEmpty()) {
            return new String[0];
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String string2 : string.split(",")) {
            String string3 = string2.trim();
            if (string3.isEmpty()) continue;
            arrayList.add(string3);
        }
        return arrayList.toArray(new String[0]);
    }

    private int enter(MiningState miningState, int n) {
        this.state = miningState;
        this.stateEntryTick = this.client.getTickCount();
        return n;
    }

    private int ticksInState() {
        return this.client.getTickCount() - this.stateEntryTick;
    }

    public String getStatus() {
        return this.status;
    }

    public String getStateName() {
        return this.state.name();
    }

    public int getTripCount() {
        return this.tripCount;
    }

    public int getXpGained() {
        if (this.startXp < 0) {
            return 0;
        }
        return Math.max(0, Skills.getExperience(Skill.MINING) - this.startXp);
    }

    private /* synthetic */ boolean lambda$findMineableRock$0(WorldPoint worldPoint, boolean bl, boolean bl2, String string, int n, List list, TileObject tileObject) {
        if (tileObject == null) {
            return false;
        }
        WorldPoint worldPoint2 = tileObject.getWorldLocation();
        if (worldPoint2 == null || worldPoint2.distanceTo(worldPoint) > 12) {
            return false;
        }
        if (this.isBlacklisted(worldPoint2)) {
            return false;
        }
        if (!this.hasAction(tileObject.getId(), "Mine")) {
            return false;
        }
        String string2 = this.nameOf(tileObject);
        if (bl && this.isDepletedName(string2)) {
            return false;
        }
        if (bl && !bl2 && !this.nameContains(string2, string)) {
            return false;
        }
        if (bl2 && bl && this.tierOfRockName(string2) > n) {
            return false;
        }
        return list == null || !this.isRockTaken(worldPoint2, list);
    }

    private static enum MiningState {
        MINING,
        WALK_TO_BANK,
        BANKING,
        RETURN_TO_MINE,
        DROPPING;

    }

    public static final class Settings {
        public final MiningOre ore;
        public final MiningMode miningMode;
        public final boolean returnToMine;
        public final String keepItems;
        public final boolean matchRockByName;
        public final String rockNameOverride;
        public final boolean avoidOtherPlayers;
        public final boolean bankAndHold;

        public Settings(MiningOre miningOre, MiningMode miningMode, boolean bl, String string, boolean bl2, String string2, boolean bl3) {
            this(miningOre, miningMode, bl, string, bl2, string2, bl3, false);
        }

        public Settings(MiningOre miningOre, MiningMode miningMode, boolean bl, String string, boolean bl2, String string2, boolean bl3, boolean bl4) {
            this.ore = miningOre;
            this.miningMode = miningMode;
            this.returnToMine = bl;
            this.keepItems = string;
            this.matchRockByName = bl2;
            this.rockNameOverride = string2;
            this.avoidOtherPlayers = bl3;
            this.bankAndHold = bl4;
        }
    }
}


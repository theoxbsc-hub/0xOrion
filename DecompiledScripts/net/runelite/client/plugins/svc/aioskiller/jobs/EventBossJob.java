/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.EquipmentInventorySlot
 *  net.runelite.api.Item
 *  net.runelite.api.NPC
 *  net.runelite.api.NPCComposition
 *  net.runelite.api.Player
 *  net.runelite.api.Prayer
 *  net.runelite.api.Skill
 *  net.runelite.api.TileObject
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.aioskiller.jobs;

import java.util.HashSet;
import java.util.Set;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConstants;
import net.runelite.client.plugins.svc.aioskiller.EventBoss;
import net.runelite.client.plugins.svc.aioskiller.MasterContext;
import net.runelite.client.plugins.svc.aioskiller.SkillerLog;
import net.runelite.client.plugins.svc.aioskiller.services.BestGearPicker;
import net.runelite.client.plugins.svc.aioskiller.services.GearPrefs;
import net.runelite.client.plugins.svc.aioskiller.services.LevelUtil;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.widgets.Prayers;

public final class EventBossJob {
    private final MasterContext ctx;
    private EventBoss boss;
    private Phase phase = Phase.DONE;
    private Outcome outcome = Outcome.NONE;
    private String status = "";
    private int[] desiredGear = new int[0];
    private boolean gearComputed = false;
    private final BestGearPicker bestGear = new BestGearPicker();
    private final Set<Integer> gaveUp = new HashSet<Integer>();
    private int withdrawId = -1;
    private int withdrawTries = 0;
    private int equipAttempts = 0;
    private boolean teleIssued = false;
    private long teleAt = 0L;
    private WorldPoint teleFrom = null;
    private int teleAttempts = 0;
    private long arriveAt = 0L;
    private long fightStartAt = 0L;
    private long lastSeenAt = 0L;
    private boolean engaged = false;
    private boolean wasInArena = false;
    private int bossIndex = -1;
    private boolean weRaisedPrayer = false;
    private long rejuvenateSince = 0L;
    private int deaths = 0;
    private long respawnAt = 0L;

    public EventBossJob(MasterContext masterContext) {
        this.ctx = masterContext;
    }

    public void begin(EventBoss eventBoss) {
        this.boss = eventBoss;
        this.outcome = Outcome.NONE;
        this.phase = Phase.GEAR_CHECK;
        this.status = eventBoss.label() + " \u2014 starting";
        this.deaths = 0;
        this.respawnAt = 0L;
        this.rejuvenateSince = 0L;
        this.resetAttempt();
    }

    private void resetAttempt() {
        this.desiredGear = new int[0];
        this.gearComputed = false;
        this.gaveUp.clear();
        this.withdrawId = -1;
        this.withdrawTries = 0;
        this.equipAttempts = 0;
        this.teleIssued = false;
        this.teleAt = 0L;
        this.teleFrom = null;
        this.teleAttempts = 0;
        this.arriveAt = 0L;
        this.fightStartAt = 0L;
        this.lastSeenAt = 0L;
        this.engaged = false;
        this.wasInArena = false;
        this.bossIndex = -1;
        this.weRaisedPrayer = false;
    }

    public boolean isFinished() {
        return this.phase == Phase.DONE;
    }

    public Outcome outcome() {
        return this.outcome;
    }

    public EventBoss boss() {
        return this.boss;
    }

    public Phase phase() {
        return this.phase;
    }

    public String status() {
        return this.status;
    }

    public int deaths() {
        return this.deaths;
    }

    public String outcomeLabel() {
        switch (this.outcome.ordinal()) {
            case 1: {
                return "boss defeated";
            }
            case 2: {
                return "died " + this.deaths + "x \u2014 skipped";
            }
            case 3: {
                return "no boss found";
            }
            case 4: {
                return "no weapon available";
            }
            case 5: {
                return "timed out";
            }
            case 6: {
                return "boss not spawned \u2014 skipped";
            }
        }
        return "done";
    }

    public int tick() {
        Player player = this.ctx.local;
        if (player == null) {
            return 600;
        }
        switch (this.phase.ordinal()) {
            case 0: {
                return this.gearCheck();
            }
            case 1: {
                return this.home(player);
            }
            case 2: {
                return this.bank(player);
            }
            case 3: {
                return this.withdraw();
            }
            case 4: {
                return this.close();
            }
            case 5: {
                return this.equip(player);
            }
            case 6: {
                return this.teleport(player);
            }
            case 7: {
                return this.arrive();
            }
            case 8: {
                return this.fight(player);
            }
            case 9: {
                return this.respawn();
            }
            case 10: {
                return this.retreat(player);
            }
            case 11: {
                return this.rejuvenate(player);
            }
        }
        return 600;
    }

    private int gearCheck() {
        this.status = this.boss.label() + " \u2014 banking for melee gear";
        this.phase = Phase.HOME;
        return 300;
    }

    private int home(Player player) {
        int n = this.ctx.nav.ensureHome(player);
        this.status = this.boss.label() + " \u2014 travel home: " + this.ctx.nav.status();
        if (n == Integer.MIN_VALUE) {
            this.phase = Phase.BANK;
            return 300;
        }
        return n;
    }

    private int bank(Player player) {
        int n = this.ctx.bank.openStep(player);
        this.status = this.boss.label() + " \u2014 " + this.ctx.bank.status();
        if (n == Integer.MIN_VALUE) {
            this.phase = Phase.WITHDRAW;
            return 300;
        }
        return n;
    }

    private int withdraw() {
        int n;
        if (!this.ctx.bank.isOpen()) {
            this.phase = Phase.BANK;
            return 300;
        }
        if (!this.gearComputed) {
            this.desiredGear = this.computeDesiredGear();
            this.gearComputed = true;
        }
        if ((n = this.firstMissingDesired()) == -1) {
            this.withdrawId = -1;
            this.withdrawTries = 0;
            this.phase = Phase.CLOSE;
            return 400;
        }
        if (n != this.withdrawId) {
            this.withdrawId = n;
            this.withdrawTries = 0;
        }
        if (++this.withdrawTries > 6) {
            this.gaveUp.add(n);
            this.withdrawId = -1;
            this.withdrawTries = 0;
            return 300;
        }
        this.status = this.boss.label() + " \u2014 withdrawing gear (" + n + ")";
        this.ctx.bank.withdrawAll(n);
        return 500;
    }

    private int close() {
        this.ctx.bank.close();
        this.phase = Phase.EQUIP;
        return 600;
    }

    private int equip(Player player) {
        boolean bl;
        int n = this.ctx.gear.equipSet(this.desiredGear, player);
        this.status = this.boss.label() + " \u2014 equip: " + this.ctx.gear.status();
        boolean bl2 = bl = n == Integer.MIN_VALUE;
        if (bl || ++this.equipAttempts > 15) {
            if (this.ctx.gear.hasWeaponEquipped()) {
                this.teleIssued = false;
                this.phase = Phase.TELEPORT;
                return 300;
            }
            this.outcome = Outcome.NO_WEAPON;
            this.status = this.boss.label() + " \u2014 no weapon available, skipping";
            this.ctx.log.record(SkillerLog.Kind.PROBLEM, "Event " + this.boss.label(), Phase.EQUIP.name(), "Could not equip any weapon for the " + this.boss.label() + " \u2014 skipped the event.", "Stock the bank with the combat weapon (or rune/dragon scimitar) so the watcher can gear up.");
            this.phase = Phase.RETREAT;
            return 300;
        }
        return n;
    }

    private int[] computeDesiredGear() {
        int[] nArray;
        if (this.ctx.config.autoBestGear() && (nArray = this.bestGear.compute(this.ctx.client, this.ctx.bank)) != null) {
            return nArray;
        }
        nArray = GearPrefs.eventGear(this.ctx.config);
        if (nArray != null) {
            return nArray;
        }
        int n = LevelUtil.level(Skill.ATTACK);
        int n2 = LevelUtil.level(Skill.DEFENCE);
        int n3 = n >= 60 && this.available(4587) ? 4587 : (n >= 40 && this.available(1333) ? 1333 : 30345);
        int[] nArray2 = n2 >= 40 && this.availableAll(AioSkillerConstants.RUNE_ARMOUR_IDS) ? AioSkillerConstants.RUNE_ARMOUR_IDS : AioSkillerConstants.ROCKCRAB_ARMOUR_IDS;
        int[] nArray3 = new int[nArray2.length + 1];
        System.arraycopy(nArray2, 0, nArray3, 0, nArray2.length);
        nArray3[nArray2.length] = n3;
        return nArray3;
    }

    private int firstMissingDesired() {
        for (int n : this.desiredGear) {
            if (this.gaveUp.contains(n) || this.ownsLive(n) || !this.ctx.bank.has(n)) continue;
            return n;
        }
        return -1;
    }

    private int teleport(Player player) {
        boolean bl;
        WorldPoint worldPoint = player.getWorldLocation();
        long l = System.currentTimeMillis();
        if (!this.teleIssued) {
            this.teleFrom = worldPoint;
            this.teleAt = l;
            this.teleIssued = true;
            this.teleAttempts = 1;
            this.status = this.boss.label() + " \u2014 teleporting (" + this.boss.command() + " " + this.teleAttempts + "/3)";
            this.typeCommand(this.boss.command());
            return 600;
        }
        boolean bl2 = bl = worldPoint != null && this.teleFrom != null && (worldPoint.getPlane() != this.teleFrom.getPlane() || worldPoint.distanceTo(this.teleFrom) > 20);
        if (bl) {
            this.status = this.boss.label() + " \u2014 arena landing\u2026";
            this.arriveAt = l;
            this.phase = Phase.ARRIVE;
            return 600;
        }
        if (l - this.teleAt > 3000L) {
            if (this.teleAttempts >= 3) {
                this.outcome = Outcome.NOT_SPAWNED;
                this.status = this.boss.label() + " \u2014 no teleport after " + this.teleAttempts + " tries (boss not spawned), skipping";
                this.phase = Phase.RETREAT;
                return 300;
            }
            this.teleFrom = worldPoint;
            this.teleAt = l;
            ++this.teleAttempts;
            this.status = this.boss.label() + " \u2014 re-teleporting (" + this.boss.command() + " " + this.teleAttempts + "/3)";
            this.typeCommand(this.boss.command());
            return 600;
        }
        this.status = this.boss.label() + " \u2014 teleporting\u2026";
        return 600;
    }

    private int arrive() {
        long l = System.currentTimeMillis();
        this.maybeProtectFromMagic();
        if (l - this.arriveAt < 2500L) {
            this.status = this.boss.label() + " \u2014 arriving";
            return 600;
        }
        this.fightStartAt = l;
        this.lastSeenAt = l;
        this.engaged = false;
        this.phase = Phase.FIGHT;
        return 300;
    }

    private int fight(Player player) {
        NPC nPC;
        WorldPoint worldPoint = player.getWorldLocation();
        long l = System.currentTimeMillis();
        boolean bl = this.ctx.nav.atHome(worldPoint);
        if (!bl) {
            this.wasInArena = true;
        }
        if (bl && this.wasInArena) {
            ++this.deaths;
            if (this.deaths > 3) {
                this.outcome = Outcome.DIED;
                this.status = this.boss.label() + " \u2014 died " + this.deaths + "x, skipping";
                this.phase = Phase.RETREAT;
                return 300;
            }
            this.respawnAt = l;
            this.status = this.boss.label() + " \u2014 died (" + this.deaths + "/3), returning";
            this.phase = Phase.RESPAWN;
            return 300;
        }
        this.maybeProtectFromMagic();
        if (l - this.fightStartAt > 240000L) {
            this.outcome = Outcome.TIMED_OUT;
            this.status = this.boss.label() + " \u2014 fight timed out, leaving";
            this.phase = Phase.RETREAT;
            return 300;
        }
        NPC nPC2 = nPC = this.bossIndex >= 0 ? this.npcByIndex(this.bossIndex) : null;
        if (nPC != null) {
            boolean bl2;
            if (nPC.isDead()) {
                this.outcome = Outcome.KILLED;
                this.status = this.boss.label() + " \u2014 defeated, leaving";
                this.phase = Phase.RETREAT;
                return 300;
            }
            this.engaged = true;
            this.lastSeenAt = l;
            boolean bl3 = bl2 = player.getInteracting() instanceof NPC && ((NPC)player.getInteracting()).getIndex() == this.bossIndex;
            if (bl2) {
                this.status = this.boss.label() + " \u2014 fighting";
                return 1000;
            }
            this.status = this.boss.label() + " \u2014 attacking (lvl " + EventBossJob.combatLevel(nPC) + ")";
            this.ctx.input.attackNpc(nPC);
            return 1500;
        }
        if (this.bossIndex >= 0 && this.engaged) {
            if (l - this.lastSeenAt > 1500L) {
                this.outcome = Outcome.KILLED;
                this.status = this.boss.label() + " \u2014 defeated, leaving";
                this.phase = Phase.RETREAT;
                return 300;
            }
            this.status = this.boss.label() + " \u2014 boss down?";
            return 600;
        }
        NPC nPC3 = this.selectBoss(worldPoint);
        if (nPC3 != null) {
            this.bossIndex = nPC3.getIndex();
            this.engaged = true;
            this.lastSeenAt = l;
            this.status = this.boss.label() + " \u2014 engaging boss (lvl " + EventBossJob.combatLevel(nPC3) + ")";
            this.ctx.input.attackNpc(nPC3);
            return 1500;
        }
        if (l - this.fightStartAt > 9000L) {
            this.outcome = Outcome.NO_NPC;
            this.status = this.boss.label() + " \u2014 no boss found, leaving";
            this.phase = Phase.RETREAT;
            return 300;
        }
        this.status = this.boss.label() + " \u2014 searching for boss";
        return 800;
    }

    private int respawn() {
        long l = System.currentTimeMillis();
        if (l - this.respawnAt < 1000L) {
            this.status = this.boss.label() + " \u2014 returning after death";
            return 400;
        }
        this.resetAttempt();
        if (this.ctx.gear.hasWeaponEquipped()) {
            this.status = this.boss.label() + " \u2014 re-teleporting after death";
            this.phase = Phase.TELEPORT;
        } else {
            this.status = this.boss.label() + " \u2014 re-gearing after death";
            this.phase = Phase.GEAR_CHECK;
        }
        return 300;
    }

    private int retreat(Player player) {
        this.deactivateProtectFromMagic();
        int n = this.ctx.nav.ensureHome(player);
        this.status = this.boss.label() + " \u2014 returning home (" + this.outcomeLabel() + "): " + this.ctx.nav.status();
        if (n == Integer.MIN_VALUE) {
            this.rejuvenateSince = 0L;
            this.phase = Phase.REJUVENATE;
            return 300;
        }
        return n;
    }

    private int rejuvenate(Player player) {
        this.deactivateProtectFromMagic();
        if (this.rejuvenateSince == 0L) {
            this.rejuvenateSince = System.currentTimeMillis();
        }
        long l = System.currentTimeMillis();
        TileObject tileObject2 = TileObjects.getNearest(tileObject -> tileObject != null && tileObject.getId() == 42822);
        if (tileObject2 != null) {
            this.status = this.boss.label() + " \u2014 rejuvenating at pool";
            this.ctx.input.interactObjectVia(tileObject2, "Rejuvenate");
            this.rejuvenateSince = 0L;
            this.phase = Phase.DONE;
            return 2500;
        }
        if (l - this.rejuvenateSince > 12000L) {
            this.status = this.boss.label() + " \u2014 rejuvenation pool not found, resuming";
            this.rejuvenateSince = 0L;
            this.phase = Phase.DONE;
            return 300;
        }
        this.status = this.boss.label() + " \u2014 looking for rejuvenation pool";
        return 600;
    }

    private void maybeProtectFromMagic() {
        try {
            if (LevelUtil.level(Skill.PRAYER) < 37) {
                return;
            }
            if (this.ctx.client.getBoostedSkillLevel(Skill.PRAYER) <= 0) {
                return;
            }
            if (!this.ctx.client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC)) {
                Prayers.toggle(Prayer.PROTECT_FROM_MAGIC);
                this.weRaisedPrayer = true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private void deactivateProtectFromMagic() {
        try {
            if (this.weRaisedPrayer && this.ctx.client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC)) {
                Prayers.toggle(Prayer.PROTECT_FROM_MAGIC);
            }
            this.weRaisedPrayer = false;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private NPC selectBoss(WorldPoint worldPoint) {
        if (worldPoint == null) {
            return null;
        }
        NPC nPC2 = null;
        int n = Integer.MIN_VALUE;
        int n2 = Integer.MAX_VALUE;
        for (NPC nPC3 : NPCs.getAll(nPC -> nPC != null && !nPC.isDead() && nPC.getName() != null && EventBossJob.isAttackable(nPC))) {
            int n3;
            int n4;
            WorldPoint worldPoint2 = nPC3.getWorldLocation();
            if (worldPoint2 == null || (n4 = worldPoint2.distanceTo(worldPoint)) > 20 || (n3 = EventBossJob.combatLevel(nPC3)) <= n && (n3 != n || n4 >= n2)) continue;
            nPC2 = nPC3;
            n = n3;
            n2 = n4;
        }
        return nPC2;
    }

    private NPC npcByIndex(int n) {
        if (n < 0) {
            return null;
        }
        return NPCs.getNearest(nPC -> nPC != null && nPC.getIndex() == n);
    }

    private static int combatLevel(NPC nPC) {
        try {
            NPCComposition nPCComposition = nPC.getTransformedComposition();
            if (nPCComposition == null) {
                nPCComposition = nPC.getComposition();
            }
            return nPCComposition != null ? nPCComposition.getCombatLevel() : 0;
        }
        catch (Throwable throwable) {
            return 0;
        }
    }

    private static boolean isAttackable(NPC nPC) {
        NPCComposition nPCComposition = nPC.getTransformedComposition();
        if (nPCComposition == null) {
            nPCComposition = nPC.getComposition();
        }
        if (nPCComposition == null) {
            return true;
        }
        String[] stringArray = nPCComposition.getActions();
        if (stringArray == null) {
            return true;
        }
        boolean bl = false;
        for (String string : stringArray) {
            if (string == null) continue;
            if (string.toLowerCase().contains("spawn-instanced")) {
                return false;
            }
            if (!string.equalsIgnoreCase("Attack")) continue;
            bl = true;
        }
        return bl;
    }

    private boolean available(int n) {
        return this.ownsLive(n) || this.ctx.bank.has(n);
    }

    private boolean availableAll(int[] nArray) {
        for (int n : nArray) {
            if (this.available(n)) continue;
            return false;
        }
        return true;
    }

    private boolean ownsLive(int n) {
        try {
            if (Inventory.getFirst(n) != null) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        for (EquipmentInventorySlot equipmentInventorySlot : EquipmentInventorySlot.values()) {
            try {
                Item item = Equipment.fromSlot(equipmentInventorySlot);
                if (item == null || item.getId() != n) continue;
                return true;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return false;
    }

    private void typeCommand(String string) {
        this.ctx.input.command(string);
    }

    public static enum Phase {
        GEAR_CHECK,
        HOME,
        BANK,
        WITHDRAW,
        CLOSE,
        EQUIP,
        TELEPORT,
        ARRIVE,
        FIGHT,
        RESPAWN,
        RETREAT,
        REJUVENATE,
        DONE;

    }

    public static enum Outcome {
        NONE,
        KILLED,
        DIED,
        NO_NPC,
        NO_WEAPON,
        TIMED_OUT,
        NOT_SPAWNED;

    }
}


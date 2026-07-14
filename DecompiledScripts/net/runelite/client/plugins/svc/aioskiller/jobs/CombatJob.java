/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.NPC
 *  net.runelite.api.Skill
 *  net.runelite.api.TileObject
 */
package net.runelite.client.plugins.svc.aioskiller.jobs;

import java.util.Arrays;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConstants;
import net.runelite.client.plugins.svc.aioskiller.JobPhase;
import net.runelite.client.plugins.svc.aioskiller.MasterContext;
import net.runelite.client.plugins.svc.aioskiller.SkillId;
import net.runelite.client.plugins.svc.aioskiller.SkillJob;
import net.runelite.client.plugins.svc.aioskiller.services.BestGearPicker;
import net.runelite.client.plugins.svc.aioskiller.services.BusyNpcTracker;
import net.runelite.client.plugins.svc.aioskiller.services.CombatFocus;
import net.runelite.client.plugins.svc.aioskiller.services.GearPrefs;
import net.runelite.client.plugins.svc.aioskiller.services.GemstoneGearAcquirer;
import net.runelite.client.plugins.svc.aioskiller.services.LevelUtil;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;

public final class CombatJob
extends SkillJob {
    private C c = C.CHECK;
    private int equipAttempts = 0;
    private int withdrawId = -1;
    private int withdrawTries = 0;
    private final BestGearPicker bestGear = new BestGearPicker();
    private int[] bestSet = null;
    private long bestSig = Long.MIN_VALUE;
    private final GemstoneGearAcquirer gemAcq;
    private final BusyNpcTracker busy = new BusyNpcTracker();
    private final CombatFocus focus = new CombatFocus();
    private Skill styledFor = null;
    private boolean lungeSet = false;

    public CombatJob(MasterContext masterContext) {
        super(masterContext);
        this.gemAcq = new GemstoneGearAcquirer(masterContext);
    }

    private boolean gemstoneFromOne() {
        return this.ctx.config.gemstoneFromLevel1();
    }

    @Override
    public SkillId id() {
        return SkillId.COMBAT;
    }

    @Override
    public int currentLevel() {
        return LevelUtil.combatMin();
    }

    @Override
    public int targetLevel() {
        return this.ctx.config.combatTarget();
    }

    @Override
    public boolean requiresEquipment() {
        return true;
    }

    private boolean autoBest() {
        return this.ctx.config.autoBestGear();
    }

    @Override
    public void reset() {
        super.reset();
        this.c = C.CHECK;
        this.equipAttempts = 0;
        this.withdrawId = -1;
        this.withdrawTries = 0;
        this.bestSet = null;
        this.bestSig = Long.MIN_VALUE;
        this.busy.clear();
        this.focus.reset();
        this.styledFor = null;
        this.lungeSet = false;
        if (this.gemstoneFromOne()) {
            this.gemAcq.begin();
        } else {
            this.gemAcq.reset();
        }
    }

    @Override
    public void onChat(String string) {
        this.gemAcq.onChat(string);
        this.busy.onChat(string);
    }

    @Override
    public boolean watchdogSuppressed() {
        return this.gemstoneFromOne() && this.phase == JobPhase.SETUP && !this.gemAcq.isDone();
    }

    @Override
    public int tick() {
        if (this.gemstoneFromOne()) {
            return this.tickGemstoneFromOne();
        }
        if (this.autoBest() && this.bestSet != null && this.bestGear.signature() != this.bestSig) {
            this.bestSet = null;
            this.equipAttempts = 0;
            this.withdrawId = -1;
            this.withdrawTries = 0;
            this.phase = JobPhase.SETUP;
            this.c = C.CHECK;
        }
        switch (this.phase) {
            case SETUP: {
                return this.setup();
            }
            case TRAVEL: {
                return this.travel();
            }
            case TRAIN: {
                return this.train();
            }
        }
        return 600;
    }

    private int[] combatOverride() {
        return GearPrefs.combatGear(this.ctx.config);
    }

    private int[] gearSet() {
        if (this.autoBest()) {
            if (this.bestSet != null) {
                return this.bestSet;
            }
            if (this.ctx.bank.isOpen()) {
                int[] nArray = this.bestGear.compute(this.ctx.client, this.ctx.bank);
                this.bestSet = nArray != null ? nArray : AioSkillerConstants.COMBAT_GEAR_IDS;
                this.bestSig = this.bestGear.signature();
                return this.bestSet;
            }
            return AioSkillerConstants.COMBAT_GEAR_IDS;
        }
        int[] nArray = this.combatOverride();
        if (nArray != null) {
            return nArray;
        }
        return AioSkillerConstants.COMBAT_GEAR_IDS;
    }

    private String combatTerm() {
        return this.gemstoneFromOne() ? "Gemstone" : "rock crab";
    }

    private int setup() {
        switch (this.c.ordinal()) {
            case 0: {
                if (this.autoBest() && this.bestSet == null) {
                    this.c = C.HOME;
                    return 200;
                }
                if (this.ctx.gear.allEquipped(this.gearSet())) {
                    this.ctx.nav.beginGlider();
                    this.phase = JobPhase.TRAVEL;
                    return 300;
                }
                if (this.firstMissingGear() == -1) {
                    this.c = C.EQUIP;
                    return 200;
                }
                this.c = C.HOME;
                return 200;
            }
            case 1: {
                int n = this.ctx.nav.ensureHome(this.ctx.local);
                this.status = "Travel home: " + this.ctx.nav.status();
                if (n == Integer.MIN_VALUE) {
                    this.c = C.BANK;
                    return 300;
                }
                return n;
            }
            case 2: {
                int n = this.ctx.bank.openStep(this.ctx.local);
                if (n == Integer.MIN_VALUE) {
                    this.c = C.WITHDRAW;
                    return 300;
                }
                return n;
            }
            case 3: {
                if (!this.ctx.bank.isOpen()) {
                    this.c = C.BANK;
                    return 300;
                }
                int n = this.firstMissingGear();
                if (n == -1) {
                    this.withdrawId = -1;
                    this.withdrawTries = 0;
                    this.c = C.CLOSE;
                    return 400;
                }
                if (!this.ctx.bank.has(n)) {
                    return this.skip("Combat gear not in bank: " + n);
                }
                if (n != this.withdrawId) {
                    this.withdrawId = n;
                    this.withdrawTries = 0;
                }
                if (++this.withdrawTries > 6) {
                    return this.skip("Could not withdraw combat gear: " + n);
                }
                this.status = "Withdrawing combat gear (" + n + ")";
                this.ctx.bank.withdrawAll(n);
                return 500;
            }
            case 4: {
                this.ctx.bank.close();
                this.c = C.EQUIP;
                return 600;
            }
            case 5: {
                int n = this.ctx.gear.equipSet(this.gearSet(), this.ctx.local);
                this.status = "Equip: " + this.ctx.gear.status();
                if (n == Integer.MIN_VALUE) {
                    this.c = C.CHECK;
                    this.ctx.nav.beginGlider();
                    this.phase = JobPhase.TRAVEL;
                    return 300;
                }
                if (++this.equipAttempts > 15) {
                    return this.skip("Combat gear not found in bank");
                }
                return n;
            }
        }
        return 600;
    }

    private int travel() {
        int n = this.ctx.nav.gliderTo(this.combatTerm(), this.ctx.local);
        this.status = "Travel (" + this.combatTerm() + "): " + this.ctx.nav.status();
        if (n == Integer.MIN_VALUE) {
            this.styledFor = null;
            this.lungeSet = false;
            this.phase = JobPhase.TRAIN;
            return 1000;
        }
        return n;
    }

    private int train() {
        if (this.ctx.nav.atHome(this.ctx.local.getWorldLocation())) {
            this.ctx.nav.beginGlider();
            this.phase = JobPhase.TRAVEL;
            return 600;
        }
        this.applyFocusStyle();
        if (this.ctx.local.getInteracting() instanceof NPC) {
            this.status = "In combat (" + this.focusName() + ")";
            return 600;
        }
        return this.trainRockCrabs();
    }

    private int trainRockCrabs() {
        NPC nPC = NPCs.getNearest(this::isCrab);
        if (nPC != null) {
            this.status = "Attacking " + nPC.getName() + " (" + this.focusName() + ")";
            this.busy.markAttempted(nPC);
            this.ctx.input.attackNpc(nPC);
            return 1500;
        }
        this.status = "Searching for crabs";
        return 1000;
    }

    private int tickGemstoneFromOne() {
        switch (this.phase) {
            case SETUP: {
                return this.setupGemstoneFromOne();
            }
            case TRAVEL: {
                return this.travel();
            }
            case TRAIN: {
                return this.trainGemstoneFromOne();
            }
        }
        return 600;
    }

    private int setupGemstoneFromOne() {
        int n = this.gemAcq.step(this.ctx.local);
        this.status = "Gemstone setup \u2014 " + this.gemAcq.status();
        if (this.gemAcq.isFailed()) {
            return this.skip("Gemstone setup: " + this.gemAcq.status());
        }
        if (!this.gemAcq.isDone()) {
            return n == Integer.MIN_VALUE ? 400 : n;
        }
        this.ctx.nav.beginGlider();
        this.phase = JobPhase.TRAVEL;
        return 300;
    }

    private int trainGemstoneFromOne() {
        if (this.ctx.nav.atHome(this.ctx.local.getWorldLocation())) {
            this.ctx.nav.beginGlider();
            this.phase = JobPhase.TRAVEL;
            return 600;
        }
        int n = this.equipGemstoneTier();
        if (n != Integer.MIN_VALUE) {
            return n;
        }
        this.applyLungeStyle();
        if (this.ctx.local.getInteracting() instanceof NPC) {
            this.status = "Fighting Gemstone Crab (lunge)";
            return 1200;
        }
        return this.fightGemstoneCrab();
    }

    private int equipGemstoneTier() {
        int[] nArray;
        int n = LevelUtil.level(Skill.ATTACK);
        int n2 = LevelUtil.level(Skill.DEFENCE);
        int n3 = n >= 60 ? 4587 : (n >= 40 ? 1333 : 1000001);
        boolean bl = n2 >= 40;
        boolean bl2 = this.owns(n3);
        if (bl && bl2) {
            nArray = CombatJob.withWeapon(AioSkillerConstants.RUNE_ARMOUR_IDS, n3);
        } else if (bl) {
            nArray = AioSkillerConstants.RUNE_ARMOUR_IDS;
        } else if (bl2) {
            nArray = new int[]{n3};
        } else {
            return Integer.MIN_VALUE;
        }
        if (this.ctx.gear.allEquipped(nArray)) {
            return Integer.MIN_VALUE;
        }
        int n4 = this.ctx.gear.equipSet(nArray, this.ctx.local);
        this.status = "Equipping gear: " + this.ctx.gear.status();
        this.styledFor = null;
        this.lungeSet = false;
        return n4;
    }

    private static int[] withWeapon(int[] nArray, int n) {
        int[] nArray2 = Arrays.copyOf(nArray, nArray.length + 1);
        nArray2[nArray.length] = n;
        return nArray2;
    }

    private int fightGemstoneCrab() {
        NPC nPC = NPCs.getNearest(this::isGemstoneCrab);
        if (nPC != null) {
            this.status = "Attacking Gemstone Crab (lunge)";
            this.busy.markAttempted(nPC);
            this.ctx.input.attackNpc(nPC);
            return 1500;
        }
        TileObject tileObject = TileObjects.getNearest(57613);
        if (tileObject != null) {
            this.status = "Crawling to next crab";
            this.ctx.input.interactObjectVia(tileObject, "Crawl-through");
            return 2500;
        }
        this.status = "Searching for gemstone crab";
        return 1200;
    }

    private void applyFocusStyle() {
        Skill skill = this.focus.select(this.targetLevel());
        if (skill != null && skill != this.styledFor) {
            this.ctx.gear.setStyle(CombatFocus.styleIndexFor(skill));
            this.styledFor = skill;
        }
    }

    private void applyLungeStyle() {
        if (!this.lungeSet) {
            this.ctx.gear.setStyle(2);
            this.lungeSet = true;
        }
    }

    private String focusName() {
        Skill skill = this.focus.current();
        return skill == null ? "done" : skill.name().toLowerCase();
    }

    private int firstMissingGear() {
        for (int n : this.gearSet()) {
            if (this.owns(n)) continue;
            return n;
        }
        return -1;
    }

    private boolean isCrab(NPC nPC) {
        if (nPC == null || nPC.isDead() || nPC.getName() == null) {
            return false;
        }
        if (nPC.getWorldLocation().distanceTo(this.ctx.local.getWorldLocation()) > 12) {
            return false;
        }
        if (this.busy.isBusy(nPC, this.ctx.local)) {
            return false;
        }
        for (String string : AioSkillerConstants.COMBAT_NPC_NAMES) {
            if (!string.equalsIgnoreCase(nPC.getName())) continue;
            return true;
        }
        return false;
    }

    private boolean isGemstoneCrab(NPC nPC) {
        if (nPC == null || nPC.isDead() || nPC.getName() == null) {
            return false;
        }
        if (nPC.getWorldLocation().distanceTo(this.ctx.local.getWorldLocation()) > 14) {
            return false;
        }
        if (this.busy.isBusy(nPC, this.ctx.local)) {
            return false;
        }
        return nPC.getName().equalsIgnoreCase("Gemstone crab");
    }

    private static enum C {
        CHECK,
        HOME,
        BANK,
        WITHDRAW,
        CLOSE,
        EQUIP;

    }
}


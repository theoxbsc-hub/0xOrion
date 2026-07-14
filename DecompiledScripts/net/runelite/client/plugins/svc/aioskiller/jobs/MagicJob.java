/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.NPC
 *  net.runelite.api.Skill
 */
package net.runelite.client.plugins.svc.aioskiller.jobs;

import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConstants;
import net.runelite.client.plugins.svc.aioskiller.JobPhase;
import net.runelite.client.plugins.svc.aioskiller.MasterContext;
import net.runelite.client.plugins.svc.aioskiller.SkillId;
import net.runelite.client.plugins.svc.aioskiller.SkillJob;
import net.runelite.client.plugins.svc.aioskiller.services.BusyNpcTracker;
import net.runelite.client.plugins.svc.aioskiller.services.LevelUtil;
import net.unethicalite.api.entities.NPCs;

public final class MagicJob
extends SkillJob {
    private C c = C.CHECK;
    private int equipAttempts = 0;
    private int withdrawId = -1;
    private int withdrawTries = 0;
    private final BusyNpcTracker busy = new BusyNpcTracker();

    public MagicJob(MasterContext masterContext) {
        super(masterContext);
    }

    @Override
    public SkillId id() {
        return SkillId.MAGIC;
    }

    @Override
    public int currentLevel() {
        return LevelUtil.level(Skill.MAGIC);
    }

    @Override
    public int targetLevel() {
        return this.ctx.config.magicTarget();
    }

    @Override
    public boolean requiresEquipment() {
        return true;
    }

    @Override
    public void reset() {
        super.reset();
        this.c = C.CHECK;
        this.equipAttempts = 0;
        this.withdrawId = -1;
        this.withdrawTries = 0;
        this.busy.clear();
    }

    @Override
    public void onChat(String string) {
        this.busy.onChat(string);
    }

    @Override
    public int tick() {
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

    private int setup() {
        switch (this.c.ordinal()) {
            case 0: {
                if (this.ctx.gear.allEquipped(AioSkillerConstants.MAGIC_GEAR_IDS)) {
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
                    return this.skip("Magic gear not in bank: " + n);
                }
                if (n != this.withdrawId) {
                    this.withdrawId = n;
                    this.withdrawTries = 0;
                }
                if (++this.withdrawTries > 6) {
                    return this.skip("Could not withdraw magic gear: " + n);
                }
                this.status = "Withdrawing magic gear (" + n + ")";
                this.ctx.bank.withdrawAll(n);
                return 500;
            }
            case 4: {
                this.ctx.bank.close();
                this.c = C.EQUIP;
                return 600;
            }
            case 5: {
                int n = this.ctx.gear.equipSet(AioSkillerConstants.MAGIC_GEAR_IDS, this.ctx.local);
                this.status = "Equip: " + this.ctx.gear.status();
                if (n == Integer.MIN_VALUE) {
                    this.c = C.CHECK;
                    this.ctx.nav.beginGlider();
                    this.phase = JobPhase.TRAVEL;
                    return 300;
                }
                if (++this.equipAttempts > 15) {
                    return this.skip("Magic gear not found in bank");
                }
                return n;
            }
        }
        return 600;
    }

    private int travel() {
        int n = this.ctx.nav.gliderTo("rock crab", this.ctx.local);
        this.status = "Travel (rock crab): " + this.ctx.nav.status();
        if (n == Integer.MIN_VALUE) {
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
        if (this.ctx.local.getInteracting() instanceof NPC) {
            this.status = "Casting";
            return 600;
        }
        NPC nPC = NPCs.getNearest(this::isCrab);
        if (nPC != null) {
            this.status = "Attacking " + nPC.getName();
            this.busy.markAttempted(nPC);
            this.ctx.input.attackNpc(nPC);
            return 1500;
        }
        this.status = "Searching for crabs";
        return 1000;
    }

    private int firstMissingGear() {
        for (int n : AioSkillerConstants.MAGIC_GEAR_IDS) {
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

    private static enum C {
        CHECK,
        HOME,
        BANK,
        WITHDRAW,
        CLOSE,
        EQUIP;

    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Item
 *  net.runelite.api.Skill
 */
package net.runelite.client.plugins.svc.aioskiller.jobs;

import net.runelite.api.Item;
import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.aioskiller.JobPhase;
import net.runelite.client.plugins.svc.aioskiller.MasterContext;
import net.runelite.client.plugins.svc.aioskiller.SkillId;
import net.runelite.client.plugins.svc.aioskiller.SkillJob;
import net.runelite.client.plugins.svc.aioskiller.jobs.OrePrep;
import net.runelite.client.plugins.svc.aioskiller.services.LevelUtil;
import net.runelite.client.plugins.svc.mining.MiningEngine;
import net.runelite.client.plugins.svc.mining.MiningMode;
import net.runelite.client.plugins.svc.mining.MiningOre;
import net.unethicalite.api.items.Inventory;

public final class MiningJob
extends SkillJob {
    private static final int PRIME_MAX_OPEN_ATTEMPTS = 15;
    private final MiningEngine engine;
    private final OrePrep orePrep = new OrePrep();
    private Setup setupStep = Setup.CURRENT;
    private boolean acquiring = false;
    private int primeOpenAttempts = 0;

    public MiningJob(MasterContext masterContext) {
        super(masterContext);
        this.engine = new MiningEngine(masterContext.client, masterContext.input);
    }

    @Override
    public SkillId id() {
        return SkillId.MINING;
    }

    @Override
    public int currentLevel() {
        return LevelUtil.level(Skill.MINING);
    }

    @Override
    public int targetLevel() {
        return this.ctx.config.miningTarget();
    }

    @Override
    public void reset() {
        super.reset();
        this.engine.reset();
        this.orePrep.reset();
        this.setupStep = Setup.CURRENT;
        this.acquiring = false;
        this.primeOpenAttempts = 0;
    }

    @Override
    public void onChat(String string) {
        this.engine.onChatMessage(string);
    }

    private int pickaxeForLevel() {
        return this.currentLevel() < 41 ? 1267 : 1275;
    }

    private boolean wantsRunePrebuy() {
        return this.targetLevel() > 41;
    }

    @Override
    public int tick() {
        switch (this.phase) {
            case SETUP: {
                return this.setup();
            }
            case TRAVEL: {
                int n = this.ctx.nav.gliderTo("The ore pit", this.ctx.local);
                this.status = "Travel (The ore pit): " + this.ctx.nav.status();
                if (n == Integer.MIN_VALUE) {
                    this.engine.reset();
                    this.phase = JobPhase.TRAIN;
                    return 600;
                }
                return n;
            }
            case TRAIN: {
                return this.train();
            }
        }
        return 600;
    }

    private int setup() {
        switch (this.setupStep.ordinal()) {
            case 0: {
                int n = this.pickaxeForLevel();
                if (this.owns(n)) {
                    this.acquiring = false;
                    this.setupStep = Setup.RUNE;
                    return 200;
                }
                if (!this.acquiring) {
                    this.ctx.acquirer.begin(n, 1, true, true);
                    this.acquiring = true;
                }
                int n2 = this.ctx.acquirer.step(this.ctx.local);
                this.status = "Pickaxe: " + this.ctx.acquirer.status();
                if (this.ctx.acquirer.isDone()) {
                    this.acquiring = false;
                    if (!this.owns(n)) {
                        return this.skip("Mining: could not obtain pickaxe");
                    }
                    this.setupStep = Setup.RUNE;
                    return 300;
                }
                return n2;
            }
            case 1: {
                if (!this.wantsRunePrebuy() || this.owns(1275)) {
                    this.acquiring = false;
                    this.setupStep = Setup.PRIME;
                    return 200;
                }
                if (!this.acquiring) {
                    this.ctx.acquirer.begin(1275, 1, true, true);
                    this.acquiring = true;
                }
                int n = this.ctx.acquirer.step(this.ctx.local);
                this.status = "Rune pickaxe (pre-buy): " + this.ctx.acquirer.status();
                if (this.ctx.acquirer.isDone()) {
                    this.acquiring = false;
                    this.setupStep = Setup.PRIME;
                    return 300;
                }
                return n;
            }
            case 2: {
                if (!this.smithingPending()) {
                    this.primeOpenAttempts = 0;
                    this.setupStep = Setup.GO;
                    return 200;
                }
                int n = this.ctx.nav.ensureHome(this.ctx.local);
                if (n != Integer.MIN_VALUE) {
                    this.status = "Ore-prep bank check: " + this.ctx.nav.status();
                    return n;
                }
                int n3 = this.ctx.bank.openStep(this.ctx.local);
                if (n3 == Integer.MIN_VALUE) {
                    this.orePrep.primeFromBank();
                    this.ctx.bank.close();
                    this.status = "Ore-prep bank check: read banked ore";
                    this.primeOpenAttempts = 0;
                    this.setupStep = Setup.GO;
                    return 300;
                }
                this.status = "Ore-prep bank check: " + this.ctx.bank.status();
                if (++this.primeOpenAttempts > 15) {
                    this.primeOpenAttempts = 0;
                    this.setupStep = Setup.GO;
                    return 300;
                }
                return n3;
            }
        }
        this.setupStep = Setup.CURRENT;
        this.ctx.bank.close();
        this.ctx.nav.beginGlider();
        this.phase = JobPhase.TRAVEL;
        return 300;
    }

    private int train() {
        boolean bl = this.owns(1275);
        boolean bl2 = this.owns(1267);
        if (!bl && !bl2) {
            this.phase = JobPhase.SETUP;
            this.setupStep = Setup.CURRENT;
            return 300;
        }
        if (this.currentLevel() >= 41 && bl2 && bl) {
            this.dropIron();
            this.status = "Switched to rune pickaxe (dropped iron)";
        }
        MiningOre miningOre = this.orePrep.decide(this.ctx.config.miningBankOre(), this.smithingPending(), this.engine.getStateName(), this.orePrepScalePercent());
        int n = this.engine.tick(this.ctx.local, new MiningEngine.Settings(miningOre, MiningMode.BANK, true, "pickaxe", true, "", true));
        this.status = (String)(this.orePrep.prepActive() ? this.orePrep.progressSummary() + " | " : "") + "Mining: " + this.engine.getStatus();
        return n < 0 ? 600 : n;
    }

    private boolean smithingPending() {
        return this.ctx.config.smithingEnabled() && LevelUtil.level(Skill.SMITHING) < this.ctx.config.smithingTarget();
    }

    private int orePrepScalePercent() {
        int n;
        boolean bl;
        boolean bl2 = bl = this.ctx.config.afkAllSkills() || this.ctx.config.smithingAfk();
        if (!(this.ctx.config.smithingEnabled() && bl && this.smithingPending())) {
            return 100;
        }
        int n2 = this.ctx.config.smithingTarget();
        int n3 = n2 >= 99 ? 500 : (n2 >= 90 ? 400 : (n = n2 >= 80 ? 200 : 100));
        if (LevelUtil.level(Skill.SMITHING) > 70) {
            n /= 2;
        }
        return n;
    }

    private void dropIron() {
        Item item = Inventory.getFirst(1267);
        if (item != null) {
            this.ctx.input.itemInteractVia(item, "Drop");
        }
    }

    private static enum Setup {
        CURRENT,
        RUNE,
        PRIME,
        GO;

    }
}


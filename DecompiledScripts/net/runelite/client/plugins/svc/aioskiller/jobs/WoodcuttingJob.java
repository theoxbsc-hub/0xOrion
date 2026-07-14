/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Skill
 */
package net.runelite.client.plugins.svc.aioskiller.jobs;

import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConstants;
import net.runelite.client.plugins.svc.aioskiller.JobPhase;
import net.runelite.client.plugins.svc.aioskiller.MasterContext;
import net.runelite.client.plugins.svc.aioskiller.SkillId;
import net.runelite.client.plugins.svc.aioskiller.SkillJob;
import net.runelite.client.plugins.svc.aioskiller.services.LevelUtil;
import net.runelite.client.plugins.svc.woodcutting.WoodcuttingEngine;

public final class WoodcuttingJob
extends SkillJob {
    private static final int[] WC_LOGS = new int[]{1511, 1519, 1515};
    private final WoodcuttingEngine engine;
    private AxeStep axeStep = AxeStep.PRIMARY;
    private boolean acquiring = false;
    private boolean triedRuneUpgrade = false;
    private int trainedTier = -1;

    public WoodcuttingJob(MasterContext masterContext) {
        super(masterContext);
        this.engine = new WoodcuttingEngine(masterContext.client, masterContext.input);
    }

    @Override
    public SkillId id() {
        return SkillId.WOODCUTTING;
    }

    @Override
    public int currentLevel() {
        return LevelUtil.level(Skill.WOODCUTTING);
    }

    @Override
    public int targetLevel() {
        return this.ctx.config.woodcuttingTarget();
    }

    @Override
    public void reset() {
        super.reset();
        this.engine.reset();
        this.axeStep = AxeStep.PRIMARY;
        this.acquiring = false;
        this.triedRuneUpgrade = false;
        this.trainedTier = -1;
    }

    private int axeForLevel() {
        return this.currentLevel() < 41 ? 1349 : 1359;
    }

    private boolean hasAnyAxe() {
        return this.owns(1349) || this.owns(1359);
    }

    private int tierForLevel() {
        int n = this.currentLevel();
        if (n < 30) {
            return 0;
        }
        if (n < 60) {
            return 1;
        }
        return 2;
    }

    private WoodcuttingEngine.Settings settingsForTier(int n) {
        switch (n) {
            case 2: {
                return new WoodcuttingEngine.Settings(1309, 1515, "Chop down");
            }
            case 1: {
                return new WoodcuttingEngine.Settings(1308, 1519, "Chop down");
            }
        }
        return new WoodcuttingEngine.Settings(1276, 1511, "Chop down");
    }

    @Override
    public int tick() {
        switch (this.phase) {
            case SETUP: {
                int n;
                int n2 = n = this.axeStep == AxeStep.PRIMARY ? this.axeForLevel() : 1349;
                if (this.owns(n)) {
                    this.acquiring = false;
                    this.axeStep = AxeStep.PRIMARY;
                    this.phase = JobPhase.TRAVEL;
                    return 300;
                }
                if (!this.acquiring) {
                    this.ctx.acquirer.begin(n, 1, true, true);
                    this.acquiring = true;
                }
                int n3 = this.ctx.acquirer.step(this.ctx.local);
                this.status = "Axe (" + (this.axeStep == AxeStep.PRIMARY ? "best" : "iron fallback") + "): " + this.ctx.acquirer.status();
                if (this.ctx.acquirer.isDone()) {
                    this.acquiring = false;
                    if (this.owns(n)) {
                        this.axeStep = AxeStep.PRIMARY;
                        this.phase = JobPhase.TRAVEL;
                        return 400;
                    }
                    if (this.axeStep == AxeStep.PRIMARY && n == 1359) {
                        this.triedRuneUpgrade = true;
                    }
                    if (this.hasAnyAxe()) {
                        this.axeStep = AxeStep.PRIMARY;
                        this.phase = JobPhase.TRAVEL;
                        return 400;
                    }
                    if (this.axeStep == AxeStep.PRIMARY && n != 1349) {
                        this.axeStep = AxeStep.FALLBACK;
                        return 300;
                    }
                    return this.skip("Woodcutting: could not obtain axe");
                }
                return n3;
            }
            case TRAVEL: {
                if (!this.ctx.nav.atHome(this.ctx.local.getWorldLocation())) {
                    int n = this.ctx.nav.ensureHome(this.ctx.local);
                    this.status = "Travel home: " + this.ctx.nav.status();
                    return n;
                }
                int n = this.ctx.nav.walkTo(AioSkillerConstants.WC_TILE, this.ctx.local, 3);
                this.status = "Walk to trees: " + this.ctx.nav.status();
                if (n == Integer.MIN_VALUE) {
                    this.engine.reset();
                    this.trainedTier = -1;
                    this.phase = JobPhase.TRAIN;
                    return 600;
                }
                return n;
            }
            case TRAIN: {
                if (!this.hasAnyAxe()) {
                    this.axeStep = AxeStep.PRIMARY;
                    this.phase = JobPhase.SETUP;
                    return 300;
                }
                if (this.currentLevel() >= 41 && !this.owns(1359) && !this.triedRuneUpgrade) {
                    this.triedRuneUpgrade = true;
                    this.axeStep = AxeStep.PRIMARY;
                    this.phase = JobPhase.SETUP;
                    return 300;
                }
                int n = this.tierForLevel();
                if (this.trainedTier != -1 && n != this.trainedTier) {
                    if (!this.engine.dropMatching(WC_LOGS)) {
                        this.status = "Clearing logs for tier switch";
                        return 500;
                    }
                    this.engine.reset();
                }
                this.trainedTier = n;
                int n4 = this.engine.tick(this.ctx.local, this.settingsForTier(n));
                this.status = "Woodcutting: " + this.engine.getStatus();
                return n4 < 0 ? 600 : n4;
            }
        }
        return 600;
    }

    private static enum AxeStep {
        PRIMARY,
        FALLBACK;

    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Skill
 */
package net.runelite.client.plugins.svc.aioskiller.jobs;

import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.aioskiller.JobPhase;
import net.runelite.client.plugins.svc.aioskiller.MasterContext;
import net.runelite.client.plugins.svc.aioskiller.SkillId;
import net.runelite.client.plugins.svc.aioskiller.SkillJob;
import net.runelite.client.plugins.svc.aioskiller.services.LevelUtil;
import net.runelite.client.plugins.svc.crafting.CraftingEngine;

public final class CraftingJob
extends SkillJob {
    private final CraftingEngine engine;
    private boolean setupStarted = false;
    private boolean gemsConfirmed = false;

    public CraftingJob(MasterContext masterContext) {
        super(masterContext);
        this.engine = new CraftingEngine(masterContext.client, masterContext.input);
    }

    @Override
    public SkillId id() {
        return SkillId.CRAFTING;
    }

    @Override
    public int currentLevel() {
        return LevelUtil.level(Skill.CRAFTING);
    }

    @Override
    public int targetLevel() {
        return this.ctx.config.craftingTarget();
    }

    @Override
    public void reset() {
        super.reset();
        this.engine.reset();
        this.setupStarted = false;
        this.gemsConfirmed = false;
    }

    @Override
    public int tick() {
        switch (this.phase) {
            case SETUP: {
                int n;
                if (!this.gemsConfirmed) {
                    n = this.ctx.bank.openStep(this.ctx.local);
                    if (n != Integer.MIN_VALUE) {
                        this.status = "Crafting: " + this.ctx.bank.status();
                        return n;
                    }
                    if (!this.engine.hasCuttableGems()) {
                        this.ctx.bank.close();
                        return this.skip("Crafting: no gems to cut");
                    }
                    this.gemsConfirmed = true;
                }
                if (!this.setupStarted) {
                    this.ctx.acquirer.begin(1755, 1, true, false);
                    this.setupStarted = true;
                }
                n = this.ctx.acquirer.step(this.ctx.local);
                this.status = "Chisel: " + this.ctx.acquirer.status();
                if (this.ctx.acquirer.isDone()) {
                    this.setupStarted = false;
                    this.phase = JobPhase.TRAVEL;
                    return 400;
                }
                return n;
            }
            case TRAVEL: {
                int n = this.ctx.nav.ensureHome(this.ctx.local);
                this.status = "Travel home: " + this.ctx.nav.status();
                if (n == Integer.MIN_VALUE) {
                    this.engine.reset();
                    this.phase = JobPhase.TRAIN;
                    return 600;
                }
                return n;
            }
            case TRAIN: {
                int n = this.engine.tick(this.ctx.local, new CraftingEngine.Settings(true));
                this.status = "Crafting: " + this.engine.getStatus();
                if (n < 0) {
                    return this.skip("Crafting: no gems to cut");
                }
                return n;
            }
        }
        return 600;
    }
}


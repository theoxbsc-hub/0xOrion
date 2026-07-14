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
import net.runelite.client.plugins.svc.smithing.SmithingEngine;
import net.runelite.client.plugins.svc.smithing.SmithingMode;

public final class SmithingJob
extends SkillJob {
    private final SmithingEngine engine;
    private boolean setupStarted = false;

    public SmithingJob(MasterContext masterContext) {
        super(masterContext);
        this.engine = new SmithingEngine(masterContext.client, masterContext.input);
    }

    @Override
    public SkillId id() {
        return SkillId.SMITHING;
    }

    @Override
    public int currentLevel() {
        return LevelUtil.level(Skill.SMITHING);
    }

    @Override
    public int targetLevel() {
        return this.ctx.config.smithingTarget();
    }

    @Override
    public void reset() {
        super.reset();
        this.engine.reset();
        this.setupStarted = false;
    }

    @Override
    public void onChat(String string) {
        this.engine.onChatMessage(string);
    }

    @Override
    public int tick() {
        switch (this.phase) {
            case SETUP: {
                if (this.owns(2347)) {
                    this.phase = JobPhase.TRAVEL;
                    return 300;
                }
                if (!this.setupStarted) {
                    this.ctx.acquirer.begin(2347, 1, true, true);
                    this.setupStarted = true;
                }
                int n = this.ctx.acquirer.step(this.ctx.local);
                this.status = "Hammer: " + this.ctx.acquirer.status();
                if (this.ctx.acquirer.isDone()) {
                    this.setupStarted = false;
                    if (!this.owns(2347)) {
                        return this.skip("Smithing: could not obtain hammer");
                    }
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
                int n = this.engine.tick(this.ctx.local, new SmithingEngine.Settings(SmithingMode.AUTO, true, 180));
                this.status = "Smithing: " + this.engine.getStatus();
                if (n < 0) {
                    return this.skip("Smithing: out of materials");
                }
                return n;
            }
        }
        return 600;
    }
}


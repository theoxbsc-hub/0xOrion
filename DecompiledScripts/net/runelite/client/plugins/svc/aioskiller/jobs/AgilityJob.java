/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Skill
 */
package net.runelite.client.plugins.svc.aioskiller.jobs;

import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.agility.AgilityCourse;
import net.runelite.client.plugins.svc.agility.AgilityEngine;
import net.runelite.client.plugins.svc.aioskiller.JobPhase;
import net.runelite.client.plugins.svc.aioskiller.MasterContext;
import net.runelite.client.plugins.svc.aioskiller.SkillId;
import net.runelite.client.plugins.svc.aioskiller.SkillJob;
import net.runelite.client.plugins.svc.aioskiller.services.LevelUtil;

public final class AgilityJob
extends SkillJob {
    private final AgilityEngine engine;
    private String traveledTerm = null;

    public AgilityJob(MasterContext masterContext) {
        super(masterContext);
        this.engine = new AgilityEngine(masterContext.client, masterContext.input);
    }

    @Override
    public SkillId id() {
        return SkillId.AGILITY;
    }

    @Override
    public int currentLevel() {
        return LevelUtil.level(Skill.AGILITY);
    }

    @Override
    public int targetLevel() {
        return this.ctx.config.agilityTarget();
    }

    @Override
    public void reset() {
        super.reset();
        this.engine.reset();
        this.traveledTerm = null;
    }

    private String termForLevel() {
        return this.currentLevel() < 50 ? "Skoblin Course" : "Arctic";
    }

    private AgilityCourse courseForLevel() {
        return this.currentLevel() < 50 ? AgilityCourse.GNOME_LVL1 : AgilityCourse.ICE_LVL50;
    }

    @Override
    public int tick() {
        switch (this.phase) {
            case SETUP: {
                this.ctx.nav.beginGlider();
                this.phase = JobPhase.TRAVEL;
                return 200;
            }
            case TRAVEL: {
                String string = this.termForLevel();
                int n = this.ctx.nav.gliderTo(string, this.ctx.local);
                this.status = "Travel (" + string + "): " + this.ctx.nav.status();
                if (n == Integer.MIN_VALUE) {
                    this.traveledTerm = string;
                    this.engine.reset();
                    this.phase = JobPhase.TRAIN;
                    return 600;
                }
                return n;
            }
            case TRAIN: {
                if (!this.termForLevel().equals(this.traveledTerm)) {
                    this.ctx.nav.beginGlider();
                    this.phase = JobPhase.TRAVEL;
                    return 300;
                }
                int n = this.engine.tick(this.ctx.local, new AgilityEngine.Settings(this.courseForLevel(), true));
                if (this.engine.needsRecovery()) {
                    this.engine.clearRecovery();
                    this.ctx.nav.beginGlider();
                    this.traveledTerm = null;
                    this.phase = JobPhase.TRAVEL;
                    this.status = "Agility: off-course \u2014 re-travelling to course start";
                    return 300;
                }
                this.status = "Agility: " + this.engine.getStatus();
                return n < 0 ? 600 : n;
            }
        }
        return 600;
    }
}


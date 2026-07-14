/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.GameState
 *  net.runelite.api.Skill
 */
package net.runelite.client.plugins.svc.aioskiller.jobs;

import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.aioskiller.JobPhase;
import net.runelite.client.plugins.svc.aioskiller.MasterContext;
import net.runelite.client.plugins.svc.aioskiller.SkillId;
import net.runelite.client.plugins.svc.aioskiller.SkillJob;
import net.runelite.client.plugins.svc.aioskiller.services.LevelUtil;
import net.runelite.client.plugins.svc.hunter.BirdSnareHunterEngine;

public final class HunterJob
extends SkillJob {
    private final BirdSnareHunterEngine engine;
    private boolean setupStarted = false;
    private boolean relocating = false;
    private String traveledTerm = null;

    public HunterJob(MasterContext masterContext) {
        super(masterContext);
        this.engine = new BirdSnareHunterEngine(masterContext.client, masterContext.input);
    }

    @Override
    public SkillId id() {
        return SkillId.HUNTER;
    }

    @Override
    public int currentLevel() {
        return LevelUtil.level(Skill.HUNTER);
    }

    @Override
    public int targetLevel() {
        return this.ctx.config.hunterTarget();
    }

    @Override
    public void reset() {
        super.reset();
        this.engine.reset();
        this.setupStarted = false;
        this.relocating = false;
        this.traveledTerm = null;
    }

    @Override
    public void onChat(String string) {
        this.engine.onChatMessage(string);
    }

    @Override
    public void onGameState(GameState gameState) {
        if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.CONNECTION_LOST) {
            this.engine.onSceneLost();
        }
    }

    private String termForLevel() {
        return this.currentLevel() < 19 ? "Crimson" : "Tropical wag";
    }

    private String birdsForLevel() {
        return this.currentLevel() < 19 ? "Crimson swift" : "Tropical wagtail";
    }

    private BirdSnareHunterEngine.Settings settings() {
        return new BirdSnareHunterEngine.Settings(this.birdsForLevel(), 3, 1, 10, true, "", "Bird snare");
    }

    @Override
    public int tick() {
        switch (this.phase) {
            case SETUP: {
                if (!this.setupStarted) {
                    this.ctx.acquirer.begin(10006, 3, true, true);
                    this.setupStarted = true;
                }
                int n = this.ctx.acquirer.step(this.ctx.local);
                this.status = "Snares: " + this.ctx.acquirer.status();
                if (this.ctx.acquirer.isDone()) {
                    this.setupStarted = false;
                    if (!this.owns(10006)) {
                        return this.skip("Hunter: could not obtain bird snares");
                    }
                    this.ctx.nav.beginGlider();
                    this.phase = JobPhase.TRAVEL;
                    return 400;
                }
                return n;
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
                if (this.relocating) {
                    int n = this.engine.recallStep(this.ctx.local);
                    this.status = "Recalling snares: " + this.engine.getStatus();
                    if (n < 0) {
                        this.relocating = false;
                        this.ctx.nav.beginGlider();
                        this.phase = JobPhase.TRAVEL;
                        return 400;
                    }
                    return n;
                }
                if (!this.termForLevel().equals(this.traveledTerm)) {
                    this.relocating = true;
                    return 300;
                }
                int n = this.engine.tick(this.ctx.local, this.settings());
                this.status = "Hunter: " + this.engine.getStatus();
                return n < 0 ? 600 : n;
            }
        }
        return 600;
    }
}


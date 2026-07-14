/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Skill
 *  net.runelite.api.coords.WorldPoint
 */
package net.runelite.client.plugins.svc.aioskiller.jobs;

import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConstants;
import net.runelite.client.plugins.svc.aioskiller.JobPhase;
import net.runelite.client.plugins.svc.aioskiller.MasterContext;
import net.runelite.client.plugins.svc.aioskiller.SkillId;
import net.runelite.client.plugins.svc.aioskiller.SkillJob;
import net.runelite.client.plugins.svc.aioskiller.services.LevelUtil;
import net.runelite.client.plugins.svc.aioskiller.services.Walk;
import net.runelite.client.plugins.svc.thieving.StallThiefEngine;

public final class ThievingJob
extends SkillJob {
    private static final int MAX_SWITCH_SELL_SESSIONS = 6;
    private static final int STALL_ARRIVE_DIST = 5;
    private final StallThiefEngine engine;
    private boolean selling = false;
    private boolean switchingStalls = false;
    private boolean switchRelocated = false;
    private int switchSellSessions = 0;
    private boolean soldFinal = false;
    private boolean finalSellDone = false;
    private StallThiefEngine.StallTarget currentStall = null;

    public ThievingJob(MasterContext masterContext) {
        super(masterContext);
        this.engine = new StallThiefEngine(masterContext.client, masterContext.input);
        this.engine.setNeverSellIds(AioSkillerConstants.COIN_POUCH_IDS);
    }

    @Override
    public SkillId id() {
        return SkillId.THIEVING;
    }

    @Override
    public int currentLevel() {
        return LevelUtil.level(Skill.THIEVING);
    }

    @Override
    public int targetLevel() {
        return this.ctx.config.thievingTarget();
    }

    @Override
    public void reset() {
        super.reset();
        this.engine.reset();
        this.selling = false;
        this.switchingStalls = false;
        this.switchRelocated = false;
        this.switchSellSessions = 0;
        this.soldFinal = false;
        this.finalSellDone = false;
        this.currentStall = null;
    }

    @Override
    public void onChat(String string) {
        this.engine.onChatMessage(string);
    }

    @Override
    public boolean isComplete() {
        return this.skipRequested || this.currentLevel() >= this.targetLevel() && this.finalSellDone;
    }

    @Override
    public int tick() {
        switch (this.phase) {
            case SETUP: {
                this.phase = JobPhase.TRAVEL;
                return 200;
            }
            case TRAVEL: {
                int n = this.ctx.nav.ensureHome(this.ctx.local);
                this.status = "Travel home: " + this.ctx.nav.status();
                if (n == Integer.MIN_VALUE) {
                    this.phase = JobPhase.TRAIN;
                    return 600;
                }
                return n;
            }
            case TRAIN: {
                StallThiefEngine.StallTarget stallTarget = StallThiefEngine.bestStallForLevel(this.currentLevel());
                if (this.currentStall == null) {
                    this.currentStall = stallTarget;
                }
                if (this.currentLevel() >= this.targetLevel()) {
                    if (!this.soldFinal) {
                        int n = this.engine.sellAllStep(this.ctx.local, this.currentStall.traderNpcId);
                        this.status = "Final sell: " + this.engine.getStatus();
                        if (this.engine.isSellingDone()) {
                            this.soldFinal = true;
                            return 400;
                        }
                        return n;
                    }
                    if (this.ctx.bank.openNextCoinPouch()) {
                        this.status = "Opening coin pouches";
                        return 600;
                    }
                    this.finalSellDone = true;
                    return 600;
                }
                if (this.switchingStalls || stallTarget.objectId != this.currentStall.objectId) {
                    if (!this.switchingStalls) {
                        this.switchingStalls = true;
                        this.switchRelocated = false;
                    }
                    if (!this.switchRelocated && this.switchSellSessions == 0 && !this.selling) {
                        WorldPoint worldPoint = this.ctx.local.getWorldLocation();
                        if (worldPoint != null && !Walk.arrived(worldPoint, this.currentStall.coords(), 5)) {
                            this.status = "Switching \u2014 returning to " + this.currentStall.label + " stall to sell";
                            this.ctx.input.walkToward(worldPoint, this.currentStall.coords());
                            return 1000;
                        }
                        this.switchRelocated = true;
                    }
                    this.selling = true;
                    int n = this.engine.sellAllStep(this.ctx.local, this.currentStall.traderNpcId);
                    this.status = "Switching stall \u2014 selling at " + this.currentStall.label + " (" + this.switchSellSessions + "/6): " + this.engine.getStatus();
                    if (this.engine.isSellingDone()) {
                        if (this.engine.hasSellableItems() && this.switchSellSessions < 6) {
                            ++this.switchSellSessions;
                            return 300;
                        }
                        if (this.ctx.bank.openNextCoinPouch()) {
                            this.status = "Switching stall \u2014 emptying coin pouches";
                            return 600;
                        }
                        this.engine.clearSellBlacklist();
                        this.switchingStalls = false;
                        this.switchRelocated = false;
                        this.selling = false;
                        this.switchSellSessions = 0;
                        this.currentStall = stallTarget;
                        return 400;
                    }
                    return n;
                }
                if (this.engine.inventoryFull() || this.selling) {
                    this.selling = true;
                    int n = this.engine.sellAllStep(this.ctx.local, this.currentStall.traderNpcId);
                    this.status = "Selling: " + this.engine.getStatus();
                    if (this.engine.isSellingDone()) {
                        if (this.ctx.bank.openNextCoinPouch()) {
                            this.status = "Emptying coin pouches";
                            return 600;
                        }
                        this.selling = false;
                        return 400;
                    }
                    return n;
                }
                WorldPoint worldPoint = this.ctx.local.getWorldLocation();
                if (worldPoint != null && !Walk.arrived(worldPoint, this.currentStall.coords(), 5)) {
                    this.status = "Walking to " + this.currentStall.label + " stall";
                    this.ctx.input.walkToward(worldPoint, this.currentStall.coords());
                    return 1000;
                }
                int n = this.engine.stealStep(this.ctx.local, this.currentStall.objectId);
                this.status = "Thieving (" + this.currentStall.label + "): " + this.engine.getStatus();
                return n;
            }
        }
        return 600;
    }
}


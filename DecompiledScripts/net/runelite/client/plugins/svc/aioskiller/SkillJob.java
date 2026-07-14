/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.EquipmentInventorySlot
 *  net.runelite.api.GameState
 *  net.runelite.api.Item
 */
package net.runelite.client.plugins.svc.aioskiller;

import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConstants;
import net.runelite.client.plugins.svc.aioskiller.JobPhase;
import net.runelite.client.plugins.svc.aioskiller.MasterContext;
import net.runelite.client.plugins.svc.aioskiller.SkillId;
import net.runelite.client.plugins.svc.aioskiller.SkillerLog;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;

public abstract class SkillJob {
    protected final MasterContext ctx;
    protected JobPhase phase = JobPhase.SETUP;
    protected String status = "Init";
    protected boolean skipRequested = false;

    protected SkillJob(MasterContext masterContext) {
        this.ctx = masterContext;
    }

    public abstract SkillId id();

    public abstract int currentLevel();

    public abstract int targetLevel();

    public abstract int tick();

    public boolean isComplete() {
        return this.skipRequested || this.currentLevel() >= this.targetLevel();
    }

    public void reset() {
        this.phase = JobPhase.SETUP;
        this.skipRequested = false;
        this.status = "Starting " + this.id().displayName();
    }

    public void onChat(String string) {
    }

    public void onGameState(GameState gameState) {
    }

    public boolean watchdogSuppressed() {
        return false;
    }

    public boolean requiresEquipment() {
        return false;
    }

    public String status() {
        return this.status;
    }

    public JobPhase phase() {
        return this.phase;
    }

    protected int skip(String string) {
        this.ctx.log.record(SkillerLog.Kind.SKILL_SKIPPED, this.id().displayName(), this.phase.name(), string, null);
        this.skipRequested = true;
        this.status = string;
        this.phase = JobPhase.SKIP;
        return 600;
    }

    protected boolean owns(int n) {
        if (Inventory.getFirst(n) != null) {
            return true;
        }
        for (EquipmentInventorySlot equipmentInventorySlot : EquipmentInventorySlot.values()) {
            Item item = Equipment.fromSlot(equipmentInventorySlot);
            if (item == null || item.getId() != n) continue;
            return true;
        }
        String string = AioSkillerConstants.knownName(n);
        if (string != null) {
            if (Inventory.getFirst(string) != null) {
                return true;
            }
            for (EquipmentInventorySlot equipmentInventorySlot : EquipmentInventorySlot.values()) {
                Item item = Equipment.fromSlot(equipmentInventorySlot);
                if (item == null || !string.equalsIgnoreCase(item.getName())) continue;
                return true;
            }
        }
        return false;
    }
}


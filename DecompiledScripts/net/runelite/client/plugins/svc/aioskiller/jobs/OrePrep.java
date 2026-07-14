/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Item
 *  net.runelite.api.Skill
 */
package net.runelite.client.plugins.svc.aioskiller.jobs;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import net.runelite.api.Item;
import net.runelite.api.Skill;
import net.runelite.client.plugins.svc.mining.MiningOre;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;

final class OrePrep {
    private static final Quota[] QUOTAS = new Quota[]{new Quota(MiningOre.COPPER, 436, 100), new Quota(MiningOre.TIN, 438, 100), new Quota(MiningOre.IRON, 440, 200), new Quota(MiningOre.COAL, 453, 1400), new Quota(MiningOre.MITHRIL, 447, 300)};
    private static final long ORE_UNAVAILABLE_MS = 30000L;
    private final Map<MiningOre, Integer> bankedCache = new EnumMap<MiningOre, Integer>(MiningOre.class);
    private final EnumSet<MiningOre> unavailable = EnumSet.noneOf(MiningOre.class);
    private MiningOre activeOre = null;
    private boolean prepActive = false;
    private String summary = "";
    private int scalePercent = 100;
    private int lastXp = -1;
    private long lastXpRiseMs = 0L;

    OrePrep() {
    }

    MiningOre decide(MiningOre miningOre, boolean bl, String string, int n) {
        this.scalePercent = Math.max(1, n);
        if (!bl) {
            this.prepActive = false;
            this.activeOre = null;
            this.summary = "";
            return miningOre;
        }
        long l = System.currentTimeMillis();
        int n2 = this.miningXp();
        boolean bl2 = "MINING".equals(string);
        if (this.lastXp < 0) {
            this.lastXp = n2;
            this.lastXpRiseMs = l;
        }
        if (n2 > this.lastXp) {
            this.lastXp = n2;
            this.lastXpRiseMs = l;
        }
        if (!bl2 || this.activeOre == null || this.activeOre.isAny()) {
            this.lastXpRiseMs = l;
        }
        if (this.activeOre != null && !this.activeOre.isAny() && bl2 && l - this.lastXpRiseMs > 30000L) {
            this.unavailable.add(this.activeOre);
            this.activeOre = null;
        }
        if (this.activeOre != null && !this.activeOre.isAny() && this.invCount(this.activeOre) > 0) {
            this.prepActive = true;
            return this.activeOre;
        }
        this.refreshBankCache();
        int n3 = this.miningLevel();
        Quota quota = null;
        boolean bl3 = false;
        for (Quota quota2 : QUOTAS) {
            if (this.unavailable.contains((Object)quota2.ore) || this.secured(quota2) >= this.need(quota2)) continue;
            if (n3 >= quota2.ore.getTier()) {
                quota = quota2;
                break;
            }
            bl3 = true;
        }
        if (quota != null) {
            this.activeOre = quota.ore;
            this.prepActive = true;
            int n4 = this.need(quota);
            this.summary = "Ore prep: " + String.valueOf((Object)quota.ore) + " " + Math.min(this.secured(quota), n4) + "/" + n4;
            return this.activeOre;
        }
        if (bl3) {
            this.activeOre = MiningOre.ANY;
            this.prepActive = true;
            this.summary = "Ore prep: leveling up to unlock the next ore";
            return MiningOre.ANY;
        }
        this.prepActive = false;
        this.activeOre = null;
        this.summary = "";
        return miningOre;
    }

    boolean prepActive() {
        return this.prepActive;
    }

    String progressSummary() {
        return this.summary;
    }

    boolean primeFromBank() {
        if (!this.safeBankOpen()) {
            return false;
        }
        this.refreshBankCache();
        return true;
    }

    void reset() {
        this.bankedCache.clear();
        this.unavailable.clear();
        this.activeOre = null;
        this.prepActive = false;
        this.summary = "";
        this.scalePercent = 100;
        this.lastXp = -1;
        this.lastXpRiseMs = 0L;
    }

    private int need(Quota quota) {
        if (this.scalePercent == 100) {
            return quota.need;
        }
        return (int)(((long)quota.need * (long)this.scalePercent + 99L) / 100L);
    }

    private int secured(Quota quota) {
        Integer n = this.bankedCache.get((Object)quota.ore);
        return (n == null ? 0 : n) + this.invCountId(quota.itemId);
    }

    private void refreshBankCache() {
        if (!this.safeBankOpen()) {
            return;
        }
        for (Quota quota : QUOTAS) {
            this.bankedCache.put(quota.ore, this.bankQty(quota.itemId));
        }
    }

    private boolean safeBankOpen() {
        try {
            return Bank.isOpen();
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private int bankQty(int n) {
        try {
            Item item = Bank.getFirst(n);
            return item == null ? 0 : item.getQuantity();
        }
        catch (Throwable throwable) {
            return 0;
        }
    }

    private int invCount(MiningOre miningOre) {
        Quota quota = this.quotaFor(miningOre);
        return quota == null ? 0 : this.invCountId(quota.itemId);
    }

    private int invCountId(int n) {
        try {
            return Inventory.getCount(n);
        }
        catch (Throwable throwable) {
            return 0;
        }
    }

    private Quota quotaFor(MiningOre miningOre) {
        for (Quota quota : QUOTAS) {
            if (quota.ore != miningOre) continue;
            return quota;
        }
        return null;
    }

    private int miningXp() {
        try {
            return Skills.getExperience(Skill.MINING);
        }
        catch (Throwable throwable) {
            return this.lastXp < 0 ? 0 : this.lastXp;
        }
    }

    private int miningLevel() {
        try {
            int n = Skills.getBoostedLevel(Skill.MINING);
            return n > 0 ? n : Skills.getLevel(Skill.MINING);
        }
        catch (Throwable throwable) {
            return 1;
        }
    }

    private static final class Quota {
        final MiningOre ore;
        final int itemId;
        final int need;

        Quota(MiningOre miningOre, int n, int n2) {
            this.ore = miningOre;
            this.itemId = n;
            this.need = n2;
        }
    }
}


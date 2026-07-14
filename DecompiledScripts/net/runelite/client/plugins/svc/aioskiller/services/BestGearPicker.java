/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.EquipmentInventorySlot
 *  net.runelite.api.ItemComposition
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.ItemComposition;
import net.runelite.client.plugins.svc.aioskiller.services.BankService;
import net.runelite.client.plugins.svc.aioskiller.services.GearCatalog;
import net.runelite.client.plugins.svc.aioskiller.services.LevelUtil;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BestGearPicker {
    private static final Logger log = LoggerFactory.getLogger(BestGearPicker.class);
    private final Set<Integer> ownedCatalogIds = new LinkedHashSet<Integer>();
    private String lastSummary = "";
    private boolean catalogLogged = false;

    public String lastSummary() {
        return this.lastSummary;
    }

    public int[] compute(Client client, BankService bankService) {
        this.logCatalogOnce();
        this.ownedCatalogIds.clear();
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        StringBuilder stringBuilder = new StringBuilder();
        boolean bl = false;
        EquipmentInventorySlot[] objectArray = GearCatalog.SLOTS;
        int n = objectArray.length;
        for (int i = 0; i < n; ++i) {
            EquipmentInventorySlot equipmentInventorySlot = objectArray[i];
            GearCatalog.Piece piece = null;
            for (GearCatalog.Piece piece2 : GearCatalog.forSlot(equipmentInventorySlot)) {
                if (!BestGearPicker.ownsAnywhere(piece2.id, bankService)) continue;
                this.ownedCatalogIds.add(piece2.id);
                if (LevelUtil.level(piece2.req) < piece2.reqLevel || piece != null && piece2.tier <= piece.tier) continue;
                piece = piece2;
            }
            if (piece == null) continue;
            arrayList.add(piece.id);
            if (equipmentInventorySlot == EquipmentInventorySlot.WEAPON) {
                bl = true;
            }
            this.appendChoice(stringBuilder, client, equipmentInventorySlot, piece);
        }
        if (!bl) {
            this.lastSummary = "no catalogue weapon owned \u2014 falling back to default kit";
            log.info("[AioSkiller] best-gear: {}", (Object)this.lastSummary);
            return null;
        }
        int[] nArray = new int[arrayList.size()];
        for (n = 0; n < nArray.length; ++n) {
            nArray[n] = (Integer)arrayList.get(n);
        }
        this.lastSummary = stringBuilder.toString();
        log.info("[AioSkiller] best-gear chosen \u2192 {}", (Object)this.lastSummary);
        return nArray;
    }

    public long signature() {
        long l = 0L;
        for (EquipmentInventorySlot equipmentInventorySlot : GearCatalog.SLOTS) {
            int n = 0;
            for (GearCatalog.Piece piece : GearCatalog.forSlot(equipmentInventorySlot)) {
                if (!this.ownedCatalogIds.contains(piece.id) || LevelUtil.level(piece.req) < piece.reqLevel || piece.tier <= n) continue;
                n = piece.tier;
            }
            l = l * 31L + (long)n;
        }
        return l;
    }

    private static boolean ownsAnywhere(int n, BankService bankService) {
        try {
            if (Inventory.contains(n)) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            if (Equipment.contains(n)) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            if (bankService != null && bankService.has(n)) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    private void appendChoice(StringBuilder stringBuilder, Client client, EquipmentInventorySlot equipmentInventorySlot, GearCatalog.Piece piece) {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(" | ");
        }
        stringBuilder.append(BestGearPicker.slotLabel(equipmentInventorySlot)).append('=').append(BestGearPicker.liveName(client, piece)).append('(').append(piece.id).append(')');
    }

    private static String liveName(Client client, GearCatalog.Piece piece) {
        if (client != null) {
            try {
                ItemComposition itemComposition = client.getItemDefinition(piece.id);
                if (itemComposition != null && itemComposition.getName() != null && !itemComposition.getName().isEmpty() && !"null".equalsIgnoreCase(itemComposition.getName())) {
                    return itemComposition.getName();
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return piece.label;
    }

    private static String slotLabel(EquipmentInventorySlot equipmentInventorySlot) {
        switch (equipmentInventorySlot) {
            case HEAD: {
                return "head";
            }
            case BODY: {
                return "body";
            }
            case LEGS: {
                return "legs";
            }
            case SHIELD: {
                return "shield";
            }
            case WEAPON: {
                return "weapon";
            }
            case BOOTS: {
                return "boots";
            }
        }
        return equipmentInventorySlot.name().toLowerCase();
    }

    private void logCatalogOnce() {
        if (this.catalogLogged) {
            return;
        }
        this.catalogLogged = true;
        StringBuilder stringBuilder = new StringBuilder();
        for (GearCatalog.Piece piece : GearCatalog.all()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(piece.label).append('=').append(piece.id).append("[req ").append(piece.reqLevel).append(' ').append(piece.req.name()).append(']');
        }
        log.info("[AioSkiller] best-gear catalogue: {}", (Object)stringBuilder);
    }
}


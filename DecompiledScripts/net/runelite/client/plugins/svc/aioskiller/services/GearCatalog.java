/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.EquipmentInventorySlot
 *  net.runelite.api.Skill
 */
package net.runelite.client.plugins.svc.aioskiller.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Skill;

public final class GearCatalog {
    private static final int T_BRONZE = 1;
    private static final int T_IRON = 2;
    private static final int T_STEEL = 3;
    private static final int T_BLACK = 4;
    private static final int T_MITHRIL = 5;
    private static final int T_ADAMANT = 6;
    private static final int T_RUNE = 7;
    private static final int T_DRAGON = 8;
    private static final int T_WHIP = 9;
    private static final int L_BRONZE = 1;
    private static final int L_IRON = 1;
    private static final int L_STEEL = 5;
    private static final int L_BLACK = 10;
    private static final int L_MITHRIL = 20;
    private static final int L_ADAMANT = 30;
    private static final int L_RUNE = 40;
    private static final int L_DRAGON = 60;
    private static final int L_WHIP = 70;
    private static final List<Piece> ALL = new ArrayList<Piece>();
    public static final EquipmentInventorySlot[] SLOTS;

    private GearCatalog() {
    }

    private static void add(int n, EquipmentInventorySlot equipmentInventorySlot, Skill skill, int n2, int n3, String string) {
        ALL.add(new Piece(n, equipmentInventorySlot, skill, n2, n3, string));
    }

    public static List<Piece> forSlot(EquipmentInventorySlot equipmentInventorySlot) {
        ArrayList<Piece> arrayList = new ArrayList<Piece>();
        for (Piece piece3 : ALL) {
            if (piece3.slot != equipmentInventorySlot) continue;
            arrayList.add(piece3);
        }
        arrayList.sort((piece, piece2) -> Integer.compare(piece2.tier, piece.tier));
        return arrayList;
    }

    public static List<Piece> all() {
        return Collections.unmodifiableList(ALL);
    }

    static {
        GearCatalog.add(1321, EquipmentInventorySlot.WEAPON, Skill.ATTACK, 1, 1, "Bronze scimitar");
        GearCatalog.add(1323, EquipmentInventorySlot.WEAPON, Skill.ATTACK, 1, 2, "Iron scimitar");
        GearCatalog.add(1325, EquipmentInventorySlot.WEAPON, Skill.ATTACK, 5, 3, "Steel scimitar");
        GearCatalog.add(1331, EquipmentInventorySlot.WEAPON, Skill.ATTACK, 10, 4, "Black scimitar");
        GearCatalog.add(1329, EquipmentInventorySlot.WEAPON, Skill.ATTACK, 20, 5, "Mithril scimitar");
        GearCatalog.add(1327, EquipmentInventorySlot.WEAPON, Skill.ATTACK, 30, 6, "Adamant scimitar");
        GearCatalog.add(1333, EquipmentInventorySlot.WEAPON, Skill.ATTACK, 40, 7, "Rune scimitar");
        GearCatalog.add(4587, EquipmentInventorySlot.WEAPON, Skill.ATTACK, 60, 8, "Dragon scimitar");
        GearCatalog.add(4151, EquipmentInventorySlot.WEAPON, Skill.ATTACK, 70, 9, "Abyssal whip");
        GearCatalog.add(1155, EquipmentInventorySlot.HEAD, Skill.DEFENCE, 1, 1, "Bronze full helm");
        GearCatalog.add(1153, EquipmentInventorySlot.HEAD, Skill.DEFENCE, 1, 2, "Iron full helm");
        GearCatalog.add(1157, EquipmentInventorySlot.HEAD, Skill.DEFENCE, 5, 3, "Steel full helm");
        GearCatalog.add(1165, EquipmentInventorySlot.HEAD, Skill.DEFENCE, 10, 4, "Black full helm");
        GearCatalog.add(1159, EquipmentInventorySlot.HEAD, Skill.DEFENCE, 20, 5, "Mithril full helm");
        GearCatalog.add(1161, EquipmentInventorySlot.HEAD, Skill.DEFENCE, 30, 6, "Adamant full helm");
        GearCatalog.add(1163, EquipmentInventorySlot.HEAD, Skill.DEFENCE, 40, 7, "Rune full helm");
        GearCatalog.add(1149, EquipmentInventorySlot.HEAD, Skill.DEFENCE, 60, 8, "Dragon med helm");
        GearCatalog.add(1117, EquipmentInventorySlot.BODY, Skill.DEFENCE, 1, 1, "Bronze platebody");
        GearCatalog.add(1115, EquipmentInventorySlot.BODY, Skill.DEFENCE, 1, 2, "Iron platebody");
        GearCatalog.add(1119, EquipmentInventorySlot.BODY, Skill.DEFENCE, 5, 3, "Steel platebody");
        GearCatalog.add(1125, EquipmentInventorySlot.BODY, Skill.DEFENCE, 10, 4, "Black platebody");
        GearCatalog.add(1121, EquipmentInventorySlot.BODY, Skill.DEFENCE, 20, 5, "Mithril platebody");
        GearCatalog.add(1123, EquipmentInventorySlot.BODY, Skill.DEFENCE, 30, 6, "Adamant platebody");
        GearCatalog.add(1127, EquipmentInventorySlot.BODY, Skill.DEFENCE, 40, 7, "Rune platebody");
        GearCatalog.add(3140, EquipmentInventorySlot.BODY, Skill.DEFENCE, 60, 8, "Dragon chainbody");
        GearCatalog.add(1075, EquipmentInventorySlot.LEGS, Skill.DEFENCE, 1, 1, "Bronze platelegs");
        GearCatalog.add(1067, EquipmentInventorySlot.LEGS, Skill.DEFENCE, 1, 2, "Iron platelegs");
        GearCatalog.add(1069, EquipmentInventorySlot.LEGS, Skill.DEFENCE, 5, 3, "Steel platelegs");
        GearCatalog.add(1077, EquipmentInventorySlot.LEGS, Skill.DEFENCE, 10, 4, "Black platelegs");
        GearCatalog.add(1071, EquipmentInventorySlot.LEGS, Skill.DEFENCE, 20, 5, "Mithril platelegs");
        GearCatalog.add(1073, EquipmentInventorySlot.LEGS, Skill.DEFENCE, 30, 6, "Adamant platelegs");
        GearCatalog.add(1079, EquipmentInventorySlot.LEGS, Skill.DEFENCE, 40, 7, "Rune platelegs");
        GearCatalog.add(4087, EquipmentInventorySlot.LEGS, Skill.DEFENCE, 60, 8, "Dragon platelegs");
        GearCatalog.add(1189, EquipmentInventorySlot.SHIELD, Skill.DEFENCE, 1, 1, "Bronze kiteshield");
        GearCatalog.add(1191, EquipmentInventorySlot.SHIELD, Skill.DEFENCE, 1, 2, "Iron kiteshield");
        GearCatalog.add(1193, EquipmentInventorySlot.SHIELD, Skill.DEFENCE, 5, 3, "Steel kiteshield");
        GearCatalog.add(1199, EquipmentInventorySlot.SHIELD, Skill.DEFENCE, 10, 4, "Black kiteshield");
        GearCatalog.add(1197, EquipmentInventorySlot.SHIELD, Skill.DEFENCE, 20, 5, "Mithril kiteshield");
        GearCatalog.add(1195, EquipmentInventorySlot.SHIELD, Skill.DEFENCE, 30, 6, "Adamant kiteshield");
        GearCatalog.add(1201, EquipmentInventorySlot.SHIELD, Skill.DEFENCE, 40, 7, "Rune kiteshield");
        GearCatalog.add(1187, EquipmentInventorySlot.SHIELD, Skill.DEFENCE, 60, 8, "Dragon sq shield");
        GearCatalog.add(3105, EquipmentInventorySlot.BOOTS, Skill.DEFENCE, 1, 1, "Climbing boots");
        GearCatalog.add(11840, EquipmentInventorySlot.BOOTS, Skill.DEFENCE, 60, 8, "Dragon boots");
        SLOTS = new EquipmentInventorySlot[]{EquipmentInventorySlot.HEAD, EquipmentInventorySlot.BODY, EquipmentInventorySlot.LEGS, EquipmentInventorySlot.SHIELD, EquipmentInventorySlot.WEAPON, EquipmentInventorySlot.BOOTS};
    }

    public static final class Piece {
        public final int id;
        public final EquipmentInventorySlot slot;
        public final Skill req;
        public final int reqLevel;
        public final int tier;
        public final String label;

        Piece(int n, EquipmentInventorySlot equipmentInventorySlot, Skill skill, int n2, int n3, String string) {
            this.id = n;
            this.slot = equipmentInventorySlot;
            this.req = skill;
            this.reqLevel = n2;
            this.tier = n3;
            this.label = string;
        }
    }
}


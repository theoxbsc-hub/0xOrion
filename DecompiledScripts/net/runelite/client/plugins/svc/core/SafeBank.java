/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Item
 */
package net.runelite.client.plugins.svc.core;

import net.runelite.api.Item;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.widgets.Dialog;

public final class SafeBank {
    private SafeBank() {
    }

    public static boolean isAmountPromptOpen() {
        try {
            return Dialog.isEnterInputOpen();
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public static boolean clearStrayAmountPrompt() {
        if (!SafeBank.isAmountPromptOpen()) {
            return false;
        }
        try {
            Dialog.enterAmount(1);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return true;
    }

    public static void withdrawAll(int n) {
        if (n > 0) {
            Bank.withdrawAll(n, Bank.WithdrawMode.ITEM);
        }
    }

    public static void withdrawAll(Item item) {
        if (item != null) {
            SafeBank.withdrawAll(item.getId());
        }
    }

    public static void withdrawExact(int n, int n2) {
        int n3;
        if (n <= 0 || n2 <= 0) {
            return;
        }
        int n4 = 0;
        for (int i = n2; i > 0 && n4++ < 64; i -= n3) {
            n3 = i >= 10 ? 10 : (i >= 5 ? 5 : 1);
            Bank.withdraw(n, n3, Bank.WithdrawMode.ITEM);
        }
    }
}


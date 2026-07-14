package net.runelite.client.plugins.ox.core;

import net.runelite.api.Item;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.widgets.Dialog;

/** Minimal bank withdraw helpers (no HumanInput). Package helper for ox scripts. */
public final class SafeBank {
    private SafeBank() {
    }

    public static boolean isAmountPromptOpen() {
        try {
            return Dialog.isEnterInputOpen();
        } catch (Throwable throwable) {
            return false;
        }
    }

    public static boolean clearStrayAmountPrompt() {
        if (!SafeBank.isAmountPromptOpen()) {
            return false;
        }
        try {
            Dialog.enterAmount(1);
        } catch (Throwable throwable) {
            // empty
        }
        return true;
    }

    public static void withdrawAll(int itemId) {
        if (itemId > 0) {
            Bank.withdrawAll(itemId, Bank.WithdrawMode.ITEM);
        }
    }

    public static void withdrawAll(Item item) {
        if (item != null) {
            SafeBank.withdrawAll(item.getId());
        }
    }

    public static void withdrawExact(int itemId, int amount) {
        if (itemId <= 0 || amount <= 0) {
            return;
        }
        int attempts = 0;
        for (int remaining = amount; remaining > 0 && attempts++ < 64; ) {
            int chunk = remaining >= 10 ? 10 : (remaining >= 5 ? 5 : 1);
            Bank.withdraw(itemId, chunk, Bank.WithdrawMode.ITEM);
            remaining -= chunk;
        }
    }
}

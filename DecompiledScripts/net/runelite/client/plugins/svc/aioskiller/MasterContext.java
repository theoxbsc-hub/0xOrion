/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.Client
 *  net.runelite.api.Player
 */
package net.runelite.client.plugins.svc.aioskiller;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.plugins.svc.aioskiller.AioSkillerConfig;
import net.runelite.client.plugins.svc.aioskiller.InputMode;
import net.runelite.client.plugins.svc.aioskiller.SkillerLog;
import net.runelite.client.plugins.svc.aioskiller.services.Acquirer;
import net.runelite.client.plugins.svc.aioskiller.services.BankReset;
import net.runelite.client.plugins.svc.aioskiller.services.BankService;
import net.runelite.client.plugins.svc.aioskiller.services.GearService;
import net.runelite.client.plugins.svc.aioskiller.services.Navigator;
import net.runelite.client.plugins.svc.aioskiller.services.ShopService;
import net.runelite.client.plugins.svc.core.AccountProfile;
import net.runelite.client.plugins.svc.core.HumanInput;

public final class MasterContext {
    public final Client client;
    public final AioSkillerConfig config;
    public final AccountProfile profile;
    public final HumanInput input;
    public final Navigator nav;
    public final BankService bank;
    public final ShopService shop;
    public final GearService gear;
    public final Acquirer acquirer;
    public final BankReset reset;
    public final SkillerLog log;
    public Player local;

    public MasterContext(Client client, AioSkillerConfig aioSkillerConfig, long l) {
        this.client = client;
        this.config = aioSkillerConfig;
        this.profile = new AccountProfile(client);
        this.input = new HumanInput(client, this.profile);
        this.input.setPacketsOnly(aioSkillerConfig.inputMode() == InputMode.PACKETS_ONLY);
        this.nav = new Navigator(client, this.input);
        this.bank = new BankService(client, this.input);
        this.shop = new ShopService(client, this.bank, this.nav, this.input);
        this.gear = new GearService(client, this.input);
        this.acquirer = new Acquirer(this.nav, this.bank, this.shop);
        this.reset = new BankReset(this.nav, this.bank);
        this.log = new SkillerLog(l);
    }
}


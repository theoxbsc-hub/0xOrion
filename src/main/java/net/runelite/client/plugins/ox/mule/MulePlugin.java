package net.runelite.client.plugins.ox.mule;

import com.google.inject.Binder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.ox.core.OxConsole;
import net.runelite.client.plugins.ox.core.OxLoopedPlugin;
import net.runelite.client.plugins.ox.core.OxPluginRegistry;
import net.runelite.client.plugins.ox.core.OxScriptActivity;
import net.runelite.client.plugins.ox.core.OxSessionHistory;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.widgets.Widgets;

@PluginDescriptor(
    name = "<html><nobr><font color='#B44DFF'>0xMule</font></nobr></html>",
    description = "Standalone mule: auto-accepts incoming trades, then banks the haul at home.",
    tags = {"0x", "mule", "trade", "bank", "bot"},
    enabledByDefault = false
)
public class MulePlugin extends OxLoopedPlugin {
    private static final String SCRIPT_ID = "mule";
    private static final int BANK_APPROACH_DISTANCE = 5;

    private enum State {
        WAITING,
        ACCEPT,
        TRADE1,
        TRADE2,
        OPEN_BANK,
        DEPOSIT
    }

    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private MuleConfig config;

    private MuleOverlay overlay;
    private State state = State.WAITING;
    private String status = "Idle";
    private long sessionStartMs;
    private String sessionId = "";
    private boolean loggedInLastTick;
    private long lastHistoryFlushMs;
    private long screen1OpenedMs;
    private long lastBankInteractMs;
    private volatile String pendingTraderName;
    private String lastTrader = "-";
    private boolean screen2AcceptClicked;
    private boolean needsBank;
    private int tradesAccepted;
    private int bankTrips;
    private int itemsBanked;
    private int pendingDepositCount;

    @Override
    public void configure(Binder binder) {
        ConfigManager cm = RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(MuleConfig.class).toInstance(cm.getConfig(MuleConfig.class));
    }

    @Override
    protected void onStart() throws Exception {
        this.overlay = new MuleOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay) this.overlay);
        this.state = State.WAITING;
        this.status = "Starting";
        this.loggedInLastTick = false;
        this.sessionStartMs = System.currentTimeMillis();
        this.sessionId = UUID.randomUUID().toString();
        this.lastHistoryFlushMs = 0L;
        this.screen1OpenedMs = 0L;
        this.lastBankInteractMs = 0L;
        this.pendingTraderName = null;
        this.lastTrader = "-";
        this.screen2AcceptClicked = false;
        this.needsBank = false;
        this.tradesAccepted = 0;
        this.bankTrips = 0;
        this.itemsBanked = 0;
        this.pendingDepositCount = 0;
        OxPluginRegistry.register("0xMule");
        publishLiveStats();
        OxConsole.info("Mule", "script ready - waiting for trades");
    }

    @Override
    protected void onStop() throws Exception {
        persistSession(true);
        OxScriptActivity.clear(SCRIPT_ID);
        if (this.overlay != null) {
            this.overlayManager.remove((Overlay) this.overlay);
        }
        this.overlay = null;
        this.status = "Idle";
        this.pendingTraderName = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        GameState gs = event.getGameState();
        if (gs == GameState.LOGIN_SCREEN || gs == GameState.CONNECTION_LOST) {
            this.loggedInLastTick = false;
            persistSession(false);
        }
        if (gs == GameState.LOGGED_IN) {
            this.loggedInLastTick = false;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        String raw = event.getMessage();
        if (raw == null) {
            return;
        }
        String plain = stripTags(raw);
        if (!plain.toLowerCase(Locale.ROOT).contains("wishes to trade")) {
            return;
        }
        String fromName = event.getName();
        if (fromName != null && !fromName.isEmpty()) {
            String cleaned = cleanName(fromName);
            if (!cleaned.isEmpty()) {
                this.pendingTraderName = cleaned;
                return;
            }
        }
        int idx = plain.toLowerCase(Locale.ROOT).indexOf("wishes to trade");
        if (idx > 0) {
            String parsed = cleanName(plain.substring(0, idx));
            if (!parsed.isEmpty()) {
                this.pendingTraderName = parsed;
            }
        }
    }

    @Override
    protected int loop() {
        GameState gameState = this.client.getGameState();
        if (gameState == GameState.LOADING) {
            this.status = "Loading...";
            return 1000;
        }
        if (gameState != GameState.LOGGED_IN) {
            publishLiveStats();
            return 1500;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            OxConsole.info("Mule", "session started - listening for trades");
            publishLiveStats();
            return 1200;
        }
        if (this.client.getLocalPlayer() == null) {
            return 600;
        }

        boolean screen2 = isOpen(334, 0);
        boolean screen1 = isOpen(335, 0);

        if (screen2) {
            this.state = State.TRADE2;
            this.screen1OpenedMs = 0L;
            Widget accept = this.client.getWidget(334, 13);
            if (accept != null && !accept.isHidden()) {
                this.status = "Accepting trade screen 2";
                MousePackets.queueClickPacket();
                WidgetPackets.widgetFirstOption(accept);
                this.screen2AcceptClicked = true;
                publishLiveStats();
                return 1000;
            }
            this.status = "Screen 2 open - accept missing";
            publishLiveStats();
            return 800;
        }

        if (screen1) {
            this.state = State.TRADE1;
            long now = System.currentTimeMillis();
            if (this.screen1OpenedMs == 0L) {
                this.screen1OpenedMs = now;
                this.status = "Trade screen 1 - waiting for items...";
                publishLiveStats();
                return 600;
            }
            long delay = this.config.screen1AcceptDelayMs();
            long elapsed = now - this.screen1OpenedMs;
            if (elapsed < delay) {
                this.status = "Trade screen 1 - accepting in " + ((delay - elapsed) / 1000L) + "s";
                publishLiveStats();
                return 600;
            }
            Widget accept = this.client.getWidget(335, 10);
            if (accept != null && !accept.isHidden()) {
                this.status = "Accepting trade screen 1";
                MousePackets.queueClickPacket();
                WidgetPackets.widgetFirstOption(accept);
            } else {
                this.status = "Screen 1 open - accept missing";
            }
            publishLiveStats();
            return 800;
        }

        this.screen1OpenedMs = 0L;

        if (this.screen2AcceptClicked) {
            this.screen2AcceptClicked = false;
            this.tradesAccepted++;
            if (this.pendingTraderName != null && !this.pendingTraderName.isEmpty()) {
                this.lastTrader = this.pendingTraderName;
            }
            OxConsole.info("Mule", "trade accepted"
                + (this.lastTrader != null && !"-".equals(this.lastTrader) ? " from " + this.lastTrader : "")
                + "  total=" + this.tradesAccepted);
            if (this.config.autoBankAfterTrade()) {
                this.needsBank = true;
            }
        }

        if (this.needsBank && this.config.autoBankAfterTrade()) {
            int bankDelay = tickBank();
            publishLiveStats();
            maybeFlushHistory();
            return bankDelay;
        }

        Widget chatAccept = findChatboxAcceptTradeWidget();
        if (chatAccept != null) {
            this.state = State.ACCEPT;
            String name = this.pendingTraderName;
            if (name == null || name.isEmpty()) {
                name = extractTradeRequesterName(chatAccept);
            }
            Player trader = null;
            if (name != null && !name.isEmpty()) {
                trader = findPlayerByName(name);
            }
            if (trader != null) {
                this.lastTrader = cleanName(trader.getName());
                this.status = "Trading with " + this.lastTrader;
                this.pendingTraderName = null;
                trader.interact("Trade with");
                publishLiveStats();
                return 1200;
            }
            if (name != null && !name.isEmpty()) {
                this.status = "Trade from " + name + " - not nearby";
                this.lastTrader = name;
            } else {
                this.status = "Accepting trade (unknown player)";
                MousePackets.queueClickPacket();
                WidgetPackets.widgetFirstOption(chatAccept);
            }
            publishLiveStats();
            return 1200;
        }

        this.pendingTraderName = null;
        this.state = State.WAITING;
        this.status = "Waiting for trade request...";
        publishLiveStats();
        maybeFlushHistory();
        return 600;
    }

    private int tickBank() {
        if (Bank.isOpen()) {
            this.state = State.DEPOSIT;
            if (hasDepositableItems()) {
                if (this.pendingDepositCount <= 0) {
                    this.pendingDepositCount = countDepositableItems();
                }
                this.status = "Depositing inventory";
                depositLoad();
                return 700;
            }
            Bank.close();
            if (this.pendingDepositCount > 0) {
                this.itemsBanked += this.pendingDepositCount;
                this.pendingDepositCount = 0;
            }
            this.bankTrips++;
            this.needsBank = false;
            this.state = State.WAITING;
            this.status = "Banked - waiting for trade";
            OxConsole.info("Mule", "banked haul  trips=" + this.bankTrips + "  items~" + this.itemsBanked);
            return 800;
        }

        this.state = State.OPEN_BANK;
        TileObject bank = findBankObject();
        if (bank == null) {
            this.status = "Bank '" + this.config.bankObjectName() + "' not found";
            return 1200;
        }

        Player local = this.client.getLocalPlayer();
        if (local != null) {
            WorldPoint here = local.getWorldLocation();
            WorldPoint there = bank.getWorldLocation();
            if (here != null && there != null && here.distanceTo(there) > BANK_APPROACH_DISTANCE) {
                this.status = "Walking to bank";
                bank.interact(resolveBankAction(bank));
                return 800;
            }
        }

        long now = System.currentTimeMillis();
        if (now - this.lastBankInteractMs < 1800L) {
            this.status = "Opening bank...";
            return 500;
        }
        this.status = "Opening bank";
        bank.interact(resolveBankAction(bank));
        this.lastBankInteractMs = now;
        return 1000;
    }

    private void depositLoad() {
        String keep = this.config.keepItems();
        if (keep == null || keep.trim().isEmpty()) {
            Bank.depositInventory();
            return;
        }
        Bank.depositAll(item -> item != null && !isKept(item.getName(), keep));
    }

    private boolean hasDepositableItems() {
        String keep = this.config.keepItems();
        for (Item item : Inventory.getAll()) {
            if (item == null || item.getId() <= 0) {
                continue;
            }
            if (!isKept(item.getName(), keep)) {
                return true;
            }
        }
        return false;
    }

    private int countDepositableItems() {
        int n = 0;
        String keep = this.config.keepItems();
        for (Item item : Inventory.getAll()) {
            if (item == null || item.getId() <= 0) {
                continue;
            }
            if (!isKept(item.getName(), keep)) {
                n += Math.max(1, item.getQuantity());
            }
        }
        return n;
    }

    private static boolean isKept(String itemName, String keepCsv) {
        if (itemName == null || keepCsv == null || keepCsv.trim().isEmpty()) {
            return false;
        }
        String lower = itemName.toLowerCase(Locale.ROOT);
        for (String part : keepCsv.split(",")) {
            String token = part.trim().toLowerCase(Locale.ROOT);
            if (!token.isEmpty() && lower.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private TileObject findBankObject() {
        int id = this.config.bankObjectId();
        if (id > 0) {
            TileObject byId = TileObjects.getNearest(obj -> obj != null && obj.getId() == id);
            if (byId != null) {
                return byId;
            }
        }
        String name = this.config.bankObjectName();
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String needle = name.trim().toLowerCase(Locale.ROOT);
        TileObject byName = TileObjects.getNearest(obj -> {
            if (obj == null) {
                return false;
            }
            try {
                String n = obj.getName();
                return n != null && n.toLowerCase(Locale.ROOT).contains(needle);
            } catch (Exception e) {
                return false;
            }
        });
        if (byName != null) {
            return byName;
        }
        // Common home alternatives
        TileObject chest = TileObjects.getNearest(obj -> {
            if (obj == null) {
                return false;
            }
            try {
                String n = obj.getName();
                return n != null && n.toLowerCase(Locale.ROOT).contains("bank chest");
            } catch (Exception e) {
                return false;
            }
        });
        return chest;
    }

    private String resolveBankAction(TileObject bank) {
        String configured = this.config.bankAction();
        if (configured != null && !configured.trim().isEmpty()) {
            return configured.trim();
        }
        String[] actions = bank.getActions();
        if (actions != null) {
            for (String candidate : new String[]{"Bank", "Use", "Open", "Deposit"}) {
                for (String action : actions) {
                    if (action != null && action.equalsIgnoreCase(candidate)) {
                        return action;
                    }
                }
            }
        }
        return "Bank";
    }

    private boolean isOpen(int group, int child) {
        Widget w = this.client.getWidget(group, child);
        return w != null && !w.isHidden();
    }

    private Widget findChatboxAcceptTradeWidget() {
        List<Widget> list = Widgets.getAll(162, widget -> {
            String[] actions = widget.getActions();
            if (actions == null) {
                return false;
            }
            for (String action : actions) {
                if (action != null && action.toLowerCase(Locale.ROOT).startsWith("accept trade")) {
                    return true;
                }
            }
            return false;
        });
        return list.isEmpty() ? null : list.get(0);
    }

    private String extractTradeRequesterName(Widget acceptWidget) {
        String[] actions = acceptWidget.getActions();
        String action = actions != null && actions[0] != null ? actions[0].trim() : "";
        if (action.length() > "Accept trade ".length()) {
            String name = cleanName(action.substring("Accept trade ".length()));
            if (!name.isEmpty()) {
                return name;
            }
        }
        List<Widget> wishes = Widgets.getAll(162, widget -> {
            String text = widget.getText();
            return text != null && text.toLowerCase(Locale.ROOT).contains("wishes to trade");
        });
        Iterator<Widget> it = wishes.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            String plain = stripTags(w.getText());
            int idx = plain.toLowerCase(Locale.ROOT).indexOf("wishes to trade");
            if (idx <= 0) {
                continue;
            }
            String name = cleanName(plain.substring(0, idx));
            if (!name.isEmpty()) {
                return name;
            }
        }
        return "";
    }

    private Player findPlayerByName(String name) {
        String cleaned = cleanName(name);
        String lower = cleaned.toLowerCase(Locale.ROOT);
        if (lower.isEmpty()) {
            return null;
        }
        Player nearest = Players.getNearest(cleaned);
        if (nearest != null) {
            return nearest;
        }
        nearest = Players.getNearest(player -> {
            if (player == null || player.getName() == null) {
                return false;
            }
            return cleanName(player.getName()).equalsIgnoreCase(cleaned);
        });
        if (nearest != null) {
            return nearest;
        }
        nearest = Players.getNearest(player -> {
            if (player == null || player.getName() == null) {
                return false;
            }
            return cleanName(player.getName()).toLowerCase(Locale.ROOT).contains(lower);
        });
        if (nearest != null) {
            return nearest;
        }
        Player best = null;
        int bestDist = Integer.MAX_VALUE;
        Player local = this.client.getLocalPlayer();
        WorldPoint here = local != null ? local.getWorldLocation() : null;
        if (here == null) {
            return null;
        }
        for (Player p : this.client.getPlayers()) {
            if (p == null || p.getName() == null) {
                continue;
            }
            String n = cleanName(p.getName());
            if (!n.equalsIgnoreCase(cleaned) && !n.toLowerCase(Locale.ROOT).contains(lower)) {
                continue;
            }
            int d = p.getWorldLocation().distanceTo(here);
            if (d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }

    private void publishLiveStats() {
        Map<String, String> stats = new LinkedHashMap<>();
        stats.put("Status", getStatus());
        stats.put("State", getState());
        stats.put("Trades", String.valueOf(this.tradesAccepted));
        stats.put("Bank trips", String.valueOf(this.bankTrips));
        stats.put("Items banked", String.valueOf(this.itemsBanked));
        stats.put("Last trader", getLastTrader());
        stats.put("Runtime", formatRuntime(this.sessionStartMs));
        OxScriptActivity.update(SCRIPT_ID, "0xMule", stats);
    }

    private void maybeFlushHistory() {
        long now = System.currentTimeMillis();
        if (now - this.lastHistoryFlushMs < 30_000L) {
            return;
        }
        this.lastHistoryFlushMs = now;
        persistSession(false);
    }

    private void persistSession(boolean ended) {
        if (this.sessionId.isEmpty()) {
            return;
        }
        if (ended && this.tradesAccepted == 0 && this.bankTrips == 0
            && System.currentTimeMillis() - this.sessionStartMs < 15_000L) {
            return;
        }
        OxSessionHistory.Session session = new OxSessionHistory.Session();
        session.id = this.sessionId;
        session.script = "0xMule";
        session.accountId = System.getProperty("ox.accountId", "");
        session.accountName = firstNonBlank(
            System.getProperty("ox.displayName", ""),
            System.getProperty("ox.username", "")
        );
        session.startMs = this.sessionStartMs;
        session.endMs = ended ? System.currentTimeMillis() : 0L;
        session.tradesAccepted = this.tradesAccepted;
        session.bankTrips = this.bankTrips;
        session.itemsGathered = this.itemsBanked;
        session.tree = getLastTrader();
        session.note = getStatus();
        OxSessionHistory.save(session);
    }

    public String getStatus() {
        return this.status == null ? "Idle" : this.status;
    }

    public String getState() {
        return this.state == null ? "WAITING" : this.state.name();
    }

    public int getTradesAccepted() {
        return this.tradesAccepted;
    }

    public int getBankTrips() {
        return this.bankTrips;
    }

    public int getItemsBanked() {
        return this.itemsBanked;
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }

    public String getLastTrader() {
        return this.lastTrader == null || this.lastTrader.isEmpty() ? "-" : this.lastTrader;
    }

    private static String stripTags(String s) {
        return s == null ? "" : s.replaceAll("<[^>]*>", "");
    }

    private static String cleanName(String s) {
        return stripTags(s).replaceAll("^[^a-zA-Z0-9]+", "").trim();
    }

    private static String formatRuntime(long startMs) {
        if (startMs <= 0L) {
            return "0s";
        }
        long sec = Math.max(0L, (System.currentTimeMillis() - startMs) / 1000L);
        long h = sec / 3600L;
        long m = (sec % 3600L) / 60L;
        long s = sec % 60L;
        if (h > 0L) {
            return h + "h " + m + "m";
        }
        if (m > 0L) {
            return m + "m";
        }
        return s + "s";
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a.trim();
        }
        return b == null ? "" : b.trim();
    }
}

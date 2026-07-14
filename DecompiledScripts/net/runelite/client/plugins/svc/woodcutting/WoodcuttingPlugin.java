package net.runelite.client.plugins.svc.woodcutting;

import com.google.inject.Binder;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.svc.core.OrionLoopedPlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
    name = "0xWCBot",
    description = "Chops a configurable tree; banks via deposit box (default 29327) or drops logs when full.",
    tags = {"0x", "woodcutting", "bot", "automation", "deposit"},
    enabledByDefault = false
)
public class WoodcuttingPlugin extends OrionLoopedPlugin {
    @Inject
    private Client client;
    @Inject
    private ConfigManager configManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private WoodcuttingConfig config;

    private WoodcuttingOverlay overlay;
    private WoodcuttingEngine engine;
    private long sessionStartMs = 0L;
    private boolean loggedInLastTick = false;

    @Override
    public void configure(Binder binder) {
        ConfigManager cm = RuneLite.getInjector().getInstance(ConfigManager.class);
        binder.bind(WoodcuttingConfig.class).toInstance(cm.getConfig(WoodcuttingConfig.class));
    }

    @Override
    protected void onStart() throws Exception {
        this.engine = new WoodcuttingEngine(this.client);
        this.overlay = new WoodcuttingOverlay(this.client, this, this.config);
        this.overlayManager.add((Overlay) this.overlay);
        this.loggedInLastTick = false;
        this.sessionStartMs = System.currentTimeMillis();
    }

    @Override
    protected void onStop() throws Exception {
        this.overlayManager.remove((Overlay) this.overlay);
        this.overlay = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        GameState state = event.getGameState();
        if (state == GameState.LOGIN_SCREEN || state == GameState.CONNECTION_LOST) {
            this.loggedInLastTick = false;
        }
    }

    @Override
    protected int loop() {
        GameState gameState = this.client.getGameState();
        if (gameState == GameState.LOADING) {
            return 1000;
        }
        if (gameState != GameState.LOGGED_IN) {
            return 1500;
        }
        if (!this.loggedInLastTick) {
            this.loggedInLastTick = true;
            return 1200;
        }
        if (this.client.getLocalPlayer() == null) {
            return 600;
        }
        return this.engine.tick(
            this.client.getLocalPlayer(),
            new WoodcuttingEngine.Settings(
                this.config.treeObjectId(),
                this.parseLogIds(),
                this.config.chopAction(),
                this.config.inventoryMode(),
                this.config.depositObjectId(),
                this.config.depositAction()
            )
        );
    }

    private List<Integer> parseLogIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        if (this.config.logItemId1() > 0) {
            ids.add(this.config.logItemId1());
        }
        if (this.config.logItemId2() > 0) {
            ids.add(this.config.logItemId2());
        }
        return ids;
    }

    public String getStatus() {
        return this.engine != null ? this.engine.getStatus() : "Idle";
    }

    public String getState() {
        return this.engine != null ? this.engine.getState() : "CHOPPING";
    }

    public int getDropCount() {
        return this.engine != null ? this.engine.getCycleCount() : 0;
    }

    public long getSessionStartMs() {
        return this.sessionStartMs;
    }
}

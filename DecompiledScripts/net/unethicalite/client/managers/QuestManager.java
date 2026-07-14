/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.api.Quest
 *  net.runelite.api.QuestState
 *  net.runelite.api.events.GameTick
 *  net.runelite.api.events.WidgetLoaded
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 */
package net.unethicalite.client.managers;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.client.Static;

@Singleton
public class QuestManager {
    private static final Map<Quest, QuestState> questStates = new HashMap<Quest, QuestState>();
    private Instant lastUpdateTime = Instant.EPOCH;

    @Inject
    QuestManager(EventBus eventBus) {
        eventBus.register((Object)this);
        Arrays.stream(Quest.values()).forEach(it -> questStates.put((Quest)it, QuestState.NOT_STARTED));
    }

    private void loadQuestStates() {
        if (Game.isLoggedIn()) {
            Client client = Static.getClient();
            GameThread.invoke(() -> Arrays.stream(Quest.values()).forEach(it -> questStates.put((Quest)it, it.getState(client))));
            this.lastUpdateTime = Instant.now();
        }
    }

    @Subscribe(priority=2.14748365E9f)
    public void onGameTick(GameTick event) {
        if (Duration.between(this.lastUpdateTime, Instant.now()).toSeconds() >= 60L) {
            this.loadQuestStates();
        }
    }

    @Subscribe(priority=2.14748365E9f)
    public void onWidgetLoaded(WidgetLoaded event) {
        if (WidgetInfo.QUEST_COMPLETED_NAME_TEXT.getGroupId() == event.getGroupId()) {
            this.loadQuestStates();
        }
    }

    public static Map<Quest, QuestState> getQuestStates() {
        return questStates;
    }
}


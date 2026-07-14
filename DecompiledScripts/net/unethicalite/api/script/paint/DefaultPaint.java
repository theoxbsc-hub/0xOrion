/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.openosrs.client.ui.overlay.components.table.TableAlignment
 *  com.openosrs.client.ui.overlay.components.table.TableComponent
 *  net.runelite.api.Skill
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayLayer
 *  net.runelite.client.ui.overlay.OverlayPosition
 *  net.runelite.client.ui.overlay.OverlayPriority
 *  net.runelite.client.ui.overlay.RenderableEntity
 *  net.runelite.client.ui.overlay.components.PanelComponent
 *  net.runelite.client.ui.overlay.components.TitleComponent
 */
package net.unethicalite.api.script.paint;

import com.openosrs.client.ui.overlay.components.table.TableAlignment;
import com.openosrs.client.ui.overlay.components.table.TableComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.RenderableEntity;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.unethicalite.api.commons.StopWatch;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.script.paint.ExperienceTracker;
import net.unethicalite.api.script.paint.Statistic;

public class DefaultPaint
extends Overlay {
    private final Map<String, Statistic> statistics = new HashMap<String, Statistic>();
    private final PanelComponent panel = new PanelComponent();
    private final List<RenderableEntity> overlays = new ArrayList<RenderableEntity>();
    private final RenderableEntity tracker = graphics -> {
        TableComponent table = new TableComponent();
        table.setColumnAlignments(new TableAlignment[]{TableAlignment.LEFT, TableAlignment.LEFT});
        this.panel.getChildren().clear();
        this.panel.setPreferredLocation(new Point(306, 6));
        this.panel.setPreferredSize(new Dimension(200, 0));
        for (Map.Entry<String, Statistic> entry : this.statistics.entrySet()) {
            String key = entry.getKey();
            Statistic statistic = entry.getValue();
            if (statistic.isHeader()) {
                this.panel.getChildren().add(TitleComponent.builder().text(key).color(Color.WHITE).build());
                continue;
            }
            String text = key + ":";
            table.addRow(new String[]{text, statistic.toString()});
        }
        this.panel.getChildren().add(table);
        return this.panel.render(graphics);
    };

    public DefaultPaint() {
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.setPriority(OverlayPriority.LOW);
        this.overlays.add(this.tracker);
    }

    public Dimension render(Graphics2D graphics) {
        this.overlays.forEach(x -> x.render(graphics));
        return null;
    }

    public void submit(String key, Statistic statistic) {
        this.statistics.put(key, statistic);
    }

    public void submit(RenderableEntity renderableEntity) {
        this.overlays.add(renderableEntity);
    }

    public void clear() {
        this.statistics.clear();
        this.overlays.clear();
    }

    public void remove(RenderableEntity renderableEntity) {
        this.overlays.remove(renderableEntity);
    }

    public void trackSkill(Skill skill, boolean trackLevels) {
        if (!this.statistics.containsKey(skill.getName() + " XP")) {
            ExperienceTracker tracker = new ExperienceTracker(skill, Skills.getExperience(skill), Skills.getLevel(skill));
            StopWatch timer = StopWatch.start();
            this.submit(skill.getName() + " XP", new Statistic(timer, tracker::getExperienceGained));
            if (trackLevels && !this.statistics.containsKey(skill.getName() + " LVLs")) {
                this.submit(skill.getName() + " LVLs", new Statistic(timer, tracker::getLevelsGained));
            }
        }
    }

    public void setHeader(String text) {
        this.statistics.put(text, new Statistic(true, null));
    }

    public RenderableEntity getTracker() {
        return this.tracker;
    }
}


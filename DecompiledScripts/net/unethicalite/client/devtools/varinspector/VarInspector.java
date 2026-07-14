/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap$Builder
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.api.VarClientInt
 *  net.runelite.api.VarClientStr
 *  net.runelite.api.VarPlayer
 *  net.runelite.api.Varbits
 *  net.runelite.api.events.VarClientIntChanged
 *  net.runelite.api.events.VarClientStrChanged
 *  net.runelite.api.events.VarbitChanged
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.ui.ColorScheme
 *  net.runelite.client.ui.DynamicGridLayout
 *  net.runelite.client.ui.FontManager
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.devtools.varinspector;

import com.google.common.collect.ImmutableMap;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import net.runelite.api.Client;
import net.runelite.api.VarClientInt;
import net.runelite.api.VarClientStr;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.events.VarClientStrChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VarInspector {
    private static final Logger log = LoggerFactory.getLogger(VarInspector.class);
    private static final int MAX_LOG_ENTRIES = 10000;
    private static final int VARBITS_ARCHIVE_ID = 14;
    private static final Map<Integer, String> VARBIT_NAMES;
    private static final Map<Integer, String> VARCINT_NAMES;
    private static final Map<Integer, String> VARCSTR_NAMES;
    @Inject
    private Client client;
    @Inject
    private EventBus eventBus;
    private JPanel tracker;
    private int lastTick = 0;
    private int[] oldVarps = null;
    private int[] oldVarps2 = null;
    private int numVarbits = 10000;
    private Map<Integer, Object> varcs = null;
    private JFrame frame;

    @Inject
    private VarInspector() {
    }

    private void addVarLog(VarType type, String name, int old, int neew) {
        this.addVarLog(type, name, Integer.toString(old), Integer.toString(neew));
    }

    private void addVarLog(VarType type, String name, String old, String neew) {
        if (!type.getCheckBox().isSelected()) {
            return;
        }
        int tick = this.client.getTickCount();
        SwingUtilities.invokeLater(() -> {
            if (tick != this.lastTick) {
                this.lastTick = tick;
                JLabel header = new JLabel("Tick " + tick);
                header.setFont(FontManager.getRunescapeSmallFont());
                header.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.LIGHT_GRAY_COLOR), BorderFactory.createEmptyBorder(3, 6, 0, 0)));
                this.tracker.add(header);
            }
            this.tracker.add(new JLabel(String.format("%s %s changed: %s -> %s", type.getName(), name, old, neew)));
            while (this.tracker.getComponentCount() > 10000) {
                this.tracker.remove(0);
            }
            this.tracker.revalidate();
        });
    }

    @Subscribe
    private void onVarbitChanged(VarbitChanged ev) {
        String name;
        int neew;
        int i;
        int[] varps = this.client.getVarps();
        for (i = 0; i < this.numVarbits; ++i) {
            try {
                int old = this.client.getVarbitValue(this.oldVarps, i);
                neew = this.client.getVarbitValue(varps, i);
                if (old == neew) continue;
                this.client.setVarbitValue(this.oldVarps2, i, neew);
                name = VARBIT_NAMES.getOrDefault(i, Integer.toString(i));
                this.addVarLog(VarType.VARBIT, name, old, neew);
                continue;
            }
            catch (IndexOutOfBoundsException e) {
                log.debug("Hit OOB at varbit {}", (Object)i);
                this.numVarbits = i;
                break;
            }
        }
        for (i = 0; i < varps.length; ++i) {
            int old = this.oldVarps2[i];
            neew = varps[i];
            if (old == neew) continue;
            name = String.format("%d", i);
            for (VarPlayer varp : VarPlayer.values()) {
                if (varp.getId() != i) continue;
                name = String.format("%s(%d)", varp.name(), i);
                break;
            }
            this.addVarLog(VarType.VARP, name, old, neew);
        }
        System.arraycopy(this.client.getVarps(), 0, this.oldVarps, 0, this.oldVarps.length);
        System.arraycopy(this.client.getVarps(), 0, this.oldVarps2, 0, this.oldVarps2.length);
    }

    @Subscribe
    private void onVarClientIntChanged(VarClientIntChanged e) {
        int idx = e.getIndex();
        int neew = this.client.getVarcMap().getOrDefault(idx, 0);
        int old = (Integer)this.varcs.getOrDefault(idx, 0);
        this.varcs.put(idx, neew);
        if (old != neew) {
            String name = VARCINT_NAMES.getOrDefault(idx, Integer.toString(idx));
            this.addVarLog(VarType.VARCINT, name, old, neew);
        }
    }

    @Subscribe
    private void onVarClientStrChanged(VarClientStrChanged e) {
        int idx = e.getIndex();
        Object neew = this.client.getVarcMap().getOrDefault(idx, "");
        Object old = (String)this.varcs.getOrDefault(idx, "");
        this.varcs.put(idx, neew);
        if (!Objects.equals(old, neew)) {
            String name = VARCSTR_NAMES.getOrDefault(idx, Integer.toString(idx));
            old = old != null ? "\"" + (String)old + "\"" : "null";
            neew = neew != null ? "\"" + (String)neew + "\"" : "null";
            this.addVarLog(VarType.VARCSTR, name, (String)old, (String)neew);
        }
    }

    public void open() {
        if (this.tracker == null) {
            this.tracker = new JPanel();
        }
        if (this.frame != null && this.frame.isVisible()) {
            this.close();
            return;
        }
        if (this.frame == null) {
            this.frame = new JFrame();
            this.frame.setAlwaysOnTop(true);
            this.frame.setTitle("Var Inspector");
            this.frame.setLayout(new BorderLayout());
            this.frame.setDefaultCloseOperation(2);
            this.frame.addWindowListener(new WindowAdapter(){

                @Override
                public void windowClosing(WindowEvent e) {
                    VarInspector.this.close();
                }
            });
            this.tracker.setLayout((LayoutManager)new DynamicGridLayout(0, 1, 0, 3));
            JPanel trackerWrapper = new JPanel();
            trackerWrapper.setLayout(new BorderLayout());
            trackerWrapper.add((Component)this.tracker, "North");
            JScrollPane trackerScroller = new JScrollPane(trackerWrapper);
            trackerScroller.setPreferredSize(new Dimension(400, 400));
            final JScrollBar vertical = trackerScroller.getVerticalScrollBar();
            vertical.addAdjustmentListener(new AdjustmentListener(){
                int lastMaximum = this.actualMax();

                private int actualMax() {
                    return vertical.getMaximum() - vertical.getModel().getExtent();
                }

                @Override
                public void adjustmentValueChanged(AdjustmentEvent e) {
                    if (vertical.getValue() >= this.lastMaximum) {
                        vertical.setValue(this.actualMax());
                    }
                    this.lastMaximum = this.actualMax();
                }
            });
            this.frame.add((Component)trackerScroller, "Center");
            JPanel trackerOpts = new JPanel();
            trackerOpts.setLayout(new FlowLayout());
            for (VarType cb : VarType.values()) {
                trackerOpts.add(cb.getCheckBox());
            }
            JButton clearBtn = new JButton("Clear");
            clearBtn.addActionListener(e -> {
                this.tracker.removeAll();
                this.tracker.revalidate();
            });
            trackerOpts.add(clearBtn);
            this.frame.add((Component)trackerOpts, "South");
            this.frame.setLocationRelativeTo(this.client.getCanvas());
            this.frame.pack();
        }
        if (this.oldVarps == null) {
            this.oldVarps = new int[this.client.getVarps().length];
            this.oldVarps2 = new int[this.client.getVarps().length];
        }
        System.arraycopy(this.client.getVarps(), 0, this.oldVarps, 0, this.oldVarps.length);
        System.arraycopy(this.client.getVarps(), 0, this.oldVarps2, 0, this.oldVarps2.length);
        this.varcs = new HashMap<Integer, Object>(this.client.getVarcMap());
        this.eventBus.register((Object)this);
        this.frame.setVisible(true);
        this.frame.toFront();
        this.frame.repaint();
    }

    public void close() {
        this.tracker.removeAll();
        this.eventBus.unregister((Object)this);
        this.frame.setVisible(false);
    }

    static {
        ImmutableMap.Builder varbits = new ImmutableMap.Builder();
        ImmutableMap.Builder varcint = new ImmutableMap.Builder();
        ImmutableMap.Builder varcstr = new ImmutableMap.Builder();
        try {
            for (Field f : Varbits.class.getDeclaredFields()) {
                varbits.put((Object)f.getInt(null), (Object)f.getName());
            }
            for (Field f : VarClientInt.class.getDeclaredFields()) {
                varcint.put((Object)f.getInt(null), (Object)f.getName());
            }
            for (Field f : VarClientStr.class.getDeclaredFields()) {
                varcstr.put((Object)f.getInt(null), (Object)f.getName());
            }
        }
        catch (IllegalAccessException ex) {
            log.error("error setting up var names", (Throwable)ex);
        }
        VARBIT_NAMES = varbits.build();
        VARCINT_NAMES = varcint.build();
        VARCSTR_NAMES = varcstr.build();
    }

    private static enum VarType {
        VARBIT("Varbit"),
        VARP("VarPlayer"),
        VARCINT("VarClientInt"),
        VARCSTR("VarClientStr");

        private final String name;
        private final JCheckBox checkBox;

        private VarType(String name) {
            this.name = name;
            this.checkBox = new JCheckBox(name, true);
        }

        String getName() {
            return this.name;
        }

        JCheckBox getCheckBox() {
            return this.checkBox;
        }
    }
}


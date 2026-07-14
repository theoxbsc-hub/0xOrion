/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.client.util.ImageUtil
 */
package net.unethicalite.client.minimal.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Panel;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import net.runelite.api.Client;
import net.runelite.client.util.ImageUtil;
import net.unethicalite.client.managers.MinimalPluginManager;
import net.unethicalite.client.minimal.MinimalModule;
import net.unethicalite.client.minimal.plugins.PluginEntry;

@Singleton
public class MinimalPluginsPanel {
    @Inject
    private Client client;
    @Inject
    private MinimalPluginManager minimalPluginManager;
    private JList<PluginEntry> pluginList;
    private JFrame frame;

    public void open() {
        if (this.frame == null) {
            this.frame = new JFrame("Scripts");
            this.frame.setAlwaysOnTop(true);
            this.pluginList = new JList();
            this.frame.setLayout(new BorderLayout());
            this.frame.setDefaultCloseOperation(2);
            this.frame.setPreferredSize(new Dimension(500, 300));
            this.loadScripts();
            Panel northPanel = new Panel(new GridLayout(1, 0));
            JButton startButton = new JButton(new ImageIcon(ImageUtil.loadImageResource(MinimalModule.class, (String)"play_arrow_white_18x18.png")));
            startButton.addActionListener(e -> {
                if (this.minimalPluginManager.isScriptRunning()) {
                    this.minimalPluginManager.stopPlugin();
                }
                PluginEntry selected = this.pluginList.getSelectedValue();
                this.minimalPluginManager.startPlugin(selected);
                this.frame.setVisible(false);
            });
            northPanel.add(startButton);
            JButton pauseButton = new JButton(new ImageIcon(ImageUtil.loadImageResource(MinimalModule.class, (String)"pause_white_18x18.png")));
            pauseButton.addActionListener(e -> this.minimalPluginManager.pauseScript());
            northPanel.add(pauseButton);
            JButton stopButton = new JButton(new ImageIcon(ImageUtil.loadImageResource(MinimalModule.class, (String)"stop_white_18x18.png")));
            stopButton.addActionListener(e -> {
                this.minimalPluginManager.stopPlugin();
                this.loadScripts();
            });
            northPanel.add(stopButton);
            JButton refreshButton = new JButton(new ImageIcon(ImageUtil.loadImageResource(MinimalModule.class, (String)"reset.png")));
            refreshButton.addActionListener(e -> this.reloadScript());
            northPanel.add(refreshButton);
            this.frame.add((Component)northPanel, "North");
            Panel centerPanel = new Panel(new GridLayout(0, 1));
            JScrollPane scriptScrollPane = new JScrollPane(this.pluginList);
            scriptScrollPane.setVerticalScrollBarPolicy(22);
            centerPanel.add(scriptScrollPane);
            this.frame.setLocationRelativeTo(this.client.getCanvas());
            this.frame.add((Component)centerPanel, "Center");
            this.frame.pack();
        }
        this.loadScripts();
        this.frame.setVisible(!this.frame.isVisible());
    }

    public List<PluginEntry> loadScripts() {
        ArrayList<PluginEntry> scriptListEntries = new ArrayList<PluginEntry>(this.minimalPluginManager.loadPlugins());
        this.pluginList.setListData((PluginEntry[])scriptListEntries.toArray(new PluginEntry[0]));
        return scriptListEntries;
    }

    public void reloadScript() {
        this.loadScripts();
        this.minimalPluginManager.restartPlugin();
    }
}


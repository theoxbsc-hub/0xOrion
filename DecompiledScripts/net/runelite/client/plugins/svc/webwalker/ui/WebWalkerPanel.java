/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.runelite.api.coords.WorldPoint
 *  net.runelite.client.ui.ColorScheme
 *  net.runelite.client.ui.PluginPanel
 */
package net.runelite.client.plugins.svc.webwalker.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.svc.webwalker.WebWalkerPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class WebWalkerPanel
extends PluginPanel {
    private static final Pattern INTS = Pattern.compile("-?\\d+");
    private final WebWalkerPlugin plugin;
    private final JTextField xField = new JTextField(5);
    private final JTextField yField = new JTextField(5);
    private final JTextField planeField = new JTextField(2);
    private final JTextField pasteField = new JTextField();
    private final JLabel statusLabel = new JLabel("Idle", 2);
    private final JButton walkButton = new JButton("Walk");

    public WebWalkerPanel(WebWalkerPlugin webWalkerPlugin) {
        this.plugin = webWalkerPlugin;
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, 1));
        jPanel.add(WebWalkerPanel.title("Web Walker"));
        jPanel.add(WebWalkerPanel.vGap(8));
        JPanel jPanel2 = new JPanel(new GridLayout(3, 2, 4, 4));
        jPanel2.add(WebWalkerPanel.label("X"));
        jPanel2.add(this.xField);
        jPanel2.add(WebWalkerPanel.label("Y"));
        jPanel2.add(this.yField);
        jPanel2.add(WebWalkerPanel.label("Plane"));
        jPanel2.add(this.planeField);
        jPanel.add(jPanel2);
        jPanel.add(WebWalkerPanel.vGap(6));
        jPanel.add(WebWalkerPanel.label("Paste (x, y, z)"));
        this.pasteField.setToolTipText("Paste \"3222, 3218, 0\" or \"new WorldPoint(3222, 3218, 0)\"");
        this.pasteField.addActionListener(actionEvent -> this.applyPaste());
        jPanel.add(this.pasteField);
        jPanel.add(WebWalkerPanel.vGap(2));
        JButton jButton = new JButton("Apply pasted");
        jButton.addActionListener(actionEvent -> this.applyPaste());
        jPanel.add(jButton);
        jPanel.add(WebWalkerPanel.vGap(8));
        JPanel jPanel3 = new JPanel(new FlowLayout(0, 4, 0));
        this.walkButton.addActionListener(actionEvent -> this.onWalk());
        JButton jButton2 = new JButton("Stop");
        jButton2.addActionListener(actionEvent -> webWalkerPlugin.stop());
        JButton jButton3 = new JButton("Use current tile");
        jButton3.addActionListener(actionEvent -> this.fillCurrent());
        jPanel3.add(this.walkButton);
        jPanel3.add(jButton2);
        jPanel.add(jPanel3);
        jPanel.add(WebWalkerPanel.vGap(4));
        jPanel.add(jButton3);
        jPanel.add(WebWalkerPanel.vGap(10));
        jPanel.add(WebWalkerPanel.label("Status"));
        this.statusLabel.setForeground(new Color(120, 200, 255));
        jPanel.add(this.statusLabel);
        this.add(jPanel, "North");
        WorldPoint worldPoint = webWalkerPlugin.getTarget();
        if (worldPoint != null) {
            this.xField.setText(Integer.toString(worldPoint.getX()));
            this.yField.setText(Integer.toString(worldPoint.getY()));
            this.planeField.setText(Integer.toString(worldPoint.getPlane()));
        }
        Timer timer = new Timer(400, actionEvent -> this.refresh());
        timer.start();
    }

    private void onWalk() {
        try {
            int n = Integer.parseInt(this.xField.getText().trim());
            int n2 = Integer.parseInt(this.yField.getText().trim());
            int n3 = this.planeField.getText().trim().isEmpty() ? 0 : Integer.parseInt(this.planeField.getText().trim());
            this.plugin.startWalk(n, n2, n3);
        }
        catch (NumberFormatException numberFormatException) {
            this.statusLabel.setText("Enter valid integers");
        }
    }

    private void applyPaste() {
        Matcher matcher = INTS.matcher(this.pasteField.getText());
        int[] nArray = new int[3];
        int n = 0;
        while (matcher.find() && n < 3) {
            try {
                nArray[n++] = Integer.parseInt(matcher.group());
            }
            catch (NumberFormatException numberFormatException) {}
        }
        if (n >= 2) {
            this.xField.setText(Integer.toString(nArray[0]));
            this.yField.setText(Integer.toString(nArray[1]));
            this.planeField.setText(Integer.toString(n >= 3 ? nArray[2] : 0));
        }
    }

    private void fillCurrent() {
        WorldPoint worldPoint = this.plugin.currentTile();
        if (worldPoint != null) {
            this.xField.setText(Integer.toString(worldPoint.getX()));
            this.yField.setText(Integer.toString(worldPoint.getY()));
            this.planeField.setText(Integer.toString(worldPoint.getPlane()));
        }
    }

    private void refresh() {
        this.statusLabel.setText("<html>" + this.plugin.getStatus() + "</html>");
        this.walkButton.setText(this.plugin.isWalking() ? "Walking\u2026" : "Walk");
    }

    private static JLabel title(String string) {
        JLabel jLabel = new JLabel(string);
        jLabel.setForeground(Color.WHITE);
        jLabel.setFont(jLabel.getFont().deriveFont(16.0f));
        return jLabel;
    }

    private static JLabel label(String string) {
        JLabel jLabel = new JLabel(string);
        jLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        return jLabel;
    }

    private static JPanel vGap(int n) {
        JPanel jPanel = new JPanel();
        jPanel.setOpaque(false);
        jPanel.setPreferredSize(new Dimension(1, n));
        return jPanel;
    }
}


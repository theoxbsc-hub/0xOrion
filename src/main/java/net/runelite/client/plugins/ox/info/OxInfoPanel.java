package net.runelite.client.plugins.ox.info;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import net.runelite.client.plugins.ox.core.OxSessionHistory;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

/**
 * Sidebar: identity and expandable past sessions.
 * Session rows use real JButtons (reliable hit targets) and natural sizing -
 * no fixed-height clamps that clip the last row.
 */
public class OxInfoPanel extends PluginPanel {
    private static final Color PURPLE = new Color(0xB44DFF);
    private static final Color MUTED = new Color(0x9A9AA8);
    private static final Color VALUE = new Color(0xEBEBF5);
    private static final Color GREEN = new Color(0x50BE78);
    private static final Color CARD_BG = ColorScheme.DARKER_GRAY_COLOR;
    private static final Color CARD_BORDER = new Color(0x373746);
    private static final Color BTN_HOVER = new Color(0x32323E);

    private final JLabel profileValue = valueLabel();
    private final JLabel userValue = valueLabel();
    private final JLabel accountIdValue = valueLabel();
    private final JLabel macValue = valueLabel();
    private final JLabel hostValue = valueLabel();
    private final JLabel proxyEnabledValue = valueLabel();
    private final JLabel proxyInfoValue = valueLabel();

    private final JPanel historyBody = column();
    private final Set<String> expandedSessionIds = new HashSet<>();
    private String lastHistorySignature = "";
    private final Timer refreshTimer;

    public OxInfoPanel() {
        setBorder(new EmptyBorder(10, 10, 24, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("0xINFO");
        title.setForeground(PURPLE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        header.add(title, BorderLayout.NORTH);
        JLabel subtitle = new JLabel("Identity | history");
        subtitle.setForeground(MUTED);
        subtitle.setBorder(new EmptyBorder(2, 0, 10, 0));
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        JPanel body = column();
        body.add(section("Profile"));
        body.add(simpleCard(
            field("Name", profileValue),
            field("Username", userValue),
            field("Account ID", accountIdValue)
        ));
        body.add(Box.createVerticalStrut(8));
        body.add(section("Identity"));
        body.add(simpleCard(field("MAC", macValue), field("Hostname", hostValue)));
        body.add(Box.createVerticalStrut(8));
        body.add(section("Proxy"));
        body.add(simpleCard(field("Enabled", proxyEnabledValue), field("Details", proxyInfoValue)));
        body.add(Box.createVerticalStrut(10));
        body.add(section("Past Sessions"));
        JLabel hint = new JLabel("Tap a session button to expand / collapse");
        hint.setForeground(MUTED);
        hint.setFont(hint.getFont().deriveFont(10f));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setBorder(new EmptyBorder(0, 0, 6, 0));
        body.add(hint);
        body.add(historyBody);
        // Extra space so the last row is never jammed against the panel edge
        body.add(Box.createVerticalStrut(48));
        add(body, BorderLayout.NORTH);

        this.refreshTimer = new Timer(1500, e -> refresh());
        this.refreshTimer.setRepeats(true);
        refresh();
    }

    @Override
    public void onActivate() {
        this.lastHistorySignature = "";
        refresh();
        if (!this.refreshTimer.isRunning()) {
            this.refreshTimer.start();
        }
    }

    @Override
    public void onDeactivate() {
        this.refreshTimer.stop();
    }

    void startAutoRefresh() {
        if (!this.refreshTimer.isRunning()) {
            this.refreshTimer.start();
        }
    }

    void stopAutoRefresh() {
        this.refreshTimer.stop();
    }

    void refresh() {
        OxRuntimeInfo info = OxRuntimeInfo.read();
        profileValue.setText(info.getDisplayName());
        userValue.setText(info.getUsername());
        accountIdValue.setText(info.getAccountId().isEmpty() ? "(none)" : info.getAccountId());
        macValue.setText(info.getMac());
        hostValue.setText(info.getHostname());
        proxyEnabledValue.setText(info.isProxyEnabled() ? "true" : "false");
        proxyEnabledValue.setForeground(info.isProxyEnabled() ? GREEN : MUTED);
        proxyInfoValue.setText(info.isProxyEnabled() ? info.getProxySummary() : "-");

        refreshHistory(info.getAccountId());
        revalidate();
        repaint();
    }

    private void refreshHistory(String accountId) {
        List<OxSessionHistory.Session> sessions = OxSessionHistory.recentForAccount(accountId, 15);
        if (sessions.isEmpty()) {
            sessions = OxSessionHistory.recent(15);
        }
        String signature = historySignature(sessions) + "|exp=" + this.expandedSessionIds;
        if (signature.equals(this.lastHistorySignature) && this.historyBody.getComponentCount() > 0) {
            return;
        }
        this.lastHistorySignature = signature;

        this.historyBody.removeAll();
        if (sessions.isEmpty()) {
            this.historyBody.add(muted("No past sessions yet. Run a bot - history saves here."));
        } else {
            for (int i = 0; i < sessions.size(); i++) {
                if (i > 0) {
                    this.historyBody.add(Box.createVerticalStrut(8));
                }
                this.historyBody.add(createSessionCard(sessions.get(i)));
            }
        }
        packColumn(this.historyBody);
    }

    private JPanel createSessionCard(OxSessionHistory.Session session) {
        String id = sessionKey(session);
        boolean expanded = this.expandedSessionIds.contains(id);

        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel caret = new JLabel(expanded ? "▼" : "▶");
        caret.setForeground(PURPLE);
        caret.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        caret.setBorder(new EmptyBorder(0, 0, 0, 8));

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("<html><body style='width:190px'>" + escape(headline(session)) + "</body></html>");
        title.setForeground(VALUE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel meta = new JLabel(session.metaLine());
        meta.setForeground(MUTED);
        meta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);
        meta.setBorder(new EmptyBorder(3, 0, 0, 0));

        textCol.add(title);
        textCol.add(meta);

        JPanel headerInner = new JPanel(new BorderLayout());
        headerInner.setOpaque(false);
        headerInner.setBorder(new EmptyBorder(10, 10, 10, 10));
        headerInner.add(caret, BorderLayout.WEST);
        headerInner.add(textCol, BorderLayout.CENTER);

        // Real button = reliable hit target for every row (including the last)
        JButton toggle = new JButton();
        toggle.setLayout(new BorderLayout());
        toggle.add(headerInner, BorderLayout.CENTER);
        toggle.setFocusPainted(false);
        toggle.setContentAreaFilled(true);
        toggle.setOpaque(true);
        toggle.setBackground(CARD_BG);
        toggle.setBorder(BorderFactory.createEmptyBorder());
        toggle.setHorizontalAlignment(SwingConstants.LEFT);
        toggle.setToolTipText(expanded ? "Collapse session" : "Expand session");
        toggle.addActionListener(e -> {
            if (this.expandedSessionIds.contains(id)) {
                this.expandedSessionIds.remove(id);
            } else {
                this.expandedSessionIds.add(id);
            }
            this.lastHistorySignature = "";
            refreshHistory(OxRuntimeInfo.read().getAccountId());
            revalidate();
            repaint();
            SwingUtilities.invokeLater(() -> {
                card.scrollRectToVisible(card.getBounds());
            });
        });
        toggle.addChangeListener(e -> {
            if (toggle.getModel().isRollover() || toggle.getModel().isArmed()) {
                toggle.setBackground(BTN_HOVER);
            } else {
                toggle.setBackground(CARD_BG);
            }
        });

        card.add(toggle, BorderLayout.NORTH);

        if (expanded) {
            JPanel details = new JPanel(new GridBagLayout());
            details.setOpaque(true);
            details.setBackground(CARD_BG);
            details.setBorder(new EmptyBorder(0, 12, 12, 12));

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(3, 0, 3, 0);

            int row = 0;
            ScriptKind kind = scriptKind(session.script);

            if (kind == ScriptKind.THIEVE) {
                c.gridy = row++;
                details.add(statLine("Steals", String.valueOf(session.steals), VALUE), c);
                c.gridy = row++;
                details.add(statLine("Sells", String.valueOf(session.sells), VALUE), c);
                c.gridy = row++;
                details.add(statLine("Stall", blankDash(session.tree), VALUE), c);
            } else if (kind == ScriptKind.WOODCUTTING) {
                c.gridy = row++;
                details.add(statLine("Logs banked", String.valueOf(session.logsBanked), VALUE), c);
                c.gridy = row++;
                details.add(statLine("Logs dropped", String.valueOf(session.logsDropped), VALUE), c);
                c.gridy = row++;
                details.add(statLine("Bank trips", String.valueOf(session.bankTrips), VALUE), c);
                c.gridy = row++;
                details.add(statLine("Tree", blankDash(session.tree), VALUE), c);
            } else if (kind == ScriptKind.MINING) {
                c.gridy = row++;
                details.add(statLine("Trips", String.valueOf(session.bankTrips), VALUE), c);
                c.gridy = row++;
                details.add(statLine("XP gained", String.valueOf(session.xpGained), VALUE), c);
                c.gridy = row++;
                details.add(statLine("Target", blankDash(session.tree), VALUE), c);
                if (session.itemsGathered > 0) {
                    c.gridy = row++;
                    details.add(statLine("Ores gathered", String.valueOf(session.itemsGathered), VALUE), c);
                }
                if (session.barsSmelted > 0) {
                    c.gridy = row++;
                    details.add(statLine("Bars worth", String.valueOf(session.barsSmelted), VALUE), c);
                }
            } else if (kind == ScriptKind.FISHING) {
                c.gridy = row++;
                details.add(statLine("Deposits", String.valueOf(session.bankTrips), VALUE), c);
                c.gridy = row++;
                details.add(statLine("Fish banked", String.valueOf(session.itemsGathered), VALUE), c);
                c.gridy = row++;
                details.add(statLine("Tier", blankDash(session.tree), VALUE), c);
            } else if (kind == ScriptKind.SMITHING) {
                c.gridy = row++;
                details.add(statLine("Bars smelted", String.valueOf(session.barsSmelted), VALUE), c);
                c.gridy = row++;
                details.add(statLine("Items smithed", String.valueOf(session.itemsSmithed), VALUE), c);
                c.gridy = row++;
                details.add(statLine("Trips", String.valueOf(session.bankTrips), VALUE), c);
                c.gridy = row++;
                details.add(statLine("XP gained", String.valueOf(session.xpGained), VALUE), c);
                c.gridy = row++;
                details.add(statLine("Task", blankDash(session.tree), VALUE), c);
            } else if (kind == ScriptKind.MULE) {
                c.gridy = row++;
                details.add(statLine("Trades accepted", String.valueOf(session.tradesAccepted), VALUE), c);
                c.gridy = row++;
                details.add(statLine("Bank trips", String.valueOf(session.bankTrips), VALUE), c);
                c.gridy = row++;
                details.add(statLine("Items banked", String.valueOf(session.itemsGathered), VALUE), c);
                c.gridy = row++;
                details.add(statLine("Last trader", blankDash(session.tree), VALUE), c);
            } else {
                // Unknown / future scripts - only show metrics that actually have data
                if (session.steals > 0 || session.sells > 0) {
                    c.gridy = row++;
                    details.add(statLine("Steals", String.valueOf(session.steals), VALUE), c);
                    c.gridy = row++;
                    details.add(statLine("Sells", String.valueOf(session.sells), VALUE), c);
                }
                if (session.logsBanked > 0 || session.logsDropped > 0 || session.bankTrips > 0) {
                    c.gridy = row++;
                    details.add(statLine("Logs banked", String.valueOf(session.logsBanked), VALUE), c);
                    c.gridy = row++;
                    details.add(statLine("Logs dropped", String.valueOf(session.logsDropped), VALUE), c);
                    c.gridy = row++;
                    details.add(statLine("Bank trips", String.valueOf(session.bankTrips), VALUE), c);
                }
                if (session.xpGained > 0) {
                    c.gridy = row++;
                    details.add(statLine("XP gained", String.valueOf(session.xpGained), VALUE), c);
                }
                if (session.barsSmelted > 0 || session.itemsSmithed > 0) {
                    c.gridy = row++;
                    details.add(statLine("Bars smelted", String.valueOf(session.barsSmelted), VALUE), c);
                    c.gridy = row++;
                    details.add(statLine("Items smithed", String.valueOf(session.itemsSmithed), VALUE), c);
                }
                if (session.itemsGathered > 0) {
                    c.gridy = row++;
                    details.add(statLine("Gathered", String.valueOf(session.itemsGathered), VALUE), c);
                }
                if (session.tree != null && !session.tree.isEmpty() && !"-".equals(session.tree)) {
                    c.gridy = row++;
                    details.add(statLine("Target", session.tree, VALUE), c);
                }
            }

            c.gridy = row++;
            details.add(statLine("Duration", formatDuration(session), VALUE), c);
            c.gridy = row++;
            details.add(statLine("Account", blankDash(
                session.accountName != null && !session.accountName.isEmpty()
                    ? session.accountName : session.accountId), VALUE), c);
            if (session.note != null && !session.note.isEmpty()) {
                c.gridy = row++;
                details.add(statLine("Last status", session.note, VALUE), c);
            }
            c.gridy = row;
            details.add(statLine("Ended", session.endMs > 0L ? "yes" : "in progress", VALUE), c);

            card.add(details, BorderLayout.CENTER);
        }

        // Width flexible, height natural - never clamp, or the last row becomes unclickable
        packWidth(card);
        return card;
    }

    private static void packColumn(JPanel root) {
        for (Component child : root.getComponents()) {
            if (child instanceof JPanel || child instanceof JLabel) {
                packWidth(child);
            }
        }
        root.revalidate();
    }

    private static void packWidth(Component c) {
        if (c instanceof JPanel) {
            ((JPanel) c).setAlignmentX(Component.LEFT_ALIGNMENT);
        } else if (c instanceof JLabel) {
            ((JLabel) c).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        // Let preferred height stay natural; only unlock width for BoxLayout
        Dimension pref = c.getPreferredSize();
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.max(pref.height, 1)));
    }

    private static String headline(OxSessionHistory.Session session) {
        String script = session.script == null || session.script.isEmpty() ? "Script" : session.script;
        StringBuilder sb = new StringBuilder(script);
        sb.append(" | ").append(formatDuration(session));
        ScriptKind kind = scriptKind(session.script);
        if (kind == ScriptKind.THIEVE) {
            if (session.steals > 0) {
                sb.append(" | steals ").append(session.steals);
            }
            if (session.sells > 0) {
                sb.append(" | sells ").append(session.sells);
            }
        } else if (kind == ScriptKind.WOODCUTTING) {
            if (session.logsBanked > 0) {
                sb.append(" | banked ").append(session.logsBanked);
            }
            if (session.logsDropped > 0) {
                sb.append(" | dropped ").append(session.logsDropped);
            }
        } else if (kind == ScriptKind.MINING) {
            if (session.bankTrips > 0) {
                sb.append(" | trips ").append(session.bankTrips);
            }
            if (session.xpGained > 0) {
                sb.append(" | xp ").append(session.xpGained);
            }
        } else if (kind == ScriptKind.FISHING) {
            if (session.bankTrips > 0) {
                sb.append(" | deposits ").append(session.bankTrips);
            }
            if (session.itemsGathered > 0) {
                sb.append(" | fish ").append(session.itemsGathered);
            }
        } else if (kind == ScriptKind.SMITHING) {
            if (session.barsSmelted > 0) {
                sb.append(" | bars ").append(session.barsSmelted);
            }
            if (session.itemsSmithed > 0) {
                sb.append(" | items ").append(session.itemsSmithed);
            }
        } else if (kind == ScriptKind.MULE) {
            if (session.tradesAccepted > 0) {
                sb.append(" | trades ").append(session.tradesAccepted);
            }
            if (session.bankTrips > 0) {
                sb.append(" | bank ").append(session.bankTrips);
            }
        } else {
            if (session.steals > 0) {
                sb.append(" | steals ").append(session.steals);
            }
            if (session.logsBanked > 0) {
                sb.append(" | banked ").append(session.logsBanked);
            }
        }
        if (session.tree != null && !session.tree.isEmpty() && !"-".equals(session.tree)) {
            sb.append(" | ").append(session.tree);
        }
        return sb.toString();
    }

    private enum ScriptKind {
        WOODCUTTING,
        THIEVE,
        MINING,
        FISHING,
        SMITHING,
        MULE,
        OTHER
    }

    private static ScriptKind scriptKind(String script) {
        if (script == null) {
            return ScriptKind.OTHER;
        }
        String s = script.toLowerCase();
        if (s.contains("thieve") || s.contains("thiev")) {
            return ScriptKind.THIEVE;
        }
        if (s.contains("wc") || s.contains("wood") || s.contains("wcbot")) {
            return ScriptKind.WOODCUTTING;
        }
        if (s.contains("mine") || s.contains("mining")) {
            return ScriptKind.MINING;
        }
        if (s.contains("fish")) {
            return ScriptKind.FISHING;
        }
        if (s.contains("smith")) {
            return ScriptKind.SMITHING;
        }
        if (s.contains("mule")) {
            return ScriptKind.MULE;
        }
        return ScriptKind.OTHER;
    }

    private static String historySignature(List<OxSessionHistory.Session> sessions) {
        StringBuilder sb = new StringBuilder();
        for (OxSessionHistory.Session s : sessions) {
            sb.append(sessionKey(s)).append('|')
                .append(s.logsBanked).append('|')
                .append(s.steals).append('|')
                .append(s.endMs).append('|')
                .append(s.tree).append(';');
        }
        return sb.toString();
    }

    private static String sessionKey(OxSessionHistory.Session session) {
        if (session.id != null && !session.id.isEmpty()) {
            return session.id;
        }
        return session.script + ":" + session.startMs;
    }

    private static String formatDuration(OxSessionHistory.Session session) {
        long sec = session.durationMs() / 1000L;
        long h = sec / 3600L;
        long m = (sec % 3600L) / 60L;
        long s = sec % 60L;
        if (h > 0L) {
            return h + "h " + m + "m";
        }
        if (m > 0L) {
            return m + "m " + s + "s";
        }
        return s + "s";
    }

    private static String blankDash(String v) {
        if (v == null || v.isEmpty() || "-".equals(v)) {
            return "-";
        }
        return v;
    }

    private static JPanel column() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private static JPanel framed(JPanel inner) {
        JPanel frame = new JPanel(new BorderLayout());
        frame.setOpaque(true);
        frame.setBackground(CARD_BG);
        frame.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            new EmptyBorder(8, 10, 8, 10)
        ));
        frame.setAlignmentX(Component.LEFT_ALIGNMENT);
        frame.add(inner, BorderLayout.CENTER);
        packWidth(frame);
        return frame;
    }

    private static JPanel simpleCard(JPanel... rows) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(true);
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            new EmptyBorder(8, 10, 8, 10)
        ));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, 0, 2, 0);
        for (int i = 0; i < rows.length; i++) {
            c.gridy = i;
            p.add(rows[i], c);
        }
        packWidth(p);
        return p;
    }

    private static JPanel field(String label, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel key = new JLabel(label);
        key.setForeground(MUTED);
        value.setHorizontalAlignment(SwingConstants.LEFT);
        row.add(key, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        return row;
    }

    private static JPanel statLine(String key, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel k = new JLabel(key);
        k.setForeground(MUTED);
        k.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        JLabel v = new JLabel(value == null ? "-" : value);
        v.setForeground(valueColor);
        v.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        v.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(k, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    private static JLabel muted(String text) {
        JLabel l = new JLabel("<html><body style='width:200px'>" + escape(text) + "</body></html>");
        l.setForeground(MUTED);
        l.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JLabel section(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setForeground(PURPLE);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 11f));
        l.setBorder(new EmptyBorder(4, 0, 4, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JLabel valueLabel() {
        JLabel l = new JLabel("-");
        l.setForeground(VALUE);
        l.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        return l;
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

/*
 * Decompiled with CFR 0.152.
 */
package net.runelite.client.plugins.svc.license;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import net.runelite.client.plugins.svc.license.LicenseClient;

final class LicenseDialog {
    private static final Color BG = new Color(921878);
    private static final Color PANEL = new Color(1448738);
    private static final Color LINE = new Color(2830650);
    private static final Color TEXT = new Color(15134195);
    private static final Color MUTED = new Color(9147550);
    private static final Color GOLD = new Color(0xE0A000);
    private static final Color GREEN = new Color(4176208);
    private static final Color RED = new Color(16743282);

    private LicenseDialog() {
    }

    static boolean authenticate(LicenseClient licenseClient, final String string, String string2) {
        boolean[] blArray = new boolean[]{false};
        JDialog jDialog = new JDialog((Frame)null, "Orion \u2014 License", true);
        jDialog.setDefaultCloseOperation(2);
        JPanel jPanel = new JPanel(new BorderLayout(0, 0));
        jPanel.setBackground(BG);
        jPanel.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));
        JLabel jLabel = new JLabel("Orion");
        jLabel.setForeground(GOLD);
        jLabel.setFont(jLabel.getFont().deriveFont(1, 26.0f));
        JLabel jLabel2 = new JLabel("Enter your license key to continue");
        jLabel2.setForeground(MUTED);
        jLabel2.setBorder(BorderFactory.createEmptyBorder(2, 0, 16, 0));
        JPanel jPanel2 = new JPanel();
        jPanel2.setBackground(BG);
        jPanel2.setLayout(new BoxLayout(jPanel2, 1));
        jLabel.setAlignmentX(0.0f);
        jLabel2.setAlignmentX(0.0f);
        jPanel2.add(jLabel);
        jPanel2.add(jLabel2);
        final JTextField jTextField = new JTextField(string2 == null ? "" : string2);
        jTextField.setBackground(new Color(856343));
        jTextField.setForeground(TEXT);
        jTextField.setCaretColor(TEXT);
        jTextField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(9, 11, 9, 11)));
        jTextField.setFont(new Font("Monospaced", 1, 15));
        jTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JButton jButton = new JButton("Validate & Launch");
        LicenseDialog.styleButton(jButton, true);
        JLabel jLabel3 = new JLabel(" ");
        jLabel3.setForeground(MUTED);
        jLabel3.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));
        final JLabel jLabel4 = new JLabel("Machine ID: " + string + "  (click to copy)");
        jLabel4.setForeground(new Color(5989229));
        jLabel4.setFont(jLabel4.getFont().deriveFont(11.0f));
        jLabel4.setCursor(Cursor.getPredefinedCursor(12));
        jLabel4.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(string), null);
                jLabel4.setText("Machine ID copied \u2713");
            }
        });
        JPanel jPanel3 = new JPanel();
        jPanel3.setBackground(BG);
        jPanel3.setLayout(new BoxLayout(jPanel3, 1));
        jTextField.setAlignmentX(0.0f);
        jButton.setAlignmentX(0.0f);
        jButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        jLabel3.setAlignmentX(0.0f);
        jLabel4.setAlignmentX(0.0f);
        jPanel3.add(jTextField);
        jPanel3.add(Box.createVerticalStrut(12));
        jPanel3.add(jButton);
        jPanel3.add(jLabel3);
        jPanel3.add(Box.createVerticalStrut(8));
        jPanel3.add(jLabel4);
        jPanel.add((Component)jPanel2, "North");
        jPanel.add((Component)jPanel3, "Center");
        jDialog.setContentPane(jPanel);
        jDialog.setMinimumSize(new Dimension(440, 300));
        jDialog.pack();
        jDialog.setLocationRelativeTo(null);
        final Runnable[] runnableArray = new Runnable[]{() -> {
            String string2 = jTextField.getText().trim().toUpperCase();
            if (string2.isEmpty()) {
                LicenseDialog.setStatus(jLabel3, "Please enter your license key.", RED);
                return;
            }
            jButton.setEnabled(false);
            jTextField.setEnabled(false);
            LicenseDialog.setStatus(jLabel3, "Validating\u2026", MUTED);
            new Thread(() -> {
                LicenseClient.Result result = licenseClient.validate(string2, string);
                SwingUtilities.invokeLater(() -> {
                    if (result.ok) {
                        blArray[0] = true;
                        LicenseDialog.setStatus(jLabel3, "\u2713 " + result.message(), GREEN);
                        new Timer(700, actionEvent -> jDialog.dispose()){
                            {
                                this.setRepeats(false);
                            }
                        }.start();
                    } else {
                        LicenseDialog.setStatus(jLabel3, "\u2717 " + result.message(), RED);
                        jButton.setEnabled(true);
                        jTextField.setEnabled(true);
                        jTextField.requestFocusInWindow();
                    }
                });
            }, "orion-license-validate").start();
        }};
        jButton.addActionListener(actionEvent -> runnableArray[0].run());
        jTextField.addActionListener(actionEvent -> runnableArray[0].run());
        if (string2 != null && !string2.isEmpty()) {
            jDialog.addWindowListener(new WindowAdapter(){

                @Override
                public void windowOpened(WindowEvent windowEvent) {
                    runnableArray[0].run();
                }
            });
        } else {
            jDialog.addWindowListener(new WindowAdapter(){

                @Override
                public void windowOpened(WindowEvent windowEvent) {
                    jTextField.requestFocusInWindow();
                }
            });
        }
        jDialog.setVisible(true);
        return blArray[0];
    }

    private static void setStatus(JLabel jLabel, String string, Color color) {
        jLabel.setText(string);
        jLabel.setForeground(color);
    }

    private static void styleButton(JButton jButton, boolean bl) {
        jButton.setFocusPainted(false);
        jButton.setFont(jButton.getFont().deriveFont(1, 14.0f));
        jButton.setMargin(new Insets(10, 16, 10, 16));
        jButton.setHorizontalAlignment(0);
        if (bl) {
            jButton.setBackground(GOLD);
            jButton.setForeground(new Color(1708544));
        } else {
            jButton.setBackground(new Color(2172461));
            jButton.setForeground(TEXT);
        }
        jButton.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        jButton.setOpaque(true);
    }

    static void applyTheme() {
        try {
            UIManager.put("OptionPane.background", BG);
            UIManager.put("Panel.background", BG);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  com.google.common.base.Strings
 *  com.google.common.collect.ComparisonChain
 *  com.google.common.collect.Sets
 *  com.google.common.primitives.Ints
 *  net.miginfocom.swing.MigLayout
 *  net.runelite.api.events.ConfigButtonClicked
 *  net.runelite.client.config.Button
 *  net.runelite.client.config.ConfigDescriptor
 *  net.runelite.client.config.ConfigItem
 *  net.runelite.client.config.ConfigItemDescriptor
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.config.ConfigSection
 *  net.runelite.client.config.ConfigSectionDescriptor
 *  net.runelite.client.config.ConfigTitle
 *  net.runelite.client.config.ConfigTitleDescriptor
 *  net.runelite.client.config.Keybind
 *  net.runelite.client.config.ModifierlessKeybind
 *  net.runelite.client.config.Range
 *  net.runelite.client.config.Units
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.events.ConfigChanged
 *  net.runelite.client.plugins.Plugin
 *  net.runelite.client.plugins.config.FixedWidthPanel
 *  net.runelite.client.plugins.config.HotkeyButton
 *  net.runelite.client.plugins.config.PluginConfigurationDescriptor
 *  net.runelite.client.plugins.config.UnitFormatter
 *  net.runelite.client.ui.ColorScheme
 *  net.runelite.client.ui.DynamicGridLayout
 *  net.runelite.client.ui.FontManager
 *  net.runelite.client.ui.PluginPanel
 *  net.runelite.client.ui.components.ColorJButton
 *  net.runelite.client.ui.components.ComboBoxListRenderer
 *  net.runelite.client.ui.components.ToggleButton
 *  net.runelite.client.ui.components.colorpicker.ColorPickerManager
 *  net.runelite.client.ui.components.colorpicker.RuneliteColorPicker
 *  net.runelite.client.util.ColorUtil
 *  net.runelite.client.util.DeferredDocumentChangedListener
 *  net.runelite.client.util.ImageUtil
 *  net.runelite.client.util.SwingUtil
 *  net.runelite.client.util.Text
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.minimal.config;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.text.JTextComponent;
import net.miginfocom.swing.MigLayout;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.client.config.Button;
import net.runelite.client.config.ConfigDescriptor;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigItemDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.ConfigSectionDescriptor;
import net.runelite.client.config.ConfigTitle;
import net.runelite.client.config.ConfigTitleDescriptor;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.ModifierlessKeybind;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.config.FixedWidthPanel;
import net.runelite.client.plugins.config.HotkeyButton;
import net.runelite.client.plugins.config.PluginConfigurationDescriptor;
import net.runelite.client.plugins.config.UnitFormatter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ColorJButton;
import net.runelite.client.ui.components.ComboBoxListRenderer;
import net.runelite.client.ui.components.ToggleButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.DeferredDocumentChangedListener;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;
import net.runelite.client.util.Text;
import net.unethicalite.client.Static;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinimalConfigPanel
extends PluginPanel {
    private static final Logger log = LoggerFactory.getLogger(MinimalConfigPanel.class);
    private static final int SPINNER_FIELD_WIDTH = 6;
    private static final ImageIcon SECTION_EXPAND_ICON;
    private static final ImageIcon SECTION_EXPAND_ICON_HOVER;
    private static final ImageIcon SECTION_RETRACT_ICON;
    private static final ImageIcon SECTION_RETRACT_ICON_HOVER;
    private static final Map<ConfigSectionDescriptor, Boolean> sectionExpandStates;
    private final ConfigManager configManager;
    private final ColorPickerManager colorPickerManager;
    private final EventBus eventBus;
    private final ListCellRenderer<Enum<?>> listCellRenderer = new ComboBoxListRenderer();
    private final JScrollPane scrollPane;
    private final FixedWidthPanel mainPanel;
    private final JLabel title;
    private PluginConfigurationDescriptor pluginConfig = null;
    private boolean skipRebuild;
    private JFrame frame = null;

    public MinimalConfigPanel(ConfigManager configManager, ColorPickerManager colorPickerManager, EventBus eventBus) {
        super(false);
        this.configManager = configManager;
        this.colorPickerManager = colorPickerManager;
        this.eventBus = eventBus;
        this.setLayout(new BorderLayout());
        this.setBackground(ColorScheme.DARK_GRAY_COLOR);
        JPanel topPanel = new JPanel();
        topPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setLayout(new BorderLayout(0, 6));
        this.add(topPanel, "North");
        this.mainPanel = new FixedWidthPanel();
        this.mainPanel.setBorder((Border)new EmptyBorder(8, 10, 10, 10));
        this.mainPanel.setLayout((LayoutManager)new DynamicGridLayout(0, 1, 0, 5));
        this.mainPanel.setAlignmentX(0.0f);
        this.mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        FixedWidthPanel northPanel = new FixedWidthPanel();
        northPanel.setLayout(new BorderLayout());
        northPanel.add((Component)this.mainPanel, "North");
        this.scrollPane = new JScrollPane((Component)northPanel);
        this.scrollPane.setHorizontalScrollBarPolicy(31);
        this.add(this.scrollPane, "Center");
        this.title = new JLabel();
        this.title.setBackground(ColorScheme.DARK_GRAY_COLOR);
        this.title.setForeground(Color.WHITE);
        topPanel.add(this.title);
    }

    private static String htmlLabel(String key, String value) {
        return "<html><body style = 'color:#a5a5a5'>" + key + ": <span style = 'color:white'>" + value + "</span></body></html>";
    }

    public void init(PluginConfigurationDescriptor pluginConfig) {
        assert (this.pluginConfig == null);
        this.pluginConfig = pluginConfig;
        this.scrollPane.getVerticalScrollBar().setValue(0);
        String name = pluginConfig.getName();
        this.title.setText(name);
        this.title.setForeground(Color.WHITE);
        this.title.setToolTipText("<html>" + name + ":<br>" + pluginConfig.getDescription() + "</html>");
        this.rebuild(false);
        this.frame = new JFrame(pluginConfig.getDescription());
        this.frame.getContentPane().setBackground(ColorScheme.DARK_GRAY_COLOR);
        this.frame.setAlwaysOnTop(true);
        this.frame.setLayout((LayoutManager)new MigLayout());
        this.frame.setDefaultCloseOperation(2);
        this.frame.add((Component)((Object)this));
        this.frame.pack();
    }

    public void open() {
        if (this.frame != null) {
            this.frame.setVisible(!this.frame.isVisible());
            if (this.frame.isVisible()) {
                this.frame.setLocationRelativeTo(Static.getClient().getCanvas());
            }
        }
    }

    private void toggleSection(ConfigSectionDescriptor csd, JButton button, JPanel contents) {
        boolean newState = !contents.isVisible();
        contents.setVisible(newState);
        button.setIcon(newState ? SECTION_RETRACT_ICON : SECTION_EXPAND_ICON);
        button.setRolloverIcon(newState ? SECTION_RETRACT_ICON_HOVER : SECTION_EXPAND_ICON_HOVER);
        button.setToolTipText(newState ? "Retract" : "Expand");
        sectionExpandStates.put(csd, newState);
        SwingUtilities.invokeLater(() -> {
            contents.revalidate();
            this.rebuild(true);
        });
    }

    private void rebuild(boolean refresh) {
        int scrollBarPosition = this.scrollPane.getVerticalScrollBar().getValue();
        this.mainPanel.removeAll();
        ConfigDescriptor cd = this.pluginConfig.getConfigDescriptor();
        HashMap<String, JPanel> sectionWidgets = new HashMap<String, JPanel>();
        HashMap<String, JPanel> titleWidgets = new HashMap<String, JPanel>();
        TreeMap<Object, JPanel> topLevelPanels = new TreeMap<Object, JPanel>((a, b) -> ComparisonChain.start().compare(a.position(), b.position()).compare((Comparable)((Object)a.name()), (Comparable)((Object)b.name())).result());
        Iterator iterator = cd.getSections().iterator();
        while (iterator.hasNext()) {
            ConfigSectionDescriptor csd;
            ConfigSection cs = (csd = (ConfigSectionDescriptor)iterator.next()).getSection();
            boolean isOpen = sectionExpandStates.getOrDefault(csd, !cs.closedByDefault());
            JPanel section = new JPanel();
            section.setBackground(ColorScheme.DARK_GRAY_COLOR);
            section.setLayout(new BoxLayout(section, 1));
            section.setMinimumSize(new Dimension(225, 0));
            JPanel sectionHeader = new JPanel();
            sectionHeader.setLayout(new BorderLayout());
            sectionHeader.setBackground(ColorScheme.DARK_GRAY_COLOR);
            sectionHeader.setMinimumSize(new Dimension(225, 0));
            sectionHeader.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR), new EmptyBorder(0, 0, 3, 1)));
            section.add((Component)sectionHeader, "North");
            final JButton sectionToggle = new JButton(isOpen ? SECTION_RETRACT_ICON : SECTION_EXPAND_ICON);
            sectionToggle.setRolloverIcon(isOpen ? SECTION_RETRACT_ICON_HOVER : SECTION_EXPAND_ICON_HOVER);
            sectionToggle.setPreferredSize(new Dimension(18, 0));
            sectionToggle.setBorder(new EmptyBorder(0, 0, 0, 5));
            sectionToggle.setToolTipText(isOpen ? "Retract" : "Expand");
            SwingUtil.removeButtonDecorations((AbstractButton)sectionToggle);
            sectionHeader.add((Component)sectionToggle, "West");
            String name = cs.name();
            JLabel sectionName = new JLabel(name);
            sectionName.setForeground(ColorScheme.BRAND_ORANGE);
            sectionName.setFont(FontManager.getRunescapeBoldFont());
            sectionName.setToolTipText("<html>" + name + ":<br>" + cs.description() + "</html>");
            sectionHeader.add((Component)sectionName, "Center");
            final JPanel sectionContents = new JPanel();
            sectionContents.setBackground(ColorScheme.DARK_GRAY_COLOR);
            sectionContents.setLayout((LayoutManager)new DynamicGridLayout(0, 1, 0, 5));
            sectionContents.setMinimumSize(new Dimension(225, 0));
            sectionContents.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, ColorScheme.MEDIUM_GRAY_COLOR), new EmptyBorder(6, 0, 6, 0)));
            sectionContents.setVisible(isOpen);
            section.add((Component)sectionContents, "South");
            MouseAdapter adapter = new MouseAdapter(){

                @Override
                public void mouseClicked(MouseEvent e) {
                    MinimalConfigPanel.this.toggleSection(csd, sectionToggle, sectionContents);
                }
            };
            sectionToggle.addActionListener(actionEvent -> this.toggleSection(csd, sectionToggle, sectionContents));
            sectionName.addMouseListener(adapter);
            sectionHeader.addMouseListener(adapter);
            sectionWidgets.put(csd.getKey(), sectionContents);
            topLevelPanels.put(csd, section);
        }
        for (ConfigTitleDescriptor ctd : cd.getTitles()) {
            ConfigTitle ct = ctd.getTitle();
            JPanel title = new JPanel();
            title.setBackground(ColorScheme.DARK_GRAY_COLOR);
            title.setLayout(new BoxLayout(title, 1));
            title.setMinimumSize(new Dimension(225, 0));
            JPanel sectionHeader = new JPanel();
            sectionHeader.setBackground(ColorScheme.DARK_GRAY_COLOR);
            sectionHeader.setLayout(new BorderLayout());
            sectionHeader.setMinimumSize(new Dimension(225, 0));
            title.add((Component)sectionHeader, "North");
            String name = ct.name();
            JLabel sectionName = new JLabel(name);
            sectionName.setForeground(ColorScheme.BRAND_ORANGE);
            sectionName.setFont(FontManager.getRunescapeBoldFont());
            sectionName.setToolTipText("<html>" + name + ":<br>" + ct.description() + "</html>");
            sectionName.setBorder(new EmptyBorder(0, 0, 3, 1));
            sectionHeader.add((Component)sectionName, "Center");
            JPanel sectionContents = new JPanel();
            sectionContents.setBackground(ColorScheme.DARK_GRAY_COLOR);
            sectionContents.setLayout((LayoutManager)new DynamicGridLayout(0, 1, 0, 5));
            sectionContents.setMinimumSize(new Dimension(225, 0));
            sectionContents.setBorder(new EmptyBorder(0, 5, 0, 0));
            title.add((Component)sectionContents, "South");
            titleWidgets.put(ctd.getKey(), sectionContents);
            JPanel section = (JPanel)sectionWidgets.get(ct.section());
            JPanel titleSection = (JPanel)titleWidgets.get(ct.title());
            if (section != null) {
                section.add(title);
                continue;
            }
            if (titleSection != null) {
                titleSection.add(title);
                continue;
            }
            topLevelPanels.put(ctd, title);
        }
        for (ConfigItemDescriptor cid : cd.getItems()) {
            if (!this.shouldBeHidden(cid)) continue;
            JPanel item = new JPanel();
            item.setBackground(ColorScheme.DARK_GRAY_COLOR);
            item.setLayout(new BorderLayout());
            item.setMinimumSize(new Dimension(225, 0));
            String name = cid.getItem().name();
            JLabel configEntryName = new JLabel(name);
            configEntryName.setForeground(Color.WHITE);
            configEntryName.setToolTipText("<html>" + name + ":<br>" + cid.getItem().description() + "</html>");
            item.add((Component)configEntryName, "Center");
            if (cid.getType() == Button.class) {
                item.remove(configEntryName);
                item.add((Component)this.createButton(cd, cid), "Center");
            } else if (cid.getType() == Boolean.TYPE) {
                item.add((Component)this.createCheckbox(cd, cid), "East");
            } else if (cid.getType() == Integer.TYPE) {
                item.add((Component)this.createIntSpinner(cd, cid), "East");
            } else if (cid.getType() == Double.TYPE) {
                item.add((Component)this.createDoubleSpinner(cd, cid), "East");
            } else if (cid.getType() == String.class) {
                JTextComponent textField = this.createTextField(cd, cid);
                if (cid.getItem().parse()) {
                    JLabel parsingLabel = this.createParseLabel(cid, textField);
                    item.add((Component)configEntryName, "North");
                    item.add((Component)textField, "Center");
                    this.parseLabel(cid.getItem(), parsingLabel, textField.getText());
                    item.add((Component)parsingLabel, "South");
                } else {
                    item.add((Component)textField, "South");
                }
            } else if (cid.getType() == Color.class) {
                item.add((Component)this.createColorPicker(cd, cid), "East");
            } else if (cid.getType() == Dimension.class) {
                item.add((Component)this.createDimension(cd, cid), "East");
            } else if (cid.getType() instanceof Class && ((Class)cid.getType()).isEnum()) {
                item.add(this.createComboBox(cd, cid), "East");
            } else if (cid.getType() == Keybind.class || cid.getType() == ModifierlessKeybind.class) {
                item.add((Component)this.createKeybind(cd, cid), "East");
            } else if (cid.getType() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType)cid.getType();
                if (parameterizedType.getRawType() == Set.class) {
                    item.add((Component)this.createList(cd, cid), "South");
                } else if (parameterizedType.getRawType() == Consumer.class) {
                    item.remove(configEntryName);
                    item.add((Component)this.createConsumer(cd, cid), "Center");
                } else if (parameterizedType.getRawType() == EnumSet.class) {
                    item.add((Component)this.createEnumSetLayout(cd, cid), "South");
                }
            }
            JPanel section = (JPanel)sectionWidgets.get(cid.getItem().section());
            JPanel title = (JPanel)titleWidgets.get(cid.getItem().title());
            if (section != null) {
                section.add(item);
                continue;
            }
            if (title != null) {
                title.add(item);
                continue;
            }
            topLevelPanels.put(cid, item);
        }
        topLevelPanels.values().forEach(arg_0 -> this.mainPanel.add(arg_0));
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            int result = JOptionPane.showOptionDialog(resetButton, "Are you sure you want to reset this plugin's configuration?", "Are you sure?", 0, 2, null, new String[]{"Yes", "No"}, "No");
            if (result == 0) {
                this.configManager.setDefaultConfiguration(this.pluginConfig.getConfig(), true);
                Plugin plugin = this.pluginConfig.getPlugin();
                if (plugin != null) {
                    plugin.resetConfiguration();
                }
                this.rebuild(false);
            }
        });
        this.mainPanel.add((Component)resetButton);
        JButton backButton = new JButton("Back");
        this.mainPanel.add((Component)backButton);
        if (refresh) {
            this.scrollPane.getVerticalScrollBar().setValue(scrollBarPosition);
        } else {
            this.scrollPane.getVerticalScrollBar().setValue(0);
        }
        this.revalidate();
    }

    private JButton createConsumer(ConfigDescriptor cd, ConfigItemDescriptor cid) {
        JButton button = new JButton(cid.getItem().name());
        button.addActionListener(e -> {
            log.debug("Running consumer: {}.{}", (Object)cd.getGroup().value(), (Object)cid.getItem().keyName());
            this.configManager.getConsumer(cd.getGroup().value(), cid.getItem().keyName()).accept(this.pluginConfig.getPlugin());
        });
        return button;
    }

    private JButton createButton(ConfigDescriptor cd, ConfigItemDescriptor cid) {
        JButton button = new JButton(cid.name());
        button.addActionListener(e -> {
            ConfigButtonClicked event = new ConfigButtonClicked();
            event.setGroup(cd.getGroup().value());
            event.setKey(cid.getItem().keyName());
            this.eventBus.post((Object)event);
        });
        return button;
    }

    private JCheckBox createCheckbox(ConfigDescriptor cd, ConfigItemDescriptor cid) {
        ToggleButton checkbox = new ToggleButton();
        checkbox.setPreferredSize(new Dimension(26, 25));
        checkbox.setSelected(Boolean.parseBoolean(this.configManager.getConfiguration(cd.getGroup().value(), cid.getItem().keyName())));
        checkbox.addActionListener(arg_0 -> this.lambda$createCheckbox$6((JCheckBox)checkbox, cd, cid, arg_0));
        return checkbox;
    }

    private JComponent createIntSpinner(ConfigDescriptor cd, ConfigItemDescriptor cid) {
        int value = Integer.parseInt(this.configManager.getConfiguration(cd.getGroup().value(), cid.getItem().keyName()));
        Units units = cid.getUnits();
        Range range = cid.getRange();
        int min = 0;
        int max = Integer.MAX_VALUE;
        if (range != null) {
            min = range.min();
            max = range.max();
        }
        value = Ints.constrainToRange((int)value, (int)min, (int)max);
        if (max < Integer.MAX_VALUE) {
            final JLabel sliderValueLabel = new JLabel();
            final JSlider slider = new JSlider(min, max, value);
            slider.setBackground(ColorScheme.DARK_GRAY_COLOR);
            if (units != null) {
                sliderValueLabel.setText(slider.getValue() + units.value());
            } else {
                sliderValueLabel.setText(String.valueOf(slider.getValue()));
            }
            slider.setPreferredSize(new Dimension(80, 25));
            slider.addChangeListener(l -> {
                if (units != null) {
                    sliderValueLabel.setText(slider.getValue() + units.value());
                } else {
                    sliderValueLabel.setText(String.valueOf(slider.getValue()));
                }
                if (!slider.getValueIsAdjusting()) {
                    this.changeConfiguration(slider, cd, cid);
                }
            });
            SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, 1);
            final JSpinner spinner = new JSpinner(model);
            JComponent editor = spinner.getEditor();
            JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor)editor).getTextField();
            spinnerTextField.setColumns(6);
            spinner.setUI(new BasicSpinnerUI(){

                @Override
                protected Component createNextButton() {
                    return null;
                }

                @Override
                protected Component createPreviousButton() {
                    return null;
                }
            });
            final JPanel subPanel = new JPanel();
            subPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
            subPanel.setPreferredSize(new Dimension(110, 25));
            subPanel.setLayout(new BorderLayout());
            spinner.addChangeListener(ce -> {
                this.changeConfiguration(spinner, cd, cid);
                if (units != null) {
                    sliderValueLabel.setText(String.valueOf(spinner.getValue()) + units.value());
                } else {
                    sliderValueLabel.setText(String.valueOf(spinner.getValue()));
                }
                slider.setValue((Integer)spinner.getValue());
                subPanel.add((Component)sliderValueLabel, "West");
                subPanel.add((Component)slider, "East");
                subPanel.remove(spinner);
                this.validate();
                this.repaint();
            });
            sliderValueLabel.addMouseListener(new MouseAdapter(){

                @Override
                public void mouseClicked(MouseEvent e) {
                    spinner.setValue(slider.getValue());
                    subPanel.remove(sliderValueLabel);
                    subPanel.remove(slider);
                    subPanel.add((Component)spinner, "East");
                    MinimalConfigPanel.this.validate();
                    MinimalConfigPanel.this.repaint();
                    JFormattedTextField tf = ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
                    tf.requestFocusInWindow();
                    SwingUtilities.invokeLater(tf::selectAll);
                }
            });
            subPanel.add((Component)sliderValueLabel, "West");
            subPanel.add((Component)slider, "East");
            return subPanel;
        }
        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, 1);
        JSpinner spinner = new JSpinner(model);
        JComponent editor = spinner.getEditor();
        JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor)editor).getTextField();
        spinnerTextField.setColumns(6);
        spinner.addChangeListener(ce -> this.changeConfiguration(spinner, cd, cid));
        if (units != null) {
            spinnerTextField.setFormatterFactory(new UnitFormatterFactory(units));
        }
        return spinner;
    }

    private JSpinner createDoubleSpinner(ConfigDescriptor cd, ConfigItemDescriptor cid) {
        double value = (Double)this.configManager.getConfiguration(cd.getGroup().value(), cid.getItem().keyName(), Double.TYPE);
        SpinnerNumberModel model = new SpinnerNumberModel(value, 0.0, Double.MAX_VALUE, 0.1);
        JSpinner spinner = new JSpinner(model);
        JComponent editor = spinner.getEditor();
        JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor)editor).getTextField();
        spinnerTextField.setColumns(6);
        spinner.addChangeListener(ce -> this.changeConfiguration(spinner, cd, cid));
        return spinner;
    }

    private JTextComponent createTextField(final ConfigDescriptor cd, final ConfigItemDescriptor cid) {
        JTextComponent textField;
        if (cid.getItem().secret()) {
            textField = new JPasswordField();
        } else {
            JTextArea textArea = new JTextArea();
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textField = textArea;
        }
        textField.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
        textField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textField.setText(this.configManager.getConfiguration(cd.getGroup().value(), cid.getItem().keyName()));
        textField.setForeground(Color.WHITE);
        textField.addFocusListener(new FocusAdapter(){

            @Override
            public void focusLost(FocusEvent e) {
                MinimalConfigPanel.this.changeConfiguration(textField, cd, cid);
            }
        });
        return textField;
    }

    private JLabel createParseLabel(ConfigItemDescriptor cid, JTextComponent textField) {
        JLabel parsingLabel = new JLabel();
        parsingLabel.setHorizontalAlignment(0);
        parsingLabel.setPreferredSize(new Dimension(225, 15));
        DeferredDocumentChangedListener listener = new DeferredDocumentChangedListener();
        listener.addChangeListener(e -> {
            if (cid.getItem().parse()) {
                this.parseLabel(cid.getItem(), parsingLabel, textField.getText());
            }
        });
        textField.getDocument().addDocumentListener((DocumentListener)listener);
        return parsingLabel;
    }

    private ColorJButton createColorPicker(final ConfigDescriptor cd, final ConfigItemDescriptor cid) {
        ColorJButton colorPickerBtn;
        boolean alphaHidden;
        Color existing = (Color)this.configManager.getConfiguration(cd.getGroup().value(), cid.getItem().keyName(), Color.class);
        boolean bl = alphaHidden = cid.getAlpha() == null;
        if (existing == null) {
            colorPickerBtn = new ColorJButton("Pick a color", Color.BLACK);
        } else {
            String colorHex = "#" + (alphaHidden ? ColorUtil.colorToHexCode((Color)existing) : ColorUtil.colorToAlphaHexCode((Color)existing)).toUpperCase();
            colorPickerBtn = new ColorJButton(colorHex, existing);
        }
        colorPickerBtn.setFocusable(false);
        colorPickerBtn.addMouseListener((MouseListener)new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                RuneliteColorPicker colorPicker = MinimalConfigPanel.this.colorPickerManager.create(SwingUtilities.windowForComponent((Component)((Object)MinimalConfigPanel.this)), colorPickerBtn.getColor(), cid.getItem().name(), alphaHidden);
                colorPicker.setLocation(MinimalConfigPanel.this.getLocationOnScreen());
                colorPicker.setOnColorChange(c -> {
                    colorPickerBtn.setColor(c);
                    colorPickerBtn.setText("#" + (alphaHidden ? ColorUtil.colorToHexCode((Color)c) : ColorUtil.colorToAlphaHexCode((Color)c)).toUpperCase());
                });
                colorPicker.setOnClose(c -> MinimalConfigPanel.this.changeConfiguration((Component)colorPicker, cd, cid));
                colorPicker.setVisible(true);
            }
        });
        return colorPickerBtn;
    }

    private JPanel createDimension(ConfigDescriptor cd, ConfigItemDescriptor cid) {
        JPanel dimensionPanel = new JPanel();
        dimensionPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        dimensionPanel.setLayout(new BorderLayout());
        String str = this.configManager.getConfiguration(cd.getGroup().value(), cid.getItem().keyName());
        String[] splitStr = str.split("x");
        int width = Integer.parseInt(splitStr[0]);
        int height = Integer.parseInt(splitStr[1]);
        SpinnerNumberModel widthModel = new SpinnerNumberModel(width, 0, Integer.MAX_VALUE, 1);
        JSpinner widthSpinner = new JSpinner(widthModel);
        JComponent widthEditor = widthSpinner.getEditor();
        JFormattedTextField widthSpinnerTextField = ((JSpinner.DefaultEditor)widthEditor).getTextField();
        widthSpinnerTextField.setColumns(4);
        SpinnerNumberModel heightModel = new SpinnerNumberModel(height, 0, Integer.MAX_VALUE, 1);
        JSpinner heightSpinner = new JSpinner(heightModel);
        JComponent heightEditor = heightSpinner.getEditor();
        JFormattedTextField heightSpinnerTextField = ((JSpinner.DefaultEditor)heightEditor).getTextField();
        heightSpinnerTextField.setColumns(4);
        ChangeListener listener = e -> this.configManager.setConfiguration(cd.getGroup().value(), cid.getItem().keyName(), String.valueOf(widthSpinner.getValue()) + "x" + String.valueOf(heightSpinner.getValue()));
        widthSpinner.addChangeListener(listener);
        heightSpinner.addChangeListener(listener);
        dimensionPanel.add((Component)widthSpinner, "West");
        dimensionPanel.add((Component)new JLabel(" x "), "Center");
        dimensionPanel.add((Component)heightSpinner, "East");
        return dimensionPanel;
    }

    private JComboBox<Enum<?>> createComboBox(ConfigDescriptor cd, ConfigItemDescriptor cid) {
        Class type = (Class)cid.getType();
        JComboBox box = new JComboBox((Enum[])type.getEnumConstants());
        box.setBackground(ColorScheme.DARK_GRAY_COLOR);
        box.setRenderer(this.listCellRenderer);
        box.setPreferredSize(new Dimension(box.getPreferredSize().width, 25));
        box.setForeground(Color.WHITE);
        box.setFocusable(false);
        try {
            Object selectedItem = Enum.valueOf(type, this.configManager.getConfiguration(cd.getGroup().value(), cid.getItem().keyName()));
            box.setSelectedItem(selectedItem);
            box.setToolTipText(Text.titleCase(selectedItem));
        }
        catch (IllegalArgumentException ex) {
            log.debug("invalid selected item", (Throwable)ex);
        }
        box.addItemListener(e -> {
            if (e.getStateChange() == 1) {
                this.changeConfiguration(box, cd, cid);
                box.setToolTipText(Text.titleCase((Enum)((Enum)box.getSelectedItem())));
            }
        });
        return box;
    }

    private HotkeyButton createKeybind(final ConfigDescriptor cd, final ConfigItemDescriptor cid) {
        Keybind startingValue = (Keybind)this.configManager.getConfiguration(cd.getGroup().value(), cid.getItem().keyName(), (Type)((Class)cid.getType()));
        final HotkeyButton button = new HotkeyButton(startingValue, cid.getType() == ModifierlessKeybind.class);
        button.addFocusListener((FocusListener)new FocusAdapter(){

            @Override
            public void focusLost(FocusEvent e) {
                MinimalConfigPanel.this.changeConfiguration((Component)button, cd, cid);
            }
        });
        return button;
    }

    private JPanel createList(ConfigDescriptor cd, ConfigItemDescriptor cid) {
        ParameterizedType parameterizedType = (ParameterizedType)cid.getType();
        Class type = (Class)parameterizedType.getActualTypeArguments()[0];
        Set set = (Set)this.configManager.getConfiguration(cd.getGroup().value(), null, cid.getItem().keyName(), (Type)parameterizedType);
        JPanel enumsetLayout = new JPanel(new GridLayout(0, 2));
        enumsetLayout.setBackground(ColorScheme.DARK_GRAY_COLOR);
        ArrayList<ToggleButton> jcheckboxes = new ArrayList<ToggleButton>();
        HashSet selectedItems = new HashSet(Objects.requireNonNullElse(set, Collections.emptySet()));
        for (Enum obj : (Enum[])type.getEnumConstants()) {
            ToggleButton checkbox2 = new ToggleButton((Object)obj);
            checkbox2.setBackground(ColorScheme.DARK_GRAY_COLOR);
            checkbox2.setSelected(selectedItems.contains(obj));
            jcheckboxes.add(checkbox2);
            enumsetLayout.add((Component)checkbox2);
        }
        jcheckboxes.forEach(checkbox -> checkbox.addActionListener(ae -> this.changeConfiguration(jcheckboxes, cd, cid)));
        return enumsetLayout;
    }

    private JPanel createEnumSetLayout(ConfigDescriptor cd, ConfigItemDescriptor cid) {
        Class enumType = cid.getItem().enumClass();
        EnumSet enumSet = (EnumSet)this.configManager.getConfiguration(cd.getGroup().value(), cid.getItem().keyName(), EnumSet.class);
        if (enumSet == null || enumSet.contains(null)) {
            enumSet = EnumSet.noneOf(enumType);
        }
        JPanel enumsetLayout = new JPanel(new GridLayout(0, 2));
        enumsetLayout.setBackground(ColorScheme.DARK_GRAY_COLOR);
        ArrayList<ToggleButton> jcheckboxes = new ArrayList<ToggleButton>();
        for (Object obj : enumType.getEnumConstants()) {
            String option = Text.titleCase((Enum)((Enum)obj));
            ToggleButton checkbox2 = new ToggleButton(option);
            checkbox2.setBackground(ColorScheme.DARK_GRAY_COLOR);
            checkbox2.setSelected(enumSet.contains(obj));
            jcheckboxes.add(checkbox2);
            enumsetLayout.add((Component)checkbox2);
        }
        jcheckboxes.forEach(checkbox -> checkbox.addActionListener(ae -> this.changeConfiguration(jcheckboxes, cd, cid)));
        return enumsetLayout;
    }

    private Boolean parse(ConfigItem item, String value) {
        try {
            Method parse = item.clazz().getMethod(item.method(), String.class);
            return (boolean)((Boolean)parse.invoke(null, value));
        }
        catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            log.error("Parsing failed: {}", (Object)ex.getMessage());
            return null;
        }
    }

    private void parseLabel(ConfigItem item, JLabel label, String value) {
        Boolean result = this.parse(item, value);
        if (result == null) {
            label.setForeground(Color.RED);
            label.setText("Parsing failed");
        } else if (result.booleanValue()) {
            label.setForeground(Color.GREEN);
            label.setText("Valid input");
        } else {
            label.setForeground(Color.RED);
            label.setText("Invalid input");
        }
    }

    private void changeConfiguration(List<ToggleButton> components, ConfigDescriptor cd, ConfigItemDescriptor cid) {
        if (cid.getItem().enumClass() != Enum.class) {
            EnumSet enumSet = EnumSet.noneOf(cid.getItem().enumClass());
            components.forEach(value -> {
                if (value.isSelected()) {
                    enumSet.add(Enum.valueOf(cid.getItem().enumClass(), String.valueOf(value.getText()).toUpperCase().replace(" ", "_")));
                }
            });
            this.configManager.setConfiguration(cd.getGroup().value(), cid.getItem().keyName(), enumSet);
        } else {
            Set values = components.stream().filter(AbstractButton::isSelected).map(ToggleButton::getObject).collect(Collectors.toSet());
            this.configManager.setConfiguration(cd.getGroup().value(), cid.getItem().keyName(), values);
        }
    }

    private void changeConfiguration(Component component, ConfigDescriptor cd, ConfigItemDescriptor cid) {
        int result;
        ConfigItem configItem = cid.getItem();
        if (!Strings.isNullOrEmpty((String)configItem.warning()) && (result = JOptionPane.showOptionDialog(component, configItem.warning(), "Are you sure?", 0, 2, null, new String[]{"Yes", "No"}, "No")) != 0) {
            this.rebuild(false);
            return;
        }
        this.skipRebuild = true;
        if (component instanceof JCheckBox) {
            JCheckBox checkbox = (JCheckBox)component;
            this.configManager.setConfiguration(cd.getGroup().value(), cid.getItem().keyName(), "" + checkbox.isSelected());
        } else if (component instanceof JSpinner) {
            JSpinner spinner = (JSpinner)component;
            this.configManager.setConfiguration(cd.getGroup().value(), cid.getItem().keyName(), String.valueOf(spinner.getValue()));
        } else if (component instanceof JSlider) {
            JSlider slider = (JSlider)component;
            this.configManager.setConfiguration(cd.getGroup().value(), cid.getItem().keyName(), (Object)slider.getValue());
        } else if (component instanceof JTextComponent) {
            JTextComponent textField = (JTextComponent)component;
            this.configManager.setConfiguration(cd.getGroup().value(), cid.getItem().keyName(), textField.getText());
        } else if (component instanceof RuneliteColorPicker) {
            RuneliteColorPicker colorPicker = (RuneliteColorPicker)component;
            this.configManager.setConfiguration(cd.getGroup().value(), cid.getItem().keyName(), "" + colorPicker.getSelectedColor().getRGB());
        } else if (component instanceof JComboBox) {
            JComboBox jComboBox = (JComboBox)component;
            this.configManager.setConfiguration(cd.getGroup().value(), cid.getItem().keyName(), ((Enum)jComboBox.getSelectedItem()).name());
        } else if (component instanceof HotkeyButton) {
            HotkeyButton hotkeyButton = (HotkeyButton)component;
            this.configManager.setConfiguration(cd.getGroup().value(), cid.getItem().keyName(), (Object)hotkeyButton.getValue());
        } else if (component instanceof JList) {
            JList list = (JList)component;
            List selectedValues = list.getSelectedValuesList();
            this.configManager.setConfiguration(cd.getGroup().value(), cid.getItem().keyName(), (Object)Sets.newHashSet(selectedValues));
        }
        if (this.enableDisable(component, cid) || this.hideUnhide(component, cd, cid)) {
            this.rebuild(true);
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(242, super.getPreferredSize().height);
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if (this.pluginConfig.getConfigDescriptor() == null) {
            return;
        }
        if (!this.skipRebuild && this.pluginConfig.getConfigDescriptor().getGroup().value().equals(event.getGroup())) {
            SwingUtilities.invokeLater(() -> this.rebuild(true));
        }
        this.skipRebuild = false;
    }

    private boolean hideUnhide(Component component, ConfigDescriptor cd, ConfigItemDescriptor cid) {
        boolean rebuild;
        block10: {
            block11: {
                block9: {
                    rebuild = false;
                    if (!(component instanceof JCheckBox)) break block9;
                    JCheckBox checkbox = (JCheckBox)component;
                    for (ConfigItemDescriptor cid2 : cd.getItems()) {
                        List itemHide;
                        if ((cid2.getItem().hidden() || !cid2.getItem().hide().isEmpty()) && (itemHide = Splitter.onPattern((String)"\\|\\|").trimResults().omitEmptyStrings().splitToList((CharSequence)String.format("%s || %s", cid2.getItem().unhide(), cid2.getItem().hide()))).contains(cid.getItem().keyName())) {
                            rebuild = true;
                        }
                        if (!checkbox.isSelected()) continue;
                        if (cid2.getItem().enabledBy().contains(cid.getItem().keyName())) {
                            this.skipRebuild = true;
                            this.configManager.setConfiguration(cd.getGroup().value(), cid2.getItem().keyName(), "true");
                            rebuild = true;
                            continue;
                        }
                        if (!cid2.getItem().disabledBy().contains(cid.getItem().keyName())) continue;
                        this.skipRebuild = true;
                        this.configManager.setConfiguration(cd.getGroup().value(), cid2.getItem().keyName(), "false");
                        rebuild = true;
                    }
                    break block10;
                }
                if (!(component instanceof JComboBox)) break block11;
                JComboBox jComboBox = (JComboBox)component;
                for (ConfigItemDescriptor cid2 : cd.getItems()) {
                    if (!cid2.getItem().hidden() && cid2.getItem().hide().isEmpty()) continue;
                    List itemHide = Splitter.onPattern((String)"\\|\\|").trimResults().omitEmptyStrings().splitToList((CharSequence)String.format("%s || %s", cid2.getItem().unhide(), cid2.getItem().hide()));
                    String changedVal = ((Enum)jComboBox.getSelectedItem()).name();
                    if (cid2.getItem().enabledBy().contains(cid.getItem().keyName()) && cid2.getItem().enabledByValue().equals(changedVal)) {
                        this.skipRebuild = true;
                        this.configManager.setConfiguration(cd.getGroup().value(), cid2.getItem().keyName(), "true");
                        rebuild = true;
                        continue;
                    }
                    if (cid2.getItem().disabledBy().contains(cid.getItem().keyName()) && cid2.getItem().disabledByValue().equals(changedVal)) {
                        this.skipRebuild = true;
                        this.configManager.setConfiguration(cd.getGroup().value(), cid2.getItem().keyName(), "false");
                        rebuild = true;
                        continue;
                    }
                    if (!itemHide.contains(cid.getItem().keyName())) continue;
                    rebuild = true;
                }
                break block10;
            }
            if (!(component instanceof JList)) break block10;
            JList jList = (JList)component;
            for (ConfigItemDescriptor cid2 : cd.getItems()) {
                if (!cid2.getItem().hidden() && cid2.getItem().hide().isEmpty()) continue;
                List itemHide = Splitter.onPattern((String)"\\|\\|").trimResults().omitEmptyStrings().splitToList((CharSequence)String.format("%s || %s", cid2.getItem().unhide(), cid2.getItem().hide()));
                String changedVal = String.valueOf(jList.getSelectedValues());
                if (cid2.getItem().enabledBy().contains(cid.getItem().keyName()) && cid2.getItem().enabledByValue().equals(changedVal)) {
                    this.skipRebuild = true;
                    this.configManager.setConfiguration(cd.getGroup().value(), cid2.getItem().keyName(), "true");
                    rebuild = true;
                    continue;
                }
                if (cid2.getItem().disabledBy().contains(cid.getItem().keyName()) && cid2.getItem().disabledByValue().equals(changedVal)) {
                    this.skipRebuild = true;
                    this.configManager.setConfiguration(cd.getGroup().value(), cid2.getItem().keyName(), "false");
                    rebuild = true;
                    continue;
                }
                if (!itemHide.contains(cid.getItem().keyName())) continue;
                rebuild = true;
            }
        }
        return rebuild;
    }

    private boolean shouldBeHidden(ConfigItemDescriptor cid) {
        boolean hide;
        ConfigDescriptor cd = this.pluginConfig.getConfigDescriptor();
        boolean unhide = cid.getItem().hidden();
        boolean bl = hide = !cid.getItem().hide().isEmpty();
        if (unhide || hide) {
            boolean show = false;
            List itemHide = Splitter.onPattern((String)"\\|\\|").trimResults().omitEmptyStrings().splitToList((CharSequence)String.format("%s || %s", cid.getItem().unhide(), cid.getItem().hide()));
            for (ConfigItemDescriptor cid2 : cd.getItems()) {
                if (!itemHide.contains(cid2.getItem().keyName())) continue;
                if (cid2.getType() == Boolean.TYPE) {
                    show = Boolean.parseBoolean(this.configManager.getConfiguration(cd.getGroup().value(), cid2.getItem().keyName()));
                    continue;
                }
                if (!(cid2.getType() instanceof Class) || !((Class)cid2.getType()).isEnum()) continue;
                Class type = (Class)cid2.getType();
                try {
                    Object selectedItem = Enum.valueOf(type, this.configManager.getConfiguration(cd.getGroup().value(), cid2.getItem().keyName()));
                    if (!cid.getItem().unhideValue().equals("")) {
                        List unhideValue = Splitter.onPattern((String)"\\|\\|").trimResults().omitEmptyStrings().splitToList((CharSequence)cid.getItem().unhideValue());
                        show = unhideValue.contains(((Enum)selectedItem).toString());
                        continue;
                    }
                    if (cid.getItem().hideValue().equals("")) continue;
                    List hideValue = Splitter.onPattern((String)"\\|\\|").trimResults().omitEmptyStrings().splitToList((CharSequence)cid.getItem().hideValue());
                    show = !hideValue.contains(((Enum)selectedItem).toString());
                }
                catch (IllegalArgumentException illegalArgumentException) {}
            }
            return !(unhide && !show || hide && show);
        }
        return true;
    }

    private boolean enableDisable(Component component, ConfigItemDescriptor cid) {
        boolean rebuild;
        block5: {
            ConfigDescriptor cd;
            block4: {
                rebuild = false;
                cd = this.pluginConfig.getConfigDescriptor();
                if (!(component instanceof JCheckBox)) break block4;
                JCheckBox checkbox = (JCheckBox)component;
                for (ConfigItemDescriptor cid2 : cd.getItems()) {
                    if (!checkbox.isSelected()) continue;
                    if (cid2.getItem().enabledBy().contains(cid.getItem().keyName())) {
                        this.skipRebuild = true;
                        this.configManager.setConfiguration(cd.getGroup().value(), cid2.getItem().keyName(), "true");
                        rebuild = true;
                        continue;
                    }
                    if (!cid2.getItem().disabledBy().contains(cid.getItem().keyName())) continue;
                    this.skipRebuild = true;
                    this.configManager.setConfiguration(cd.getGroup().value(), cid2.getItem().keyName(), "false");
                    rebuild = true;
                }
                break block5;
            }
            if (!(component instanceof JComboBox)) break block5;
            JComboBox jComboBox = (JComboBox)component;
            for (ConfigItemDescriptor cid2 : cd.getItems()) {
                String changedVal = ((Enum)jComboBox.getSelectedItem()).name();
                if (cid2.getItem().enabledBy().contains(cid.getItem().keyName()) && cid2.getItem().enabledByValue().equals(changedVal)) {
                    this.skipRebuild = true;
                    this.configManager.setConfiguration(cd.getGroup().value(), cid2.getItem().keyName(), "true");
                    rebuild = true;
                    continue;
                }
                if (!cid2.getItem().disabledBy().contains(cid.getItem().keyName()) || !cid2.getItem().disabledByValue().equals(changedVal)) continue;
                this.skipRebuild = true;
                this.configManager.setConfiguration(cd.getGroup().value(), cid2.getItem().keyName(), "false");
                rebuild = true;
            }
        }
        return rebuild;
    }

    private /* synthetic */ void lambda$createCheckbox$6(JCheckBox checkbox, ConfigDescriptor cd, ConfigItemDescriptor cid, ActionEvent ae) {
        this.changeConfiguration(checkbox, cd, cid);
    }

    static {
        sectionExpandStates = new HashMap<ConfigSectionDescriptor, Boolean>();
        BufferedImage sectionRetractIcon = ImageUtil.loadImageResource(MinimalConfigPanel.class, (String)"arrow_right.png");
        sectionRetractIcon = ImageUtil.luminanceOffset((Image)sectionRetractIcon, (int)-121);
        SECTION_EXPAND_ICON = new ImageIcon(sectionRetractIcon);
        SECTION_EXPAND_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset((Image)sectionRetractIcon, (int)-100));
        BufferedImage sectionExpandIcon = ImageUtil.rotateImage((BufferedImage)sectionRetractIcon, (double)1.5707963267948966);
        SECTION_RETRACT_ICON = new ImageIcon(sectionExpandIcon);
        SECTION_RETRACT_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset((Image)sectionExpandIcon, (int)-100));
    }

    static final class UnitFormatterFactory
    extends JFormattedTextField.AbstractFormatterFactory {
        private final Units units;
        private final Map<JFormattedTextField, JFormattedTextField.AbstractFormatter> formatters = new HashMap<JFormattedTextField, JFormattedTextField.AbstractFormatter>();

        @Override
        public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField tf) {
            return this.formatters.computeIfAbsent(tf, key -> new UnitFormatter(this.units));
        }

        public UnitFormatterFactory(Units units) {
            this.units = units;
        }
    }
}


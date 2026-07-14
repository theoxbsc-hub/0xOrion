/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.inject.Inject
 *  javax.inject.Singleton
 *  net.runelite.api.Client
 *  net.runelite.api.events.ScriptPostFired
 *  net.runelite.api.events.ScriptPreFired
 *  net.runelite.api.util.Text
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.client.config.ConfigManager
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.ui.ColorScheme
 *  net.runelite.client.ui.DynamicGridLayout
 *  net.runelite.client.ui.FontManager
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.unethicalite.client.devtools.scriptinspector;

import com.google.common.collect.Lists;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.util.Text;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.unethicalite.client.devtools.widgetinspector.WidgetInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ScriptInspector {
    private static final Logger log = LoggerFactory.getLogger(ScriptInspector.class);
    private static final String DEFAULT_BLACKLIST = "3174,1004";
    private static final int MAX_LOG_ENTRIES = 10000;
    private final Client client;
    private final EventBus eventBus;
    private final ConfigManager configManager;
    private final JPanel tracker = new JPanel();
    private ScriptTreeNode currentNode;
    private int lastTick;
    private Set<Integer> blacklist;
    private Set<Integer> highlights;
    private JList jList;
    private DefaultListModel listModel;
    private ListState state = ListState.BLACKLIST;
    private JFrame frame;

    @Inject
    ScriptInspector(Client client, EventBus eventBus, ConfigManager configManager) {
        this.eventBus = eventBus;
        this.client = client;
        this.configManager = configManager;
    }

    @Subscribe
    public void onScriptPreFired(ScriptPreFired event) {
        ScriptTreeNode newNode = new ScriptTreeNode(event.getScriptId());
        if (event.getScriptEvent() != null) {
            newNode.setSource(event.getScriptEvent().getSource());
        }
        if (this.currentNode == null) {
            this.currentNode = newNode;
        } else {
            int count = 0;
            Enumeration<TreeNode> children = this.currentNode.children();
            if (children != null) {
                while (children.hasMoreElements()) {
                    ScriptTreeNode child = (ScriptTreeNode)children.nextElement();
                    if (child.getScriptId() != event.getScriptId()) continue;
                    ++count;
                }
                newNode.setDuplicateNumber(count + 1);
            }
            this.currentNode.add(newNode);
            this.currentNode = newNode;
        }
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event) {
        if (this.currentNode == null || this.currentNode.getScriptId() != event.getScriptId()) {
            log.warn("a script was post-fired that was never pre-fired. Script id: " + event.getScriptId());
            return;
        }
        if (this.currentNode.getParent() != null) {
            this.currentNode = (ScriptTreeNode)this.currentNode.getParent();
        } else {
            this.addScriptLog(this.currentNode);
            this.currentNode = null;
        }
    }

    public void open() {
        if (this.frame != null && this.frame.isVisible()) {
            this.close();
            return;
        }
        if (this.frame == null) {
            this.frame = new JFrame();
            this.frame.setTitle("Script Inspector");
            this.frame.setLayout(new BorderLayout());
            this.frame.setDefaultCloseOperation(0);
            this.frame.addWindowListener(new WindowAdapter(){

                @Override
                public void windowClosing(WindowEvent e) {
                    ScriptInspector.this.close();
                }
            });
            this.tracker.setLayout((LayoutManager)new DynamicGridLayout(0, 1, 0, 3));
            JPanel leftSide = new JPanel();
            leftSide.setLayout(new BorderLayout());
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
            leftSide.add((Component)trackerScroller, "Center");
            JPanel bottomLeftRow = new JPanel();
            JButton clearBtn = new JButton("Clear");
            clearBtn.addActionListener(e -> {
                this.tracker.removeAll();
                this.tracker.revalidate();
            });
            bottomLeftRow.add(clearBtn);
            leftSide.add((Component)bottomLeftRow, "South");
            this.frame.add((Component)leftSide, "Center");
            String blacklistConfig = this.configManager.getConfiguration("unethicalite", "devtools.blacklist");
            if (blacklistConfig == null) {
                blacklistConfig = DEFAULT_BLACKLIST;
            }
            try {
                this.blacklist = new HashSet<Integer>(Lists.transform((List)Text.fromCSV((String)blacklistConfig), Integer::parseInt));
            }
            catch (NumberFormatException e2) {
                this.blacklist = new HashSet<Integer>(Lists.transform((List)Text.fromCSV((String)DEFAULT_BLACKLIST), Integer::parseInt));
            }
            String highlightsConfig = this.configManager.getConfiguration("unethicalite", "devtools.highlights");
            if (highlightsConfig == null) {
                highlightsConfig = "";
            }
            try {
                this.highlights = new HashSet<Integer>(Lists.transform((List)Text.fromCSV((String)highlightsConfig), Integer::parseInt));
            }
            catch (NumberFormatException e3) {
                this.blacklist = new HashSet<Integer>();
            }
            JPanel rightSide = new JPanel();
            rightSide.setLayout(new BorderLayout());
            this.listModel = new DefaultListModel();
            this.changeState(ListState.BLACKLIST);
            this.jList = new JList(this.listModel);
            this.jList.setSelectionMode(0);
            JScrollPane listScrollPane = new JScrollPane(this.jList);
            JButton blacklistButton = new JButton("Blacklist");
            blacklistButton.addActionListener(e -> this.changeState(ListState.BLACKLIST));
            JButton highlightsButton = new JButton("Highlights");
            highlightsButton.addActionListener(e -> this.changeState(ListState.HIGHLIGHT));
            JPanel topLeftRow = new JPanel();
            topLeftRow.setLayout(new FlowLayout());
            topLeftRow.add(blacklistButton);
            topLeftRow.add(highlightsButton);
            rightSide.add((Component)topLeftRow, "North");
            rightSide.add((Component)listScrollPane, "Center");
            JSpinner jSpinner = new JSpinner();
            JComponent mySpinnerEditor = jSpinner.getEditor();
            JFormattedTextField textField = ((JSpinner.DefaultEditor)mySpinnerEditor).getTextField();
            textField.setColumns(5);
            JButton addButton = new JButton("Add");
            addButton.addActionListener(e -> this.addToSet(jSpinner));
            JButton removeButton = new JButton("Remove");
            removeButton.addActionListener(e -> this.removeSelectedFromSet());
            JPanel bottomButtonRow = new JPanel();
            bottomButtonRow.setLayout(new FlowLayout());
            bottomButtonRow.add(addButton);
            bottomButtonRow.add(jSpinner);
            bottomButtonRow.add(removeButton);
            rightSide.add((Component)bottomButtonRow, "South");
            this.frame.add((Component)rightSide, "East");
            this.frame.pack();
        }
        this.eventBus.register((Object)this);
        this.frame.setVisible(true);
        this.frame.toFront();
        this.frame.repaint();
    }

    public void close() {
        this.configManager.setConfiguration("unethicalite", "devtools.highlights", Text.toCSV((Collection)Lists.transform(new ArrayList<Integer>(this.highlights), String::valueOf)));
        this.configManager.setConfiguration("unethicalite", "devtools.blacklist", Text.toCSV((Collection)Lists.transform(new ArrayList<Integer>(this.blacklist), String::valueOf)));
        this.currentNode = null;
        this.eventBus.unregister((Object)this);
        this.frame.setVisible(false);
    }

    private void addScriptLog(ScriptTreeNode treeNode) {
        if (this.blacklist.contains(treeNode.getScriptId())) {
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
            DefaultTreeModel treeModel = new DefaultTreeModel(treeNode);
            JTree tree = new JTree(treeModel);
            tree.setRootVisible(true);
            tree.setShowsRootHandles(true);
            tree.collapsePath(new TreePath(treeNode));
            ScriptTreeNode highlightNode = this.findHighlightPathNode(treeNode);
            if (highlightNode != null) {
                tree.setExpandsSelectedPaths(true);
                tree.setSelectionPath(new TreePath(treeModel.getPathToRoot(highlightNode)));
            }
            this.tracker.add(tree);
            while (this.tracker.getComponentCount() > 10000) {
                this.tracker.remove(0);
            }
            this.tracker.revalidate();
        });
    }

    private void changeState(ListState state) {
        this.state = state;
        this.refreshList();
    }

    private void addToSet(JSpinner spinner) {
        int script = (Integer)spinner.getValue();
        Set<Integer> set = this.getSet();
        set.add(script);
        this.refreshList();
        spinner.setValue(0);
    }

    private void removeSelectedFromSet() {
        int index = this.jList.getSelectedIndex();
        if (index == -1) {
            return;
        }
        int script = (Integer)this.listModel.get(index);
        this.getSet().remove(script);
        this.refreshList();
    }

    private void refreshList() {
        this.listModel.clear();
        Set<Integer> set = this.getSet();
        for (Integer i : set) {
            this.listModel.addElement(i);
        }
    }

    private Set<Integer> getSet() {
        Set<Integer> set = this.state == ListState.BLACKLIST ? this.blacklist : this.highlights;
        return set;
    }

    private ScriptTreeNode findHighlightPathNode(ScriptTreeNode node) {
        if (this.highlights.contains(node.getScriptId())) {
            return node;
        }
        Enumeration<TreeNode> children = node.children();
        if (children != null) {
            while (children.hasMoreElements()) {
                ScriptTreeNode child = (ScriptTreeNode)children.nextElement();
                ScriptTreeNode find = this.findHighlightPathNode(child);
                if (find == null) continue;
                return find;
            }
        }
        return null;
    }

    private class ScriptTreeNode
    extends DefaultMutableTreeNode {
        private final int scriptId;
        private Widget source;
        private int duplicateNumber = 1;

        @Override
        public String toString() {
            Object output = Integer.toString(this.scriptId);
            if (this.duplicateNumber != 1) {
                output = (String)output + " (" + this.duplicateNumber + ")";
            }
            if (this.source != null) {
                WidgetInfo info;
                int id = this.source.getId();
                output = (String)output + "  -  " + WidgetInfo.TO_GROUP((int)id) + "." + WidgetInfo.TO_CHILD((int)id);
                if (this.source.getIndex() != -1) {
                    output = (String)output + "[" + this.source.getIndex() + "]";
                }
                if ((info = WidgetInspector.getWidgetInfo(id)) != null) {
                    output = (String)output + " " + info.name();
                }
            }
            return output;
        }

        public ScriptTreeNode(int scriptId) {
            this.scriptId = scriptId;
        }

        public int getScriptId() {
            return this.scriptId;
        }

        public Widget getSource() {
            return this.source;
        }

        public int getDuplicateNumber() {
            return this.duplicateNumber;
        }

        public void setSource(Widget source) {
            this.source = source;
        }

        public void setDuplicateNumber(int duplicateNumber) {
            this.duplicateNumber = duplicateNumber;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ScriptTreeNode)) {
                return false;
            }
            ScriptTreeNode other = (ScriptTreeNode)o;
            if (!other.canEqual(this)) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            if (this.getScriptId() != other.getScriptId()) {
                return false;
            }
            if (this.getDuplicateNumber() != other.getDuplicateNumber()) {
                return false;
            }
            Widget this$source = this.getSource();
            Widget other$source = other.getSource();
            return !(this$source == null ? other$source != null : !this$source.equals(other$source));
        }

        protected boolean canEqual(Object other) {
            return other instanceof ScriptTreeNode;
        }

        public int hashCode() {
            int PRIME = 59;
            int result = super.hashCode();
            result = result * 59 + this.getScriptId();
            result = result * 59 + this.getDuplicateNumber();
            Widget $source = this.getSource();
            result = result * 59 + ($source == null ? 43 : $source.hashCode());
            return result;
        }
    }

    private static enum ListState {
        BLACKLIST,
        HIGHLIGHT;

    }
}


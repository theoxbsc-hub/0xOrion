/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.inject.Provider
 *  com.google.inject.Singleton
 *  javax.inject.Inject
 *  net.runelite.api.Client
 *  net.runelite.api.MenuAction
 *  net.runelite.api.MenuEntry
 *  net.runelite.api.events.MenuEntryAdded
 *  net.runelite.api.events.MenuOptionClicked
 *  net.runelite.api.widgets.Widget
 *  net.runelite.api.widgets.WidgetInfo
 *  net.runelite.api.widgets.WidgetItem
 *  net.runelite.client.callback.ClientThread
 *  net.runelite.client.eventbus.EventBus
 *  net.runelite.client.eventbus.Subscribe
 *  net.runelite.client.ui.overlay.Overlay
 *  net.runelite.client.ui.overlay.OverlayManager
 *  net.runelite.client.util.ColorUtil
 */
package net.unethicalite.client.devtools.widgetinspector;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.unethicalite.client.devtools.widgetinspector.WidgetInfoTableModel;
import net.unethicalite.client.devtools.widgetinspector.WidgetInspectorOverlay;
import net.unethicalite.client.devtools.widgetinspector.WidgetItemNode;
import net.unethicalite.client.devtools.widgetinspector.WidgetTreeNode;

@Singleton
public class WidgetInspector {
    static final Color SELECTED_WIDGET_COLOR = Color.CYAN;
    private static final Map<Integer, WidgetInfo> widgetIdMap = new HashMap<Integer, WidgetInfo>();
    private static final float SELECTED_WIDGET_HUE;
    private final Provider<WidgetInspectorOverlay> overlay;
    private final OverlayManager overlayManager;
    private final WidgetInfoTableModel infoTableModel;
    private final ClientThread clientThread;
    private final Client client;
    private JTree widgetTree;
    private JCheckBox hideHidden;
    private DefaultMutableTreeNode root;
    private Widget selectedWidget;
    private int selectedItem;
    private Widget picker = null;
    private boolean pickerSelected = false;
    private JFrame frame;

    @Inject
    private WidgetInspector(Client client, ClientThread clientThread, WidgetInfoTableModel infoTableModel, EventBus eventBus, Provider<WidgetInspectorOverlay> overlay, OverlayManager overlayManager) {
        this.infoTableModel = infoTableModel;
        this.overlay = overlay;
        this.overlayManager = overlayManager;
        this.client = client;
        this.clientThread = clientThread;
        eventBus.register((Object)this);
    }

    public static WidgetInfo getWidgetInfo(int packedId) {
        if (widgetIdMap.isEmpty()) {
            WidgetInfo[] widgets;
            for (WidgetInfo w : widgets = WidgetInfo.values()) {
                widgetIdMap.put(w.getPackedId(), w);
            }
        }
        return widgetIdMap.get(packedId);
    }

    private void refreshWidgets() {
        this.clientThread.invokeLater(() -> {
            Widget[] rootWidgets = this.client.getWidgetRoots();
            this.root = new DefaultMutableTreeNode();
            Widget wasSelectedWidget = this.selectedWidget;
            int wasSelectedItem = this.selectedItem;
            this.selectedWidget = null;
            this.selectedItem = -1;
            for (Widget widget : rootWidgets) {
                DefaultMutableTreeNode childNode = this.addWidget("R", widget);
                if (childNode == null) continue;
                this.root.add(childNode);
            }
            SwingUtilities.invokeLater(() -> {
                this.widgetTree.setModel(new DefaultTreeModel(this.root));
                this.setSelectedWidget(wasSelectedWidget, wasSelectedItem, true);
            });
        });
    }

    private DefaultMutableTreeNode addWidget(String type, Widget widget) {
        Collection items;
        DefaultMutableTreeNode childNode;
        if (widget == null || this.hideHidden.isSelected() && widget.isHidden()) {
            return null;
        }
        WidgetTreeNode node = new WidgetTreeNode(type, widget);
        Widget[] childComponents = widget.getDynamicChildren();
        if (childComponents != null) {
            for (Widget component : childComponents) {
                childNode = this.addWidget("D", component);
                if (childNode == null) continue;
                node.add(childNode);
            }
        }
        if ((childComponents = widget.getStaticChildren()) != null) {
            for (Widget component : childComponents) {
                childNode = this.addWidget("S", component);
                if (childNode == null) continue;
                node.add(childNode);
            }
        }
        if ((childComponents = widget.getNestedChildren()) != null) {
            for (Widget component : childComponents) {
                childNode = this.addWidget("N", component);
                if (childNode == null) continue;
                node.add(childNode);
            }
        }
        if ((items = widget.getWidgetItems()) != null) {
            for (WidgetItem item : items) {
                if (item == null) continue;
                node.add(new WidgetItemNode(item));
            }
        }
        return node;
    }

    private void setSelectedWidget(Widget widget, int item, boolean updateTree) {
        this.infoTableModel.setWidget(widget);
        if (this.selectedWidget == widget && this.selectedItem == item) {
            return;
        }
        this.selectedWidget = widget;
        this.selectedItem = item;
        if (this.root == null || !updateTree) {
            return;
        }
        this.clientThread.invoke(() -> {
            DefaultMutableTreeNode inner;
            Stack<Widget> treePath = new Stack<Widget>();
            for (Widget w = widget; w != null; w = w.getParent()) {
                treePath.push(w);
            }
            DefaultMutableTreeNode node = this.root;
            block1: while (!treePath.empty()) {
                Widget w = (Widget)treePath.pop();
                Enumeration<TreeNode> it = node.children();
                while (it.hasMoreElements()) {
                    inner = (WidgetTreeNode)it.nextElement();
                    if (((WidgetTreeNode)inner).getWidget().getId() != w.getId() || ((WidgetTreeNode)inner).getWidget().getIndex() != w.getIndex()) continue;
                    node = inner;
                    continue block1;
                }
            }
            if (this.selectedItem != -1) {
                Enumeration<TreeNode> it = node.children();
                while (it.hasMoreElements()) {
                    TreeNode wiw = it.nextElement();
                    if (!(wiw instanceof WidgetItemNode) || ((WidgetItemNode)(inner = (WidgetItemNode)wiw)).getWidgetItem().getIndex() != this.selectedItem) continue;
                    node = inner;
                    break;
                }
            }
            DefaultMutableTreeNode fnode = node;
            SwingUtilities.invokeLater(() -> {
                this.widgetTree.getSelectionModel().clearSelection();
                this.widgetTree.getSelectionModel().addSelectionPath(new TreePath(fnode.getPath()));
            });
        });
    }

    public void open() {
        if (this.frame != null && this.frame.isVisible()) {
            this.close();
            return;
        }
        if (this.frame == null) {
            this.frame = new JFrame();
            this.frame.setAlwaysOnTop(true);
            this.frame.setTitle("Widget Inspector");
            this.frame.setLayout(new BorderLayout());
            this.frame.addWindowListener(new WindowAdapter(){

                @Override
                public void windowClosing(WindowEvent e) {
                    WidgetInspector.this.close();
                }
            });
            this.widgetTree = new JTree(new DefaultMutableTreeNode());
            this.widgetTree.setRootVisible(false);
            this.widgetTree.setShowsRootHandles(true);
            this.widgetTree.getSelectionModel().addTreeSelectionListener(e -> {
                Object selected = this.widgetTree.getLastSelectedPathComponent();
                if (selected instanceof WidgetTreeNode) {
                    WidgetTreeNode node = (WidgetTreeNode)selected;
                    Widget widget = node.getWidget();
                    this.setSelectedWidget(widget, -1, false);
                } else if (selected instanceof WidgetItemNode) {
                    WidgetItemNode node = (WidgetItemNode)selected;
                    this.setSelectedWidget(node.getWidgetItem().getWidget(), node.getWidgetItem().getIndex(), false);
                }
            });
            JScrollPane treeScrollPane = new JScrollPane(this.widgetTree);
            treeScrollPane.setPreferredSize(new Dimension(200, 400));
            JTable widgetInfo = new JTable(this.infoTableModel);
            JScrollPane infoScrollPane = new JScrollPane(widgetInfo);
            infoScrollPane.setPreferredSize(new Dimension(400, 400));
            JPanel bottomPanel = new JPanel();
            this.frame.add((Component)bottomPanel, "South");
            JButton refreshWidgetsBtn = new JButton("Refresh");
            refreshWidgetsBtn.addActionListener(e -> this.refreshWidgets());
            bottomPanel.add(refreshWidgetsBtn);
            this.hideHidden = new JCheckBox("Hide hidden");
            this.hideHidden.setSelected(true);
            this.hideHidden.addItemListener(ev -> this.refreshWidgets());
            bottomPanel.add(this.hideHidden);
            JButton revalidateWidget = new JButton("Revalidate");
            revalidateWidget.addActionListener(ev -> this.clientThread.invokeLater(() -> {
                if (this.selectedWidget == null) {
                    return;
                }
                this.selectedWidget.revalidate();
            }));
            bottomPanel.add(revalidateWidget);
            JSplitPane split = new JSplitPane(1, treeScrollPane, infoScrollPane);
            this.frame.add((Component)split, "Center");
            this.frame.setLocationRelativeTo(this.client.getCanvas());
            this.frame.pack();
        }
        this.frame.setVisible(true);
        this.frame.toFront();
        this.frame.repaint();
        this.overlayManager.add((Overlay)this.overlay.get());
        this.clientThread.invokeLater(this::addPickerWidget);
    }

    public void close() {
        this.overlayManager.remove((Overlay)this.overlay.get());
        this.clientThread.invokeLater(this::removePickerWidget);
        this.setSelectedWidget(null, -1, false);
        this.frame.setVisible(false);
    }

    private void removePickerWidget() {
        if (this.picker == null) {
            return;
        }
        Widget parent = this.picker.getParent();
        if (parent == null) {
            return;
        }
        Widget[] children = parent.getChildren();
        if (children == null || children.length <= this.picker.getIndex() || children[this.picker.getIndex()] != this.picker) {
            return;
        }
        children[this.picker.getIndex()] = null;
    }

    private void addPickerWidget() {
        this.removePickerWidget();
        int x = 10;
        int y = 2;
        Widget parent = this.client.getWidget(WidgetInfo.MINIMAP_ORBS);
        if (parent == null) {
            Widget[] roots = this.client.getWidgetRoots();
            parent = Stream.of(roots).filter(w -> w.getType() == 0 && w.getContentType() == 0 && !w.isSelfHidden()).max(Comparator.comparingInt(w -> w.getRelativeX() + w.getRelativeY()).reversed().thenComparingInt(Widget::getId)).get();
            x = 4;
            y = 4;
        }
        this.picker = parent.createChild(-1, 5);
        this.picker.setSpriteId(1653);
        this.picker.setOriginalWidth(15);
        this.picker.setOriginalHeight(17);
        this.picker.setOriginalX(x);
        this.picker.setOriginalY(y);
        this.picker.revalidate();
        this.picker.setTargetVerb("Select");
        this.picker.setName("Pick");
        this.picker.setClickMask(98304);
        this.picker.setNoClickThrough(true);
        this.picker.setOnTargetEnterListener(new Object[]{ev -> {
            this.pickerSelected = true;
            this.picker.setOpacity(30);
            this.client.setAllWidgetsAreOpTargetable(true);
        }});
        this.picker.setOnTargetLeaveListener(new Object[]{ev -> this.onPickerDeselect()});
    }

    private void onPickerDeselect() {
        this.client.setAllWidgetsAreOpTargetable(false);
        this.picker.setOpacity(0);
        this.pickerSelected = false;
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked ev) {
        if (!this.pickerSelected) {
            return;
        }
        this.onPickerDeselect();
        this.client.setSpellSelected(false);
        ev.consume();
        Object target = this.getWidgetOrWidgetItemForMenuOption(ev.getMenuAction().getId(), ev.getParam0(), ev.getParam1());
        if (target == null) {
            return;
        }
        if (target instanceof WidgetItem) {
            WidgetItem iw = (WidgetItem)target;
            this.setSelectedWidget(iw.getWidget(), iw.getIndex(), true);
        } else {
            this.setSelectedWidget((Widget)target, -1, true);
        }
    }

    @Subscribe
    private void onMenuEntryAdded(MenuEntryAdded event) {
        if (!this.pickerSelected) {
            return;
        }
        MenuEntry[] menuEntries = this.client.getMenuEntries();
        for (int i = 0; i < menuEntries.length; ++i) {
            MenuEntry entry = menuEntries[i];
            if (entry.getOpcode() != MenuAction.WIDGET_USE_ON_ITEM.getId() && entry.getOpcode() != MenuAction.WIDGET_TARGET_ON_WIDGET.getId()) continue;
            String name = WidgetInfo.TO_GROUP((int)entry.getParam1()) + "." + WidgetInfo.TO_CHILD((int)entry.getParam1());
            if (entry.getParam0() != -1) {
                name = name + " [" + entry.getParam0() + "]";
            }
            Color color = this.colorForWidget(i, menuEntries.length);
            entry.setTarget(ColorUtil.wrapWithColorTag((String)name, (Color)color));
        }
        this.client.setMenuEntries(menuEntries);
    }

    Color colorForWidget(int index, int length) {
        float h = SELECTED_WIDGET_HUE + 0.1f + 0.8f / (float)length * (float)index;
        return Color.getHSBColor(h, 1.0f, 1.0f);
    }

    Object getWidgetOrWidgetItemForMenuOption(int type, int param0, int param1) {
        if (type == MenuAction.WIDGET_TARGET_ON_WIDGET.getId()) {
            Widget w = this.client.getWidget(WidgetInfo.TO_GROUP((int)param1), WidgetInfo.TO_CHILD((int)param1));
            if (param0 != -1) {
                w = w.getChild(param0);
            }
            return w;
        }
        if (type == MenuAction.ITEM_USE_ON_ITEM.getId()) {
            Widget w = this.client.getWidget(WidgetInfo.TO_GROUP((int)param1), WidgetInfo.TO_CHILD((int)param1));
            return w.getWidgetItem(param0);
        }
        return null;
    }

    Widget getSelectedWidget() {
        return this.selectedWidget;
    }

    int getSelectedItem() {
        return this.selectedItem;
    }

    boolean isPickerSelected() {
        return this.pickerSelected;
    }

    static {
        float[] hsb = new float[3];
        Color.RGBtoHSB(SELECTED_WIDGET_COLOR.getRed(), SELECTED_WIDGET_COLOR.getGreen(), SELECTED_WIDGET_COLOR.getBlue(), hsb);
        SELECTED_WIDGET_HUE = hsb[0];
    }
}


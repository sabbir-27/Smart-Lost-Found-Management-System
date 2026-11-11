package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.Item;
import com.smartlostfound.model.ItemStatus;
import com.smartlostfound.model.UserAccount;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User-centric panel displaying only the property reports and claims
 * submitted by the currently logged-in account.
 */
public class MyItemsPanel extends JPanel {

    private final LostFoundController controller;

    private JLabel myTotalKpiLabel;
    private JLabel myLostKpiLabel;
    private JLabel myFoundKpiLabel;
    private JLabel myRecoveredKpiLabel;

    private JTable itemTable;
    private ItemTableModel tableModel;
    private JLabel statusLabel;

    private JButton viewDetailsBtn;
    private JButton updateStatusBtn;
    private JButton deleteBtn;

    public MyItemsPanel(LostFoundController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 14));
        setBackground(UIUtils.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Top: Banner and KPIs
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        topContainer.add(createHeaderBanner());
        topContainer.add(Box.createVerticalStrut(14));
        topContainer.add(createKpiSection());

        add(topContainer, BorderLayout.NORTH);

        // 2. Center: My Items Table
        add(createTableSection(), BorderLayout.CENTER);

        // 3. Bottom: Actions
        add(createBottomActionSection(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderBanner() {
        JPanel banner = UIUtils.createCardPanel(10);
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(new EmptyBorder(12, 18, 12, 18));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);

        JLabel title = new JLabel("📂 My Reported Property & Claims");
        title.setFont(UIUtils.FONT_HEADER);
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel sub = new JLabel("Track the real-time custody status, match alerts, and resolutions for your reported items.");
        sub.setFont(UIUtils.FONT_SUBTITLE);
        sub.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        left.add(title);
        left.add(sub);
        banner.add(left, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton newLostBtn = UIUtils.createModernButton("+ Report Lost", UIUtils.COLOR_PRIMARY_LIGHT,
                UIUtils.COLOR_PRIMARY_BORDER, UIUtils.COLOR_PRIMARY, 8);
        newLostBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof MainFrame) {
                ((MainFrame) w).switchToReportLost();
            }
        });

        JButton newFoundBtn = UIUtils.createDarkButton("+ Report Found");
        newFoundBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof MainFrame) {
                ((MainFrame) w).switchToReportFound();
            }
        });

        right.add(newLostBtn);
        right.add(newFoundBtn);
        banner.add(right, BorderLayout.EAST);

        return banner;
    }

    private JPanel createKpiSection() {
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiPanel.setOpaque(false);

        myTotalKpiLabel = new JLabel("0");
        myLostKpiLabel = new JLabel("0");
        myFoundKpiLabel = new JLabel("0");
        myRecoveredKpiLabel = new JLabel("0");

        kpiPanel.add(UIUtils.createKpiCard("■", "My Submissions", myTotalKpiLabel, UIUtils.COLOR_TEXT_PRIMARY, "Total items reported by you"));
        kpiPanel.add(UIUtils.createKpiCard("▲", "My Lost Items", myLostKpiLabel, UIUtils.COLOR_PRIMARY, "Currently awaiting match"));
        kpiPanel.add(UIUtils.createKpiCard("▼", "Found Property", myFoundKpiLabel, UIUtils.COLOR_BLUE, "Turned in / logged by you"));
        kpiPanel.add(UIUtils.createKpiCard("●", "Recovered", myRecoveredKpiLabel, UIUtils.COLOR_SUCCESS, "Successfully returned / claimed"));

        return kpiPanel;
    }

    private JPanel createTableSection() {
        JPanel tableCard = UIUtils.createCardPanel(12);
        tableCard.setLayout(new BorderLayout());

        tableModel = new ItemTableModel();
        itemTable = new JTable(tableModel);
        itemTable.setRowHeight(42);
        itemTable.setFont(UIUtils.FONT_BODY);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setGridColor(UIUtils.COLOR_BORDER);
        itemTable.setShowGrid(true);
        itemTable.setIntercellSpacing(new Dimension(1, 1));
        itemTable.setSelectionBackground(UIUtils.COLOR_PRIMARY_LIGHT);
        itemTable.setSelectionForeground(UIUtils.COLOR_TEXT_PRIMARY);

        // Header
        JTableHeader header = itemTable.getTableHeader();
        header.setFont(UIUtils.FONT_BODY_BOLD);
        header.setBackground(Color.WHITE);
        header.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIUtils.COLOR_BORDER));

        // Renderer
        ItemStatusCellRenderer renderer = new ItemStatusCellRenderer();
        for (int i = 0; i < itemTable.getColumnCount(); i++) {
            itemTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Selection listener
        itemTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = itemTable.getSelectedRow() != -1;
            viewDetailsBtn.setEnabled(hasSelection);
            updateStatusBtn.setEnabled(hasSelection);
            deleteBtn.setEnabled(hasSelection);
        });

        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setBorder(new LineBorder(UIUtils.COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        return tableCard;
    }

    private JPanel createBottomActionSection() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 4, 0, 4));

        statusLabel = new JLabel("Showing 0 items reported by you");
        statusLabel.setFont(UIUtils.FONT_BODY_BOLD);
        statusLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        footer.add(statusLabel, BorderLayout.WEST);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroup.setOpaque(false);

        viewDetailsBtn = UIUtils.createSecondaryButton("View Full Details");
        viewDetailsBtn.setEnabled(false);
        viewDetailsBtn.addActionListener(e -> onViewDetails());

        updateStatusBtn = UIUtils.createPrimaryButton("Update Status / Resolve");
        updateStatusBtn.setEnabled(false);
        updateStatusBtn.addActionListener(e -> onUpdateStatus());

        deleteBtn = UIUtils.createDangerButton("Withdraw / Delete Report");
        deleteBtn.setEnabled(false);
        deleteBtn.addActionListener(e -> onDeleteItem());

        JButton refreshBtn = UIUtils.createDarkButton("Refresh List");
        refreshBtn.addActionListener(e -> refreshData());

        buttonGroup.add(viewDetailsBtn);
        buttonGroup.add(updateStatusBtn);
        buttonGroup.add(deleteBtn);
        buttonGroup.add(refreshBtn);

        footer.add(buttonGroup, BorderLayout.EAST);
        return footer;
    }

    public void refreshData() {
        UserAccount current = controller.getCurrentUser();
        List<Item> all = controller.getManager().getAllItems();
        List<Item> myItems = new ArrayList<>();

        if (current != null) {
            String myName = current.getName().toLowerCase();
            String myEmail = current.getEmail().toLowerCase();
            String myPhone = current.getPhone().toLowerCase();
            String myUser = current.getUsername().toLowerCase();

            for (Item item : all) {
                if (item.getReporter() != null) {
                    String repName = item.getReporter().getName().toLowerCase();
                    String repEmail = item.getReporter().getEmail().toLowerCase();
                    String repPhone = item.getReporter().getPhone().toLowerCase();

                    boolean matches = (!myName.isEmpty() && repName.contains(myName)) ||
                            (!myEmail.isEmpty() && repEmail.equalsIgnoreCase(myEmail)) ||
                            (!myPhone.isEmpty() && repPhone.equalsIgnoreCase(myPhone)) ||
                            repName.contains(myUser);

                    if (matches) {
                        myItems.add(item);
                    }
                }
            }
        }

        tableModel.setItems(myItems);

        long lostCount = myItems.stream().filter(i -> i.getStatus() == ItemStatus.LOST).count();
        long foundCount = myItems.stream().filter(i -> i.getStatus() == ItemStatus.FOUND).count();
        long resolvedCount = myItems.stream().filter(i -> i.getStatus() == ItemStatus.RECOVERED || i.getStatus() == ItemStatus.CLAIMED).count();

        myTotalKpiLabel.setText(String.valueOf(myItems.size()));
        myLostKpiLabel.setText(String.valueOf(lostCount));
        myFoundKpiLabel.setText(String.valueOf(foundCount));
        myRecoveredKpiLabel.setText(String.valueOf(resolvedCount));

        statusLabel.setText("Showing " + myItems.size() + " items reported by @" +
                (current != null ? current.getUsername() : "anonymous"));

        viewDetailsBtn.setEnabled(false);
        updateStatusBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
    }

    public Item getSelectedItem() {
        int row = itemTable.getSelectedRow();
        if (row != -1 && row < tableModel.getRowCount()) {
            return tableModel.getItemAt(row);
        }
        return null;
    }

    private void onViewDetails() {
        Item item = getSelectedItem();
        if (item != null) {
            controller.openItemDetailDialog(item, SwingUtilities.getWindowAncestor(this));
        }
    }

    private void onUpdateStatus() {
        Item item = getSelectedItem();
        if (item != null) {
            controller.openUpdateStatusDialog(item, SwingUtilities.getWindowAncestor(this));
        }
    }

    private void onDeleteItem() {
        Item item = getSelectedItem();
        if (item != null) {
            controller.handleDeleteItem(item.getId(), SwingUtilities.getWindowAncestor(this));
            refreshData();
        }
    }
}

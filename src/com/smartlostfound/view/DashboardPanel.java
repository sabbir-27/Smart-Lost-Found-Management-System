package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.Item;
import com.smartlostfound.model.ItemCategory;
import com.smartlostfound.model.ItemStatus;
import com.smartlostfound.service.LostFoundManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Centralized dashboard view housing the search engine, filtering controls,
 * KPI analytical metric cards, and the interactive items {@link JTable}.
 */
public class DashboardPanel extends JPanel {

    private final LostFoundController controller;

    // KPI labels
    private JLabel totalKpiLabel;
    private JLabel lostKpiLabel;
    private JLabel foundKpiLabel;
    private JLabel resolvedKpiLabel;

    // Search and filter controls
    private JTextField searchField;
    private JComboBox<String> typeFilterCombo;
    private JComboBox<Object> categoryFilterCombo;
    private JComboBox<Object> statusFilterCombo;

    // Table and model
    private JTable itemTable;
    private ItemTableModel tableModel;
    private TableRowSorter<ItemTableModel> rowSorter;
    private JLabel countStatusLabel;

    // Action buttons
    private JButton viewDetailsBtn;
    private JButton updateStatusBtn;
    private JButton deleteBtn;

    public DashboardPanel(LostFoundController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UIUtils.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(12, 16, 12, 16));

        // Top Section: KPI Metric Cards
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        topContainer.add(createKpiSection());
        topContainer.add(Box.createVerticalStrut(12));
        topContainer.add(createSearchFilterSection());

        add(topContainer, BorderLayout.NORTH);

        // Center Section: Table
        add(createTableSection(), BorderLayout.CENTER);

        // Bottom Section: Actions & Count Status
        add(createBottomActionSection(), BorderLayout.SOUTH);
    }

    private JPanel createKpiSection() {
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiPanel.setOpaque(false);

        totalKpiLabel = new JLabel("0");
        lostKpiLabel = new JLabel("0");
        foundKpiLabel = new JLabel("0");
        resolvedKpiLabel = new JLabel("0");

        kpiPanel.add(UIUtils.createKpiCard("Total Records", totalKpiLabel, UIUtils.COLOR_PRIMARY, "Logged items in system"));
        kpiPanel.add(UIUtils.createKpiCard("Active Lost Items", lostKpiLabel, UIUtils.COLOR_DANGER, "Awaiting recovery"));
        kpiPanel.add(UIUtils.createKpiCard("Active Found Items", foundKpiLabel, new Color(13, 148, 136), "Secured in custody"));
        kpiPanel.add(UIUtils.createKpiCard("Resolved Items", resolvedKpiLabel, UIUtils.COLOR_SUCCESS, "Claimed or recovered"));

        return kpiPanel;
    }

    private JPanel createSearchFilterSection() {
        JPanel filterCard = UIUtils.createCardPanel();
        filterCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Search Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        filterCard.add(new JLabel("Keyword:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.35;
        searchField = UIUtils.createStyledTextField(18);
        searchField.setToolTipText("Search by name, ID, description, location, or reporter");
        // Real-time search listener
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { triggerSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { triggerSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { triggerSearch(); }
        });
        filterCard.add(searchField, gbc);

        // Type Filter
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        filterCard.add(new JLabel("Type:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.15;
        typeFilterCombo = new JComboBox<>(new String[]{"ALL", "LOST", "FOUND"});
        typeFilterCombo.setFont(UIUtils.FONT_BODY);
        typeFilterCombo.addActionListener(e -> triggerSearch());
        filterCard.add(typeFilterCombo, gbc);

        // Category Filter
        gbc.gridx = 4;
        gbc.weightx = 0.0;
        filterCard.add(new JLabel("Category:"), gbc);

        gbc.gridx = 5;
        gbc.weightx = 0.25;
        DefaultComboBoxModel<Object> categoryModel = new DefaultComboBoxModel<>();
        categoryModel.addElement("All Categories");
        for (ItemCategory cat : ItemCategory.values()) {
            categoryModel.addElement(cat);
        }
        categoryFilterCombo = new JComboBox<>(categoryModel);
        categoryFilterCombo.setFont(UIUtils.FONT_BODY);
        categoryFilterCombo.addActionListener(e -> triggerSearch());
        filterCard.add(categoryFilterCombo, gbc);

        // Status Filter
        gbc.gridx = 6;
        gbc.weightx = 0.0;
        filterCard.add(new JLabel("Status:"), gbc);

        gbc.gridx = 7;
        gbc.weightx = 0.15;
        DefaultComboBoxModel<Object> statusModel = new DefaultComboBoxModel<>();
        statusModel.addElement("All Statuses");
        for (ItemStatus stat : ItemStatus.values()) {
            statusModel.addElement(stat);
        }
        statusFilterCombo = new JComboBox<>(statusModel);
        statusFilterCombo.setFont(UIUtils.FONT_BODY);
        statusFilterCombo.addActionListener(e -> triggerSearch());
        filterCard.add(statusFilterCombo, gbc);

        // Reset Button
        gbc.gridx = 8;
        gbc.weightx = 0.0;
        JButton resetBtn = UIUtils.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> resetFilters());
        filterCard.add(resetBtn, gbc);

        return filterCard;
    }

    private JPanel createTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIUtils.COLOR_SURFACE);
        panel.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));

        tableModel = new ItemTableModel();
        itemTable = new JTable(tableModel);
        itemTable.setFont(UIUtils.FONT_BODY);
        itemTable.setRowHeight(34);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setGridColor(UIUtils.COLOR_BORDER);
        itemTable.setShowGrid(true);
        itemTable.setIntercellSpacing(new Dimension(1, 1));
        itemTable.setSelectionBackground(UIUtils.COLOR_TABLE_SELECTION);
        itemTable.setSelectionForeground(UIUtils.COLOR_TEXT_PRIMARY);

        // Custom status badge renderer
        itemTable.getColumnModel().getColumn(7).setCellRenderer(new ItemStatusCellRenderer());

        // Column widths
        itemTable.getColumnModel().getColumn(0).setPreferredWidth(95);  // ID
        itemTable.getColumnModel().getColumn(1).setPreferredWidth(65);  // Type
        itemTable.getColumnModel().getColumn(2).setPreferredWidth(170); // Name
        itemTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Category
        itemTable.getColumnModel().getColumn(4).setPreferredWidth(90);  // Date
        itemTable.getColumnModel().getColumn(5).setPreferredWidth(170); // Location
        itemTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Specifics
        itemTable.getColumnModel().getColumn(7).setPreferredWidth(105); // Status
        itemTable.getColumnModel().getColumn(8).setPreferredWidth(140); // Reporter

        // Table Header Styling
        JTableHeader header = itemTable.getTableHeader();
        header.setFont(UIUtils.FONT_BODY_BOLD);
        header.setBackground(UIUtils.COLOR_TABLE_HEADER);
        header.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);

        // Selection Listener for button states
        itemTable.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

        // Double click to view details
        itemTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && itemTable.getSelectedRow() != -1) {
                    onViewDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomActionSection() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 0, 0, 0));

        countStatusLabel = new JLabel("Showing 0 records");
        countStatusLabel.setFont(UIUtils.FONT_BODY_BOLD);
        countStatusLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);
        footer.add(countStatusLabel, BorderLayout.WEST);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroup.setOpaque(false);

        viewDetailsBtn = UIUtils.createSecondaryButton("View Details");
        viewDetailsBtn.setEnabled(false);
        viewDetailsBtn.addActionListener(e -> onViewDetails());

        updateStatusBtn = UIUtils.createPrimaryButton("Update Status");
        updateStatusBtn.setEnabled(false);
        updateStatusBtn.addActionListener(e -> onUpdateStatus());

        deleteBtn = UIUtils.createDangerButton("Delete Record");
        deleteBtn.setEnabled(false);
        deleteBtn.addActionListener(e -> onDeleteItem());

        JButton reloadBtn = UIUtils.createSecondaryButton("Reload Data");
        reloadBtn.addActionListener(e -> controller.handleReloadData());

        buttonGroup.add(viewDetailsBtn);
        buttonGroup.add(updateStatusBtn);
        buttonGroup.add(deleteBtn);
        buttonGroup.add(reloadBtn);

        footer.add(buttonGroup, BorderLayout.EAST);
        return footer;
    }

    private void updateButtonStates() {
        boolean hasSelection = itemTable.getSelectedRow() != -1;
        viewDetailsBtn.setEnabled(hasSelection);
        updateStatusBtn.setEnabled(hasSelection);
        deleteBtn.setEnabled(hasSelection);
    }

    public Item getSelectedItem() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow != -1) {
            return tableModel.getItemAt(selectedRow);
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
        }
    }

    private void triggerSearch() {
        String keyword = searchField.getText().trim();
        String type = (String) typeFilterCombo.getSelectedItem();

        Object catObj = categoryFilterCombo.getSelectedItem();
        ItemCategory category = (catObj instanceof ItemCategory) ? (ItemCategory) catObj : null;

        Object statObj = statusFilterCombo.getSelectedItem();
        ItemStatus status = (statObj instanceof ItemStatus) ? (ItemStatus) statObj : null;

        controller.handleFilterItems(keyword, category, status, type);
    }

    public void resetFilters() {
        searchField.setText("");
        typeFilterCombo.setSelectedIndex(0);
        categoryFilterCombo.setSelectedIndex(0);
        statusFilterCombo.setSelectedIndex(0);
        triggerSearch();
    }

    /**
     * Updates the table with newly filtered items and refreshes counter text.
     *
     * @param items Filtered list of items
     * @param totalTotalCount Total records across entire database
     */
    public void displayItems(List<Item> items, int totalTotalCount) {
        tableModel.setItems(items);
        countStatusLabel.setText(String.format("Showing %d of %d records", items.size(), totalTotalCount));
        updateButtonStates();
    }

    /**
     * Updates KPI analytical summary cards.
     *
     * @param stats Aggregate statistics
     */
    public void updateStatistics(LostFoundManager.SystemStats stats) {
        totalKpiLabel.setText(String.valueOf(stats.getTotalCount()));
        lostKpiLabel.setText(String.valueOf(stats.getLostCount()));
        foundKpiLabel.setText(String.valueOf(stats.getFoundCount()));
        resolvedKpiLabel.setText(String.valueOf(stats.getResolvedCount()));
    }
}

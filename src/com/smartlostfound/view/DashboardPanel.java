package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.Item;
import com.smartlostfound.model.ItemCategory;
import com.smartlostfound.model.ItemStatus;
import com.smartlostfound.service.LostFoundManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Enterprise dashboard view styled with the DesignRush Best Designs concept:
 * High-contrast KPI metric cards, an elevated search and filter toolbar,
 * a clean data table with soft red selection states, and centralized record actions.
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
    private JLabel countStatusLabel;
    private int hoveredRow = -1;

    // Action buttons
    private JButton viewDetailsBtn;
    private JButton updateStatusBtn;
    private JButton deleteBtn;

    public DashboardPanel(LostFoundController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 14));
        setBackground(UIUtils.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Header: KPIs and Search Controls
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        topContainer.add(createKpiSection());
        topContainer.add(Box.createVerticalStrut(14));
        topContainer.add(createSearchFilterSection());

        add(topContainer, BorderLayout.NORTH);

        // Center Table Section
        add(createTableSection(), BorderLayout.CENTER);

        // Bottom Action Bar
        add(createBottomActionSection(), BorderLayout.SOUTH);
    }

    private JPanel createKpiSection() {
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiPanel.setOpaque(false);

        totalKpiLabel = new JLabel("0");
        lostKpiLabel = new JLabel("0");
        foundKpiLabel = new JLabel("0");
        resolvedKpiLabel = new JLabel("0");

        kpiPanel.add(UIUtils.createKpiCard("■", "Total Inventory", totalKpiLabel, UIUtils.COLOR_TEXT_PRIMARY, "All registered property"));
        kpiPanel.add(UIUtils.createKpiCard("▲", "Active Lost", lostKpiLabel, UIUtils.COLOR_PRIMARY, "Awaiting discovery & match"));
        kpiPanel.add(UIUtils.createKpiCard("▼", "In Custody", foundKpiLabel, UIUtils.COLOR_BLUE, "Secured on premises"));
        kpiPanel.add(UIUtils.createKpiCard("●", "Resolved", resolvedKpiLabel, UIUtils.COLOR_SUCCESS, "Recovered & claimed items"));

        // Responsive KPI card wrapping for all display resolutions
        kpiPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = kpiPanel.getWidth();
                GridLayout layout = (GridLayout) kpiPanel.getLayout();
                if (w < 820 && layout.getRows() != 2) {
                    kpiPanel.setLayout(new GridLayout(2, 2, 10, 10));
                    kpiPanel.revalidate();
                } else if (w >= 820 && layout.getRows() != 1) {
                    kpiPanel.setLayout(new GridLayout(1, 4, 14, 0));
                    kpiPanel.revalidate();
                }
            }
        });

        return kpiPanel;
    }

    private JPanel createSearchFilterSection() {
        JPanel filterCard = UIUtils.createCardPanel(10);
        filterCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Full-Width Search Input & Reset Button
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(UIUtils.FONT_BODY_BOLD);
        searchLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        filterCard.add(searchLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.gridwidth = 4;
        searchField = UIUtils.createStyledTextField(18);
        searchField.setToolTipText("Filter by item name, ID, location, description, or reporter");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { triggerSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { triggerSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { triggerSearch(); }
        });
        filterCard.add(searchField, gbc);

        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        JButton resetBtn = UIUtils.createSecondaryButton("Reset Filters");
        resetBtn.addActionListener(e -> resetFilters());
        filterCard.add(resetBtn, gbc);

        // Row 1: Dropdown filters (Spaced evenly and clearly visible on all devices)
        gbc.gridy = 1;
        gbc.gridwidth = 1;

        // Type Filter
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel typeLabel = new JLabel("Type:");
        typeLabel.setFont(UIUtils.FONT_BODY_BOLD);
        typeLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        filterCard.add(typeLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.25;
        typeFilterCombo = new JComboBox<>(new String[]{"ALL", "LOST", "FOUND"});
        typeFilterCombo.setFont(UIUtils.FONT_BODY);
        typeFilterCombo.setBackground(Color.WHITE);
        typeFilterCombo.addActionListener(e -> triggerSearch());
        filterCard.add(typeFilterCombo, gbc);

        // Category Filter
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JLabel catLabel = new JLabel("Category:");
        catLabel.setFont(UIUtils.FONT_BODY_BOLD);
        catLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        filterCard.add(catLabel, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.45;
        DefaultComboBoxModel<Object> categoryModel = new DefaultComboBoxModel<>();
        categoryModel.addElement("All Categories");
        for (ItemCategory cat : ItemCategory.values()) {
            categoryModel.addElement(cat);
        }
        categoryFilterCombo = new JComboBox<>(categoryModel);
        categoryFilterCombo.setFont(UIUtils.FONT_BODY);
        categoryFilterCombo.setBackground(Color.WHITE);
        categoryFilterCombo.addActionListener(e -> triggerSearch());
        filterCard.add(categoryFilterCombo, gbc);

        // Status Filter
        gbc.gridx = 4;
        gbc.weightx = 0.0;
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(UIUtils.FONT_BODY_BOLD);
        statusLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        filterCard.add(statusLabel, gbc);

        gbc.gridx = 5;
        gbc.weightx = 0.30;
        DefaultComboBoxModel<Object> statusModel = new DefaultComboBoxModel<>();
        statusModel.addElement("All Statuses");
        for (ItemStatus stat : ItemStatus.values()) {
            statusModel.addElement(stat);
        }
        statusFilterCombo = new JComboBox<>(statusModel);
        statusFilterCombo.setFont(UIUtils.FONT_BODY);
        statusFilterCombo.setBackground(Color.WHITE);
        statusFilterCombo.addActionListener(e -> triggerSearch());
        filterCard.add(statusFilterCombo, gbc);

        return filterCard;
    }

    private JPanel createTableSection() {
        JPanel tableContainer = UIUtils.createCardPanel(10);
        tableContainer.setLayout(new BorderLayout());
        tableContainer.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.COLOR_BORDER, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));

        tableModel = new ItemTableModel();
        itemTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    if (row == hoveredRow) {
                        c.setBackground(UIUtils.COLOR_TABLE_HOVER);
                    } else {
                        c.setBackground((row % 2 == 0) ? Color.WHITE : UIUtils.COLOR_TABLE_ROW_ALT);
                    }
                } else {
                    c.setBackground(UIUtils.COLOR_TABLE_SELECTION);
                }
                return c;
            }
        };

        itemTable.setFont(UIUtils.FONT_BODY);
        itemTable.setRowHeight(44);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setShowGrid(false);
        itemTable.setIntercellSpacing(new Dimension(0, 1));
        itemTable.setSelectionBackground(UIUtils.COLOR_TABLE_SELECTION);
        itemTable.setSelectionForeground(UIUtils.COLOR_TEXT_PRIMARY);

        // Column 0: Monospace ID with red tint styling
        itemTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setFont(UIUtils.FONT_MONO);
                l.setForeground(UIUtils.COLOR_PRIMARY);
                l.setBorder(new EmptyBorder(0, 12, 0, 0));
                return l;
            }
        });

        // Column 1: Type Pill Renderer (DesignRush Red for Lost, Charcoal for Found)
        itemTable.getColumnModel().getColumn(1).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                String typeStr = value != null ? value.toString() : "";
                boolean isLost = "Lost".equalsIgnoreCase(typeStr);

                JPanel p = new JPanel(new GridBagLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(isLost ? new Color(254, 242, 242) : new Color(244, 244, 245));
                        g2.fill(new RoundRectangle2D.Float(8, 8, getWidth() - 16, getHeight() - 16, 10, 10));
                        g2.setColor(isLost ? new Color(254, 202, 202) : new Color(228, 228, 231));
                        g2.draw(new RoundRectangle2D.Float(8.5f, 8.5f, getWidth() - 17, getHeight() - 17, 10, 10));
                        g2.dispose();
                    }
                };
                p.setOpaque(false);

                JLabel l = new JLabel(isLost ? "LOST" : "FOUND");
                l.setFont(UIUtils.FONT_SMALL_BOLD);
                l.setForeground(isLost ? UIUtils.COLOR_PRIMARY : UIUtils.COLOR_CHARCOAL_BTN);
                p.add(l);
                return p;
            }
        });

        // Column 2: Item Name (Bold Primary)
        itemTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setFont(UIUtils.FONT_BODY_BOLD);
                l.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
                l.setBorder(new EmptyBorder(0, 8, 0, 8));
                return l;
            }
        });

        // Column 7: Status Pill Badge Renderer
        itemTable.getColumnModel().getColumn(7).setCellRenderer(new ItemStatusCellRenderer());

        // Preferred Column Widths
        itemTable.getColumnModel().getColumn(0).setPreferredWidth(100); // ID
        itemTable.getColumnModel().getColumn(1).setPreferredWidth(75);  // Type
        itemTable.getColumnModel().getColumn(2).setPreferredWidth(180); // Name
        itemTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Category
        itemTable.getColumnModel().getColumn(4).setPreferredWidth(95);  // Date
        itemTable.getColumnModel().getColumn(5).setPreferredWidth(180); // Location
        itemTable.getColumnModel().getColumn(6).setPreferredWidth(130); // Specifics
        itemTable.getColumnModel().getColumn(7).setPreferredWidth(110); // Status
        itemTable.getColumnModel().getColumn(8).setPreferredWidth(140); // Reporter

        // Table Header Styling
        JTableHeader header = itemTable.getTableHeader();
        header.setFont(UIUtils.FONT_BODY_BOLD);
        header.setBackground(UIUtils.COLOR_TABLE_HEADER);
        header.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.COLOR_BORDER));
        header.setReorderingAllowed(false);

        // Row hover detection
        itemTable.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = itemTable.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    itemTable.repaint();
                }
            }
        });

        itemTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                itemTable.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && itemTable.getSelectedRow() != -1) {
                    onViewDetails();
                }
            }
        });

        itemTable.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        return tableContainer;
    }

    private JPanel createBottomActionSection() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 4, 0, 4));

        countStatusLabel = new JLabel("Showing 0 records");
        countStatusLabel.setFont(UIUtils.FONT_BODY_BOLD);
        countStatusLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        footer.add(countStatusLabel, BorderLayout.WEST);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroup.setOpaque(false);

        viewDetailsBtn = UIUtils.createSecondaryButton("View Full Details");
        viewDetailsBtn.setEnabled(false);
        viewDetailsBtn.addActionListener(e -> onViewDetails());

        updateStatusBtn = UIUtils.createPrimaryButton("Update Status");
        updateStatusBtn.setEnabled(false);
        updateStatusBtn.addActionListener(e -> onUpdateStatus());

        deleteBtn = UIUtils.createDangerButton("Delete Record");
        deleteBtn.setEnabled(false);
        deleteBtn.addActionListener(e -> onDeleteItem());

        JButton refreshBtn = UIUtils.createSecondaryButton("Refresh Records");
        refreshBtn.addActionListener(e -> {
            controller.refreshView();
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof MainFrame) {
                ((MainFrame) w).setStatusMessage("Inventory records refreshed.");
            }
        });

        JButton reloadBtn = UIUtils.createDarkButton("Sync Storage");
        reloadBtn.addActionListener(e -> controller.handleReloadData());

        buttonGroup.add(viewDetailsBtn);
        buttonGroup.add(updateStatusBtn);
        buttonGroup.add(deleteBtn);
        buttonGroup.add(refreshBtn);
        buttonGroup.add(reloadBtn);

        footer.add(buttonGroup, BorderLayout.EAST);
        return footer;
    }

    private void updateButtonStates() {
        boolean hasSelection = getSelectedItem() != null;
        viewDetailsBtn.setEnabled(hasSelection);
        updateStatusBtn.setEnabled(hasSelection);
        deleteBtn.setEnabled(hasSelection);
    }

    public Item getSelectedItem() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow != -1 && selectedRow < itemTable.getRowCount()) {
            int modelRow = itemTable.convertRowIndexToModel(selectedRow);
            if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
                return tableModel.getItemAt(modelRow);
            }
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

    public void displayItems(List<Item> items, int totalTotalCount) {
        itemTable.clearSelection();
        tableModel.setItems(items);
        countStatusLabel.setText(String.format("Showing %d of %d active records", items.size(), totalTotalCount));
        updateButtonStates();
    }

    public void updateStatistics(LostFoundManager.SystemStats stats) {
        totalKpiLabel.setText(String.valueOf(stats.getTotalCount()));
        lostKpiLabel.setText(String.valueOf(stats.getLostCount()));
        foundKpiLabel.setText(String.valueOf(stats.getFoundCount()));
        resolvedKpiLabel.setText(String.valueOf(stats.getResolvedCount()));
    }
}

package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.UserAccount;
import com.smartlostfound.model.UserRole;
import com.smartlostfound.service.LostFoundManager;
import com.smartlostfound.service.UserManager;
import com.smartlostfound.storage.DataPersistenceException;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Executive Admin Console Panel.
 * Provides authorized administrators with complete user directory management,
 * role modification, account suspension, security auditing, and system diagnostics.
 */
public class AdminConsolePanel extends JPanel {

    private final LostFoundController controller;

    private JLabel totalUsersKpiLabel;
    private JLabel adminCountKpiLabel;
    private JLabel activeCountKpiLabel;
    private JLabel inventoryCountKpiLabel;

    private JTable userTable;
    private UserTableModel userTableModel;
    private JLabel userCountStatusLabel;

    private JButton toggleRoleBtn;
    private JButton toggleActiveBtn;
    private JButton deleteUserBtn;

    public AdminConsolePanel(LostFoundController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Top Section: Admin Header & KPIs
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        topContainer.add(createAdminBanner());
        topContainer.add(Box.createVerticalStrut(14));
        topContainer.add(createKpiSection());

        add(topContainer, BorderLayout.NORTH);

        // 2. Center: User Directory Management Table
        add(createUserDirectorySection(), BorderLayout.CENTER);

        // 3. Bottom: System Diagnostics & Maintenance Card
        add(createDiagnosticsSection(), BorderLayout.SOUTH);
    }

    private JPanel createAdminBanner() {
        JPanel banner = UIUtils.createCardPanel(10);
        banner.setLayout(new BorderLayout(16, 0));
        banner.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        banner.setBorder(new EmptyBorder(14, 20, 14, 20));

        // Left info
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 3));
        left.setOpaque(false);

        JLabel title = new JLabel("🛡️ EXECUTIVE ADMINISTRATIVE CONTROL CONSOLE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Privileged oversight: Manage credentials, modify roles, monitor audit logs, and trigger system sync.");
        sub.setFont(UIUtils.FONT_SMALL);
        sub.setForeground(UIUtils.COLOR_SIDEBAR_TEXT);

        left.add(title);
        left.add(sub);
        banner.add(left, BorderLayout.CENTER);

        // Right Badge
        JLabel badge = new JLabel("ADMIN PRIVILEGES ACTIVE");
        badge.setFont(UIUtils.FONT_SMALL_BOLD);
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(UIUtils.COLOR_PRIMARY);
        badge.setBorder(new EmptyBorder(6, 12, 6, 12));

        banner.add(badge, BorderLayout.EAST);

        return banner;
    }

    private JPanel createKpiSection() {
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiPanel.setOpaque(false);

        totalUsersKpiLabel = new JLabel("0");
        adminCountKpiLabel = new JLabel("0");
        activeCountKpiLabel = new JLabel("0");
        inventoryCountKpiLabel = new JLabel("0");

        kpiPanel.add(UIUtils.createKpiCard("👥", "Registered Users", totalUsersKpiLabel, UIUtils.COLOR_TEXT_PRIMARY, "Total system accounts"));
        kpiPanel.add(UIUtils.createKpiCard("🛡️", "Administrators", adminCountKpiLabel, UIUtils.COLOR_PRIMARY, "Full governance access"));
        kpiPanel.add(UIUtils.createKpiCard("⚡", "Active Accounts", activeCountKpiLabel, UIUtils.COLOR_SUCCESS, "Enabled authentication logins"));
        kpiPanel.add(UIUtils.createKpiCard("📦", "Managed Items", inventoryCountKpiLabel, UIUtils.COLOR_BLUE, "Total inventory records"));

        return kpiPanel;
    }

    private JPanel createUserDirectorySection() {
        JPanel card = UIUtils.createCardPanel(12);
        card.setLayout(new BorderLayout(0, 12));

        // Header
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        cardHeader.setBorder(new EmptyBorder(4, 4, 8, 4));

        JLabel title = new JLabel("User Directory & Access Control");
        title.setFont(UIUtils.FONT_HEADER);
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        cardHeader.add(title, BorderLayout.WEST);

        // Action Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);

        JButton refreshBtn = UIUtils.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshData());

        JButton addAccountBtn = UIUtils.createModernButton("+ Create Account", UIUtils.COLOR_PRIMARY_LIGHT,
                UIUtils.COLOR_PRIMARY_BORDER, UIUtils.COLOR_PRIMARY, 8);
        addAccountBtn.addActionListener(e -> openAddUserDialog());

        toggleRoleBtn = UIUtils.createSecondaryButton("👑 Switch Role (User/Admin)");
        toggleRoleBtn.setEnabled(false);
        toggleRoleBtn.addActionListener(e -> handleToggleRole());

        toggleActiveBtn = UIUtils.createSecondaryButton("🚫 Toggle Active / Suspend");
        toggleActiveBtn.setEnabled(false);
        toggleActiveBtn.addActionListener(e -> handleToggleActive());

        deleteUserBtn = UIUtils.createDangerButton("🗑️ Delete User");
        deleteUserBtn.setEnabled(false);
        deleteUserBtn.addActionListener(e -> handleDeleteUser());

        toolbar.add(refreshBtn);
        toolbar.add(addAccountBtn);
        toolbar.add(toggleRoleBtn);
        toolbar.add(toggleActiveBtn);
        toolbar.add(deleteUserBtn);

        cardHeader.add(toolbar, BorderLayout.EAST);
        card.add(cardHeader, BorderLayout.NORTH);

        // Table
        userTableModel = new UserTableModel();
        userTable = new JTable(userTableModel);
        userTable.setRowHeight(38);
        userTable.setFont(UIUtils.FONT_BODY);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setGridColor(UIUtils.COLOR_BORDER);
        userTable.setShowGrid(true);
        userTable.setIntercellSpacing(new Dimension(1, 1));
        userTable.setSelectionBackground(UIUtils.COLOR_PRIMARY_LIGHT);
        userTable.setSelectionForeground(UIUtils.COLOR_TEXT_PRIMARY);

        // Header Styling
        JTableHeader header = userTable.getTableHeader();
        header.setFont(UIUtils.FONT_BODY_BOLD);
        header.setBackground(Color.WHITE);
        header.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIUtils.COLOR_BORDER));

        // Renderers
        userTable.setDefaultRenderer(Object.class, new UserTableCellRenderer());

        userTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = userTable.getSelectedRow() != -1;
            toggleRoleBtn.setEnabled(hasSelection);
            toggleActiveBtn.setEnabled(hasSelection);
            deleteUserBtn.setEnabled(hasSelection);
        });

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(new LineBorder(UIUtils.COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        card.add(scrollPane, BorderLayout.CENTER);

        // Footer count
        userCountStatusLabel = new JLabel("Directory: 0 accounts loaded");
        userCountStatusLabel.setFont(UIUtils.FONT_SMALL);
        userCountStatusLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);
        userCountStatusLabel.setBorder(new EmptyBorder(6, 4, 2, 4));
        card.add(userCountStatusLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createDiagnosticsSection() {
        JPanel diagCard = UIUtils.createCardPanel(10);
        diagCard.setLayout(new BorderLayout(14, 0));
        diagCard.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);

        JLabel title = new JLabel("Storage Integrity & Telemetry");
        title.setFont(UIUtils.FONT_BODY_BOLD);
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        String pathInfo = "Item DB: " + controller.getStoragePathDescription() +
                "  |  User DB: " + controller.getUserManager().getStoragePath();
        JLabel pathLabel = new JLabel(pathInfo);
        pathLabel.setFont(UIUtils.FONT_SMALL);
        pathLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        left.add(title);
        left.add(pathLabel);
        diagCard.add(left, BorderLayout.CENTER);

        JButton forceSyncBtn = UIUtils.createDarkButton("💾 Force Save to Disk");
        forceSyncBtn.addActionListener(e -> {
            try {
                controller.getUserManager().persistCurrentState();
                controller.handleReloadData();
                JOptionPane.showMessageDialog(this,
                        "Storage successfully committed to disk.\n" +
                        "Items CSV & Users CSV are synchronized.",
                        "Storage Synchronized", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } catch (DataPersistenceException ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to persist storage:\n" + ex.getMessage(),
                        "Storage Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        diagCard.add(forceSyncBtn, BorderLayout.EAST);
        return diagCard;
    }

    public void refreshData() {
        UserManager um = controller.getUserManager();
        List<UserAccount> users = um.getAllUsers();
        userTableModel.setUsers(users);

        totalUsersKpiLabel.setText(String.valueOf(um.getTotalCount()));
        adminCountKpiLabel.setText(String.valueOf(um.getAdminCount()));
        activeCountKpiLabel.setText(String.valueOf(um.getActiveCount()));

        LostFoundManager.SystemStats stats = controller.getManager().getStatistics();
        inventoryCountKpiLabel.setText(String.valueOf(stats.getTotalCount()));

        userCountStatusLabel.setText("Directory: " + users.size() + " accounts registered in system");

        toggleRoleBtn.setEnabled(false);
        toggleActiveBtn.setEnabled(false);
        deleteUserBtn.setEnabled(false);
    }

    private UserAccount getSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row >= 0 && row < userTableModel.getRowCount()) {
            return userTableModel.getUserAt(row);
        }
        return null;
    }

    private void handleToggleRole() {
        UserAccount user = getSelectedUser();
        if (user == null) return;

        UserRole newRole = user.isAdmin() ? UserRole.USER : UserRole.ADMIN;
        int conf = JOptionPane.showConfirmDialog(this,
                "Change role for @" + user.getUsername() + " from " + user.getRole().getDisplayName() +
                " to " + newRole.getDisplayName() + "?",
                "Confirm Role Change", JOptionPane.YES_NO_OPTION);

        if (conf == JOptionPane.YES_OPTION) {
            try {
                controller.getUserManager().updateUserRole(user.getUsername(), newRole);
                refreshData();
                JOptionPane.showMessageDialog(this,
                        "User @" + user.getUsername() + " is now an " + newRole.getDisplayName() + ".",
                        "Role Updated", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleToggleActive() {
        UserAccount user = getSelectedUser();
        if (user == null) return;

        String action = user.isActive() ? "deactivate" : "activate";
        int conf = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + action + " account @" + user.getUsername() + "?",
                "Confirm Account Status Change", JOptionPane.YES_NO_OPTION);

        if (conf == JOptionPane.YES_OPTION) {
            try {
                controller.getUserManager().toggleUserStatus(user.getUsername());
                refreshData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteUser() {
        UserAccount user = getSelectedUser();
        if (user == null) return;

        UserAccount current = controller.getCurrentUser();
        if (current != null && current.getUsername().equalsIgnoreCase(user.getUsername())) {
            JOptionPane.showMessageDialog(this,
                    "You cannot delete your own logged-in account.",
                    "Action Forbidden", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int conf = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete account @" + user.getUsername() + " (" + user.getName() + ")?\n" +
                "This action cannot be undone.",
                "Confirm Account Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (conf == JOptionPane.YES_OPTION) {
            try {
                controller.getUserManager().deleteUser(user.getUsername(), current != null ? current.getUsername() : "");
                refreshData();
                JOptionPane.showMessageDialog(this,
                        "User @" + user.getUsername() + " has been removed.",
                        "Account Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openAddUserDialog() {
        JTextField nameField = UIUtils.createStyledTextField(15);
        JTextField usernameField = UIUtils.createStyledTextField(15);
        JTextField emailField = UIUtils.createStyledTextField(15);
        JTextField phoneField = UIUtils.createStyledTextField(15);
        JPasswordField passField = new JPasswordField(15);
        JComboBox<UserRole> roleCombo = new JComboBox<>(UserRole.values());

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Email Address:"));
        panel.add(emailField);
        panel.add(new JLabel("Phone Number:"));
        panel.add(phoneField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);
        panel.add(new JLabel("Account Role:"));
        panel.add(roleCombo);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Create New User / Admin Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String pass = new String(passField.getPassword());
            UserRole role = (UserRole) roleCombo.getSelectedItem();

            try {
                UserAccount created = controller.getUserManager().registerUser(
                        username, pass, name, email, phone, role);
                refreshData();
                JOptionPane.showMessageDialog(this,
                        "Created account @" + created.getUsername() + " successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Creation Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Custom Table Model for User Accounts.
     */
    private static class UserTableModel extends AbstractTableModel {
        private final String[] columns = {"Username", "Role", "Full Name", "Email", "Phone", "Status", "Created"};
        private final List<UserAccount> users = new ArrayList<>();

        public void setUsers(List<UserAccount> newUsers) {
            users.clear();
            if (newUsers != null) {
                users.addAll(newUsers);
            }
            fireTableDataChanged();
        }

        public UserAccount getUserAt(int row) {
            if (row >= 0 && row < users.size()) {
                return users.get(row);
            }
            return null;
        }

        @Override
        public int getRowCount() {
            return users.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            UserAccount u = users.get(rowIndex);
            switch (columnIndex) {
                case 0: return "@" + u.getUsername();
                case 1: return u.getRole();
                case 2: return u.getName();
                case 3: return u.getEmail().isEmpty() ? "—" : u.getEmail();
                case 4: return u.getPhone().isEmpty() ? "—" : u.getPhone();
                case 5: return u.isActive() ? "ACTIVE" : "SUSPENDED";
                case 6: return u.getCreatedAt() != null ?
                        u.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "—";
                default: return "";
            }
        }
    }

    /**
     * Custom Cell Renderer for User Table with styled Badges.
     */
    private static class UserTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setBorder(new EmptyBorder(0, 10, 0, 10));
            setFont(UIUtils.FONT_BODY);

            if (column == 0) {
                setFont(UIUtils.FONT_BODY_BOLD);
                setForeground(UIUtils.COLOR_TEXT_PRIMARY);
            } else if (column == 1) {
                // Role Badge
                if (value instanceof UserRole) {
                    UserRole role = (UserRole) value;
                    if (role == UserRole.ADMIN) {
                        setText("  🛡️ ADMIN  ");
                        setForeground(UIUtils.COLOR_PRIMARY);
                        setFont(UIUtils.FONT_SMALL_BOLD);
                    } else {
                        setText("  👤 USER  ");
                        setForeground(UIUtils.COLOR_TEXT_SECONDARY);
                        setFont(UIUtils.FONT_SMALL_BOLD);
                    }
                }
            } else if (column == 5) {
                // Status Badge
                String s = String.valueOf(value);
                if ("ACTIVE".equalsIgnoreCase(s)) {
                    setForeground(UIUtils.COLOR_SUCCESS);
                    setFont(UIUtils.FONT_SMALL_BOLD);
                } else {
                    setForeground(UIUtils.COLOR_PRIMARY);
                    setFont(UIUtils.FONT_SMALL_BOLD);
                }
            } else {
                setForeground(isSelected ? UIUtils.COLOR_TEXT_PRIMARY : UIUtils.COLOR_TEXT_SECONDARY);
            }

            return c;
        }
    }
}

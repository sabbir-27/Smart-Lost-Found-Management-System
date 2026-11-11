package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Enterprise application frame styled with DesignRush Best Designs aesthetics:
 * Carbon-black sidebar, vibrant signature red accents, editorial typography,
 * and card-based modular layout.
 */
public class MainFrame extends JFrame {

    private final LostFoundController controller;

    private CardLayout contentCardLayout;
    private JPanel mainContentCardPanel;

    private DashboardPanel dashboardPanel;
    private ReportLostItemPanel reportLostPanel;
    private ReportFoundItemPanel reportFoundPanel;

    private JLabel breadcrumbTitleLabel;
    private JLabel breadcrumbSubtitleLabel;
    private JLabel statusMessageLabel;

    private final Map<String, SidebarNavButton> navButtons = new HashMap<>();
    private String currentNavKey = "DASHBOARD";

    public MainFrame(LostFoundController controller) {
        super("Smart Lost & Found Management System - DesignRush Edition");
        this.controller = controller;

        initComponents();
        setupWindow();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.COLOR_BACKGROUND);

        // 1. Left Sidebar Navigation (Carbon Black)
        add(createSidebarPanel(), BorderLayout.WEST);

        // 2. Right Workspace Container
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(UIUtils.COLOR_BACKGROUND);

        // Top Editorial Breadcrumb & Action Bar
        workspace.add(createTopBar(), BorderLayout.NORTH);

        // Center Views (CardLayout)
        contentCardLayout = new CardLayout();
        mainContentCardPanel = new JPanel(contentCardLayout);
        mainContentCardPanel.setBackground(UIUtils.COLOR_BACKGROUND);

        dashboardPanel = new DashboardPanel(controller);
        reportLostPanel = new ReportLostItemPanel(controller);
        reportFoundPanel = new ReportFoundItemPanel(controller);

        mainContentCardPanel.add(dashboardPanel, "DASHBOARD");
        mainContentCardPanel.add(reportLostPanel, "REPORT_LOST");
        mainContentCardPanel.add(reportFoundPanel, "REPORT_FOUND");

        workspace.add(mainContentCardPanel, BorderLayout.CENTER);

        // Bottom Telemetry Status Bar
        workspace.add(createStatusBar(), BorderLayout.SOUTH);

        add(workspace, BorderLayout.CENTER);

        // Default navigation
        setActiveNav("DASHBOARD");
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        sidebar.setBorder(new EmptyBorder(22, 16, 22, 16));

        // Brand Header (DesignRush style)
        JPanel brandPanel = new JPanel(new BorderLayout(10, 6));
        brandPanel.setOpaque(false);

        // Red Accent Icon
        JLabel logoIcon = new JLabel("■");
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoIcon.setForeground(UIUtils.COLOR_PRIMARY);

        JPanel brandTextPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        brandTextPanel.setOpaque(false);

        JLabel brandTitle = new JLabel("LOST & FOUND");
        brandTitle.setFont(UIUtils.FONT_LOGO);
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSubtitle = new JLabel("DESIGNRUSH RECOVERY HUB");
        brandSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 9));
        brandSubtitle.setForeground(UIUtils.COLOR_PRIMARY);

        brandTextPanel.add(brandTitle);
        brandTextPanel.add(brandSubtitle);

        brandPanel.add(logoIcon, BorderLayout.WEST);
        brandPanel.add(brandTextPanel, BorderLayout.CENTER);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Navigation Items
        JPanel navContainer = new JPanel();
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setOpaque(false);
        navContainer.setBorder(new EmptyBorder(32, 0, 0, 0));

        SidebarNavButton dashBtn = new SidebarNavButton("●  Dashboard & Records", () -> switchToDashboard());
        SidebarNavButton lostBtn = new SidebarNavButton("▲  Report Lost Property", () -> switchToReportLost());
        SidebarNavButton foundBtn = new SidebarNavButton("▼  Report Found Property", () -> switchToReportFound());

        navButtons.put("DASHBOARD", dashBtn);
        navButtons.put("REPORT_LOST", lostBtn);
        navButtons.put("REPORT_FOUND", foundBtn);

        navContainer.add(dashBtn);
        navContainer.add(Box.createVerticalStrut(8));
        navContainer.add(lostBtn);
        navContainer.add(Box.createVerticalStrut(8));
        navContainer.add(foundBtn);

        sidebar.add(navContainer, BorderLayout.CENTER);

        // Bottom System Status Card
        JPanel footerCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtils.COLOR_SIDEBAR_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        footerCard.setOpaque(false);
        footerCard.setLayout(new GridLayout(2, 1, 2, 4));
        footerCard.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel onlineStatus = new JLabel("● Storage Synced");
        onlineStatus.setFont(UIUtils.FONT_SMALL_BOLD);
        onlineStatus.setForeground(UIUtils.COLOR_SUCCESS);

        JLabel versionLabel = new JLabel("Best Designs Theme v2.2");
        versionLabel.setFont(UIUtils.FONT_SMALL);
        versionLabel.setForeground(UIUtils.COLOR_SIDEBAR_TEXT);

        footerCard.add(onlineStatus);
        footerCard.add(versionLabel);

        sidebar.add(footerCard, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(14, 24, 14, 24)
        ));

        // Breadcrumb
        JPanel breadcrumbPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        breadcrumbPanel.setOpaque(false);

        breadcrumbTitleLabel = new JLabel("Inventory Dashboard");
        breadcrumbTitleLabel.setFont(UIUtils.FONT_HEADER);
        breadcrumbTitleLabel.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        breadcrumbSubtitleLabel = new JLabel("Real-time property registry, status intelligence, and custody verification");
        breadcrumbSubtitleLabel.setFont(UIUtils.FONT_SUBTITLE);
        breadcrumbSubtitleLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        breadcrumbPanel.add(breadcrumbTitleLabel);
        breadcrumbPanel.add(breadcrumbSubtitleLabel);
        topBar.add(breadcrumbPanel, BorderLayout.WEST);

        // Action Buttons
        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        quickActions.setOpaque(false);

        JButton newLostBtn = UIUtils.createModernButton("+ Report Lost", UIUtils.COLOR_PRIMARY_LIGHT,
                UIUtils.COLOR_PRIMARY_BORDER, UIUtils.COLOR_PRIMARY, 8);
        newLostBtn.addActionListener(e -> switchToReportLost());

        JButton newFoundBtn = UIUtils.createDarkButton("+ Report Found");
        newFoundBtn.addActionListener(e -> switchToReportFound());

        quickActions.add(newLostBtn);
        quickActions.add(newFoundBtn);

        topBar.add(quickActions, BorderLayout.EAST);
        return topBar;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Color.WHITE);
        statusBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(8, 20, 8, 20)
        ));

        statusMessageLabel = new JLabel("System Ready. All records synchronized.");
        statusMessageLabel.setFont(UIUtils.FONT_SMALL);
        statusMessageLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        JLabel timestampLabel = new JLabel("DesignRush Hub • " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        timestampLabel.setFont(UIUtils.FONT_SMALL);
        timestampLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        statusBar.add(statusMessageLabel, BorderLayout.WEST);
        statusBar.add(timestampLabel, BorderLayout.EAST);

        return statusBar;
    }

    private void setupWindow() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.handleApplicationExit(MainFrame.this);
            }
        });

        setSize(1220, 820);
        setMinimumSize(new Dimension(1020, 680));
        setLocationRelativeTo(null);
    }

    private void setActiveNav(String key) {
        this.currentNavKey = key;
        for (Map.Entry<String, SidebarNavButton> entry : navButtons.entrySet()) {
            entry.getValue().setSelected(entry.getKey().equals(key));
        }

        switch (key) {
            case "DASHBOARD":
                breadcrumbTitleLabel.setText("Inventory Dashboard");
                breadcrumbSubtitleLabel.setText("Real-time property registry, status intelligence, and custody verification");
                contentCardLayout.show(mainContentCardPanel, "DASHBOARD");
                controller.refreshView();
                break;
            case "REPORT_LOST":
                breadcrumbTitleLabel.setText("Report a Lost Item");
                breadcrumbSubtitleLabel.setText("Submit owner claims and item specifications to enable matching by custodians");
                contentCardLayout.show(mainContentCardPanel, "REPORT_LOST");
                break;
            case "REPORT_FOUND":
                breadcrumbTitleLabel.setText("Report a Found Item");
                breadcrumbSubtitleLabel.setText("Log discovered property into inventory and establish safe custody tracking");
                contentCardLayout.show(mainContentCardPanel, "REPORT_FOUND");
                break;
        }
    }

    public void setStatusMessage(String message) {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText(message);
        }
    }

    public void switchToDashboard() {
        setActiveNav("DASHBOARD");
    }

    public void switchToReportLost() {
        setActiveNav("REPORT_LOST");
    }

    public void switchToReportFound() {
        setActiveNav("REPORT_FOUND");
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public ReportLostItemPanel getReportLostPanel() {
        return reportLostPanel;
    }

    public ReportFoundItemPanel getReportFoundPanel() {
        return reportFoundPanel;
    }

    /**
     * Modern sidebar navigation button styled with DesignRush red active bar.
     */
    private static class SidebarNavButton extends JPanel {
        private boolean selected = false;
        private boolean hovered = false;
        private final JLabel label;

        public SidebarNavButton(String title, Runnable onClick) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setPreferredSize(new Dimension(228, 44));

            label = new JLabel(title);
            label.setFont(UIUtils.FONT_BODY_BOLD);
            label.setForeground(UIUtils.COLOR_SIDEBAR_TEXT);
            label.setBorder(new EmptyBorder(0, 16, 0, 16));
            add(label, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    onClick.run();
                }
            });
        }

        public void setSelected(boolean sel) {
            this.selected = sel;
            label.setForeground(selected ? Color.WHITE : UIUtils.COLOR_SIDEBAR_TEXT);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (selected) {
                g2.setColor(UIUtils.COLOR_SIDEBAR_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));

                // Left active accent bar (DesignRush Signature Red)
                g2.setColor(UIUtils.COLOR_PRIMARY);
                g2.fill(new RoundRectangle2D.Float(2, 6, 4, getHeight() - 12, 4, 4));
            } else if (hovered) {
                g2.setColor(UIUtils.COLOR_SIDEBAR_HOVER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}

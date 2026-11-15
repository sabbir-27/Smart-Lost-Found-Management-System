package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.UserAccount;
import com.smartlostfound.model.UserRole;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Enterprise application frame styled with DesignRush Best Designs aesthetics:
 * Carbon-black gradient sidebar, vibrant signature red accents, editorial typography,
 * social media activity feed, dedicated User Panel, and executive Admin Console.
 */
public class MainFrame extends JFrame {

    private final LostFoundController controller;

    private CardLayout contentCardLayout;
    private JPanel mainContentCardPanel;

    private DashboardPanel dashboardPanel;
    private CommunityFeedPanel feedPanel;
    private ReportLostItemPanel reportLostPanel;
    private ReportFoundItemPanel reportFoundPanel;
    private MyItemsPanel myItemsPanel;
    private AdminConsolePanel adminConsolePanel;

    private JLabel breadcrumbTitleLabel;
    private JLabel breadcrumbSubtitleLabel;
    private JLabel statusMessageLabel;

    // Top Bar Action Container
    private JPanel topBarActionsContainer;

    // Sidebar responsive toggle components
    private JPanel sidebarPanel;
    private JPanel brandTextPanel;
    private JLabel adminSectionLabel;
    private JPanel footerCard;
    private JButton toggleSidebarBtn;
    private boolean isSidebarCollapsed = false;
    private boolean wasAutoCollapsed = false;

    // User Profile Chip components
    private AvatarCircleLabel userAvatarLabel;
    private JLabel userNameLabel;
    private JLabel userRoleBadgeLabel;
    private JButton authBtn;

    // Root Full-Page Switcher (Auth Page vs Main Workspace)
    private CardLayout rootCardLayout;
    private JPanel rootCardPanel;
    private AuthPagePanel authPagePanel;
    private JPanel mainWorkspaceWrapper;

    private final Map<String, SidebarNavButton> navButtons = new HashMap<>();
    private String currentNavKey = "DASHBOARD";

    public MainFrame(LostFoundController controller) {
        super("Smart Lost & Found Management System");
        this.controller = controller;

        initComponents();
        setupWindow();

        if (controller.getCurrentUser() != null) {
            updateUserSession(controller.getCurrentUser());
            showMainWorkspace();
        } else {
            updateUserSession(null);
            showAuthPage();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.COLOR_BACKGROUND);

        rootCardLayout = new CardLayout();
        rootCardPanel = new JPanel(rootCardLayout);
        rootCardPanel.setBackground(UIUtils.COLOR_BACKGROUND);

        // 1. Full-Page Authentication Portal View
        authPagePanel = new AuthPagePanel(controller, this);
        rootCardPanel.add(authPagePanel, "AUTH_VIEW");

        // 2. Main Workspace View (Sidebar + Top Bar + Content Panels + Status Bar)
        mainWorkspaceWrapper = new JPanel(new BorderLayout());
        mainWorkspaceWrapper.setBackground(UIUtils.COLOR_BACKGROUND);

        mainWorkspaceWrapper.add(createSidebarPanel(), BorderLayout.WEST);

        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(UIUtils.COLOR_BACKGROUND);

        workspace.add(createTopBar(), BorderLayout.NORTH);

        contentCardLayout = new CardLayout();
        mainContentCardPanel = new JPanel(contentCardLayout);
        mainContentCardPanel.setBackground(UIUtils.COLOR_BACKGROUND);

        dashboardPanel = new DashboardPanel(controller);
        feedPanel = new CommunityFeedPanel(controller);
        reportLostPanel = new ReportLostItemPanel(controller);
        reportFoundPanel = new ReportFoundItemPanel(controller);
        myItemsPanel = new MyItemsPanel(controller);
        adminConsolePanel = new AdminConsolePanel(controller);

        mainContentCardPanel.add(dashboardPanel, "DASHBOARD");
        mainContentCardPanel.add(feedPanel, "FEED");
        mainContentCardPanel.add(reportLostPanel, "REPORT_LOST");
        mainContentCardPanel.add(reportFoundPanel, "REPORT_FOUND");
        mainContentCardPanel.add(myItemsPanel, "MY_ITEMS");
        mainContentCardPanel.add(adminConsolePanel, "ADMIN_CONSOLE");

        workspace.add(mainContentCardPanel, BorderLayout.CENTER);
        workspace.add(createStatusBar(), BorderLayout.SOUTH);

        mainWorkspaceWrapper.add(workspace, BorderLayout.CENTER);
        rootCardPanel.add(mainWorkspaceWrapper, "APP_VIEW");

        add(rootCardPanel, BorderLayout.CENTER);

        // Default navigation
        setActiveNav("DASHBOARD");
    }

    private JPanel createSidebarPanel() {
        sidebarPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Vertical obsidian to deep carbon gradient
                GradientPaint gp = new GradientPaint(0, 0, UIUtils.COLOR_SIDEBAR_TOP, 0, getHeight(), UIUtils.COLOR_SIDEBAR_BOTTOM);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        sidebarPanel.setOpaque(false);
        sidebarPanel.setPreferredSize(new Dimension(260, 0));
        sidebarPanel.setBorder(new EmptyBorder(22, 16, 22, 16));

        // Brand Header (DesignRush style) with responsive collapse button
        JPanel brandPanel = new JPanel(new BorderLayout(8, 6));
        brandPanel.setOpaque(false);

        // Red Accent Icon in glossy capsule
        JPanel logoCapsule = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(229, 46, 45, 35));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(new Color(229, 46, 45, 90));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoCapsule.setPreferredSize(new Dimension(38, 38));
        logoCapsule.setOpaque(false);

        JLabel logoIcon = new JLabel("■");
        logoIcon.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 20));
        logoIcon.setForeground(UIUtils.COLOR_PRIMARY);
        logoCapsule.add(logoIcon);

        brandTextPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        brandTextPanel.setOpaque(false);

        JLabel brandTitle = new JLabel("LOST & FOUND");
        brandTitle.setFont(UIUtils.FONT_LOGO);
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSubtitle = new JLabel("DESIGNRUSH RECOVERY HUB");
        brandSubtitle.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 9));
        brandSubtitle.setForeground(UIUtils.COLOR_PRIMARY);

        brandTextPanel.add(brandTitle);
        brandTextPanel.add(brandSubtitle);

        // Sidebar Collapse Toggle Button
        toggleSidebarBtn = new JButton("◀") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        toggleSidebarBtn.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 12));
        toggleSidebarBtn.setForeground(UIUtils.COLOR_SIDEBAR_TEXT);
        toggleSidebarBtn.setFocusPainted(false);
        toggleSidebarBtn.setBorderPainted(false);
        toggleSidebarBtn.setContentAreaFilled(false);
        toggleSidebarBtn.setOpaque(false);
        toggleSidebarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleSidebarBtn.setPreferredSize(new Dimension(28, 28));
        toggleSidebarBtn.setToolTipText("Toggle Sidebar (Compact / Expanded)");
        toggleSidebarBtn.addActionListener(e -> setSidebarCollapsed(!isSidebarCollapsed));

        brandPanel.add(logoCapsule, BorderLayout.WEST);
        brandPanel.add(brandTextPanel, BorderLayout.CENTER);
        brandPanel.add(toggleSidebarBtn, BorderLayout.EAST);

        sidebarPanel.add(brandPanel, BorderLayout.NORTH);

        // Navigation Items
        JPanel navContainer = new JPanel();
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setOpaque(false);
        navContainer.setBorder(new EmptyBorder(26, 0, 0, 0));

        SidebarNavButton dashBtn = new SidebarNavButton("●", "Dashboard & Records", () -> switchToDashboard());
        SidebarNavButton feedBtn = new SidebarNavButton("📰", "Community Feed", () -> switchToFeed());
        SidebarNavButton lostBtn = new SidebarNavButton("▲", "Report Lost Property", () -> switchToReportLost());
        SidebarNavButton foundBtn = new SidebarNavButton("▼", "Report Found Property", () -> switchToReportFound());
        SidebarNavButton myItemsBtn = new SidebarNavButton("📂", "My Reported Items", () -> switchToMyItems());
        SidebarNavButton adminBtn = new SidebarNavButton("🛡️", "Admin Console", () -> switchToAdminConsole());

        navButtons.put("DASHBOARD", dashBtn);
        navButtons.put("FEED", feedBtn);
        navButtons.put("REPORT_LOST", lostBtn);
        navButtons.put("REPORT_FOUND", foundBtn);
        navButtons.put("MY_ITEMS", myItemsBtn);
        navButtons.put("ADMIN_CONSOLE", adminBtn);

        navContainer.add(dashBtn);
        navContainer.add(Box.createVerticalStrut(6));
        navContainer.add(feedBtn);
        navContainer.add(Box.createVerticalStrut(6));
        navContainer.add(lostBtn);
        navContainer.add(Box.createVerticalStrut(6));
        navContainer.add(foundBtn);
        navContainer.add(Box.createVerticalStrut(6));
        navContainer.add(myItemsBtn);
        navContainer.add(Box.createVerticalStrut(14));

        // Distinct Admin Section Divider
        adminSectionLabel = new JLabel("MANAGEMENT & GOVERNANCE");
        adminSectionLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 9));
        adminSectionLabel.setForeground(UIUtils.COLOR_SIDEBAR_MUTED);
        adminSectionLabel.setBorder(new EmptyBorder(0, 16, 4, 0));
        navContainer.add(adminSectionLabel);
        navContainer.add(adminBtn);

        sidebarPanel.add(navContainer, BorderLayout.CENTER);

        // Bottom System Status Card
        footerCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtils.COLOR_SIDEBAR_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(new Color(255, 255, 255, 12));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        footerCard.setOpaque(false);
        footerCard.setLayout(new GridLayout(2, 1, 2, 4));
        footerCard.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel onlineStatus = new JLabel("● SYSTEM ACTIVE & SYNCED");
        onlineStatus.setFont(UIUtils.FONT_SMALL_BOLD);
        onlineStatus.setForeground(UIUtils.COLOR_SUCCESS);

        JLabel versionLabel = new JLabel("Best Designs v2.2 • Executive Tier");
        versionLabel.setFont(UIUtils.FONT_SMALL);
        versionLabel.setForeground(UIUtils.COLOR_SIDEBAR_TEXT);

        footerCard.add(onlineStatus);
        footerCard.add(versionLabel);

        sidebarPanel.add(footerCard, BorderLayout.SOUTH);

        return sidebarPanel;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(12, 24, 12, 24)
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

        // Right Side: User Profile & Actions
        JPanel rightContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightContainer.setOpaque(false);

        // User Profile Chip
        JPanel profileChip = createProfileChip();
        rightContainer.add(profileChip);

        // Vertical separator
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 30));
        sep.setForeground(UIUtils.COLOR_BORDER);
        rightContainer.add(sep);

        // Dynamic Quick Action Buttons container (contextual per active view)
        topBarActionsContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topBarActionsContainer.setOpaque(false);
        rightContainer.add(topBarActionsContainer);

        topBar.add(rightContainer, BorderLayout.EAST);
        return topBar;
    }

    private JPanel createProfileChip() {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtils.COLOR_SURFACE_ALT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));
                g2.setColor(UIUtils.COLOR_BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 24, 24));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setOpaque(false);
        chip.setBorder(new EmptyBorder(4, 8, 4, 8));

        userAvatarLabel = new AvatarCircleLabel("SA", UIUtils.COLOR_PRIMARY);

        JPanel details = new JPanel(new GridLayout(2, 1, 0, 1));
        details.setOpaque(false);

        userNameLabel = new JLabel("System Administrator");
        userNameLabel.setFont(UIUtils.FONT_BODY_BOLD);
        userNameLabel.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        userRoleBadgeLabel = new JLabel("🛡️ ADMIN");
        userRoleBadgeLabel.setFont(UIUtils.FONT_BADGE);
        userRoleBadgeLabel.setForeground(UIUtils.COLOR_PRIMARY);

        details.add(userNameLabel);
        details.add(userRoleBadgeLabel);

        authBtn = UIUtils.createModernButton(controller.getCurrentUser() == null ? "Sign In" : "Switch",
                new Color(244, 244, 246), new Color(228, 228, 232), UIUtils.COLOR_TEXT_PRIMARY, 6);
        authBtn.setFont(UIUtils.FONT_SMALL_BOLD);
        authBtn.setBorder(new EmptyBorder(5, 10, 5, 10));
        authBtn.addActionListener(e -> showAuthPage());

        chip.add(userAvatarLabel);
        chip.add(details);
        chip.add(authBtn);

        return chip;
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

        JLabel timestampLabel = new JLabel("Community Hub • " +
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

        // Dynamic responsive sizing honoring OS desktop work area and device dimensions
        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int initialW = Math.min(1280, (int) (screenBounds.width * 0.94));
        int initialH = Math.min(840, (int) (screenBounds.height * 0.92));

        setSize(initialW, initialH);
        setMinimumSize(new Dimension(800, 540));
        setLocationRelativeTo(null);

        // Auto-collapse sidebar on smaller displays or tiling
        if (initialW < 960) {
            setSidebarCollapsed(true);
            wasAutoCollapsed = true;
        }

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = getWidth();
                if (w < 920 && !isSidebarCollapsed) {
                    setSidebarCollapsed(true);
                    wasAutoCollapsed = true;
                } else if (w >= 1080 && isSidebarCollapsed && wasAutoCollapsed) {
                    setSidebarCollapsed(false);
                    wasAutoCollapsed = false;
                }
            }
        });
    }

    public void setSidebarCollapsed(boolean collapsed) {
        this.isSidebarCollapsed = collapsed;
        if (sidebarPanel == null) return;

        if (collapsed) {
            sidebarPanel.setPreferredSize(new Dimension(68, 0));
            sidebarPanel.setBorder(new EmptyBorder(18, 10, 18, 10));
            if (brandTextPanel != null) brandTextPanel.setVisible(false);
            if (adminSectionLabel != null) adminSectionLabel.setVisible(false);
            if (footerCard != null) footerCard.setVisible(false);
            if (toggleSidebarBtn != null) {
                toggleSidebarBtn.setText("▶");
                toggleSidebarBtn.setToolTipText("Expand Sidebar Navigation");
            }
            for (SidebarNavButton btn : navButtons.values()) {
                btn.setCollapsed(true);
            }
        } else {
            sidebarPanel.setPreferredSize(new Dimension(260, 0));
            sidebarPanel.setBorder(new EmptyBorder(22, 16, 22, 16));
            if (brandTextPanel != null) brandTextPanel.setVisible(true);
            if (adminSectionLabel != null) adminSectionLabel.setVisible(true);
            if (footerCard != null) footerCard.setVisible(true);
            if (toggleSidebarBtn != null) {
                toggleSidebarBtn.setText("◀");
                toggleSidebarBtn.setToolTipText("Collapse Sidebar Navigation");
            }
            for (SidebarNavButton btn : navButtons.values()) {
                btn.setCollapsed(false);
            }
        }

        sidebarPanel.revalidate();
        sidebarPanel.repaint();
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
            case "FEED":
                breadcrumbTitleLabel.setText("Community Social Feed");
                breadcrumbSubtitleLabel.setText("Facebook-style community stream of lost items, reported sightings, and reward notices");
                contentCardLayout.show(mainContentCardPanel, "FEED");
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
            case "MY_ITEMS":
                breadcrumbTitleLabel.setText("My Reported Items & Claims");
                breadcrumbSubtitleLabel.setText("Track the real-time custody status, match alerts, and resolutions for your submissions");
                contentCardLayout.show(mainContentCardPanel, "MY_ITEMS");
                myItemsPanel.refreshData();
                break;
            case "ADMIN_CONSOLE":
                breadcrumbTitleLabel.setText("Executive Administration Console");
                breadcrumbSubtitleLabel.setText("Manage user directory, role permissions, account statuses, and system diagnostics");
                contentCardLayout.show(mainContentCardPanel, "ADMIN_CONSOLE");
                adminConsolePanel.refreshData();
                break;
        }

        updateTopBarActions(key);
    }

    /**
     * Updates top action buttons contextually to eliminate duplicate report actions
     * when browsing dedicated sections like the Community Feed or reporting forms.
     */
    private void updateTopBarActions(String key) {
        if (topBarActionsContainer == null) return;
        topBarActionsContainer.removeAll();

        switch (key) {
            case "FEED":
                // In Community Feed: do not duplicate "Report Lost" / "Report Found" buttons
                // from the header since the feed already has its own dedicated social composer.
                JButton refreshFeedBtn = UIUtils.createModernButton("🔄 Refresh Feed", new Color(244, 244, 246),
                        new Color(228, 228, 232), UIUtils.COLOR_TEXT_PRIMARY, 8);
                refreshFeedBtn.addActionListener(e -> controller.refreshView());
                topBarActionsContainer.add(refreshFeedBtn);
                break;

            case "DASHBOARD":
                JButton feedToggleBtn = UIUtils.createModernButton("📰 Community Feed", new Color(244, 244, 246),
                        new Color(228, 228, 232), UIUtils.COLOR_TEXT_PRIMARY, 8);
                feedToggleBtn.addActionListener(e -> switchToFeed());

                JButton dashLostBtn = UIUtils.createPrimaryButton("+ Report Lost");
                dashLostBtn.addActionListener(e -> switchToReportLost());

                JButton dashFoundBtn = UIUtils.createDarkButton("+ Report Found");
                dashFoundBtn.addActionListener(e -> switchToReportFound());

                topBarActionsContainer.add(feedToggleBtn);
                topBarActionsContainer.add(dashLostBtn);
                topBarActionsContainer.add(dashFoundBtn);
                break;

            case "REPORT_LOST":
                JButton feedFromLost = UIUtils.createModernButton("📰 Community Feed", new Color(244, 244, 246),
                        new Color(228, 228, 232), UIUtils.COLOR_TEXT_PRIMARY, 8);
                feedFromLost.addActionListener(e -> switchToFeed());

                JButton backDashFromLost = UIUtils.createDarkButton("● Inventory Records");
                backDashFromLost.addActionListener(e -> switchToDashboard());

                topBarActionsContainer.add(feedFromLost);
                topBarActionsContainer.add(backDashFromLost);
                break;

            case "REPORT_FOUND":
                JButton feedFromFound = UIUtils.createModernButton("📰 Community Feed", new Color(244, 244, 246),
                        new Color(228, 228, 232), UIUtils.COLOR_TEXT_PRIMARY, 8);
                feedFromFound.addActionListener(e -> switchToFeed());

                JButton backDashFromFound = UIUtils.createDarkButton("● Inventory Records");
                backDashFromFound.addActionListener(e -> switchToDashboard());

                topBarActionsContainer.add(feedFromFound);
                topBarActionsContainer.add(backDashFromFound);
                break;

            case "MY_ITEMS":
                JButton myLostBtn = UIUtils.createPrimaryButton("+ Report Lost");
                myLostBtn.addActionListener(e -> switchToReportLost());

                JButton myFoundBtn = UIUtils.createDarkButton("+ Report Found");
                myFoundBtn.addActionListener(e -> switchToReportFound());

                topBarActionsContainer.add(myLostBtn);
                topBarActionsContainer.add(myFoundBtn);
                break;

            case "ADMIN_CONSOLE":
                JButton syncBtn = UIUtils.createDarkButton("🔄 Sync Directory");
                syncBtn.addActionListener(e -> adminConsolePanel.refreshData());
                topBarActionsContainer.add(syncBtn);
                break;
        }

        topBarActionsContainer.revalidate();
        topBarActionsContainer.repaint();
    }

    public void updateUserSession(UserAccount user) {
        if (user == null) {
            userNameLabel.setText("Guest Visitor");
            userRoleBadgeLabel.setText("🔒 BROWSE ONLY");
            userRoleBadgeLabel.setForeground(new Color(248, 113, 113));
            userAvatarLabel.setText("GV");
            userAvatarLabel.setBackgroundColor(new Color(63, 63, 70));
            if (authBtn != null) {
                authBtn.setText("Sign In / Register");
            }
            if (reportLostPanel != null) {
                reportLostPanel.autoFillReporter(null);
            }
            if (reportFoundPanel != null) {
                reportFoundPanel.autoFillReporter(null);
            }

            // Lock all non-feed sections in sidebar for guest browsers
            setNavButtonLocked("DASHBOARD", true);
            setNavButtonLocked("REPORT_LOST", true);
            setNavButtonLocked("REPORT_FOUND", true);
            setNavButtonLocked("MY_ITEMS", true);
            setNavButtonLocked("ADMIN_CONSOLE", true);
            setNavButtonLocked("FEED", false);

            setStatusMessage("Browsing in Guest Mode (Community Feed Only) • Sign in to access other features");
            repaint();
            return;
        }

        userNameLabel.setText(user.getName() + " (@" + user.getUsername() + ")");
        if (user.isAdmin()) {
            userRoleBadgeLabel.setText("🛡️ ADMINISTRATOR");
            userRoleBadgeLabel.setForeground(UIUtils.COLOR_PRIMARY);
            userAvatarLabel.setBackgroundColor(UIUtils.COLOR_PRIMARY);
        } else {
            userRoleBadgeLabel.setText("👤 STANDARD USER");
            userRoleBadgeLabel.setForeground(UIUtils.COLOR_BLUE);
            userAvatarLabel.setBackgroundColor(new Color(39, 39, 42));
        }

        userAvatarLabel.setText(user.getInitials());

        if (authBtn != null) {
            authBtn.setText("Switch");
        }

        // Unlock sections according to account permissions
        setNavButtonLocked("DASHBOARD", false);
        setNavButtonLocked("FEED", false);
        setNavButtonLocked("REPORT_LOST", false);
        setNavButtonLocked("REPORT_FOUND", false);
        setNavButtonLocked("MY_ITEMS", false);
        setNavButtonLocked("ADMIN_CONSOLE", !user.isAdmin());

        // Pre-fill reporting forms with this user's profile
        if (reportLostPanel != null) {
            reportLostPanel.autoFillReporter(user);
        }
        if (reportFoundPanel != null) {
            reportFoundPanel.autoFillReporter(user);
        }

        setStatusMessage("Authenticated as @" + user.getUsername() + " (" + user.getRole().getDisplayName() + ")");
        repaint();
    }

    private void setNavButtonLocked(String key, boolean locked) {
        SidebarNavButton btn = navButtons.get(key);
        if (btn != null) {
            btn.setLocked(locked);
        }
    }

    public void enterGuestBrowseMode() {
        controller.setCurrentUser(null);
        updateUserSession(null);
        showMainWorkspace();
        setActiveNav("FEED");
        setStatusMessage("Guest Mode (Community Feed Only) • Sign in to report or claim items");
    }

    public void promptGuestSignIn(String actionDescription) {
        int choice = JOptionPane.showOptionDialog(
                this,
                "Authentication Required:\n\n" +
                "You are currently browsing in Guest Mode (Community Feed Only).\n" +
                "To " + actionDescription + ", please sign in to your account or register.\n",
                "Sign In Required",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Sign In / Register", "Stay on Feed"},
                "Sign In / Register"
        );
        if (choice == 0) {
            showAuthPage();
        }
    }

    public void setStatusMessage(String message) {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText(message);
        }
    }

    public void switchToDashboard() {
        if (controller.getCurrentUser() == null) {
            promptGuestSignIn("access the Dashboard and inventory records");
            return;
        }
        setActiveNav("DASHBOARD");
    }

    public void switchToFeed() {
        setActiveNav("FEED");
    }

    public void switchToReportLost() {
        if (controller.getCurrentUser() == null) {
            promptGuestReportAction("Lost", "REPORT_LOST");
            return;
        }
        setActiveNav("REPORT_LOST");
    }

    public void switchToReportFound() {
        if (controller.getCurrentUser() == null) {
            promptGuestReportAction("Found", "REPORT_FOUND");
            return;
        }
        setActiveNav("REPORT_FOUND");
    }

    public void promptGuestReportAction(String reportType, String targetNavKey) {
        GuestReportAuthDialog.prompt(this, controller, reportType, targetNavKey);
    }

    public void switchToMyItems() {
        if (controller.getCurrentUser() == null) {
            promptGuestSignIn("view and manage your reported items");
            return;
        }
        setActiveNav("MY_ITEMS");
    }

    public void switchToAdminConsole() {
        if (controller.getCurrentUser() == null) {
            promptGuestSignIn("access the Executive Administrative Control Console");
            return;
        }
        if (!controller.isAdmin()) {
            UserAccount current = controller.getCurrentUser();
            int choice = JOptionPane.showConfirmDialog(this,
                    "Access Restricted:\n" +
                    "Executive Administrator privileges are required to access the Admin Console.\n" +
                    "Currently signed in as: @" + current.getUsername() +
                    " (" + current.getRole().getDisplayName() + ").\n\n" +
                    "Would you like to switch to an Administrator account (e.g. admin / admin123)?",
                    "Administrative Permission Required",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                showAuthPage();
            }
            return;
        }

        setActiveNav("ADMIN_CONSOLE");
    }

    public void showAuthPage() {
        showAuthPage(false, null);
    }

    public void showAuthPage(boolean showRegister, String pendingTarget) {
        if (authPagePanel != null) {
            authPagePanel.updateReturnToAppVisibility(controller.getCurrentUser() != null);
            authPagePanel.resetFields();
            authPagePanel.setPendingPostAuthTarget(pendingTarget);
            if (showRegister) {
                authPagePanel.showRegisterCard();
            } else {
                authPagePanel.showSignInCard();
            }
        }
        rootCardLayout.show(rootCardPanel, "AUTH_VIEW");
        setTitle("Sign In & Authentication - Smart Lost & Found");
    }

    public void showMainWorkspace() {
        rootCardLayout.show(rootCardPanel, "APP_VIEW");
        setTitle("Smart Lost & Found Management System");
        setActiveNav(currentNavKey);
        controller.refreshView();
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public CommunityFeedPanel getFeedPanel() {
        return feedPanel;
    }

    public ReportLostItemPanel getReportLostPanel() {
        return reportLostPanel;
    }

    public ReportFoundItemPanel getReportFoundPanel() {
        return reportFoundPanel;
    }

    public MyItemsPanel getMyItemsPanel() {
        return myItemsPanel;
    }

    public AdminConsolePanel getAdminConsolePanel() {
        return adminConsolePanel;
    }

    /**
     * Circular Avatar Label with anti-aliased colored edge ring.
     */
    private static class AvatarCircleLabel extends JLabel {
        private Color bgColor;

        public AvatarCircleLabel(String initials, Color bgColor) {
            super(initials, SwingConstants.CENTER);
            this.bgColor = bgColor;
            setPreferredSize(new Dimension(36, 36));
            setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 12));
            setForeground(Color.WHITE);
            setOpaque(false);
        }

        public void setBackgroundColor(Color color) {
            this.bgColor = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fill circle
            g2.setColor(bgColor);
            g2.fill(new Ellipse2D.Float(1, 1, getWidth() - 2, getHeight() - 2));

            // Inner highlight ring
            g2.setColor(new Color(255, 255, 255, 90));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new Ellipse2D.Float(1.5f, 1.5f, getWidth() - 3, getHeight() - 3));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Modern sidebar navigation button styled with DesignRush red active bar and subtle ambient glow.
     */
    private static class SidebarNavButton extends JPanel {
        private boolean selected = false;
        private boolean hovered = false;
        private boolean collapsed = false;
        private final String iconOnly;
        private final String fullTitle;
        private final JLabel label;

        public SidebarNavButton(String iconOnly, String fullTitle, Runnable onClick) {
            this.iconOnly = iconOnly;
            this.fullTitle = fullTitle;
            setLayout(new BorderLayout());
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setPreferredSize(new Dimension(228, 44));
            setToolTipText(fullTitle);

            label = new JLabel(iconOnly + "  " + fullTitle);
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

        private boolean locked = false;

        public void setLocked(boolean locked) {
            this.locked = locked;
            updateDisplay();
        }

        public void setCollapsed(boolean col) {
            this.collapsed = col;
            updateDisplay();
        }

        private void updateDisplay() {
            if (collapsed) {
                label.setText(locked ? "🔒" : iconOnly);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(new EmptyBorder(0, 0, 0, 0));
                setPreferredSize(new Dimension(48, 44));
            } else {
                String text = iconOnly + "  " + fullTitle + (locked ? "  🔒" : "");
                label.setText(text);
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setBorder(new EmptyBorder(0, 16, 0, 16));
                setPreferredSize(new Dimension(228, 44));
            }
            label.setForeground(locked ? UIUtils.COLOR_SIDEBAR_MUTED : (selected ? Color.WHITE : UIUtils.COLOR_SIDEBAR_TEXT));
            setToolTipText(fullTitle + (locked ? " (Sign in required)" : ""));
            revalidate();
            repaint();
        }

        public void setSelected(boolean sel) {
            this.selected = sel;
            label.setForeground(selected ? Color.WHITE : (locked ? UIUtils.COLOR_SIDEBAR_MUTED : UIUtils.COLOR_SIDEBAR_TEXT));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (selected) {
                // Active Card surface
                g2.setColor(UIUtils.COLOR_SIDEBAR_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));

                // Left active accent bar (DesignRush Signature Red)
                g2.setColor(UIUtils.COLOR_PRIMARY);
                g2.fill(new RoundRectangle2D.Float(2, 6, 4, getHeight() - 12, 4, 4));

                // Subtle red glow on active
                g2.setColor(new Color(229, 46, 45, 18));
                g2.fill(new RoundRectangle2D.Float(6, 0, getWidth() - 6, getHeight(), 10, 10));
            } else if (hovered) {
                g2.setColor(UIUtils.COLOR_SIDEBAR_HOVER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}

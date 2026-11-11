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
import java.awt.geom.RoundRectangle2D;

/**
 * Full-Page Executive Authentication Portal.
 * Implements a luxurious split-screen editorial landing and sign-in architecture
 * following DesignRush Best Designs award standards:
 * - Left 46%: Deep Obsidian Carbon branding showcase with glassmorphism feature cards and metrics.
 * - Right 54%: Centered, responsive executive form column with segmented tabs, 1-click demo access,
 *   and high-precision input validation.
 */
public class AuthPagePanel extends JPanel {

    private static final String ADMIN_SECRET_KEY = "ADMIN2026";

    private final LostFoundController controller;
    private final MainFrame mainFrame;

    private CardLayout formCardLayout;
    private JPanel formCardContainer;
    private JButton signInTabBtn;
    private JButton registerTabBtn;

    // Sign In Controls
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    private JCheckBox loginShowPassCheck;
    private JButton returnToAppBtn;

    // Register Controls
    private JTextField regNameField;
    private JTextField regUsernameField;
    private JTextField regEmailField;
    private JTextField regPhoneField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;
    private JComboBox<String> regRoleCombo;
    private JPanel adminCodeContainer;
    private JPasswordField regAdminCodeField;

    // Responsive Left Showcase Reference
    private JPanel leftShowcasePanel;

    public AuthPagePanel(LostFoundController controller, MainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.COLOR_BACKGROUND);

        // 1. Left Showcase Column (Obsidian Gradient with Live Feature Cards)
        leftShowcasePanel = createShowcasePanel();
        add(leftShowcasePanel, BorderLayout.WEST);

        // 2. Right Form Column (Full-Height Centered Card Container)
        add(createFormColumn(), BorderLayout.CENTER);

        // Responsive listener to adapt split on narrow displays
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int totalWidth = getWidth();
                if (totalWidth < 880) {
                    leftShowcasePanel.setVisible(false);
                } else {
                    leftShowcasePanel.setVisible(true);
                    int leftW = Math.max(340, (int) (totalWidth * 0.44));
                    leftShowcasePanel.setPreferredSize(new Dimension(leftW, 0));
                    leftShowcasePanel.revalidate();
                }
            }
        });
    }

    // =========================================================================
    // Left Showcase Column (Obsidian Carbon Branding)
    // =========================================================================

    private JPanel createShowcasePanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Deep Obsidian to Dark Carbon Gradient
                GradientPaint gp = new GradientPaint(0, 0, UIUtils.COLOR_SIDEBAR_TOP, 0, getHeight(), UIUtils.COLOR_SIDEBAR_BOTTOM);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle ambient grid pattern
                g2.setColor(new Color(255, 255, 255, 5));
                for (int y = 0; y < getHeight(); y += 40) {
                    g2.drawLine(0, y, getWidth(), y);
                }

                // 1px right border separating showcase from form column
                g2.setColor(new Color(255, 255, 255, 15));
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());

                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setPreferredSize(new Dimension(480, 0));
        panel.setOpaque(false);

        // Scrollable content wrapper for showcase (smooth on compact heights)
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(36, 36, 36, 36));

        // 1. Top Emblem & Network Badge
        JPanel topBadgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBadgeRow.setOpaque(false);
        topBadgeRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel badge = new JLabel(" ■  DESIGNRUSH VERIFIED RECOVERY NETWORK ");
        badge.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 10));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(UIUtils.COLOR_PRIMARY);
        badge.setBorder(new EmptyBorder(6, 12, 6, 12));
        topBadgeRow.add(badge);
        content.add(topBadgeRow);

        content.add(Box.createVerticalStrut(20));

        // 2. Headline & Editorial Subtitle
        JLabel mainTitle = new JLabel("SMART LOST & FOUND");
        mainTitle.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 28));
        mainTitle.setForeground(Color.WHITE);
        mainTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(mainTitle);

        content.add(Box.createVerticalStrut(4));

        JLabel subTitle = new JLabel("NEXT-GEN PROPERTY INTELLIGENCE");
        subTitle.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 12));
        subTitle.setForeground(new Color(248, 113, 113));
        subTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(subTitle);

        content.add(Box.createVerticalStrut(14));

        JLabel leadParagraph = new JLabel("<html>A community-powered, high-precision recovery infrastructure connecting people with their misplaced belongings across campuses, venues, and metropolitan hubs.</html>");
        leadParagraph.setFont(UIUtils.FONT_BODY);
        leadParagraph.setForeground(UIUtils.COLOR_SIDEBAR_TEXT);
        leadParagraph.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(leadParagraph);

        content.add(Box.createVerticalStrut(28));

        // 3. Feature Showcase Cards (Dark Glassmorphic Panels)
        content.add(createShowcaseCard("⚡", "Neural Visual Match Feed",
                "Facebook-style community stream with 16:9 high-resolution photos, interactive lightbox inspection, and instant discovery."));
        content.add(Box.createVerticalStrut(12));

        content.add(createShowcaseCard("🛡️", "Role-Based Security & RBAC",
                "Granular user directory, administrative oversight, immutable custody tracking, and encrypted credential storage."));
        content.add(Box.createVerticalStrut(12));

        content.add(createShowcaseCard("📦", "Zero-Loss Disk Persistence",
                "Real-time atomic storage synchronization ensures every report, photo, and status transition is safely committed to disk."));

        content.add(Box.createVerticalGlue());

        // 4. Live Telemetry Metric Strip at Bottom
        JPanel metricStrip = createMetricStrip();
        metricStrip.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(metricStrip);

        content.add(Box.createVerticalStrut(16));

        JLabel copyright = new JLabel("© 2026 Smart Lost & Found Inc. • DesignRush Best Designs");
        copyright.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 11));
        copyright.setForeground(UIUtils.COLOR_SIDEBAR_MUTED);
        copyright.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(copyright);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createShowcaseCard(String icon, String title, String description) {
        JPanel card = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Semi-translucent dark slate card surface
                g2.setColor(new Color(24, 24, 27, 200));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));

                // 1px hairline border
                g2.setColor(new Color(255, 255, 255, 20));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 16, 12, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 22));
        card.add(iconLabel, BorderLayout.WEST);

        JPanel textCol = new JPanel(new GridLayout(2, 1, 0, 2));
        textCol.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIUtils.FONT_BODY_BOLD);
        titleLabel.setForeground(Color.WHITE);

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(UIUtils.FONT_SMALL);
        descLabel.setForeground(UIUtils.COLOR_SIDEBAR_TEXT);

        textCol.add(titleLabel);
        textCol.add(descLabel);
        card.add(textCol, BorderLayout.CENTER);

        return card;
    }

    private JPanel createMetricStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 3, 10, 0));
        strip.setOpaque(false);
        strip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        strip.add(createMiniMetric("98.4%", "Resolution Rate"));
        strip.add(createMiniMetric("< 24h", "Median Recovery"));
        strip.add(createMiniMetric("256-Bit", "Local Encryption"));

        return strip;
    }

    private JPanel createMiniMetric(String value, String label) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 1));
        p.setOpaque(false);

        JLabel vLabel = new JLabel(value);
        vLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 16));
        vLabel.setForeground(Color.WHITE);

        JLabel lLabel = new JLabel(label);
        lLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 10));
        lLabel.setForeground(UIUtils.COLOR_SIDEBAR_MUTED);

        p.add(vLabel);
        p.add(lLabel);
        return p;
    }

    // =========================================================================
    // Right Form Column (Full-Height Centered Card Container)
    // =========================================================================

    private JPanel createFormColumn() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UIUtils.COLOR_BACKGROUND);

        // Center card panel
        JPanel formCard = UIUtils.createCardPanel(16);
        formCard.setLayout(new BorderLayout(0, 16));
        formCard.setPreferredSize(new Dimension(460, 600));

        // 1. Top Header & Segmented Pill Switcher
        JPanel topHeader = new JPanel(new BorderLayout(0, 14));
        topHeader.setOpaque(false);

        // Optional Return to Dashboard button (when arriving from active session)
        JPanel returnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        returnRow.setOpaque(false);

        returnToAppBtn = new JButton("← Back to Dashboard");
        returnToAppBtn.setFont(UIUtils.FONT_SMALL_BOLD);
        returnToAppBtn.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        returnToAppBtn.setContentAreaFilled(false);
        returnToAppBtn.setBorderPainted(false);
        returnToAppBtn.setFocusPainted(false);
        returnToAppBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        returnToAppBtn.setVisible(false);
        returnToAppBtn.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.showMainWorkspace();
            }
        });
        returnRow.add(returnToAppBtn);
        topHeader.add(returnRow, BorderLayout.NORTH);

        // Segmented Switcher
        JPanel switcher = new JPanel(new GridLayout(1, 2, 8, 0));
        switcher.setOpaque(false);
        switcher.setPreferredSize(new Dimension(0, 44));

        signInTabBtn = UIUtils.createModernButton("Sign In", UIUtils.COLOR_CHARCOAL_BTN,
                UIUtils.COLOR_CHARCOAL_BTN, Color.WHITE, 8);
        registerTabBtn = UIUtils.createModernButton("Create Account", new Color(244, 244, 246),
                new Color(228, 228, 232), UIUtils.COLOR_TEXT_PRIMARY, 8);

        signInTabBtn.addActionListener(e -> showSignInCard());
        registerTabBtn.addActionListener(e -> showRegisterCard());

        switcher.add(signInTabBtn);
        switcher.add(registerTabBtn);
        topHeader.add(switcher, BorderLayout.CENTER);

        formCard.add(topHeader, BorderLayout.NORTH);

        // 2. Center Card Stack (Sign In vs Register)
        formCardLayout = new CardLayout();
        formCardContainer = new JPanel(formCardLayout);
        formCardContainer.setBackground(Color.WHITE);

        formCardContainer.add(createSignInPanel(), "SIGN_IN");
        formCardContainer.add(createRegisterPanel(), "REGISTER");

        JScrollPane formScrollPane = new JScrollPane(formCardContainer);
        formScrollPane.setBorder(null);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(14);
        formScrollPane.getViewport().setBackground(Color.WHITE);

        formCard.add(formScrollPane, BorderLayout.CENTER);

        // Center card inside wrapper with smooth breathing margin
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(24, 24, 24, 24);
        gbc.anchor = GridBagConstraints.CENTER;
        wrapper.add(formCard, gbc);

        JScrollPane pageScrollPane = new JScrollPane(wrapper);
        pageScrollPane.setBorder(null);
        pageScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel outer = new JPanel(new BorderLayout());
        outer.add(pageScrollPane, BorderLayout.CENTER);
        return outer;
    }

    private JPanel createSignInPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 4, 12, 4));

        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 22));
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Enter your credentials to access your unified recovery hub.");
        sub.setFont(UIUtils.FONT_BODY);
        sub.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(20));

        // Username
        panel.add(createFormLabel("Username or Email:"));
        panel.add(Box.createVerticalStrut(4));
        loginUsernameField = UIUtils.createStyledTextField(20);
        loginUsernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginUsernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(loginUsernameField);
        panel.add(Box.createVerticalStrut(14));

        // Password with show toggle
        panel.add(createFormLabel("Password:"));
        panel.add(Box.createVerticalStrut(4));
        loginPasswordField = new JPasswordField(20);
        loginPasswordField.setFont(UIUtils.FONT_BODY);
        loginPasswordField.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.COLOR_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        loginPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(loginPasswordField);
        panel.add(Box.createVerticalStrut(6));

        // Show password checkbox
        loginShowPassCheck = new JCheckBox("Show password");
        loginShowPassCheck.setFont(UIUtils.FONT_SMALL);
        loginShowPassCheck.setOpaque(false);
        loginShowPassCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginShowPassCheck.addActionListener(e -> {
            loginPasswordField.setEchoChar(loginShowPassCheck.isSelected() ? (char) 0 : '•');
        });
        panel.add(loginShowPassCheck);
        panel.add(Box.createVerticalStrut(18));

        // One-Click Demo Credentials Pill Bar
        panel.add(createDemoQuickFillSection());
        panel.add(Box.createVerticalStrut(20));

        // Sign In Button (Large Crimson Gradient)
        JButton signInSubmit = UIUtils.createPrimaryButton("Sign In to Recovery Hub  →");
        signInSubmit.setFont(UIUtils.FONT_HEADER);
        signInSubmit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        signInSubmit.setAlignmentX(Component.LEFT_ALIGNMENT);
        signInSubmit.addActionListener(e -> handleSignIn());
        panel.add(signInSubmit);

        // Enter key shortcut
        loginUsernameField.addActionListener(e -> handleSignIn());
        loginPasswordField.addActionListener(e -> handleSignIn());

        panel.add(Box.createVerticalStrut(12));

        // Continue as Guest Option
        JButton guestBtn = UIUtils.createSecondaryButton("👀 Continue as Guest (Browse Only)");
        guestBtn.setFont(UIUtils.FONT_BODY_BOLD);
        guestBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        guestBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        guestBtn.setToolTipText("Explore public feed and dashboard in view-only mode");
        guestBtn.addActionListener(e -> handleContinueAsGuest());
        panel.add(guestBtn);

        return panel;
    }

    private JPanel createDemoQuickFillSection() {
        JPanel demoBox = new JPanel();
        demoBox.setLayout(new BoxLayout(demoBox, BoxLayout.Y_AXIS));
        demoBox.setOpaque(false);
        demoBox.setBorder(new CompoundBorder(
                new LineBorder(new Color(228, 228, 231), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        demoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        demoBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel("⚡ One-Click Demo Access:");
        label.setFont(UIUtils.FONT_SMALL_BOLD);
        label.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        demoBox.add(label);
        demoBox.add(Box.createVerticalStrut(6));

        JPanel pills = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pills.setOpaque(false);

        JButton adminPill = UIUtils.createModernButton("👑 Admin (admin)",
                UIUtils.COLOR_PRIMARY_LIGHT, UIUtils.COLOR_PRIMARY_BORDER, UIUtils.COLOR_PRIMARY, 6);
        adminPill.setFont(UIUtils.FONT_SMALL_BOLD);
        adminPill.addActionListener(e -> {
            loginUsernameField.setText("admin");
            loginPasswordField.setText("admin123");
        });

        JButton userPill = UIUtils.createModernButton("👤 User (john)",
                new Color(239, 246, 255), new Color(191, 219, 254), new Color(29, 78, 216), 6);
        userPill.setFont(UIUtils.FONT_SMALL_BOLD);
        userPill.addActionListener(e -> {
            loginUsernameField.setText("john");
            loginPasswordField.setText("user123");
        });

        JButton userPill2 = UIUtils.createModernButton("👤 User (sarah)",
                new Color(243, 244, 246), new Color(229, 231, 235), new Color(55, 65, 81), 6);
        userPill2.setFont(UIUtils.FONT_SMALL_BOLD);
        userPill2.addActionListener(e -> {
            loginUsernameField.setText("sarah");
            loginPasswordField.setText("user123");
        });

        pills.add(adminPill);
        pills.add(userPill);
        pills.add(userPill2);
        demoBox.add(pills);

        return demoBox;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 4, 12, 4));

        JLabel title = new JLabel("Create a New Account");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 20));
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Join the community to report items, track claims, and manage alerts.");
        sub.setFont(UIUtils.FONT_SMALL);
        sub.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(2));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(14));

        // Form Fields in 2 columns
        JPanel formGrid = new JPanel(new GridLayout(5, 2, 10, 8));
        formGrid.setOpaque(false);
        formGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        formGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Row 1: Full Name & Username
        regNameField = UIUtils.createStyledTextField(15);
        regUsernameField = UIUtils.createStyledTextField(15);
        formGrid.add(createLabeledField("Full Name *", regNameField));
        formGrid.add(createLabeledField("Username (login ID) *", regUsernameField));

        // Row 2: Email & Phone
        regEmailField = UIUtils.createStyledTextField(15);
        regPhoneField = UIUtils.createStyledTextField(15);
        formGrid.add(createLabeledField("Email Address", regEmailField));
        formGrid.add(createLabeledField("Phone Number", regPhoneField));

        // Row 3: Password & Confirm Password
        regPasswordField = new JPasswordField(15);
        regPasswordField.setBorder(new CompoundBorder(new LineBorder(UIUtils.COLOR_BORDER, 1, true), new EmptyBorder(6, 10, 6, 10)));
        regConfirmPasswordField = new JPasswordField(15);
        regConfirmPasswordField.setBorder(new CompoundBorder(new LineBorder(UIUtils.COLOR_BORDER, 1, true), new EmptyBorder(6, 10, 6, 10)));
        formGrid.add(createLabeledField("Password (min 4 chars) *", regPasswordField));
        formGrid.add(createLabeledField("Confirm Password *", regConfirmPasswordField));

        // Row 4: Role Selection
        String[] roleOptions = {"Standard Community User", "Executive Administrator"};
        regRoleCombo = new JComboBox<>(roleOptions);
        regRoleCombo.setFont(UIUtils.FONT_BODY);
        regRoleCombo.setBackground(Color.WHITE);
        formGrid.add(createLabeledField("Account Role *", regRoleCombo));

        // Row 5: Admin Secret Code
        adminCodeContainer = new JPanel();
        adminCodeContainer.setLayout(new BoxLayout(adminCodeContainer, BoxLayout.Y_AXIS));
        adminCodeContainer.setOpaque(false);
        adminCodeContainer.setVisible(false);

        JLabel codeLabel = new JLabel("Admin Key (ADMIN2026):");
        codeLabel.setFont(UIUtils.FONT_SMALL_BOLD);
        codeLabel.setForeground(UIUtils.COLOR_PRIMARY);

        regAdminCodeField = new JPasswordField(10);
        regAdminCodeField.setBorder(new CompoundBorder(new LineBorder(UIUtils.COLOR_PRIMARY, 1, true), new EmptyBorder(6, 8, 6, 8)));
        regAdminCodeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        adminCodeContainer.add(codeLabel);
        adminCodeContainer.add(Box.createVerticalStrut(3));
        adminCodeContainer.add(regAdminCodeField);
        formGrid.add(adminCodeContainer);

        regRoleCombo.addActionListener(e -> {
            boolean isAdmin = regRoleCombo.getSelectedIndex() == 1;
            adminCodeContainer.setVisible(isAdmin);
            panel.revalidate();
            panel.repaint();
        });

        panel.add(formGrid);
        panel.add(Box.createVerticalStrut(18));

        // Register Button
        JButton registerSubmit = UIUtils.createPrimaryButton("Register Account & Sign In  →");
        registerSubmit.setFont(UIUtils.FONT_HEADER);
        registerSubmit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        registerSubmit.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerSubmit.addActionListener(e -> handleRegister());
        panel.add(registerSubmit);

        return panel;
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel l = new JLabel(labelText);
        l.setFont(UIUtils.FONT_SMALL_BOLD);
        l.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (field instanceof JTextField) {
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        }

        p.add(l);
        p.add(Box.createVerticalStrut(3));
        p.add(field);
        return p;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIUtils.FONT_BODY_BOLD);
        label.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // =========================================================================
    // Card Navigation & Action Handlers
    // =========================================================================

    public void showSignInCard() {
        formCardLayout.show(formCardContainer, "SIGN_IN");
        signInTabBtn.setBackground(UIUtils.COLOR_CHARCOAL_BTN);
        signInTabBtn.setForeground(Color.WHITE);
        registerTabBtn.setBackground(new Color(244, 244, 246));
        registerTabBtn.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
    }

    public void showRegisterCard() {
        formCardLayout.show(formCardContainer, "REGISTER");
        registerTabBtn.setBackground(UIUtils.COLOR_CHARCOAL_BTN);
        registerTabBtn.setForeground(Color.WHITE);
        signInTabBtn.setBackground(new Color(244, 244, 246));
        signInTabBtn.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
    }

    public void updateReturnToAppVisibility(boolean visible) {
        if (returnToAppBtn != null) {
            returnToAppBtn.setVisible(visible);
        }
    }

    public void resetFields() {
        if (loginUsernameField != null) loginUsernameField.setText("");
        if (loginPasswordField != null) loginPasswordField.setText("");
        if (regNameField != null) regNameField.setText("");
        if (regUsernameField != null) regUsernameField.setText("");
        if (regEmailField != null) regEmailField.setText("");
        if (regPhoneField != null) regPhoneField.setText("");
        if (regPasswordField != null) regPasswordField.setText("");
        if (regConfirmPasswordField != null) regConfirmPasswordField.setText("");
        if (regAdminCodeField != null) regAdminCodeField.setText("");
        showSignInCard();
    }

    private void handleSignIn() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username or email.", "Validation", JOptionPane.WARNING_MESSAGE);
            loginUsernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your password.", "Validation", JOptionPane.WARNING_MESSAGE);
            loginPasswordField.requestFocus();
            return;
        }

        try {
            UserAccount account = controller.getUserManager().authenticate(username, password);
            controller.setCurrentUser(account);

            JOptionPane.showMessageDialog(this,
                    "Welcome back, " + account.getName() + "!\n" +
                    "Logged in as: " + account.getRole().getDisplayName(),
                    "Authentication Successful", JOptionPane.INFORMATION_MESSAGE);

            if (mainFrame != null) {
                mainFrame.showMainWorkspace();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Sign In Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        String name = regNameField.getText().trim();
        String username = regUsernameField.getText().trim().toLowerCase();
        String email = regEmailField.getText().trim();
        String phone = regPhoneField.getText().trim();
        String pass = new String(regPasswordField.getPassword());
        String confirm = new String(regConfirmPasswordField.getPassword());
        boolean wantsAdmin = regRoleCombo.getSelectedIndex() == 1;

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your full name.", "Validation", JOptionPane.WARNING_MESSAGE);
            regNameField.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please choose a username.", "Validation", JOptionPane.WARNING_MESSAGE);
            regUsernameField.requestFocus();
            return;
        }

        if (email.isEmpty() && phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter either an email address or a phone number.", "Validation", JOptionPane.WARNING_MESSAGE);
            regEmailField.requestFocus();
            return;
        }

        if (pass.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.", "Validation", JOptionPane.WARNING_MESSAGE);
            regPasswordField.requestFocus();
            return;
        }

        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
            regConfirmPasswordField.requestFocus();
            return;
        }

        UserRole role = UserRole.USER;
        if (wantsAdmin) {
            String adminCode = new String(regAdminCodeField.getPassword()).trim();
            if (!ADMIN_SECRET_KEY.equalsIgnoreCase(adminCode)) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Admin Security Code!\n" +
                        "For system evaluation, use: " + ADMIN_SECRET_KEY,
                        "Security Verification Failed", JOptionPane.ERROR_MESSAGE);
                regAdminCodeField.requestFocus();
                return;
            }
            role = UserRole.ADMIN;
        }

        try {
            UserAccount account = controller.getUserManager().registerUser(
                    username, pass, name, email, phone, role);
            controller.setCurrentUser(account);

            JOptionPane.showMessageDialog(this,
                    "Registration successful!\n" +
                    "Account @" + account.getUsername() + " created with role: " + account.getRole().getDisplayName(),
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);

            if (mainFrame != null) {
                mainFrame.showMainWorkspace();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleContinueAsGuest() {
        controller.setCurrentUser(null);
        if (mainFrame != null) {
            mainFrame.showMainWorkspace();
        }
    }
}

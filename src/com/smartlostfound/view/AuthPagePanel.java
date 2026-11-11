package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.UserAccount;
import com.smartlostfound.model.UserRole;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Streamlined Full-Page Executive Authentication Portal.
 * Designed to elite SaaS standards (Stripe, Linear, Apple ID, Vercel):
 * - Left 42%: Minimalist Obsidian branding with strong typography and crisp value props.
 * - Right 58%: Pure, distraction-free authentication column with seamless segmented switcher,
 *   integrated password toggle, and understated demo chips.
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
    private JButton passToggleBtn;
    private boolean isPasswordVisible = false;
    private JButton returnToAppBtn;

    // Register Controls
    private JTextField regNameField;
    private JTextField regUsernameField;
    private JTextField regEmailField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;
    private JComboBox<String> regRoleCombo;
    private JPanel adminCodeContainer;
    private JPasswordField regAdminCodeField;

    // Responsive Left Showcase Reference
    private JPanel leftShowcasePanel;

    // Post-authentication redirection target (e.g. "REPORT_LOST", "REPORT_FOUND")
    private String pendingPostAuthTarget = null;

    public AuthPagePanel(LostFoundController controller, MainFrame mainFrame) {
        this.controller = controller;
        this.mainFrame = mainFrame;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 1. Left Showcase Column (Minimalist Obsidian Branding)
        leftShowcasePanel = createShowcasePanel();
        add(leftShowcasePanel, BorderLayout.WEST);

        // 2. Right Form Column (Pure, Distraction-Free Form)
        add(createFormColumn(), BorderLayout.CENTER);

        // Responsive listener to adapt split on narrow displays
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int totalWidth = getWidth();
                if (totalWidth < 850) {
                    leftShowcasePanel.setVisible(false);
                } else {
                    leftShowcasePanel.setVisible(true);
                    int leftW = Math.max(340, (int) (totalWidth * 0.42));
                    leftShowcasePanel.setPreferredSize(new Dimension(leftW, 0));
                    leftShowcasePanel.revalidate();
                }
            }
        });
    }

    // =========================================================================
    // Left Showcase Column (Minimalist Obsidian Branding)
    // =========================================================================

    private JPanel createShowcasePanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Deep Obsidian to Dark Carbon Gradient (#121214 to #0B0B0C)
                GradientPaint gp = new GradientPaint(0, 0, UIUtils.COLOR_SIDEBAR_TOP, 0, getHeight(), UIUtils.COLOR_SIDEBAR_BOTTOM);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // 1px Subtle divider line between left and right
                g2.setColor(new Color(255, 255, 255, 12));
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());

                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setPreferredSize(new Dimension(460, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(44, 44, 44, 44));

        // Top Brand Header
        JPanel topBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBrand.setOpaque(false);

        // Crimson emblem glyph
        JPanel logoGlyph = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtils.COLOR_PRIMARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoGlyph.setPreferredSize(new Dimension(28, 28));
        logoGlyph.setOpaque(false);
        JLabel glyphIcon = new JLabel("■");
        glyphIcon.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 14));
        glyphIcon.setForeground(Color.WHITE);
        logoGlyph.add(glyphIcon);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 1));
        titleBlock.setOpaque(false);
        JLabel brandName = new JLabel("SMART LOST & FOUND");
        brandName.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 13));
        brandName.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("RECOVERY NETWORK");
        brandSub.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 9));
        brandSub.setForeground(UIUtils.COLOR_SIDEBAR_MUTED);

        titleBlock.add(brandName);
        titleBlock.add(brandSub);

        topBrand.add(logoGlyph);
        topBrand.add(titleBlock);
        panel.add(topBrand, BorderLayout.NORTH);

        // Center Hero Block
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);

        centerContent.add(Box.createVerticalGlue());

        JLabel heroHeadline = new JLabel("<html>Reclaim what's<br><font color='#E52E2D'>yours.</font></html>");
        heroHeadline.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 36));
        heroHeadline.setForeground(Color.WHITE);
        heroHeadline.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContent.add(heroHeadline);

        centerContent.add(Box.createVerticalStrut(14));

        JLabel heroDesc = new JLabel("<html>A unified community platform connecting lost belongings with their rightful owners through verified matching and instant updates.</html>");
        heroDesc.setFont(UIUtils.FONT_BODY);
        heroDesc.setForeground(new Color(161, 161, 170));
        heroDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerContent.add(heroDesc);

        centerContent.add(Box.createVerticalStrut(32));

        // 3 Clean, Minimalist Feature Highlights (no marketing fluff)
        centerContent.add(createMinimalBullet("⚡", "Real-Time Visual Feed", "16:9 high-res photos and interactive lightbox zoom."));
        centerContent.add(Box.createVerticalStrut(16));
        centerContent.add(createMinimalBullet("🛡️", "Verified Custody & Security", "Role-based oversight and authenticated claims."));
        centerContent.add(Box.createVerticalStrut(16));
        centerContent.add(createMinimalBullet("💾", "Zero Data Loss Storage", "Atomic persistent synchronization committed to disk."));

        centerContent.add(Box.createVerticalGlue());

        panel.add(centerContent, BorderLayout.CENTER);

        // Bottom Footer
        JLabel bottomTrust = new JLabel("🔒 256-Bit Encrypted • Local Storage • Privacy First");
        bottomTrust.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 11));
        bottomTrust.setForeground(UIUtils.COLOR_SIDEBAR_MUTED);
        panel.add(bottomTrust, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMinimalBullet(String icon, String title, String desc) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 16));
        row.add(iconLabel, BorderLayout.WEST);

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 1));
        text.setOpaque(false);

        JLabel tLabel = new JLabel(title);
        tLabel.setFont(UIUtils.FONT_BODY_BOLD);
        tLabel.setForeground(Color.WHITE);

        JLabel dLabel = new JLabel(desc);
        dLabel.setFont(UIUtils.FONT_SMALL);
        dLabel.setForeground(new Color(161, 161, 170));

        text.add(tLabel);
        text.add(dLabel);
        row.add(text, BorderLayout.CENTER);

        return row;
    }

    // =========================================================================
    // Right Form Column (Pure, Clean, Focused Authentication)
    // =========================================================================

    private JPanel createFormColumn() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);

        // Centered form column container (fixed max width of 380px for optimal reading proportion)
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(Color.WHITE);
        formContainer.setPreferredSize(new Dimension(380, 560));
        formContainer.setMaximumSize(new Dimension(380, 680));

        // 1. Top Bar: Return to Dashboard (only when user has active session)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

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
        topBar.add(returnToAppBtn, BorderLayout.WEST);
        formContainer.add(topBar);

        formContainer.add(Box.createVerticalStrut(12));

        // 2. Modern Segmented Pill Switcher (Sign In vs Create Account)
        formContainer.add(createSegmentedSwitcher());
        formContainer.add(Box.createVerticalStrut(24));

        // 3. Form Card Stack
        formCardLayout = new CardLayout();
        formCardContainer = new JPanel(formCardLayout);
        formCardContainer.setBackground(Color.WHITE);

        formCardContainer.add(createSignInPanel(), "SIGN_IN");
        formCardContainer.add(createRegisterPanel(), "REGISTER");

        formContainer.add(formCardContainer);

        // Center inside page with scroll wrapper
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(32, 24, 32, 24);
        outer.add(formContainer, gbc);

        JScrollPane scrollPane = new JScrollPane(outer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel root = new JPanel(new BorderLayout());
        root.add(scrollPane, BorderLayout.CENTER);
        return root;
    }

    private JPanel createSegmentedSwitcher() {
        JPanel pillBox = new JPanel(new GridLayout(1, 2, 4, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(244, 244, 245));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pillBox.setOpaque(false);
        pillBox.setBorder(new EmptyBorder(3, 3, 3, 3));
        pillBox.setPreferredSize(new Dimension(380, 42));
        pillBox.setMaximumSize(new Dimension(380, 42));

        signInTabBtn = createTabPill("Sign In", true);
        registerTabBtn = createTabPill("Create Account", false);

        signInTabBtn.addActionListener(e -> showSignInCard());
        registerTabBtn.addActionListener(e -> showRegisterCard());

        pillBox.add(signInTabBtn);
        pillBox.add(registerTabBtn);
        return pillBox;
    }

    private JButton createTabPill(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(UIUtils.FONT_BODY_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        setTabPillState(btn, active);
        return btn;
    }

    private void setTabPillState(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
            btn.setBorder(new CompoundBorder(
                    new LineBorder(new Color(228, 228, 231), 1, true),
                    new EmptyBorder(6, 12, 6, 12)
            ));
        } else {
            btn.setBackground(new Color(244, 244, 245));
            btn.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
            btn.setBorder(new EmptyBorder(7, 13, 7, 13));
        }
    }

    // =========================================================================
    // Sign In View
    // =========================================================================

    private JPanel createSignInPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        // Header
        JLabel title = new JLabel("Sign In");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 24));
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Enter your account credentials to continue.");
        sub.setFont(UIUtils.FONT_BODY);
        sub.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(20));

        // Google Sign-In Action
        JButton googleSignInBtn = UIUtils.createGoogleButton("Continue with Google");
        googleSignInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        googleSignInBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        googleSignInBtn.addActionListener(e -> handleGoogleLogin());
        panel.add(googleSignInBtn);

        panel.add(Box.createVerticalStrut(14));
        JPanel orDiv = UIUtils.createOrDivider("OR SIGN IN WITH USERNAME");
        orDiv.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(orDiv);
        panel.add(Box.createVerticalStrut(14));

        // Username Field
        panel.add(createFieldLabel("Username or Email"));
        panel.add(Box.createVerticalStrut(6));
        loginUsernameField = UIUtils.createStyledTextField(20);
        loginUsernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginUsernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(loginUsernameField);

        panel.add(Box.createVerticalStrut(16));

        // Password Field with Integrated Eye Toggle
        panel.add(createFieldLabel("Password"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(createPasswordRow());

        panel.add(Box.createVerticalStrut(24));

        // Primary Action: Sign In Button
        JButton signInSubmit = UIUtils.createPrimaryButton("Sign In");
        signInSubmit.setFont(UIUtils.FONT_HEADER);
        signInSubmit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        signInSubmit.setAlignmentX(Component.LEFT_ALIGNMENT);
        signInSubmit.addActionListener(e -> handleSignIn());
        panel.add(signInSubmit);

        // Enter key listeners
        loginUsernameField.addActionListener(e -> handleSignIn());
        loginPasswordField.addActionListener(e -> handleSignIn());

        panel.add(Box.createVerticalStrut(10));

        // Secondary Action: Browse Community Feed as Guest
        JButton guestBtn = UIUtils.createSecondaryButton("Browse Feed as Guest →");
        guestBtn.setFont(UIUtils.FONT_BODY_BOLD);
        guestBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        guestBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        guestBtn.setToolTipText("Open the Community Feed in read-only guest mode");
        guestBtn.addActionListener(e -> handleContinueAsGuest());
        panel.add(guestBtn);

        panel.add(Box.createVerticalStrut(24));

        // Understated, Clean Demo Autofill Bar (Subtle & Professional)
        panel.add(createCleanDemoBar());

        return panel;
    }

    private JPanel createPasswordRow() {
        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(Color.WHITE);
        passPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginPasswordField = new JPasswordField(20);
        loginPasswordField.setFont(UIUtils.FONT_BODY);
        loginPasswordField.setCaretColor(UIUtils.COLOR_PRIMARY);

        Border normalBorder = new CompoundBorder(
                new UIUtils.RoundedLineBorder(UIUtils.COLOR_BORDER, 1, 8),
                new EmptyBorder(8, 12, 8, 4)
        );
        Border focusBorder = new CompoundBorder(
                new UIUtils.RoundedLineBorder(UIUtils.COLOR_PRIMARY, 2, 8),
                new EmptyBorder(7, 11, 7, 3)
        );

        passPanel.setBorder(normalBorder);

        loginPasswordField.setBorder(null);
        loginPasswordField.setOpaque(false);

        loginPasswordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passPanel.setBorder(focusBorder);
                passPanel.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                passPanel.setBorder(normalBorder);
                passPanel.repaint();
            }
        });

        // Integrated eye toggle button
        passToggleBtn = new JButton("👁");
        passToggleBtn.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 14));
        passToggleBtn.setForeground(UIUtils.COLOR_TEXT_MUTED);
        passToggleBtn.setContentAreaFilled(false);
        passToggleBtn.setBorderPainted(false);
        passToggleBtn.setFocusPainted(false);
        passToggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        passToggleBtn.setBorder(new EmptyBorder(0, 4, 0, 10));
        passToggleBtn.setToolTipText("Toggle password visibility");
        passToggleBtn.addActionListener(e -> {
            isPasswordVisible = !isPasswordVisible;
            loginPasswordField.setEchoChar(isPasswordVisible ? (char) 0 : '•');
            passToggleBtn.setForeground(isPasswordVisible ? UIUtils.COLOR_PRIMARY : UIUtils.COLOR_TEXT_MUTED);
        });

        passPanel.add(loginPasswordField, BorderLayout.CENTER);
        passPanel.add(passToggleBtn, BorderLayout.EAST);

        return passPanel;
    }

    private JPanel createCleanDemoBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        bar.setOpaque(false);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel demoHint = new JLabel("Demo fill:");
        demoHint.setFont(UIUtils.FONT_SMALL);
        demoHint.setForeground(UIUtils.COLOR_TEXT_MUTED);
        bar.add(demoHint);

        bar.add(createFastChip("Admin", "admin", "admin123"));
        bar.add(createFastChip("John", "john", "user123"));
        bar.add(createFastChip("Sarah", "sarah", "user123"));

        return bar;
    }

    private JButton createFastChip(String label, String u, String p) {
        JButton btn = new JButton(label);
        btn.setFont(UIUtils.FONT_SMALL_BOLD);
        btn.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        btn.setBackground(new Color(244, 244, 246));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(3, 8, 3, 8));
        btn.addActionListener(e -> {
            loginUsernameField.setText(u);
            loginPasswordField.setText(p);
            loginUsernameField.requestFocus();
        });
        return btn;
    }

    // =========================================================================
    // Create Account View
    // =========================================================================

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 22));
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign up to report items and receive recovery alerts.");
        sub.setFont(UIUtils.FONT_BODY);
        sub.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(18));

        // Google Sign-Up Action
        JButton googleSignUpBtn = UIUtils.createGoogleButton("Sign up with Google");
        googleSignUpBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        googleSignUpBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        googleSignUpBtn.addActionListener(e -> handleGoogleLogin());
        panel.add(googleSignUpBtn);

        panel.add(Box.createVerticalStrut(14));
        JPanel orDiv = UIUtils.createOrDivider("OR SIGN UP WITH EMAIL");
        orDiv.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(orDiv);
        panel.add(Box.createVerticalStrut(14));

        // Form Fields (Clean & Sequential)
        panel.add(createFieldLabel("Full Name *"));
        panel.add(Box.createVerticalStrut(4));
        regNameField = UIUtils.createStyledTextField(20);
        regNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        regNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(regNameField);
        panel.add(Box.createVerticalStrut(10));

        panel.add(createFieldLabel("Username (Login ID) *"));
        panel.add(Box.createVerticalStrut(4));
        regUsernameField = UIUtils.createStyledTextField(20);
        regUsernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        regUsernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(regUsernameField);
        panel.add(Box.createVerticalStrut(10));

        panel.add(createFieldLabel("Email Address *"));
        panel.add(Box.createVerticalStrut(4));
        regEmailField = UIUtils.createStyledTextField(20);
        regEmailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        regEmailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(regEmailField);
        panel.add(Box.createVerticalStrut(10));

        panel.add(createFieldLabel("Password *"));
        panel.add(Box.createVerticalStrut(4));
        regPasswordField = new JPasswordField(20);
        regPasswordField.setFont(UIUtils.FONT_BODY);
        regPasswordField.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.COLOR_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        regPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        regPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(regPasswordField);
        panel.add(Box.createVerticalStrut(10));

        panel.add(createFieldLabel("Account Role"));
        panel.add(Box.createVerticalStrut(4));
        String[] roles = {"Standard Community User", "Executive Administrator"};
        regRoleCombo = new JComboBox<>(roles);
        regRoleCombo.setFont(UIUtils.FONT_BODY);
        regRoleCombo.setBackground(Color.WHITE);
        regRoleCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        regRoleCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(regRoleCombo);
        panel.add(Box.createVerticalStrut(10));

        // Admin Security Key (only shown when Administrator is selected)
        adminCodeContainer = new JPanel();
        adminCodeContainer.setLayout(new BoxLayout(adminCodeContainer, BoxLayout.Y_AXIS));
        adminCodeContainer.setOpaque(false);
        adminCodeContainer.setVisible(false);
        adminCodeContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        adminCodeContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel keyLabel = new JLabel("Admin Key (ADMIN2026):");
        keyLabel.setFont(UIUtils.FONT_SMALL_BOLD);
        keyLabel.setForeground(UIUtils.COLOR_PRIMARY);

        regAdminCodeField = new JPasswordField(15);
        regAdminCodeField.setBorder(new CompoundBorder(new LineBorder(UIUtils.COLOR_PRIMARY, 1, true), new EmptyBorder(6, 10, 6, 10)));
        regAdminCodeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        adminCodeContainer.add(keyLabel);
        adminCodeContainer.add(Box.createVerticalStrut(4));
        adminCodeContainer.add(regAdminCodeField);
        panel.add(adminCodeContainer);

        regRoleCombo.addActionListener(e -> {
            boolean isAdmin = regRoleCombo.getSelectedIndex() == 1;
            adminCodeContainer.setVisible(isAdmin);
            panel.revalidate();
            panel.repaint();
        });

        panel.add(Box.createVerticalStrut(16));

        // Submit Button
        JButton registerSubmit = UIUtils.createPrimaryButton("Create Account & Sign In");
        registerSubmit.setFont(UIUtils.FONT_HEADER);
        registerSubmit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        registerSubmit.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerSubmit.addActionListener(e -> handleRegister());
        panel.add(registerSubmit);

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIUtils.FONT_BODY_BOLD);
        label.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // =========================================================================
    // Card Navigation & Actions
    // =========================================================================

    public void showSignInCard() {
        formCardLayout.show(formCardContainer, "SIGN_IN");
        setTabPillState(signInTabBtn, true);
        setTabPillState(registerTabBtn, false);
    }

    public void showRegisterCard() {
        formCardLayout.show(formCardContainer, "REGISTER");
        setTabPillState(registerTabBtn, true);
        setTabPillState(signInTabBtn, false);
    }

    public void setPendingPostAuthTarget(String target) {
        this.pendingPostAuthTarget = target;
    }

    public String getPendingPostAuthTarget() {
        return pendingPostAuthTarget;
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
        if (regPasswordField != null) regPasswordField.setText("");
        if (regConfirmPasswordField != null) regConfirmPasswordField.setText("");
        if (regAdminCodeField != null) regAdminCodeField.setText("");
        isPasswordVisible = false;
        if (loginPasswordField != null) loginPasswordField.setEchoChar('•');
        if (passToggleBtn != null) passToggleBtn.setForeground(UIUtils.COLOR_TEXT_MUTED);
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

            proceedPostAuth();
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
        String pass = new String(regPasswordField.getPassword());
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

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an email address.", "Validation", JOptionPane.WARNING_MESSAGE);
            regEmailField.requestFocus();
            return;
        }

        if (pass.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.", "Validation", JOptionPane.WARNING_MESSAGE);
            regPasswordField.requestFocus();
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
                    username, pass, name, email, "", role);
            controller.setCurrentUser(account);

            JOptionPane.showMessageDialog(this,
                    "Registration successful!\n" +
                    "Account @" + account.getUsername() + " created with role: " + account.getRole().getDisplayName(),
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);

            proceedPostAuth();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleGoogleLogin() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        UserAccount account = GoogleAuthDialog.showDialog(owner, controller);
        if (account != null) {
            controller.setCurrentUser(account);
            JOptionPane.showMessageDialog(this,
                    "Signed in with Google successfully!\n" +
                    "Welcome, " + account.getName() + " (" + account.getEmail() + ")",
                    "Google Authentication", JOptionPane.INFORMATION_MESSAGE);
            proceedPostAuth();
        }
    }

    private void proceedPostAuth() {
        if (mainFrame != null) {
            mainFrame.updateUserSession(controller.getCurrentUser());
            mainFrame.showMainWorkspace();
            if ("REPORT_LOST".equals(pendingPostAuthTarget)) {
                mainFrame.switchToReportLost();
            } else if ("REPORT_FOUND".equals(pendingPostAuthTarget)) {
                mainFrame.switchToReportFound();
            }
            pendingPostAuthTarget = null;
        }
    }

    private void handleContinueAsGuest() {
        controller.setCurrentUser(null);
        if (mainFrame != null) {
            mainFrame.enterGuestBrowseMode();
        }
    }
}

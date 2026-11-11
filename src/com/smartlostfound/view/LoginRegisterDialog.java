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
 * High-end modal authentication gateway for login and registration.
 * Follows the DesignRush Best Designs aesthetic: Carbon-black gradient branding sidebar,
 * signature red action buttons, and segmented navigation tabs.
 */
public class LoginRegisterDialog extends JDialog {

    private static final String ADMIN_SECRET_KEY = "ADMIN2026";

    private final LostFoundController controller;
    private UserAccount authenticatedAccount = null;
    private boolean initialLaunch = false;
    private boolean enteredApp = false;

    private CardLayout authCardLayout;
    private JPanel authCardsContainer;
    private JButton signInTabBtn;
    private JButton registerTabBtn;

    // Sign In Fields
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    private JCheckBox loginShowPassCheck;

    // Register Fields
    private JTextField regNameField;
    private JTextField regUsernameField;
    private JTextField regEmailField;
    private JTextField regPhoneField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;
    private JComboBox<String> regRoleCombo;
    private JPanel adminCodeContainer;
    private JPasswordField regAdminCodeField;

    public LoginRegisterDialog(Window parent, LostFoundController controller) {
        super(parent, "Account Authentication - Smart Lost & Found", ModalityType.APPLICATION_MODAL);
        this.controller = controller;

        initComponents();
        GraphicsConfiguration gc = getGraphicsConfiguration();
        Rectangle screen = gc != null ? gc.getBounds() : new Rectangle(0, 0, 1280, 800);
        int dialogW = Math.min(780, (int) (screen.width * 0.95));
        int dialogH = Math.min(640, (int) (screen.height * 0.92));
        setSize(dialogW, dialogH);
        setMinimumSize(new Dimension(Math.min(680, screen.width - 40), Math.min(480, screen.height - 40)));
        setResizable(true);
        setLocationRelativeTo(parent);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (initialLaunch && !enteredApp) {
                    System.exit(0);
                }
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 1. Left Branding Panel (Carbon Gradient)
        add(createBrandingPanel(), BorderLayout.WEST);

        // 2. Right Form Panel (White)
        add(createFormPanel(), BorderLayout.CENTER);
    }

    private JPanel createBrandingPanel() {
        JPanel brandPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UIUtils.COLOR_SIDEBAR_TOP, 0, getHeight(), UIUtils.COLOR_SIDEBAR_BOTTOM);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        brandPanel.setPreferredSize(new Dimension(290, 0));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(32, 24, 32, 24));

        // Top Brand Header
        JPanel top = new JPanel(new GridLayout(4, 1, 0, 4));
        top.setOpaque(false);

        JLabel logo = new JLabel("■");
        logo.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 26));
        logo.setForeground(UIUtils.COLOR_PRIMARY);

        JLabel title = new JLabel("LOST & FOUND");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("DESIGNRUSH RECOVERY HUB");
        subtitle.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 10));
        subtitle.setForeground(UIUtils.COLOR_PRIMARY);

        JLabel tagline = new JLabel("Enterprise Property Intelligence");
        tagline.setFont(UIUtils.FONT_SMALL);
        tagline.setForeground(UIUtils.COLOR_SIDEBAR_TEXT);

        top.add(logo);
        top.add(title);
        top.add(subtitle);
        top.add(tagline);
        brandPanel.add(top, BorderLayout.NORTH);

        // Center Perks
        JPanel centerPerks = new JPanel();
        centerPerks.setLayout(new BoxLayout(centerPerks, BoxLayout.Y_AXIS));
        centerPerks.setOpaque(false);
        centerPerks.setBorder(new EmptyBorder(32, 0, 0, 0));

        centerPerks.add(createPerkItem("🛡️ Role-Based Access Control", "Distinct privileges for Users and Administrators."));
        centerPerks.add(Box.createVerticalStrut(16));
        centerPerks.add(createPerkItem("📰 Social Activity Feed", "Live community stream for sightings & rewards."));
        centerPerks.add(Box.createVerticalStrut(16));
        centerPerks.add(createPerkItem("🔒 Secure Credentials", "Encrypted SHA-256 storage and session guards."));

        brandPanel.add(centerPerks, BorderLayout.CENTER);

        // Bottom Footer
        JLabel footer = new JLabel("Production Tier v2.2 • MVC");
        footer.setFont(UIUtils.FONT_SMALL);
        footer.setForeground(UIUtils.COLOR_SIDEBAR_TEXT);
        brandPanel.add(footer, BorderLayout.SOUTH);

        return brandPanel;
    }

    private JPanel createPerkItem(String title, String desc) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(UIUtils.FONT_BODY_BOLD);
        t.setForeground(Color.WHITE);

        JLabel d = new JLabel("<html>" + desc + "</html>");
        d.setFont(UIUtils.FONT_SMALL);
        d.setForeground(UIUtils.COLOR_SIDEBAR_TEXT);

        p.add(t);
        p.add(d);
        return p;
    }

    private JPanel createFormPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Top Segmented Switcher
        JPanel topSwitcher = new JPanel(new GridLayout(1, 2, 8, 0));
        topSwitcher.setOpaque(false);
        topSwitcher.setPreferredSize(new Dimension(0, 42));

        signInTabBtn = UIUtils.createModernButton("Sign In", UIUtils.COLOR_CHARCOAL_BTN,
                UIUtils.COLOR_CHARCOAL_BTN, Color.WHITE, 8);
        registerTabBtn = UIUtils.createModernButton("Create Account", new Color(244, 244, 246),
                new Color(228, 228, 232), UIUtils.COLOR_TEXT_PRIMARY, 8);

        signInTabBtn.addActionListener(e -> showSignInCard());
        registerTabBtn.addActionListener(e -> showRegisterCard());

        topSwitcher.add(signInTabBtn);
        topSwitcher.add(registerTabBtn);
        container.add(topSwitcher, BorderLayout.NORTH);

        // Center Cards inside ScrollPane for compact/small laptop displays
        authCardLayout = new CardLayout();
        authCardsContainer = new JPanel(authCardLayout);
        authCardsContainer.setBackground(Color.WHITE);

        authCardsContainer.add(createSignInPanel(), "SIGN_IN");
        authCardsContainer.add(createRegisterPanel(), "REGISTER");

        JScrollPane authScrollPane = new JScrollPane(authCardsContainer);
        authScrollPane.setBorder(null);
        authScrollPane.getVerticalScrollBar().setUnitIncrement(14);
        authScrollPane.getViewport().setBackground(Color.WHITE);

        container.add(authScrollPane, BorderLayout.CENTER);

        return container;
    }

    private JPanel createSignInPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 4, 10, 4));

        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 20));
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in with your username and password to proceed.");
        sub.setFont(UIUtils.FONT_BODY);
        sub.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(16));

        // Google Sign-In Action
        JButton googleBtn = UIUtils.createGoogleButton("Continue with Google");
        googleBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        googleBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        googleBtn.addActionListener(e -> handleGoogleLogin());
        panel.add(googleBtn);

        panel.add(Box.createVerticalStrut(12));
        JPanel orDiv = UIUtils.createOrDivider("OR SIGN IN WITH USERNAME");
        orDiv.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(orDiv);
        panel.add(Box.createVerticalStrut(12));

        // Username
        panel.add(createFormLabel("Username:"));
        panel.add(Box.createVerticalStrut(4));
        loginUsernameField = UIUtils.createStyledTextField(20);
        loginUsernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginUsernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(loginUsernameField);
        panel.add(Box.createVerticalStrut(12));

        // Password
        panel.add(createFormLabel("Password:"));
        panel.add(Box.createVerticalStrut(4));
        loginPasswordField = new JPasswordField(20);
        loginPasswordField.setFont(UIUtils.FONT_BODY);
        loginPasswordField.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.COLOR_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        loginPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
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
        panel.add(Box.createVerticalStrut(16));

        // One-Click Demo Credentials
        panel.add(createDemoQuickFillSection());
        panel.add(Box.createVerticalStrut(18));

        // Sign In Button
        JButton signInSubmit = UIUtils.createPrimaryButton("Sign In to Recovery Hub");
        signInSubmit.setFont(UIUtils.FONT_HEADER);
        signInSubmit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        signInSubmit.setAlignmentX(Component.LEFT_ALIGNMENT);
        signInSubmit.addActionListener(e -> handleSignIn());
        panel.add(signInSubmit);

        // Enter key shortcut
        loginUsernameField.addActionListener(e -> handleSignIn());
        loginPasswordField.addActionListener(e -> handleSignIn());

        panel.add(Box.createVerticalStrut(10));

        String guestText = (controller.getCurrentUser() != null)
                ? "🔒 Sign Out & Continue as Guest"
                : "👀 Continue as Guest (Browse Only)";
        JButton guestBtn = UIUtils.createSecondaryButton(guestText);
        guestBtn.setFont(UIUtils.FONT_BODY_BOLD);
        guestBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        guestBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        guestBtn.setToolTipText("Explore feed and dashboard without logging in");
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
        demoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        demoBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel("⚡ Quick Demo Access (1-Click Fill):");
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
        panel.setBorder(new EmptyBorder(14, 4, 10, 4));

        JLabel title = new JLabel("Create a New Account");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 18));
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Register to report property, track claims, and receive match alerts.");
        sub.setFont(UIUtils.FONT_SMALL);
        sub.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(2));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(12));

        // Google Sign-Up Action
        JButton googleRegBtn = UIUtils.createGoogleButton("Sign up with Google");
        googleRegBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        googleRegBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        googleRegBtn.addActionListener(e -> handleGoogleLogin());
        panel.add(googleRegBtn);

        panel.add(Box.createVerticalStrut(10));
        JPanel orRegDiv = UIUtils.createOrDivider("OR SIGN UP WITH EMAIL");
        orRegDiv.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(orRegDiv);
        panel.add(Box.createVerticalStrut(10));

        // Form Fields in 2 columns
        JPanel formGrid = new JPanel(new GridLayout(5, 2, 10, 8));
        formGrid.setOpaque(false);
        formGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
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

        // Row 4: Account Type
        regRoleCombo = new JComboBox<>(new String[]{"Standard User (Community Member)", "System Administrator (Admin Privileges)"});
        regRoleCombo.setFont(UIUtils.FONT_BODY);

        formGrid.add(createLabeledField("Account Role *", regRoleCombo));

        // Row 5: Admin Secret Code
        regAdminCodeField = new JPasswordField(15);
        regAdminCodeField.setBorder(new CompoundBorder(new LineBorder(UIUtils.COLOR_BORDER, 1, true), new EmptyBorder(6, 10, 6, 10)));
        adminCodeContainer = createLabeledField("Admin Security Code (Use: ADMIN2026)", regAdminCodeField);
        adminCodeContainer.setVisible(false);

        formGrid.add(adminCodeContainer);

        regRoleCombo.addActionListener(e -> {
            boolean isAdmin = regRoleCombo.getSelectedIndex() == 1;
            adminCodeContainer.setVisible(isAdmin);
            panel.revalidate();
            panel.repaint();
        });

        panel.add(formGrid);
        panel.add(Box.createVerticalStrut(14));

        // Register Button
        JButton registerSubmit = UIUtils.createPrimaryButton("Register Account & Sign In");
        registerSubmit.setFont(UIUtils.FONT_HEADER);
        registerSubmit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
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
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
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

    private void showSignInCard() {
        authCardLayout.show(authCardsContainer, "SIGN_IN");
        signInTabBtn.setBackground(UIUtils.COLOR_CHARCOAL_BTN);
        signInTabBtn.setForeground(Color.WHITE);
        registerTabBtn.setBackground(new Color(244, 244, 246));
        registerTabBtn.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
    }

    private void showRegisterCard() {
        authCardLayout.show(authCardsContainer, "REGISTER");
        registerTabBtn.setBackground(UIUtils.COLOR_CHARCOAL_BTN);
        registerTabBtn.setForeground(Color.WHITE);
        signInTabBtn.setBackground(new Color(244, 244, 246));
        signInTabBtn.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
    }

    private void handleSignIn() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username.", "Validation", JOptionPane.WARNING_MESSAGE);
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
            this.enteredApp = true;
            this.authenticatedAccount = account;
            controller.setCurrentUser(account);

            JOptionPane.showMessageDialog(this,
                    "Welcome back, " + account.getName() + "!\n" +
                    "Logged in as: " + account.getRole().getDisplayName(),
                    "Authentication Successful", JOptionPane.INFORMATION_MESSAGE);

            dispose();
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
            this.enteredApp = true;
            this.authenticatedAccount = account;
            controller.setCurrentUser(account);

            JOptionPane.showMessageDialog(this,
                    "Registration successful!\n" +
                    "Account @" + account.getUsername() + " created with role: " + account.getRole().getDisplayName(),
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);

            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleContinueAsGuest() {
        this.enteredApp = true;
        this.authenticatedAccount = null;
        controller.setCurrentUser(null);
        dispose();
    }

    private void handleGoogleLogin() {
        UserAccount account = GoogleAuthDialog.showDialog(this, controller);
        if (account != null) {
            this.enteredApp = true;
            this.authenticatedAccount = account;
            controller.setCurrentUser(account);
            JOptionPane.showMessageDialog(this,
                    "Signed in with Google successfully!\n" +
                    "Welcome, " + account.getName() + " (" + account.getEmail() + ")",
                    "Google Authentication", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    public void setInitialLaunch(boolean initialLaunch) {
        this.initialLaunch = initialLaunch;
    }

    public boolean hasEnteredApp() {
        return enteredApp;
    }

    public UserAccount getAuthenticatedAccount() {
        return authenticatedAccount;
    }
}

package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.UserAccount;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Authentic Google Identity Account Chooser Dialog.
 * Follows Google Material Design specifications:
 * - Pure white background (#FFFFFF) with Google standard typography and 4-color 'G' icon.
 * - One-click account selector (Alex Rivera, John Doe, Sarah Jenkins).
 * - "Use another account" expandable input for custom Gmail / Google Workspace profiles.
 */
public class GoogleAuthDialog extends JDialog {

    private final LostFoundController controller;
    private UserAccount authenticatedAccount = null;

    private JPanel customAccountPanel;
    private JTextField customEmailField;
    private JTextField customNameField;
    private boolean isCustomExpanded = false;

    public GoogleAuthDialog(Window owner, LostFoundController controller) {
        super(owner, "Sign in with Google", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(32, 36, 28, 36));

        // 1. Google Brand Header (Icon + Title)
        JPanel brandHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        brandHeader.setOpaque(false);
        JLabel gIcon = new JLabel(new UIUtils.GoogleLogoIcon(32));
        brandHeader.add(gIcon);
        brandHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(brandHeader);

        root.add(Box.createVerticalStrut(16));

        JLabel title = new JLabel("Choose an account");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 20));
        title.setForeground(new Color(32, 33, 36)); // Google dark #202124
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(title);

        root.add(Box.createVerticalStrut(4));

        JLabel subtitle = new JLabel("to continue to Smart Lost & Found");
        subtitle.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 13));
        subtitle.setForeground(new Color(95, 99, 104)); // Google secondary #5F6368
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(subtitle);

        root.add(Box.createVerticalStrut(24));

        // 2. Account Chooser List Container
        JPanel accountList = new JPanel();
        accountList.setLayout(new BoxLayout(accountList, BoxLayout.Y_AXIS));
        accountList.setBackground(Color.WHITE);
        accountList.setBorder(new LineBorder(new Color(228, 228, 231), 1, true));
        accountList.setAlignmentX(Component.CENTER_ALIGNMENT);
        accountList.setMaximumSize(new Dimension(380, 400));

        // Preset Google Profile 1: Alex Rivera
        accountList.add(createAccountRow(
                "Alex Rivera",
                "alex.rivera.dev@gmail.com",
                new Color(66, 133, 244), // Google Blue
                "AR"
        ));
        accountList.add(createDividerLine());

        // Preset Google Profile 2: John Doe
        accountList.add(createAccountRow(
                "John Doe",
                "john.doe.lostfound@gmail.com",
                new Color(234, 67, 53), // Google Red
                "JD"
        ));
        accountList.add(createDividerLine());

        // Preset Google Profile 3: Sarah Jenkins
        accountList.add(createAccountRow(
                "Sarah Jenkins",
                "sarah.jenkins.community@gmail.com",
                new Color(52, 168, 83), // Google Green
                "SJ"
        ));
        accountList.add(createDividerLine());

        // Option 4: "Use another account"
        accountList.add(createUseAnotherAccountRow());

        root.add(accountList);

        // 3. Expandable Custom Account Panel (Initially hidden)
        customAccountPanel = createCustomAccountPanel();
        customAccountPanel.setVisible(false);
        customAccountPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        customAccountPanel.setMaximumSize(new Dimension(380, 160));
        root.add(customAccountPanel);

        root.add(Box.createVerticalStrut(20));

        // 4. Google Disclaimer & Privacy Footer
        JLabel disclaimer = new JLabel("<html><center style='color:#70757a; font-size:10px; line-height:1.4;'>"
                + "To continue, Google will share your name, email address, and language preference with Smart Lost & Found."
                + "</center></html>");
        disclaimer.setAlignmentX(Component.CENTER_ALIGNMENT);
        disclaimer.setMaximumSize(new Dimension(380, 40));
        root.add(disclaimer);

        root.add(Box.createVerticalStrut(16));

        // Cancel Button
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 12));
        cancelBtn.setForeground(new Color(95, 99, 104));
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelBtn.addActionListener(e -> dispose());
        root.add(cancelBtn);

        add(root, BorderLayout.CENTER);

        pack();
        setSize(450, 520);
        setLocationRelativeTo(getOwner());
    }

    private JPanel createAccountRow(String name, String email, Color avatarColor, String initials) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(12, 16, 12, 16));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        // Circular Avatar with Initials
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(avatarColor);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(initials)) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initials, tx, ty);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(34, 34));
        avatar.setOpaque(false);

        // Account Details (Name & Email)
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 13));
        nameLabel.setForeground(new Color(32, 33, 36));

        JLabel emailLabel = new JLabel(email);
        emailLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 11));
        emailLabel.setForeground(new Color(95, 99, 104));

        textPanel.add(nameLabel);
        textPanel.add(emailLabel);

        row.add(avatar, BorderLayout.WEST);
        row.add(textPanel, BorderLayout.CENTER);

        // Hover & Click Listeners
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackground(new Color(248, 249, 250));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                row.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                selectGoogleAccount(email, name);
            }
        });

        return row;
    }

    private JPanel createUseAnotherAccountRow() {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(12, 16, 12, 16));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Icon for another account (User silhouette circle)
        JLabel plusIcon = new JLabel("＋");
        plusIcon.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 18));
        plusIcon.setForeground(new Color(95, 99, 104));
        plusIcon.setPreferredSize(new Dimension(34, 34));
        plusIcon.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel label = new JLabel("Use another account");
        label.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 13));
        label.setForeground(new Color(32, 33, 36));

        row.add(plusIcon, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);

        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackground(new Color(248, 249, 250));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                row.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                isCustomExpanded = !isCustomExpanded;
                customAccountPanel.setVisible(isCustomExpanded);
                if (isCustomExpanded) {
                    setSize(450, 640);
                    customEmailField.requestFocus();
                } else {
                    setSize(450, 520);
                }
                revalidate();
                repaint();
            }
        });

        return row;
    }

    private JPanel createCustomAccountPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(250, 250, 252));
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(228, 228, 231), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel h = new JLabel("Enter your Google Account:");
        h.setFont(UIUtils.FONT_SMALL_BOLD);
        h.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        panel.add(h);

        panel.add(Box.createVerticalStrut(8));

        customNameField = UIUtils.createStyledTextField(18);
        customNameField.setToolTipText("Full Name");
        customNameField.setText("Google User");
        customNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        panel.add(customNameField);

        panel.add(Box.createVerticalStrut(6));

        customEmailField = UIUtils.createStyledTextField(18);
        customEmailField.setToolTipText("Google Email (@gmail.com)");
        customEmailField.setText("");
        customEmailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        panel.add(customEmailField);

        panel.add(Box.createVerticalStrut(10));

        JButton connectBtn = UIUtils.createGoogleButton("Continue as Google User");
        connectBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        connectBtn.addActionListener(e -> {
            String email = customEmailField.getText().trim();
            String name = customNameField.getText().trim();
            if (email.isEmpty() || !email.contains("@")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid Google email address.", "Google Authentication", JOptionPane.WARNING_MESSAGE);
                customEmailField.requestFocus();
                return;
            }
            selectGoogleAccount(email, name.isEmpty() ? email.split("@")[0] : name);
        });
        panel.add(connectBtn);

        return panel;
    }

    private JSeparator createDividerLine() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(241, 243, 244));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private void selectGoogleAccount(String email, String name) {
        try {
            UserAccount account = controller.getUserManager().authenticateOrRegisterGoogle(email, name);
            this.authenticatedAccount = account;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to authenticate with Google:\n" + ex.getMessage(),
                    "Google Sign-In Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public UserAccount getAuthenticatedAccount() {
        return authenticatedAccount;
    }

    /**
     * Static helper to prompt Google Sign-In and return the authenticated UserAccount.
     */
    public static UserAccount showDialog(Window owner, LostFoundController controller) {
        GoogleAuthDialog dialog = new GoogleAuthDialog(owner, controller);
        dialog.setVisible(true);
        return dialog.getAuthenticatedAccount();
    }
}

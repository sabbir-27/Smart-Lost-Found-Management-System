package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.UserAccount;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Dedicated Authentication Intercept Dialog for Guest Users attempting to Report Lost or Found items.
 * Fulfills requirement: "guest user when create report lost and report found then need sign up, sign up also google login".
 *
 * Provides:
 * 1. Immediate 1-Click "Continue with Google" sign-up/login.
 * 2. Dedicated "Create Account (Sign Up)" pathway.
 * 3. Fallback "Already have an account? Sign In" link.
 * 4. Automatic post-auth navigation to the requested report form.
 */
public class GuestReportAuthDialog extends JDialog {

    private final LostFoundController controller;
    private final MainFrame mainFrame;
    private final String reportType;       // "Lost" or "Found"
    private final String targetNavKey;     // "REPORT_LOST" or "REPORT_FOUND"

    public GuestReportAuthDialog(MainFrame mainFrame, LostFoundController controller,
                                 String reportType, String targetNavKey) {
        super(mainFrame, "Sign Up Required", ModalityType.APPLICATION_MODAL);
        this.mainFrame = mainFrame;
        this.controller = controller;
        this.reportType = reportType;
        this.targetNavKey = targetNavKey;

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

        // 1. Accent Icon Badge (Red for Lost, Blue for Found)
        JPanel iconBadge = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(254, 242, 242));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(new Color(254, 202, 202));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBadge.setPreferredSize(new Dimension(52, 52));
        iconBadge.setMaximumSize(new Dimension(52, 52));
        iconBadge.setOpaque(false);
        iconBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel iconLabel = new JLabel("Lost".equalsIgnoreCase(reportType) ? "▲" : "▼");
        iconLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 22));
        iconLabel.setForeground(UIUtils.COLOR_PRIMARY);
        iconBadge.add(iconLabel);
        root.add(iconBadge);

        root.add(Box.createVerticalStrut(18));

        // 2. Headings
        JLabel title = new JLabel("Sign Up Required to Report " + reportType);
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 20));
        title.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(title);

        root.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel("<html><center style='color:#71717A; font-size:12px; line-height:1.5;'>"
                + "You are currently browsing as a guest.<br>"
                + "Please sign up to submit reports so you can track recovery progress, "
                + "communicate with finders, and claim your items safely."
                + "</center></html>");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setMaximumSize(new Dimension(380, 56));
        root.add(subtitle);

        root.add(Box.createVerticalStrut(26));

        // 3. Prominent Google Login Action
        JButton googleBtn = UIUtils.createGoogleButton("Sign up with Google");
        googleBtn.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 14));
        googleBtn.setMaximumSize(new Dimension(380, 46));
        googleBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        googleBtn.addActionListener(e -> handleGoogleAuth());
        root.add(googleBtn);

        root.add(Box.createVerticalStrut(16));

        // 4. Subtle "OR SIGN UP WITH EMAIL" Divider
        JPanel divider = UIUtils.createOrDivider("OR SIGN UP WITH EMAIL");
        divider.setAlignmentX(Component.CENTER_ALIGNMENT);
        divider.setMaximumSize(new Dimension(380, 24));
        root.add(divider);

        root.add(Box.createVerticalStrut(16));

        // 5. Standard Email Sign Up Action
        JButton signUpBtn = UIUtils.createPrimaryButton("Create Account with Email");
        signUpBtn.setFont(UIUtils.FONT_HEADER);
        signUpBtn.setMaximumSize(new Dimension(380, 44));
        signUpBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpBtn.addActionListener(e -> handleEmailSignUp());
        root.add(signUpBtn);

        root.add(Box.createVerticalStrut(16));

        // 6. Already have an account link
        JPanel footerLinks = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        footerLinks.setOpaque(false);
        footerLinks.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel existingLabel = new JLabel("Already have an account?");
        existingLabel.setFont(UIUtils.FONT_SMALL);
        existingLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        JButton signInLink = new JButton("Sign In →");
        signInLink.setFont(UIUtils.FONT_SMALL_BOLD);
        signInLink.setForeground(UIUtils.COLOR_PRIMARY);
        signInLink.setContentAreaFilled(false);
        signInLink.setBorderPainted(false);
        signInLink.setFocusPainted(false);
        signInLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signInLink.setBorder(new EmptyBorder(0, 2, 0, 0));
        signInLink.addActionListener(e -> handleEmailSignIn());

        footerLinks.add(existingLabel);
        footerLinks.add(signInLink);
        root.add(footerLinks);

        root.add(Box.createVerticalStrut(12));

        // 7. Cancel / Stay on Feed
        JButton cancelBtn = new JButton("Stay on Community Feed");
        cancelBtn.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 12));
        cancelBtn.setForeground(UIUtils.COLOR_TEXT_MUTED);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelBtn.addActionListener(e -> dispose());
        root.add(cancelBtn);

        add(root, BorderLayout.CENTER);

        pack();
        setSize(460, 480);
        setLocationRelativeTo(mainFrame);
    }

    private void handleGoogleAuth() {
        UserAccount account = GoogleAuthDialog.showDialog(this, controller);
        if (account != null) {
            controller.setCurrentUser(account);
            if (mainFrame != null) {
                mainFrame.updateUserSession(account);
                mainFrame.showMainWorkspace();
                if ("REPORT_LOST".equals(targetNavKey)) {
                    mainFrame.switchToReportLost();
                } else if ("REPORT_FOUND".equals(targetNavKey)) {
                    mainFrame.switchToReportFound();
                }
            }
            dispose();
        }
    }

    private void handleEmailSignUp() {
        dispose();
        if (mainFrame != null) {
            mainFrame.showAuthPage(true, targetNavKey);
        }
    }

    private void handleEmailSignIn() {
        dispose();
        if (mainFrame != null) {
            mainFrame.showAuthPage(false, targetNavKey);
        }
    }

    public static void prompt(MainFrame frame, LostFoundController controller, String reportType, String targetNavKey) {
        GuestReportAuthDialog dialog = new GuestReportAuthDialog(frame, controller, reportType, targetNavKey);
        dialog.setVisible(true);
    }
}

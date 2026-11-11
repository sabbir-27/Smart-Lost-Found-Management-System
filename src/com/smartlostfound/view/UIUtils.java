package com.smartlostfound.view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.List;

/**
 * Executive Design System &amp; UI Utilities.
 * Implements the DesignRush "Best Designs" award-winning aesthetic:
 * high-contrast carbon obsidian (#0B0B0C - #16171A), vibrant signature crimson (#E52E2D),
 * pure white elevated surfaces (#FFFFFF), multi-layered ambient shadows,
 * and high-definition typography with sub-pixel antialiasing.
 */
public final class UIUtils {

    // =========================================================================
    // Dynamic Typography Engine (Adapts to Ubuntu, macOS, Windows, Linux)
    // =========================================================================
    public static final String FONT_FAMILY = resolveBestFontFamily();

    public static final Font FONT_HERO = new Font(FONT_FAMILY, Font.BOLD, 26);
    public static final Font FONT_LOGO = new Font(FONT_FAMILY, Font.BOLD, 17);
    public static final Font FONT_HEADER = new Font(FONT_FAMILY, Font.BOLD, 20);
    public static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 15);
    public static final Font FONT_SUBTITLE = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font(FONT_FAMILY, Font.BOLD, 11);
    public static final Font FONT_BADGE = new Font(FONT_FAMILY, Font.BOLD, 10);
    public static final Font FONT_KPI_NUMBER = new Font(FONT_FAMILY, Font.BOLD, 30);
    public static final Font FONT_MONO = new Font("Monospaced", Font.BOLD, 12);

    // =========================================================================
    // DesignRush Executive Color Tokens
    // =========================================================================

    // Signature Crimson Brand Accents
    public static final Color COLOR_PRIMARY = new Color(229, 46, 45);          // #E52E2D - DesignRush Signature Red
    public static final Color COLOR_PRIMARY_HOVER = new Color(200, 32, 31);    // #C8201F - Darker crimson on hover
    public static final Color COLOR_PRIMARY_DARK = new Color(175, 24, 24);     // Gradient deep edge
    public static final Color COLOR_PRIMARY_LIGHT = new Color(254, 242, 242);  // #FEF2F2 - Soft rose background
    public static final Color COLOR_PRIMARY_BORDER = new Color(254, 202, 202); // #FECACA - Delicate red border
    public static final Color COLOR_PRIMARY_GLOW = new Color(229, 46, 45, 30); // Subtle red halo

    // Carbon Obsidian & Zinc Sidebar (Dual-tone gradient)
    public static final Color COLOR_SIDEBAR_TOP = new Color(11, 11, 14);       // #0B0B0E - Pitch Obsidian
    public static final Color COLOR_SIDEBAR_BOTTOM = new Color(18, 19, 23);    // #121317 - Deep Carbon Zinc
    public static final Color COLOR_SIDEBAR_BG = new Color(11, 11, 14);        // Primary fallback
    public static final Color COLOR_SIDEBAR_CARD = new Color(26, 27, 34);      // Elevated sidebar surface
    public static final Color COLOR_SIDEBAR_HOVER = new Color(38, 40, 50);     // Hover state
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(229, 46, 45);    // Active Crimson Indicator
    public static final Color COLOR_SIDEBAR_TEXT = new Color(161, 161, 170);   // #A1A1AA - Zinc 400
    public static final Color COLOR_SIDEBAR_MUTED = new Color(113, 113, 122);  // #71717A - Zinc 500

    // Canvas & Elevated Surfaces
    public static final Color COLOR_BACKGROUND = new Color(248, 249, 250);     // #F8F9FA - Clean Executive Backdrop
    public static final Color COLOR_SURFACE = new Color(255, 255, 255);        // #FFFFFF - Pure White Card Surface
    public static final Color COLOR_SURFACE_ALT = new Color(250, 250, 252);    // Subtle contrast surface
    public static final Color COLOR_BORDER = new Color(228, 228, 231);         // #E4E4E7 - Clean Hairline Border
    public static final Color COLOR_BORDER_LIGHT = new Color(240, 240, 243);   // Soft divider
    public static final Color COLOR_BORDER_FOCUS = new Color(229, 46, 45);     // Red Focus Ring

    // Typography
    public static final Color COLOR_TEXT_PRIMARY = new Color(15, 15, 17);      // #0F0F11 - Pitch Black
    public static final Color COLOR_TEXT_SECONDARY = new Color(82, 82, 91);    // #52525B - Zinc 600
    public static final Color COLOR_TEXT_MUTED = new Color(140, 140, 150);     // #8C8C96 - Zinc 450

    // Supporting Status Accents
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);         // #10B981 - Emerald Green
    public static final Color COLOR_SUCCESS_LIGHT = new Color(236, 253, 245);  // #ECFDF5
    public static final Color COLOR_SUCCESS_BORDER = new Color(167, 243, 208); // #A7F3D0

    public static final Color COLOR_BLUE = new Color(2, 132, 199);             // #0284C7 - Electric Sky Blue
    public static final Color COLOR_BLUE_LIGHT = new Color(240, 249, 255);     // #F0F9FF
    public static final Color COLOR_BLUE_BORDER = new Color(186, 230, 253);    // #BAE6FD

    public static final Color COLOR_WARNING = new Color(245, 158, 11);         // #F59E0B - Amber
    public static final Color COLOR_WARNING_LIGHT = new Color(254, 252, 232);  // #FEFCE8

    public static final Color COLOR_PURPLE = new Color(124, 58, 237);          // #7C3AED - Royal Violet
    public static final Color COLOR_PURPLE_LIGHT = new Color(245, 243, 255);   // #F5F3FF

    public static final Color COLOR_CHARCOAL_BTN = new Color(24, 24, 27);      // #18181B - Solid Dark Obsidian
    public static final Color COLOR_DANGER = new Color(229, 46, 45);
    public static final Color COLOR_DANGER_HOVER = new Color(185, 28, 28);
    public static final Color COLOR_TEAL = new Color(13, 148, 136);

    // Table Styling Tokens
    public static final Color COLOR_TABLE_HEADER = new Color(245, 245, 247);
    public static final Color COLOR_TABLE_ROW_ALT = new Color(251, 251, 253);
    public static final Color COLOR_TABLE_HOVER = new Color(244, 244, 247);
    public static final Color COLOR_TABLE_SELECTION = new Color(254, 242, 242);// Soft Rose Tint

    private UIUtils() {
    }

    /**
     * Dynamically discovers the crispest system font available in the runtime environment.
     */
    private static String resolveBestFontFamily() {
        String[] preferred = {
                "Ubuntu Sans", "Ubuntu", "Segoe UI", "SF Pro Display",
                "Inter", "Roboto", "Noto Sans", "Liberation Sans", "SansSerif"
        };
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            List<String> available = Arrays.asList(ge.getAvailableFontFamilyNames());
            for (String font : preferred) {
                if (available.contains(font)) {
                    return font;
                }
            }
        } catch (Exception ignored) {
        }
        return Font.SANS_SERIF;
    }

    /**
     * Initializes global Swing Look &amp; Feel with high-definition anti-aliasing.
     */
    public static void applyModernLookAndFeel() {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");

        try {
            boolean nimbusSet = false;
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equalsIgnoreCase(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    nimbusSet = true;
                    break;
                }
            }
            if (!nimbusSet) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }

            UIManager.put("Button.font", FONT_BODY_BOLD);
            UIManager.put("Label.font", FONT_BODY);
            UIManager.put("TextField.font", FONT_BODY);
            UIManager.put("ComboBox.font", FONT_BODY);
            UIManager.put("Table.font", FONT_BODY);
            UIManager.put("TableHeader.font", FONT_BODY_BOLD);

        } catch (Exception ignored) {
        }
    }

    /**
     * Creates an elevated modern card panel with smooth multi-layer drop shadows.
     */
    public static JPanel createCardPanel() {
        return new ElevatedCardPanel(12);
    }

    /**
     * Creates an elevated modern card panel with a specific corner radius.
     */
    public static JPanel createCardPanel(int cornerRadius) {
        return new ElevatedCardPanel(cornerRadius);
    }

    /**
     * Creates a luxurious primary action button using DesignRush signature crimson gradient.
     */
    public static JButton createPrimaryButton(String text) {
        return createGradientButton(text, COLOR_PRIMARY, COLOR_PRIMARY_DARK, Color.WHITE, 8);
    }

    /**
     * Creates a secondary/neutral button with clean editorial outline and subtle hover lift.
     */
    public static JButton createSecondaryButton(String text) {
        return createModernButton(text, new Color(244, 244, 246), new Color(228, 228, 232), COLOR_TEXT_PRIMARY, 8);
    }

    /**
     * Creates a solid dark obsidian action button.
     */
    public static JButton createDarkButton(String text) {
        return createGradientButton(text, COLOR_CHARCOAL_BTN, new Color(14, 14, 17), Color.WHITE, 8);
    }

    /**
     * Creates a success green action button.
     */
    public static JButton createSuccessButton(String text) {
        return createGradientButton(text, COLOR_SUCCESS, new Color(5, 150, 105), Color.WHITE, 8);
    }

    /**
     * Creates a danger/delete action button with soft red styling.
     */
    public static JButton createDangerButton(String text) {
        return createModernButton(text, COLOR_PRIMARY_LIGHT, COLOR_PRIMARY_BORDER, COLOR_PRIMARY, 8);
    }

    /**
     * Factory for creating rich gradient buttons with 1px top highlight and smooth feedback.
     */
    public static JButton createGradientButton(String text, Color topColor, Color bottomColor, Color fg, int radius) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                Color c1 = topColor;
                Color c2 = bottomColor;

                if (!isEnabled()) {
                    c1 = new Color(228, 228, 231);
                    c2 = new Color(212, 212, 216);
                } else if (getModel().isPressed()) {
                    c1 = bottomColor.darker();
                    c2 = bottomColor.darker();
                } else if (getModel().isRollover()) {
                    c1 = topColor.brighter();
                    c2 = bottomColor;
                }

                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, c1, 0, h, c2);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, radius * 2, radius * 2));

                // 1px top subtle glass highlight
                if (isEnabled()) {
                    g2.setColor(new Color(255, 255, 255, 45));
                    g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, radius * 2 - 2, radius * 2 - 2));
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(FONT_BODY_BOLD);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(9, 18, 9, 18));

        return button;
    }

    /**
     * Factory for creating modern rounded buttons with custom hover and click feedback.
     */
    public static JButton createModernButton(String text, Color bg, Color hoverBg, Color fg, int radius) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color currentBg = bg;
                if (!isEnabled()) {
                    currentBg = new Color(228, 228, 231);
                } else if (getModel().isPressed()) {
                    currentBg = hoverBg.darker();
                } else if (getModel().isRollover()) {
                    currentBg = hoverBg;
                }

                g2.setColor(currentBg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius * 2, radius * 2));

                // Clean hairline border
                if (bg.equals(COLOR_PRIMARY_LIGHT) || bg.equals(new Color(244, 244, 246))) {
                    g2.setColor(COLOR_BORDER);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, radius * 2, radius * 2));
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(FONT_BODY_BOLD);
        button.setForeground(fg);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(9, 18, 9, 18));

        return button;
    }

    /**
     * Creates a focus-aware text field with DesignRush red focus ring.
     */
    public static JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FONT_BODY);
        field.setBackground(COLOR_SURFACE);
        field.setForeground(COLOR_TEXT_PRIMARY);
        field.setCaretColor(COLOR_PRIMARY);

        Border normalBorder = new CompoundBorder(
                new RoundedLineBorder(COLOR_BORDER, 1, 8),
                new EmptyBorder(8, 12, 8, 12)
        );
        Border focusBorder = new CompoundBorder(
                new RoundedLineBorder(COLOR_PRIMARY, 2, 8),
                new EmptyBorder(7, 11, 7, 11)
        );

        field.setBorder(normalBorder);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(focusBorder);
                field.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(normalBorder);
                field.repaint();
            }
        });

        return field;
    }

    /**
     * Creates a focus-aware text area with clean inner padding and rounded border.
     */
    public static JTextArea createStyledTextArea(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setFont(FONT_BODY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(COLOR_SURFACE);
        area.setForeground(COLOR_TEXT_PRIMARY);
        area.setCaretColor(COLOR_PRIMARY);
        area.setBorder(new EmptyBorder(8, 12, 8, 12));
        return area;
    }

    /**
     * Creates an editorial section title with DesignRush red badge numbering.
     */
    public static JLabel createSectionTitle(String badgeNumber, String title) {
        JLabel label = new JLabel(String.format("<html><b><font color='#E52E2D'>[%s]</font></b> &nbsp;%s</html>",
                badgeNumber, title));
        label.setFont(FONT_TITLE);
        label.setForeground(COLOR_TEXT_PRIMARY);
        return label;
    }

    /**
     * Creates a standard form label.
     */
    public static JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY_BOLD);
        label.setForeground(COLOR_TEXT_PRIMARY);
        return label;
    }

    /**
     * Creates a required form label with red asterisk.
     */
    public static JLabel createRequiredFormLabel(String text) {
        JLabel label = new JLabel("<html>" + text + " <font color='#E52E2D'>*</font></html>");
        label.setFont(FONT_BODY_BOLD);
        label.setForeground(COLOR_TEXT_PRIMARY);
        return label;
    }

    /**
     * Creates an elevated executive KPI metric card with colored top accent bar.
     */
    public static JPanel createKpiCard(String iconSymbol, String title, JLabel valueNumberLabel, Color accentColor, String subtitle) {
        JPanel card = new ElevatedCardPanel(12) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Top accent strip (DesignRush indicator)
                g2.setColor(accentColor);
                g2.fill(new RoundRectangle2D.Float(14, 0, getWidth() - 28, 4, 3, 3));
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(10, 8));
        card.setBorder(new EmptyBorder(18, 18, 16, 18));

        // Top bar with Icon and Title
        JPanel topRow = new JPanel(new BorderLayout(10, 0));
        topRow.setOpaque(false);

        // Icon inside subtle circular capsule
        JPanel iconCapsule = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 25));
                g2.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconCapsule.setPreferredSize(new Dimension(32, 32));
        iconCapsule.setOpaque(false);

        JLabel iconLabel = new JLabel(iconSymbol);
        iconLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 15));
        iconLabel.setForeground(accentColor);
        iconCapsule.add(iconLabel);

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(FONT_SMALL_BOLD);
        titleLabel.setForeground(COLOR_TEXT_MUTED);

        topRow.add(iconCapsule, BorderLayout.WEST);
        topRow.add(titleLabel, BorderLayout.CENTER);

        // Center Metric value
        valueNumberLabel.setFont(FONT_KPI_NUMBER);
        valueNumberLabel.setForeground(COLOR_TEXT_PRIMARY);

        // Bottom subtitle
        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(FONT_SMALL);
        subLabel.setForeground(COLOR_TEXT_SECONDARY);

        card.add(topRow, BorderLayout.NORTH);
        card.add(valueNumberLabel, BorderLayout.CENTER);
        card.add(subLabel, BorderLayout.SOUTH);

        return card;
    }

    // =========================================================================
    // Custom Graphics Painting Helpers
    // =========================================================================

    /**
     * Elevated Card Panel with multi-layered soft drop shadow and hairline border.
     */
    public static class ElevatedCardPanel extends JPanel {
        private final int radius;

        public ElevatedCardPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
            setBorder(new EmptyBorder(16, 18, 16, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Layer 1: Ambient soft shadow
            g2.setColor(new Color(15, 15, 20, 6));
            g2.fill(new RoundRectangle2D.Float(0, 3, width, height - 3, radius * 2, radius * 2));

            // Layer 2: Key contact shadow
            g2.setColor(new Color(15, 15, 20, 10));
            g2.fill(new RoundRectangle2D.Float(0, 1, width, height - 1, radius * 2, radius * 2));

            // Card surface background
            g2.setColor(COLOR_SURFACE);
            g2.fill(new RoundRectangle2D.Float(0, 0, width - 1, height - 1, radius * 2, radius * 2));

            // 1px Hairline border
            g2.setColor(COLOR_BORDER);
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 2, height - 2, radius * 2, radius * 2));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Custom rounded border for input fields with subpixel precision.
     */
    public static class RoundedLineBorder implements Border {
        private final Color color;
        private final int thickness;
        private final int radius;

        public RoundedLineBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x + thickness / 2.0f, y + thickness / 2.0f,
                    width - thickness, height - thickness, radius * 2, radius * 2));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness + 2, thickness + 4, thickness + 2, thickness + 4);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}

package com.smartlostfound.view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * UI Utilities class providing standard modern design tokens, color schemes,
 * typography, component factories, and consistent spacing across the application.
 */
public final class UIUtils {

    // Primary Colors
    public static final Color COLOR_PRIMARY = new Color(37, 99, 235);        // #2563EB - Royal Blue
    public static final Color COLOR_PRIMARY_HOVER = new Color(29, 78, 216);  // #1D4ED8
    public static final Color COLOR_SECONDARY = new Color(100, 116, 139);    // #64748B - Slate
    public static final Color COLOR_SECONDARY_HOVER = new Color(71, 85, 105);
    public static final Color COLOR_SUCCESS = new Color(22, 163, 74);        // #16A34A - Green
    public static final Color COLOR_SUCCESS_HOVER = new Color(21, 128, 61);
    public static final Color COLOR_DANGER = new Color(220, 38, 38);         // #DC2626 - Red
    public static final Color COLOR_DANGER_HOVER = new Color(185, 28, 28);
    public static final Color COLOR_WARNING = new Color(217, 119, 6);        // #D97706 - Amber

    // Neutral Surfaces & Borders
    public static final Color COLOR_BACKGROUND = new Color(248, 250, 252);   // #F8FAFC - Off white
    public static final Color COLOR_SURFACE = new Color(255, 255, 255);      // #FFFFFF - Pure white
    public static final Color COLOR_BORDER = new Color(226, 232, 240);       // #E2E8F0 - Soft gray
    public static final Color COLOR_HEADER_BG = new Color(15, 23, 42);       // #0F172A - Deep slate
    public static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);    // #0F172A - Deep slate
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);   // #64748B - Cool gray
    public static final Color COLOR_TABLE_HEADER = new Color(241, 245, 249); // #F1F5F9 - Table header
    public static final Color COLOR_TABLE_ROW_ALT = new Color(248, 250, 252);// #F8FAFC
    public static final Color COLOR_TABLE_SELECTION = new Color(219, 234, 254);// #DBEAFE

    // Typography
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_KPI_NUMBER = new Font("SansSerif", Font.BOLD, 24);

    private UIUtils() {
        // Utility class private constructor
    }

    /**
     * Initializes global Swing Look &amp; Feel with modern anti-aliasing and native decorations.
     */
    public static void applyModernLookAndFeel() {
        try {
            // Prefer Nimbus or System L&F
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

            // Global UI properties
            UIManager.put("Button.font", FONT_BODY_BOLD);
            UIManager.put("Label.font", FONT_BODY);
            UIManager.put("TextField.font", FONT_BODY);
            UIManager.put("ComboBox.font", FONT_BODY);
            UIManager.put("Table.font", FONT_BODY);
            UIManager.put("TableHeader.font", FONT_BODY_BOLD);
            UIManager.put("TabbedPane.font", FONT_BODY_BOLD);

        } catch (Exception ignored) {
            // Fallback to default Swing look and feel
        }
    }

    /**
     * Creates a styled modern card panel with subtle border and crisp white background.
     *
     * @return JPanel configured as a card
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(COLOR_SURFACE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return card;
    }

    /**
     * Creates a primary filled action button with hover animation and rounded borders.
     *
     * @param text Button label
     * @return Styled JButton
     */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, COLOR_PRIMARY, COLOR_PRIMARY_HOVER, Color.WHITE);
    }

    /**
     * Creates a secondary/neutral action button.
     *
     * @param text Button label
     * @return Styled JButton
     */
    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, COLOR_SECONDARY, COLOR_SECONDARY_HOVER, Color.WHITE);
    }

    /**
     * Creates a success colored action button.
     *
     * @param text Button label
     * @return Styled JButton
     */
    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, COLOR_SUCCESS, COLOR_SUCCESS_HOVER, Color.WHITE);
    }

    /**
     * Creates a danger/delete action button.
     *
     * @param text Button label
     * @return Styled JButton
     */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, COLOR_DANGER, COLOR_DANGER_HOVER, Color.WHITE);
    }

    /**
     * Factory to create styled custom buttons.
     */
    public static JButton createStyledButton(String text, Color bg, Color hoverBg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverBg);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });

        return button;
    }

    /**
     * Creates an aesthetically styled JTextField with inner padding.
     *
     * @param columns Visible column width
     * @return Styled JTextField
     */
    public static JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FONT_BODY);
        field.setBorder(createInputFieldBorder());
        field.setBackground(COLOR_SURFACE);
        return field;
    }

    /**
     * Creates an aesthetically styled JTextArea with word wrap and inner padding.
     *
     * @param rows Visible rows
     * @param cols Visible columns
     * @return Styled JTextArea
     */
    public static JTextArea createStyledTextArea(int rows, int cols) {
        JTextArea area = new JTextArea(rows, cols);
        area.setFont(FONT_BODY);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(6, 8, 6, 8));
        area.setBackground(COLOR_SURFACE);
        return area;
    }

    /**
     * Standard border for text fields and inputs.
     *
     * @return Compound border
     */
    public static Border createInputFieldBorder() {
        return new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        );
    }

    /**
     * Creates a form label with title font styling.
     *
     * @param text Label text
     * @return Styled JLabel
     */
    public static JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY_BOLD);
        label.setForeground(COLOR_TEXT_PRIMARY);
        return label;
    }

    /**
     * Creates a form label marked with an asterisk to denote required inputs.
     *
     * @param text Label text
     * @return Styled JLabel with red asterisk
     */
    public static JLabel createRequiredFormLabel(String text) {
        JLabel label = new JLabel("<html>" + text + " <font color='#DC2626'>*</font></html>");
        label.setFont(FONT_BODY_BOLD);
        label.setForeground(COLOR_TEXT_PRIMARY);
        return label;
    }

    /**
     * Creates a KPI metric summary card for the dashboard header.
     *
     * @param title Title of the metric
     * @param valueNumberLabel Reference to updateable label for metric value
     * @param accentColor Accent color for the top border / highlight
     * @param subtitle Subtitle description
     * @return Formatted metric card JPanel
     */
    public static JPanel createKpiCard(String title, JLabel valueNumberLabel, Color accentColor, String subtitle) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(COLOR_SURFACE);
        card.setBorder(new CompoundBorder(
                new CompoundBorder(
                        BorderFactory.createMatteBorder(3, 0, 0, 0, accentColor),
                        new LineBorder(COLOR_BORDER, 1)
                ),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        titleLabel.setForeground(COLOR_TEXT_MUTED);

        valueNumberLabel.setFont(FONT_KPI_NUMBER);
        valueNumberLabel.setForeground(COLOR_TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(FONT_SMALL);
        subtitleLabel.setForeground(COLOR_TEXT_MUTED);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueNumberLabel, BorderLayout.CENTER);
        card.add(subtitleLabel, BorderLayout.SOUTH);

        return card;
    }
}

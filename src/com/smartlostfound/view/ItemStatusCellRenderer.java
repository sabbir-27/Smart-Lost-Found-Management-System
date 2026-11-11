package com.smartlostfound.view;

import com.smartlostfound.model.ItemStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern table cell renderer displaying lifecycle statuses as refined,
 * anti-aliased pill badges with proportional typography and distinct color tokens.
 */
public class ItemStatusCellRenderer extends DefaultTableCellRenderer {

    public ItemStatusCellRenderer() {
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        if (value instanceof ItemStatus) {
            ItemStatus status = (ItemStatus) value;
            return new BadgePanel(status, isSelected);
        }

        return label;
    }

    /**
     * Inner custom component painting the status badge with anti-aliasing.
     */
    private static class BadgePanel extends JPanel {
        private final ItemStatus status;
        private final boolean isSelected;

        public BadgePanel(ItemStatus status, boolean isSelected) {
            this.status = status;
            this.isSelected = isSelected;
            setLayout(new GridBagLayout());
            setOpaque(true);

            String prefix;
            switch (status) {
                case LOST: prefix = "● "; break;
                case FOUND: prefix = "● "; break;
                case RECOVERED: prefix = "✓ "; break;
                case CLAIMED: prefix = "★ "; break;
                default: prefix = "● "; break;
            }

            JLabel label = new JLabel(prefix + status.getDisplayName().toUpperCase());
            label.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 10));
            label.setForeground(status.getForegroundColor());
            label.setBorder(new EmptyBorder(2, 10, 2, 10));

            add(label);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Cell Background
            if (isSelected) {
                g2.setColor(UIUtils.COLOR_TABLE_SELECTION);
            } else {
                g2.setColor(Color.WHITE);
            }
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Rounded Pill Badge
            int badgeWidth = Math.min(getWidth() - 12, 100);
            int badgeHeight = 26;
            int x = (getWidth() - badgeWidth) / 2;
            int y = (getHeight() - badgeHeight) / 2;

            // Fill badge background
            g2.setColor(status.getBackgroundColor());
            g2.fill(new RoundRectangle2D.Float(x, y, badgeWidth, badgeHeight, badgeHeight, badgeHeight));

            // Draw soft 1px border
            Color fg = status.getForegroundColor();
            g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 90));
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, badgeWidth - 1, badgeHeight - 1, badgeHeight, badgeHeight));

            g2.dispose();
            super.paintChildren(g);
        }
    }
}

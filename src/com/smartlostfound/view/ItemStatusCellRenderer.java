package com.smartlostfound.view;

import com.smartlostfound.model.ItemStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom table cell renderer that displays lifecycle statuses as modern
 * rounded color badges (pills) matching modern web/desktop styling.
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
     * Inner custom component to paint the status badge with rounded corners.
     */
    private static class BadgePanel extends JPanel {
        private final ItemStatus status;
        private final boolean isSelected;

        public BadgePanel(ItemStatus status, boolean isSelected) {
            this.status = status;
            this.isSelected = isSelected;
            setLayout(new GridBagLayout());
            setOpaque(true);

            JLabel label = new JLabel(status.getDisplayName());
            label.setFont(new Font("SansSerif", Font.BOLD, 11));
            label.setForeground(status.getForegroundColor());
            label.setBorder(new EmptyBorder(3, 10, 3, 10));

            add(label);
        }

        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fill table cell background
            g2.setColor(isSelected ? UIUtils.COLOR_TABLE_SELECTION : Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Paint rounded pill badge
            int badgeWidth = Math.min(getWidth() - 16, 95);
            int badgeHeight = getHeight() - 10;
            int x = (getWidth() - badgeWidth) / 2;
            int y = 5;

            g2.setColor(status.getBackgroundColor());
            g2.fillRoundRect(x, y, badgeWidth, badgeHeight, 12, 12);

            g2.setColor(status.getForegroundColor());
            g2.drawRoundRect(x, y, badgeWidth, badgeHeight, 12, 12);

            g2.dispose();
            super.paintChildren(g);
        }
    }
}

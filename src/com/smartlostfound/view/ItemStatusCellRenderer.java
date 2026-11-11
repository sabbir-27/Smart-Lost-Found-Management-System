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
            return new BadgePanel(status, isSelected, table.getSelectionBackground());
        }

        return label;
    }

    /**
     * Inner custom component painting the status badge with anti-aliasing.
     */
    private static class BadgePanel extends JPanel {
        private final ItemStatus status;
        private final boolean isSelected;
        private final Color selectionBg;

        public BadgePanel(ItemStatus status, boolean isSelected, Color selectionBg) {
            this.status = status;
            this.isSelected = isSelected;
            this.selectionBg = selectionBg;
            setLayout(new GridBagLayout());
            setOpaque(true);

            JLabel label = new JLabel(status.getDisplayName().toUpperCase());
            label.setFont(new Font("Segoe UI", Font.BOLD, 10));
            label.setForeground(status.getForegroundColor());
            label.setBorder(new EmptyBorder(3, 10, 3, 10));

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
            int badgeWidth = Math.min(getWidth() - 16, 92);
            int badgeHeight = 24;
            int x = (getWidth() - badgeWidth) / 2;
            int y = (getHeight() - badgeHeight) / 2;

            g2.setColor(status.getBackgroundColor());
            g2.fill(new RoundRectangle2D.Float(x, y, badgeWidth, badgeHeight, 12, 12));

            g2.setColor(new Color(status.getForegroundColor().getRed(),
                                  status.getForegroundColor().getGreen(),
                                  status.getForegroundColor().getBlue(), 120));
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, badgeWidth - 1, badgeHeight - 1, 12, 12));

            g2.dispose();
            super.paintChildren(g);
        }
    }
}

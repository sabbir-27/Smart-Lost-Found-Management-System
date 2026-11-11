package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.FoundItem;
import com.smartlostfound.model.Item;
import com.smartlostfound.model.LostItem;
import com.smartlostfound.model.User;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Social media activity feed view inspired by Facebook's newsfeed.
 * Displays lost and found property reports as social post cards featuring
 * author avatars, status badges, reward callouts, and community reaction buttons.
 */
public class CommunityFeedPanel extends JPanel {

    private final LostFoundController controller;
    private final Map<String, Integer> helpfulReactions = new HashMap<>();

    private JPanel feedPostsContainer;
    private JTextField feedSearchField;
    private String currentFeedFilter = "ALL"; // ALL, LOST, FOUND, REWARD

    public CommunityFeedPanel(LostFoundController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.COLOR_BACKGROUND);

        // Feed Header & Creator Bar
        JPanel topHeader = new JPanel();
        topHeader.setLayout(new BoxLayout(topHeader, BoxLayout.Y_AXIS));
        topHeader.setOpaque(false);
        topHeader.setBorder(new EmptyBorder(16, 20, 10, 20));

        topHeader.add(createPostComposerCard());
        topHeader.add(Box.createVerticalStrut(12));
        topHeader.add(createFeedFilterBar());

        add(topHeader, BorderLayout.NORTH);

        // Scrollable Feed Posts Container
        feedPostsContainer = new JPanel();
        feedPostsContainer.setLayout(new BoxLayout(feedPostsContainer, BoxLayout.Y_AXIS));
        feedPostsContainer.setBackground(UIUtils.COLOR_BACKGROUND);
        feedPostsContainer.setBorder(new EmptyBorder(6, 20, 24, 20));

        JScrollPane scrollPane = new JScrollPane(feedPostsContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(UIUtils.COLOR_BACKGROUND);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Top Facebook-style "What did you lose or find?" composer card.
     */
    private JPanel createPostComposerCard() {
        JPanel card = UIUtils.createCardPanel(10);
        card.setLayout(new BorderLayout(14, 10));

        // Left prompt
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        AvatarIcon avatar = new AvatarIcon("YOU", new Color(39, 39, 42));
        JLabel prompt = new JLabel("Lost an item or found someone's property? Share it with the community...");
        prompt.setFont(UIUtils.FONT_BODY);
        prompt.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        left.add(avatar);
        left.add(prompt);
        card.add(left, BorderLayout.WEST);

        // Right quick buttons
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightButtons.setOpaque(false);

        JButton postLostBtn = UIUtils.createModernButton("▲ Report Lost", UIUtils.COLOR_PRIMARY,
                UIUtils.COLOR_PRIMARY_HOVER, Color.WHITE, 8);
        postLostBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof MainFrame) {
                ((MainFrame) w).switchToReportLost();
            }
        });

        JButton postFoundBtn = UIUtils.createDarkButton("▼ Report Found");
        postFoundBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof MainFrame) {
                ((MainFrame) w).switchToReportFound();
            }
        });

        rightButtons.add(postLostBtn);
        rightButtons.add(postFoundBtn);
        card.add(rightButtons, BorderLayout.EAST);

        return card;
    }

    /**
     * Filter bar with Facebook-style pill filters and search.
     */
    private JPanel createFeedFilterBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);

        JPanel pillGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pillGroup.setOpaque(false);

        pillGroup.add(createFilterChip("All Posts", "ALL", true));
        pillGroup.add(createFilterChip("▲ Lost Items", "LOST", false));
        pillGroup.add(createFilterChip("▼ Found Items", "FOUND", false));
        pillGroup.add(createFilterChip("💰 With Reward", "REWARD", false));

        bar.add(pillGroup, BorderLayout.WEST);

        // Feed keyword search
        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchWrapper.setOpaque(false);
        feedSearchField = UIUtils.createStyledTextField(18);
        feedSearchField.setToolTipText("Filter feed items by keyword...");
        feedSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshFeed(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshFeed(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshFeed(); }
        });
        searchWrapper.add(new JLabel("Filter: "));
        searchWrapper.add(feedSearchField);
        bar.add(searchWrapper, BorderLayout.EAST);

        return bar;
    }

    private JButton createFilterChip(String text, String filterKey, boolean defaultSelected) {
        JButton btn = new JButton(text);
        btn.setFont(UIUtils.FONT_SMALL_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));

        applyChipStyle(btn, defaultSelected);

        btn.addActionListener(e -> {
            this.currentFeedFilter = filterKey;
            // Update all chips in parent
            Container parent = btn.getParent();
            for (Component c : parent.getComponents()) {
                if (c instanceof JButton) {
                    applyChipStyle((JButton) c, c == btn);
                }
            }
            refreshFeed();
        });

        return btn;
    }

    private void applyChipStyle(JButton btn, boolean isSelected) {
        if (isSelected) {
            btn.setBackground(UIUtils.COLOR_CHARCOAL_BTN);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        }
        btn.repaint();
    }

    /**
     * Refreshes the feed using current items from the controller/manager.
     *
     * @param items List of items to render
     */
    public void displayFeedItems(List<Item> items) {
        feedPostsContainer.removeAll();

        String keyword = feedSearchField != null ? feedSearchField.getText().trim().toLowerCase() : "";

        int renderedCount = 0;
        for (Item item : items) {
            // Filter check
            if ("LOST".equals(currentFeedFilter) && !(item instanceof LostItem)) continue;
            if ("FOUND".equals(currentFeedFilter) && !(item instanceof FoundItem)) continue;
            if ("REWARD".equals(currentFeedFilter)) {
                if (!(item instanceof LostItem) || ((LostItem) item).getRewardAmount() <= 0.0) continue;
            }

            // Keyword match
            if (!keyword.isEmpty() && !item.matchesKeyword(keyword)) continue;

            feedPostsContainer.add(createPostCard(item));
            feedPostsContainer.add(Box.createVerticalStrut(14));
            renderedCount++;
        }

        if (renderedCount == 0) {
            feedPostsContainer.add(createEmptyFeedPlaceholder());
        }

        feedPostsContainer.revalidate();
        feedPostsContainer.repaint();
    }

    private JPanel createEmptyFeedPlaceholder() {
        JPanel placeholder = UIUtils.createCardPanel(10);
        placeholder.setLayout(new GridBagLayout());
        placeholder.setPreferredSize(new Dimension(600, 160));

        JLabel icon = new JLabel("🔍");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 36));

        JLabel msg = new JLabel("No matching property reports found in the feed.");
        msg.setFont(UIUtils.FONT_TITLE);
        msg.setForeground(UIUtils.COLOR_TEXT_MUTED);

        JPanel p = new JPanel(new GridLayout(2, 1, 0, 8));
        p.setOpaque(false);
        p.add(icon);
        p.add(msg);

        placeholder.add(p);
        return placeholder;
    }

    /**
     * Creates an individual Facebook-style Post Card for an item.
     */
    private JPanel createPostCard(Item item) {
        JPanel postCard = UIUtils.createCardPanel(12);
        postCard.setLayout(new BorderLayout(0, 12));
        postCard.setMaximumSize(new Dimension(860, 560));

        boolean isLost = item instanceof LostItem;
        User user = item.getReporter();
        String authorName = (user != null && !user.getName().isEmpty()) ? user.getName() : "Anonymous Community Member";
        String initials = getInitials(authorName);

        // 1. Post Header (Avatar + Name + Timestamp + Type Badge)
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setOpaque(false);

        // Left Avatar & User info
        JPanel userMeta = new JPanel(new BorderLayout(10, 0));
        userMeta.setOpaque(false);

        AvatarIcon avatar = new AvatarIcon(initials, isLost ? UIUtils.COLOR_PRIMARY : UIUtils.COLOR_CHARCOAL_BTN);
        userMeta.add(avatar, BorderLayout.WEST);

        JPanel nameAndDate = new JPanel(new GridLayout(2, 1, 0, 2));
        nameAndDate.setOpaque(false);

        JLabel nameLabel = new JLabel(authorName);
        nameLabel.setFont(UIUtils.FONT_BODY_BOLD);
        nameLabel.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel subMeta = new JLabel(String.format("Reported on %s • 📍 %s", item.getFormattedDate(), item.getSpecificLocation()));
        subMeta.setFont(UIUtils.FONT_SMALL);
        subMeta.setForeground(UIUtils.COLOR_TEXT_MUTED);

        nameAndDate.add(nameLabel);
        nameAndDate.add(subMeta);
        userMeta.add(nameAndDate, BorderLayout.CENTER);

        headerPanel.add(userMeta, BorderLayout.WEST);

        // Right: Status and Tracking ID
        JPanel rightBadgeGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBadgeGroup.setOpaque(false);

        JLabel idLabel = new JLabel(item.getId());
        idLabel.setFont(UIUtils.FONT_MONO);
        idLabel.setForeground(isLost ? UIUtils.COLOR_PRIMARY : UIUtils.COLOR_TEXT_SECONDARY);

        JLabel statusBadge = new JLabel("  " + item.getStatus().getDisplayName().toUpperCase() + "  ");
        statusBadge.setFont(UIUtils.FONT_SMALL_BOLD);
        statusBadge.setOpaque(true);
        statusBadge.setBackground(item.getStatus().getBackgroundColor());
        statusBadge.setForeground(item.getStatus().getForegroundColor());
        statusBadge.setBorder(new CompoundBorder(
                new LineBorder(item.getStatus().getForegroundColor(), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));

        rightBadgeGroup.add(idLabel);
        rightBadgeGroup.add(statusBadge);
        headerPanel.add(rightBadgeGroup, BorderLayout.EAST);

        postCard.add(headerPanel, BorderLayout.NORTH);

        // 2. Post Content Body
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setOpaque(false);

        // Item Title
        JLabel titleLabel = new JLabel((isLost ? "⚠️ LOST: " : "📦 FOUND: ") + item.getName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        bodyPanel.add(titleLabel);
        bodyPanel.add(Box.createVerticalStrut(6));

        // Description text
        JTextArea descText = new JTextArea(item.getDescription());
        descText.setFont(UIUtils.FONT_BODY);
        descText.setLineWrap(true);
        descText.setWrapStyleWord(true);
        descText.setEditable(false);
        descText.setOpaque(false);
        descText.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        descText.setBorder(null);
        bodyPanel.add(descText);
        bodyPanel.add(Box.createVerticalStrut(10));

        // Highlight chip box (Reward or Storage custody)
        JPanel highlightBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        highlightBox.setBackground(new Color(249, 250, 251));
        highlightBox.setBorder(new LineBorder(UIUtils.COLOR_BORDER, 1, true));

        JLabel catChip = new JLabel("🏷️ " + item.getCategory().getDisplayName());
        catChip.setFont(UIUtils.FONT_SMALL_BOLD);
        catChip.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        highlightBox.add(catChip);

        if (isLost) {
            LostItem lost = (LostItem) item;
            if (lost.getRewardAmount() > 0.0) {
                JLabel rewardChip = new JLabel(String.format("💰 REWARD: $%.2f", lost.getRewardAmount()));
                rewardChip.setFont(UIUtils.FONT_SMALL_BOLD);
                rewardChip.setForeground(new Color(220, 38, 38));
                highlightBox.add(rewardChip);
            }
        } else {
            FoundItem found = (FoundItem) item;
            JLabel storageChip = new JLabel("🔒 Custody: " + found.getStorageLocation());
            storageChip.setFont(UIUtils.FONT_SMALL_BOLD);
            storageChip.setForeground(new Color(2, 132, 199));
            highlightBox.add(storageChip);
        }

        bodyPanel.add(highlightBox);
        bodyPanel.add(Box.createVerticalStrut(10));

        // Item Picture / Visual Photo Banner (Facebook Post Image)
        ImageIcon thumbnail = ImageHelper.getItemImageThumbnail(item, 760, 220);
        JLabel imageLabel = new JLabel(thumbnail);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imageLabel.setToolTipText("Click image to view full post and owner contact");
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.openItemDetailDialog(item, SwingUtilities.getWindowAncestor(CommunityFeedPanel.this));
            }
        });
        bodyPanel.add(imageLabel);

        postCard.add(bodyPanel, BorderLayout.CENTER);

        // 3. Post Footer Actions (Like/Looking, Contact, Update, Details)
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(8, 0, 0, 0)
        ));

        // Reaction Counter
        int count = helpfulReactions.getOrDefault(item.getId(), isLost ? 3 : 1);
        JLabel reactionCountLabel = new JLabel(String.format("👍 %d community members keeping an eye out", count));
        reactionCountLabel.setFont(UIUtils.FONT_SMALL);
        reactionCountLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);
        footer.add(reactionCountLabel, BorderLayout.WEST);

        // Action Buttons (Facebook style)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton helpfulBtn = new JButton("👍 Looking Out");
        helpfulBtn.setFont(UIUtils.FONT_SMALL_BOLD);
        helpfulBtn.setBackground(new Color(244, 244, 245));
        helpfulBtn.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        helpfulBtn.setFocusPainted(false);
        helpfulBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        helpfulBtn.addActionListener(e -> {
            int newCount = helpfulReactions.getOrDefault(item.getId(), isLost ? 3 : 1) + 1;
            helpfulReactions.put(item.getId(), newCount);
            reactionCountLabel.setText(String.format("👍 %d community members keeping an eye out", newCount));
            helpfulBtn.setText("✓ You're Looking");
            helpfulBtn.setEnabled(false);
        });

        JButton contactBtn = new JButton(isLost ? "💬 Contact Owner" : "💬 Contact Finder");
        contactBtn.setFont(UIUtils.FONT_SMALL_BOLD);
        contactBtn.setBackground(new Color(254, 242, 242));
        contactBtn.setForeground(UIUtils.COLOR_PRIMARY);
        contactBtn.setFocusPainted(false);
        contactBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        contactBtn.addActionListener(e -> openContactDialog(item, authorName, user));

        JButton updateBtn = new JButton("🔄 Update");
        updateBtn.setFont(UIUtils.FONT_SMALL_BOLD);
        updateBtn.setBackground(Color.WHITE);
        updateBtn.setForeground(UIUtils.COLOR_TEXT_SECONDARY);
        updateBtn.setFocusPainted(false);
        updateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateBtn.addActionListener(e -> controller.openUpdateStatusDialog(item, SwingUtilities.getWindowAncestor(this)));

        JButton detailsBtn = new JButton("View Full Post ➔");
        detailsBtn.setFont(UIUtils.FONT_SMALL_BOLD);
        detailsBtn.setBackground(Color.WHITE);
        detailsBtn.setForeground(UIUtils.COLOR_PRIMARY);
        detailsBtn.setFocusPainted(false);
        detailsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        detailsBtn.addActionListener(e -> controller.openItemDetailDialog(item, SwingUtilities.getWindowAncestor(this)));

        actions.add(helpfulBtn);
        actions.add(contactBtn);
        actions.add(updateBtn);
        actions.add(detailsBtn);

        footer.add(actions, BorderLayout.EAST);
        postCard.add(footer, BorderLayout.SOUTH);

        return postCard;
    }

    private void openContactDialog(Item item, String authorName, User user) {
        String message = String.format(
                "<html><body style='width: 320px; font-family: Segoe UI, sans-serif;'>" +
                "<h3 style='margin-bottom: 4px;'>Contact %s regarding <b>%s</b></h3>" +
                "<p style='color: #52525B;'>Use the contact channels below to report a match, sighting, or claim:</p>" +
                "<div style='background: #F4F4F5; padding: 10px; border-radius: 6px;'>" +
                "<b>Email:</b> %s<br>" +
                "<b>Phone:</b> %s<br>" +
                "<b>Tracking ID:</b> %s" +
                "</div>" +
                "</body></html>",
                authorName,
                item.getName(),
                (user != null && !user.getEmail().isEmpty()) ? user.getEmail() : "None provided",
                (user != null && !user.getPhone().isEmpty()) ? user.getPhone() : "None provided",
                item.getId()
        );

        JOptionPane.showMessageDialog(this, message, "Contact " + authorName, JOptionPane.INFORMATION_MESSAGE);
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private void refreshFeed() {
        controller.refreshView();
    }

    /**
     * Circular social media avatar component.
     */
    private static class AvatarIcon extends JComponent {
        private final String text;
        private final Color bg;

        public AvatarIcon(String text, Color bg) {
            this.text = text;
            this.bg = bg;
            setPreferredSize(new Dimension(42, 42));
            setMinimumSize(new Dimension(42, 42));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(bg);
            g2.fill(new Ellipse2D.Float(1, 1, 40, 40));

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (42 - fm.stringWidth(text)) / 2;
            int ty = ((42 - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(text, tx, ty);

            g2.dispose();
        }
    }
}

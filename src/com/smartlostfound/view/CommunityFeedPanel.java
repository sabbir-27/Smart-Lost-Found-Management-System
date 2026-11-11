package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Facebook-style Community Social Feed view for the Smart Lost &amp; Found System.
 * Features centered social post cards, photorealistic preview banners,
 * interactive sighting tips &amp; comment threads, and reaction counters.
 */
public class CommunityFeedPanel extends JPanel {

    private final LostFoundController controller;

    // Reactions state
    private final Map<String, Integer> helpfulReactions = new HashMap<>();
    private final Map<String, Boolean> userHasReacted = new HashMap<>();

    // Comments state
    private final Map<String, List<FeedComment>> postComments = new HashMap<>();

    private JPanel feedPostsContainer;
    private JTextField feedSearchField;
    private String currentFeedFilter = "ALL"; // ALL, LOST, FOUND, REWARD, TIPS

    public CommunityFeedPanel(LostFoundController controller) {
        this.controller = controller;
        seedInitialComments();
        initComponents();
    }

    /**
     * Pre-populates realistic community sighting tips so the social feed is active immediately.
     */
    private void seedInitialComments() {
        // LOST-1001 MacBook Pro
        List<FeedComment> macComments = new ArrayList<>();
        macComments.add(new FeedComment("Officer Davis", "OD",
                "Campus security sweep completed at 4 PM. Checked locker #4 at main desk.", "2h ago"));
        macComments.add(new FeedComment("David Miller", "DM",
                "Study rooms on 2nd floor library were wiped down around 4:30 PM. Might want to ask the custodian.", "45m ago"));
        postComments.put("LOST-1001", macComments);

        // FND-2001 Osprey Backpack
        List<FeedComment> bagComments = new ArrayList<>();
        bagComments.add(new FeedComment("Sarah Jenkins", "SJ",
                "Saw a student near gym bleachers looking around anxiously this morning!", "1h ago"));
        postComments.put("FND-2001", bagComments);

        // LOST-1002 Leather Wallet
        List<FeedComment> walletComments = new ArrayList<>();
        walletComments.add(new FeedComment("Alex Reed", "AR",
                "The barista at Starbucks mentioned someone turned in a cardholder earlier.", "3h ago"));
        postComments.put("LOST-1002", walletComments);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.COLOR_BACKGROUND);

        // 1. Top Section: Composer Card & Filter Bar (Centered column)
        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setOpaque(false);
        topWrapper.setBorder(new EmptyBorder(16, 20, 10, 20));

        JPanel centeredTopHeader = new JPanel();
        centeredTopHeader.setLayout(new BoxLayout(centeredTopHeader, BoxLayout.Y_AXIS));
        centeredTopHeader.setOpaque(false);
        centeredTopHeader.setMaximumSize(new Dimension(740, Integer.MAX_VALUE));
        centeredTopHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        centeredTopHeader.add(createPostComposerCard());
        centeredTopHeader.add(Box.createVerticalStrut(12));
        centeredTopHeader.add(createFeedFilterBar());

        topWrapper.add(centeredTopHeader);
        add(topWrapper, BorderLayout.NORTH);

        // 2. Center: Scrollable Feed Posts Container (Horizontally Centered)
        feedPostsContainer = new JPanel();
        feedPostsContainer.setLayout(new BoxLayout(feedPostsContainer, BoxLayout.Y_AXIS));
        feedPostsContainer.setBackground(UIUtils.COLOR_BACKGROUND);
        feedPostsContainer.setBorder(new EmptyBorder(6, 20, 30, 20));

        JScrollPane scrollPane = new JScrollPane(feedPostsContainer);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getViewport().setBackground(UIUtils.COLOR_BACKGROUND);

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Facebook-style Post Composer Card.
     */
    private JPanel createPostComposerCard() {
        JPanel card = UIUtils.createCardPanel(12);
        card.setLayout(new BorderLayout(14, 10));
        card.setMaximumSize(new Dimension(740, 76));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Left Prompt
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        UserAccount current = controller.getCurrentUser();
        String initials = current != null ? current.getInitials() : "ME";
        AvatarIcon avatar = new AvatarIcon(initials, current != null && current.isAdmin() ? UIUtils.COLOR_PRIMARY : new Color(39, 39, 42));

        JLabel prompt = new JLabel("Lost an item or discovered property? Broadcast it to the community...");
        prompt.setFont(UIUtils.FONT_BODY);
        prompt.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        left.add(avatar);
        left.add(prompt);
        card.add(left, BorderLayout.WEST);

        // Right Quick Action Buttons
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightButtons.setOpaque(false);

        JButton postLostBtn = UIUtils.createPrimaryButton("▲ Report Lost");
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
        bar.setMaximumSize(new Dimension(740, 40));
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel pillGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pillGroup.setOpaque(false);

        pillGroup.add(createFilterChip("All Posts", "ALL"));
        pillGroup.add(createFilterChip("Lost Only", "LOST"));
        pillGroup.add(createFilterChip("Found Only", "FOUND"));
        pillGroup.add(createFilterChip("🏆 With Reward", "REWARD"));

        bar.add(pillGroup, BorderLayout.WEST);

        // Feed keyword search
        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchWrapper.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 12));

        feedSearchField = UIUtils.createStyledTextField(16);
        feedSearchField.setToolTipText("Filter feed by keyword or item title...");
        feedSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshFeed(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshFeed(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshFeed(); }
        });

        searchWrapper.add(searchIcon);
        searchWrapper.add(feedSearchField);
        bar.add(searchWrapper, BorderLayout.EAST);

        return bar;
    }

    private JButton createFilterChip(String text, String filterKey) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean selected = filterKey.equals(currentFeedFilter);
                if (selected) {
                    g2.setColor(UIUtils.COLOR_CHARCOAL_BTN);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                    g2.setColor(UIUtils.COLOR_BORDER);
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 20, 20));
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UIUtils.FONT_SMALL_BOLD);
        btn.setForeground(filterKey.equals(currentFeedFilter) ? Color.WHITE : UIUtils.COLOR_TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));

        btn.addActionListener(e -> {
            this.currentFeedFilter = filterKey;
            Container parent = btn.getParent();
            if (parent != null) {
                for (Component c : parent.getComponents()) {
                    if (c instanceof JButton) {
                        ((JButton) c).setForeground(c == btn ? Color.WHITE : UIUtils.COLOR_TEXT_PRIMARY);
                        c.repaint();
                    }
                }
            }
            refreshFeed();
        });

        return btn;
    }

    public void displayFeedItems(List<Item> items) {
        feedPostsContainer.removeAll();

        String keyword = feedSearchField != null ? feedSearchField.getText().trim().toLowerCase() : "";

        int renderedCount = 0;
        for (Item item : items) {
            if ("LOST".equals(currentFeedFilter) && !(item instanceof LostItem)) continue;
            if ("FOUND".equals(currentFeedFilter) && !(item instanceof FoundItem)) continue;
            if ("REWARD".equals(currentFeedFilter)) {
                if (!(item instanceof LostItem) || ((LostItem) item).getRewardAmount() <= 0.0) continue;
            }

            if (!keyword.isEmpty() && !item.matchesKeyword(keyword)) continue;

            feedPostsContainer.add(createPostCard(item));
            feedPostsContainer.add(Box.createVerticalStrut(18));
            renderedCount++;
        }

        if (renderedCount == 0) {
            feedPostsContainer.add(createEmptyFeedPlaceholder());
        }

        feedPostsContainer.revalidate();
        feedPostsContainer.repaint();
    }

    private JPanel createEmptyFeedPlaceholder() {
        JPanel placeholder = UIUtils.createCardPanel(12);
        placeholder.setLayout(new GridBagLayout());
        placeholder.setMaximumSize(new Dimension(740, 160));
        placeholder.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel icon = new JLabel("🔍");
        icon.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 36));

        JLabel msg = new JLabel("No matching property reports found in the community feed.");
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
        JPanel postCard = UIUtils.createCardPanel(14);
        postCard.setLayout(new BorderLayout(0, 12));
        postCard.setMaximumSize(new Dimension(740, Integer.MAX_VALUE));
        postCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        boolean isLost = item instanceof LostItem;
        User user = item.getReporter();
        String authorName = (user != null && !user.getName().isEmpty()) ? user.getName() : "Community Member";
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
        statusBadge.setFont(UIUtils.FONT_BADGE);
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

        // Item Title (Clickable link to full details)
        JLabel titleLabel = new JLabel((isLost ? "⚠️ LOST: " : "📦 FOUND: ") + item.getName());
        titleLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 16));
        titleLabel.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
        titleLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        titleLabel.setToolTipText("Click to view complete property record and custodian details");
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.openItemDetailDialog(item, SwingUtilities.getWindowAncestor(CommunityFeedPanel.this));
            }
        });
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
                JLabel rewardChip = new JLabel(String.format("🏆 CASH REWARD: $%.2f", lost.getRewardAmount()));
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

        // Item Picture / Visual Photo Banner (Responsive 16:9 - auto-fits card width on all devices)
        ResponsivePhotoCard photoCard = new ResponsivePhotoCard(item);
        photoCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        bodyPanel.add(photoCard);

        postCard.add(bodyPanel, BorderLayout.CENTER);

        // 3. Post Footer Actions & Comments Thread
        JPanel footerSection = new JPanel();
        footerSection.setLayout(new BoxLayout(footerSection, BoxLayout.Y_AXIS));
        footerSection.setOpaque(false);
        footerSection.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(8, 0, 0, 0)
        ));

        // Reaction Bar
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setOpaque(false);

        // Reaction Counter
        int defaultCount = isLost ? 3 : 1;
        int currentReactions = helpfulReactions.getOrDefault(item.getId(), defaultCount);
        boolean reacted = userHasReacted.getOrDefault(item.getId(), false);

        JLabel reactionCountLabel = new JLabel(String.format("👍 %d keeping an eye out", currentReactions));
        reactionCountLabel.setFont(UIUtils.FONT_SMALL);
        reactionCountLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);
        actionBar.add(reactionCountLabel, BorderLayout.WEST);

        // Action Buttons (Facebook style)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton helpfulBtn = UIUtils.createModernButton(reacted ? "✓ Looking" : "👍 Look Out",
                reacted ? UIUtils.COLOR_PRIMARY_LIGHT : new Color(244, 244, 246),
                new Color(228, 228, 232),
                reacted ? UIUtils.COLOR_PRIMARY : UIUtils.COLOR_TEXT_PRIMARY, 6);
        helpfulBtn.setFont(UIUtils.FONT_SMALL_BOLD);
        helpfulBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
        helpfulBtn.addActionListener(e -> {
            boolean has = userHasReacted.getOrDefault(item.getId(), false);
            int cnt = helpfulReactions.getOrDefault(item.getId(), defaultCount);
            if (!has) {
                cnt++;
                userHasReacted.put(item.getId(), true);
                helpfulReactions.put(item.getId(), cnt);
                helpfulBtn.setText("✓ Looking");
                helpfulBtn.setBackground(UIUtils.COLOR_PRIMARY_LIGHT);
                helpfulBtn.setForeground(UIUtils.COLOR_PRIMARY);
            } else {
                cnt--;
                userHasReacted.put(item.getId(), false);
                helpfulReactions.put(item.getId(), cnt);
                helpfulBtn.setText("👍 Look Out");
                helpfulBtn.setBackground(new Color(244, 244, 246));
                helpfulBtn.setForeground(UIUtils.COLOR_TEXT_PRIMARY);
            }
            reactionCountLabel.setText(String.format("👍 %d keeping an eye out", cnt));
        });

        JButton contactBtn = UIUtils.createModernButton(isLost ? "💬 Contact Owner" : "💬 Contact Finder",
                UIUtils.COLOR_PRIMARY_LIGHT, UIUtils.COLOR_PRIMARY_BORDER, UIUtils.COLOR_PRIMARY, 6);
        contactBtn.setFont(UIUtils.FONT_SMALL_BOLD);
        contactBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
        contactBtn.addActionListener(e -> openContactDialog(item, authorName, user));

        JButton detailsBtn = UIUtils.createModernButton("Full Post ➔", Color.WHITE,
                UIUtils.COLOR_PRIMARY_LIGHT, UIUtils.COLOR_PRIMARY, 6);
        detailsBtn.setFont(UIUtils.FONT_SMALL_BOLD);
        detailsBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
        detailsBtn.addActionListener(e -> controller.openItemDetailDialog(item, SwingUtilities.getWindowAncestor(this)));

        actions.add(helpfulBtn);
        actions.add(contactBtn);
        actions.add(detailsBtn);
        actionBar.add(actions, BorderLayout.EAST);

        footerSection.add(actionBar);
        footerSection.add(Box.createVerticalStrut(10));

        // 4. Interactive Sighting Comments Thread (Facebook Style)
        footerSection.add(createCommentsThreadPanel(item));

        postCard.add(footerSection, BorderLayout.SOUTH);
        return postCard;
    }

    /**
     * Creates an interactive, expandable Facebook-style comments section.
     */
    private JPanel createCommentsThreadPanel(Item item) {
        JPanel threadPanel = new JPanel();
        threadPanel.setLayout(new BoxLayout(threadPanel, BoxLayout.Y_AXIS));
        threadPanel.setBackground(new Color(250, 250, 252));
        threadPanel.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.COLOR_BORDER_LIGHT, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        // Comments List Container
        JPanel commentList = new JPanel();
        commentList.setLayout(new BoxLayout(commentList, BoxLayout.Y_AXIS));
        commentList.setOpaque(false);

        List<FeedComment> comments = postComments.computeIfAbsent(item.getId(), k -> new ArrayList<>());
        for (FeedComment c : comments) {
            commentList.add(createCommentBubble(c));
            commentList.add(Box.createVerticalStrut(6));
        }

        threadPanel.add(commentList);
        threadPanel.add(Box.createVerticalStrut(8));

        // Add Comment Input Box
        JPanel addCommentRow = new JPanel(new BorderLayout(8, 0));
        addCommentRow.setOpaque(false);

        UserAccount current = controller.getCurrentUser();
        String myInitials = current != null ? current.getInitials() : "ME";
        AvatarIcon myAvatar = new AvatarIcon(myInitials, current != null && current.isAdmin() ? UIUtils.COLOR_PRIMARY : new Color(39, 39, 42));
        myAvatar.setPreferredSize(new Dimension(30, 30));

        JTextField commentField = new JTextField();
        commentField.setFont(UIUtils.FONT_BODY);
        commentField.putClientProperty("JTextField.placeholderText", "Write a sighting tip or comment... (Press Enter)");
        commentField.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.COLOR_BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        JButton postCommentBtn = UIUtils.createPrimaryButton("Post");
        postCommentBtn.setFont(UIUtils.FONT_SMALL_BOLD);
        postCommentBtn.setBorder(new EmptyBorder(6, 12, 6, 12));

        Runnable submitComment = () -> {
            String text = commentField.getText().trim();
            if (!text.isEmpty()) {
                String author = current != null ? current.getName() : "Community Member";
                String inits = current != null ? current.getInitials() : "CM";
                FeedComment newComment = new FeedComment(author, inits, text, "Just now");
                comments.add(newComment);
                commentList.add(createCommentBubble(newComment));
                commentList.add(Box.createVerticalStrut(6));
                commentField.setText("");
                threadPanel.revalidate();
                threadPanel.repaint();
            }
        };

        commentField.addActionListener(e -> submitComment.run());
        postCommentBtn.addActionListener(e -> submitComment.run());

        addCommentRow.add(myAvatar, BorderLayout.WEST);
        addCommentRow.add(commentField, BorderLayout.CENTER);
        addCommentRow.add(postCommentBtn, BorderLayout.EAST);

        threadPanel.add(addCommentRow);
        return threadPanel;
    }

    private JPanel createCommentBubble(FeedComment comment) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        AvatarIcon avatar = new AvatarIcon(comment.getInitials(), new Color(71, 85, 105));
        avatar.setPreferredSize(new Dimension(30, 30));
        row.add(avatar, BorderLayout.WEST);

        JPanel bubble = new JPanel(new BorderLayout(0, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(new EmptyBorder(6, 10, 6, 10));

        JPanel topMeta = new JPanel(new BorderLayout());
        topMeta.setOpaque(false);

        JLabel authorLabel = new JLabel(comment.getAuthorName());
        authorLabel.setFont(UIUtils.FONT_SMALL_BOLD);
        authorLabel.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        JLabel timeLabel = new JLabel(comment.getTimestamp());
        timeLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 10));
        timeLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        topMeta.add(authorLabel, BorderLayout.WEST);
        topMeta.add(timeLabel, BorderLayout.EAST);

        JLabel textLabel = new JLabel("<html>" + comment.getText() + "</html>");
        textLabel.setFont(UIUtils.FONT_BODY);
        textLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        bubble.add(topMeta, BorderLayout.NORTH);
        bubble.add(textLabel, BorderLayout.CENTER);

        row.add(bubble, BorderLayout.CENTER);
        return row;
    }

    private void openContactDialog(Item item, String authorName, User user) {
        String message = String.format(
                "<html><body style='width: 320px; font-family: " + UIUtils.FONT_FAMILY + ", sans-serif;'>" +
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
     * Data record representing a social comment or sighting tip.
     */
    public static class FeedComment {
        private final String authorName;
        private final String initials;
        private final String text;
        private final String timestamp;

        public FeedComment(String authorName, String initials, String text, String timestamp) {
            this.authorName = authorName;
            this.initials = initials;
            this.text = text;
            this.timestamp = timestamp;
        }

        public String getAuthorName() {
            return authorName;
        }

        public String getInitials() {
            return initials;
        }

        public String getText() {
            return text;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Circular social media avatar component with anti-aliasing.
     */
    private static class AvatarIcon extends JComponent {
        private final String text;
        private final Color bg;

        public AvatarIcon(String text, Color bg) {
            this.text = text;
            this.bg = bg;
            setPreferredSize(new Dimension(38, 38));
            setMinimumSize(new Dimension(38, 38));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int d = Math.min(getWidth(), getHeight()) - 2;
            g2.setColor(bg);
            g2.fill(new Ellipse2D.Float(1, 1, d, d));

            // Inner highlight ring
            g2.setColor(new Color(255, 255, 255, 80));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new Ellipse2D.Float(1.5f, 1.5f, d - 1, d - 1));

            g2.setColor(Color.WHITE);
            g2.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(text)) / 2;
            int ty = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(text, tx, ty);

            g2.dispose();
        }
    }

    /**
     * Dynamically responsive 16:9 photo container that automatically scales
     * to fit the exact width of the social post card on any device or display resolution.
     */
    public static class ResponsivePhotoCard extends JPanel {
        private final BufferedImage original;
        private final Item item;
        private BufferedImage cachedScaled;
        private int cachedW = -1;
        private int cachedH = -1;

        public ResponsivePhotoCard(Item item) {
            this.item = item;
            this.original = ImageHelper.getOriginalImage(item);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("Click to view full photo in high-resolution lightbox");

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    new PhotoLightboxDialog(SwingUtilities.getWindowAncestor(ResponsivePhotoCard.this), item).setVisible(true);
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            int w = 680;
            Container p = getParent();
            if (p != null && p.getWidth() > 0) {
                w = Math.min(700, Math.max(260, p.getWidth() - 8));
            }
            int h = Math.max(160, (int) Math.round(w * 9.0 / 16.0));
            return new Dimension(w, h);
        }

        @Override
        public Dimension getMaximumSize() {
            int w = 700;
            int h = (int) Math.round(w * 9.0 / 16.0);
            return new Dimension(w, h);
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(240, 135);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            if (cachedScaled == null || cachedW != w || cachedH != h) {
                cachedScaled = ImageHelper.scaleAndCrop(original, w, h, 10);
                cachedW = w;
                cachedH = h;
            }

            g.drawImage(cachedScaled, 0, 0, null);
        }
    }
}

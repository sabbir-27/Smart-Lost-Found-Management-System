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

/**
 * Enterprise inspection modal dialog providing comprehensive record intelligence,
 * custody status, and reporter verification channels.
 */
public class ItemDetailDialog extends JDialog {

    private final Item item;
    private final LostFoundController controller;

    public ItemDetailDialog(Window owner, Item item, LostFoundController controller) {
        super(owner, "Record Intelligence - " + item.getId(), ModalityType.APPLICATION_MODAL);
        this.item = item;
        this.controller = controller;

        initComponents();
        setSize(680, 590);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.COLOR_BACKGROUND);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIUtils.COLOR_SIDEBAR_BG);
        headerPanel.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel headerLeft = new JPanel(new GridLayout(2, 1, 2, 4));
        headerLeft.setOpaque(false);

        JLabel typeAndIdLabel = new JLabel(item.getItemType().toUpperCase() + " PROPERTY  •  " + item.getId());
        typeAndIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        typeAndIdLabel.setForeground(item instanceof LostItem ? new Color(248, 113, 113) : new Color(147, 197, 253));

        JLabel titleLabel = new JLabel(item.getName());
        titleLabel.setFont(UIUtils.FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);

        headerLeft.add(typeAndIdLabel);
        headerLeft.add(titleLabel);

        // Status Badge in header
        JPanel statusBadge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        statusBadge.setOpaque(false);
        JLabel statusLabel = new JLabel("  " + item.getStatus().getDisplayName().toUpperCase() + "  ");
        statusLabel.setOpaque(true);
        statusLabel.setBackground(item.getStatus().getBackgroundColor());
        statusLabel.setForeground(item.getStatus().getForegroundColor());
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setBorder(new CompoundBorder(
                new LineBorder(item.getStatus().getForegroundColor(), 1, true),
                new EmptyBorder(4, 12, 4, 12)
        ));
        statusBadge.add(statusLabel);

        headerPanel.add(headerLeft, BorderLayout.CENTER);
        headerPanel.add(statusBadge, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content Scroll Pane
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Section 1: Item Attributes Card
        JPanel infoCard = UIUtils.createCardPanel(10);
        infoCard.setLayout(new GridLayout(0, 2, 14, 10));

        addDetailRow(infoCard, "Category:", item.getCategory().getDisplayName());
        addDetailRow(infoCard, "Date " + (item instanceof LostItem ? "Lost" : "Discovered") + ":", item.getFormattedDate());
        addDetailRow(infoCard, "Location " + (item instanceof LostItem ? "Lost" : "Found") + ":", item.getSpecificLocation());

        if (item instanceof LostItem) {
            LostItem lost = (LostItem) item;
            addDetailRow(infoCard, "Reward Offered:", lost.getFormattedReward());
        } else if (item instanceof FoundItem) {
            FoundItem found = (FoundItem) item;
            addDetailRow(infoCard, "Custody Storage:", found.getStorageLocation());
            if (!found.getFinderContactInfo().isEmpty()) {
                addDetailRow(infoCard, "Finder Notes:", found.getFinderContactInfo());
            }
        }

        addDetailRow(infoCard, "System Registered:", item.getFormattedCreatedAt());

        contentPanel.add(infoCard);
        contentPanel.add(Box.createVerticalStrut(14));

        // Section 2: Narrative Description Card
        JPanel descCard = UIUtils.createCardPanel(10);
        descCard.setLayout(new BorderLayout(5, 8));
        JLabel descTitle = UIUtils.createFormLabel("Detailed Narrative & Distinct Identifiers");
        JTextArea descArea = new JTextArea(item.getDescription());
        descArea.setFont(UIUtils.FONT_BODY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBackground(new Color(248, 250, 252));
        descArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(500, 80));
        descScroll.setBorder(new LineBorder(UIUtils.COLOR_BORDER, 1));

        descCard.add(descTitle, BorderLayout.NORTH);
        descCard.add(descScroll, BorderLayout.CENTER);

        contentPanel.add(descCard);
        contentPanel.add(Box.createVerticalStrut(14));

        // Section 3: Contact Details Card
        JPanel contactCard = UIUtils.createCardPanel(10);
        contactCard.setLayout(new GridLayout(0, 2, 14, 8));
        JLabel contactTitle = UIUtils.createFormLabel(
                item instanceof LostItem ? "Owner Verification & Contact" : "Finder / Officer Contact");

        User user = item.getReporter();
        addDetailRow(contactCard, "Full Name:", (user != null && !user.getName().isEmpty()) ? user.getName() : "Anonymous");
        addDetailRow(contactCard, "Email:", (user != null && !user.getEmail().isEmpty()) ? user.getEmail() : "None provided");
        addDetailRow(contactCard, "Phone:", (user != null && !user.getPhone().isEmpty()) ? user.getPhone() : "None provided");

        JPanel contactWrapper = UIUtils.createCardPanel(10);
        contactWrapper.setLayout(new BorderLayout(5, 8));
        contactWrapper.add(contactTitle, BorderLayout.NORTH);
        contactWrapper.add(contactCard, BorderLayout.CENTER);

        contentPanel.add(contactWrapper);

        // Section 4: Resolution Notes (if any)
        if (item.getResolutionNotes() != null && !item.getResolutionNotes().isEmpty()) {
            contentPanel.add(Box.createVerticalStrut(14));
            JPanel notesCard = UIUtils.createCardPanel(10);
            notesCard.setLayout(new BorderLayout(5, 6));
            JLabel notesTitle = UIUtils.createFormLabel("Lifecycle & Resolution Audit History");
            JTextArea notesArea = new JTextArea(item.getResolutionNotes());
            notesArea.setFont(UIUtils.FONT_BODY);
            notesArea.setEditable(false);
            notesArea.setBackground(new Color(240, 253, 244));
            notesArea.setBorder(new EmptyBorder(8, 8, 8, 8));
            notesCard.add(notesTitle, BorderLayout.NORTH);
            notesCard.add(notesArea, BorderLayout.CENTER);
            contentPanel.add(notesCard);
        }

        JScrollPane mainScroll = new JScrollPane(contentPanel);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(14);
        add(mainScroll, BorderLayout.CENTER);

        // Footer Action Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(8, 20, 8, 20)
        ));

        JButton updateStatusBtn = UIUtils.createPrimaryButton("Update Status");
        updateStatusBtn.addActionListener(e -> {
            Window parentWindow = this.getOwner();
            dispose();
            controller.openUpdateStatusDialog(item, parentWindow);
        });

        JButton closeBtn = UIUtils.createSecondaryButton("Close Window");
        closeBtn.addActionListener(e -> dispose());

        footerPanel.add(updateStatusBtn);
        footerPanel.add(closeBtn);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void addDetailRow(JPanel panel, String labelText, String valueText) {
        JLabel label = new JLabel(labelText);
        label.setFont(UIUtils.FONT_BODY_BOLD);
        label.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

        JLabel val = new JLabel((valueText != null && !valueText.isEmpty()) ? valueText : "—");
        val.setFont(UIUtils.FONT_BODY);
        val.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

        panel.add(label);
        panel.add(val);
    }
}

package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.Item;
import com.smartlostfound.model.ItemStatus;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Enterprise modal dialog for updating an item's operational status
 * and entering verification/claimer audit remarks.
 */
public class UpdateStatusDialog extends JDialog {

    private final Item item;
    private final LostFoundController controller;

    private JComboBox<ItemStatus> statusComboBox;
    private JTextArea notesTextArea;

    public UpdateStatusDialog(Window owner, Item item, LostFoundController controller) {
        super(owner, "Update Status - " + item.getId(), ModalityType.APPLICATION_MODAL);
        this.item = item;
        this.controller = controller;

        initComponents();
        GraphicsConfiguration gc = getGraphicsConfiguration();
        Rectangle screen = gc != null ? gc.getBounds() : new Rectangle(0, 0, 1280, 800);
        setSize(Math.min(540, screen.width - 40), Math.min(460, screen.height - 40));
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.COLOR_BACKGROUND);

        // Header Panel with Obsidian Gradient
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UIUtils.COLOR_SIDEBAR_TOP, 0, getHeight(), UIUtils.COLOR_SIDEBAR_BOTTOM);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(16, 22, 16, 22));

        JLabel titleLabel = new JLabel("Transition Item Status");
        titleLabel.setFont(UIUtils.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        JLabel itemSubtitle = new JLabel(item.getId() + " • " + item.getName());
        itemSubtitle.setFont(UIUtils.FONT_SUBTITLE);
        itemSubtitle.setForeground(new Color(203, 213, 225));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(itemSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Body Panel inside Card
        JPanel bodyWrapper = new JPanel(new BorderLayout());
        bodyWrapper.setBackground(UIUtils.COLOR_BACKGROUND);
        bodyWrapper.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel formCard = UIUtils.createCardPanel(12);
        formCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;

        // Current Status display
        formCard.add(UIUtils.createFormLabel("Current Status:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        JLabel currentStatusLabel = new JLabel("● " + item.getStatus().getDisplayName().toUpperCase());
        currentStatusLabel.setFont(UIUtils.FONT_BODY_BOLD);
        currentStatusLabel.setForeground(item.getStatus().getForegroundColor());
        formCard.add(currentStatusLabel, gbc);

        // New Status selector
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.35;
        formCard.add(UIUtils.createRequiredFormLabel("New Status:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        statusComboBox = new JComboBox<>(ItemStatus.values());
        statusComboBox.setSelectedItem(suggestNextStatus(item.getStatus()));
        statusComboBox.setFont(UIUtils.FONT_BODY);
        statusComboBox.setBackground(Color.WHITE);
        formCard.add(statusComboBox, gbc);

        // Resolution Notes
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(14, 8, 4, 8);
        formCard.add(UIUtils.createFormLabel("Resolution / Claim Verification Remarks:"), gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(4, 8, 8, 8);
        notesTextArea = UIUtils.createStyledTextArea(4, 25);
        if (item.getResolutionNotes() != null) {
            notesTextArea.setText(item.getResolutionNotes());
        }
        JScrollPane notesScroll = new JScrollPane(notesTextArea);
        notesScroll.setBorder(new LineBorder(UIUtils.COLOR_BORDER, 1));
        formCard.add(notesScroll, gbc);

        bodyWrapper.add(formCard, BorderLayout.CENTER);
        add(bodyWrapper, BorderLayout.CENTER);

        // Footer Action Buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(8, 20, 8, 20)
        ));

        JButton cancelBtn = UIUtils.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = UIUtils.createPrimaryButton("Save Status Update");
        saveBtn.addActionListener(e -> saveStatus());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private ItemStatus suggestNextStatus(ItemStatus current) {
        switch (current) {
            case LOST: return ItemStatus.RECOVERED;
            case FOUND: return ItemStatus.CLAIMED;
            default: return current;
        }
    }

    private void saveStatus() {
        ItemStatus newStatus = (ItemStatus) statusComboBox.getSelectedItem();
        String notes = notesTextArea.getText().trim();

        if (newStatus == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a target status.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        controller.handleUpdateStatus(item.getId(), newStatus, notes, this);
    }
}

package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.Item;
import com.smartlostfound.model.ItemCategory;
import com.smartlostfound.model.User;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Intuitive form view for submitting a new Lost Item report.
 * Features comprehensive validation, clear sectioning, and helpful inline guides.
 */
public class ReportLostItemPanel extends JPanel {

    private final LostFoundController controller;

    private JTextField nameField;
    private JComboBox<ItemCategory> categoryComboBox;
    private JTextField dateField;
    private JTextField locationField;
    private JTextField rewardField;
    private JTextArea descriptionArea;

    private JTextField reporterNameField;
    private JTextField reporterEmailField;
    private JTextField reporterPhoneField;

    public ReportLostItemPanel(LostFoundController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UIUtils.COLOR_BACKGROUND);

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIUtils.COLOR_HEADER_BG);
        headerPanel.setBorder(new EmptyBorder(18, 28, 18, 28));

        JLabel titleLabel = new JLabel("Report a Lost Item");
        titleLabel.setFont(UIUtils.FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Log details of an item you have lost to assist custodians in identifying and returning it to you.");
        subtitleLabel.setFont(UIUtils.FONT_SUBTITLE);
        subtitleLabel.setForeground(new Color(203, 213, 225));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form Container inside ScrollPane
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(UIUtils.COLOR_BACKGROUND);

        JPanel formCard = UIUtils.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        formCard.setPreferredSize(new Dimension(720, 620));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Section 1 Header: Item Details
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        JLabel itemSectionTitle = new JLabel("1. Lost Item Details");
        itemSectionTitle.setFont(UIUtils.FONT_TITLE);
        itemSectionTitle.setForeground(UIUtils.COLOR_PRIMARY);
        formCard.add(itemSectionTitle, gbc);

        // Item Name
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createRequiredFormLabel("Item Name / Title:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        nameField = UIUtils.createStyledTextField(25);
        formCard.add(nameField, gbc);
        row++;

        // Category
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createRequiredFormLabel("Category:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        categoryComboBox = new JComboBox<>(ItemCategory.values());
        categoryComboBox.setFont(UIUtils.FONT_BODY);
        formCard.add(categoryComboBox, gbc);
        row++;

        // Date Lost
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createRequiredFormLabel("Date Lost (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        dateField = UIUtils.createStyledTextField(15);
        dateField.setText(LocalDate.now().format(Item.DATE_FORMATTER));
        formCard.add(dateField, gbc);
        row++;

        // Location Lost
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createRequiredFormLabel("Location Lost:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        locationField = UIUtils.createStyledTextField(25);
        formCard.add(locationField, gbc);
        row++;

        // Reward Amount
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createFormLabel("Reward Amount ($ optional):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        rewardField = UIUtils.createStyledTextField(15);
        rewardField.setText("0.00");
        formCard.add(rewardField, gbc);
        row++;

        // Description
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formCard.add(UIUtils.createRequiredFormLabel("Description & Features:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.CENTER;
        descriptionArea = UIUtils.createStyledTextArea(4, 25);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(new LineBorder(UIUtils.COLOR_BORDER, 1));
        formCard.add(descScroll, gbc);
        row++;

        // Section 2 Header: Reporter Contact
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 8, 6, 8);
        JLabel reporterSectionTitle = new JLabel("2. Owner / Contact Information");
        reporterSectionTitle.setFont(UIUtils.FONT_TITLE);
        reporterSectionTitle.setForeground(UIUtils.COLOR_PRIMARY);
        formCard.add(reporterSectionTitle, gbc);

        // Reporter Name
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createRequiredFormLabel("Full Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        reporterNameField = UIUtils.createStyledTextField(25);
        formCard.add(reporterNameField, gbc);
        row++;

        // Reporter Email
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createFormLabel("Email Address:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        reporterEmailField = UIUtils.createStyledTextField(25);
        formCard.add(reporterEmailField, gbc);
        row++;

        // Reporter Phone
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createFormLabel("Phone Number:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        reporterPhoneField = UIUtils.createStyledTextField(25);
        formCard.add(reporterPhoneField, gbc);
        row++;

        // Action Buttons Row
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 8, 8, 8);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setOpaque(false);

        JButton resetBtn = UIUtils.createSecondaryButton("Reset Form");
        resetBtn.addActionListener(e -> resetForm());

        JButton submitBtn = UIUtils.createPrimaryButton("Submit Lost Item Report");
        submitBtn.addActionListener(e -> submitForm());

        buttonPanel.add(resetBtn);
        buttonPanel.add(submitBtn);
        formCard.add(buttonPanel, gbc);

        centerWrapper.add(formCard);

        JScrollPane scrollPane = new JScrollPane(centerWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void submitForm() {
        // Validation
        String name = nameField.getText().trim();
        if (name.length() < 2) {
            showValidationError("Please enter a valid item name (at least 2 characters).", nameField);
            return;
        }

        ItemCategory category = (ItemCategory) categoryComboBox.getSelectedItem();
        if (category == null) {
            showValidationError("Please select an item category.", categoryComboBox);
            return;
        }

        String dateStr = dateField.getText().trim();
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, Item.DATE_FORMATTER);
            if (date.isAfter(LocalDate.now())) {
                showValidationError("Date lost cannot be in the future.", dateField);
                return;
            }
        } catch (DateTimeParseException e) {
            showValidationError("Invalid date format. Please use YYYY-MM-DD (e.g. 2026-11-10).", dateField);
            return;
        }

        String location = locationField.getText().trim();
        if (location.isEmpty()) {
            showValidationError("Please enter the location where the item was lost.", locationField);
            return;
        }

        String rewardStr = rewardField.getText().trim();
        double reward = 0.0;
        if (!rewardStr.isEmpty()) {
            try {
                reward = Double.parseDouble(rewardStr);
                if (reward < 0.0) {
                    showValidationError("Reward amount cannot be negative.", rewardField);
                    return;
                }
            } catch (NumberFormatException e) {
                showValidationError("Reward must be a valid numeric amount (e.g. 25.00).", rewardField);
                return;
            }
        }

        String description = descriptionArea.getText().trim();
        if (description.length() < 5) {
            showValidationError("Please provide a descriptive explanation (at least 5 characters).", descriptionArea);
            return;
        }

        String reporterName = reporterNameField.getText().trim();
        if (reporterName.isEmpty()) {
            showValidationError("Please enter your name.", reporterNameField);
            return;
        }

        String email = reporterEmailField.getText().trim();
        String phone = reporterPhoneField.getText().trim();
        if (email.isEmpty() && phone.isEmpty()) {
            showValidationError("Please provide at least one contact method (email or phone number).", reporterEmailField);
            return;
        }

        User reporter = new User(reporterName, email, phone);

        // Forward to controller
        controller.handleReportLostItem(
                name, category, date, description, reporter, location, reward,
                SwingUtilities.getWindowAncestor(this),
                this::resetForm
        );
    }

    public void resetForm() {
        nameField.setText("");
        categoryComboBox.setSelectedIndex(0);
        dateField.setText(LocalDate.now().format(Item.DATE_FORMATTER));
        locationField.setText("");
        rewardField.setText("0.00");
        descriptionArea.setText("");
        reporterNameField.setText("");
        reporterEmailField.setText("");
        reporterPhoneField.setText("");
        nameField.requestFocusInWindow();
    }

    private void showValidationError(String message, JComponent targetComponent) {
        JOptionPane.showMessageDialog(this, message, "Input Validation Error", JOptionPane.WARNING_MESSAGE);
        targetComponent.requestFocusInWindow();
    }
}

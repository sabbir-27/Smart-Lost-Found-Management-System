package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.Item;
import com.smartlostfound.model.ItemCategory;
import com.smartlostfound.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Intuitive form view for logging a newly discovered (Found) item.
 * Captures discovery location, custody/storage location, and finder details with full validation.
 */
public class ReportFoundItemPanel extends JPanel {

    private final LostFoundController controller;

    private JTextField nameField;
    private JComboBox<ItemCategory> categoryComboBox;
    private JTextField dateField;
    private JTextField locationFoundField;
    private JTextField storageLocationField;
    private JTextArea descriptionArea;

    private JTextField finderNameField;
    private JTextField finderEmailField;
    private JTextField finderPhoneField;
    private JTextField finderNotesField;

    public ReportFoundItemPanel(LostFoundController controller) {
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

        JLabel titleLabel = new JLabel("Report a Found Item");
        titleLabel.setFont(UIUtils.FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Log an item turned in or discovered on premises to maintain custody and enable owner claims.");
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
        formCard.setPreferredSize(new Dimension(720, 660));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Section 1 Header: Found Item Details
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        JLabel itemSectionTitle = new JLabel("1. Discovered Item Information");
        itemSectionTitle.setFont(UIUtils.FONT_TITLE);
        itemSectionTitle.setForeground(new Color(13, 148, 136)); // Teal accent
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

        // Date Found
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createRequiredFormLabel("Date Discovered (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        dateField = UIUtils.createStyledTextField(15);
        dateField.setText(LocalDate.now().format(Item.DATE_FORMATTER));
        formCard.add(dateField, gbc);
        row++;

        // Location Found
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createRequiredFormLabel("Location Found:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        locationFoundField = UIUtils.createStyledTextField(25);
        formCard.add(locationFoundField, gbc);
        row++;

        // Custody / Storage Location
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createRequiredFormLabel("Holding / Storage Location:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        storageLocationField = UIUtils.createStyledTextField(25);
        storageLocationField.setToolTipText("e.g. Front Office Locker 3, Security Safe, Desk Drawer");
        formCard.add(storageLocationField, gbc);
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

        // Section 2 Header: Finder Information
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 8, 6, 8);
        JLabel finderSectionTitle = new JLabel("2. Finder / Custodian Information");
        finderSectionTitle.setFont(UIUtils.FONT_TITLE);
        finderSectionTitle.setForeground(new Color(13, 148, 136));
        formCard.add(finderSectionTitle, gbc);

        // Finder Name
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createRequiredFormLabel("Finder Full Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        finderNameField = UIUtils.createStyledTextField(25);
        formCard.add(finderNameField, gbc);
        row++;

        // Finder Email
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createFormLabel("Finder Email:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        finderEmailField = UIUtils.createStyledTextField(25);
        formCard.add(finderEmailField, gbc);
        row++;

        // Finder Phone
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createFormLabel("Finder Phone:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        finderPhoneField = UIUtils.createStyledTextField(25);
        formCard.add(finderPhoneField, gbc);
        row++;

        // Additional Custody Notes
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createFormLabel("Custody / Claim Notes:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        finderNotesField = UIUtils.createStyledTextField(25);
        finderNotesField.setToolTipText("e.g. Turned in by cleaning staff, requires ID proof");
        formCard.add(finderNotesField, gbc);
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

        JButton submitBtn = UIUtils.createStyledButton("Submit Found Item Report",
                new Color(13, 148, 136), new Color(15, 118, 110), Color.WHITE);
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
                showValidationError("Discovery date cannot be in the future.", dateField);
                return;
            }
        } catch (DateTimeParseException e) {
            showValidationError("Invalid date format. Please use YYYY-MM-DD (e.g. 2026-11-10).", dateField);
            return;
        }

        String locationFound = locationFoundField.getText().trim();
        if (locationFound.isEmpty()) {
            showValidationError("Please enter the location where the item was discovered.", locationFoundField);
            return;
        }

        String storageLocation = storageLocationField.getText().trim();
        if (storageLocation.isEmpty()) {
            showValidationError("Please specify where the item is currently kept for safe-keeping.", storageLocationField);
            return;
        }

        String description = descriptionArea.getText().trim();
        if (description.length() < 5) {
            showValidationError("Please enter a clear description (at least 5 characters).", descriptionArea);
            return;
        }

        String finderName = finderNameField.getText().trim();
        if (finderName.isEmpty()) {
            showValidationError("Please enter finder or officer name.", finderNameField);
            return;
        }

        String email = finderEmailField.getText().trim();
        String phone = finderPhoneField.getText().trim();
        if (email.isEmpty() && phone.isEmpty()) {
            showValidationError("Please provide at least one contact method for the finder.", finderEmailField);
            return;
        }

        String finderNotes = finderNotesField.getText().trim();
        User reporter = new User(finderName, email, phone);

        controller.handleReportFoundItem(
                name, category, date, description, reporter, locationFound,
                storageLocation, finderNotes,
                SwingUtilities.getWindowAncestor(this),
                this::resetForm
        );
    }

    public void resetForm() {
        nameField.setText("");
        categoryComboBox.setSelectedIndex(0);
        dateField.setText(LocalDate.now().format(Item.DATE_FORMATTER));
        locationFoundField.setText("");
        storageLocationField.setText("");
        descriptionArea.setText("");
        finderNameField.setText("");
        finderEmailField.setText("");
        finderPhoneField.setText("");
        finderNotesField.setText("");
        nameField.requestFocusInWindow();
    }

    private void showValidationError(String message, JComponent targetComponent) {
        JOptionPane.showMessageDialog(this, message, "Input Validation Error", JOptionPane.WARNING_MESSAGE);
        targetComponent.requestFocusInWindow();
    }
}

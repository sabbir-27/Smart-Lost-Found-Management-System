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
 * Enterprise form view for registering a newly lost item.
 * Incorporates focused input styling, clean field grouping, and robust validation.
 */
public class ReportLostItemPanel extends JPanel {

    private final LostFoundController controller;

    private JTextField nameField;
    private JComboBox<ItemCategory> categoryComboBox;
    private JTextField dateField;
    private JTextField locationField;
    private JTextField rewardField;
    private JTextArea descriptionArea;
    private JLabel photoStatusLabel;
    private String selectedImagePath = "";

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

        // Center Form inside ScrollPane
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(UIUtils.COLOR_BACKGROUND);
        centerWrapper.setBorder(new EmptyBorder(24, 20, 24, 20));

        JPanel formCard = UIUtils.createCardPanel(14);
        formCard.setLayout(new GridBagLayout());
        formCard.setPreferredSize(new Dimension(740, 640));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Section 1 Header
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 10, 10, 10);
        formCard.add(UIUtils.createSectionTitle("1", "Lost Item Specifications"), gbc);

        // Item Name
        gbc.insets = new Insets(5, 10, 5, 10);
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
        categoryComboBox.setBackground(Color.WHITE);
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
        formCard.add(UIUtils.createFormLabel("Reward Offered ($ optional):"), gbc);

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
        formCard.add(UIUtils.createRequiredFormLabel("Description & Identifiers:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.CENTER;
        descriptionArea = UIUtils.createStyledTextArea(3, 25);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(new LineBorder(UIUtils.COLOR_BORDER, 1));
        formCard.add(descScroll, gbc);
        row++;

        // Item Photo Attachment
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createFormLabel("Attach Item Photo:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        photoPanel.setOpaque(false);

        JButton browsePhotoBtn = UIUtils.createSecondaryButton("📷 Browse Photo...");
        browsePhotoBtn.addActionListener(e -> selectPhoto());

        photoStatusLabel = new JLabel("No photo attached (visual banner will be generated)");
        photoStatusLabel.setFont(UIUtils.FONT_SMALL);
        photoStatusLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        photoPanel.add(browsePhotoBtn);
        photoPanel.add(photoStatusLabel);
        formCard.add(photoPanel, gbc);
        row++;

        // Section 2 Header
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 10, 10, 10);
        formCard.add(UIUtils.createSectionTitle("2", "Owner & Verification Contact"), gbc);

        // Reporter Name
        gbc.insets = new Insets(5, 10, 5, 10);
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
        gbc.insets = new Insets(20, 10, 8, 10);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setOpaque(false);

        JButton resetBtn = UIUtils.createSecondaryButton("Clear Form");
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

        controller.handleReportLostItem(
                name, category, date, description, reporter, location, reward, selectedImagePath,
                SwingUtilities.getWindowAncestor(this),
                this::resetForm
        );
    }

    private void selectPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Lost Item Photo");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files (*.jpg, *.jpeg, *.png, *.webp, *.gif)", "jpg", "jpeg", "png", "webp", "gif"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImagePath = chooser.getSelectedFile().getAbsolutePath();
            photoStatusLabel.setText("✓ " + chooser.getSelectedFile().getName());
            photoStatusLabel.setForeground(UIUtils.COLOR_SUCCESS);
        }
    }

    public void autoFillReporter(User user) {
        if (user != null) {
            if (reporterNameField != null) {
                reporterNameField.setText(user.getName());
            }
            if (reporterEmailField != null) {
                reporterEmailField.setText(user.getEmail());
            }
            if (reporterPhoneField != null) {
                reporterPhoneField.setText(user.getPhone());
            }
        }
    }

    public void resetForm() {
        nameField.setText("");
        categoryComboBox.setSelectedIndex(0);
        dateField.setText(LocalDate.now().format(Item.DATE_FORMATTER));
        locationField.setText("");
        rewardField.setText("0.00");
        descriptionArea.setText("");
        selectedImagePath = "";
        if (photoStatusLabel != null) {
            photoStatusLabel.setText("No photo attached (visual banner will be generated)");
            photoStatusLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);
        }
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

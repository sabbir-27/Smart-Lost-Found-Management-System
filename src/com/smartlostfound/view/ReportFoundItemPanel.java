package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.FoundItem;
import com.smartlostfound.model.Item;
import com.smartlostfound.model.ItemCategory;
import com.smartlostfound.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Enterprise form view for logging property discovered on premises.
 * Includes custody/holding location specifications and finder verification contact channels.
 */
public class ReportFoundItemPanel extends JPanel {

    private final LostFoundController controller;

    private JTextField nameField;
    private JComboBox<ItemCategory> categoryComboBox;
    private JTextField dateField;
    private JTextField locationFoundField;
    private JTextField storageLocationField;
    private JTextArea descriptionArea;

    // Photo Upload & Preview UI
    private JPanel photoSectionContainer;
    private JLabel photoThumbnailLabel;
    private JLabel photoFileNameLabel;
    private JLabel photoSpecsLabel;
    private String selectedImagePath = "";

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

        // Center Form inside ScrollPane
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(UIUtils.COLOR_BACKGROUND);
        centerWrapper.setBorder(new EmptyBorder(24, 20, 24, 20));

        JPanel formCard = new UIUtils.ElevatedCardPanel(14) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                int targetW = 740;
                Container p = getParent();
                if (p != null && p.getWidth() > 0) {
                    int avail = p.getWidth() - 32;
                    d.width = Math.max(340, Math.min(targetW, avail));
                } else {
                    d.width = targetW;
                }
                return d;
            }
        };
        formCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Section 1 Header
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 10, 10, 10);
        formCard.add(UIUtils.createSectionTitle("1", "Discovered Property & Custody Location"), gbc);

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

        // Storage / Custody Location
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createRequiredFormLabel("Holding / Storage Location:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        storageLocationField = UIUtils.createStyledTextField(25);
        storageLocationField.setToolTipText("e.g. Front Office Locker 3, Security Safe, Vault Drawer #2");
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
        formCard.add(createPhotoAttachmentComponent(), gbc);
        row++;

        // Section 2 Header
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 10, 10, 10);
        formCard.add(UIUtils.createSectionTitle("2", "Finder / Custodian Contact Details"), gbc);

        // Finder Name
        gbc.insets = new Insets(5, 10, 5, 10);
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

        // Custody Notes
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formCard.add(UIUtils.createFormLabel("Custody Notes / Conditions:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        finderNotesField = UIUtils.createStyledTextField(25);
        finderNotesField.setToolTipText("e.g. Turned in by janitorial staff, requires ID proof");
        formCard.add(finderNotesField, gbc);
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

        JButton submitBtn = UIUtils.createModernButton("Submit Found Item Report",
                UIUtils.COLOR_TEAL, new Color(15, 118, 110), Color.WHITE, 8);
        submitBtn.addActionListener(e -> submitForm());

        buttonPanel.add(resetBtn);
        buttonPanel.add(submitBtn);
        formCard.add(buttonPanel, gbc);

        centerWrapper.add(formCard);
        centerWrapper.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                formCard.revalidate();
            }
        });

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
                storageLocation, finderNotes, selectedImagePath,
                SwingUtilities.getWindowAncestor(this),
                this::resetForm
        );
    }

    private JPanel createPhotoAttachmentComponent() {
        photoSectionContainer = new JPanel(new BorderLayout());
        photoSectionContainer.setOpaque(false);
        renderPhotoSection();
        return photoSectionContainer;
    }

    private void renderPhotoSection() {
        if (photoSectionContainer == null) return;
        photoSectionContainer.removeAll();

        if (selectedImagePath == null || selectedImagePath.trim().isEmpty()) {
            // Elegant Upload Dropzone Card
            JPanel dropzone = new JPanel(new BorderLayout(12, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(248, 250, 252));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(new Color(203, 213, 225));
                    float[] dash = {4f, 4f};
                    g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            dropzone.setOpaque(false);
            dropzone.setBorder(new EmptyBorder(12, 16, 12, 16));
            dropzone.setCursor(new Cursor(Cursor.HAND_CURSOR));
            dropzone.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectPhoto();
                }
            });

            JPanel textCol = new JPanel(new GridLayout(2, 1, 0, 2));
            textCol.setOpaque(false);

            JLabel mainPrompt = new JLabel("📷  Upload Property Photograph");
            mainPrompt.setFont(UIUtils.FONT_BODY_BOLD);
            mainPrompt.setForeground(UIUtils.COLOR_TEXT_PRIMARY);

            JLabel subPrompt = new JLabel("PNG, JPG, WEBP • Max 10MB • Automatically cached and published to Community Feed");
            subPrompt.setFont(UIUtils.FONT_SMALL);
            subPrompt.setForeground(UIUtils.COLOR_TEXT_MUTED);

            textCol.add(mainPrompt);
            textCol.add(subPrompt);
            dropzone.add(textCol, BorderLayout.CENTER);

            JButton browseBtn = UIUtils.createSecondaryButton("Browse Photo...");
            browseBtn.addActionListener(e -> selectPhoto());
            dropzone.add(browseBtn, BorderLayout.EAST);

            photoSectionContainer.add(dropzone, BorderLayout.CENTER);
        } else {
            // Active Preview Card with Thumbnail and Specs
            JPanel previewCard = new JPanel(new BorderLayout(14, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(241, 245, 249));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.setColor(new Color(226, 232, 240));
                    g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 10, 10));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            previewCard.setOpaque(false);
            previewCard.setBorder(new EmptyBorder(10, 12, 10, 14));

            // Thumbnail on left
            File imgFile = new File(selectedImagePath);
            ImageIcon thumbIcon = null;
            if (imgFile.exists()) {
                try {
                    BufferedImage bimg = javax.imageio.ImageIO.read(imgFile);
                    if (bimg != null) {
                        thumbIcon = new ImageIcon(ImageHelper.scaleAndCrop(bimg, 110, 72, 6));
                    }
                } catch (Exception ignored) {}
            }
            if (thumbIcon == null) {
                thumbIcon = new ImageIcon(ImageHelper.generateEditorialBanner(
                        new FoundItem("TEMP", "Preview", ItemCategory.ELECTRONICS, LocalDate.now(), "", new User("Preview", "", ""), "Preview Location", "Storage"), 110, 72));
            }

            photoThumbnailLabel = new JLabel(thumbIcon);
            photoThumbnailLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            photoThumbnailLabel.setToolTipText("Click to inspect photo in high-resolution lightbox");
            photoThumbnailLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    inspectSelectedPhoto();
                }
            });
            previewCard.add(photoThumbnailLabel, BorderLayout.WEST);

            // Middle: Specs & Verification
            JPanel metaCol = new JPanel(new GridLayout(3, 1, 0, 2));
            metaCol.setOpaque(false);

            String displayName = imgFile.getName();
            photoFileNameLabel = new JLabel("✓ " + displayName);
            photoFileNameLabel.setFont(UIUtils.FONT_BODY_BOLD);
            photoFileNameLabel.setForeground(UIUtils.COLOR_SUCCESS);

            String specsText = ImageHelper.getImageMetadata(selectedImagePath);
            photoSpecsLabel = new JLabel(specsText);
            photoSpecsLabel.setFont(UIUtils.FONT_SMALL);
            photoSpecsLabel.setForeground(UIUtils.COLOR_TEXT_SECONDARY);

            JLabel readyLabel = new JLabel("● Image Cached & Ready for High-Res Feed Banner");
            readyLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 10));
            readyLabel.setForeground(new Color(2, 132, 199));

            metaCol.add(photoFileNameLabel);
            metaCol.add(photoSpecsLabel);
            metaCol.add(readyLabel);
            previewCard.add(metaCol, BorderLayout.CENTER);

            // Right: Actions
            JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 12));
            btnGroup.setOpaque(false);

            JButton inspectBtn = UIUtils.createSecondaryButton("🔍 Inspect");
            inspectBtn.addActionListener(e -> inspectSelectedPhoto());

            JButton changeBtn = UIUtils.createSecondaryButton("🔄 Change");
            changeBtn.addActionListener(e -> selectPhoto());

            JButton removeBtn = UIUtils.createModernButton("✕ Remove", new Color(254, 242, 242),
                    new Color(254, 226, 226), UIUtils.COLOR_PRIMARY, 6);
            removeBtn.addActionListener(e -> clearSelectedPhoto());

            btnGroup.add(inspectBtn);
            btnGroup.add(changeBtn);
            btnGroup.add(removeBtn);
            previewCard.add(btnGroup, BorderLayout.EAST);

            photoSectionContainer.add(previewCard, BorderLayout.CENTER);
        }

        photoSectionContainer.revalidate();
        photoSectionContainer.repaint();
    }

    private void inspectSelectedPhoto() {
        if (selectedImagePath != null && !selectedImagePath.trim().isEmpty()) {
            ItemCategory cat = (ItemCategory) categoryComboBox.getSelectedItem();
            if (cat == null) cat = ItemCategory.OTHER;
            FoundItem temp = new FoundItem("PREVIEW", nameField.getText().isEmpty() ? "Item Photograph" : nameField.getText(),
                    cat, LocalDate.now(), descriptionArea.getText(),
                    new com.smartlostfound.model.User("Finder", "", ""),
                    locationFoundField.getText(), storageLocationField.getText());
            temp.setImagePath(selectedImagePath);
            new PhotoLightboxDialog(SwingUtilities.getWindowAncestor(this), temp).setVisible(true);
        }
    }

    private void selectPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Found Item Photo");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files (*.jpg, *.jpeg, *.png, *.webp, *.gif)", "jpg", "jpeg", "png", "webp", "gif"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File chosenFile = chooser.getSelectedFile();
            this.selectedImagePath = ImageHelper.copyImageToStorage(chosenFile);
            renderPhotoSection();
        }
    }

    private void clearSelectedPhoto() {
        this.selectedImagePath = "";
        renderPhotoSection();
    }

    public void autoFillReporter(User user) {
        if (user != null) {
            if (finderNameField != null) {
                finderNameField.setText(user.getName());
            }
            if (finderEmailField != null) {
                finderEmailField.setText(user.getEmail());
            }
            if (finderPhoneField != null) {
                finderPhoneField.setText(user.getPhone());
            }
        }
    }

    public void resetForm() {
        nameField.setText("");
        categoryComboBox.setSelectedIndex(0);
        dateField.setText(LocalDate.now().format(Item.DATE_FORMATTER));
        locationFoundField.setText("");
        storageLocationField.setText("");
        descriptionArea.setText("");
        clearSelectedPhoto();
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

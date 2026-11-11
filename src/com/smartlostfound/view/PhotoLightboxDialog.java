package com.smartlostfound.view;

import com.smartlostfound.model.FoundItem;
import com.smartlostfound.model.Item;
import com.smartlostfound.model.LostItem;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Ultra-luxurious modal Lightbox photo viewer for inspecting item photographs
 * in high-resolution with zoom controls, specifications overlay, and image export.
 */
public class PhotoLightboxDialog extends JDialog {

    private final Item item;
    private final BufferedImage originalImage;
    private boolean isActualSize = false;

    private JLabel imageDisplayLabel;
    private JScrollPane imageScrollPane;
    private JButton zoomToggleBtn;

    public PhotoLightboxDialog(Window owner, Item item) {
        super(owner, "Photo Inspection — " + item.getName(), ModalityType.APPLICATION_MODAL);
        this.item = item;
        this.originalImage = ImageHelper.getOriginalImage(item);

        initComponents();
        setupKeyBindings();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(11, 11, 14));

        // 1. Top Header Bar (Obsidian Black)
        add(createHeaderBar(), BorderLayout.NORTH);

        // 2. Center Image Viewport
        imageDisplayLabel = new JLabel();
        imageDisplayLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageDisplayLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageDisplayLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imageDisplayLabel.setToolTipText("Click image to toggle between Fit to Window and Actual Size (100%)");
        imageDisplayLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleZoom();
            }
        });

        imageScrollPane = new JScrollPane(imageDisplayLabel);
        imageScrollPane.setBorder(null);
        imageScrollPane.getViewport().setBackground(new Color(11, 11, 14));
        imageScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        imageScrollPane.getHorizontalScrollBar().setUnitIncrement(20);

        add(imageScrollPane, BorderLayout.CENTER);

        // 3. Bottom Information & Action Bar
        add(createBottomBar(), BorderLayout.SOUTH);

        // Window configuration
        setSize(960, 680);
        setMinimumSize(new Dimension(740, 520));
        setLocationRelativeTo(getOwner());

        // Update image fit once window is laid out
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!isActualSize) {
                    updateFitToWindow();
                }
            }
        });

        SwingUtilities.invokeLater(this::updateFitToWindow);
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(new Color(18, 19, 24));
        header.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(39, 39, 42)),
                new EmptyBorder(12, 20, 12, 20)
        ));

        // Left Meta
        JPanel leftMeta = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftMeta.setOpaque(false);

        boolean isLost = item instanceof LostItem;

        JLabel idLabel = new JLabel("  " + item.getId() + "  ");
        idLabel.setFont(UIUtils.FONT_MONO);
        idLabel.setOpaque(true);
        idLabel.setBackground(isLost ? new Color(229, 46, 45, 30) : new Color(2, 132, 199, 30));
        idLabel.setForeground(isLost ? UIUtils.COLOR_PRIMARY : new Color(56, 189, 248));
        idLabel.setBorder(new LineBorder(isLost ? UIUtils.COLOR_PRIMARY : new Color(2, 132, 199), 1, true));

        JLabel titleLabel = new JLabel(item.getName());
        titleLabel.setFont(UIUtils.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        JLabel statusBadge = new JLabel("  " + item.getStatus().getDisplayName().toUpperCase() + "  ");
        statusBadge.setFont(UIUtils.FONT_BADGE);
        statusBadge.setOpaque(true);
        statusBadge.setBackground(item.getStatus().getBackgroundColor());
        statusBadge.setForeground(item.getStatus().getForegroundColor());
        statusBadge.setBorder(new LineBorder(item.getStatus().getForegroundColor(), 1, true));

        leftMeta.add(idLabel);
        leftMeta.add(titleLabel);
        leftMeta.add(statusBadge);
        header.add(leftMeta, BorderLayout.WEST);

        // Right Actions
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        zoomToggleBtn = UIUtils.createModernButton("🔍 Actual Size (100%)", new Color(39, 39, 42),
                new Color(63, 63, 70), Color.WHITE, 6);
        zoomToggleBtn.addActionListener(e -> toggleZoom());

        JButton exportBtn = UIUtils.createModernButton("💾 Export Photo...", new Color(39, 39, 42),
                new Color(63, 63, 70), Color.WHITE, 6);
        exportBtn.addActionListener(e -> exportImage());

        JButton closeBtn = UIUtils.createModernButton("✕ Close (Esc)", new Color(229, 46, 45),
                new Color(185, 28, 28), Color.WHITE, 6);
        closeBtn.addActionListener(e -> dispose());

        rightActions.add(zoomToggleBtn);
        rightActions.add(exportBtn);
        rightActions.add(closeBtn);
        header.add(rightActions, BorderLayout.EAST);

        return header;
    }

    private JPanel createBottomBar() {
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setBackground(new Color(18, 19, 24));
        footer.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(39, 39, 42)),
                new EmptyBorder(10, 20, 10, 20)
        ));

        // File specs
        String specs = String.format("📷 %d × %d px  •  %s  •  📍 %s",
                originalImage.getWidth(), originalImage.getHeight(),
                item.getCategory().getDisplayName(),
                item.getSpecificLocation());

        JLabel specsLabel = new JLabel(specs);
        specsLabel.setFont(UIUtils.FONT_BODY);
        specsLabel.setForeground(new Color(161, 161, 170));
        footer.add(specsLabel, BorderLayout.WEST);

        JLabel hintLabel = new JLabel("Double-click or press 'Z' to toggle zoom  •  Press ESC to close");
        hintLabel.setFont(UIUtils.FONT_SMALL);
        hintLabel.setForeground(new Color(113, 113, 122));
        footer.add(hintLabel, BorderLayout.EAST);

        return footer;
    }

    private void updateFitToWindow() {
        if (originalImage == null) return;

        int availW = Math.max(200, imageScrollPane.getViewport().getWidth() - 32);
        int availH = Math.max(200, imageScrollPane.getViewport().getHeight() - 32);

        double imgW = originalImage.getWidth();
        double imgH = originalImage.getHeight();

        double scale = Math.min((double) availW / imgW, (double) availH / imgH);
        scale = Math.min(scale, 1.0); // Don't upscale past 100%

        int targetW = (int) Math.round(imgW * scale);
        int targetH = (int) Math.round(imgH * scale);

        BufferedImage scaled = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(originalImage, 0, 0, targetW, targetH, null);

        // Draw subtle border around photo
        g2.setColor(new Color(63, 63, 70));
        g2.drawRect(0, 0, targetW - 1, targetH - 1);
        g2.dispose();

        imageDisplayLabel.setIcon(new ImageIcon(scaled));
        isActualSize = false;
        zoomToggleBtn.setText("🔍 Actual Size (100%)");
    }

    private void toggleZoom() {
        if (isActualSize) {
            updateFitToWindow();
        } else {
            imageDisplayLabel.setIcon(new ImageIcon(originalImage));
            isActualSize = true;
            zoomToggleBtn.setText("🔍 Fit Window");
        }
        imageScrollPane.revalidate();
        imageScrollPane.repaint();
    }

    private void exportImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Item Photo");
        String defaultName = item.getId().toLowerCase() + "_photo.png";
        chooser.setSelectedFile(new File(defaultName));
        chooser.setFileFilter(new FileNameExtensionFilter("PNG Image (*.png)", "png"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File targetFile = chooser.getSelectedFile();
            if (!targetFile.getName().toLowerCase().endsWith(".png")) {
                targetFile = new File(targetFile.getParentFile(), targetFile.getName() + ".png");
            }
            try {
                ImageIO.write(originalImage, "PNG", targetFile);
                JOptionPane.showMessageDialog(this,
                        "Photo successfully exported to:\n" + targetFile.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to save image: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setupKeyBindings() {
        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
        am.put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), "toggleZoom");
        am.put("toggleZoom", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleZoom();
            }
        });
    }
}

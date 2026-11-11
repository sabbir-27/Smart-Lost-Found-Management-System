package com.smartlostfound.view;

import com.smartlostfound.model.FoundItem;
import com.smartlostfound.model.Item;
import com.smartlostfound.model.LostItem;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Image processing and dummy photo generator for lost and found items.
 * Generates and caches photorealistic dummy product mockups on disk in data/images/
 * and provides bicubic aspect-ratio scaling for feed post cards and inspection dialogs.
 */
public final class ImageHelper {

    public static final String IMAGES_DIR = "data/images";

    private ImageHelper() {
    }

    /**
     * Loads and smoothly scales an image, or generates an editorial banner if absent.
     *
     * @param item Target item to render image for
     * @param targetWidth Desired display width
     * @param targetHeight Desired display height
     * @return Scaled and formatted ImageIcon
     */
    public static ImageIcon getItemImageThumbnail(Item item, int targetWidth, int targetHeight) {
        ensureDummyPhotosExist();

        String path = item.getImagePath();
        // If empty path, check if a default dummy photo matches this item's ID
        if (path == null || path.trim().isEmpty()) {
            path = getDefaultPhotoPathForId(item.getId());
            item.setImagePath(path);
        }

        if (path != null && !path.trim().isEmpty()) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                try {
                    BufferedImage original = ImageIO.read(file);
                    if (original != null) {
                        return new ImageIcon(scaleAndCrop(original, targetWidth, targetHeight, 10));
                    }
                } catch (Exception e) {
                    System.err.println("Could not load image: " + path + " (" + e.getMessage() + ")");
                }
            }
        }

        // Fallback to high-contrast graphic banner
        return new ImageIcon(generateEditorialBanner(item, targetWidth, targetHeight));
    }

    public static String getDefaultPhotoPathForId(String id) {
        if (id == null) return "";
        switch (id.toUpperCase()) {
            case "LOST-1001":
                return IMAGES_DIR + "/macbook.png";
            case "FND-2001":
                return IMAGES_DIR + "/backpack.png";
            case "LOST-1002":
                return IMAGES_DIR + "/wallet.png";
            case "FND-2002":
                return IMAGES_DIR + "/keys.png";
            case "LOST-1003":
                return IMAGES_DIR + "/earring.png";
            case "FND-2003":
                return IMAGES_DIR + "/passport.png";
            default:
                return "";
        }
    }

    /**
     * Scales and crops an image to fit exact dimensions with rounded corners.
     */
    public static BufferedImage scaleAndCrop(BufferedImage src, int width, int height, int radius) {
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Clip to rounded rectangle
        g2.setClip(new RoundRectangle2D.Float(0, 0, width, height, radius * 2, radius * 2));

        // Aspect fill
        double srcAspect = (double) src.getWidth() / src.getHeight();
        double dstAspect = (double) width / height;

        int drawW = width;
        int drawH = height;
        int drawX = 0;
        int drawY = 0;

        if (srcAspect > dstAspect) {
            drawW = (int) (height * srcAspect);
            drawX = -(drawW - width) / 2;
        } else {
            drawH = (int) (width / srcAspect);
            drawY = -(drawH - height) / 2;
        }

        g2.drawImage(src, drawX, drawY, drawW, drawH, null);

        // Subtle outer border
        g2.setClip(null);
        g2.setColor(new Color(228, 228, 231));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1, height - 1, radius * 2, radius * 2));

        g2.dispose();
        return output;
    }

    /**
     * Generates a sleek, DesignRush-styled graphic banner if no photo file is found.
     */
    public static BufferedImage generateEditorialBanner(Item item, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        boolean isLost = item instanceof LostItem;

        // Gradient background
        Color bgStart = isLost ? new Color(26, 26, 30) : new Color(18, 24, 38);
        Color bgEnd = isLost ? new Color(15, 15, 17) : new Color(10, 15, 26);
        GradientPaint gp = new GradientPaint(0, 0, bgStart, width, height, bgEnd);
        g2.setPaint(gp);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, 16, 16));

        // Center Icon bubble
        String iconSymbol = getCategorySymbol(item.getCategory().name());
        g2.setColor(isLost ? new Color(229, 46, 45, 180) : new Color(2, 132, 199, 180));
        g2.fillOval((width - 76) / 2, (height - 76) / 2 - 12, 76, 76);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 34));
        FontMetrics fm = g2.getFontMetrics();
        int iconX = (width - fm.stringWidth(iconSymbol)) / 2;
        int iconY = ((height - 76) / 2 - 12) + ((76 - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(iconSymbol, iconX, iconY);

        // Title text in banner
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        fm = g2.getFontMetrics();
        String bannerTitle = item.getName();
        if (bannerTitle.length() > 38) {
            bannerTitle = bannerTitle.substring(0, 35) + "...";
        }
        int titleX = (width - fm.stringWidth(bannerTitle)) / 2;
        g2.drawString(bannerTitle, titleX, height - 34);

        // Category subtitle
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.setColor(new Color(161, 161, 170));
        fm = g2.getFontMetrics();
        String catSubtitle = item.getCategory().getDisplayName().toUpperCase() + "  •  " + item.getId();
        int subX = (width - fm.stringWidth(catSubtitle)) / 2;
        g2.drawString(catSubtitle, subX, height - 16);

        // Top Left Status Badge Pill
        int badgeW = 96;
        int badgeH = 24;
        g2.setColor(isLost ? UIUtils.COLOR_PRIMARY : new Color(2, 132, 199));
        g2.fill(new RoundRectangle2D.Float(14, 14, badgeW, badgeH, 10, 10));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        fm = g2.getFontMetrics();
        String badgeText = isLost ? "● LOST ITEM" : "■ FOUND ITEM";
        int bx = 14 + (badgeW - fm.stringWidth(badgeText)) / 2;
        int by = 14 + ((badgeH - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(badgeText, bx, by);

        g2.dispose();
        return image;
    }

    private static String getCategorySymbol(String categoryName) {
        if (categoryName == null) return "★";
        switch (categoryName.toUpperCase()) {
            case "ELECTRONICS":
                return "💻";
            case "BAG_BACKPACK":
                return "🎒";
            case "WALLET_PURSE":
                return "👛";
            case "KEYS":
                return "🔑";
            case "JEWELRY_WATCH":
                return "⌚";
            case "DOCUMENTS_ID":
                return "📄";
            case "CLOTHING":
                return "👕";
            case "BOOKS_STATIONERY":
                return "📚";
            default:
                return "🏷️";
        }
    }

    /**
     * Ensures all realistic dummy product photo files are generated and stored in data/images/
     */
    public static synchronized void ensureDummyPhotosExist() {
        try {
            Path dir = Paths.get(IMAGES_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            File macbookFile = new File(IMAGES_DIR + "/macbook.png");
            if (!macbookFile.exists()) {
                ImageIO.write(createMacBookPhoto(), "PNG", macbookFile);
            }

            File backpackFile = new File(IMAGES_DIR + "/backpack.png");
            if (!backpackFile.exists()) {
                ImageIO.write(createBackpackPhoto(), "PNG", backpackFile);
            }

            File walletFile = new File(IMAGES_DIR + "/wallet.png");
            if (!walletFile.exists()) {
                ImageIO.write(createWalletPhoto(), "PNG", walletFile);
            }

            File keysFile = new File(IMAGES_DIR + "/keys.png");
            if (!keysFile.exists()) {
                ImageIO.write(createKeysPhoto(), "PNG", keysFile);
            }

            File earringFile = new File(IMAGES_DIR + "/earring.png");
            if (!earringFile.exists()) {
                ImageIO.write(createEarringPhoto(), "PNG", earringFile);
            }

            File passportFile = new File(IMAGES_DIR + "/passport.png");
            if (!passportFile.exists()) {
                ImageIO.write(createPassportPhoto(), "PNG", passportFile);
            }

        } catch (Exception e) {
            System.err.println("Warning generating dummy photos: " + e.getMessage());
        }
    }

    // =========================================================================
    // Photorealistic Dummy Image Generators (800 x 480)
    // =========================================================================

    private static BufferedImage createMacBookPhoto() {
        int w = 800, h = 480;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        configureGraphics(g2);

        // Clean studio tabletop background
        g2.setPaint(new LinearGradientPaint(0, 0, w, h,
                new float[]{0f, 1f},
                new Color[]{new Color(241, 245, 249), new Color(203, 213, 225)}));
        g2.fillRect(0, 0, w, h);

        // Soft contact shadow
        g2.setColor(new Color(15, 23, 42, 45));
        g2.fill(new RoundRectangle2D.Float(160, 400, 480, 26, 30, 30));

        // Screen Lid
        g2.setColor(new Color(30, 41, 59));
        g2.fill(new RoundRectangle2D.Float(200, 70, 400, 250, 16, 16));

        // Screen Bezel & Display
        g2.setColor(new Color(15, 23, 42));
        g2.fill(new RoundRectangle2D.Float(210, 80, 380, 230, 8, 8));

        // Vibrant Screen Desktop Wallpaper
        g2.setPaint(new LinearGradientPaint(210, 80, 590, 310,
                new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(99, 102, 241), new Color(236, 72, 153), new Color(244, 63, 94)}));
        g2.fillRect(212, 82, 376, 226);

        // Notch at top of screen
        g2.setColor(Color.BLACK);
        g2.fill(new RoundRectangle2D.Float(380, 80, 40, 10, 6, 6));

        // Code Editor Window on screen
        g2.setColor(new Color(15, 23, 42, 220));
        g2.fill(new RoundRectangle2D.Float(240, 110, 320, 170, 8, 8));
        g2.setColor(new Color(34, 197, 94));
        g2.fillOval(250, 118, 6, 6);
        g2.setColor(new Color(234, 179, 8));
        g2.fillOval(260, 118, 6, 6);
        g2.setColor(new Color(239, 68, 68));
        g2.fillOval(270, 118, 6, 6);

        // Syntax code lines
        g2.setColor(new Color(147, 197, 253));
        g2.fillRect(250, 136, 140, 6);
        g2.setColor(new Color(244, 114, 182));
        g2.fillRect(250, 148, 180, 6);
        g2.setColor(new Color(253, 224, 71));
        g2.fillRect(270, 160, 120, 6);
        g2.setColor(new Color(167, 139, 250));
        g2.fillRect(270, 172, 200, 6);
        g2.setColor(new Color(147, 197, 253));
        g2.fillRect(250, 184, 160, 6);

        // Lower Chassis (Keyboard Deck)
        Polygon deck = new Polygon();
        deck.addPoint(170, 405);
        deck.addPoint(630, 405);
        deck.addPoint(595, 320);
        deck.addPoint(205, 320);
        g2.setColor(new Color(203, 213, 225));
        g2.fill(deck);

        // Keyboard well
        g2.setColor(new Color(15, 23, 42));
        g2.fill(new RoundRectangle2D.Float(230, 328, 340, 44, 4, 4));

        // Key grid
        g2.setColor(new Color(51, 65, 85));
        for (int kx = 236; kx < 560; kx += 18) {
            for (int ky = 332; ky < 366; ky += 11) {
                g2.fillRect(kx, ky, 14, 8);
            }
        }

        // Trackpad
        g2.setColor(new Color(226, 232, 240));
        g2.draw(new RoundRectangle2D.Float(360, 375, 80, 26, 6, 6));

        // Product Banner Overlay
        drawPhotoOverlay(g2, "SPACE GRAY MACBOOK PRO 14\"", "Serial ending in 89Q2 • University Science Library", w, h);

        g2.dispose();
        return img;
    }

    private static BufferedImage createBackpackPhoto() {
        int w = 800, h = 480;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        configureGraphics(g2);

        // Warm neutral background
        g2.setPaint(new LinearGradientPaint(0, 0, w, h,
                new float[]{0f, 1f},
                new Color[]{new Color(248, 250, 252), new Color(226, 232, 240)}));
        g2.fillRect(0, 0, w, h);

        // Soft drop shadow
        g2.setColor(new Color(15, 23, 42, 40));
        g2.fill(new RoundRectangle2D.Float(270, 390, 260, 30, 40, 40));

        // Backpack Main Body (Deep Navy)
        g2.setColor(new Color(30, 58, 138));
        g2.fill(new RoundRectangle2D.Float(290, 90, 220, 300, 50, 50));

        // Shoulder Straps visible behind
        g2.setColor(new Color(15, 23, 42));
        g2.setStroke(new BasicStroke(16f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(245, 110, 100, 220, 60, 160);
        g2.drawArc(455, 110, 100, 220, -40, 160);

        // Top grab handle
        g2.setColor(new Color(15, 23, 42));
        g2.setStroke(new BasicStroke(10f));
        g2.drawArc(360, 60, 80, 50, 0, 180);

        // Front Upper Utility Pocket (Slate Blue)
        g2.setColor(new Color(37, 99, 235));
        g2.fill(new RoundRectangle2D.Float(315, 120, 170, 90, 24, 24));
        g2.setColor(new Color(241, 245, 249));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(330, 150, 470, 150); // Zipper line

        // Front Lower Utility Pocket
        g2.setColor(new Color(29, 78, 216));
        g2.fill(new RoundRectangle2D.Float(310, 225, 180, 140, 24, 24));
        g2.setColor(new Color(241, 245, 249));
        g2.drawLine(325, 255, 475, 255); // Zipper line

        // Side Mesh Pocket with Blue Water Bottle
        g2.setColor(new Color(2, 132, 199));
        g2.fill(new RoundRectangle2D.Float(265, 200, 35, 130, 12, 12)); // Bottle
        g2.setColor(new Color(203, 213, 225));
        g2.fillRect(272, 180, 20, 22); // Steel cap
        g2.setColor(new Color(15, 23, 42, 180));
        g2.fill(new RoundRectangle2D.Float(260, 240, 40, 100, 14, 14)); // Mesh

        // Osprey style logo emblem
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.drawString("OSPREY", 370, 190);

        drawPhotoOverlay(g2, "NAVY OSPREY BACKPACK", "Cafeteria East Wing • Contains notebooks and bottle", w, h);

        g2.dispose();
        return img;
    }

    private static BufferedImage createWalletPhoto() {
        int w = 800, h = 480;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        configureGraphics(g2);

        // Elegant light tabletop
        g2.setPaint(new LinearGradientPaint(0, 0, w, h,
                new float[]{0f, 1f},
                new Color[]{new Color(245, 245, 244), new Color(214, 211, 209)}));
        g2.fillRect(0, 0, w, h);

        // Drop shadow
        g2.setColor(new Color(28, 25, 23, 50));
        g2.fill(new RoundRectangle2D.Float(220, 340, 360, 35, 30, 30));

        // Wallet Left Flap (Chestnut Leather)
        g2.setColor(new Color(120, 53, 15));
        g2.fill(new RoundRectangle2D.Float(240, 130, 160, 220, 16, 16));

        // Wallet Right Flap (Bi-fold open angle)
        g2.setColor(new Color(146, 64, 14));
        g2.fill(new RoundRectangle2D.Float(400, 130, 160, 220, 16, 16));

        // Fine perimeter stitching
        g2.setColor(new Color(254, 243, 199));
        float[] dash = {4f, 4f};
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
        g2.draw(new RoundRectangle2D.Float(246, 136, 148, 208, 12, 12));
        g2.draw(new RoundRectangle2D.Float(406, 136, 148, 208, 12, 12));

        // Credit card slots in left flap
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(69, 26, 3));
        g2.drawLine(255, 180, 385, 180);
        g2.drawLine(255, 220, 385, 220);
        g2.drawLine(255, 260, 385, 260);

        // Student ID Card peeking out
        g2.setColor(new Color(239, 246, 255));
        g2.fill(new RoundRectangle2D.Float(415, 150, 130, 80, 8, 8));
        g2.setColor(new Color(37, 99, 235));
        g2.fillRect(415, 150, 130, 18);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
        g2.drawString("CAMPUS STUDENT ID", 422, 163);

        // Embossed brand logo
        g2.setColor(new Color(254, 243, 199, 180));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString("TOMMY HILFIGER", 260, 320);

        drawPhotoOverlay(g2, "BROWN LEATHER BI-FOLD WALLET", "Gymnasium Locker Room • Driver's License & Student ID", w, h);

        g2.dispose();
        return img;
    }

    private static BufferedImage createKeysPhoto() {
        int w = 800, h = 480;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        configureGraphics(g2);

        // Dark modern counter
        g2.setPaint(new LinearGradientPaint(0, 0, w, h,
                new float[]{0f, 1f},
                new Color[]{new Color(228, 228, 231), new Color(161, 161, 170)}));
        g2.fillRect(0, 0, w, h);

        // Shadow
        g2.setColor(new Color(15, 15, 17, 45));
        g2.fill(new RoundRectangle2D.Float(240, 330, 320, 30, 20, 20));

        // Red Anodized Aluminum Carabiner
        g2.setColor(new Color(220, 38, 38));
        g2.setStroke(new BasicStroke(14f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new RoundRectangle2D.Float(320, 100, 160, 110, 40, 40));
        // Silver gate
        g2.setColor(new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(8f));
        g2.drawLine(440, 120, 440, 180);

        // Split Ring
        g2.setColor(new Color(148, 163, 184));
        g2.setStroke(new BasicStroke(5f));
        g2.drawOval(375, 195, 50, 50);

        // Toyota Key Fob (Sleek Black)
        g2.setColor(new Color(24, 24, 27));
        g2.fill(new RoundRectangle2D.Float(280, 230, 100, 160, 28, 28));

        // Chrome trim
        g2.setColor(new Color(203, 213, 225));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Float(284, 234, 92, 152, 24, 24));

        // Fob buttons
        g2.setColor(new Color(39, 39, 42));
        g2.fill(new RoundRectangle2D.Float(305, 255, 50, 28, 8, 8));
        g2.fill(new RoundRectangle2D.Float(305, 290, 50, 28, 8, 8));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.drawString("LOCK", 314, 274);
        g2.drawString("UNLK", 314, 309);

        // House Brass Key
        g2.setColor(new Color(217, 119, 6));
        g2.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        g2.drawLine(410, 240, 480, 360);
        g2.fillRect(470, 340, 18, 12);
        g2.fillRect(455, 315, 18, 12);

        // Gym barcode tag
        g2.setColor(new Color(239, 68, 68));
        g2.fill(new RoundRectangle2D.Float(420, 250, 70, 40, 8, 8));
        g2.setColor(Color.WHITE);
        g2.fillRect(430, 258, 50, 24);
        g2.setColor(Color.BLACK);
        for (int bx = 435; bx < 475; bx += 4) {
            g2.drawLine(bx, 260, bx, 280);
        }

        drawPhotoOverlay(g2, "TOYOTA KEY FOB & CARABINER", "Parking Lot B Section 3 • Red anodized clasp", w, h);

        g2.dispose();
        return img;
    }

    private static BufferedImage createEarringPhoto() {
        int w = 800, h = 480;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        configureGraphics(g2);

        // Deep Navy Velvet Jewelry Box Background
        g2.setPaint(new RadialGradientPaint(w / 2f, h / 2f, 340f,
                new float[]{0f, 1f},
                new Color[]{new Color(30, 41, 59), new Color(11, 11, 12)}));
        g2.fillRect(0, 0, w, h);

        // Velvet ring box cushion oval
        g2.setColor(new Color(15, 23, 42));
        g2.fill(new Ellipse2D.Float(250, 130, 300, 220));
        g2.setColor(new Color(51, 65, 85));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new Ellipse2D.Float(250, 130, 300, 220));

        // White Gold Prongs
        g2.setColor(new Color(226, 232, 240));
        g2.fillOval(385, 225, 30, 30);

        // Solitaire Diamond Gem (Brilliant cut polygon)
        Polygon diamond = new Polygon();
        diamond.addPoint(400, 175);
        diamond.addPoint(440, 210);
        diamond.addPoint(440, 260);
        diamond.addPoint(400, 295);
        diamond.addPoint(360, 260);
        diamond.addPoint(360, 210);

        g2.setPaint(new LinearGradientPaint(360, 175, 440, 295,
                new float[]{0f, 0.4f, 0.7f, 1f},
                new Color[]{new Color(255, 255, 255), new Color(199, 210, 254), new Color(255, 255, 255), new Color(224, 231, 255)}));
        g2.fill(diamond);

        // Facet lines
        g2.setColor(new Color(255, 255, 255, 200));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(diamond);
        g2.drawLine(400, 175, 400, 295);
        g2.drawLine(360, 210, 440, 260);
        g2.drawLine(360, 260, 440, 210);

        // Specular Sparkle Star
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
        g2.drawString("✦", 442, 195);
        g2.drawString("✧", 335, 285);

        drawPhotoOverlay(g2, "WHITE GOLD SOLITAIRE DIAMOND STUD", "Auditorium Hall B • Screw backing graduation gift", w, h);

        g2.dispose();
        return img;
    }

    private static BufferedImage createPassportPhoto() {
        int w = 800, h = 480;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        configureGraphics(g2);

        // Clean counter background
        g2.setPaint(new LinearGradientPaint(0, 0, w, h,
                new float[]{0f, 1f},
                new Color[]{new Color(241, 245, 249), new Color(203, 213, 225)}));
        g2.fillRect(0, 0, w, h);

        // Shadow
        g2.setColor(new Color(15, 23, 42, 50));
        g2.fill(new RoundRectangle2D.Float(270, 375, 260, 25, 20, 20));

        // Passport Booklet (Deep Navy Leatherette)
        g2.setColor(new Color(30, 41, 59));
        g2.fill(new RoundRectangle2D.Float(285, 75, 230, 310, 14, 14));

        // Gold embossed frame
        g2.setColor(new Color(217, 119, 6));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Float(295, 85, 210, 290, 10, 10));

        // Golden Emblem Seal
        g2.fillOval(370, 180, 60, 60);
        g2.setColor(new Color(30, 41, 59));
        g2.fillOval(376, 186, 48, 48);
        g2.setColor(new Color(217, 119, 6));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2.drawString("★", 392, 218);

        // Golden Typography
        g2.setFont(new Font("Georgia", Font.BOLD, 16));
        g2.drawString("PASSPORT", 350, 140);

        // Biometric chip symbol
        g2.draw(new RoundRectangle2D.Float(385, 320, 30, 20, 4, 4));
        g2.fillOval(395, 325, 10, 10);

        drawPhotoOverlay(g2, "INTERNATIONAL PASSPORT IN LEATHER CASE", "Admin Building Lobby • Blue cover with initials E.K.", w, h);

        g2.dispose();
        return img;
    }

    private static void drawPhotoOverlay(Graphics2D g2, String title, String subtitle, int w, int h) {
        // Bottom frosted banner overlay
        g2.setColor(new Color(11, 11, 12, 210));
        g2.fill(new RoundRectangle2D.Float(14, h - 70, w - 28, 56, 12, 12));

        g2.setColor(UIUtils.COLOR_PRIMARY);
        g2.fillOval(30, h - 45, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.drawString(title, 50, h - 42);

        g2.setColor(new Color(161, 161, 170));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.drawString(subtitle, 50, h - 26);
    }

    private static void configureGraphics(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }
}

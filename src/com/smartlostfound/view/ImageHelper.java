package com.smartlostfound.view;

import com.smartlostfound.model.FoundItem;
import com.smartlostfound.model.Item;
import com.smartlostfound.model.LostItem;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Image processing and graphic banner utility for lost and found items.
 * Handles loading, high-quality bicubic downscaling, rounded corner rendering,
 * and generating rich editorial banners when custom photos are not provided.
 */
public final class ImageHelper {

    private ImageHelper() {
    }

    /**
     * Loads and smoothly scales an image, or generates an editorial banner if absent.
     *
     * @param item Item to render image for
     * @param targetWidth Desired display width
     * @param targetHeight Desired display height
     * @return Scaled and formatted ImageIcon
     */
    public static ImageIcon getItemImageThumbnail(Item item, int targetWidth, int targetHeight) {
        String path = item.getImagePath();
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

        // Generate synthetic high-contrast editorial banner
        return new ImageIcon(generateEditorialBanner(item, targetWidth, targetHeight));
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

        // Calculate aspect fill
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

        // Subtle inner border
        g2.setClip(null);
        g2.setColor(new Color(15, 15, 17, 30));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1, height - 1, radius * 2, radius * 2));

        g2.dispose();
        return output;
    }

    /**
     * Generates a sleek, Facebook/DesignRush-styled graphic banner representing the item.
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

        // Subtle geometric grid accent
        g2.setColor(new Color(255, 255, 255, 7));
        for (int x = 0; x < width; x += 32) {
            g2.drawLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += 32) {
            g2.drawLine(0, y, width, y);
        }

        // Center Icon & Category visual
        String iconSymbol = getCategorySymbol(item.getCategory().name());
        g2.setColor(isLost ? new Color(229, 46, 45, 180) : new Color(2, 132, 199, 180));
        g2.fillOval((width - 80) / 2, (height - 80) / 2 - 14, 80, 80);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 36));
        FontMetrics fm = g2.getFontMetrics();
        int iconX = (width - fm.stringWidth(iconSymbol)) / 2;
        int iconY = ((height - 80) / 2 - 14) + ((80 - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(iconSymbol, iconX, iconY);

        // Title text in banner
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        fm = g2.getFontMetrics();
        String bannerTitle = item.getName();
        if (bannerTitle.length() > 38) {
            bannerTitle = bannerTitle.substring(0, 35) + "...";
        }
        int titleX = (width - fm.stringWidth(bannerTitle)) / 2;
        g2.drawString(bannerTitle, titleX, height - 36);

        // Category subtitle
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.setColor(new Color(161, 161, 170));
        fm = g2.getFontMetrics();
        String catSubtitle = item.getCategory().getDisplayName().toUpperCase() + "  •  " + item.getId();
        int subX = (width - fm.stringWidth(catSubtitle)) / 2;
        g2.drawString(catSubtitle, subX, height - 18);

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

        // Reward / Holding Tag on Top Right
        if (isLost && ((LostItem) item).getRewardAmount() > 0.0) {
            String rewardText = String.format("$%.0f REWARD", ((LostItem) item).getRewardAmount());
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            fm = g2.getFontMetrics();
            int rW = fm.stringWidth(rewardText) + 16;
            g2.setColor(new Color(16, 185, 129));
            g2.fill(new RoundRectangle2D.Float(width - rW - 14, 14, rW, 24, 10, 10));
            g2.setColor(Color.WHITE);
            int rx = (width - rW - 14) + 8;
            int ry = 14 + ((24 - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(rewardText, rx, ry);
        } else if (!isLost) {
            String secureText = "IN CUSTODY";
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            fm = g2.getFontMetrics();
            int sW = fm.stringWidth(secureText) + 16;
            g2.setColor(new Color(24, 24, 27, 220));
            g2.fill(new RoundRectangle2D.Float(width - sW - 14, 14, sW, 24, 10, 10));
            g2.setColor(Color.WHITE);
            int sx = (width - sW - 14) + 8;
            int sy = 14 + ((24 - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(secureText, sx, sy);
        }

        // Outer Hairline Border
        g2.setColor(new Color(255, 255, 255, 24));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1, height - 1, 16, 16));

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
     * Initializes default realistic photographic illustrations in data/images/
     */
    public static void initializeSampleImagesDirectory() {
        try {
            Path dir = Paths.get("data/images");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException ignored) {
        }
    }
}

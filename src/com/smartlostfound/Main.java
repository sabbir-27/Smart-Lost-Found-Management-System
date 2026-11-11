package com.smartlostfound;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.service.LostFoundManager;
import com.smartlostfound.view.MainFrame;
import com.smartlostfound.view.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Application entry point for the Smart Lost &amp; Found Management System.
 * Bootstraps the Model-View-Controller components, applies modern UI styling,
 * and launches the Swing interface safely on the Event Dispatch Thread (EDT).
 */
public class Main {

    /**
     * Main executable entry point.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // Handle headless environments gracefully
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("==========================================================");
            System.out.println("Smart Lost & Found Management System");
            System.out.println("==========================================================");
            System.out.println("Headless environment detected (no display device found).");
            System.out.println("Initializing business model and verifying storage service...");

            LostFoundManager manager = new LostFoundManager();
            System.out.println("Storage path: " + manager.getStoragePath());
            System.out.println("Loaded items: " + manager.getAllItems().size());
            LostFoundManager.SystemStats stats = manager.getStatistics();
            System.out.printf("Total: %d | Lost: %d | Found: %d | Resolved: %d\n",
                    stats.getTotalCount(), stats.getLostCount(), stats.getFoundCount(), stats.getResolvedCount());
            System.out.println("All models and persistence layers validated successfully!");
            System.out.println("To launch the Swing GUI, run this on a desktop environment with a graphical display.");
            System.out.println("==========================================================");
            return;
        }

        // Configure modern UI theme and anti-aliasing
        UIUtils.applyModernLookAndFeel();

        // Launch GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Model / Business Logic Service
                LostFoundManager manager = new LostFoundManager();

                // 2. Controller
                LostFoundController controller = new LostFoundController(manager);

                // 3. View
                MainFrame frame = new MainFrame(controller);
                controller.setMainFrame(frame);

                // Display main window
                frame.setVisible(true);

            } catch (Exception e) {
                System.err.println("Fatal error initializing application: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to launch Smart Lost & Found System:\n" + e.getMessage(),
                        "Initialization Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

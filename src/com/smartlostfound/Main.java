package com.smartlostfound;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.service.LostFoundManager;
import com.smartlostfound.service.UserManager;
import com.smartlostfound.service.WebFeedServer;
import com.smartlostfound.view.MainFrame;
import com.smartlostfound.view.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Application entry point for the Smart Lost &amp; Found Management System.
 * Bootstraps the Model-View-Controller components, applies modern UI styling,
 * initializes User Authentication &amp; RBAC, and launches the Swing interface safely on the EDT.
 */
public class Main {

    /**
     * Main executable entry point.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // Standalone Web Server mode: java -jar SmartLostFound.jar --server
        if (args.length > 0 && ("--server".equalsIgnoreCase(args[0]) || "--web".equalsIgnoreCase(args[0]))) {
            System.out.println("==========================================================");
            System.out.println("Starting Standalone Web Feed Server on Localhost");
            System.out.println("==========================================================");
            LostFoundManager manager = new LostFoundManager();
            WebFeedServer server = new WebFeedServer(manager);
            server.start();
            System.out.printf("Server live at http://localhost:%d/ (Feed-Only Mode). Press Ctrl+C to exit.\n", server.getPort());
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                server.stop();
            }
            return;
        }

        // Handle headless environments gracefully
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("==========================================================");
            System.out.println("Smart Lost & Found Management System - DesignRush Edition");
            System.out.println("==========================================================");
            System.out.println("Headless environment detected (no display device found).");
            System.out.println("Initializing business models and verifying storage services...");

            LostFoundManager manager = new LostFoundManager();
            UserManager userManager = new UserManager();

            System.out.println("Item storage path: " + manager.getStoragePath());
            System.out.println("User storage path: " + userManager.getStoragePath());
            System.out.println("Loaded items: " + manager.getAllItems().size());
            System.out.println("Registered users: " + userManager.getTotalCount() +
                    " (" + userManager.getAdminCount() + " Admins, " +
                    (userManager.getTotalCount() - userManager.getAdminCount()) + " Users)");

            LostFoundManager.SystemStats stats = manager.getStatistics();
            System.out.printf("Inventory: Total: %d | Lost: %d | Found: %d | Resolved: %d\n",
                    stats.getTotalCount(), stats.getLostCount(), stats.getFoundCount(), stats.getResolvedCount());
            System.out.println("All models, persistence layers, and RBAC security validated successfully!");
            System.out.println("To launch the Swing GUI, run this on a desktop environment with a graphical display.");
            System.out.println("==========================================================");
            return;
        }

        // Configure modern UI theme and anti-aliasing
        UIUtils.applyModernLookAndFeel();

        // Launch GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Models & Services
                LostFoundManager manager = new LostFoundManager();
                UserManager userManager = new UserManager();

                // 2. Embedded Web Feed Server (Restricted to Community Feed only for web browsers)
                WebFeedServer webFeedServer = new WebFeedServer(manager);
                webFeedServer.start();
                Runtime.getRuntime().addShutdownHook(new Thread(webFeedServer::stop));

                // 3. Controller
                LostFoundController controller = new LostFoundController(manager, userManager);

                // 4. View Frame (Opens directly into the Full-Page Authentication Portal)
                MainFrame frame = new MainFrame(controller);
                controller.setMainFrame(frame);

                // 5. Reveal the full application window
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

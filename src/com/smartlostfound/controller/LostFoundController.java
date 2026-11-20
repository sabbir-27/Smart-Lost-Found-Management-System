package com.smartlostfound.controller;

import com.smartlostfound.model.*;
import com.smartlostfound.service.LostFoundManager;
import com.smartlostfound.service.UserManager;
import com.smartlostfound.storage.DataPersistenceException;
import com.smartlostfound.view.ItemDetailDialog;
import com.smartlostfound.view.LoginRegisterDialog;
import com.smartlostfound.view.MainFrame;
import com.smartlostfound.view.UpdateStatusDialog;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Central controller in the MVC architecture.
 * Mediates user actions originating from Views, executes business rules and queries
 * via {@link LostFoundManager} and {@link UserManager}, and updates View components accordingly.
 */
public class LostFoundController {

    private final LostFoundManager manager;
    private final UserManager userManager;
    private UserAccount currentUser;
    private MainFrame mainFrame;

    // Last known filter parameters for consistent view refresh
    private String currentKeyword = "";
    private ItemCategory currentCategory = null;
    private ItemStatus currentStatus = null;
    private String currentType = "ALL";

    /**
     * Constructs the controller with default backing services.
     *
     * @param manager Core business logic manager
     */
    public LostFoundController(LostFoundManager manager) {
        this(manager, new UserManager());
    }

    /**
     * Constructs the controller with specific managers for dependency injection and testing.
     *
     * @param manager Item inventory manager
     * @param userManager User accounts and authentication manager
     */
    public LostFoundController(LostFoundManager manager, UserManager userManager) {
        this.manager = manager;
        this.userManager = (userManager != null) ? userManager : new UserManager();
        this.currentUser = null;
    }

    /**
     * Associates the main view frame with this controller.
     *
     * @param mainFrame Main application view
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        if (mainFrame != null) {
            mainFrame.updateUserSession(currentUser);
        }
        refreshView();
    }

    public LostFoundManager getManager() {
        return manager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public UserAccount getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserAccount currentUser) {
        this.currentUser = currentUser;
        if (mainFrame != null) {
            mainFrame.updateUserSession(currentUser);
        }
        refreshView();
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Evaluates whether the current active user has permission to modify or delete an item.
     * Administrators have universal access; standard users can modify items they reported.
     */
    public boolean canModifyItem(Item item) {
        if (item == null || currentUser == null) {
            return false;
        }
        if (currentUser.isAdmin()) {
            return true;
        }
        return isItemReportedByUser(item, currentUser);
    }

    /**
     * Verifies if a given item was reported by the specified user account based on
     * matching contact email, phone, name, or username.
     */
    public boolean isItemReportedByUser(Item item, UserAccount user) {
        if (item == null || user == null || item.getReporter() == null) {
            return false;
        }

        String myName = user.getName() != null ? user.getName().toLowerCase().trim() : "";
        String myEmail = user.getEmail() != null ? user.getEmail().toLowerCase().trim() : "";
        String myPhone = user.getPhone() != null ? user.getPhone().toLowerCase().trim() : "";
        String myUser = user.getUsername() != null ? user.getUsername().toLowerCase().trim() : "";

        String repName = item.getReporter().getName() != null ? item.getReporter().getName().toLowerCase().trim() : "";
        String repEmail = item.getReporter().getEmail() != null ? item.getReporter().getEmail().toLowerCase().trim() : "";
        String repPhone = item.getReporter().getPhone() != null ? item.getReporter().getPhone().toLowerCase().trim() : "";

        boolean matchEmail = !myEmail.isEmpty() && !repEmail.isEmpty() && repEmail.equalsIgnoreCase(myEmail);
        boolean matchPhone = !myPhone.isEmpty() && !repPhone.isEmpty() && repPhone.equalsIgnoreCase(myPhone);
        boolean matchName = !myName.isEmpty() && !repName.isEmpty() && (repName.contains(myName) || myName.contains(repName));
        boolean matchUsername = !myUser.isEmpty() && ((!repName.isEmpty() && repName.contains(myUser)) || (!repEmail.isEmpty() && repEmail.contains(myUser)));

        return matchEmail || matchPhone || matchName || matchUsername;
    }

    /**
     * Opens the login &amp; registration modal dialog.
     */
    public void openLoginDialog(Window parent) {
        if (mainFrame != null) {
            mainFrame.showAuthPage();
            return;
        }
        LoginRegisterDialog dialog = new LoginRegisterDialog(parent, this);
        dialog.setInitialLaunch(false);
        dialog.setVisible(true);
    }

    /**
     * Handles submission of a new Lost Item report.
     */
    public void handleReportLostItem(String name, ItemCategory category, LocalDate date,
                                     String description, User reporter, String locationLost,
                                     double rewardAmount, Window parent, Runnable onSuccess) {
        handleReportLostItem(name, category, date, description, reporter, locationLost, rewardAmount, "", parent, onSuccess);
    }

    /**
     * Handles submission of a new Lost Item report with image attachment.
     */
    public void handleReportLostItem(String name, ItemCategory category, LocalDate date,
                                     String description, User reporter, String locationLost,
                                     double rewardAmount, String imagePath, Window parent, Runnable onSuccess) {
        try {
            LostItem item = manager.reportLostItem(
                    name, category, date, description, reporter, locationLost, rewardAmount, imagePath);

            JOptionPane.showMessageDialog(parent,
                    "Lost Item report submitted successfully!\n" +
                    "Assigned Tracking ID: " + item.getId() + "\n\n" +
                    "Custodians will reference this ID upon match.",
                    "Report Submitted", JOptionPane.INFORMATION_MESSAGE);

            if (onSuccess != null) {
                onSuccess.run();
            }

            refreshView();
            if (mainFrame != null) {
                mainFrame.setStatusMessage("Report logged: " + item.getId() + " - " + item.getName());
                mainFrame.switchToDashboard();
            }
        } catch (DataPersistenceException e) {
            JOptionPane.showMessageDialog(parent,
                    "Error saving lost item report to storage:\n" + e.getMessage(),
                    "Storage Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                    "An unexpected error occurred while reporting:\n" + e.getMessage(),
                    "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void handleReportFoundItem(String name, ItemCategory category, LocalDate date,
                                      String description, User reporter, String locationFound,
                                      String storageLocation, String finderNotes,
                                      Window parent, Runnable onSuccess) {
        handleReportFoundItem(name, category, date, description, reporter, locationFound, storageLocation, finderNotes, "", parent, onSuccess);
    }

    /**
     * Handles submission of a new Found Item report with image attachment.
     */
    public void handleReportFoundItem(String name, ItemCategory category, LocalDate date,
                                      String description, User reporter, String locationFound,
                                      String storageLocation, String finderNotes, String imagePath,
                                      Window parent, Runnable onSuccess) {
        try {
            FoundItem item = manager.reportFoundItem(
                    name, category, date, description, reporter, locationFound, storageLocation, finderNotes, imagePath);

            JOptionPane.showMessageDialog(parent,
                    "Found Item report submitted successfully!\n" +
                    "Assigned Storage ID: " + item.getId() + "\n\n" +
                    "Item logged at: " + item.getStorageLocation(),
                    "Report Submitted", JOptionPane.INFORMATION_MESSAGE);

            if (onSuccess != null) {
                onSuccess.run();
            }

            refreshView();
            if (mainFrame != null) {
                mainFrame.setStatusMessage("Found report logged: " + item.getId() + " - " + item.getName());
                mainFrame.switchToDashboard();
            }
        } catch (DataPersistenceException e) {
            JOptionPane.showMessageDialog(parent,
                    "Error saving found item report to storage:\n" + e.getMessage(),
                    "Storage Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                    "An unexpected error occurred while reporting:\n" + e.getMessage(),
                    "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles lifecycle status updates for an existing item with role verification.
     */
    public void handleUpdateStatus(String itemId, ItemStatus newStatus, String resolutionNotes, Window dialog) {
        try {
            Item item = manager.updateItemStatus(itemId, newStatus, resolutionNotes);

            JOptionPane.showMessageDialog(dialog,
                    "Status for item " + item.getId() + " successfully changed to: " + newStatus.getDisplayName(),
                    "Status Updated", JOptionPane.INFORMATION_MESSAGE);

            if (dialog != null) {
                dialog.dispose();
            }

            refreshView();
            if (mainFrame != null) {
                mainFrame.setStatusMessage("Updated status of " + item.getId() + " to " + newStatus.getDisplayName());
            }
        } catch (DataPersistenceException e) {
            JOptionPane.showMessageDialog(dialog,
                    "Failed to save status update to storage:\n" + e.getMessage(),
                    "Persistence Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog,
                    "Failed to update item status:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles item deletion after user confirmation and role-based permission verification.
     */
    public boolean handleDeleteItem(String itemId, Window parent) {
        if (itemId == null || itemId.trim().isEmpty()) {
            return false;
        }
        Optional<Item> itemOpt = manager.getItemById(itemId);
        if (!itemOpt.isPresent()) {
            JOptionPane.showMessageDialog(parent,
                    "Record not found: " + itemId,
                    "Not Found", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        Item item = itemOpt.get();
        if (currentUser == null) {
            int signInChoice = JOptionPane.showOptionDialog(parent,
                    "Authentication Required:\n\n" +
                    "You are currently browsing as a Guest.\n" +
                    "Please sign in to delete or manage property records.",
                    "Sign In Required",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[]{"Sign In / Register", "Cancel"},
                    "Sign In / Register");
            if (signInChoice == 0) {
                openLoginDialog(parent);
            }
            return false;
        }

        if (!canModifyItem(item)) {
            String reporterDisplay = (item.getReporter() != null && !item.getReporter().getName().isEmpty())
                    ? item.getReporter().getName()
                    : "another user";
            JOptionPane.showMessageDialog(parent,
                    "Permission Denied:\n" +
                    "Standard users (@" + currentUser.getUsername() +
                    ") can only delete property reports submitted by themselves.\n" +
                    "This item was reported by: " + reporterDisplay + ".\n\n" +
                    "Please contact a System Administrator (@admin) to manage this record.",
                    "Administrative Permission Required", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int choice = JOptionPane.showConfirmDialog(parent,
                "Are you sure you want to permanently delete record " + itemId + " (" + item.getName() + ")?\n" +
                "This action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = manager.deleteItem(itemId);
                if (deleted) {
                    // Refresh views and KPI statistics immediately before showing dialog
                    refreshView();
                    if (mainFrame != null) {
                        mainFrame.setStatusMessage("Deleted record: " + itemId);
                    }
                    JOptionPane.showMessageDialog(parent,
                            "Record " + itemId + " has been successfully removed.",
                            "Record Deleted", JOptionPane.INFORMATION_MESSAGE);
                    return true;
                } else {
                    JOptionPane.showMessageDialog(parent,
                            "Could not delete record " + itemId + ". The item was not found.",
                            "Deletion Failed", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (DataPersistenceException e) {
                JOptionPane.showMessageDialog(parent,
                        "Failed to update storage file after deletion:\n" + e.getMessage(),
                        "Storage Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }

    /**
     * Refreshes search and filter queries against the business layer.
     */
    public void handleFilterItems(String keyword, ItemCategory category, ItemStatus status, String type) {
        this.currentKeyword = keyword;
        this.currentCategory = category;
        this.currentStatus = status;
        this.currentType = type;
        refreshView();
    }

    /**
     * Re-executes current filters and updates the Dashboard view, Feed, User Panel, and Admin Console.
     */
    public void refreshView() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refreshView);
            return;
        }

        if (mainFrame == null) {
            return;
        }

        if (mainFrame.getDashboardPanel() != null) {
            List<Item> filtered = manager.searchAndFilter(
                    currentKeyword, currentCategory, currentStatus, currentType);
            int totalRecords = manager.getAllItems().size();

            mainFrame.getDashboardPanel().displayItems(filtered, totalRecords);
            mainFrame.getDashboardPanel().updateStatistics(manager.getStatistics());
        }

        if (mainFrame.getFeedPanel() != null) {
            mainFrame.getFeedPanel().displayFeedItems(manager.getAllItems());
        }

        if (mainFrame.getMyItemsPanel() != null) {
            mainFrame.getMyItemsPanel().refreshData();
        }

        if (mainFrame.getAdminConsolePanel() != null) {
            mainFrame.getAdminConsolePanel().refreshData();
        }

        mainFrame.revalidate();
        mainFrame.repaint();
    }

    /**
     * Reloads data from the persistence storage.
     */
    public void handleReloadData() {
        manager.initializeData();
        userManager.initializeData();
        refreshView();
        if (mainFrame != null) {
            mainFrame.setStatusMessage("Data reloaded from persistence storage. Total records: " + 
                    manager.getAllItems().size());
        }
    }

    /**
     * Opens the item detail modal dialog.
     */
    public void openItemDetailDialog(Item item, Window parent) {
        ItemDetailDialog dialog = new ItemDetailDialog(parent, item, this);
        dialog.setVisible(true);
    }

    /**
     * Opens the status transition modal dialog.
     */
    public void openUpdateStatusDialog(Item item, Window parent) {
        UpdateStatusDialog dialog = new UpdateStatusDialog(parent, item, this);
        dialog.setVisible(true);
    }

    /**
     * Handles clean application exit, ensuring persistence sync.
     */
    public void handleApplicationExit(Window window) {
        try {
            manager.persistCurrentState();
            userManager.persistCurrentState();
        } catch (DataPersistenceException e) {
            System.err.println("Warning: Error during final save on exit: " + e.getMessage());
        }
        window.dispose();
        System.exit(0);
    }

    /**
     * Returns a brief description of the storage path.
     */
    public String getStoragePathDescription() {
        return manager.getStoragePath();
    }
}

package com.smartlostfound.controller;

import com.smartlostfound.model.*;
import com.smartlostfound.service.LostFoundManager;
import com.smartlostfound.storage.DataPersistenceException;
import com.smartlostfound.view.ItemDetailDialog;
import com.smartlostfound.view.MainFrame;
import com.smartlostfound.view.UpdateStatusDialog;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Central controller in the MVC architecture.
 * Mediates user actions originating from Views, executes business rules and queries
 * via {@link LostFoundManager}, and updates View components accordingly.
 */
public class LostFoundController {

    private final LostFoundManager manager;
    private MainFrame mainFrame;

    // Last known filter parameters for consistent view refresh
    private String currentKeyword = "";
    private ItemCategory currentCategory = null;
    private ItemStatus currentStatus = null;
    private String currentType = "ALL";

    /**
     * Constructs the controller with the backing service manager.
     *
     * @param manager Core business logic manager
     */
    public LostFoundController(LostFoundManager manager) {
        this.manager = manager;
    }

    /**
     * Associates the main view frame with this controller.
     *
     * @param mainFrame Main application view
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        refreshView();
    }

    /**
     * Handles submission of a new Lost Item report.
     */
    public void handleReportLostItem(String name, ItemCategory category, LocalDate date,
                                     String description, User reporter, String locationLost,
                                     double rewardAmount, Window parent, Runnable onSuccess) {
        try {
            LostItem item = manager.reportLostItem(
                    name, category, date, description, reporter, locationLost, rewardAmount);

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

    /**
     * Handles submission of a new Found Item report.
     */
    public void handleReportFoundItem(String name, ItemCategory category, LocalDate date,
                                      String description, User reporter, String locationFound,
                                      String storageLocation, String finderNotes,
                                      Window parent, Runnable onSuccess) {
        try {
            FoundItem item = manager.reportFoundItem(
                    name, category, date, description, reporter, locationFound, storageLocation, finderNotes);

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
     * Handles lifecycle status updates for an existing item.
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
     * Handles item deletion after user confirmation.
     */
    public void handleDeleteItem(String itemId, Window parent) {
        int choice = JOptionPane.showConfirmDialog(parent,
                "Are you sure you want to permanently delete record " + itemId + "?\n" +
                "This action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = manager.deleteItem(itemId);
                if (deleted) {
                    JOptionPane.showMessageDialog(parent,
                            "Record " + itemId + " has been successfully removed.",
                            "Record Deleted", JOptionPane.INFORMATION_MESSAGE);
                    refreshView();
                    if (mainFrame != null) {
                        mainFrame.setStatusMessage("Deleted record: " + itemId);
                    }
                } else {
                    JOptionPane.showMessageDialog(parent,
                            "Record not found: " + itemId,
                            "Not Found", JOptionPane.WARNING_MESSAGE);
                }
            } catch (DataPersistenceException e) {
                JOptionPane.showMessageDialog(parent,
                        "Failed to update storage file after deletion:\n" + e.getMessage(),
                        "Storage Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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
     * Re-executes current filters and updates the Dashboard view and stats.
     */
    public void refreshView() {
        if (mainFrame == null || mainFrame.getDashboardPanel() == null) {
            return;
        }

        List<Item> filtered = manager.searchAndFilter(
                currentKeyword, currentCategory, currentStatus, currentType);
        int totalRecords = manager.getAllItems().size();

        mainFrame.getDashboardPanel().displayItems(filtered, totalRecords);
        mainFrame.getDashboardPanel().updateStatistics(manager.getStatistics());
    }

    /**
     * Reloads data from the persistence storage.
     */
    public void handleReloadData() {
        manager.initializeData();
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

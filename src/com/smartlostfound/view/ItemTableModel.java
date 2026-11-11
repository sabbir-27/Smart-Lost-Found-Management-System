package com.smartlostfound.view;

import com.smartlostfound.model.Item;
import com.smartlostfound.model.ItemStatus;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom table model providing structured tabular data of {@link Item} records
 * for the dashboard JTable. Supports polymorphic property rendering and fast row lookups.
 */
public class ItemTableModel extends AbstractTableModel {

    private static final String[] COLUMN_NAMES = {
            "ID", "Type", "Item Name", "Category", "Date", "Location", "Specifics", "Status", "Reported By"
    };

    private static final Class<?>[] COLUMN_CLASSES = {
            String.class, String.class, String.class, String.class, String.class,
            String.class, String.class, ItemStatus.class, String.class
    };

    private List<Item> items;

    /**
     * Constructs an empty table model.
     */
    public ItemTableModel() {
        this.items = new ArrayList<>();
    }

    /**
     * Constructs a table model with initial items.
     *
     * @param items Initial list of items
     */
    public ItemTableModel(List<Item> items) {
        this.items = (items != null) ? new ArrayList<>(items) : new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        if (column >= 0 && column < COLUMN_NAMES.length) {
            return COLUMN_NAMES[column];
        }
        return super.getColumnName(column);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex >= 0 && columnIndex < COLUMN_CLASSES.length) {
            return COLUMN_CLASSES[columnIndex];
        }
        return Object.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= items.size()) {
            return null;
        }

        Item item = items.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return item.getId();
            case 1:
                return item.getItemType();
            case 2:
                return item.getName();
            case 3:
                return item.getCategory().getDisplayName();
            case 4:
                return item.getFormattedDate();
            case 5:
                return item.getSpecificLocation();
            case 6:
                return item.getSpecificDetailsSummary();
            case 7:
                return item.getStatus();
            case 8:
                return item.getReporter() != null ? item.getReporter().getName() : "Anonymous";
            default:
                return "";
        }
    }

    /**
     * Retrieves the complete {@link Item} object corresponding to a given row.
     *
     * @param rowIndex Zero-based row index
     * @return The Item object at the index, or null if invalid
     */
    public Item getItemAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < items.size()) {
            return items.get(rowIndex);
        }
        return null;
    }

    /**
     * Updates the underlying items collection and fires model refresh.
     *
     * @param newItems Replacement items list
     */
    public void setItems(List<Item> newItems) {
        this.items = (newItems != null) ? new ArrayList<>(newItems) : new ArrayList<>();
        fireTableDataChanged();
    }

    /**
     * Returns an unmodifiable copy of the currently displayed items.
     *
     * @return List of current items
     */
    public List<Item> getItems() {
        return new ArrayList<>(items);
    }
}

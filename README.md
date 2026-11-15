# Smart Lost & Found Management System

A desktop application developed in **Java 11+** and **Java Swing**, implementing the **Model-View-Controller (MVC)** architectural pattern and strictly applying Object-Oriented Programming (OOP) principles: **Encapsulation, Inheritance, Polymorphism, and Abstraction**.

---

## 1. Architecture & Design Principles

### Model-View-Controller (MVC) Separation
```
+-------------------------------------------------------------+
|                            VIEW                             |
|  - MainFrame (Navigation Tabs, Status Bar)                  |
|  - DashboardPanel (JTable, KPI Cards, Filters, Actions)     |
|  - ReportLostItemPanel (Validated Lost Submission Form)     |
|  - ReportFoundItemPanel (Validated Found Submission Form)   |
|  - ItemDetailDialog (Full Modal Item Overview)              |
|  - UpdateStatusDialog (Lifecycle Transition Modal)          |
|  - ItemStatusCellRenderer (Modern Pill/Badge Renderer)      |
+------------------------------+------------------------------+
                               | Event Invocations / Updates
                               v
+-------------------------------------------------------------+
|                         CONTROLLER                          |
|  - LostFoundController                                      |
|    * Handles UI events, input validation, dialog flows      |
|    * Invokes business logic and persistence                 |
|    * Coordinates view refreshes and status notifications    |
+------------------------------+------------------------------+
                               | Business Logic & Queries
                               v
+-------------------------------------------------------------+
|                       MODEL & SERVICE                       |
|  - Item (Abstract Base Class)                               |
|    * LostItem (Subclass with location & reward)             |
|    * FoundItem (Subclass with custody storage & notes)      |
|  - User (Reporter / Custodian / Claimer)                    |
|  - ItemCategory (Enum classification)                       |
|  - ItemStatus (Enum lifecycle state & color mappings)       |
|  - LostFoundManager (Core Service & Business Engine)        |
+------------------------------+------------------------------+
                               | Atomic Save / Load
                               v
+-------------------------------------------------------------+
|                          STORAGE                            |
|  - ItemStorageService (Persistence Interface)               |
|  - CsvItemStorageService (RFC-4180 CSV Engine)              |
+-------------------------------------------------------------+
```

### Core OOP Principles Applied

1. **Abstraction**:
   - `Item` is defined as an `abstract` base class containing common domain attributes (`id`, `name`, `category`, `date`, `description`, `status`, `reporter`, `createdAt`) and declaring abstract methods:
     - `getItemType()`
     - `getSpecificLocation()`
     - `getSpecificDetailsSummary()`
     - `getSpecificField1()`
     - `getSpecificField2()`
   - `ItemStorageService` abstracts data persistence away from `LostFoundManager`, allowing swap between CSV, JSON, or SQL database engines without changing business logic.

2. **Inheritance**:
   - `LostItem extends Item`: Specializes the item with `locationLost` and `rewardAmount`.
   - `FoundItem extends Item`: Specializes the item with `locationFound`, physical `storageLocation` (e.g. security locker), and `finderContactInfo`.

3. **Polymorphism**:
   - `LostItem` and `FoundItem` implement abstract methods according to their specific context.
   - `ItemTableModel`, `ItemDetailDialog`, and `CsvItemStorageService` operate seamlessly against `Item` references without brittle `instanceof` cascades.
   - Dynamic dispatch automatically formats table column summaries (e.g. `"Reward: $50.00"` vs `"Stored: Central Security Office, Locker #08"`).

4. **Encapsulation**:
   - All class members are strictly `private`.
   - Mutators enforce validation (e.g. `setName` prohibits null/blank, `setRewardAmount` rejects negative values, `User` enforces contact consistency).
   - Defensive copies are utilized when returning lists from the manager (`Collections.unmodifiableList`).

---

## 2. Package Structure

```
smart-lost-found/
├── build.sh                                    # Automated build, test, jar packaging script
├── run.sh                                      # Execution launch script
├── SmartLostFound.jar                          # Self-contained executable JAR
├── data/
│   └── lost_found_items.csv                    # RFC-4180 CSV persistence file
├── docs/                                       # Generated JavaDoc documentation
├── test/
│   └── com/smartlostfound/
│       └── TestSuite.java                      # 53 comprehensive unit & integration tests
└── src/
    └── com/smartlostfound/
        ├── Main.java                           # Application entry point & EDT bootstrap
        ├── model/
        │   ├── Item.java                       # Abstract base class
        │   ├── LostItem.java                   # Subclass for lost property
        │   ├── FoundItem.java                  # Subclass for discovered property
        │   ├── User.java                       # Encapsulated reporter/claimer details
        │   ├── ItemCategory.java               # Categorization enum
        │   └── ItemStatus.java                 # Lifecycle status enum with badge styling
        ├── service/
        │   └── LostFoundManager.java           # Business logic, search, ID sequence & statistics
        ├── storage/
        │   ├── DataPersistenceException.java   # Checked persistence exception
        │   ├── ItemStorageService.java         # Storage abstraction interface
        │   └── CsvItemStorageService.java      # RFC-4180 multiline CSV reader/writer
        ├── controller/
        │   └── LostFoundController.java        # Central MVC Controller
        └── view/
            ├── UIUtils.java                    # Design tokens, color palette, button styling
            ├── MainFrame.java                  # Top-level window with tabs and status bar
            ├── DashboardPanel.java             # Central table, live search, filters, KPI cards
            ├── CommunityFeedPanel.java         # Facebook-style community social feed
            ├── ItemTableModel.java             # Custom AbstractTableModel
            ├── ItemStatusCellRenderer.java     # Rounded pill badge renderer
            ├── ReportLostItemPanel.java        # Form for reporting lost items
            ├── ReportFoundItemPanel.java       # Form for reporting found items
            ├── ItemDetailDialog.java           # Modal dialog with complete item breakdown
            └── UpdateStatusDialog.java         # Modal dialog for lifecycle status transitions
```

---

## 3. Features & Requirements Checklist

- [x] **Language & UI**: Java 11+ and standard Java Swing.
- [x] **MVC Architecture**: Models, Views, Controllers, and Services decoupled into explicit packages.
- [x] **Domain Model**: `Item` abstract base class, `LostItem`, `FoundItem`, `User`, and `LostFoundManager`.
- [x] **Facebook-Style Community Feed**:
  - Social media timeline stream with author avatar circles, relative date and location metadata, and item headline cards.
  - High-resolution picture banners for every post card, supporting custom photo uploads (`.jpg`, `.png`, `.webp`, `.gif`) or automatically generated high-contrast editorial product illustrations.
  - Visual badges for `LOST` and `FOUND`, reward banners, and safe custody storage notes.
  - Interactive reaction buttons: `👍 Looking Out` (with live counter), `💬 Contact Owner/Finder`, `🔄 Update Status`, and `View Full Post ➔`.
  - Top "What's on your mind?" composer quick actions: `[ ▲ Report Lost ]` and `[ ▼ Report Found ]`.
  - Quick filter chips: `[ All Posts ]`, `[ ▲ Lost Items ]`, `[ ▼ Found Items ]`, and `[ 💰 With Reward ]`.
- [x] **Photo & Image Attachment**:
  - Integrated `JFileChooser` on both Lost and Found reporting forms allowing users to browse and attach real photos.
  - Image scaling and cropping engine (`ImageHelper`) with bilinear downscaling and rounded corner clipping.
  - Images persist across application restarts in CSV storage.
- [x] **Distinct Reporting Forms**:
  - `ReportLostItemPanel`: Title, category, date lost, location lost, optional reward, narrative description, and owner contact details.
  - `ReportFoundItemPanel`: Title, category, date found, location discovered, physical holding/safe location, narrative description, and finder contact notes.
- [x] **Centralized Dashboard**:
  - `JTable` with custom table model (`ItemTableModel`).
  - Search engine supporting keyword filtering (ID, title, description, location, reporter).
  - Multi-criteria filter bars: Item Type (`ALL`, `LOST`, `FOUND`), Category dropdown, and Status dropdown.
  - 4 KPI analytical metric cards (Total Records, Active Lost, Active Found, Resolved).
  - Modern color-coded status badges for `LOST`, `FOUND`, `RECOVERED`, `CLAIMED`, and `ARCHIVED`.
- [x] **Status Management**:
  - Select item and launch "Update Status" modal dialog.
  - Update status (e.g. `LOST` $\rightarrow$ `RECOVERED` or `FOUND` $\rightarrow$ `CLAIMED`) with resolution remarks.
- [x] **Data Persistence**:
  - File-based persistence in `data/lost_found_items.csv` adhering to RFC-4180 (handling embedded commas, quotes, and multiline text).
  - Automatically loads on launch and auto-persists updates.
  - Pre-populated with realistic sample records if storage file is initialized empty.
- [x] **Input Validation & Error Handling**:
  - Pre-submission field checks preventing empty entries, invalid dates, negative numbers, or missing contact channels.
  - Clean `JOptionPane` warning/information/error dialogs.
- [x] **JavaDoc Documentation**: Clean HTML documentation generated in `docs/` covering classes, methods, and parameters.

---

## 4. How to Compile and Run

### Prerequisites
- JDK 11 or higher (`java -version`, `javac -version`).

### Method 1: Using the Automated Scripts (Recommended)

1. **Build and test the project:**
   ```bash
   ./build.sh
   ```
   *Compiles source code, runs 53 automated tests, generates JavaDoc, and creates `SmartLostFound.jar`.*

2. **Launch the desktop application:**
   ```bash
   ./run.sh
   ```

---

### Method 2: Manual Compilation via Command Line

1. **Compile all Java source files:**
   ```bash
   mkdir -p bin
   javac -d bin $(find src -name "*.java")
   ```

2. **Run the application:**
   ```bash
   java -cp bin com.smartlostfound.Main
   ```

3. **(Optional) Run the Automated Test Suite:**
   ```bash
   javac -d bin -cp bin $(find test -name "*.java")
   java -cp bin com.smartlostfound.TestSuite
   ```

4. **(Optional) Build Executable JAR:**
   ```bash
   jar cfe SmartLostFound.jar com.smartlostfound.Main -C bin com
   java -jar SmartLostFound.jar
   ```

5. **(Optional) Generate JavaDocs:**
   ```bash
   javadoc -d docs -sourcepath src -subpackages com.smartlostfound -Xdoclint:none -quiet
   ```

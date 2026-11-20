# Smart Lost & Found Management System

**A Java-Based Desktop Application for Efficient Lost and Found Item Management**

## Project Overview

The **Smart Lost & Found Management System** is a desktop application developed using **Java 11+ and Java Swing** to streamline the process of reporting, tracking, searching, and recovering lost and found items.

The application provides a centralized platform where users can report lost belongings, register discovered items, browse community submissions, and manage item recovery statuses through an intuitive graphical interface.

The system follows the **Model-View-Controller (MVC)** architectural pattern and applies the four fundamental Object-Oriented Programming (OOP) principles: Encapsulation, Inheritance, Polymorphism, and Abstraction.

It also features an interactive community feed, image attachments, advanced search and filtering, structured data persistence, and automated testing.

---

## 1. Technology Stack

| Component | Technology |
|---|---|
| Programming Language | Java 11+ |
| GUI Framework | Java Swing |
| Architecture | Model-View-Controller (MVC) |
| Programming Paradigm | Object-Oriented Programming |
| Data Persistence | CSV (RFC 4180) |
| Data Visualization | Java Swing JTable and Custom Renderers |
| Image Processing | Java AWT and ImageIO |
| Testing | Custom Automated Test Suite |
| Documentation | JavaDoc |
| Build System | Shell Scripts and JDK Tools |
| Packaging | Executable JAR |

---

## 2. System Architecture

The application follows a layered MVC architecture to maintain separation of concerns, modularity, and maintainability.

### 2.1 Architectural Overview

```text
+-----------------------------------------------------------+
|                         VIEW LAYER                        |
|                                                           |
|  MainFrame                                                |
|  DashboardPanel                                           |
|  CommunityFeedPanel                                       |
|  ReportLostItemPanel                                      |
|  ReportFoundItemPanel                                     |
|  ItemDetailDialog                                         |
|  UpdateStatusDialog                                       |
|  ItemTableModel                                           |
|  ItemStatusCellRenderer                                   |
+----------------------------+------------------------------+
                             |
                             | User Events / UI Updates
                             v
+-----------------------------------------------------------+
|                     CONTROLLER LAYER                     |
|                                                           |
|  LostFoundController                                      |
|                                                           |
|  - Handles user interactions                              |
|  - Validates input data                                   |
|  - Coordinates application workflows                     |
|  - Invokes business services                              |
|  - Updates application views                              |
+----------------------------+------------------------------+
                             |
                             | Business Operations
                             v
+-----------------------------------------------------------+
|                       MODEL LAYER                         |
|                                                           |
|  Item (Abstract Base Class)                               |
|      |                                                    |
|      +---- LostItem                                       |
|      +---- FoundItem                                      |
|                                                           |
|  User                                                     |
|  ItemCategory                                             |
|  ItemStatus                                               |
+----------------------------+------------------------------+
                             |
                             v
+-----------------------------------------------------------+
|                      SERVICE LAYER                        |
|                                                           |
|  LostFoundManager                                         |
|                                                           |
|  - Business rule enforcement                              |
|  - Item management                                        |
|  - Search and filtering                                   |
|  - Statistics and reporting                               |
|  - Identifier generation                                  |
+----------------------------+------------------------------+
                             |
                             v
+-----------------------------------------------------------+
|                     STORAGE LAYER                         |
|                                                           |
|  ItemStorageService (Interface)                           |
|      |                                                    |
|      +---- CsvItemStorageService                          |
|                                                           |
|  DataPersistenceException                                 |
+----------------------------+------------------------------+
                             |
                             v
+-----------------------------------------------------------+
|                       DATA STORAGE                        |
|                                                           |
|  data/lost_found_items.csv                                |
|                                                           |
|  Structured CSV Persistence                               |
+-----------------------------------------------------------+
```

### 2.2 Architectural Responsibilities

**View Layer**

Responsible for the graphical user interface, navigation, user input, data presentation, and interactive components.

**Controller Layer**

Acts as the communication bridge between the user interface and application services. It handles events, coordinates workflows, and manages input validation.

**Model Layer**

Defines the application's domain entities, attributes, relationships, and item lifecycle states.

**Service Layer**

Contains the core business logic, including item registration, searching, filtering, status management, and statistics generation.

**Storage Layer**

Provides an abstraction for data persistence and implements CSV-based storage operations independently of the business logic.

---

## 3. Object-Oriented Programming Principles

The system explicitly demonstrates all four fundamental OOP principles.

### 3.1 Abstraction

Abstraction is implemented through the abstract `Item` base class and the `ItemStorageService` interface.

- `Item` defines common attributes and abstract methods shared by lost and found items.
- `ItemStorageService` abstracts persistence operations from the business layer.
- The storage implementation can be extended to support alternative formats, such as JSON or relational databases, without redesigning the business logic.

### 3.2 Inheritance

Inheritance is implemented through specialized item classes.

```java
public abstract class Item {
    // Common item attributes and behaviors
}

public class LostItem extends Item {
    // Location lost and reward information
}

public class FoundItem extends Item {
    // Location found and custody information
}
```

- `LostItem` extends `Item` with `locationLost` and `rewardAmount`.
- `FoundItem` extends `Item` with `locationFound`, `storageLocation`, and `finderContactInfo`.

This approach promotes code reuse and reduces duplication.

### 3.3 Polymorphism

Polymorphism allows different item types to be processed through a common `Item` reference.

- `LostItem` and `FoundItem` provide their own implementations of abstract methods.
- `ItemTableModel` and `ItemDetailDialog` work with the common `Item` abstraction.
- `CsvItemStorageService` handles both item types through polymorphic operations.
- Dynamic method dispatch provides item-specific information without requiring repetitive type checks.

### 3.4 Encapsulation

Encapsulation protects application data and maintains object consistency.

- Class attributes are declared `private`.
- Public getters and validated setters control access to object properties.
- Invalid values, including blank names and negative reward amounts, are rejected.
- User contact information is validated.
- Defensive copies and unmodifiable collections prevent unintended external modification of managed data.

---

## 4. Project Structure

```text
smart-lost-found/
│
├── build.sh
├── run.sh
├── SmartLostFound.jar
│
├── data/
│   └── lost_found_items.csv
│
├── docs/
│   └── JavaDoc Documentation
│
├── test/
│   └── com/smartlostfound/
│       └── TestSuite.java
│
└── src/
    └── com/smartlostfound/
        │
        ├── Main.java
        │
        ├── model/
        │   ├── Item.java
        │   ├── LostItem.java
        │   ├── FoundItem.java
        │   ├── User.java
        │   ├── ItemCategory.java
        │   └── ItemStatus.java
        │
        ├── service/
        │   └── LostFoundManager.java
        │
        ├── storage/
        │   ├── DataPersistenceException.java
        │   ├── ItemStorageService.java
        │   └── CsvItemStorageService.java
        │
        ├── controller/
        │   └── LostFoundController.java
        │
        └── view/
            ├── UIUtils.java
            ├── MainFrame.java
            ├── DashboardPanel.java
            ├── CommunityFeedPanel.java
            ├── ItemTableModel.java
            ├── ItemStatusCellRenderer.java
            ├── ReportLostItemPanel.java
            ├── ReportFoundItemPanel.java
            ├── ItemDetailDialog.java
            └── UpdateStatusDialog.java
```

---

## 5. Core Features and Functional Requirements

### 5.1 Community Feed

This community feed allows users to browse lost and found item submissions in a familiar social-media-style interface.

Key capabilities:

- Timeline-based display of community posts.
- Reporter avatars and relative timestamps.
- Item descriptions and location information.
- Image banners and photo attachments.
- Visual indicators for LOST and FOUND posts.
- Reward information and secure custody details.
- Interactive Looking Out reaction with a live counter.
- Contact Owner/Finder action.
- Update Status functionality.
- Full post details.
- Quick reporting actions through a What's on your mind? composer.
- Category-based and item-type filtering.

### 5.2 Lost Item Reporting

A dedicated reporting form enables users to register missing belongings.

Information collected:

- Item title.
- Item category.
- Date lost.
- Location lost.
- Optional reward amount.
- Detailed description.
- Owner contact information.
- Supporting photographs.

### 5.3 Found Item Reporting

A separate reporting form allows users to register discovered belongings.

Information collected:

- Item title.
- Item category.
- Date found.
- Location discovered.
- Physical storage or custody location.
- Detailed description.
- Finder contact information.
- Supporting photographs.

### 5.4 Centralized Management Dashboard

The dashboard provides a consolidated overview of registered items.

Features include:

- Interactive JTable-based item listing.
- Keyword-based search.
- Search by item ID, title, description, location, and reporter.
- Item-type filtering.
- Category filtering.
- Status filtering.
- Four KPI cards:
  - Total Records.
  - Active Lost Items.
  - Active Found Items.
  - Resolved Items.
- Color-coded item status indicators.
- Item details and management actions.

### 5.5 Item Status Management

The application supports lifecycle-based item status management.

Supported statuses:

| Status | Description |
|---|---|
| LOST | An item has been reported missing. |
| FOUND | An item has been discovered and registered. |
| RECOVERED | A lost item has been successfully recovered. |
| CLAIMED | A found item has been claimed. |
| ARCHIVED | A record has been archived. |

Users can update item statuses through a dedicated modal dialog and provide resolution remarks.

### 5.6 Image and Attachment Management

The application supports image attachments for lost and found item reports.

Supported formats:

- JPG / JPEG
- PNG
- WEBP
- GIF

Image-related capabilities:

- File selection through `JFileChooser`.
- Image scaling and downsampling.
- Bilinear image interpolation.
- Rounded-corner image rendering.
- Image persistence across application restarts through CSV storage.

### 5.7 Data Persistence

The system uses CSV-based file persistence to maintain application records.

Implementation highlights:

- RFC 4180-compliant CSV processing.
- Handling of embedded commas, quotation marks, and multiline text.
- Automatic loading of stored records during application startup.
- Automatic persistence of application updates.
- Initial sample data for an empty storage file.
- Storage abstraction through `ItemStorageService`.

### 5.8 Input Validation and Error Handling

Input validation helps maintain data integrity and prevent invalid submissions.

Validation includes:

- Required-field verification.
- Blank and null value prevention.
- Date validation.
- Negative reward amount prevention.
- Contact information validation.
- Appropriate error, warning, and confirmation dialogs using `JOptionPane`.
- Checked persistence exceptions for storage-related failures.

---

## 6. Testing and Documentation

### 6.1 Automated Testing

The project includes a custom automated test suite containing **53 unit and integration test cases**.

The test suite is located at:

```text
test/com/smartlostfound/TestSuite.java
```

### 6.2 JavaDoc Documentation

JavaDoc documentation is generated to describe the application's classes, methods, parameters, and software structure.

Generated documentation is available in:

```text
docs/
```

### 6.3 Build Automation

The project includes shell scripts to simplify compilation, testing, documentation generation, and executable JAR packaging.

- `build.sh` – Automates the build and packaging workflow.
- `run.sh` – Launches the desktop application.

---

## 7. Installation and Execution

### 7.1 System Requirements

Before running the application, ensure that the following requirements are met:

- Java Development Kit (JDK) 11 or higher.
- A supported operating system with a graphical desktop environment.
- Terminal or command-line access.

Verify Java installation:

```bash
java -version
javac -version
```

### 7.2 Recommended Method: Automated Build

**Step 1: Navigate to the project directory.**

```bash
cd smart-lost-found
```

**Step 2: Make the scripts executable.**

```bash
chmod +x build.sh run.sh
```

**Step 3: Build the application.**

```bash
./build.sh
```

The build process compiles the source code, executes the automated test suite, generates JavaDoc documentation, and packages the application into an executable JAR.

**Step 4: Launch the application.**

```bash
./run.sh
```

### 7.3 Alternative Method: Manual Compilation

Compile the application:

```bash
mkdir -p bin
javac -d bin $(find src -name "*.java")
```

Launch the application:

```bash
java -cp bin com.smartlostfound.Main
```

### 7.4 Execute the Automated Test Suite

```bash
javac -d bin -cp bin $(find test -name "*.java")
java -cp bin com.smartlostfound.TestSuite
```

### 7.5 Generate JavaDoc Documentation

```bash
javadoc -d docs \
-sourcepath src \
-subpackages com.smartlostfound \
-Xdoclint:none \
-quiet
```

### 7.6 Create an Executable JAR

```bash
jar cfe SmartLostFound.jar \
com.smartlostfound.Main -C bin com
```

Run the packaged application:

```bash
java -jar SmartLostFound.jar
```

---

## 8. Design and Implementation Highlights

The project emphasizes the following software engineering practices:

1. **Separation of Concerns:** Independent architectural layers for presentation, business logic, domain models, and persistence.
2. **Modular Design:** Organized packages and clearly defined class responsibilities.
3. **Code Reusability:** Shared item attributes and behaviors through inheritance and abstraction.
4. **Maintainability:** Consistent naming conventions, encapsulation, and centralized business logic.
5. **Data Integrity:** Validated inputs and structured persistence operations.
6. **User Experience:** Interactive desktop components, visual status indicators, and an accessible reporting workflow.
7. **Extensibility:** Storage abstraction and modular architecture support future enhancements.
8. **Testability:** Automated tests help verify core functionality and application behavior.

---

## 9. Future Enhancement Opportunities

The current implementation provides a foundation for further development. Potential future improvements include:

- Database integration using MySQL or PostgreSQL.
- User authentication and role-based access control.
- Real-time notifications for potential item matches.
- Intelligent lost and found item matching.
- Advanced image-based item similarity detection.
- Secure in-app messaging between owners and finders.
- Administrative management and analytics.
- Cloud-based image storage and backup.
- Web and mobile application integration.

*These are proposed enhancements and are not part of the currently documented implementation.*

---

## 10. Conclusion

The **Smart Lost & Found Management System** demonstrates the practical application of Java-based desktop software development, Object-Oriented Programming, MVC architecture, and structured data persistence.

By integrating item reporting, community-based browsing, centralized management, image attachments, and lifecycle tracking, the application provides a structured solution for managing lost and found belongings.

The modular architecture, reusable domain models, automated testing, and documentation establish a maintainable foundation for future development and expansion.

**Project Type:** Academic Software Engineering Project  
**Application Type:** Desktop Application  
**Language:** Java 11+  
**GUI Framework:** Java Swing  
**Architecture:** MVC  
**Data Storage:** CSV  
**Testing:** 53 Automated Test Cases
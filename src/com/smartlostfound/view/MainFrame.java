package com.smartlostfound.view;

import com.smartlostfound.controller.LostFoundController;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main application window for the Smart Lost &amp; Found Management System.
 * Hosts the top navigation tabs, header bar, dashboard, reporting panels,
 * and operational status bar.
 */
public class MainFrame extends JFrame {

    private final LostFoundController controller;

    private JTabbedPane tabbedPane;
    private DashboardPanel dashboardPanel;
    private ReportLostItemPanel reportLostPanel;
    private ReportFoundItemPanel reportFoundPanel;
    private JLabel statusMessageLabel;
    private JLabel storagePathLabel;

    public MainFrame(LostFoundController controller) {
        super("Smart Lost & Found Management System");
        this.controller = controller;

        initComponents();
        setupWindow();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.COLOR_BACKGROUND);

        // Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Tabbed View
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(UIUtils.FONT_TITLE);
        tabbedPane.setBackground(UIUtils.COLOR_BACKGROUND);

        dashboardPanel = new DashboardPanel(controller);
        reportLostPanel = new ReportLostItemPanel(controller);
        reportFoundPanel = new ReportFoundItemPanel(controller);

        tabbedPane.addTab("  Dashboard & Records  ", dashboardPanel);
        tabbedPane.addTab("  Report Lost Item  ", reportLostPanel);
        tabbedPane.addTab("  Report Found Item  ", reportFoundPanel);

        // When switching tabs, if switching to dashboard, refresh filter
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 0) {
                controller.refreshView();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom Status Bar
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.COLOR_HEADER_BG);
        header.setBorder(new EmptyBorder(14, 24, 14, 24));

        // Brand & Title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel logoText = new JLabel("Smart Lost & Found");
        logoText.setFont(UIUtils.FONT_HEADER);
        logoText.setForeground(Color.WHITE);

        JLabel badge = new JLabel(" ENTERPRISE EDITION ");
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));
        badge.setOpaque(true);
        badge.setBackground(UIUtils.COLOR_PRIMARY);
        badge.setForeground(Color.WHITE);
        badge.setBorder(new EmptyBorder(3, 6, 3, 6));

        left.add(logoText);
        left.add(badge);

        // Right details
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        storagePathLabel = new JLabel("Storage: " + controller.getStoragePathDescription());
        storagePathLabel.setFont(UIUtils.FONT_SMALL);
        storagePathLabel.setForeground(new Color(148, 163, 184));

        right.add(storagePathLabel);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Color.WHITE);
        statusBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(6, 16, 6, 16)
        ));

        statusMessageLabel = new JLabel("System Ready. All records synchronized.");
        statusMessageLabel.setFont(UIUtils.FONT_SMALL);
        statusMessageLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        JLabel timestampLabel = new JLabel("Session started: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        timestampLabel.setFont(UIUtils.FONT_SMALL);
        timestampLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        statusBar.add(statusMessageLabel, BorderLayout.WEST);
        statusBar.add(timestampLabel, BorderLayout.EAST);

        return statusBar;
    }

    private void setupWindow() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.handleApplicationExit(MainFrame.this);
            }
        });

        setSize(1150, 780);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
    }

    public void setStatusMessage(String message) {
        if (statusMessageLabel != null) {
            statusMessageLabel.setText(message);
        }
    }

    public void switchToDashboard() {
        if (tabbedPane != null) {
            tabbedPane.setSelectedIndex(0);
        }
    }

    public void switchToReportLost() {
        if (tabbedPane != null) {
            tabbedPane.setSelectedIndex(1);
        }
    }

    public void switchToReportFound() {
        if (tabbedPane != null) {
            tabbedPane.setSelectedIndex(2);
        }
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public ReportLostItemPanel getReportLostPanel() {
        return reportLostPanel;
    }

    public ReportFoundItemPanel getReportFoundPanel() {
        return reportFoundPanel;
    }
}

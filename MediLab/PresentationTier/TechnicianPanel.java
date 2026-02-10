package PresentationTier;

import ApplicationTier.Model.Employee;

import javax.swing.*;
import java.awt.*;

public class TechnicianPanel extends JPanel {
    private final TechnicianDashboard dashboard;

    public TechnicianPanel(Employee user) {
        super(new BorderLayout());
        dashboard = new TechnicianDashboard(user);
        JPanel main = dashboard.getMainPanel();
        Container p = main.getParent(); if (p != null) p.remove(main);
        add(main, BorderLayout.CENTER);
        dashboard.dispose();
    }

    public void refreshTodayOrders() { dashboard.refreshTodayOrders(); }
    public void refreshWorklistTable() { dashboard.refreshWorklistTable(); }
    public void refreshBenchTable() { dashboard.refreshBenchTable(); }
    public void refreshHistoryTable() { dashboard.refreshHistoryTable(); }
    public void refreshCatalogTable() { dashboard.refreshCatalogTable(); }

    public void showSubPanel(String key) { try { dashboard.showSubPanel(key); } catch (Exception ignored) {} }
}

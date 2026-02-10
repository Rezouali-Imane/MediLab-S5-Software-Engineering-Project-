package PresentationTier;

import ApplicationTier.Model.Employee;

import javax.swing.*;
import java.awt.*;

public class FinancialPanel extends JPanel {
    private final FinancialManagerDashboard dashboard;

    public FinancialPanel(Employee user) {
        super(new BorderLayout());
        dashboard = new FinancialManagerDashboard(user);
        JPanel main = dashboard.getMainPanel();
        Container p = main.getParent(); if (p != null) p.remove(main);
        add(main, BorderLayout.CENTER);
    }

    public void refreshStats() { dashboard.refreshStats(); }
    public void showSubPanel(String key) { try { dashboard.showSubPanel(key); } catch (Exception ignored) {} }
}

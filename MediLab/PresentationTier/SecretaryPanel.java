package PresentationTier;

import ApplicationTier.Model.Employee;
import javax.swing.*;
import java.awt.*;


public class SecretaryPanel extends JPanel {
    private final SecretaryDashboard dashboard;

    public SecretaryPanel(Employee user) {
        super(new BorderLayout());
        dashboard = new SecretaryDashboard(user);
        JPanel main = dashboard.getMainPanel();
        Container p = main.getParent();
        if (p != null) p.remove(main);
        add(main, BorderLayout.CENTER);
        dashboard.dispose();
    }

    public void refreshPatientTable() { dashboard.refreshPatientTable(); }
    public void refreshApptTable() { dashboard.refreshApptTable(); }
    public void refreshTodayAppointments() { dashboard.refreshTodayAppointments(); }
    public void refreshDeliveryTable() { dashboard.refreshDeliveryTable(); }
    public void refreshInvoicesTable() { dashboard.refreshInvoicesTable(); }

    public void showSubPanel(String key) {
        try { dashboard.showSubPanel(key); } catch (Exception ignored) {}
    }
}

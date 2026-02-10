package PresentationTier;

import ApplicationTier.Model.*;
import ApplicationTier.Model.Enums.Role;
import javax.swing.*;

public class DashboardRouter {

    public static void routeUser(Employee user) {
        Role role = user.getRole();
        System.out.println("Routing user: " + user.getUsername() + " to " + role + " Dashboard.");

        try {
            switch (role) {
                case SECRETARY -> new SecretaryDashboard(user).setVisible(true);
                case TECHNICIAN -> new TechnicianDashboard(user).setVisible(true);
                case FINANCIAL_MANAGER -> new FinancialManagerDashboard(user).setVisible(true);
                case ADMIN -> new AdminDashboard(user).setVisible(true);
                case SUPER_ADMIN -> new SuperAdminDashboard(user).setVisible(true);
                default -> JOptionPane.showMessageDialog(null, "Role not recognized.");
            }
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(null, "Failed to open dashboard: " + t.getMessage());
        }
    }
}

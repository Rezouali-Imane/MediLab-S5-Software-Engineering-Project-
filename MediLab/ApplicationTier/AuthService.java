package ApplicationTier;
import ApplicationTier.Model.*;
import DAO.*;

public class AuthService {

    private final EmployeeDAO employeeDAO;
        private final LoginAuditDAO auditDAO;

    public AuthService() {
        this.employeeDAO = new EmployeeDAO();
        this.auditDAO = new LoginAuditDAO();
    }

    public Employee login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            System.out.println("Login Error: Empty credentials");
            // record audit
            try { ApplicationTier.Model.LoginAudit la = new ApplicationTier.Model.LoginAudit(username, false, "Empty credentials", new java.util.Date(), null); auditDAO.createAudit(la); } catch (Throwable ignored) {}
            return null;
        }


        try {
            Employee user = employeeDAO.login(username, password);
            if (user != null) {
                System.out.println("User Logged in: " + user.getUsername() + " [" + user.getRole() + "]");
                try { ApplicationTier.Model.LoginAudit la = new ApplicationTier.Model.LoginAudit(user.getUsername(), true, "Login successful", new java.util.Date(), null); auditDAO.createAudit(la); } catch (Throwable ignored) {}
                return user;
            } else {
                System.out.println("Login Error: Invalid username or password");
                try { ApplicationTier.Model.LoginAudit la = new ApplicationTier.Model.LoginAudit(username, false, "Invalid credentials", new java.util.Date(), null); auditDAO.createAudit(la); } catch (Throwable ignored) {}
                return null;
            }
        } catch (Throwable t) {

            System.err.println("Authentication failed due to DB error: " + t.getMessage());
            try { ApplicationTier.Model.LoginAudit la = new ApplicationTier.Model.LoginAudit(username, false, "DB error: " + t.getMessage(), new java.util.Date(), null); auditDAO.createAudit(la); } catch (Throwable ignored) {}
            return null;
        }
    }
}
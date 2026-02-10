package DAO;
import ApplicationTier.Model.Employee;
import ApplicationTier.Model.Enums.Role;
import ApplicationTier.Model.Admin;
import ApplicationTier.Model.FinancialManager;
import ApplicationTier.Model.Secretary;
import ApplicationTier.Model.SuperAdmin;
import ApplicationTier.Model.Technician;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmployeeDAO {

    private static final Logger LOGGER = Logger.getLogger(EmployeeDAO.class.getName());

    // login
    public Employee login(String username, String password) {
        String sql = "SELECT * FROM Employee WHERE username = ? AND password = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                return null;
            }
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "login failed", e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
        }
        return null;
    }

    //create
    public boolean addEmployee(Employee emp) {
        String sql = "INSERT INTO Employee (firstName, lastName, username, password, role, hireDate, phone, address ,email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, emp.getFirstName());
            stmt.setString(2, emp.getLastName());
            stmt.setString(3, emp.getUsername());
            stmt.setString(4, emp.getPassword());
            stmt.setString(5, emp.getRole().toString());
            stmt.setDate(6, new java.sql.Date(emp.getHireDate().getTime()));
            stmt.setString(7, emp.getPhone());
            stmt.setString(8, emp.getAddress());
            stmt.setString(9, emp.getEmail());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.WARNING, "addEmployee failed", e);
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
        }
    }

    // get all employees
    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM Employee";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return list;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "getAllEmployees failed", e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
        }
        return list;
    }

    // delete
    public boolean deleteEmployee(int id) {
        String sql = "DELETE FROM Employee WHERE employeeId = ?";
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.WARNING, "deleteEmployee failed", e);
            return false;
        } finally { try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {} }
    }

    // update
    public boolean updateEmployee(Employee emp) {
        String sql = "UPDATE Employee SET firstName=?, lastName=?, username=?, password=?, role=?, phone=?, email=?, address=? WHERE employeeId=?";
        Connection conn = null; PreparedStatement stmt = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, emp.getFirstName());
            stmt.setString(2, emp.getLastName());
            stmt.setString(3, emp.getUsername());
            stmt.setString(4, emp.getPassword());
            stmt.setString(5, emp.getRole().toString());
            stmt.setString(6, emp.getPhone());
            stmt.setString(7, emp.getEmail());
            stmt.setString(8, emp.getAddress());
            stmt.setInt(9, emp.getEmployeeId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(java.util.logging.Level.WARNING, "updateEmployee failed", e);
            return false;
        } finally { try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {} }
    }

    // map row to Employee object with Factory Pattern
    private Employee mapRow(ResultSet rs) throws SQLException {
        String roleStr = rs.getString("role");
        Role role = Role.valueOf(roleStr.toUpperCase());

        Employee emp;
        switch (role) {
            case SUPER_ADMIN:
                emp = new SuperAdmin();
                break;
            case ADMIN:
                emp = new Admin();
                break;
            case SECRETARY:
                emp = new Secretary();
                break;
            case TECHNICIAN:
                emp = new Technician();
                break;
            case FINANCIAL_MANAGER:
                emp = new FinancialManager();
                break;
            default:
                emp = new Employee();
                break;
        }

        // Common attributes for all employees
        emp.setEmployeeId(rs.getInt("employeeId"));
        emp.setFirstName(rs.getString("firstName"));
        emp.setLastName(rs.getString("lastName"));
        emp.setUsername(rs.getString("username"));
        emp.setPassword(rs.getString("password"));
        emp.setRole(role);
        emp.setHireDate(rs.getDate("hireDate"));
        emp.setPhone(rs.getString("phone"));
        emp.setAddress(rs.getString("address"));
        emp.setEmail(rs.getString("email"));

        return emp;
    }
}
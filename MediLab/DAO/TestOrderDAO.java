package DAO;
import ApplicationTier.Model.TestOrder;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestOrderDAO {

    private static final Logger LOGGER = Logger.getLogger(TestOrderDAO.class.getName());

    public int createOrder(TestOrder order) {
        boolean hasAppointmentId = order.getAppointmentId() > 0;
        String sql;
        if (hasAppointmentId) {
            sql = "INSERT INTO TestOrder (patientId, technicianId, appointmentId, status) VALUES (?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO TestOrder (patientId, technicianId, status) VALUES (?, ?, ?)";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, order.getPatientId());
            if (order.getTechnicianId() > 0) stmt.setInt(2, order.getTechnicianId());
            else stmt.setNull(2, java.sql.Types.INTEGER);

            int paramIndex = 3;
            if (hasAppointmentId) {
                stmt.setInt(paramIndex++, order.getAppointmentId());
            }
            stmt.setString(paramIndex, order.getStatus() != null ? order.getStatus().toString() : ApplicationTier.Model.Enums.TestOrderStatus.CREATED.toString());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "createOrder failed", e); }
        return -1;
    }

    public List<TestOrder> getPendingOrders() {
        List<TestOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM TestOrder WHERE status != 'COMPLETED'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "getPendingOrders failed", e); }
        return list;
    }


    public List<TestOrder> getOrdersByAppointment(int appointmentId) {
        List<TestOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM TestOrder WHERE appointmentId = ? ORDER BY dateOrdered DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.FINER, "getOrdersByAppointment query failed (maybe missing appointmentId): " + e.getMessage(), e);
        }
        return list;
    }

    public boolean updateStatus(int orderId, String status) {
        String sql = "UPDATE TestOrder SET status = ? WHERE orderId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "updateStatus failed", e); return false; }
    }


    public boolean updateTechnician(int orderId, int technicianId) {
        String sql = "UPDATE TestOrder SET technicianId = ? WHERE orderId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (technicianId > 0) stmt.setInt(1, technicianId); else stmt.setNull(1, java.sql.Types.INTEGER);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "updateTech failed", e); return false; }
    }

    public boolean updateAppointmentId(int orderId, int appointmentId) {
        String sql = "UPDATE TestOrder SET appointmentId = ? WHERE orderId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (appointmentId > 0) stmt.setInt(1, appointmentId); else stmt.setNull(1, java.sql.Types.INTEGER);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "updateAppointmentId failed", e); return false; }
    }


    public List<TestOrder> getOrdersForPatient(int patientId) {
        List<TestOrder> list = new ArrayList<>();
        String sql = "SELECT * FROM TestOrder WHERE patientId = ? ORDER BY dateOrdered DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "getOrdersForPatient failed", e); }
        return list;
    }


    public TestOrder getOrderById(int orderId) {
        String sql = "SELECT * FROM TestOrder WHERE orderId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "getOrderById failed", e); }
        return null;
    }


    public int countOrdersByStatus(String status) {
        String sql = "SELECT COUNT(*) AS c FROM TestOrder WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("c");
            }
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "countOrdersByStatus failed", e); }
        return 0;
    }


    private TestOrder mapRow(ResultSet rs) throws SQLException {
        TestOrder o = new TestOrder();
        o.setOrderId(rs.getInt("orderId"));
        o.setPatientId(rs.getInt("patientId"));
        o.setTechnicianId(rs.getInt("technicianId"));
        o.setDateOrdered(rs.getTimestamp("dateOrdered"));
        o.setStatus(rs.getString("status"));
        try { o.setAppointmentId(rs.getInt("appointmentId")); } catch (Exception ignored) {}
        return o;
    }
}

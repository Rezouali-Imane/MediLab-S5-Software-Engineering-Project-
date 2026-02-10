package DAO;
import ApplicationTier.Model.Invoice;
import DAO.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(InvoiceDAO.class.getName());

    public boolean createInvoice(Invoice inv) {
        String sql = "INSERT INTO Invoice (orderId, patientId, totalAmount, paymentStatus) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, inv.getOrderId());
            stmt.setInt(2, inv.getPatientId());
            stmt.setDouble(3, inv.getTotalAmount());
            stmt.setString(4, inv.getPaymentStatus() != null ? inv.getPaymentStatus().toString() : null);
            int affected = stmt.executeUpdate();
            if (affected <= 0) return false;
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys != null && keys.next()) {
                    int id = keys.getInt(1);
                    inv.setInvoiceId(id);
                }
            } catch (SQLException ignore) {}
            return true;
        } catch (SQLException e) { LOGGER.log(java.util.logging.Level.WARNING, "createInvoice failed for orderId=" + (inv==null?"?":inv.getOrderId()), e); return false; }
    }

    public boolean updatePaymentStatus(int invoiceId, String status) {
        String sql = "UPDATE Invoice SET paymentStatus = ? WHERE invoiceId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, invoiceId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public Invoice getInvoiceByOrder(int orderId) {
        String sql = "SELECT * FROM Invoice WHERE orderId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                Invoice i = new Invoice();
                i.setInvoiceId(rs.getInt("invoiceId"));
                i.setOrderId(rs.getInt("orderId"));
                i.setPatientId(rs.getInt("patientId"));
                i.setTotalAmount(rs.getDouble("totalAmount"));
                i.setPaymentStatus(rs.getString("paymentStatus"));
                i.setDate(rs.getTimestamp("date"));
                return i;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Invoice getInvoiceById(int invoiceId) {
        String sql = "SELECT * FROM Invoice WHERE invoiceId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, invoiceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) {
                    Invoice i = new Invoice();
                    i.setInvoiceId(rs.getInt("invoiceId"));
                    i.setOrderId(rs.getInt("orderId"));
                    i.setPatientId(rs.getInt("patientId"));
                    i.setTotalAmount(rs.getDouble("totalAmount"));
                    i.setPaymentStatus(rs.getString("paymentStatus"));
                    i.setDate(rs.getTimestamp("date"));
                    return i;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Invoice> listAllInvoices() {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM Invoice ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                Invoice i = new Invoice();
                i.setInvoiceId(rs.getInt("invoiceId"));
                i.setOrderId(rs.getInt("orderId"));
                i.setPatientId(rs.getInt("patientId"));
                i.setTotalAmount(rs.getDouble("totalAmount"));
                i.setPaymentStatus(rs.getString("paymentStatus"));
                i.setDate(rs.getTimestamp("date"));
                list.add(i);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateInvoice(int invoiceId, double amount, String paymentStatus) {
        String sql = "UPDATE Invoice SET totalAmount = ?, paymentStatus = ? WHERE invoiceId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setString(2, paymentStatus);
            stmt.setInt(3, invoiceId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteInvoice(int invoiceId) {
        String sql = "DELETE FROM Invoice WHERE invoiceId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, invoiceId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
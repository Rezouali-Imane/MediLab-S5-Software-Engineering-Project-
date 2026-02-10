package DAO;
import ApplicationTier.Model.TestResult;
import DAO.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestResultDAO {

    private static final Logger LOGGER = Logger.getLogger(TestResultDAO.class.getName());

    public boolean addResult(TestResult res) {
        String checkSql = "SELECT 1 FROM TestOrder WHERE orderId = ?";
        String sql = "INSERT INTO TestResult (orderId, testTypeId, technicianId, isValidated) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement chk = conn.prepareStatement(checkSql)) {

            chk.setInt(1, res.getOrderId());
            try (ResultSet rs = chk.executeQuery()) {
                if (!rs.next()) {
                    LOGGER.warning("Attempted to add TestResult for non-existent orderId=" + res.getOrderId());
                    return false;
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, res.getOrderId());
                stmt.setInt(2, res.getTestTypeId());
                stmt.setInt(3, res.getTechnicianId());
                stmt.setBoolean(4, false); // Not validated yet
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "addResult failed", e); return false; }
    }


    public boolean updateResultValue(int resultId, String value, String interpretation) {
        String sql = "UPDATE TestResult SET value = ?, interpretation = ?, resultDate = NOW() WHERE resultId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setString(2, interpretation);
            stmt.setInt(3, resultId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "updateResultValue failed", e); return false; }
    }


    public boolean validateResult(int resultId) {
        String sql = "UPDATE TestResult SET isValidated = TRUE WHERE resultId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, resultId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "validateResult failed", e); return false; }
    }


    public List<TestResult> getResultsByOrder(int orderId) {
        List<TestResult> list = new ArrayList<>();
        String sql = "SELECT tr.*, tt.name as testName FROM TestResult tr " +
                "JOIN TestType tt ON tr.testTypeId = tt.testTypeId " +
                "WHERE tr.orderId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TestResult r = new TestResult();
                r.setResultId(rs.getInt("resultId"));
                r.setOrderId(rs.getInt("orderId"));
                r.setTestTypeId(rs.getInt("testTypeId"));
                r.setTechnicianId(rs.getInt("technicianId"));
                r.setValue(rs.getString("value"));
                r.setInterpretation(rs.getString("interpretation"));
                r.setValidated(rs.getBoolean("isValidated"));
                r.setResultDate(rs.getTimestamp("resultDate"));
                r.setTestName(rs.getString("testName")); // From JOIN
                list.add(r);
            }
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "getResultsByOrder failed", e); }
        return list;
    }

    public List<TestResult> getPendingResultsByTechnician(int technicianId) {
        List<TestResult> list = new ArrayList<>();
        String sql = "SELECT tr.*, tt.name as testName FROM TestResult tr " +
                "JOIN TestType tt ON tr.testTypeId = tt.testTypeId " +
                "WHERE tr.technicianId = ? AND tr.isValidated = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, technicianId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TestResult r = new TestResult();
                r.setResultId(rs.getInt("resultId"));
                r.setOrderId(rs.getInt("orderId"));
                r.setTestTypeId(rs.getInt("testTypeId"));
                r.setTechnicianId(rs.getInt("technicianId"));
                r.setValue(rs.getString("value"));
                r.setInterpretation(rs.getString("interpretation"));
                r.setValidated(rs.getBoolean("isValidated"));
                r.setResultDate(rs.getTimestamp("resultDate"));
                r.setTestName(rs.getString("testName")); // From JOIN
                list.add(r);
            }
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "getPendingResultsByTechnician failed", e); }
        return list;
    }
    public int countValidatedResults() {
        String sql = "SELECT COUNT(*) AS c FROM TestResult WHERE isValidated = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("c");
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "countValidatedResults failed", e); }
        return 0;
    }

    public TestResult getResultById(int resultId) {
        String sql = "SELECT tr.*, tt.name as testName FROM TestResult tr " +
                "JOIN TestType tt ON tr.testTypeId = tt.testTypeId " +
                "WHERE tr.resultId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resultId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                TestResult r = new TestResult();
                r.setResultId(rs.getInt("resultId"));
                r.setOrderId(rs.getInt("orderId"));
                r.setTestTypeId(rs.getInt("testTypeId"));
                r.setTechnicianId(rs.getInt("technicianId"));
                r.setValue(rs.getString("value"));
                r.setInterpretation(rs.getString("interpretation"));
                r.setValidated(rs.getBoolean("isValidated"));
                r.setResultDate(rs.getTimestamp("resultDate"));
                r.setTestName(rs.getString("testName")); // From JOIN
                return r;
            }
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "getResultById failed", e); }
        return null;
    }



    public List<TestResult> getResultsByTechnicianAll(int technicianId) {
        List<TestResult> list = new ArrayList<>();
        String sql = "SELECT tr.*, tt.name as testName FROM TestResult tr " +
                "JOIN TestType tt ON tr.testTypeId = tt.testTypeId " +
                "WHERE tr.technicianId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, technicianId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TestResult r = new TestResult();
                r.setResultId(rs.getInt("resultId"));
                r.setOrderId(rs.getInt("orderId"));
                r.setTestTypeId(rs.getInt("testTypeId"));
                r.setTechnicianId(rs.getInt("technicianId"));
                r.setValue(rs.getString("value"));
                r.setInterpretation(rs.getString("interpretation"));
                r.setValidated(rs.getBoolean("isValidated"));
                r.setResultDate(rs.getTimestamp("resultDate"));
                r.setTestName(rs.getString("testName")); // From JOIN
                list.add(r);
            }
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "getResultsByTechnicianAll failed", e); }
        return list;
    }

    public List<TestResult> getPendingResultsAll() {
        List<TestResult> list = new ArrayList<>();
        String sql = "SELECT tr.*, tt.name as testName FROM TestResult tr " +
                "JOIN TestType tt ON tr.testTypeId = tt.testTypeId " +
                "WHERE tr.isValidated = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TestResult r = new TestResult();
                r.setResultId(rs.getInt("resultId"));
                r.setOrderId(rs.getInt("orderId"));
                r.setTestTypeId(rs.getInt("testTypeId"));
                r.setTechnicianId(rs.getInt("technicianId"));
                r.setValue(rs.getString("value"));
                r.setInterpretation(rs.getString("interpretation"));
                r.setValidated(rs.getBoolean("isValidated"));
                r.setResultDate(rs.getTimestamp("resultDate"));
                r.setTestName(rs.getString("testName"));
                list.add(r);
            }
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "getPendingResultsAll failed", e); }
        return list;
    }


    public List<TestResult> getAllResultsAll() {
        List<TestResult> list = new ArrayList<>();
        String sql = "SELECT tr.*, tt.name as testName FROM TestResult tr " +
                "JOIN TestType tt ON tr.testTypeId = tt.testTypeId";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TestResult r = new TestResult();
                r.setResultId(rs.getInt("resultId"));
                r.setOrderId(rs.getInt("orderId"));
                r.setTestTypeId(rs.getInt("testTypeId"));
                r.setTechnicianId(rs.getInt("technicianId"));
                r.setValue(rs.getString("value"));
                r.setInterpretation(rs.getString("interpretation"));
                r.setValidated(rs.getBoolean("isValidated"));
                r.setResultDate(rs.getTimestamp("resultDate"));
                r.setTestName(rs.getString("testName"));
                list.add(r);
            }
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "getAllResultsAll failed", e); }
        return list;
    }
}


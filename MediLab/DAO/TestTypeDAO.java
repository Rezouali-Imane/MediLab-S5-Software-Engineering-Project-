package DAO;
import ApplicationTier.Model.TestType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestTypeDAO {

    private static final Logger LOGGER = Logger.getLogger(TestTypeDAO.class.getName());

    public List<TestType> getAllTestTypes() {
        List<TestType> tests = new ArrayList<>();
        String sql = "SELECT * FROM TestType";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tests.add(mapResultSetToTestType(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "getAllTestTypes failed", e);
        }
        return tests;
    }


    public List<TestType> getTestTypesByCategory(int categoryId) {
        List<TestType> tests = new ArrayList<>();
        String sql = "SELECT * FROM TestType WHERE categoryId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tests.add(mapResultSetToTestType(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "getTestTypesByCategory failed", e);
        }
        return tests;
    }

    private TestType mapResultSetToTestType(ResultSet rs) throws SQLException {
        TestType t = new TestType();
        t.setTestTypeId(rs.getInt("testTypeId"));
        t.setCategoryId(rs.getInt("categoryId"));
        t.setName(rs.getString("name"));
        t.setDescription(rs.getString("description"));
        t.setPrice(rs.getDouble("price"));
        t.setNormalRange(rs.getString("normalRange"));
        return t;
    }

    public TestType getTestTypeById(int testTypeId) {
        String sql = "SELECT * FROM TestType WHERE testTypeId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, testTypeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToTestType(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "getTestTypeById failed", e);
        }
        return null;
    }
}
package DAO;
import ApplicationTier.Model.TestCategory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestCategoryDAO {

    public List<TestCategory> getAllCategories() {
        List<TestCategory> categories = new ArrayList<>();
        String sql = "SELECT * FROM TestCategory";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                TestCategory cat = new TestCategory();
                cat.setCategoryId(rs.getInt("categoryId"));
                cat.setName(rs.getString("name"));
                cat.setDescription(rs.getString("description"));
                categories.add(cat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

}
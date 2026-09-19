package DAO;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/medical_lab_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final Properties CONFIG = loadConfig();

    private static Connection connection = null;

    private DatabaseConnection() {}

    private static Properties loadConfig() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        } catch (IOException ignored) {
        }
        return properties;
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String url = CONFIG.getProperty("db.url", DEFAULT_URL);
                String user = CONFIG.getProperty("db.user", DEFAULT_USER);
                String password = CONFIG.getProperty("db.password", DEFAULT_PASSWORD);
                connection = DriverManager.getConnection(url, user, password);
            } catch (ClassNotFoundException e) {
                throw new SQLException("JDBC Driver not found", e);
            }
        }
        return connection;
    }
}

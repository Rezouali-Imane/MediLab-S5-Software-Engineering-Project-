package DAO;
import ApplicationTier.Model.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatientDAO {

    private static final Logger LOGGER = Logger.getLogger(PatientDAO.class.getName());

    public boolean addPatient(Patient p) {
        String sql = "INSERT INTO Patient (firstName, lastName, dateOfBirth, gender, phone, email, address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {


            stmt.setString(1, p.getFirstName() != null ? p.getFirstName().trim() : null);
            stmt.setString(2, p.getLastName() != null ? p.getLastName().trim() : null);


            if (p.getDateOfBirth() != null) {
                stmt.setDate(3, new java.sql.Date(p.getDateOfBirth().getTime()));
            } else {
                stmt.setNull(3, Types.DATE);
            }

            stmt.setString(4, p.getGender());
            stmt.setString(5, p.getPhone());
            stmt.setString(6, p.getEmail());
            stmt.setString(7, p.getAddress());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "addPatient failed", e); return false; }
    }

    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM Patient";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "getAllPatients failed", e); }
        return list;
    }

    public Patient getPatientById(int id) {
        String sql = "SELECT * FROM Patient WHERE patientId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) return mapRow(rs);
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "getPatientById failed", e); }
        return null;
    }

    public boolean deletePatient(int patientId) {
        String sql = "DELETE FROM Patient WHERE patientId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "deletePatient failed", e); return false; }
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patientId"));
        p.setFirstName(rs.getString("firstName"));
        p.setLastName(rs.getString("lastName"));
        p.setDateOfBirth(rs.getDate("dateOfBirth"));
        p.setGender(rs.getString("gender"));
        p.setPhone(rs.getString("phone"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        p.setRegistrationDate(rs.getTimestamp("registrationDate"));
        return p;
    }
}

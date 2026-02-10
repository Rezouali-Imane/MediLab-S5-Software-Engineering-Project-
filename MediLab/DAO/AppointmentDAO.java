package DAO;
import ApplicationTier.Model.Appointment;
import ApplicationTier.Model.Enums;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AppointmentDAO {

    private static final Logger LOGGER = Logger.getLogger(AppointmentDAO.class.getName());

    public boolean addAppointment(Appointment app) {
        String sql = "INSERT INTO Appointment (patientId, secretaryId, date, reason, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, app.getPatientId());
            stmt.setInt(2, app.getSecretaryId());
            stmt.setTimestamp(3, new java.sql.Timestamp(app.getDate().getTime()));
            stmt.setString(4, app.getReason());
            // Guard against null status (some callers may not set it) and default to SCHEDULED
            stmt.setString(5, app.getStatus() != null ? app.getStatus().toString() : ApplicationTier.Model.Enums.AppointmentStatus.SCHEDULED.toString());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = stmt.getGeneratedKeys()) {
                    if (gk.next()) {
                        int id = gk.getInt(1);
                        app.setAppointmentId(id);
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "addAppointment failed", e); return false; }
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM Appointment ORDER BY date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Appointment a = new Appointment();
                a.setAppointmentId(rs.getInt("appointmentId"));
                a.setPatientId(rs.getInt("patientId"));
                a.setSecretaryId(rs.getInt("secretaryId"));
                a.setDate(rs.getTimestamp("date"));
                a.setReason(rs.getString("reason"));
                a.setStatus(rs.getString("status"));
                list.add(a);
            }
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "getAllAppointments failed", e); }
        return list;
    }

    // Update status
    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE Appointment SET status = ? WHERE appointmentId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "updateStatus failed", e); return false; }
    }

    // Update appointment details
    public boolean updateAppointment(Appointment app) {
        String sql = "UPDATE Appointment SET patientId = ?, secretaryId = ?, date = ?, reason = ?, status = ? WHERE appointmentId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, app.getPatientId());
            stmt.setInt(2, app.getSecretaryId());
            stmt.setTimestamp(3, new java.sql.Timestamp(app.getDate().getTime()));
            stmt.setString(4, app.getReason());
            stmt.setString(5, app.getStatus() != null ? app.getStatus().toString() : "SCHEDULED");
            stmt.setInt(6, app.getAppointmentId());
            int affected = stmt.executeUpdate();
            java.util.logging.Logger.getLogger(AppointmentDAO.class.getName()).info("updateAppointment: appointmentId=" + app.getAppointmentId() + " patientId=" + app.getPatientId() + " affected=" + affected);
            return affected > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "updateAppointment failed", e);
            return false;
        }
    }


    public boolean deleteAppointment(int appointmentId) {
        String sql = "DELETE FROM Appointment WHERE appointmentId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "deleteAppointment failed", e);
            return false;
        }
    }

    public Appointment getAppointmentById(int appointmentId) {
        String sql = "SELECT * FROM Appointment WHERE appointmentId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Appointment a = new Appointment();
                    a.setAppointmentId(rs.getInt("appointmentId"));
                    a.setPatientId(rs.getInt("patientId"));
                    a.setSecretaryId(rs.getInt("secretaryId"));
                    a.setDate(rs.getTimestamp("date"));
                    a.setReason(rs.getString("reason"));
                    a.setStatus(rs.getString("status"));
                    return a;
                }
            }
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "getAppointmentById failed", e); }
        return null;
    }
}
package DAO;

import ApplicationTier.Model.LoginAudit;
import DAO.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginAuditDAO {

    private static final Logger LOGGER = Logger.getLogger(LoginAuditDAO.class.getName());

    public boolean createAudit(LoginAudit a) {
        String sql = "INSERT INTO LoginAudit (username, success, message, timestamp, remoteAddr) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, a.getUsername());
            stmt.setBoolean(2, a.isSuccess());
            stmt.setString(3, a.getMessage());
            if (a.getTimestamp() != null) stmt.setTimestamp(4, new Timestamp(a.getTimestamp().getTime())); else stmt.setTimestamp(4, new Timestamp(new Date().getTime()));
            stmt.setString(5, a.getRemoteAddr());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "createAudit failed", e); return false; }
    }



    public List<LoginAudit> query(int offset, int pageSize, String search) {
        List<LoginAudit> out = new ArrayList<>();
        String base = "SELECT * FROM LoginAudit";
        String where = "";
        if (search != null && !search.trim().isEmpty()) where = " WHERE username LIKE ? OR message LIKE ? ";
        String order = " ORDER BY timestamp DESC LIMIT ? OFFSET ?";
        String sql = base + where + order;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            int idx = 1;
            if (!where.isEmpty()) {
                String like = "%" + search + "%";
                stmt.setString(idx++, like);
                stmt.setString(idx++, like);
            }
            stmt.setInt(idx++, pageSize);
            stmt.setInt(idx, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LoginAudit a = new LoginAudit();
                    a.setId(rs.getInt("id"));
                    a.setUsername(rs.getString("username"));
                    a.setSuccess(rs.getBoolean("success"));
                    a.setMessage(rs.getString("message"));
                    a.setTimestamp(rs.getTimestamp("timestamp"));
                    a.setRemoteAddr(rs.getString("remoteAddr"));
                    out.add(a);
                }
            }
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "query failed", e); }
        return out;
    }


    public List<LoginAudit> queryWithFilters(java.util.Date from, java.util.Date to, Boolean success, int offset, int pageSize, String usernameSearch) {
        List<LoginAudit> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder("SELECT * FROM LoginAudit");
        List<Object> params = new ArrayList<>();
        boolean whereAdded = false;

        if (from != null) {
            sb.append(whereAdded ? " AND" : " WHERE"); sb.append(" timestamp >= ?"); params.add(new Timestamp(from.getTime())); whereAdded = true;
        }
        if (to != null) {
            sb.append(whereAdded ? " AND" : " WHERE"); sb.append(" timestamp <= ?"); params.add(new Timestamp(to.getTime())); whereAdded = true;
        }
        if (success != null) {
            sb.append(whereAdded ? " AND" : " WHERE"); sb.append(" success = ?"); params.add(success);
            whereAdded = true;
        }
        if (usernameSearch != null && !usernameSearch.trim().isEmpty()) {
            sb.append(whereAdded ? " AND" : " WHERE"); sb.append(" username LIKE ?"); params.add("%" + usernameSearch + "%"); whereAdded = true;
        }

        sb.append(" ORDER BY timestamp DESC LIMIT ? OFFSET ?");

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            for (Object o : params) {
                if (o instanceof Timestamp) stmt.setTimestamp(idx++, (Timestamp) o);
                else if (o instanceof Boolean) stmt.setBoolean(idx++, (Boolean) o);
                else stmt.setString(idx++, String.valueOf(o));
            }
            stmt.setInt(idx++, pageSize);
            stmt.setInt(idx, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LoginAudit a = new LoginAudit();
                    a.setId(rs.getInt("id"));
                    a.setUsername(rs.getString("username"));
                    a.setSuccess(rs.getBoolean("success"));
                    a.setMessage(rs.getString("message"));
                    a.setTimestamp(rs.getTimestamp("timestamp"));
                    a.setRemoteAddr(rs.getString("remoteAddr"));
                    out.add(a);
                }
            }
        } catch (SQLException e) { LOGGER.log(Level.WARNING, "queryWithFilters failed", e); }
        return out;
    }
}

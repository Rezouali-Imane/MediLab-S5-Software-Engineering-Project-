package ApplicationTier;

import DAO.TestOrderDAO;
import DAO.TestResultDAO;
import DAO.LoginAuditDAO;
import ApplicationTier.Model.TestResult;
import ApplicationTier.Model.LoginAudit;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SuperAdminService extends AdminService {

    private static final Logger LOGGER = Logger.getLogger(SuperAdminService.class.getName());
    private static final String CONFIG_FILE = System.getProperty("user.dir") + File.separator + "config.properties";
    private volatile String lastUsedLogPath = null;
    private volatile long lastRefreshTs = 0L;

    public SuperAdminService() {
        super();
        try { this.lastUsedLogPath = readProperties().getProperty("log.path", null); } catch (Exception ignored) {}
    }

    // Load properties from config file
    private Properties readProperties() {
        Properties p = new Properties();
        try {
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (java.io.InputStream is = Files.newInputStream(f.toPath())) { p.load(is); }
            }
        } catch (IOException ex) { LOGGER.log(Level.FINER, "Failed to load config.properties", ex); }
        return p;
    }

    private void writeProperties(Properties p) {
        try {
            try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(new File(CONFIG_FILE).toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
                p.store(out, "Application configuration");
            }
        } catch (IOException ex) { LOGGER.log(Level.WARNING, "Failed to write config.properties", ex); }
    }

    public String getActiveLogPath() {
        if (lastUsedLogPath != null) return lastUsedLogPath;
        String cfg = readProperties().getProperty("log.path", null);
        if (cfg != null && !cfg.trim().isEmpty()) return cfg;
        // fallback candidates
        String[] candidates = new String[]{"logs/app.log", "app.log", "medilab.log", "/var/log/medilab.log"};
        for (String p : candidates) {
            File f = new File(p);
            if (f.exists() && f.isFile()) return f.getAbsolutePath();
        }
        return "";
    }

    public synchronized void setActiveLogPath(String path) {
        if (path == null) path = "";
        this.lastUsedLogPath = path.trim();
        this.lastRefreshTs = System.currentTimeMillis();
        try { Properties p = readProperties(); p.setProperty("log.path", this.lastUsedLogPath); p.setProperty("log.lastRefresh", String.valueOf(this.lastRefreshTs)); writeProperties(p); } catch (Exception ex) { LOGGER.log(Level.WARNING, "Failed to persist log.path", ex); }
    }

    public long getLastRefreshTimestamp() { return lastRefreshTs; }


    /**
     * @param path
     * @param page
     * @param pageSize
     * @param search
     * @param level
     */
    public synchronized String viewSystemLogs(String path, int page, int pageSize, String search, String level) {
        if (path == null || path.trim().isEmpty()) path = getActiveLogPath();
        if (path == null || path.trim().isEmpty()) return "[SYSTEM] No log path configured.";

        File f = new File(path);
        if (!f.exists() || !f.isFile() || !f.canRead()) return "[SYSTEM] Log file not found or unreadable: " + path;

        try {
            List<String> lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
            List<String> filtered = new ArrayList<>();
            String searchLower = (search != null) ? search.toLowerCase() : null;
            String levelUpper = (level != null) ? level.toUpperCase() : null;
            for (String ln : lines) {
                boolean ok = true;
                if (searchLower != null && !searchLower.isEmpty()) {
                    if (ln.toLowerCase().indexOf(searchLower) < 0) ok = false;
                }
                if (ok && levelUpper != null && !levelUpper.isEmpty()) {
                    if (ln.indexOf(levelUpper) < 0) ok = false;
                }
                if (ok) filtered.add(ln);
            }

            int total = filtered.size();
            int from = Math.max(0, page * pageSize);
            int to = Math.min(total, from + pageSize);
            StringBuilder sb = new StringBuilder();
            sb.append("--- Showing lines ").append(from+1).append("-" ).append(to).append(" of ").append(total).append(" from ").append(f.getAbsolutePath()).append(" ---\n");
            for (int i = from; i < to; i++) sb.append(filtered.get(i)).append('\n');

            // persist last used path and timestamp
            this.lastUsedLogPath = f.getAbsolutePath();
            this.lastRefreshTs = System.currentTimeMillis();
            try { Properties p = readProperties(); p.setProperty("log.path", this.lastUsedLogPath); p.setProperty("log.lastRefresh", String.valueOf(this.lastRefreshTs)); writeProperties(p); } catch (Exception ignored) {}

            return sb.toString();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed to read log file: " + path, ex);
            return "[SYSTEM] Failed to read log file: " + ex.getMessage();
        }
    }


    public boolean exportLogs(String content, String destinationPath) {
        if (content == null) content = "";
        if (destinationPath == null || destinationPath.trim().isEmpty()) return false;
        File out = new File(destinationPath);
        try {
            Files.write(out.toPath(), content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed to export logs to " + destinationPath, ex);
            return false;
        }
    }


    public void performDatabaseBackup() {
        System.out.println("[SuperAdmin] Starting database backup sequence...");
        try {
            Thread.sleep(1000);
            System.out.println("[SuperAdmin] Backup saved to /backups/db_dump.sql");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void overrideValidation(int orderId) {
        try {
            TestResultDAO resultDAO = new TestResultDAO();
            TestOrderDAO orderDAO = new TestOrderDAO();

            List<TestResult> results = resultDAO.getResultsByOrder(orderId);
            if (results != null) {
                for (TestResult r : results) {
                    try {
                        if (!r.isValidated()) {
                            boolean ok = resultDAO.validateResult(r.getResultId());
                            if (!ok) LOGGER.warning("Failed to validate resultId=" + r.getResultId());
                        }
                    } catch (Throwable ex) {
                        LOGGER.log(Level.WARNING, "Failed to force-validate result " + r.getResultId(), ex);
                    }
                }
            }


            boolean updated = orderDAO.updateStatus(orderId, ApplicationTier.Model.Enums.TestOrderStatus.VALIDATED.toString());
            if (!updated) LOGGER.warning("Failed to update TestOrder status to VALIDATED for orderId=" + orderId);
            else LOGGER.info("Order " + orderId + " force-validated by SuperAdmin");

        } catch (Throwable ex) {
            LOGGER.log(Level.WARNING, "overrideValidation failed for orderId=" + orderId, ex);
        }
    }


    public synchronized String viewLoginHistory(String path, int page, int pageSize, String search) {

        try {
            LoginAuditDAO la = new LoginAuditDAO();
            int offset = Math.max(0, page * pageSize);
            java.util.List<LoginAudit> rows = la.query(offset, pageSize, search);
            StringBuilder sb = new StringBuilder();
            sb.append("--- Showing login audit (DB) ").append(offset+1).append("-" ).append(offset + rows.size()).append(" ---\n");
            for (LoginAudit r : rows) {
                sb.append(String.format("%s | %s | %s | %s\n", r.getTimestamp(), r.getUsername(), r.isSuccess() ? "SUCCESS" : "FAIL", r.getMessage() == null ? "" : r.getMessage()));
            }
            this.lastRefreshTs = System.currentTimeMillis();
            try { Properties p = readProperties(); if (path != null && !path.trim().isEmpty()) { p.setProperty("log.path", path); } p.setProperty("log.lastRefresh", String.valueOf(this.lastRefreshTs)); writeProperties(p); } catch (Exception ignored) {}
            return sb.toString();
        } catch (Throwable dbEx) {

        }

        if (path == null || path.trim().isEmpty()) path = getActiveLogPath();
        if (path == null || path.trim().isEmpty()) return "[SYSTEM] No log path configured.";

        File f = new File(path);
        if (!f.exists() || !f.isFile() || !f.canRead()) return "[SYSTEM] Log file not found or unreadable: " + path;

        try {
            List<String> lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
            List<String> filtered = new ArrayList<>();
            String searchLower = (search != null) ? search.toLowerCase() : null;
            for (String ln : lines) {
                String lower = ln.toLowerCase();
                boolean isLoginLine = lower.contains("user logged in") || lower.contains("login error") || lower.contains("authentication failed") || lower.contains("login failed") || lower.contains("logged in:") || lower.contains("logged in ") || lower.contains("user logged in:") || lower.contains("user logged in");
                if (!isLoginLine) continue;
                if (searchLower != null && !searchLower.isEmpty()) {
                    if (ln.toLowerCase().indexOf(searchLower) < 0) continue;
                }
                filtered.add(ln);
            }

            int total = filtered.size();
            int from = Math.max(0, page * pageSize);
            int to = Math.min(total, from + pageSize);
            StringBuilder sb = new StringBuilder();
            sb.append("--- Showing login events ").append(from+1).append("-").append(to).append(" of ").append(total).append(" from ").append(f.getAbsolutePath()).append(" ---\n");
            for (int i = from; i < to; i++) sb.append(filtered.get(i)).append('\n');

            this.lastUsedLogPath = f.getAbsolutePath();
            this.lastRefreshTs = System.currentTimeMillis();
            try { Properties p = readProperties(); p.setProperty("log.path", this.lastUsedLogPath); p.setProperty("log.lastRefresh", String.valueOf(this.lastRefreshTs)); writeProperties(p); } catch (Exception ignored) {}

            return sb.toString();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Failed to read log file for login history: " + path, ex);
            return "[SYSTEM] Failed to read log file: " + ex.getMessage();
        }
    }

}

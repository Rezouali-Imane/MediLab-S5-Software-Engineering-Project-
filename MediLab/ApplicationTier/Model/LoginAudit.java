package ApplicationTier.Model;

import java.util.Date;

public class LoginAudit {
    private int id;
    private String username;
    private boolean success;
    private String message;
    private Date timestamp;
    private String remoteAddr; // optional

    public LoginAudit() {}

    public LoginAudit(String username, boolean success, String message, Date timestamp, String remoteAddr) {
        this.username = username;
        this.success = success;
        this.message = message;
        this.timestamp = timestamp;
        this.remoteAddr = remoteAddr;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getRemoteAddr() { return remoteAddr; }
    public void setRemoteAddr(String remoteAddr) { this.remoteAddr = remoteAddr; }
}


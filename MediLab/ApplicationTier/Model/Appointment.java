package ApplicationTier.Model;


import java.util.Date;
import ApplicationTier.Model.Enums.AppointmentStatus;

public class Appointment {
    private int appointmentId;
    private int patientId;
    private int secretaryId;
    private Date date;
    private String reason;
    private AppointmentStatus status;

    public Appointment() {}

    public Appointment(int patientId, int secretaryId, Date date, String reason) {
        this.patientId = patientId;
        this.secretaryId = secretaryId;
        this.date = date;
        this.reason = reason;
        this.status = AppointmentStatus.SCHEDULED; // Default
    }


    { this.status = AppointmentStatus.SCHEDULED; }
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int id) { this.appointmentId = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int id) { this.patientId = id; }

    public int getSecretaryId() { return secretaryId; }
    public void setSecretaryId(int id) { this.secretaryId = id; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getReason() { return reason; }
    public void setReason(String r) { this.reason = r; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus s) { this.status = s; }

    public void setStatus(String s) {
        if (s != null) {
            try { this.status = AppointmentStatus.valueOf(s.toUpperCase()); }
            catch (IllegalArgumentException ex) { this.status = AppointmentStatus.SCHEDULED; }
        } else {
            this.status = AppointmentStatus.SCHEDULED;
        }
    }


}
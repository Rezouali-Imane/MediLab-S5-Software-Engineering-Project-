package ApplicationTier.Model;
import java.util.Date;
import ApplicationTier.Model.Enums.TestOrderStatus;

public class TestOrder {
    private int orderId;
    private int patientId;
    private int technicianId; // Nullable if not yet assigned
    private Date dateOrdered;
    private TestOrderStatus status;
    private int appointmentId;

    public TestOrder() {}

    public TestOrder(int patientId, int technicianId, TestOrderStatus status) {
        this.patientId = patientId;
        this.technicianId = technicianId;
        this.dateOrdered = new Date();
        this.status = status;
    }


    public TestOrder(int patientId, int technicianId, int appointmentId, TestOrderStatus status) {
        this.patientId = patientId;
        this.technicianId = technicianId;
        this.appointmentId = appointmentId;
        this.dateOrdered = new Date();
        this.status = status;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int id) { this.orderId = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int id) { this.patientId = id; }

    public int getTechnicianId() { return technicianId; }
    public void setTechnicianId(int id) { this.technicianId = id; }

    public Date getDateOrdered() { return dateOrdered; }
    public void setDateOrdered(Date date) { this.dateOrdered = date; }

    public TestOrderStatus getStatus() { return status; }
    public void setStatus(TestOrderStatus status) { this.status = status; }

    public void setStatus(String s) {
        if(s != null) this.status = TestOrderStatus.valueOf(s.toUpperCase());
    }

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
}
package ApplicationTier.Model;
import ApplicationTier.Model.Enums.PaymentStatus;
import java.util.Date;


public class Invoice {
    private int invoiceId;
    private int orderId;
    private int patientId;
    private Date date;
    private double totalAmount;
    private PaymentStatus paymentStatus;

    public Invoice() {}

    public Invoice(int orderId, int patientId, double totalAmount) {
        this.orderId = orderId;
        this.patientId = patientId;
        this.totalAmount = totalAmount;
        this.date = new Date();
        this.paymentStatus = PaymentStatus.UNPAID;
    }

    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int id) { this.invoiceId = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int id) { this.orderId = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int id) { this.patientId = id; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double a) { this.totalAmount = a; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }

    public void setPaymentStatus(String s) {
        if(s != null) this.paymentStatus = PaymentStatus.valueOf(s.toUpperCase());
    }
}
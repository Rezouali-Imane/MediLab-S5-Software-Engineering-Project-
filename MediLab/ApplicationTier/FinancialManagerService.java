package ApplicationTier;
import ApplicationTier.Model.*;
import ApplicationTier.Model.Enums.PaymentStatus;
import DAO.*;
import java.util.List;
import java.util.logging.Level;
import java.time.LocalDate;
import java.time.ZoneId;

public class FinancialManagerService {

    private final InvoiceDAO invoiceDAO;
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FinancialManagerService.class.getName());

    public FinancialManagerService() {
        this.invoiceDAO = new InvoiceDAO();
    }

    public Invoice findInvoiceById(int invoiceId) {
        try { return invoiceDAO.getInvoiceById(invoiceId); } catch (Exception ex) { LOGGER.log(Level.WARNING, "findInvoiceById failed", ex); return null; }
    }

    public List<Invoice> findAllInvoices() {
        try { return invoiceDAO.listAllInvoices(); } catch (Exception ex) { LOGGER.log(Level.WARNING, "findAllInvoices failed", ex); return java.util.Collections.emptyList(); }
    }

    public boolean validatePayment(int invoiceId, double amountPaid) {
        if (invoiceId <= 0) return false;
        try {
            Invoice inv = invoiceDAO.getInvoiceById(invoiceId);
            if (inv == null) {
                LOGGER.warning("validatePayment: invoice not found: " + invoiceId);
                return false;
            }
            if (amountPaid <= 0) {
                LOGGER.warning("validatePayment: amountPaid must be > 0");
                return false;
            }


            boolean ok = invoiceDAO.updatePaymentStatus(invoiceId, PaymentStatus.PAID.toString());
            if (!ok) {
                LOGGER.warning("validatePayment: DAO failed to update status for invoice " + invoiceId);
                return false;
            }


            if (Double.compare(inv.getTotalAmount(), amountPaid) != 0) {
                invoiceDAO.updateInvoice(invoiceId, amountPaid, PaymentStatus.PAID.toString());
            }

            LOGGER.info("Invoice " + invoiceId + " marked as PAID (amount=" + amountPaid + ")");
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "validatePayment failed", ex);
            return false;
        }
    }



    public java.util.List<Invoice> getInvoicesBetween(LocalDate start, LocalDate end) {
        try {
            List<Invoice> all = invoiceDAO.listAllInvoices();
            java.util.List<Invoice> out = new java.util.ArrayList<>();
            if (all == null) return out;
            for (Invoice inv : all) {
                if (inv == null || inv.getDate() == null) continue;
                LocalDate d = inv.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if ((d.isEqual(start) || d.isAfter(start)) && (d.isEqual(end) || d.isBefore(end))) out.add(inv);
            }
            return out;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "getInvoicesBetween failed", ex);
            return java.util.Collections.emptyList();
        }
    }


    public boolean deleteInvoice(int invoiceId) {
        if (invoiceId <= 0) return false;
        try { return invoiceDAO.deleteInvoice(invoiceId); } catch (Exception ex) { LOGGER.log(Level.WARNING, "deleteInvoice failed", ex); return false; }
    }


    public java.util.List<TestResult> getResultsForOrder(int orderId) {
        try {
            TechnicianService ts = new TechnicianService();
            return ts.getResultsForOrder(orderId);
        } catch (Exception ex) {
            LOGGER.log(Level.FINER, "getResultsForOrder failed", ex);
            return java.util.Collections.emptyList();
        }
    }

}


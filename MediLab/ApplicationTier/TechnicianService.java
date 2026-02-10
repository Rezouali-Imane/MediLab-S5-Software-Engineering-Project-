package ApplicationTier;

import ApplicationTier.Model.*;
import DAO.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class TechnicianService {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(TechnicianService.class.getName());

    private final TestOrderDAO orderDAO;
    private final TestResultDAO resultDAO;
    private final InvoiceDAO invoiceDAO;
    private final AppointmentDAO appointmentDAO;
    private final TestTypeDAO testTypeDAO;

    public TechnicianService() {
        this.orderDAO = new TestOrderDAO();
        this.resultDAO = new TestResultDAO();
        this.invoiceDAO = new InvoiceDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.testTypeDAO = new TestTypeDAO();
    }

    public interface ValidationListener {
        void onOrderValidated(int orderId);
    }

    private static final CopyOnWriteArrayList<ValidationListener> VALIDATION_LISTENERS = new CopyOnWriteArrayList<>();

    public static void addValidationListener(ValidationListener l) {
        if (l != null) VALIDATION_LISTENERS.add(l);
    }

    public static void removeValidationListener(ValidationListener l) {
        VALIDATION_LISTENERS.remove(l);
    }

    private static void notifyOrderValidated(int orderId) {
        try {
            java.util.logging.Logger.getLogger(TechnicianService.class.getName()).info("notifyOrderValidated: orderId=" + orderId + " listeners=" + VALIDATION_LISTENERS.size());
        } catch (Throwable ignored) {}
        for (ValidationListener l : VALIDATION_LISTENERS) {
            try {
                l.onOrderValidated(orderId);
            } catch (Throwable ex) {
                java.util.logging.Logger.getLogger(TechnicianService.class.getName()).log(java.util.logging.Level.WARNING, "Validation listener failed for order=" + orderId, ex);
            }
        }
    }


    public List<TestType> getTestCatalog() {
        return testTypeDAO.getAllTestTypes();
    }


    public List<TestOrder> getPendingOrders() {
        return orderDAO.getPendingOrders();
    }



    public boolean enterResultDataAs(Enums.Role actorRole, int resultId, String value, String interpretation) {
        if (actorRole == null) return false;
        if (!(actorRole == ApplicationTier.Model.Enums.Role.TECHNICIAN || actorRole == ApplicationTier.Model.Enums.Role.SUPER_ADMIN)) return false;
        return resultDAO.updateResultValue(resultId, value, interpretation);
    }


    public boolean validateResultAs(int actorEmployeeId, ApplicationTier.Model.Enums.Role actorRole, int resultId) {
        if (actorRole == null) return false;
        if (!(actorRole == ApplicationTier.Model.Enums.Role.TECHNICIAN || actorRole == ApplicationTier.Model.Enums.Role.SUPER_ADMIN)) return false;
        boolean ok = resultDAO.validateResult(resultId);
        if (ok) {

            TestResult tr = resultDAO.getResultById(resultId);
            if (tr != null) postValidationOrderCheck(tr.getOrderId());
        } else {
            LOGGER.warning("validateResultAs: DAO failed to validate resultId=" + resultId + " actor=" + actorEmployeeId);
        }
        return ok;
    }


    public boolean updateOrderStatus(int orderId, ApplicationTier.Model.Enums.TestOrderStatus status) {
        if (orderId <= 0 || status == null) return false;
        return orderDAO.updateStatus(orderId, status.toString());
    }


    public List<TestResult> getResultsForOrder(int orderId) {
        return resultDAO.getResultsByOrder(orderId);
    }

    public List<TestResult> getPendingResultsForTechnician(int technicianId) {
        return resultDAO.getPendingResultsByTechnician(technicianId);
    }


    public List<TestResult> getPendingResultsAll() {
        return resultDAO.getPendingResultsAll();
    }


    public void postValidationOrderCheck(int orderId) {
        List<TestResult> results = resultDAO.getResultsByOrder(orderId);
        if (results == null || results.isEmpty()) return;

        boolean allValidated = true;
        for (TestResult r : results) {
            if (!r.isValidated()) {
                allValidated = false;
                break;
            }
        }

        if (allValidated) {
            orderDAO.updateStatus(orderId, ApplicationTier.Model.Enums.TestOrderStatus.VALIDATED.toString());
            try {
                ApplicationTier.Model.Invoice existingInv = invoiceDAO.getInvoiceByOrder(orderId);
                if (existingInv == null) {

                    double totalAmount = 0.0;
                    java.util.List<TestResult> resList = resultDAO.getResultsByOrder(orderId);
                    if (resList != null && !resList.isEmpty()) {
                        for (TestResult rr : resList) {
                            if (rr.getTestTypeId() > 0) {
                                TestType tt = testTypeDAO.getTestTypeById(rr.getTestTypeId());
                                if (tt != null) totalAmount += tt.getPrice();
                            }
                        }
                    }
                    TestOrder ord = orderDAO.getOrderById(orderId);
                    int patientId = (ord != null) ? ord.getPatientId() : -1;
                    int pidForInvoice = patientId > 0 ? patientId : 0;
                    try {
                        ApplicationTier.Model.Invoice inv = new ApplicationTier.Model.Invoice(orderId, pidForInvoice, totalAmount);
                        boolean created = invoiceDAO.createInvoice(inv);
                        if (created) LOGGER.info("Auto-created invoice for order " + orderId + " amount=" + totalAmount + " patientId=" + pidForInvoice);
                        else LOGGER.warning("Auto-create invoice failed for order " + orderId + " amount=" + totalAmount + " patientId=" + pidForInvoice);
                        // Attempt to retrieve the invoice we just created to verify persistence
                        try {
                            ApplicationTier.Model.Invoice check = invoiceDAO.getInvoiceByOrder(orderId);
                            if (check != null) {
                                LOGGER.info("Invoice lookup after create returned invoiceId=" + check.getInvoiceId() + " orderId=" + check.getOrderId() + " status=" + check.getPaymentStatus());
                            } else {
                                LOGGER.warning("Invoice lookup after create returned null for orderId=" + orderId);
                            }
                        } catch (Throwable ex2) {
                            LOGGER.log(java.util.logging.Level.WARNING, "Exception when verifying created invoice for order " + orderId, ex2);
                        }
                    } catch (Throwable ex) {
                        LOGGER.log(java.util.logging.Level.WARNING, "Exception while auto-creating invoice for order " + orderId, ex);
                    }
                 }
             } catch (Throwable ex) {
                 LOGGER.log(java.util.logging.Level.FINER, "Failed to ensure invoice for order " + orderId, ex);
             }

            notifyOrderValidated(orderId);

              try {
                  SecretaryService sec = new SecretaryService();
                  for (TestResult r : results) {

                  }
              } catch (Throwable ignored) {}
          }
     }

    public TestResult getResultById(int resultId) {
        return resultDAO.getResultById(resultId);
    }


    public List<TestResult> getAllResultsForTechnician(int technicianId) {
        return resultDAO.getResultsByTechnicianAll(technicianId);
    }


    public List<TestResult> getAllResultsAll() {
        return resultDAO.getAllResultsAll();
    }

    public boolean createResultsForOrder(int orderId, int technicianId, List<TestType> tests) {
        if (orderId <= 0 || tests == null || tests.isEmpty()) return false;
        try {
            for (TestType t : tests) {
                TestResult res = new TestResult();
                res.setOrderId(orderId);
                res.setTestTypeId(t.getTestTypeId());
                res.setTechnicianId(technicianId);
                res.setValidated(false);
                resultDAO.addResult(res);
            }
            return true;
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "createResultsForOrder failed", ex);
            return false;
        }
    }


    public List<TestOrder> getOrdersByAppointment(int appointmentId) {
        try {
            return orderDAO.getOrdersByAppointment(appointmentId);
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "getOrdersByAppointment failed", ex);
            return java.util.Collections.emptyList();
        }
    }

    public java.util.List<TestOrder> getOrRestoreOrdersForAppointment(int appointmentId) {
        try {
            if (appointmentId <= 0) return java.util.Collections.emptyList();

            try {
                ApplicationTier.SecretaryService sec = new ApplicationTier.SecretaryService();
                java.util.List<TestOrder> sOrders = sec.getOrdersByAppointment(appointmentId);
                if (sOrders != null && !sOrders.isEmpty()) return sOrders;
            } catch (Throwable ignored) {}

            java.util.List<TestOrder> orders = orderDAO.getOrdersByAppointment(appointmentId);
            if (orders != null && !orders.isEmpty()) return orders;


            TestOrder created = getOrCreateOrderForAppointment(appointmentId);
            if (created != null) return java.util.Collections.singletonList(created);

            Appointment app = appointmentDAO.getAppointmentById(appointmentId);
            if (app != null) {
                java.util.List<TestOrder> byPatient = orderDAO.getOrdersForPatient(app.getPatientId());
                if (byPatient != null && !byPatient.isEmpty()) return byPatient;
            }
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.FINER, "getOrRestoreOrdersForAppointment failed for appt=" + appointmentId, ex);
        }
        return java.util.Collections.emptyList();
    }

    public TestOrder getOrderById(int orderId) {
        return orderDAO.getOrderById(orderId);
    }


    public TestOrder getOrCreateOrderForAppointment(int appointmentId) {
        try {
            if (appointmentId <= 0) return null;
            List<TestOrder> orders = orderDAO.getOrdersByAppointment(appointmentId);
            if (orders != null && !orders.isEmpty()) return orders.get(0);

            Appointment app = appointmentDAO.getAppointmentById(appointmentId);
            if (app == null) return null;

            TestOrder order = new TestOrder();
            order.setPatientId(app.getPatientId());
            order.setTechnicianId(0); // unassigned
            order.setAppointmentId(appointmentId);
            order.setStatus(ApplicationTier.Model.Enums.TestOrderStatus.CREATED.toString());
            int id = orderDAO.createOrder(order);
            if (id > 0) {
                order.setOrderId(id);
                return order;
            }
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "getOrCreateOrderForAppointment failed", ex);
        }
        return null;
    }



    public java.util.List<ApplicationTier.Model.Technician> getAllTechnicians() {
        try {
            DAO.EmployeeDAO ed = new DAO.EmployeeDAO();
            java.util.List<ApplicationTier.Model.Employee> all = ed.getAllEmployees();
            java.util.List<ApplicationTier.Model.Technician> techs = new java.util.ArrayList<>();
            if (all != null) {
                for (ApplicationTier.Model.Employee e : all)
                    if (e instanceof ApplicationTier.Model.Technician) techs.add((ApplicationTier.Model.Technician) e);
            }
            return techs;
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "getAllTechnicians failed", ex);
            return java.util.Collections.emptyList();
        }
    }

    public static class AssignmentRecord {
        public final int orderId;
        public final int technicianId;
        public final int assignedBy;
        public final java.util.Date timestamp;

        public AssignmentRecord(int orderId, int technicianId, int assignedBy, java.util.Date timestamp) {
            this.orderId = orderId;
            this.technicianId = technicianId;
            this.assignedBy = assignedBy;
            this.timestamp = timestamp;
        }
    }

    private final java.util.List<AssignmentRecord> assignmentAudit = new java.util.concurrent.CopyOnWriteArrayList<>();


    public String getTechnicianName(int technicianId) {
        if (technicianId <= 0) return "Unassigned";
        try {
            java.util.List<ApplicationTier.Model.Technician> tlist = getAllTechnicians();
            if (tlist != null) {
                for (ApplicationTier.Model.Technician t : tlist) {
                    if (t != null && t.getEmployeeId() == technicianId) return t.getFirstName() + " " + t.getLastName();
                }
            }
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.FINER, "getTechnicianName failed", ex);
        }
        return "Tech#" + technicianId;
    }

    public java.util.List<Appointment> getTodayAppointments() {
        java.util.List<Appointment> out = new java.util.ArrayList<>();
        try {
            java.util.List<Appointment> all = appointmentDAO.getAllAppointments();
            if (all == null) return out;
            java.time.LocalDate today = java.time.LocalDate.now();
            for (Appointment a : all) {
                if (a == null || a.getDate() == null) continue;
                java.time.LocalDate d = new java.sql.Date(a.getDate().getTime()).toLocalDate();
                if (d.equals(today)) out.add(a);
            }
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "getTodayAppointments failed", ex);
        }
        return out;
    }
}

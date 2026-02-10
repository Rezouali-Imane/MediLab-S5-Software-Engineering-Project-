package ApplicationTier;

import DAO.*;
import ApplicationTier.Model.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;


public class SecretaryService {

    private final PatientDAO patientDAO;
    private final AppointmentDAO appointmentDAO;
    private final TestResultDAO resultDAO;
    private final InvoiceDAO invoiceDAO;
    private final TestOrderDAO orderDAO;
    private final TestCategoryDAO categoryDAO;
    private final DAO.EmployeeDAO employeeDAO;

    private static final Logger LOGGER = Logger.getLogger(SecretaryService.class.getName());

    private final CopyOnWriteArrayList<String> notificationQueue = new CopyOnWriteArrayList<>();
    // Shared in-memory map of appointmentId -> orderId so different service instances can see auto-created orders
    private static final java.util.concurrent.ConcurrentMap<Integer, Integer> appointmentOrderMap = new java.util.concurrent.ConcurrentHashMap<>();

    public SecretaryService() {
        this(new PatientDAO(), new AppointmentDAO(), new TestResultDAO(), new InvoiceDAO(), new TestOrderDAO());


        try {
            ApplicationTier.TechnicianService.addValidationListener(orderId -> {
                try {
                    TestOrder o = orderDAO.getOrderById(orderId);
                    Patient p = (o != null) ? patientDAO.getPatientById(o.getPatientId()) : null;
                    String patientName = (p != null) ? (p.getFirstName() + " " + p.getLastName()) : "Patient#" + (o != null ? o.getPatientId() : "?");
                    String msg = "Order #" + orderId + " for " + patientName + " has been VALIDATED.";
                    notificationQueue.add(msg);
                } catch (Exception ex) {
                    LOGGER.log(Level.FINER, "Validation listener handler failed", ex);
                }
            });
        } catch (Throwable t) {
            LOGGER.log(Level.FINER, "Could not register validation listener", t);
        }
    }


    SecretaryService(PatientDAO patientDAO, AppointmentDAO appointmentDAO, TestResultDAO resultDAO, InvoiceDAO invoiceDAO, TestOrderDAO orderDAO) {
        this.patientDAO = patientDAO;
        this.appointmentDAO = appointmentDAO;
        this.resultDAO = resultDAO;
        this.invoiceDAO = invoiceDAO;
        this.orderDAO = orderDAO;
        this.categoryDAO = new TestCategoryDAO();
        this.employeeDAO = new DAO.EmployeeDAO();
    }


    // Fetch all test categories
    public List<TestCategory> getAllCategories() {
        try {
            return categoryDAO.getAllCategories();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to load categories", ex);
            return java.util.Collections.emptyList();
        }
    }

    // Count of validated results
    public int countValidatedResults() {
        try {
            return resultDAO.countValidatedResults();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "countValidatedResults failed", ex);
            return 0;
        }
    }

    // Invoice list
    public List<Invoice> listAllInvoices() {
        try {
            return invoiceDAO.listAllInvoices();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "listAllInvoices failed", ex);
            return java.util.Collections.emptyList();
        }
    }


    public Invoice getInvoiceByOrder(int orderId) {
        try {
            return invoiceDAO.getInvoiceByOrder(orderId);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "getInvoiceByOrder failed", ex);
            return null;
        }
    }

    public Invoice getInvoiceById(int id) {
        try {
            return invoiceDAO.getInvoiceById(id);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "getInvoiceById failed", ex);
            return null;
        }
    }

    // Create an invoice record
    public boolean createInvoice(Invoice invoice) {
        if (invoice == null) return false;
        if (invoice.getTotalAmount() <= 0) return false;
        try {
            return invoiceDAO.createInvoice(invoice);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "createInvoice failed", ex);
            return false;
        }
    }

    public List<TestResult> getResultsByOrder(int orderId) {
        try {
            return resultDAO.getResultsByOrder(orderId);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "getResultsByOrder failed", ex);
            return java.util.Collections.emptyList();
        }
    }

    public List<TestOrder> getPendingOrders() {
        try {
            return orderDAO.getPendingOrders();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "getPendingOrders failed", ex);
            return java.util.Collections.emptyList();
        }
    }


    public List<Appointment> viewSchedule() {
        try {
            return appointmentDAO.getAllAppointments();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "viewSchedule failed", ex);
            return java.util.Collections.emptyList();
        }
    }



    public boolean deleteAppointment(int appointmentId) {
        try {
            return appointmentDAO.deleteAppointment(appointmentId);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "deleteAppointment failed", ex);
            return false;
        }
    }

    public boolean updateAppointmentStatus(int appointmentId, ApplicationTier.Model.Enums.AppointmentStatus status) {
        if (status == null) return false;
        try {
            return appointmentDAO.updateStatus(appointmentId, status.toString());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "updateAppointmentStatus failed", ex);
            return false;
        }
    }

    // Create invoice linked to an existing TestOrder ID
    public boolean createInvoiceForOrderId(int orderId, double totalAmount) {
        try {
            TestOrder o = orderDAO.getOrderById(orderId);
            if (o == null) return false;
            Invoice inv = new Invoice(orderId, o.getPatientId(), totalAmount);
            return createInvoice(inv);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "createInvoiceForOrderId failed", ex);
            return false;
        }
    }

    public Invoice deliverInvoice(int orderId) {
        try {
            return invoiceDAO.getInvoiceByOrder(orderId);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "deliverInvoice failed", ex);
            return null;
        }
    }

    // deliver reesults for an order if business rules are met
    public java.util.List<TestResult> deliverResult(int orderId) {
        try {
            if (!isOrderReadyForDelivery(orderId)) {
                return null;
            }
            return resultDAO.getResultsByOrder(orderId);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "deliverResult failed", ex);
            return null;
        }
    }


    public java.util.List<TestType> getTestTypesByCategory(int categoryId) {
        try {
            return new TestTypeDAO().getTestTypesByCategory(categoryId);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "getTestTypesByCategory failed", ex);
            return java.util.Collections.emptyList();
        }
    }

    // register a new patient
    public boolean registerPatient(Patient patient) {
        if (patient.getFirstName().isEmpty() || patient.getLastName().isEmpty()) return false;
        return patientDAO.addPatient(patient);
    }

    public List<Patient> getAllPatients() {
        return patientDAO.getAllPatients();
    }

    // Create appointment and auto-create a TestOrder for the patient
    public boolean createAppointment(Appointment app, java.util.List<Integer> testTypeIds) {
        try {
            java.time.LocalDate apd = new java.sql.Date(app.getDate().getTime()).toLocalDate();
            if (apd.isBefore(java.time.LocalDate.now())) {
                LOGGER.warning("Attempt to create appointment in the past: " + apd);
                return false;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Invalid appointment date", ex);
            return false;
        }

        boolean created = appointmentDAO.addAppointment(app);
        if (!created) return false;
        try {
            TestOrder order = new TestOrder();
            order.setPatientId(app.getPatientId());
            order.setTechnicianId(0);
            if (app.getAppointmentId() > 0) order.setAppointmentId(app.getAppointmentId());
            order.setStatus(ApplicationTier.Model.Enums.TestOrderStatus.CREATED.toString());
            int orderId = orderDAO.createOrder(order);
            if (orderId != -1) {
                LOGGER.info("Created TestOrder #" + orderId + " for appointment patient " + app.getPatientId());
                if (app.getAppointmentId() > 0) appointmentOrderMap.put(app.getAppointmentId(), orderId);
                // Persist chosen test types (if any) by creating placeholder TestResult rows
                if (testTypeIds != null && !testTypeIds.isEmpty()) {
                    try {
                        for (Integer ttId : testTypeIds) {
                            ApplicationTier.Model.TestResult placeholder = new ApplicationTier.Model.TestResult(orderId, ttId, 0); // technician unassigned
                            boolean added = resultDAO.addResult(placeholder);
                            if (!added)
                                LOGGER.warning("Failed to create TestResult placeholder for testType " + ttId + " on order " + orderId);
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Exception when attaching test types to order", ex);
                    }
                }
                if (order.getTechnicianId() > 0) {
                    LOGGER.info("Technician " + order.getTechnicianId() + " notified of TestOrder #" + orderId);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to auto-create TestOrder for appointment", ex);
        }
        return true;
    }


    public boolean createAppointment(Appointment app, java.util.List<Integer> testTypeIds, int technicianId) {
        try {
            java.time.LocalDate apd = new java.sql.Date(app.getDate().getTime()).toLocalDate();
            if (apd.isBefore(java.time.LocalDate.now())) {
                LOGGER.warning("Attempt to create appointment in the past: " + apd);
                return false;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Invalid appointment date", ex);
            return false;
        }

        boolean created = appointmentDAO.addAppointment(app);
        if (!created) return false;

        try {
            TestOrder order = new TestOrder();
            order.setPatientId(app.getPatientId());
            order.setTechnicianId(technicianId);
            if (app.getAppointmentId() > 0) order.setAppointmentId(app.getAppointmentId());
            order.setStatus(ApplicationTier.Model.Enums.TestOrderStatus.CREATED.toString());
            int orderId = orderDAO.createOrder(order);
            if (orderId != -1) {
                LOGGER.info("Created TestOrder #" + orderId + " for appointment patient " + app.getPatientId() + " (tech=" + technicianId + ")");
                if (app.getAppointmentId() > 0) appointmentOrderMap.put(app.getAppointmentId(), orderId);
                if (testTypeIds != null && !testTypeIds.isEmpty()) {
                    for (Integer ttId : testTypeIds) {
                        TestResult placeholder = new TestResult(orderId, ttId, technicianId);
                        boolean added = resultDAO.addResult(placeholder);
                        if (!added)
                            LOGGER.warning("Failed to create TestResult placeholder for testType " + ttId + " on order " + orderId);
                    }
                }
                if (technicianId > 0) LOGGER.info("Technician " + technicianId + " assigned to TestOrder #" + orderId);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to auto-create TestOrder for appointment with technician", ex);
        }
        return true;
    }


    public java.util.List<ApplicationTier.Model.Technician> getAllTechnicians() {
        try {
            java.util.List<ApplicationTier.Model.Employee> all = employeeDAO.getAllEmployees();
            java.util.List<ApplicationTier.Model.Technician> techs = new java.util.ArrayList<>();
            if (all != null) {
                for (ApplicationTier.Model.Employee e : all) {
                    if (e instanceof ApplicationTier.Model.Technician) techs.add((ApplicationTier.Model.Technician) e);
                }
            }
            return techs;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "getAllTechnicians failed", ex);
            return java.util.Collections.emptyList();
        }
    }


    public java.util.List<String> fetchAndClearNotifications() {
        java.util.List<String> out = new ArrayList<>();
        try {
            out.addAll(notificationQueue);
            notificationQueue.clear();
        } catch (Exception ex) {
            LOGGER.log(Level.FINER, "fetchAndClearNotifications failed", ex);
        }
        return out;
    }


    public boolean isOrderReadyForDelivery(int orderId) {
        try {
            java.util.List<TestResult> results = resultDAO.getResultsByOrder(orderId);
            if (results == null || results.isEmpty()) return false;
            for (TestResult r : results) if (!r.isValidated()) return false;
            Invoice inv = invoiceDAO.getInvoiceByOrder(orderId);
            if (inv == null) return false;
            if (inv.getPaymentStatus() == null) return false;
            return inv.getPaymentStatus().toString().equalsIgnoreCase(ApplicationTier.Model.Enums.PaymentStatus.PAID.toString());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "isOrderReadyForDelivery failed", ex);
            return false;
        }
    }

    public Patient getPatientById(int patientId) {
        try {
            return patientDAO.getPatientById(patientId);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "getPatientById failed", ex);
            return null;
        }
    }

    public java.util.List<TestOrder> getOrdersByAppointment(int appointmentId) {
        try {
            Integer mapped = appointmentOrderMap.get(appointmentId);
            if (mapped != null) {
                TestOrder o = orderDAO.getOrderById(mapped);
                if (o != null) return java.util.Collections.singletonList(o);
            }
            return orderDAO.getOrdersByAppointment(appointmentId);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "getOrdersByAppointment failed", ex);
            return java.util.Collections.emptyList();
        }
    }

    public java.util.List<TestOrder> getOrdersForPatient(int patientId) {
        try {
            return orderDAO.getOrdersForPatient(patientId);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "getOrdersForPatient failed", ex);
            return java.util.Collections.emptyList();
        }
    }


    public int countValidatedOrders() {
        try {
            return orderDAO.countOrdersByStatus("VALIDATED");
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "countValidatedOrders failed", ex);
            return 0;
        }
    }

    public boolean createInvoiceForOrder(int orderId, int patientId, double totalAmount) {
        try {
            if (orderId > 0) return createInvoiceForOrderId(orderId, totalAmount);
            Invoice inv = new Invoice(0, patientId, totalAmount);
            return createInvoice(inv);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "createInvoiceForOrder failed", ex);
            return false;
        }
    }

    public boolean deletePatient(int patientId) {
        try {
            return patientDAO.deletePatient(patientId);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "deletePatient failed", ex);
            return false;
        }
    }

    public boolean updateAppointment(ApplicationTier.Model.Appointment app, java.util.List<Integer> testTypeIds, Integer technicianId) {
        if (app == null) return false;
        try {
            if (app.getAppointmentId() <= 0) {
                LOGGER.warning("updateAppointment called without valid appointmentId");
                return false;
            }
            if (app.getDate() == null) {
                LOGGER.warning("updateAppointment called with null date for appointmentId=" + app.getAppointmentId());
                return false;
            }

            ApplicationTier.Model.Appointment stored = appointmentDAO.getAppointmentById(app.getAppointmentId());
            if (stored == null) {
                LOGGER.warning("No existing appointment found in DB with id=" + app.getAppointmentId());
                return false;
            }


            stored.setPatientId(app.getPatientId() > 0 ? app.getPatientId() : stored.getPatientId());
            stored.setSecretaryId(app.getSecretaryId() > 0 ? app.getSecretaryId() : stored.getSecretaryId());
            stored.setDate(app.getDate() != null ? app.getDate() : stored.getDate());
            stored.setReason(app.getReason() != null && !app.getReason().isEmpty() ? app.getReason() : stored.getReason());
            if (app.getStatus() != null) stored.setStatus(app.getStatus());

            LOGGER.info("Updating appointmentId=" + stored.getAppointmentId() + " (patient=" + stored.getPatientId() + ")");
            boolean appointmentUpdated = appointmentDAO.updateAppointment(stored);
            if (!appointmentUpdated) LOGGER.warning("Appointment update failed for id=" + stored.getAppointmentId());

            boolean newOrderCreated = false;

            if (testTypeIds != null && !testTypeIds.isEmpty()) {
                try {
                    java.util.List<TestOrder> linkedOrders = this.getOrdersByAppointment(stored.getAppointmentId());
                    int targetOrderId = -1;
                    if (linkedOrders != null && !linkedOrders.isEmpty()) {
                        targetOrderId = linkedOrders.get(0).getOrderId();
                        LOGGER.info("Found linked TestOrder id=" + targetOrderId + " for appointment " + stored.getAppointmentId() + ", updating placeholders");
                        if (technicianId != null && technicianId > 0) orderDAO.updateTechnician(targetOrderId, technicianId);

                        java.util.List<TestResult> existing = resultDAO.getResultsByOrder(targetOrderId);
                        java.util.Set<Integer> existingTypes = new java.util.HashSet<>();
                        if (existing != null) for (TestResult r : existing) existingTypes.add(r.getTestTypeId());

                        for (Integer ttId : testTypeIds) {
                            if (!existingTypes.contains(ttId)) {
                                TestResult placeholder = new TestResult(targetOrderId, ttId, technicianId != null ? technicianId : 0);
                                boolean added = resultDAO.addResult(placeholder);
                                if (!added) LOGGER.log(java.util.logging.Level.WARNING, "Failed to add TestResult placeholder for existing orderId=" + targetOrderId + " testTypeId=" + ttId);
                            }
                        }
                    } else {
                        LOGGER.info("No linked TestOrder found for appointment " + stored.getAppointmentId() + ", creating new TestOrder for edited tests");
                        TestOrder newOrder = new TestOrder();
                        newOrder.setPatientId(app.getPatientId());
                        newOrder.setTechnicianId(technicianId != null ? technicianId : 0);
                        newOrder.setStatus(ApplicationTier.Model.Enums.TestOrderStatus.CREATED.toString());
                        int newOrderId = orderDAO.createOrder(newOrder);
                        if (newOrderId > 0) {
                            newOrderCreated = true;
                            for (Integer ttId : testTypeIds) {
                                TestResult placeholder = new TestResult(newOrderId, ttId, technicianId != null ? technicianId : 0);
                                boolean added = resultDAO.addResult(placeholder);
                                if (!added) LOGGER.log(java.util.logging.Level.WARNING, "Failed to add TestResult placeholder for new orderId=" + newOrderId + " testTypeId=" + ttId);
                            }
                            LOGGER.info("Created new unlinked TestOrder id=" + newOrderId + " for appointment edit");
                        } else {
                            LOGGER.warning("Failed to create fallback TestOrder during appointment update for apptId=" + app.getAppointmentId());
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.log(java.util.logging.Level.WARNING, "Failed while updating/creating TestOrder during appointment edit", ex);
                }
            }

            return appointmentUpdated || newOrderCreated;
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "updateAppointment (with tests) failed", ex);
            return false;
        }
    }


    public boolean linkOrderToAppointment(int orderId, int appointmentId) {
        if (orderId <= 0 || appointmentId <= 0) return false;
        try {
            boolean ok = orderDAO.updateAppointmentId(orderId, appointmentId);
            if (ok) appointmentOrderMap.put(appointmentId, orderId);
            return ok;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "linkOrderToAppointment failed", ex);
            return false;
        }
    }

}

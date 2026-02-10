package PresentationTier;

import ApplicationTier.Model.*;
import ApplicationTier.Model.Enums.AppointmentStatus;
import ApplicationTier.SecretaryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SecretaryDashboard extends JFrame {

    private final Employee currentUser;
    private final SecretaryService secretaryService;


    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(SecretaryDashboard.class.getName());

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private final List<MenuButton> menuButtons = new ArrayList<>();
    private final java.util.Map<String, MenuButton> navButtonMap = new java.util.HashMap<>();
    private ApplicationTier.TechnicianService.ValidationListener validationListener;
    private final Color bgMain = UiPalette.BG;
    private final Color accentYellow = UiPalette.SECONDARY;
    private final Color textDark = UiPalette.TEXT;
    private final Color textLight = UiPalette.TEXT_LIGHT;
    private final Color white = UiPalette.WHITE;
    private final Color primaryAction = UiPalette.MEDICAL_BLUE;


    public static final String PANEL_HOME = "HOME";
    public static final String PANEL_PATIENTS = "PATIENTS";
    public static final String PANEL_APPOINTMENTS = "APPOINTMENTS";
    public static final String PANEL_DELIVERY = "DELIVERY";
    public static final String PANEL_INVOICES = "INVOICES";

    public SecretaryDashboard(Employee user) {
        this.currentUser = user;
        this.secretaryService = new SecretaryService();
        initUI();
    }

     public JPanel getMainPanel() {
        return contentPanel;
    }


    public void showSubPanel(String key) {
        if (key == null) return;
        try {
            switch (key) {
                case PANEL_HOME: cardLayout.show(contentPanel, PANEL_HOME); break;
                case PANEL_PATIENTS: cardLayout.show(contentPanel, PANEL_PATIENTS); break;
                case PANEL_APPOINTMENTS: cardLayout.show(contentPanel, PANEL_APPOINTMENTS); break;
                case PANEL_DELIVERY: cardLayout.show(contentPanel, PANEL_DELIVERY); break;
                case PANEL_INVOICES: cardLayout.show(contentPanel, PANEL_INVOICES); break;
                default: break;
            }
        } catch (Exception ignored) {}
    }


    private void initUI() {
        setTitle("MediLab - Front Desk | " + currentUser.getFirstName());
        setSize(1300, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel sidebar = new JPanel();
        sidebar.setBackground(white);
        // Match Admin sidebar width
        sidebar.setPreferredSize(new Dimension(260, getHeight()));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));


        JLabel brand = new JLabel("MediLab");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 28));
        brand.setForeground(primaryAction);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);

        JLabel roleLabel = new JLabel("RECEPTION");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleLabel.setForeground(UiPalette.TEXT_LIGHT);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(roleLabel);

        sidebar.add(Box.createVerticalStrut(50));


        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        navPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        addSidebarButtons(navPanel);
        sidebar.add(navPanel);


        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(bgMain);
        contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        contentPanel.add(createHomePanel(), PANEL_HOME);
        contentPanel.add(createPatientsPanel(), PANEL_PATIENTS);
        contentPanel.add(createAppointmentsPanel(), PANEL_APPOINTMENTS);
        contentPanel.add(createDeliveryPanel(), PANEL_DELIVERY);
        contentPanel.add(createInvoicesPanel(), PANEL_INVOICES);

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);


        refreshPatientTable();
        refreshApptTable();
        refreshDeliveryTable();
        refreshInvoicesTable();
    }

    private void addSidebarButtons(JPanel navPanel) {
        navPanel.add(createSectionLabel("MAIN MENU"));
        navPanel.add(Box.createVerticalStrut(5));

        navPanel.add(createNavButton("Dashboard", PANEL_HOME));
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(createNavButton("Patient Management", PANEL_PATIENTS));
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(createNavButton("Appointments", PANEL_APPOINTMENTS));

        navPanel.add(Box.createVerticalStrut(25));
        navPanel.add(createSectionLabel("OPERATIONS"));
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(createNavButton("Ready Results", PANEL_DELIVERY));
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(createNavButton("Invoices", PANEL_INVOICES));

        navPanel.add(Box.createVerticalGlue());

        JButton btnProfile = createNavButton("My Profile", null);
        for (ActionListener al : btnProfile.getActionListeners()) btnProfile.removeActionListener(al);
        btnProfile.addActionListener(e -> openProfileWindow());
        navPanel.add(btnProfile);

        JButton btnLogout = createNavButton("Logout", null);
        for (ActionListener al : btnLogout.getActionListeners()) btnLogout.removeActionListener(al);
        btnLogout.addActionListener(e -> logout());
        btnLogout.setForeground(UiPalette.ERROR.darker());
        navPanel.add(btnLogout);
    }

    private DefaultTableModel todayApptModel;
    private JTable todayApptTable;

    private JPanel createHomePanel() {
        JPanel p = new JPanel(new BorderLayout(16, 16));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleWrap = new JPanel(new GridLayout(2, 1));
        titleWrap.setOpaque(false);
        JLabel title = new JLabel("Good Morning, " + currentUser.getFirstName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(textDark);
        JLabel subtitle = new JLabel("Front Desk — Today's Overview");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(textLight);
        titleWrap.add(title);
        titleWrap.add(subtitle);

        header.add(titleWrap, BorderLayout.WEST);

        JPanel hdrActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        hdrActions.setOpaque(false);
        RoundedTextField txtSearch = new RoundedTextField(18);
        txtSearch.setPlaceholder("Search appointments (patient id, reason)...");
        txtSearch.setPreferredSize(new Dimension(340, 36));

        FlatButton btnRefresh = new FlatButton("Refresh");
        btnRefresh.setPreferredSize(new Dimension(120, 36));

        hdrActions.add(txtSearch);
        hdrActions.add(btnRefresh);
        header.add(hdrActions, BorderLayout.EAST);

        p.add(header, BorderLayout.NORTH);

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 12));
        statsRow.setOpaque(false);

        JLabel lblApptCount = new JLabel("0", SwingConstants.CENTER);
        lblApptCount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblApptCount.setForeground(textDark);

        JLabel lblResultsCount = new JLabel("0", SwingConstants.CENTER);
        lblResultsCount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblResultsCount.setForeground(UiPalette.SECONDARY.darker());

        JLabel lblPendingInv = new JLabel("0", SwingConstants.CENTER);
        lblPendingInv.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblPendingInv.setForeground(UiPalette.ERROR.darker());

        JPanel cardAppts = createStatCard("Appointments Today", "0", UiPalette.MEDICAL_BLUE);
        JPanel cardResults = createStatCard("Results Ready", "0", UiPalette.SECONDARY);
        JPanel cardPending = createStatCard("Pending Invoices", "0", UiPalette.ERROR);

        cardAppts.removeAll(); cardAppts.setLayout(new BorderLayout());
        cardAppts.add(new JLabel("Appointments Today", SwingConstants.LEFT), BorderLayout.NORTH);
        cardAppts.add(lblApptCount, BorderLayout.CENTER);

        cardResults.removeAll(); cardResults.setLayout(new BorderLayout());
        cardResults.add(new JLabel("Results Ready", SwingConstants.LEFT), BorderLayout.NORTH);
        cardResults.add(lblResultsCount, BorderLayout.CENTER);

        cardPending.removeAll(); cardPending.setLayout(new BorderLayout());
        cardPending.add(new JLabel("Pending Invoices", SwingConstants.LEFT), BorderLayout.NORTH);
        cardPending.add(lblPendingInv, BorderLayout.CENTER);

        statsRow.add(cardAppts);
        statsRow.add(cardResults);
        statsRow.add(cardPending);

        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.add(statsRow, BorderLayout.NORTH);

        String[] cols = {"ID", "Patient ID", "Time / Date", "Reason", "Status"};
        todayApptModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c){return false;} };
        todayApptTable = styleTable(new JTable(todayApptModel));
        todayApptTable.setFillsViewportHeight(true);

        TableRowSorter<DefaultTableModel> todaySorter = new TableRowSorter<>(todayApptModel);
        todayApptTable.setRowSorter(todaySorter);
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = txtSearch.getText();
                if (text == null || text.trim().isEmpty()) todaySorter.setRowFilter(null);
                else todaySorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        JScrollPane scroll = new JScrollPane(todayApptTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        center.add(scroll, BorderLayout.CENTER);

        p.add(center, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> {
            refreshTodayAppointments();
            int apptCount = todayApptModel.getRowCount();
            lblApptCount.setText(String.valueOf(apptCount));
            try {
                int resultsCount = secretaryService.countValidatedOrders();
                lblResultsCount.setText(String.valueOf(resultsCount));
            } catch (Exception ex) { lblResultsCount.setText("0"); }

            try {
                int pending = 0;
                java.util.List<Invoice> allInv = secretaryService.listAllInvoices();
                for (Invoice inv : allInv) {
                    var st = inv.getPaymentStatus();
                    if (st == null || !st.toString().equalsIgnoreCase("PAID")) pending++;
                }
                lblPendingInv.setText(String.valueOf(pending));
            } catch (Exception ex) { lblPendingInv.setText("0"); }

            updateSidebarBadges();
        });

        btnRefresh.doClick();

        return p;
    }


    public void refreshTodayAppointments() {
        todayApptModel.setRowCount(0);
        try {
            List<Appointment> all = secretaryService.viewSchedule();
            if (all == null) return;

            LocalDate today = LocalDate.now();

            for (Appointment a : all) {
                Date date = a.getDate();
                if (date == null) continue;

                LocalDate local = new java.sql.Date(date.getTime()).toLocalDate();

                if (local.equals(today)) {
                    todayApptModel.addRow(new Object[]{
                            a.getAppointmentId(),
                            a.getPatientId(),
                            a.getDate(),
                            a.getReason(),
                            a.getStatus()
                    });
                }
            }

        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "Error refreshing today's appointments", ex);
        }
    }




      private DefaultTableModel patientModel;
    private JTable patientTable;
    private TableRowSorter<DefaultTableModel> patientSorter;

    private JPanel createPatientsPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setOpaque(false);

        // Header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Patient Directory");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(textDark);

        // Search Bar
        RoundedTextField txtSearch = new RoundedTextField(20);
        txtSearch.setPlaceholder("Search Patients...");
        txtSearch.setPreferredSize(new Dimension(300, 45));
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = txtSearch.getText();
                if (text.trim().length() == 0) patientSorter.setRowFilter(null);
                else patientSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        FlatButton btnAdd = new FlatButton("+ New Patient");
        btnAdd.setPreferredSize(new Dimension(160, 45));
        btnAdd.addActionListener(e -> showPatientDialog(null));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(txtSearch);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(btnAdd);

        top.add(title, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "First Name", "Last Name", "Phone", "Email", "Gender", "Date of Birth", "Address"};
        patientModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        patientTable = styleTable(new JTable(patientModel));
        patientSorter = new TableRowSorter<>(patientModel);
        patientTable.setRowSorter(patientSorter);

        patientTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && patientTable.getSelectedRow() != -1) {
                    editSelectedPatient();
                }
            }
        });

        JPopupMenu popup = new JPopupMenu();
        JMenuItem itemEdit = new JMenuItem("Edit Patient");
        JMenuItem itemDel = new JMenuItem("Delete Patient");
        itemEdit.addActionListener(e -> editSelectedPatient());
        itemDel.addActionListener(e -> deleteSelectedPatient());
        popup.add(itemEdit);
        popup.addSeparator();
        popup.add(itemDel);
        patientTable.setComponentPopupMenu(popup);

        JScrollPane scroll = new JScrollPane(patientTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(white);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(white);
        card.add(scroll);

        p.add(card, BorderLayout.CENTER);
        return p;
    }

    private void editSelectedPatient() {
        int row = patientTable.getSelectedRow();
        if (row == -1) return;
        int modelRow = patientTable.convertRowIndexToModel(row);

        Patient p = new Patient();
        p.setPatientId((int) patientModel.getValueAt(modelRow, 0));
        p.setFirstName((String) patientModel.getValueAt(modelRow, 1));
        p.setLastName((String) patientModel.getValueAt(modelRow, 2));
        p.setPhone((String) patientModel.getValueAt(modelRow, 3));
        p.setEmail((String) patientModel.getValueAt(modelRow, 4));
        p.setGender((String) patientModel.getValueAt(modelRow, 5));

        Object dateObj = patientModel.getValueAt(modelRow, 6);
        if (dateObj instanceof java.util.Date) p.setDateOfBirth((java.util.Date) dateObj);
        else if (dateObj instanceof String) {
            try {
                p.setDateOfBirth(java.sql.Date.valueOf((String) dateObj));
            } catch (Exception ignored) {
            }
        }

        p.setAddress((String) patientModel.getValueAt(modelRow, 7));
        showPatientDialog(p);
    }

    private void deleteSelectedPatient() {
        int row = patientTable.getSelectedRow();
        if (row == -1) return;
        int modelRow = patientTable.convertRowIndexToModel(row);
        int patientId = (int) patientModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this patient?\nThis will remove the patient record and may fail if there are linked appointments/orders.", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = secretaryService.deletePatient(patientId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Patient deleted.");
                refreshPatientTable();
            } else {
                JOptionPane.showMessageDialog(this, "Cannot delete patient. There may be linked appointments or orders. Please remove those first.", "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred while deleting the patient: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showPatientDialog(Patient existing) {
        boolean isEdit = (existing != null);
        JDialog d = new JDialog(this, isEdit ? "Edit Patient" : "New Patient Registration", true);
        d.setSize(600, 700);
        d.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(white);
        p.setBorder(new EmptyBorder(30, 40, 30, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 0, 5, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;

        RoundedTextField txtFirst = new RoundedTextField(20);
        txtFirst.setPlaceholder("First Name (Max 50)");
        RoundedTextField txtLast = new RoundedTextField(20);
        txtLast.setPlaceholder("Last Name (Max 50)");
        RoundedTextField txtPhone = new RoundedTextField(20);
        txtPhone.setPlaceholder("Phone (Max 20)");
        RoundedTextField txtEmail = new RoundedTextField(20);
        txtEmail.setPlaceholder("Email (Max 100)");
        RoundedTextField txtAddress = new RoundedTextField(20);
        txtAddress.setPlaceholder("Address (Max 255)");
        RoundedTextField txtDob = new RoundedTextField(20);
        txtDob.setPlaceholder("YYYY-MM-DD");

        String[] genders = {"Male", "Female"};
        JComboBox<String> cmbGender = new JComboBox<>(genders);
        cmbGender.setBackground(white);

        if (isEdit) {
            txtFirst.setText(existing.getFirstName());
            txtLast.setText(existing.getLastName());
            txtPhone.setText(existing.getPhone());
            txtEmail.setText(existing.getEmail());
            txtDob.setText(existing.getDateOfBirth() != null ? existing.getDateOfBirth().toString() : "");
            txtAddress.setText(existing.getAddress());
            cmbGender.setSelectedItem(existing.getGender());
        }

        addDialogInput(p, c, "First Name *", txtFirst);
        addDialogInput(p, c, "Last Name *", txtLast);
        addDialogInput(p, c, "Phone", txtPhone);
        addDialogInput(p, c, "Email", txtEmail);
        addDialogInput(p, c, "Gender *", cmbGender);
        addDialogInput(p, c, "Date of Birth *", txtDob);
        addDialogInput(p, c, "Address", txtAddress);

        c.insets = new Insets(30, 0, 0, 0);
        FlatButton btnSave = new FlatButton(isEdit ? "Update Patient" : "Register Patient");
        btnSave.setPreferredSize(new Dimension(200, 45));

        btnSave.addActionListener(e -> {
            try {
                if (txtFirst.getText().length() > 50 || txtLast.getText().length() > 50) {
                    JOptionPane.showMessageDialog(d, "Names too long (Max 50).");
                    return;
                }

                Patient pObj = isEdit ? existing : new Patient();
                pObj.setFirstName(txtFirst.getText());
                pObj.setLastName(txtLast.getText());
                pObj.setPhone(txtPhone.getText());
                pObj.setEmail(txtEmail.getText());
                pObj.setGender((String) cmbGender.getSelectedItem());
                pObj.setDateOfBirth(java.sql.Date.valueOf(txtDob.getText()));
                pObj.setAddress(txtAddress.getText());

                if (!isEdit) secretaryService.registerPatient(pObj);

                JOptionPane.showMessageDialog(d, "Success!");
                refreshPatientTable();
                d.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Invalid Date (YYYY-MM-DD)", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        p.add(btnSave, c);
        d.add(p);
        d.setVisible(true);
    }

     private DefaultTableModel apptModel;
    private JTable apptTable;

    private JPanel createAppointmentsPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Appointments");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(textDark);

        FlatButton btnBook = new FlatButton("+ Book Appointment");
        btnBook.setPreferredSize(new Dimension(200, 45));
        btnBook.addActionListener(e -> showBookAppointmentDialog());

        top.add(title, BorderLayout.WEST);
        top.add(btnBook, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Patient ID", "Date", "Reason / Test", "Status"};
        apptModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        apptTable = styleTable(new JTable(apptModel));

         JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        btnBar.setOpaque(false);
        FlatButton btnEdit = new FlatButton("Edit");
        FlatButton btnComplete = new FlatButton("Complete");
        FlatButton btnCancel = new FlatButton("Cancel");
        FlatButton btnDelete = new FlatButton("Delete");
        FlatButton btnInvoice = new FlatButton("Invoice");

        Dimension bsz = new Dimension(110, 36);
        btnEdit.setPreferredSize(bsz); btnComplete.setPreferredSize(bsz); btnCancel.setPreferredSize(bsz);
        btnDelete.setPreferredSize(bsz); btnInvoice.setPreferredSize(bsz);
        btnBar.add(btnEdit); btnBar.add(btnComplete); btnBar.add(btnCancel); btnBar.add(btnDelete); btnBar.add(btnInvoice);

        btnEdit.addActionListener(e -> {
            int row = apptTable.getSelectedRow(); if (row == -1) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
            int modelRow = apptTable.convertRowIndexToModel(row);
            Appointment a = new Appointment();
            a.setAppointmentId((int) apptModel.getValueAt(modelRow, 0));
            a.setPatientId((int) apptModel.getValueAt(modelRow, 1));
            Object dateObj = apptModel.getValueAt(modelRow, 2);
            if (dateObj instanceof java.util.Date) a.setDate((java.util.Date) dateObj);
            else if (dateObj instanceof String) try { a.setDate(java.sql.Date.valueOf((String) dateObj)); } catch (Exception ignored) {}
            a.setReason((String) apptModel.getValueAt(modelRow, 3));
            Object statusObj = apptModel.getValueAt(modelRow, 4);
            if (statusObj != null) a.setStatus(statusObj.toString());
            a.setSecretaryId(currentUser.getEmployeeId());
            showEditAppointmentDialog(a);
        });

        btnComplete.addActionListener(e -> {
            int row = apptTable.getSelectedRow(); if (row == -1) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
            int modelRow = apptTable.convertRowIndexToModel(row);
            int apptId = (int) apptModel.getValueAt(modelRow, 0);
            if (secretaryService.updateAppointmentStatus(apptId, AppointmentStatus.COMPLETED)) JOptionPane.showMessageDialog(this, "Appointment marked COMPLETED.");
            else JOptionPane.showMessageDialog(this, "Failed to update status.");
            refreshApptTable();
        });

        btnCancel.addActionListener(e -> {
            int row = apptTable.getSelectedRow(); if (row == -1) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
            int modelRow = apptTable.convertRowIndexToModel(row);
            int apptId = (int) apptModel.getValueAt(modelRow, 0);
            if (secretaryService.updateAppointmentStatus(apptId, AppointmentStatus.CANCELLED)) JOptionPane.showMessageDialog(this, "Appointment Cancelled.");
            else JOptionPane.showMessageDialog(this, "Failed to cancel.");
            refreshApptTable();
        });

        btnDelete.addActionListener(e -> {
            int row = apptTable.getSelectedRow(); if (row == -1) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
            int modelRow = apptTable.convertRowIndexToModel(row);
            int apptId = (int) apptModel.getValueAt(modelRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (secretaryService.deleteAppointment(apptId)) { JOptionPane.showMessageDialog(this, "Deleted."); refreshApptTable(); }
                else JOptionPane.showMessageDialog(this, "Failed to delete appointment.");
            }
        });

        btnInvoice.addActionListener(e -> {
            int row = apptTable.getSelectedRow(); if (row == -1) { JOptionPane.showMessageDialog(this, "Select an appointment first."); return; }
            int modelRow = apptTable.convertRowIndexToModel(row);
            int apptId = (int) apptModel.getValueAt(modelRow, 0);
            int patientId = (int) apptModel.getValueAt(modelRow, 1);

            // Open unified Invoice Dialog which handles order selection, creation, export and mailto fallback
            InvoiceDialog dlg = new InvoiceDialog(this, apptId, patientId);
            dlg.setVisible(true);
        });

        JScrollPane scroll = new JScrollPane(apptTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(white);
        card.add(btnBar, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        p.add(card, BorderLayout.CENTER);

        JPopupMenu popup = new JPopupMenu();
        JMenuItem pmEdit = new JMenuItem("Edit Appointment"); pmEdit.addActionListener(ev -> btnEdit.doClick());
        JMenuItem pmInv = new JMenuItem("Create Invoice"); pmInv.addActionListener(ev -> btnInvoice.doClick());
        popup.add(pmEdit); popup.addSeparator(); popup.add(pmInv);
        apptTable.setComponentPopupMenu(popup);

        refreshApptTable();

        return p;
    }

    private void showBookAppointmentDialog() {
        openAppointmentDialog(null, false);
    }

    private void showEditAppointmentDialog(Appointment a) {
        openAppointmentDialog(a, true);
    }

    private void openAppointmentDialog(Appointment existing, boolean isEdit) {
        JDialog d = new JDialog(this, isEdit ? "Edit Appointment" : "Book Appointment", true);
        d.setSize(820, 560);
        d.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(white);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

         JPanel top = new JPanel(new GridBagLayout()); top.setOpaque(false);
        GridBagConstraints tc = new GridBagConstraints();
        tc.insets = new Insets(6, 6, 6, 6);
        tc.fill = GridBagConstraints.HORIZONTAL;
        tc.gridx = 0; tc.gridy = 0; tc.weightx = 0.6;

        JLabel lblPatient = new JLabel("Patient"); lblPatient.setFont(new Font("Segoe UI", Font.BOLD, 12)); lblPatient.setForeground(textLight);
        JComboBox<Patient> cmbPatient = new JComboBox<>(); cmbPatient.setBackground(white);
        java.util.List<Patient> patients = secretaryService.getAllPatients();
        int selIndex = -1; int idx=0;
        for (Patient p : patients) { cmbPatient.addItem(p); if (existing != null && p.getPatientId() == existing.getPatientId()) selIndex = idx; idx++; }
        if (selIndex >= 0) cmbPatient.setSelectedIndex(selIndex);
        if (!isEdit && cmbPatient.getItemCount() > 0 && cmbPatient.getSelectedItem() == null) cmbPatient.setSelectedIndex(0);
        cmbPatient.setRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){
                super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                if (value instanceof Patient pat) setText(pat.getFirstName()+" "+pat.getLastName()+" (ID: "+pat.getPatientId()+")");
                return this;
            }
        });

        tc.gridx = 0; top.add(lblPatient, tc);
        tc.gridx = 1; tc.weightx = 0.4; JLabel lblDate = new JLabel("Date (YYYY-MM-DD)"); lblDate.setFont(new Font("Segoe UI", Font.BOLD, 12)); lblDate.setForeground(textLight); top.add(lblDate, tc);
        tc.gridy = 1; tc.gridx = 0; tc.weightx = 0.6; top.add(cmbPatient, tc);
        RoundedTextField txtDate = new RoundedTextField(20);
        try {
            if (existing != null && existing.getDate() != null) txtDate.setText(new java.sql.Date(existing.getDate().getTime()).toLocalDate().toString());
            else txtDate.setText(LocalDate.now().toString());
        } catch (Exception ignored) {}
        tc.gridx = 1; top.add(txtDate, tc);

        root.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(12,12)); center.setOpaque(false);

        DefaultListModel<TestCategory> catModel = new DefaultListModel<>();
        JList<TestCategory> lstCategories = new JList<>(catModel);
        lstCategories.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstCategories.setCellRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){
                super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                if (value instanceof TestCategory tcg) setText(tcg.getName());
                return this;
            }
        });
        java.util.List<TestCategory> categories = secretaryService.getAllCategories();
        for (TestCategory c : categories) catModel.addElement(c);
        JScrollPane scCats = new JScrollPane(lstCategories); scCats.setPreferredSize(new Dimension(200, 320));
        JPanel leftWrap = new JPanel(new BorderLayout(6,6)); leftWrap.setOpaque(false);
        leftWrap.add(new JLabel("Categories (multi-select)"), BorderLayout.NORTH);
        leftWrap.add(scCats, BorderLayout.CENTER);
        FlatButton btnLoad = new FlatButton("Load tests from selected"); btnLoad.setPreferredSize(new Dimension(220, 40));
        leftWrap.add(btnLoad, BorderLayout.SOUTH);

        CheckBoxList<TestType> chkTests = new CheckBoxList<>();
        JScrollPane scTests = new JScrollPane(chkTests);
        scTests.setPreferredSize(new Dimension(340, 320));
        JPanel midWrap = new JPanel(new BorderLayout(6,6)); midWrap.setOpaque(false);
        midWrap.add(new JLabel("Available Tests"), BorderLayout.NORTH);
        midWrap.add(scTests, BorderLayout.CENTER);

        DefaultListModel<TestType> selModel = new DefaultListModel<>();
        JList<TestType> preview = new JList<>(selModel);
        preview.setCellRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus){
                super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                if (value instanceof TestType tt) setText(tt.getName()+" ("+String.format("%.2f", tt.getPrice())+")");
                return this;
            }
        });
        JScrollPane scPreview = new JScrollPane(preview); scPreview.setPreferredSize(new Dimension(240,320));
        JPanel rightWrap = new JPanel(new BorderLayout(6,6)); rightWrap.setOpaque(false);
        rightWrap.add(new JLabel("Selected Tests Preview"), BorderLayout.NORTH);
        rightWrap.add(scPreview, BorderLayout.CENTER);
        JLabel lblTotal = new JLabel("Total: 0.00 EUR"); lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblTotal.setForeground(UiPalette.MEDICAL_BLUE);
        JPanel totalWrap = new JPanel(new FlowLayout(FlowLayout.LEFT)); totalWrap.setOpaque(false); totalWrap.add(lblTotal);
        rightWrap.add(totalWrap, BorderLayout.SOUTH);

        JPanel three = new JPanel(new GridLayout(1,3,12,0)); three.setOpaque(false);
        three.add(leftWrap); three.add(midWrap); three.add(rightWrap);
        center.add(three, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        final java.util.concurrent.atomic.AtomicBoolean isPreloading = new java.util.concurrent.atomic.AtomicBoolean(false);

        java.util.Set<Integer> existingTestTypeIds = new java.util.HashSet<>();
        Integer preAssignedTech = null;
        if (existing != null) {
            try {
                java.util.List<TestOrder> orders = secretaryService.getOrdersByAppointment(existing.getAppointmentId());
                if (orders != null && !orders.isEmpty()) {
                    for (TestOrder o : orders) {
                        if (o.getTechnicianId() > 0) preAssignedTech = o.getTechnicianId();
                        java.util.List<TestResult> existingResults = secretaryService.getResultsByOrder(o.getOrderId());
                        if (existingResults != null) for (TestResult r : existingResults) {
                            // guard against 0/invalid ids
                            if (r.getTestTypeId() > 0) existingTestTypeIds.add(r.getTestTypeId());
                        }
                    }
                } else {
                    // Fallback: some DB schemas may not have appointmentId; try to find the most recent order for this patient
                    java.util.List<TestOrder> patientOrders = secretaryService.getOrdersForPatient(existing.getPatientId());
                    if (patientOrders != null && !patientOrders.isEmpty()) {
                        TestOrder latest = patientOrders.get(0); // DAO returns ordered by date desc
                        if (latest != null) {
                            if (latest.getTechnicianId() > 0) preAssignedTech = latest.getTechnicianId();
                            java.util.List<TestResult> existingResults = secretaryService.getResultsByOrder(latest.getOrderId());
                            if (existingResults != null) for (TestResult r : existingResults) if (r.getTestTypeId() > 0) existingTestTypeIds.add(r.getTestTypeId());
                        }
                    }
                }
            } catch (Exception ex) { LOGGER.log(java.util.logging.Level.FINER, "preload existing tests failed", ex); }
        }

        if (isEdit) {
            try {
                isPreloading.set(true);
                preloadAppointmentSelections(existing, catModel, lstCategories, chkTests, selModel, lblTotal, isPreloading, existingTestTypeIds);
            } finally {
                isPreloading.set(false);
            }
        }

        btnLoad.addActionListener(ev -> {
            if (isPreloading.get()) return;
            java.util.List<TestCategory> selectedCats = lstCategories.getSelectedValuesList();
            // preserve user selections
            java.util.Set<Integer> preserved = new java.util.HashSet<>();
            for (TestType st : chkTests.getSelectedItems()) preserved.add(st.getTestTypeId());
            loadTestsForCategories(selectedCats, chkTests, preserved, existingTestTypeIds, isPreloading, selModel, lblTotal);
        });

        chkTests.addSelectionListener(() -> {
            if (isPreloading.get()) return;
            selModel.clear(); double tot = 0; for (TestType t : chkTests.getSelectedItems()) { selModel.addElement(t); tot += t.getPrice(); } lblTotal.setText(String.format("Total: %.2f EUR", tot));
        });

        JPanel previewBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); previewBtns.setOpaque(false);
        FlatButton btnRemovePreview = new FlatButton("Remove selected");
        FlatButton btnClearPreview = new FlatButton("Clear all");
        btnRemovePreview.setPreferredSize(new Dimension(160, 36));
        btnClearPreview.setPreferredSize(new Dimension(140, 36));
        previewBtns.add(btnRemovePreview); previewBtns.add(btnClearPreview);
        btnRemovePreview.addActionListener(ev -> { for (TestType t : preview.getSelectedValuesList()) chkTests.setChecked(t, false); selModel.clear(); double tot=0.0; for (TestType t2: chkTests.getSelectedItems()){ selModel.addElement(t2); tot+=t2.getPrice(); } lblTotal.setText(String.format("Total: %.2f EUR", tot)); });
        btnClearPreview.addActionListener(ev -> { chkTests.clearAll(); selModel.clear(); lblTotal.setText("Total: 0.00 EUR"); });
        rightWrap.add(previewBtns, BorderLayout.NORTH);

        lstCategories.addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) return;
            if (isPreloading.get()) return;
            java.util.List<TestCategory> selectedCats = lstCategories.getSelectedValuesList();
            java.util.Set<Integer> preservedIds = new java.util.HashSet<>();
            for (TestType st : chkTests.getSelectedItems()) preservedIds.add(st.getTestTypeId());
            loadTestsForCategories(selectedCats, chkTests, preservedIds, existingTestTypeIds, isPreloading, selModel, lblTotal);
        });

        JPanel bottom = new JPanel(new BorderLayout()); bottom.setOpaque(false);
        JPanel tech = new JPanel(new FlowLayout(FlowLayout.LEFT)); tech.setOpaque(false);
        tech.add(new JLabel("Assign Technician (optional): "));
        JComboBox<ApplicationTier.Model.Technician> cmbTech = new JComboBox<>(); cmbTech.setBackground(white);
        try { for (ApplicationTier.Model.Technician t : secretaryService.getAllTechnicians()) cmbTech.addItem(t); } catch (Exception ignored) {}
        cmbTech.setRenderer(new DefaultListCellRenderer(){ @Override public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean s,boolean f){ super.getListCellRendererComponent(l,v,i,s,f); if (v instanceof ApplicationTier.Model.Technician tt) setText(tt.getFirstName()+" "+tt.getLastName()+" (ID: "+tt.getEmployeeId()+")"); return this; }});
        if (preAssignedTech != null) { for (int i=0;i<cmbTech.getItemCount();i++) { ApplicationTier.Model.Technician it = cmbTech.getItemAt(i); if (it.getEmployeeId() == preAssignedTech) { cmbTech.setSelectedIndex(i); break; } } }
        tech.add(cmbTech);

        FlatButton btnSave = new FlatButton("Confirm Booking"); btnSave.setPreferredSize(new Dimension(220, 44));
        btnSave.addActionListener(ev -> {
            Patient p = (Patient) cmbPatient.getSelectedItem(); if (p == null) { JOptionPane.showMessageDialog(d, "Please select a patient."); return; }
            final java.sql.Date sqlDate;
            try { sqlDate = java.sql.Date.valueOf(txtDate.getText().trim()); } catch (Exception ex) { JOptionPane.showMessageDialog(d, "Invalid date. Use YYYY-MM-DD."); return; }
            final java.util.List<TestType> chosen = chkTests.getSelectedItems(); if (!isEdit && (chosen == null || chosen.isEmpty())) { JOptionPane.showMessageDialog(d, "Please select at least one test."); return; }

            final StringBuilder reason = new StringBuilder(); final java.util.List<Integer> testTypeIds = new java.util.ArrayList<>();
            if (chosen != null) for (int i = 0; i < chosen.size(); i++) { if (i>0) reason.append(", "); reason.append(chosen.get(i).getName()); testTypeIds.add(chosen.get(i).getTestTypeId()); }

            final Appointment app = (existing != null) ? existing : new Appointment();
            app.setPatientId(p.getPatientId());
            app.setSecretaryId(currentUser.getEmployeeId());
            app.setDate(sqlDate);
            app.setReason(reason.toString().isEmpty() ? app.getReason() : reason.toString());

            final ApplicationTier.Model.Technician assigned = (ApplicationTier.Model.Technician) cmbTech.getSelectedItem();
            final Integer techId = (assigned != null) ? assigned.getEmployeeId() : null;

            try {
                java.util.logging.Logger.getLogger(SecretaryDashboard.class.getName()).info("Saving appointment (isEdit=" + isEdit + ") apptId=" + (app.getAppointmentId()) + " tests=" + testTypeIds);
            } catch (Exception ignore) {}

             btnSave.setEnabled(false);
            final Cursor oldCursor = d.getCursor();
            d.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    if (isEdit) return secretaryService.updateAppointment(app, testTypeIds, techId);
                    else {
                        if (techId != null) return secretaryService.createAppointment(app, testTypeIds, techId);
                        return secretaryService.createAppointment(app, testTypeIds);
                    }
                }

                @Override
                protected void done() {
                    try {
                        boolean ok = get();
                        if (!ok) {
                            JOptionPane.showMessageDialog(d, isEdit ? "Failed to update appointment." : "Failed to book appointment.");
                        } else {
                            JOptionPane.showMessageDialog(d, isEdit ? "Appointment updated and tests attached." : "Appointment booked and test order created.");
                            refreshApptTable();
                            d.dispose();
                        }
                    } catch (Exception ex) {
                        LOGGER.log(java.util.logging.Level.WARNING, "Appointment save failed", ex);
                        JOptionPane.showMessageDialog(d, "Unexpected: " + ex.getMessage());
                    } finally {
                        btnSave.setEnabled(true);
                        d.setCursor(oldCursor);
                    }
                }
            };
            worker.execute();
        });

        bottom.add(tech, BorderLayout.WEST);
        JPanel saveWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT)); saveWrap.setOpaque(false); saveWrap.add(btnSave); bottom.add(saveWrap, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);

        d.setContentPane(root);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }



    private void preloadAppointmentSelections(Appointment existing,
                                               DefaultListModel<TestCategory> catModel,
                                               JList<TestCategory> lstCategories,
                                               CheckBoxList<TestType> chkTests,
                                               DefaultListModel<TestType> selModel,
                                               JLabel lblTotal,
                                               java.util.concurrent.atomic.AtomicBoolean isPreloading,
                                               java.util.Set<Integer> preExistingTestTypeIds) {
        if (existing == null) return;

        java.util.Set<Integer> existingTestTypeIds = (preExistingTestTypeIds != null) ? new java.util.HashSet<>(preExistingTestTypeIds) : new java.util.HashSet<>();
        if (preExistingTestTypeIds == null) {
            try {
                java.util.List<TestOrder> orders = secretaryService.getOrdersByAppointment(existing.getAppointmentId());
                if (orders != null) {
                    for (TestOrder o : orders) {
                        java.util.List<TestResult> results = secretaryService.getResultsByOrder(o.getOrderId());
                        if (results != null) for (TestResult r : results) if (r.getTestTypeId() > 0) existingTestTypeIds.add(r.getTestTypeId());
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(java.util.logging.Level.FINER, "preloadAppointmentSelections: failed to load existing order/results", ex);
            }
        }

        if (existingTestTypeIds.isEmpty()) {
            try {
                java.util.List<TestOrder> patientOrders = secretaryService.getOrdersForPatient(existing.getPatientId());
                if (patientOrders != null && !patientOrders.isEmpty()) {
                    TestOrder bestOrder = null;
                    java.sql.Date apptDate = existing.getDate() != null ? new java.sql.Date(existing.getDate().getTime()) : null;
                    for (TestOrder o : patientOrders) {
                            java.util.Date ordDate = o.getDateOrdered();
                            if (ordDate == null) continue;
                            if (apptDate == null || !ordDate.before(apptDate)) {
                                bestOrder = o;
                                break;
                            }
                        }
                        if (bestOrder == null) bestOrder = patientOrders.get(0);
                        if (bestOrder != null) {
                            secretaryService.linkOrderToAppointment(bestOrder.getOrderId(), existing.getAppointmentId());
                            java.util.List<TestResult> results = secretaryService.getResultsByOrder(bestOrder.getOrderId());
                            if (results != null) {
                                for (TestResult r : results) {
                                    if (r.getTestTypeId() > 0) existingTestTypeIds.add(r.getTestTypeId());
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(java.util.logging.Level.FINER, "preloadAppointmentSelections: fallback patient order mapping failed", ex);
            }
        }
        if (existingTestTypeIds.isEmpty()) return; // nothing to preselect

        java.util.List<Integer> categoryIndicesToSelect = new java.util.ArrayList<>();
        for (int ci = 0; ci < catModel.size(); ci++) {
            TestCategory tcg = catModel.get(ci);
            try {
                java.util.List<TestType> types = secretaryService.getTestTypesByCategory(tcg.getCategoryId());
                if (types != null) {
                    for (TestType tt : types) {
                        if (existingTestTypeIds.contains(tt.getTestTypeId())) { categoryIndicesToSelect.add(ci); break; }
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(java.util.logging.Level.FINER, "preloadAppointmentSelections: failed load types for category " + tcg.getCategoryId(), ex);
            }
        }


        if (!categoryIndicesToSelect.isEmpty()) {
            int[] idxs = categoryIndicesToSelect.stream().mapToInt(Integer::intValue).toArray();
            lstCategories.setSelectedIndices(idxs);
        }


        java.util.List<TestCategory> catsToLoad = new java.util.ArrayList<>();
        for (int idx : categoryIndicesToSelect) catsToLoad.add(catModel.get(idx));
        java.util.Set<Integer> emptyPreserve = new java.util.HashSet<>();
        loadTestsForCategories(catsToLoad, chkTests, emptyPreserve, existingTestTypeIds, isPreloading, selModel, lblTotal);
    }

      private void loadTestsForCategories(java.util.List<TestCategory> categories,
                                        CheckBoxList<TestType> chkTests,
                                        java.util.Set<Integer> preservedIds,
                                        java.util.Set<Integer> existingTestTypeIds,
                                        java.util.concurrent.atomic.AtomicBoolean isPreloading,
                                        DefaultListModel<TestType> selModel,
                                        JLabel lblTotal) {
        if (categories == null || categories.isEmpty()) return;
        if (isPreloading != null && isPreloading.get()) {
            }

        for (TestCategory tcg : categories) {
            try {
                java.util.List<TestType> types = secretaryService.getTestTypesByCategory(tcg.getCategoryId());
                if (types == null) continue;
                for (TestType t : types) {
                    if (!chkTests.contains(t.getTestTypeId())) chkTests.addItem(t);
                    boolean shouldCheck = (preservedIds != null && preservedIds.contains(t.getTestTypeId()))
                            || (existingTestTypeIds != null && existingTestTypeIds.contains(t.getTestTypeId()));
                    if (shouldCheck) chkTests.setChecked(t, true, false);
                }
            } catch (Exception ex) {
                LOGGER.log(java.util.logging.Level.FINER, "loadTestsForCategories failure for category " + tcg.getCategoryId(), ex);
            }
        }

        selModel.clear(); double tmpTot = 0.0; for (TestType t2 : chkTests.getSelectedItems()) { selModel.addElement(t2); tmpTot += t2.getPrice(); } lblTotal.setText(String.format("Total: %.2f EUR", tmpTot));
    }

    private DefaultTableModel deliveryModel;
    private JTable deliveryTable;

    private JPanel createDeliveryPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Results & Invoices");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(textDark);

        FlatButton btnRefresh = new FlatButton("Refresh List");
        btnRefresh.setPreferredSize(new Dimension(150, 40));
        btnRefresh.addActionListener(e -> refreshDeliveryTable());

        top.add(title, BorderLayout.WEST);
        top.add(btnRefresh, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        String[] cols = {"Order ID", "Patient ID", "Test Info", "Result Status", "Invoice Status"};
        deliveryModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        deliveryTable = styleTable(new JTable(deliveryModel));

        JPopupMenu popup = new JPopupMenu();
        JMenuItem itemPrintRes = new JMenuItem("Deliver Result");
        JMenuItem itemPrintInv = new JMenuItem("Deliver Invoice");

        itemPrintRes.addActionListener(e -> {
            int row = deliveryTable.getSelectedRow();
            if (row != -1) {
                int orderId = (int) deliveryModel.getValueAt(row, 0);
                deliverResultAction(orderId);
            }
        });

        itemPrintInv.addActionListener(e -> {
            int row = deliveryTable.getSelectedRow();
            if (row != -1) {
                int orderId = (int) deliveryModel.getValueAt(row, 0);
                Invoice inv = secretaryService.deliverInvoice(orderId);
                if (inv != null)
                    JOptionPane.showMessageDialog(this, "Invoice #" + inv.getInvoiceId() + " sent to print.");
                else JOptionPane.showMessageDialog(this, "No Invoice found for this order.");
            }
        });

        popup.add(itemPrintRes);
        popup.add(itemPrintInv);
        deliveryTable.setComponentPopupMenu(popup);

        JScrollPane scroll = new JScrollPane(deliveryTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(white);
        card.add(scroll);
        p.add(card, BorderLayout.CENTER);

        return p;
    }

    private DefaultTableModel invoicesModel;
    private JTable invoicesTable;

    private JPanel createInvoicesPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        JLabel title = new JLabel("Invoices"); title.setFont(new Font("Segoe UI", Font.BOLD, 28)); title.setForeground(textDark);
        FlatButton btnRefreshInv = new FlatButton("Refresh List"); btnRefreshInv.setPreferredSize(new Dimension(150,40));
        btnRefreshInv.addActionListener(e -> refreshInvoicesTable());
        top.add(title, BorderLayout.WEST); top.add(btnRefreshInv, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        String[] cols = {"Invoice ID","Order ID","Patient ID","Date","Amount","Status"};
        invoicesModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        invoicesTable = styleTable(new JTable(invoicesModel));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT)); toolbar.setOpaque(false);
        FlatButton btnPrint = new FlatButton("Print (HTML)"); FlatButton btnSend = new FlatButton("Send (simulated)");
        Dimension bsz = new Dimension(140,36); btnPrint.setPreferredSize(bsz); btnSend.setPreferredSize(bsz);
        toolbar.add(btnPrint); toolbar.add(btnSend);

        btnPrint.addActionListener(e -> {
            int row = invoicesTable.getSelectedRow(); if (row == -1) { JOptionPane.showMessageDialog(this, "Select an invoice first."); return; }
            int modelRow = invoicesTable.convertRowIndexToModel(row);
            int invId = (int) invoicesModel.getValueAt(modelRow, 0);
            var inv = secretaryService.getInvoiceById(invId);
            if (inv == null) { JOptionPane.showMessageDialog(this, "Invoice not found."); return; }
            if (inv.getPaymentStatus() == null || !inv.getPaymentStatus().toString().equalsIgnoreCase("PAID")) {
                JOptionPane.showMessageDialog(this, "Printing is blocked: invoice must be marked PAID by Financial Manager before printing.", "Print Blocked", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String html = "<html><body><h2>Invoice #"+inv.getInvoiceId()+"</h2><p>Order: "+inv.getOrderId()+"</p><p>Patient: "+inv.getPatientId()+"</p><p>Amount: "+inv.getTotalAmount()+" EUR</p></body></html>";
            JFileChooser fc = new JFileChooser(); if (fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
                try (var fw = new java.io.FileWriter(fc.getSelectedFile())) { fw.write(html); JOptionPane.showMessageDialog(this, "Exported."); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage()); }
            }
        });

        btnSend.addActionListener(e -> {
            int row = invoicesTable.getSelectedRow(); if (row == -1) { JOptionPane.showMessageDialog(this, "Select an invoice first."); return; }
            int modelRow = invoicesTable.convertRowIndexToModel(row);
            int invId = (int) invoicesModel.getValueAt(modelRow, 0);
            var inv = secretaryService.getInvoiceById(invId);
            if (inv == null) { JOptionPane.showMessageDialog(this, "Invoice not found."); return; }

            Patient patient = secretaryService.getPatientById(inv.getPatientId());
            String defaultTo = patient != null ? (patient.getEmail() != null ? patient.getEmail() : "") : "";

            JDialog dlg = new JDialog(this, "Simulate Invoice Email", true);
            dlg.setSize(600, 420);
            dlg.setLocationRelativeTo(this);
            JPanel panel = new JPanel(new BorderLayout(12,12));
            panel.setBorder(new EmptyBorder(12,12,12,12));
            panel.setBackground(white);

            JPanel fields = new JPanel(new GridLayout(3,1,6,6));
            fields.setOpaque(false);
            RoundedTextField txtTo = new RoundedTextField(40); txtTo.setText(defaultTo); addDialogInput(fields, new GridBagConstraints(), "To", txtTo);
            RoundedTextField txtSubject = new RoundedTextField(40); txtSubject.setText("Your MediLab Invoice #" + inv.getInvoiceId()); addDialogInput(fields, new GridBagConstraints(), "Subject", txtSubject);

            JTextArea body = new JTextArea();
            body.setLineWrap(true);
            body.setWrapStyleWord(true);
            body.setText("Dear patient,\n\nPlease find your invoice attached (simulated).\n\nInvoice #: " + inv.getInvoiceId()
                    + "\nOrder #: " + inv.getOrderId() + "\nAmount: " + inv.getTotalAmount() + " EUR\n\nRegards,\nMediLab Front Desk");
            JScrollPane bodyScroll = new JScrollPane(body);
            bodyScroll.setPreferredSize(new Dimension(540, 180));
            panel.add(fields, BorderLayout.NORTH);
            panel.add(bodyScroll, BorderLayout.CENTER);

            FlatButton sendSim = new FlatButton("Send (Simulated)");
            sendSim.setPreferredSize(new Dimension(180, 40));

            JDialog progress = new JDialog(dlg, "Sending…", true);
            progress.setUndecorated(true);
            JPanel pContent = new JPanel(new BorderLayout(12,12)) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(primaryAction);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                    g2.dispose();
                }
            };
            pContent.setBorder(new EmptyBorder(20,24,20,24));
            pContent.setBackground(UiPalette.withAlpha(UiPalette.WHITE, 245));
            JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); header.setOpaque(false);
            JLabel hdrIcon = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
            JLabel hdrTitle = new JLabel("Sending email…"); hdrTitle.setFont(new Font("Segoe UI", Font.BOLD, 16)); hdrTitle.setForeground(textDark);
            header.add(hdrIcon); header.add(hdrTitle);
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue(0);
            bar.setStringPainted(true);
            bar.setForeground(UiPalette.MEDICAL_BLUE);
            bar.setBackground(UiPalette.withAlpha(UiPalette.WHITE, 40));
            JLabel sub = new JLabel("Connecting to SMTP… (simulated)"); sub.setForeground(textLight);
            pContent.add(header, BorderLayout.NORTH);
            JPanel centerWrap = new JPanel(new BorderLayout()); centerWrap.setOpaque(false);
            centerWrap.add(sub, BorderLayout.NORTH); centerWrap.add(bar, BorderLayout.CENTER);
            pContent.add(centerWrap, BorderLayout.CENTER);
            JPanel shadow = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(UiPalette.withAlpha(UiPalette.TEXT_PRIMARY, 50));
                    g2.fillRoundRect(6,6,getWidth()-12,getHeight()-12,18,18);
                    g2.dispose();
                }
            }; shadow.setOpaque(false); shadow.setBorder(new EmptyBorder(6,6,6,6)); shadow.add(pContent);
            progress.setContentPane(shadow);
            progress.setSize(380, 170);
            progress.setLocationRelativeTo(dlg);

            sendSim.addActionListener(ae -> {
                String to = txtTo.getText();
                if (to == null || to.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Recipient email required.");
                    return;
                }
                sendSim.setEnabled(false);
                txtTo.setEnabled(false); txtSubject.setEnabled(false); body.setEnabled(false);
                SwingUtilities.invokeLater(() -> progress.setVisible(true));

                bar.setIndeterminate(false);
                bar.setMinimum(0);
                bar.setMaximum(100);
                bar.setValue(0);
                bar.setForeground(UiPalette.MEDICAL_BLUE);

                SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        int pct = 0;
                        publish(5);
                        Thread.sleep(600);
                        for (int i = 6; i <= 40; i += 2) { Thread.sleep(30); publish(i); }
                        publish(40);
                        Thread.sleep(400);
                        for (int i = 41; i <= 75; i += 4) { Thread.sleep(45); publish(i); }
                        publish(80);
                        Thread.sleep(700);
                        for (int i = 81; i <= 95; i += 3) { Thread.sleep(40); publish(i); }
                        publish(98); Thread.sleep(200);
                        publish(100);
                        return null;
                    }

                    @Override
                    protected void process(java.util.List<Integer> chunks) {
                        int v = chunks.get(chunks.size()-1);
                        bar.setValue(v);
                        if (v < 20) sub.setText("Connecting to SMTP… (simulated)");
                        else if (v < 50) sub.setText("Authenticating… (simulated)");
                        else if (v < 80) sub.setText("Uploading attachment… (simulated)");
                        else if (v < 100) sub.setText("Sending email… (simulated)");
                        else sub.setText("Completed");
                    }

                    @Override protected void done() {
                        // Hide overlay
                        progress.setVisible(false);
                        progress.dispose();

                        Object oldOptionBg = UIManager.get("OptionPane.background");
                        Object oldPanelBg = UIManager.get("Panel.background");
                        Object oldMsgFg = UIManager.get("OptionPane.messageForeground");
                        try {
                            UIManager.put("OptionPane.background", UiPalette.WHITE);
                            UIManager.put("Panel.background", UiPalette.WHITE);
                            UIManager.put("OptionPane.messageForeground", UiPalette.TEXT);

                            JOptionPane.showMessageDialog(dlg, "Email sent successfully (simulated) to " + to + "\nInvoice #" + inv.getInvoiceId());
                        } finally {
                            if (oldOptionBg != null) UIManager.put("OptionPane.background", oldOptionBg); else UIManager.put("OptionPane.background", null);
                            if (oldPanelBg != null) UIManager.put("Panel.background", oldPanelBg); else UIManager.put("Panel.background", null);
                            if (oldMsgFg != null) UIManager.put("OptionPane.messageForeground", oldMsgFg); else UIManager.put("OptionPane.messageForeground", null);
                        }

                        dlg.dispose();
                    }
                };
                worker.execute();
            });

            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            foot.setOpaque(false);
            foot.add(sendSim);
            panel.add(foot, BorderLayout.SOUTH);

            dlg.add(panel);
            dlg.setVisible(true);
        });

        JScrollPane scroll = new JScrollPane(invoicesTable); scroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel card = new JPanel(new BorderLayout()); card.setBackground(white); card.add(toolbar, BorderLayout.NORTH); card.add(scroll, BorderLayout.CENTER);
        p.add(card, BorderLayout.CENTER);
        return p;
    }

    public void refreshInvoicesTable() {
         invoicesModel.setRowCount(0);
         var list = secretaryService.listAllInvoices();
         if (list != null) {
             for (var inv : list) {
                 java.util.List<TestResult> results = secretaryService.getResultsByOrder(inv.getOrderId());
                 if (results == null || results.isEmpty()) continue;
                 invoicesModel.addRow(new Object[]{inv.getInvoiceId(), inv.getOrderId(), inv.getPatientId(), inv.getDate(), inv.getTotalAmount(), inv.getPaymentStatus()});
             }
         }
    }

    public void refreshDeliveryTable() {
         deliveryModel.setRowCount(0);
         try {
             for (TestOrder o : secretaryService.getPendingOrders()) {
                 int orderId = o.getOrderId();
                 int patientId = o.getPatientId();

                 java.util.List<TestResult> results = secretaryService.getResultsByOrder(orderId);
                 StringBuilder tests = new StringBuilder();
                 boolean allValidated = true;
                 if (results == null || results.isEmpty()) {
                     continue;
                 } else {
                     for (int i = 0; i < results.size(); i++) {
                         TestResult r = results.get(i);
                         if (r.getTestName() != null) tests.append(r.getTestName());
                         else tests.append("Test#").append(r.getTestTypeId());
                         if (i < results.size() - 1) tests.append(", ");
                         if (!r.isValidated()) allValidated = false;
                     }
                 }

                String resultStatus = allValidated ? "VALIDATED" : "PENDING";

                ApplicationTier.Model.Invoice inv = secretaryService.getInvoiceByOrder(orderId);
                String invoiceStatus = (inv == null) ? "NO INVOICE" : (inv.getPaymentStatus() != null ? inv.getPaymentStatus().toString() : "UNKNOWN");

                deliveryModel.addRow(new Object[]{orderId, patientId, tests.toString(), resultStatus, invoiceStatus});
            }
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "Failed to refresh delivery table", ex);
        }
        updateSidebarBadges();
     }

    private Timer notificationTimer;
    private void startNotificationPolling() {
        if (notificationTimer != null) return;
        notificationTimer = new Timer(5000, e -> {
            try {
                java.util.List<String> notes = secretaryService.fetchAndClearNotifications();
                if (notes != null && !notes.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (String n : notes) sb.append("• ").append(n).append("\n");
                    // Show a non-blocking info dialog and refresh delivery table so Secretary can act
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(SecretaryDashboard.this, sb.toString(), "New Validated Results", JOptionPane.INFORMATION_MESSAGE);
                        refreshDeliveryTable();
                    });
                }
            } catch (Throwable ignored) {}
        });
        notificationTimer.setInitialDelay(2000);
        notificationTimer.start();
    }

    @Override public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) startNotificationPolling();
        else if (notificationTimer != null) notificationTimer.stop();
    }

    public void refreshPatientTable() {
        patientModel.setRowCount(0);
        try {
            for (Patient p : secretaryService.getAllPatients()) {
                patientModel.addRow(new Object[]{
                        p.getPatientId(), p.getFirstName(), p.getLastName(),
                        p.getPhone(), p.getEmail(), p.getGender(), p.getDateOfBirth(), p.getAddress()
                });
            }
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.WARNING, "Failed loading patients", e);
        }
    }

    public void refreshApptTable() {
        apptModel.setRowCount(0);
        try {
            List<Appointment> list = secretaryService.viewSchedule();
            if (list != null) {
                for (Appointment a : list) {
                    apptModel.addRow(new Object[]{a.getAppointmentId(), a.getPatientId(), a.getDate(), a.getReason(), a.getStatus()});
                }
            }
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.WARNING, "Failed loading appointments", e);
        }
    }

    private JTable styleTable(JTable table) {
        table.setRowHeight(50);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setGridColor(UiPalette.PANEL);
        table.setSelectionBackground(UiPalette.MEDICAL_BLUE);
        table.setSelectionForeground(textDark);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(white);
        table.getTableHeader().setForeground(textLight);
        return table;
    }

    private void addDialogInput(JPanel p, GridBagConstraints c, String lbl, JComponent comp) {
        JLabel l = new JLabel(lbl);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(UiPalette.TEXT_LIGHT);
        p.add(l, c);
        c.gridy++;
        comp.setPreferredSize(new Dimension(300, 40));
        p.add(comp, c);
        c.gridy++;
    }

    private JPanel createStatCard(String title, String val, Color c) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(white);
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        JLabel lTitle = new JLabel(title.toUpperCase());
        lTitle.setForeground(UiPalette.TEXT_LIGHT);
        lTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        JLabel lVal = new JLabel(val);
        lVal.setForeground(c);
        lVal.setFont(new Font("Segoe UI", Font.BOLD, 48));
        card.add(lTitle, BorderLayout.NORTH);
        card.add(lVal, BorderLayout.CENTER);
        return card;
    }

    private JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(UiPalette.TEXT_LIGHT);
        lbl.setBorder(new EmptyBorder(10, 30, 5, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private MenuButton createNavButton(String text, String targetPanel) {
        MenuButton btn = new MenuButton(text);
        btn.addActionListener(e -> {
            for (MenuButton b : menuButtons) b.setSelected(false);
            btn.setSelected(true);
            if (targetPanel != null) {
                if (targetPanel.equals(PANEL_PATIENTS)) refreshPatientTable();
                if (targetPanel.equals(PANEL_APPOINTMENTS)) refreshApptTable();
                if (targetPanel.equals(PANEL_DELIVERY)) refreshDeliveryTable();
                if (targetPanel.equals(PANEL_INVOICES)) refreshInvoicesTable();
                cardLayout.show(contentPanel, targetPanel);
            }
        });
        if (menuButtons.isEmpty() && targetPanel != null) btn.setSelected(true);
        menuButtons.add(btn);
        if (targetPanel != null) navButtonMap.put(targetPanel, btn);
        return btn;
    }
    private void updateSidebarBadges() {
        try {
            int validatedOrders = secretaryService.countValidatedOrders();
            MenuButton b = navButtonMap.get(PANEL_DELIVERY);
            if (b != null) {
                String base = "Ready Results";
                if (validatedOrders > 0) b.setText(base + " (" + validatedOrders + ")");
                else b.setText(base);
            }
        } catch (Exception ex) {

        }
    }

    private void logout() {

        try { if (validationListener != null) ApplicationTier.TechnicianService.removeValidationListener(validationListener); } catch (Exception ignored) {}
        dispose();
        new LoginPage().setVisible(true);
    }


    private void openProfileWindow() {
        JDialog profileDialog = new JDialog(this, "My Profile", true);
        profileDialog.setSize(520, 620);
        profileDialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(white);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 20, 0);

        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // use primary action color (medical blue / or purple in global palette)
                g2.setColor(primaryAction);
                g2.fillOval(0,0,120,120);
                String initials = "";
                if (currentUser.getFirstName() != null && !currentUser.getFirstName().isEmpty()) initials += currentUser.getFirstName().charAt(0);
                if (currentUser.getLastName() != null && !currentUser.getLastName().isEmpty()) initials += currentUser.getLastName().charAt(0);
                g2.setColor(textDark);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 42));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (120 - fm.stringWidth(initials)) / 2;
                int ty = (120 + fm.getAscent()) / 2 - 6;
                g2.drawString(initials, tx, ty);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(120,120));
        p.add(avatar, c);

        c.gridy++; c.insets = new Insets(10,0,5,0);
        JLabel name = new JLabel(currentUser.getFullName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 26)); name.setForeground(textDark);
        p.add(name, c);

        c.gridy++; c.insets = new Insets(0,0,20,0);
        JLabel role = new JLabel(currentUser.getRole().toString()); role.setFont(new Font("Segoe UI", Font.BOLD, 12)); role.setForeground(primaryAction);
        p.add(role, c);


        c.gridy++; c.insets = new Insets(8,0,8,0);
        JPanel infoGrid = new JPanel(new GridLayout(3,2,30,12)); infoGrid.setOpaque(false);
        addProfileField(infoGrid, "Username", currentUser.getUsername());
        addProfileField(infoGrid, "Employee ID", String.valueOf(currentUser.getEmployeeId()));
        addProfileField(infoGrid, "Email", currentUser.getEmail());
        addProfileField(infoGrid, "Phone", currentUser.getPhone());
        addProfileField(infoGrid, "Address", currentUser.getAddress());
        addProfileField(infoGrid, "Hire Date", currentUser.getHireDate() != null ? currentUser.getHireDate().toString() : "N/A");
        p.add(infoGrid, c);

        c.gridy++; c.insets = new Insets(30,0,0,0);
        FlatButton btnClose = new FlatButton("Close"); btnClose.setPreferredSize(new Dimension(160,40));
        btnClose.addActionListener(e -> profileDialog.dispose());
        p.add(btnClose, c);

        profileDialog.add(p);
        profileDialog.setVisible(true);
    }

    private void addProfileField(JPanel p, String label, String value) {
         JPanel field = new JPanel(new BorderLayout(0,6)); field.setOpaque(false);
         JLabel l = new JLabel(label.toUpperCase()); l.setFont(new Font("Segoe UI", Font.BOLD, 11)); l.setForeground(textLight);
         JLabel v = new JLabel(value != null ? value : "-"); v.setFont(new Font("Segoe UI", Font.PLAIN, 14)); v.setForeground(textDark);
         field.add(l, BorderLayout.NORTH); field.add(v, BorderLayout.CENTER);
         p.add(field);
     }


    class MenuButton extends JButton {
        boolean isSelected = false;

        public MenuButton(String t) {
            super(t);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(textLight);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(12, 25, 12, 10));
            setMaximumSize(new Dimension(240, 50));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setOpaque(false);

        }

        public void setSelected(boolean b) {
            isSelected = b;
            setForeground(b ? textDark : textLight);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected) {
                g2.setColor(UiPalette.withAlpha(UiPalette.MEDICAL_BLUE, 20)); // Very light accent bg
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(primaryAction);
                g2.fillRect(0, 8, 4, getHeight() - 16); // Accent bar
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class FlatButton extends JButton {
        public FlatButton(String t) {
            super(t);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            // Button text should be white by default to match requested theme
            setForeground(UiPalette.WHITE);
            setBackground(UiPalette.MEDICAL_BLUE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    static class RoundedTextField extends JTextField {
        private String ph = "";

        public RoundedTextField(int c) {
            super(c);
            setOpaque(false);
            setBorder(new EmptyBorder(5, 15, 5, 15));
        }

        public void setPlaceholder(String p) {
            ph = p;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UiPalette.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 45, 45);
            g2.setColor(UiPalette.PANEL);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 45, 45);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UiPalette.TEXT_LIGHT);
                g2.drawString(ph, 15, getHeight() / 2 + 5);
                g2.dispose();
            }
        }


    }


    static class CheckBoxList<E> extends JList<E> {
        private final DefaultListModel<E> model = new DefaultListModel<>();
        private final java.util.Set<Object> checkedKeys = new java.util.HashSet<>();
        private Runnable selectionListener = null;

        private Object keyOf(E item) {
            if (item instanceof TestType) return ((TestType) item).getTestTypeId();
            return item;
        }

        public CheckBoxList() {
            super();
            setModel(model);
            setCellRenderer(new ListCellRenderer<E>() {
                @Override
                public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
                    JCheckBox box = new JCheckBox();
                    box.setOpaque(false);
                    box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    if (value instanceof TestType) {
                        TestType tt = (TestType) value;
                        box.setText(tt.getName() + " (" + String.format("%.2f", tt.getPrice()) + ")");
                    } else {
                        box.setText(String.valueOf(value));
                    }
                    box.setSelected(checkedKeys.contains(keyOf(value)));
                    return box;
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    int idx = locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        E item = model.get(idx);
                        Object k = keyOf(item);
                        if (checkedKeys.contains(k)) checkedKeys.remove(k); else checkedKeys.add(k);
                        repaint(getCellBounds(idx, idx));
                        if (selectionListener != null) selectionListener.run();
                    }
                }
            });
        }

        public void addItem(E item) { model.addElement(item); }

        public boolean contains(int testTypeId) {
            for (int i = 0; i < model.size(); i++) {
                Object v = model.get(i);
                if (v instanceof TestType tt && tt.getTestTypeId() == testTypeId) return true;
            }
            return false;
        }

        public java.util.List<E> getSelectedItems() {
            java.util.List<E> out = new java.util.ArrayList<>();
            for (int i = 0; i < model.size(); i++) {
                E it = model.get(i);
                if (checkedKeys.contains(keyOf(it))) out.add(it);
            }
            return out;
        }

        public void setChecked(E item, boolean checked) { setChecked(item, checked, true); }

        public void setChecked(E item, boolean checked, boolean fireEvent) {
            Object k = keyOf(item);
            for (int i = 0; i < model.size(); i++) {
                E m = model.get(i);
                boolean match;
                if (m instanceof TestType && item instanceof TestType) match = ((TestType) m).getTestTypeId() == ((TestType) item).getTestTypeId();
                else match = m.equals(item);
                if (match) {
                    if (checked) checkedKeys.add(k); else checkedKeys.remove(k);
                    repaint(getCellBounds(i, i));
                    if (fireEvent && selectionListener != null) selectionListener.run();
                    return;
                }
            }
        }

        public void clearAll() { model.clear(); checkedKeys.clear(); if (selectionListener != null) selectionListener.run(); }

        public void addSelectionListener(Runnable r) { this.selectionListener = r; }
    }

    private void deliverResultAction(int orderId) {
        if (secretaryService.isOrderReadyForDelivery(orderId)) {
            java.util.List<TestResult> results = secretaryService.deliverResult(orderId);
            if (results != null && !results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Results for Order #" + orderId + " are ready and can be delivered/printed.", "Delivery Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "An unexpected error occurred while fetching results for Order #" + orderId, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            ApplicationTier.Model.Invoice inv = secretaryService.getInvoiceByOrder(orderId);
            boolean isPaid = inv != null && inv.getPaymentStatus() != null && inv.getPaymentStatus().toString().equalsIgnoreCase("PAID");

            java.util.List<TestResult> results = secretaryService.getResultsByOrder(orderId);
            boolean allValidated = results != null && results.stream().allMatch(TestResult::isValidated);

            if (!allValidated) {
                JOptionPane.showMessageDialog(this, "Cannot deliver: Results for Order #" + orderId + " are not yet validated by the technician.", "Delivery Blocked", JOptionPane.WARNING_MESSAGE);
            } else if (!isPaid) {
                JOptionPane.showMessageDialog(this, "Cannot deliver: Invoice for Order #" + orderId + " has not been marked as PAID by the Financial Manager.", "Delivery Blocked", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Cannot deliver results for Order #" + orderId + ". Please check status.", "Delivery Blocked", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}

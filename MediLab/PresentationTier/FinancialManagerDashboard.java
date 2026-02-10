package PresentationTier;

import ApplicationTier.FinancialManagerService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionListener;
import ApplicationTier.Model.Employee;

public class FinancialManagerDashboard extends JFrame {

    private final Employee currentUser;
    private final FinancialManagerService fmService = new FinancialManagerService();

    private JPanel contentPanel;
    private CardLayout cardLayout;

    private JLabel lblPendingCount;
    private JLabel lblPendingAmount;
    private javax.swing.table.DefaultTableModel invoicesModel;
    private javax.swing.JTable invoicesTable;

    private static final String PANEL_INVOICES = "INVOICES";
    private static final String PANEL_REPORTS = "REPORTS";
    private static final String PANEL_HOME = "HOME";
    private final java.util.List<MenuButton> menuButtons = new java.util.ArrayList<>();

    private final ApplicationTier.TechnicianService.ValidationListener validationListener = orderId -> SwingUtilities.invokeLater(() -> {
        try { refreshStats(); } catch (Exception ignored) {}
        try { refreshInvoices(); } catch (Exception ignored) {}
        try {
            java.util.logging.Logger.getLogger(FinancialManagerDashboard.class.getName()).info("Validation notification received for orderId=" + orderId);
        } catch (Exception ignored) {}
    });

    public FinancialManagerDashboard(Employee user) {
        this.currentUser = user;
        initUI();
        try {
            ApplicationTier.TechnicianService.addValidationListener(validationListener);
            java.util.logging.Logger.getLogger(FinancialManagerDashboard.class.getName()).info("Registered Finance validation listener");
        } catch (Throwable ex) {
            java.util.logging.Logger.getLogger(FinancialManagerDashboard.class.getName()).log(java.util.logging.Level.WARNING, "Failed to register validation listener", ex);
        }
    }

    @Override
    public void dispose() {
        try {
            ApplicationTier.TechnicianService.removeValidationListener(validationListener);
            java.util.logging.Logger.getLogger(FinancialManagerDashboard.class.getName()).info("Unregistered Finance validation listener");
        } catch (Throwable ignored) {}
        super.dispose();
    }


    public JPanel getMainPanel() {
        return contentPanel;
    }


    public void showSubPanel(String key) {
        if (key == null) return;
        try {
            switch (key) {
                case PANEL_HOME: cardLayout.show(contentPanel, PANEL_HOME); break;
                case PANEL_INVOICES: cardLayout.show(contentPanel, PANEL_INVOICES); break;
                case PANEL_REPORTS: cardLayout.show(contentPanel, PANEL_REPORTS); break;
                default: break;
            }
        } catch (Exception ignored) {}
    }

    private void initUI() {
        setTitle("MediLab - Finance | " + currentUser.getFirstName());
        setSize(1200, 820);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel sidebar = new JPanel();
        sidebar.setBackground(UiPalette.WHITE);
        sidebar.setPreferredSize(new Dimension(260, getHeight()));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(28, 20, 28, 20));

        JLabel brand = new JLabel("MediLab"); brand.setFont(new Font("Segoe UI", Font.BOLD, 28)); brand.setForeground(UiPalette.MEDICAL_BLUE); brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);
        JLabel role = new JLabel("FINANCE"); role.setFont(new Font("Segoe UI", Font.BOLD, 11)); role.setForeground(UiPalette.TEXT_LIGHT); role.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(role);
        sidebar.add(Box.createVerticalStrut(30));

        JPanel nav = new JPanel(); nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS)); nav.setOpaque(false);
        nav.add(createSectionLabel("MAIN MENU")); nav.add(Box.createVerticalStrut(6));
        nav.add(createNavButton("Home", PANEL_HOME)); nav.add(Box.createVerticalStrut(6));
        nav.add(createNavButton("Invoices", PANEL_INVOICES)); nav.add(Box.createVerticalStrut(6));
        nav.add(createNavButton("Reports", PANEL_REPORTS)); nav.add(Box.createVerticalGlue());
        sidebar.add(nav);


        MenuButton btnProfile = createNavButton("My Profile", null);

        for (ActionListener al : btnProfile.getActionListeners()) btnProfile.removeActionListener(al);
        btnProfile.addActionListener(e -> openProfile());
        sidebar.add(btnProfile);

        MenuButton btnLogout = createNavButton("Logout", null);
        for (ActionListener al : btnLogout.getActionListeners()) btnLogout.removeActionListener(al);
        btnLogout.addActionListener(e -> { dispose(); new LoginPage().setVisible(true); });
        btnLogout.setForeground(UiPalette.ERROR);
        sidebar.add(btnLogout);

        add(sidebar, BorderLayout.WEST);


        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UiPalette.BG);
        contentPanel.setBorder(new EmptyBorder(22,22,22,22));



        contentPanel.add(createHomePanel(), PANEL_HOME);
        contentPanel.add(createInvoicesPanel(), PANEL_INVOICES);

        contentPanel.add(createReportsPanel(), PANEL_REPORTS);

        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, PANEL_HOME);
        refreshStats();
    }

    private JPanel createReportsPanel() {
        JPanel p = new JPanel(new BorderLayout(12,12)); p.setOpaque(false);
        JLabel title = new JLabel("Financial Reports"); title.setFont(new Font("Segoe UI", Font.BOLD, 28)); title.setForeground(UiPalette.TEXT);
        p.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(12,12)); center.setOpaque(false);

        JPanel rangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)); rangePanel.setOpaque(false);
        rangePanel.setBorder(new EmptyBorder(6,6,6,6));
        rangePanel.add(new JLabel("From:"));
        SpinnerDateModel fromModel = new SpinnerDateModel(java.util.Date.from(java.time.LocalDate.now().minusDays(7).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spFrom = new JSpinner(fromModel); spFrom.setEditor(new JSpinner.DateEditor(spFrom, "yyyy-MM-dd")); spFrom.setPreferredSize(new Dimension(120, 28));
        SpinnerDateModel toModel = new SpinnerDateModel(java.util.Date.from(java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner spTo = new JSpinner(toModel); spTo.setEditor(new JSpinner.DateEditor(spTo, "yyyy-MM-dd")); spTo.setPreferredSize(new Dimension(120, 28));
        rangePanel.add(spFrom); rangePanel.add(new JLabel("To:")); rangePanel.add(spTo);

        UiPalette.FlatButton btnRun = new UiPalette.FlatButton("Run"); btnRun.setPreferredSize(new Dimension(110, 34));
        UiPalette.FlatButton btnPreview = new UiPalette.FlatButton("Preview"); btnPreview.setPreferredSize(new Dimension(110, 34));
        UiPalette.FlatButton btnPdf = new UiPalette.FlatButton("Export PDF"); btnPdf.setPreferredSize(new Dimension(120, 34));
        UiPalette.FlatButton btnHtml = new UiPalette.FlatButton("Export HTML"); btnHtml.setPreferredSize(new Dimension(120, 34));
        rangePanel.add(btnRun); rangePanel.add(btnPreview); rangePanel.add(btnPdf); rangePanel.add(btnHtml);

        center.add(rangePanel, BorderLayout.NORTH);

         String[] cols = {"Invoice ID","Order ID","Patient ID","Date","Amount","Status"};
        DefaultTableModel invModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c){return false;} };
        JTable invTable = new JTable(invModel); invTable.setRowHeight(40); invTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane tableSp = new JScrollPane(invTable); tableSp.setBorder(BorderFactory.createEmptyBorder());
        center.add(tableSp, BorderLayout.CENTER);

        JPanel totals = new JPanel(new FlowLayout(FlowLayout.RIGHT)); totals.setOpaque(false);
        JLabel lblCount = new JLabel("Count: 0"); lblCount.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblCount.setForeground(UiPalette.TEXT);
        JLabel lblSum = new JLabel("Total (PAID): 0.00 EUR"); lblSum.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblSum.setForeground(UiPalette.ACCENT);
        totals.add(lblCount); totals.add(Box.createHorizontalStrut(12)); totals.add(lblSum);
        center.add(totals, BorderLayout.SOUTH);

        p.add(center, BorderLayout.CENTER);

        btnRun.addActionListener(e -> {
            java.util.Date fromDate = (java.util.Date) spFrom.getValue();
            java.util.Date toDate = (java.util.Date) spTo.getValue();
            java.time.LocalDate start = fromDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            java.time.LocalDate end = toDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            if (end.isBefore(start)) { JOptionPane.showMessageDialog(this, "Invalid range: 'To' must be same or after 'From'.", "Invalid Range", JOptionPane.WARNING_MESSAGE); return; }

            invModel.setRowCount(0);
            final JDialog spinner = new JDialog(this, "Running Report...", true);
            spinner.setUndecorated(true);
            JPanel spPanel = new JPanel(new BorderLayout()); spPanel.setBorder(new EmptyBorder(12,12,12,12)); spPanel.setBackground(UiPalette.WHITE);
            spPanel.add(new JLabel("Generating report, please wait...", SwingConstants.CENTER), BorderLayout.CENTER);
            spinner.add(spPanel); spinner.setSize(260,80); spinner.setLocationRelativeTo(this);

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                java.util.List<ApplicationTier.Model.Invoice> list;
                double paidSum = 0.0; int count = 0;
                @Override protected Void doInBackground() {
                    list = fmService.getInvoicesBetween(start, end);
                    if (list != null) {
                        for (ApplicationTier.Model.Invoice inv : list) {
                            if (inv.getPaymentStatus() != null && inv.getPaymentStatus().toString().equalsIgnoreCase(ApplicationTier.Model.Enums.PaymentStatus.PAID.toString())) paidSum += inv.getTotalAmount();
                            count++;
                        }
                    }
                    return null;
                }
                @Override protected void done() {
                    spinner.dispose();
                    invModel.setRowCount(0);
                    if (list != null) {
                        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
                        for (ApplicationTier.Model.Invoice inv : list) invModel.addRow(new Object[]{inv.getInvoiceId(), inv.getOrderId(), inv.getPatientId(), inv.getDate(), df.format(inv.getTotalAmount()), inv.getPaymentStatus()});
                    }
                    lblCount.setText("Count: " + count);
                    lblSum.setText(String.format("Total (PAID): %.2f EUR", paidSum));
                }
            };
            worker.execute(); spinner.setVisible(true);
        });

        btnPreview.addActionListener(e -> {
            if (invModel.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "No data to preview. Run a report first."); return; }
            String html = buildHtmlFromModel(invModel, "Report Preview");
            JDialog dlg = new JDialog(this, "Report Preview", true); dlg.setSize(800, 600); dlg.setLocationRelativeTo(this);
            JEditorPane ep = new JEditorPane("text/html", html); ep.setEditable(false);
            JScrollPane spHtml = new JScrollPane(ep); spHtml.setBorder(BorderFactory.createEmptyBorder());
            JPanel panel = new JPanel(new BorderLayout()); panel.setBackground(UiPalette.WHITE); panel.add(spHtml, BorderLayout.CENTER);
            UiPalette.FlatButton close = new UiPalette.FlatButton("Close"); close.addActionListener(ae -> dlg.dispose());
            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT)); foot.setOpaque(false); foot.add(close);
            panel.add(foot, BorderLayout.SOUTH);
            dlg.add(panel); dlg.setVisible(true);
        });

        btnPdf.addActionListener(e -> {
            if (invModel.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "No data to export. Run a report first."); return; }
            JFileChooser fc = new JFileChooser(); if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            java.io.File f = fc.getSelectedFile();
            if (isOpenPdfAvailable()) { JOptionPane.showMessageDialog(this, "OpenPDF detected. PDF export not implemented in this scaffold; writing HTML fallback instead."); }
            try {
                writeHtmlReport(f, invModel);
                JOptionPane.showMessageDialog(this, "Exported (HTML format) to " + f.getAbsolutePath() + (isOpenPdfAvailable()?"\n(Note: OpenPDF detected but PDF generation not implemented in scaffold.)":"\n(Install OpenPDF to enable true PDF export.)"));
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage()); }
        });

        btnHtml.addActionListener(e -> {
            if (invModel.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "No data to export. Run a report first."); return; }
            JFileChooser fc = new JFileChooser(); if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            java.io.File f = fc.getSelectedFile();
            try { writeHtmlReport(f, invModel); JOptionPane.showMessageDialog(this, "Exported HTML to " + f.getAbsolutePath()); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage()); }
        });

        return p;
    }

    private JPanel createHomePanel() {
        JPanel p = new JPanel(new BorderLayout(16, 16)); p.setOpaque(false);


        JPanel statsRow = new JPanel(new GridLayout(1, 2, 12, 12)); statsRow.setOpaque(false);
        JPanel cardPending = createStatMiniCard("Pending Invoices", "0");
        JPanel cardAmount = createStatMiniCard("Total Pending (EUR)", "0.00");
        lblPendingCount = (JLabel) cardPending.getComponent(1);
        lblPendingAmount = (JLabel) cardAmount.getComponent(1);
        statsRow.add(cardPending); statsRow.add(cardAmount);

        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actionsRow.setOpaque(false);
        JTextField txtSearch = new JTextField(); txtSearch.setPreferredSize(new Dimension(300, 34));
        UiPalette.FlatButton btnRefresh = new UiPalette.FlatButton("Refresh"); btnRefresh.setPreferredSize(new Dimension(120, 34));
        actionsRow.add(txtSearch); actionsRow.add(btnRefresh);

        String[] cols = {"Invoice ID","Order ID","Patient ID","Date","Amount","Status"};
        DefaultTableModel homeModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        JTable homeTable = new JTable(homeModel); homeTable.setRowHeight(40); homeTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(homeModel); homeTable.setRowSorter(sorter);
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter(){ public void keyReleased(java.awt.event.KeyEvent e){ String t = txtSearch.getText(); sorter.setRowFilter(t==null||t.trim().isEmpty()?null:javax.swing.RowFilter.regexFilter("(?i)"+t)); }});

        JScrollPane sp = new JScrollPane(homeTable); sp.setBorder(BorderFactory.createEmptyBorder());
        JPanel center = new JPanel(new BorderLayout(12,12)); center.setOpaque(false);
        JPanel topArea = new JPanel(new BorderLayout()); topArea.setOpaque(false);
        topArea.add(statsRow, BorderLayout.NORTH);
        topArea.add(actionsRow, BorderLayout.SOUTH);
        center.add(topArea, BorderLayout.NORTH);
        JPanel card = new JPanel(new BorderLayout()); card.setBackground(UiPalette.WHITE); card.setBorder(new EmptyBorder(12,12,12,12));
        card.add(sp, BorderLayout.CENTER);
        center.add(card, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> {
            homeModel.setRowCount(0);
            java.util.List<ApplicationTier.Model.Invoice> all = fmService.findAllInvoices();
            int pending = 0; double totalPending = 0.0;
            if (all != null) {
                for (ApplicationTier.Model.Invoice inv : all) {
                    boolean isPaid = inv.getPaymentStatus() != null && inv.getPaymentStatus().toString().equalsIgnoreCase(ApplicationTier.Model.Enums.PaymentStatus.PAID.toString());
                    if (!isPaid) {
                        homeModel.addRow(new Object[]{inv.getInvoiceId(), inv.getOrderId(), inv.getPatientId(), inv.getDate(), inv.getTotalAmount(), inv.getPaymentStatus()});
                        pending++; totalPending += inv.getTotalAmount();
                    }
                }
            }
            lblPendingCount.setText(String.valueOf(pending));
            lblPendingAmount.setText(String.format("%.2f", totalPending));
        });
        btnRefresh.doClick();

        return p;
    }

    private JPanel createInvoicesPanel() {
        JPanel p = new JPanel(new BorderLayout(12,12)); p.setOpaque(false);
        JLabel title = new JLabel("Invoices"); title.setFont(new Font("Segoe UI", Font.BOLD, 28)); title.setForeground(UiPalette.TEXT);
        p.add(title, BorderLayout.NORTH);

        String[] cols = {"Invoice ID","Order ID","Patient ID","Date","Amount","Status"};
        invoicesModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        invoicesTable = new JTable(invoicesModel); invoicesTable.setRowHeight(40); invoicesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane sp = new JScrollPane(invoicesTable); sp.setBorder(BorderFactory.createEmptyBorder());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8,8)); toolbar.setOpaque(false);
        UiPalette.FlatButton btnRefresh = new UiPalette.FlatButton("Refresh");
        UiPalette.FlatButton btnMarkPaid = new UiPalette.FlatButton("Mark Paid");
        UiPalette.FlatButton btnMarkPaidBulk = new UiPalette.FlatButton("Mark Paid (Selected)");
        UiPalette.FlatButton btnDeleteInv = new UiPalette.FlatButton("Delete Selected");
        UiPalette.FlatButton btnExportCsv = new UiPalette.FlatButton("Export CSV");
        UiPalette.FlatButton btnPrint = new UiPalette.FlatButton("Print (HTML)");
        UiPalette.FlatButton btnSend = new UiPalette.FlatButton("Send (simulated)");
        String[] statusOptions = {"All","PAID","UNPAID"};
        JComboBox<String> cbStatus = new JComboBox<>(statusOptions); cbStatus.setPreferredSize(new Dimension(120, 30));
        toolbar.add(btnRefresh); toolbar.add(cbStatus); toolbar.add(btnMarkPaid); toolbar.add(btnMarkPaidBulk); toolbar.add(btnDeleteInv); toolbar.add(btnExportCsv); toolbar.add(btnPrint); toolbar.add(btnSend);

        btnRefresh.addActionListener(e -> refreshInvoices());

        btnMarkPaid.addActionListener(e -> {
            int r = invoicesTable.getSelectedRow(); if (r == -1) { JOptionPane.showMessageDialog(this, "Select an invoice first."); return; }
            int mr = invoicesTable.convertRowIndexToModel(r);
            int invId = (int) invoicesModel.getValueAt(mr, 0);
            ApplicationTier.Model.Invoice inv = fmService.findInvoiceById(invId);
            if (inv == null) { JOptionPane.showMessageDialog(this, "Invoice not found."); return; }
            int confirm = JOptionPane.showConfirmDialog(this, "Mark invoice #"+invId+" as PAID?","Confirm Payment", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            boolean ok = fmService.validatePayment(invId, inv.getTotalAmount());
            if (ok) { JOptionPane.showMessageDialog(this, "Invoice marked PAID."); } else { JOptionPane.showMessageDialog(this, "Failed to mark as PAID.", "Error", JOptionPane.ERROR_MESSAGE); }
            btnRefresh.doClick();
        });

        btnMarkPaidBulk.addActionListener(e -> {
            int[] sel = invoicesTable.getSelectedRows(); if (sel==null||sel.length==0){ JOptionPane.showMessageDialog(this, "Select one or more invoices to mark paid."); return; }
            int confirm = JOptionPane.showConfirmDialog(this, "Mark selected invoice(s) as PAID?","Confirm Bulk Payment", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            int success = 0; for (int r : sel) { int mr = invoicesTable.convertRowIndexToModel(r); int invId = (int) invoicesModel.getValueAt(mr, 0); ApplicationTier.Model.Invoice inv = fmService.findInvoiceById(invId); if (inv!=null) { boolean ok = fmService.validatePayment(invId, inv.getTotalAmount()); if (ok) success++; } }
            JOptionPane.showMessageDialog(this, "Marked " + success + " invoice(s) as PAID."); refreshInvoices();
        });

        btnDeleteInv.addActionListener(e -> {
            int[] sel = invoicesTable.getSelectedRows(); if (sel==null||sel.length==0){ JOptionPane.showMessageDialog(this, "Select one or more invoices to delete."); return; }
            int confirm = JOptionPane.showConfirmDialog(this, "Delete selected invoice(s)? This is irreversible.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            int success = 0; for (int r : sel) { int mr = invoicesTable.convertRowIndexToModel(r); int invId = (int) invoicesModel.getValueAt(mr, 0); if (fmService.deleteInvoice(invId)) success++; }
            JOptionPane.showMessageDialog(this, "Deleted " + success + " invoice(s)."); refreshInvoices();
        });

        btnExportCsv.addActionListener(e -> {
            if (invoicesModel.getRowCount()==0){ JOptionPane.showMessageDialog(this, "No invoices to export."); return; }
            JFileChooser fc = new JFileChooser(); if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return; java.io.File f = fc.getSelectedFile();
            try (java.io.FileWriter fw = new java.io.FileWriter(f)) {
                // header
                for (int cidx=0;cidx<invoicesModel.getColumnCount();cidx++){ fw.write(invoicesModel.getColumnName(cidx)); if (cidx<invoicesModel.getColumnCount()-1) fw.write(','); }
                fw.write('\n');
                for (int r=0;r<invoicesModel.getRowCount();r++){ for (int cidx=0;cidx<invoicesModel.getColumnCount();cidx++){ Object v = invoicesModel.getValueAt(r,cidx); String cell = v==null?"":String.valueOf(v).replaceAll("\n"," ").replaceAll(",",";"); fw.write(cell); if (cidx<invoicesModel.getColumnCount()-1) fw.write(','); } fw.write('\n'); }
                JOptionPane.showMessageDialog(this, "Exported CSV to " + f.getAbsolutePath());
            } catch (Exception ex){ JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage()); }
        });

        cbStatus.addActionListener(e -> { String sel = (String) cbStatus.getSelectedItem(); applyStatusFilter(invoicesModel, sel); });

        invoicesTable.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent e) {
                 if (e.getClickCount() == 2) {
                     int r = invoicesTable.getSelectedRow(); if (r == -1) return;
                     int mr = invoicesTable.convertRowIndexToModel(r);
                     int invId = (int) invoicesModel.getValueAt(mr, 0);
                     ApplicationTier.Model.Invoice inv = fmService.findInvoiceById(invId);
                     if (inv == null) { JOptionPane.showMessageDialog(null, "Invoice not found."); return; }
                     StringBuilder html = new StringBuilder(); html.append("<html><body><h2>Invoice #").append(inv.getInvoiceId()).append("</h2>");
                     html.append("<p>Order: ").append(inv.getOrderId()).append("</p><p>Patient: ").append(inv.getPatientId()).append("</p>");
                     html.append(String.format("<p>Amount: %.2f EUR</p><p>Status: %s</p>", inv.getTotalAmount(), inv.getPaymentStatus()));
                     // Load tests for the order
                     java.util.List<ApplicationTier.Model.TestResult> results = fmService.getResultsForOrder(inv.getOrderId());
                     if (results != null && !results.isEmpty()) {
                         html.append("<h3>Tests</h3><ul>");
                         for (ApplicationTier.Model.TestResult tr : results) html.append("<li>").append(tr.getTestName()).append(" - ").append(tr.getValue()==null?"(no value)":tr.getValue()).append(tr.isValidated()?" (validated)":"").append("</li>");
                         html.append("</ul>");
                     }
                     html.append("</body></html>");
                     JDialog dlg = new JDialog(FinancialManagerDashboard.this, "Invoice Preview", true); dlg.setSize(640,480); dlg.setLocationRelativeTo(FinancialManagerDashboard.this);
                     JEditorPane ep = new JEditorPane("text/html", html.toString()); ep.setEditable(false);
                     dlg.add(new JScrollPane(ep)); dlg.setVisible(true);
                 }
             }
        });

        JPanel card = new JPanel(new BorderLayout()); card.setBackground(UiPalette.WHITE);
        card.add(toolbar, BorderLayout.NORTH); card.add(sp, BorderLayout.CENTER);
        p.add(card, BorderLayout.CENTER);
        // initial load
        refreshInvoices();
        return p;
    }

    public void refreshInvoices() {
        try {
            if (invoicesModel == null) return;
            invoicesModel.setRowCount(0);
            java.util.List<ApplicationTier.Model.Invoice> list = fmService.findAllInvoices();
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
            if (list != null) {
                for (ApplicationTier.Model.Invoice inv : list) {
                    invoicesModel.addRow(new Object[]{inv.getInvoiceId(), inv.getOrderId(), inv.getPatientId(), inv.getDate(), df.format(inv.getTotalAmount()), inv.getPaymentStatus()});
                }
            }
            refreshStats();
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(FinancialManagerDashboard.class.getName()).log(java.util.logging.Level.WARNING, "refreshInvoices failed", ex);
        }
    }

    private void applyStatusFilter(DefaultTableModel model, String status) {
        if (status == null || status.equalsIgnoreCase("All")) return; // rely on Refresh for heavy filtering
        for (int r = model.getRowCount()-1; r>=0; r--) {
            Object s = model.getValueAt(r, 5); String st = s==null?"":s.toString();
            if (status.equalsIgnoreCase("PAID")) { if (!st.equalsIgnoreCase(ApplicationTier.Model.Enums.PaymentStatus.PAID.toString())) model.removeRow(r); }
            else if (status.equalsIgnoreCase("UNPAID")) { if (st.equalsIgnoreCase(ApplicationTier.Model.Enums.PaymentStatus.PAID.toString())) model.removeRow(r); }
        }
    }



    private JPanel createStatMiniCard(String title, String val) {
        JPanel card = new JPanel(new BorderLayout()); card.setOpaque(false);
        card.setBorder(new EmptyBorder(8,12,8,12));
        JLabel t = new JLabel(title); t.setFont(new Font("Segoe UI", Font.BOLD, 12)); t.setForeground(UiPalette.TEXT_LIGHT);
        JLabel v = new JLabel(val); v.setFont(new Font("Segoe UI", Font.BOLD, 18)); v.setForeground(UiPalette.ACCENT);
        card.add(t, BorderLayout.NORTH); card.add(v, BorderLayout.CENTER);
        return card;
    }

    public void refreshStats() {
        try {
            java.util.List<ApplicationTier.Model.Invoice> all = fmService.findAllInvoices();
            int pending = 0; double totalPending = 0.0;
            if (all != null) {
                for (ApplicationTier.Model.Invoice inv : all) {
                    if (inv.getPaymentStatus() == null || !inv.getPaymentStatus().toString().equalsIgnoreCase(ApplicationTier.Model.Enums.PaymentStatus.PAID.toString())) {
                        pending++;
                        totalPending += inv.getTotalAmount();
                    }
                }
            }
            lblPendingCount.setText(String.valueOf(pending));
            lblPendingAmount.setText(String.format("%.2f", totalPending));
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(FinancialManagerDashboard.class.getName()).log(java.util.logging.Level.WARNING, "refreshStats failed", ex);
        }
    }

    private boolean isOpenPdfAvailable() {
        try { Class.forName("com.lowagie.text.Document"); return true; } catch (Throwable t) { return false; }
    }

    private MenuButton createNavButton(String text, String panelKey) {
        MenuButton btn = new MenuButton(text);
        btn.addActionListener(e -> {
            for (MenuButton b : menuButtons) b.setSelected(false);
            btn.setSelected(true);
            if (panelKey != null) cardLayout.show(contentPanel, panelKey);
        });
        if (menuButtons.isEmpty() && panelKey != null) btn.setSelected(true);
        menuButtons.add(btn);
        return btn;
    }

    private JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text); lbl.setFont(new Font("Segoe UI", Font.BOLD, 10)); lbl.setForeground(UiPalette.TEXT_LIGHT); lbl.setBorder(new EmptyBorder(10, 30, 5, 0)); return lbl;
    }

    private void openProfile() {
        JDialog profileDialog = new JDialog(this, "My Profile", true);
        profileDialog.setSize(520, 620);
        profileDialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(UiPalette.WHITE);
        GridBagConstraints c = new GridBagConstraints(); c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.CENTER; c.insets = new Insets(0,0,20,0);

        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiPalette.MEDICAL_BLUE);
                g2.fillOval(0,0,120,120);
                String initials = "";
                if (currentUser.getFirstName() != null && !currentUser.getFirstName().isEmpty()) initials += currentUser.getFirstName().charAt(0);
                if (currentUser.getLastName() != null && !currentUser.getLastName().isEmpty()) initials += currentUser.getLastName().charAt(0);
                g2.setColor(UiPalette.TEXT);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 42));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (120 - fm.stringWidth(initials)) / 2;
                int ty = (120 + fm.getAscent()) / 2 - 6;
                g2.drawString(initials, tx, ty);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(120,120)); p.add(avatar, c);

        c.gridy++; c.insets = new Insets(10,0,5,0);
        JLabel name = new JLabel(currentUser.getFullName()); name.setFont(new Font("Segoe UI", Font.BOLD, 26)); name.setForeground(UiPalette.TEXT);
        p.add(name, c);

        c.gridy++; c.insets = new Insets(0,0,20,0);
        JLabel role = new JLabel("FINANCE"); role.setFont(new Font("Segoe UI", Font.BOLD, 12)); role.setForeground(UiPalette.MEDICAL_BLUE);
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
        UiPalette.FlatButton close = new UiPalette.FlatButton("Close"); close.setPreferredSize(new Dimension(160,40)); close.addActionListener(ae -> profileDialog.dispose()); p.add(close, c);
        profileDialog.add(p); profileDialog.setVisible(true);
    }

    private void addProfileField(JPanel p, String label, String value) {
        JPanel field = new JPanel(new BorderLayout(0,6)); field.setOpaque(false);
        JLabel l = new JLabel(label.toUpperCase()); l.setFont(new Font("Segoe UI", Font.BOLD, 11)); l.setForeground(UiPalette.TEXT_LIGHT);
        JLabel v = new JLabel(value != null ? value : "-"); v.setFont(new Font("Segoe UI", Font.PLAIN, 14)); v.setForeground(UiPalette.TEXT);
        field.add(l, BorderLayout.NORTH); field.add(v, BorderLayout.CENTER);
        p.add(field);
    }


    private void writeHtmlReport(java.io.File f, DefaultTableModel invModel) throws java.io.IOException {
        try (java.io.FileWriter fw = new java.io.FileWriter(f)) {
            fw.write("<html><head><meta charset=\"utf-8\"><style>table{border-collapse:collapse;}td,th{padding:6px;border:1px solid " + UiPalette.toHex(UiPalette.PANEL_BG) + ";}th{background:" + UiPalette.toHex(UiPalette.WHITE) + ";}</style></head><body>");
            fw.write("<h2>Invoice Report</h2><table>");
            fw.write("<tr>");
            for (int c = 0; c < invModel.getColumnCount(); c++) fw.write("<th>" + escapeHtml(invModel.getColumnName(c)) + "</th>");
            fw.write("</tr>");
            for (int r = 0; r < invModel.getRowCount(); r++) {
                fw.write("<tr>");
                for (int c = 0; c < invModel.getColumnCount(); c++) fw.write("<td>" + escapeHtml(String.valueOf(invModel.getValueAt(r, c))) + "</td>");
                fw.write("</tr>");
            }
            fw.write("</table></body></html>");
        }
    }

    private String escapeHtml(String s) { if (s == null) return ""; return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); }

    private String buildHtmlFromModel(DefaultTableModel m, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset=\"utf-8\"><style>");
        sb.append("body{font-family:Segoe UI, sans-serif;} table{border-collapse:collapse;width:100%;} th,td{border:1px solid " + UiPalette.toHex(UiPalette.PANEL_BG) + ";padding:8px;} th{background:" + UiPalette.toHex(UiPalette.WHITE) + ";text-align:left;} h2{color:" + UiPalette.toHex(UiPalette.ACCENT) + ";}");
        sb.append("</style></head><body>");
        sb.append("<h2>").append(title).append("</h2><table><tr>");
        for (int c = 0; c < m.getColumnCount(); c++) sb.append("<th>").append(escapeHtml(m.getColumnName(c))).append("</th>");
        sb.append("</tr>");
        for (int r = 0; r < m.getRowCount(); r++) {
            sb.append("<tr>");
            for (int c = 0; c < m.getColumnCount(); c++) sb.append("<td>").append(escapeHtml(String.valueOf(m.getValueAt(r, c)))).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table></body></html>");
        return sb.toString();
    }

    class MenuButton extends JButton {
        private boolean isSelected = false;

        public MenuButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(UiPalette.TEXT_LIGHT);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(12, 25, 12, 10));
            setMaximumSize(new Dimension(240, 50));
            setOpaque(false);
        }

        public void setSelected(boolean b) {
            isSelected = b;
            setForeground(b ? UiPalette.MEDICAL_BLUE : UiPalette.TEXT_LIGHT);
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected) {
                g2.setColor(UiPalette.withAlpha(UiPalette.MEDICAL_BLUE, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(UiPalette.MEDICAL_BLUE);
                g2.fillRect(0, 8, 4, getHeight() - 16);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

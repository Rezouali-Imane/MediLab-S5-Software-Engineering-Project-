package PresentationTier;

import ApplicationTier.Model.Enums.Role;
import ApplicationTier.Model.Employee;
import ApplicationTier.SuperAdminService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;


interface SecretaryAccess {
    void openSecretaryConsole();
    JPanel getSecretaryPanel();
    void refreshSecretaryConsole();
}

interface TechnicianAccess {
    void openTechnicianConsole();
    JPanel getTechnicianPanel();
    void refreshTechnicianConsole();
}

interface FinanceAccess {
    void openFinanceConsole();
    JPanel getFinancePanel();
    void refreshFinanceConsole();
}

    public class SuperAdminDashboard extends AdminDashboard implements SecretaryAccess, TechnicianAccess, FinanceAccess {

    private final SuperAdminService superService;

    private static final String PANEL_LOGS = "LOGS";
    private static final String PANEL_AUDIT = "AUDIT";
    private static final String PANEL_BACKUP = "BACKUP";
    private static final String PANEL_OVERRIDE = "OVERRIDE";
    private static final String PANEL_STRICT_ADD = "STRICT_ADD";
    private static final String PANEL_SECRETARY_CONSOLE = "SEC_CONSOLE";
    private static final String PANEL_TECHNICIAN_CONSOLE = "TECH_CONSOLE";
    private static final String PANEL_FINANCE_CONSOLE = "FIN_CONSOLE";

    private final ApplicationTier.SecretaryService secService = new ApplicationTier.SecretaryService();
    private final ApplicationTier.TechnicianService techServiceLocal = new ApplicationTier.TechnicianService();
    private final ApplicationTier.FinancialManagerService fmServiceLocal = new ApplicationTier.FinancialManagerService();
    private final JPanel secretaryPanel;
    private final JPanel technicianPanel;
    private final JPanel financePanel;

    public SuperAdminDashboard(Employee user) {
        super(user);
        this.superService = new SuperAdminService();


        contentPanel.add(createLogsPanel(), PANEL_LOGS);
        contentPanel.add(createAuditPanel(), PANEL_AUDIT);
        contentPanel.add(createBackupPanel(), PANEL_BACKUP);
        contentPanel.add(createOverridePanel(), PANEL_OVERRIDE);
        contentPanel.add(createStrictRegistrationPanel(), PANEL_STRICT_ADD);
        contentPanel.add(createUnifiedHomePanel(), "UNIFIED_HOME");
        cardLayout.show(contentPanel, "UNIFIED_HOME");
        SecretaryPanel secPanel = new SecretaryPanel(user);
        contentPanel.add(secPanel, PANEL_SECRETARY_CONSOLE);
        secretaryPanel = secPanel;

        TechnicianPanel techPanel = new TechnicianPanel(user);
        contentPanel.add(techPanel, PANEL_TECHNICIAN_CONSOLE);
        technicianPanel = techPanel;

        FinancialPanel finPanel = new FinancialPanel(user);
        contentPanel.add(finPanel, PANEL_FINANCE_CONSOLE);
        financePanel = finPanel;

        setTitle("MediLab - SUPER ADMIN | System Control Center");
    }


    @Override
    protected void addSidebarButtons(JPanel navPanel) {
        navPanel.add(createSectionLabel("MAIN"));
        navPanel.add(Box.createVerticalStrut(8));
        navPanel.add(createNavButton("Super Admin Home", "UNIFIED_HOME"));
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(createNavButton("Staff Directory", PANEL_STAFF));
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(createNavButton("Register Staff", PANEL_STRICT_ADD));

        navPanel.add(Box.createVerticalStrut(18));
        navPanel.add(createSectionLabel("SYSTEM"));
        navPanel.add(Box.createVerticalStrut(6));
        MenuButton btnLogs = createNavButton("View System Logs", PANEL_LOGS);
        navPanel.add(Box.createVerticalStrut(6));
        navPanel.add(createNavButton("Login Audit", PANEL_AUDIT));
        btnLogs.setForeground(UiPalette.SECONDARY.darker());
        navPanel.add(btnLogs);
        navPanel.add(Box.createVerticalStrut(6));
        MenuButton btnBackup = createNavButton("Database Backup", PANEL_BACKUP);
        btnBackup.setForeground(UiPalette.SECONDARY.darker());
        navPanel.add(btnBackup);
        navPanel.add(Box.createVerticalStrut(6));
        MenuButton btnOverride = createNavButton("Override Validation", PANEL_OVERRIDE);
        btnOverride.setForeground(UiPalette.ERROR.darker());
        navPanel.add(btnOverride);

        navPanel.add(Box.createVerticalStrut(18));
        navPanel.add(createSectionLabel("ROLE CONSOLES"));
        navPanel.add(Box.createVerticalStrut(6));
        MenuButton secHeader = new MenuButton("Secretary");
        secHeader.setToolTipText("Toggle Secretary pages");
        JPanel secSub = new JPanel(); secSub.setLayout(new BoxLayout(secSub, BoxLayout.Y_AXIS)); secSub.setOpaque(false);
        secSub.setBorder(new EmptyBorder(6, 14, 6, 0)); secSub.setVisible(false);
        secHeader.addActionListener(e -> secSub.setVisible(!secSub.isVisible()));
        navPanel.add(secHeader);
        MenuButton sHome = new MenuButton("  • Dashboard"); sHome.setFont(sHome.getFont().deriveFont(Font.PLAIN, 12f)); sHome.addActionListener(a -> { try { if (secretaryPanel instanceof SecretaryPanel) { ((SecretaryPanel)secretaryPanel).refreshTodayAppointments(); ((SecretaryPanel)secretaryPanel).showSubPanel(SecretaryDashboard.PANEL_HOME); } cardLayout.show(contentPanel, PANEL_SECRETARY_CONSOLE);} catch (Exception ignored){} }); secSub.add(sHome);
        MenuButton sPatients = new MenuButton("  • Patient Management"); sPatients.setFont(sPatients.getFont().deriveFont(Font.PLAIN, 12f)); sPatients.addActionListener(a -> { try { if (secretaryPanel instanceof SecretaryPanel) { ((SecretaryPanel)secretaryPanel).refreshPatientTable(); ((SecretaryPanel)secretaryPanel).showSubPanel(SecretaryDashboard.PANEL_PATIENTS); } cardLayout.show(contentPanel, PANEL_SECRETARY_CONSOLE);} catch (Exception ignored){} }); secSub.add(sPatients);
        MenuButton sAppts = new MenuButton("  • Appointments"); sAppts.setFont(sAppts.getFont().deriveFont(Font.PLAIN, 12f)); sAppts.addActionListener(a -> { try { if (secretaryPanel instanceof SecretaryPanel) { ((SecretaryPanel)secretaryPanel).refreshApptTable(); ((SecretaryPanel)secretaryPanel).showSubPanel(SecretaryDashboard.PANEL_APPOINTMENTS); } cardLayout.show(contentPanel, PANEL_SECRETARY_CONSOLE);} catch (Exception ignored){} }); secSub.add(sAppts);
        MenuButton sReady = new MenuButton("  • Ready Results"); sReady.setFont(sReady.getFont().deriveFont(Font.PLAIN, 12f)); sReady.addActionListener(a -> { try { if (secretaryPanel instanceof SecretaryPanel) { ((SecretaryPanel)secretaryPanel).refreshDeliveryTable(); ((SecretaryPanel)secretaryPanel).showSubPanel(SecretaryDashboard.PANEL_DELIVERY); } cardLayout.show(contentPanel, PANEL_SECRETARY_CONSOLE);} catch (Exception ignored){} }); secSub.add(sReady);
        MenuButton sInv = new MenuButton("  • Invoices"); sInv.setFont(sInv.getFont().deriveFont(Font.PLAIN, 12f)); sInv.addActionListener(a -> { try { if (secretaryPanel instanceof SecretaryPanel) { ((SecretaryPanel)secretaryPanel).refreshInvoicesTable(); ((SecretaryPanel)secretaryPanel).showSubPanel(SecretaryDashboard.PANEL_INVOICES); } cardLayout.show(contentPanel, PANEL_SECRETARY_CONSOLE);} catch (Exception ignored){} }); secSub.add(sInv);
        navPanel.add(secSub);

        navPanel.add(Box.createVerticalStrut(8));

        MenuButton techHeader = new MenuButton("Technician");
        JPanel techSub = new JPanel(); techSub.setLayout(new BoxLayout(techSub, BoxLayout.Y_AXIS)); techSub.setOpaque(false);
        techSub.setBorder(new EmptyBorder(6, 14, 6, 0)); techSub.setVisible(false);
        techHeader.addActionListener(e -> techSub.setVisible(!techSub.isVisible()));
        navPanel.add(techHeader);
         MenuButton tHome = new MenuButton("  • Today's Orders"); tHome.setFont(tHome.getFont().deriveFont(Font.PLAIN, 12f)); tHome.addActionListener(a -> { try { if (technicianPanel instanceof TechnicianPanel) { ((TechnicianPanel)technicianPanel).refreshTodayOrders(); ((TechnicianPanel)technicianPanel).showSubPanel(TechnicianDashboard.KEY_HOME); } cardLayout.show(contentPanel, PANEL_TECHNICIAN_CONSOLE);} catch (Exception ignored){} }); techSub.add(tHome);
        MenuButton tWork = new MenuButton("  • Pending Orders"); tWork.setFont(tWork.getFont().deriveFont(Font.PLAIN, 12f)); tWork.addActionListener(a -> { try { if (technicianPanel instanceof TechnicianPanel) { ((TechnicianPanel)technicianPanel).refreshWorklistTable(); ((TechnicianPanel)technicianPanel).showSubPanel(TechnicianDashboard.KEY_WORKLIST); } cardLayout.show(contentPanel, PANEL_TECHNICIAN_CONSOLE);} catch (Exception ignored){} }); techSub.add(tWork);
        MenuButton tBench = new MenuButton("  • Lab Bench"); tBench.setFont(tBench.getFont().deriveFont(Font.PLAIN, 12f)); tBench.addActionListener(a -> { try { if (technicianPanel instanceof TechnicianPanel) { ((TechnicianPanel)technicianPanel).refreshBenchTable(); ((TechnicianPanel)technicianPanel).showSubPanel(TechnicianDashboard.KEY_BENCH); } cardLayout.show(contentPanel, PANEL_TECHNICIAN_CONSOLE);} catch (Exception ignored){} }); techSub.add(tBench);
        MenuButton tHist = new MenuButton("  • Test History"); tHist.setFont(tHist.getFont().deriveFont(Font.PLAIN, 12f)); tHist.addActionListener(a -> { try { if (technicianPanel instanceof TechnicianPanel) { ((TechnicianPanel)technicianPanel).refreshHistoryTable(); ((TechnicianPanel)technicianPanel).showSubPanel(TechnicianDashboard.KEY_HISTORY); } cardLayout.show(contentPanel, PANEL_TECHNICIAN_CONSOLE);} catch (Exception ignored){} }); techSub.add(tHist);
        MenuButton tCat = new MenuButton("  • Test Catalog"); tCat.setFont(tCat.getFont().deriveFont(Font.PLAIN, 12f)); tCat.addActionListener(a -> { try { if (technicianPanel instanceof TechnicianPanel) { ((TechnicianPanel)technicianPanel).refreshCatalogTable(); ((TechnicianPanel)technicianPanel).showSubPanel(TechnicianDashboard.KEY_CATALOG); } cardLayout.show(contentPanel, PANEL_TECHNICIAN_CONSOLE);} catch (Exception ignored){} }); techSub.add(tCat);
        navPanel.add(techSub);

        navPanel.add(Box.createVerticalStrut(8));

        MenuButton finHeader = new MenuButton("Finance");
        JPanel finSub = new JPanel(); finSub.setLayout(new BoxLayout(finSub, BoxLayout.Y_AXIS)); finSub.setOpaque(false);
        finSub.setBorder(new EmptyBorder(6, 14, 6, 0)); finSub.setVisible(false);
        finHeader.addActionListener(e -> finSub.setVisible(!finSub.isVisible()));
        navPanel.add(finHeader);
        MenuButton fHome = new MenuButton("  • Finance - Home"); fHome.setFont(fHome.getFont().deriveFont(Font.PLAIN, 12f)); fHome.addActionListener(a -> { try { if (financePanel instanceof FinancialPanel) { ((FinancialPanel)financePanel).refreshStats(); ((FinancialPanel)financePanel).showSubPanel("HOME"); } cardLayout.show(contentPanel, PANEL_FINANCE_CONSOLE);} catch (Exception ignored){} }); finSub.add(fHome);
        MenuButton fInv = new MenuButton("  • Invoices"); fInv.setFont(fInv.getFont().deriveFont(Font.PLAIN, 12f)); fInv.addActionListener(a -> { try { if (financePanel instanceof FinancialPanel) { ((FinancialPanel)financePanel).refreshStats(); ((FinancialPanel)financePanel).showSubPanel("INVOICES"); } cardLayout.show(contentPanel, PANEL_FINANCE_CONSOLE);} catch (Exception ignored){} }); finSub.add(fInv);
        MenuButton fRpt = new MenuButton("  • Reports"); fRpt.setFont(fRpt.getFont().deriveFont(Font.PLAIN, 12f)); fRpt.addActionListener(a -> { try { if (financePanel instanceof FinancialPanel) { ((FinancialPanel)financePanel).refreshStats(); ((FinancialPanel)financePanel).showSubPanel("REPORTS"); } cardLayout.show(contentPanel, PANEL_FINANCE_CONSOLE);} catch (Exception ignored){} }); finSub.add(fRpt);
        navPanel.add(finSub);

        navPanel.add(Box.createVerticalGlue());

        navPanel.add(createSectionLabel("SESSION"));
        navPanel.add(Box.createVerticalStrut(8));
        MenuButton btnProfile = createNavButton("My Profile", null);
        for (ActionListener al : btnProfile.getActionListeners()) btnProfile.removeActionListener(al);
        btnProfile.addActionListener(e -> openProfileWindow());
        navPanel.add(btnProfile);

        navPanel.add(Box.createVerticalStrut(5));
        MenuButton btnLogout = createNavButton("Logout", null);
        for (ActionListener al : btnLogout.getActionListeners()) btnLogout.removeActionListener(al);
        btnLogout.addActionListener(e -> logout());
        btnLogout.setForeground(UiPalette.ERROR.darker());
        navPanel.add(btnLogout);
    }

    private void logout() {
        dispose();
        new PresentationTier.LoginPage().setVisible(true);
    }

    private void styleDarkButton(AbstractButton b, Color bg) {
        if (b == null) return;
        if (bg == null) bg = UiPalette.PRIMARY_DARK;
        b.setBackground(bg);
        b.setForeground(UiPalette.contrastText(bg));
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setFont(UiPalette.UI_FONT_BOLD);
    }

    private JPanel createLogsPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setBackground(bgMain);
        p.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("System Audit Logs");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(textDark);
        p.add(title, BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setOpaque(false);
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setPreferredSize(new Dimension(110, 34));
        JButton btnDialog = new JButton("Open in Dialog");
        btnDialog.setPreferredSize(new Dimension(140, 34));
        styleDarkButton(btnRefresh, UiPalette.PRIMARY);
        styleDarkButton(btnDialog, UiPalette.SECONDARY.darker());
        toolbar.add(btnRefresh);
        toolbar.add(btnDialog);
        p.add(toolbar, BorderLayout.SOUTH);

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        logArea.setBackground(UiPalette.PANEL.darker());
        logArea.setForeground(UiPalette.SUCCESS.darker());
        logArea.setMargin(new Insets(15, 15, 15, 15));

        JPanel hud = new JPanel(new BorderLayout(8,0)); hud.setOpaque(false);
        JLabel lblPath = new JLabel("Path: " + (superService.getActiveLogPath().isEmpty() ? "(not set)" : superService.getActiveLogPath()));
        lblPath.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblPath.setForeground(textLight);
        JLabel lblTs = new JLabel("Last refresh: N/A"); lblTs.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblTs.setForeground(textLight);
        hud.add(lblPath, BorderLayout.WEST); hud.add(lblTs, BorderLayout.EAST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); controls.setOpaque(false);
        JButton btnPrev = new JButton("Recent"); btnPrev.setPreferredSize(new Dimension(80, 30));
        JButton btnNext = new JButton("Prev"); btnNext.setPreferredSize(new Dimension(80, 30));
        JTextField txtSearch = new JTextField(18); txtSearch.setPreferredSize(new Dimension(220, 30)); txtSearch.setToolTipText("Search login history...");
        JButton btnExport = new JButton("Export"); btnExport.setPreferredSize(new Dimension(100,30));
        styleDarkButton(btnPrev, UiPalette.PRIMARY_DARK);
        styleDarkButton(btnNext, UiPalette.PRIMARY_DARK);
        styleDarkButton(btnExport, UiPalette.PRIMARY);
        controls.add(new JLabel("Search:")); controls.add(txtSearch); controls.add(btnPrev); controls.add(btnNext); controls.add(btnExport);

        final int[] page = {0}; final int pageSize = 50;

        String logs = superService.viewLoginHistory(superService.getActiveLogPath(), page[0], pageSize, null);
        logArea.setText(logs != null ? logs : "> Waiting for login history...");

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(white);
        card.setBorder(BorderFactory.createLineBorder(UiPalette.PANEL_BG));
        card.add(hud, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(controls, BorderLayout.SOUTH);
        p.add(card, BorderLayout.CENTER);

        Runnable refreshHud = () -> {
            long ts = superService.getLastRefreshTimestamp();
            if (ts > 0) {
                java.time.Instant inst = java.time.Instant.ofEpochMilli(ts);
                String pretty = java.time.ZonedDateTime.ofInstant(inst, java.time.ZoneId.systemDefault()).toString();
                lblTs.setText("Last refresh: " + pretty);
            } else lblTs.setText("Last refresh: N/A");
            lblPath.setText("Path: " + (superService.getActiveLogPath().isEmpty() ? "(not set)" : superService.getActiveLogPath()));
        };
        refreshHud.run();

        btnPrev.addActionListener(ev -> {
            if (page[0] > 0) page[0]--;
            String s = superService.viewLoginHistory(superService.getActiveLogPath(), page[0], pageSize, txtSearch.getText());
            logArea.setText(s);
            logArea.setCaretPosition(0);
            refreshHud.run();
        });
        btnNext.addActionListener(ev -> {
            page[0]++;
            String s = superService.viewLoginHistory(superService.getActiveLogPath(), page[0], pageSize, txtSearch.getText());
            if (s.contains("Showing login events") && s.contains("of 0")) { page[0]--; return; }
            logArea.setText(s);
            logArea.setCaretPosition(0);
            refreshHud.run();
        });

        txtSearch.addActionListener(ev -> {
            page[0] = 0;
            String s = superService.viewLoginHistory(superService.getActiveLogPath(), page[0], pageSize, txtSearch.getText());
            logArea.setText(s); logArea.setCaretPosition(0); refreshHud.run();
        });

        btnExport.addActionListener(ev -> {
            String content = logArea.getText();
            String dest = System.getProperty("user.home") + System.getProperty("file.separator") + "medilab_login_history.txt";
            boolean ok = superService.exportLogs(content, dest);
            if (ok) JOptionPane.showMessageDialog(this, "Exported login history to " + dest);
            else JOptionPane.showMessageDialog(this, "Export failed.");
        });

        btnRefresh.addActionListener(ev -> {
            page[0] = 0;
            String s = superService.viewLoginHistory(superService.getActiveLogPath(), page[0], pageSize, txtSearch.getText());
            logArea.setText(s); logArea.setCaretPosition(0); refreshHud.run();
        });

         btnDialog.addActionListener(ev -> {
            JDialog dlg = new JDialog(SuperAdminDashboard.this, "Login History", true);
            dlg.setSize(900, 640);
            dlg.setResizable(true);
            dlg.setLocationRelativeTo(SuperAdminDashboard.this);

            JPanel dp = new JPanel(new BorderLayout(10, 10));
            dp.setBorder(new EmptyBorder(12, 12, 12, 12));
            dp.setBackground(white);

            JTextArea dlgArea = new JTextArea();
            dlgArea.setEditable(false);
            dlgArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
            dlgArea.setBackground(Color.black);
            dlgArea.setForeground(Color.green);
            dlgArea.setMargin(new Insets(12,12,12,12));

            JScrollPane dsp = new JScrollPane(dlgArea);
            dsp.setBorder(BorderFactory.createEmptyBorder());
            dp.add(dsp, BorderLayout.CENTER);

            JPanel df = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            df.setOpaque(false);
            JButton dlgRefresh = new JButton("Refresh");
            JButton dlgCopy = new JButton("Copy");
            JButton dlgExport = new JButton("Export");
            JButton dlgClose = new JButton("Close");
            dlgRefresh.setPreferredSize(new Dimension(100, 32));
            dlgCopy.setPreferredSize(new Dimension(100, 32));
            dlgExport.setPreferredSize(new Dimension(100, 32));
            dlgClose.setPreferredSize(new Dimension(100, 32));
            styleDarkButton(dlgRefresh, UiPalette.PRIMARY);
            styleDarkButton(dlgCopy, UiPalette.SECONDARY.darker());
            styleDarkButton(dlgExport, UiPalette.PRIMARY);
            styleDarkButton(dlgClose, UiPalette.PRIMARY_DARK);
            df.add(dlgRefresh); df.add(dlgCopy); df.add(dlgExport); df.add(dlgClose);
            dp.add(df, BorderLayout.SOUTH);

            String current = superService.viewLoginHistory(superService.getActiveLogPath(), 0, 100, null);
            dlgArea.setText(current != null ? current : "> No login events");
            dlgArea.setCaretPosition(0);

            dlgRefresh.addActionListener(a -> {
                dlgRefresh.setEnabled(false);
                SwingUtilities.invokeLater(() -> {
                    try {
                        String s = superService.viewLoginHistory(superService.getActiveLogPath(), 0, 100, null);
                        dlgArea.setText(s != null ? s : "> No login events");
                        dlgArea.setCaretPosition(0);
                    } finally { dlgRefresh.setEnabled(true); }
                });
            });

            dlgCopy.addActionListener(a -> {
                String txt = dlgArea.getText();
                if (txt != null && !txt.isEmpty()) {
                    try { java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(txt), null); JOptionPane.showMessageDialog(dlg, "Copied"); }
                    catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Failed to copy: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
                }
            });

            dlgExport.addActionListener(a -> {
                String dest = System.getProperty("user.home") + System.getProperty("file.separator") + "medilab_login_history_dialog.txt";
                boolean ok = superService.exportLogs(dlgArea.getText(), dest);
                if (ok) JOptionPane.showMessageDialog(dlg, "Exported to " + dest);
                else JOptionPane.showMessageDialog(dlg, "Export failed.");
            });

            dlgClose.addActionListener(a -> dlg.dispose());

            dlg.setContentPane(dp);
            dlg.setVisible(true);
        });

        return p;
    }

      private JPanel createBackupPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(bgMain);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(white);
        card.setBorder(new EmptyBorder(50, 80, 50, 80));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.insets = new Insets(10, 0, 10, 0);

        JLabel icon = new JLabel("💾");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 72));
        card.add(icon, c);

        c.gridy++;
        JLabel lblTitle = new JLabel("Disaster Recovery");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(textDark);
        card.add(lblTitle, c);

        c.gridy++;
        JLabel lblDesc = new JLabel("<html><center>Create a full SQL dump of the database.<br>Use this for migration or emergency restoration.</center></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDesc.setForeground(textLight);
        card.add(lblDesc, c);

        c.gridy++; c.insets = new Insets(40, 0, 20, 0);
        UiPalette.FlatButton btnBackup = new UiPalette.FlatButton("PERFORM BACKUP"); // The Action Button
        btnBackup.setPreferredSize(new Dimension(250, 55));
        card.add(btnBackup, c);

        c.gridy++; c.insets = new Insets(0, 0, 0, 0);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(250, 8));
        progressBar.setForeground(accentYellow);
        card.add(progressBar, c);

        btnBackup.addActionListener(e -> {
            btnBackup.setEnabled(false);
            progressBar.setVisible(true);

            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception {
                    superService.performDatabaseBackup(); // CALLS SERVICE
                    Thread.sleep(1500);
                    return null;
                }
                @Override protected void done() {
                    progressBar.setVisible(false);
                    btnBackup.setEnabled(true);
                    JOptionPane.showMessageDialog(p, "Backup Completed Successfully.");
                }
            }.execute();
        });

        p.add(card);
        return p;
    }

     private JPanel createOverridePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(bgMain);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(white);
        card.setBorder(new EmptyBorder(50, 80, 50, 80));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.insets = new Insets(10, 0, 10, 0);

        JLabel icon = new JLabel("⚠️");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 72));
        card.add(icon, c);

        c.gridy++;
        JLabel lblTitle = new JLabel("Emergency Override");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(UiPalette.ERROR.darker());
        card.add(lblTitle, c);

        c.gridy++;
        JLabel lblDesc = new JLabel("<html><center>Force status validation on a specific Order.<br>Use only if Technician is unavailable.</center></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDesc.setForeground(textLight);
        card.add(lblDesc, c);

        c.gridy++;
        RoundedTextField txtOrderId = new RoundedTextField(20);
        txtOrderId.setPlaceholder("Enter Order ID (e.g. 101)");
        txtOrderId.setPreferredSize(new Dimension(250, 50));
        card.add(txtOrderId, c);

        c.gridy++; c.insets = new Insets(30, 0, 20, 0);
        UiPalette.FlatButton btnForce = new UiPalette.FlatButton("OVERRIDE VALIDATION"); // The Action Button
        btnForce.setBackground(UiPalette.ERROR.darker());
        btnForce.setForeground(UiPalette.WHITE);
        btnForce.setPreferredSize(new Dimension(250, 55));
        card.add(btnForce, c);

        btnForce.addActionListener(e -> {
            String input = txtOrderId.getText();
            if (input == null || input.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an Order ID.");
                return;
            }
            try {
                int orderId = Integer.parseInt(input);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to FORCE VALIDATE Order #" + orderId + "?\nThis action will be logged.",
                        "Confirm Override", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    superService.overrideValidation(orderId); // CALLS SERVICE
                    JOptionPane.showMessageDialog(this, "Order #" + orderId + " has been validated.");
                    txtOrderId.setText("");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Order ID.");
            }
        });

        p.add(card);
        return p;
    }


     private JPanel createStrictRegistrationPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setBackground(bgMain);
        p.setBorder(new EmptyBorder(30, 60, 30, 60));

        JLabel title = new JLabel("Hiring Portal");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(textDark);
        p.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(white);
        form.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(15, 20, 5, 20);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0; c.weightx = 0.5;


        RoundedTextField txtFirst = new RoundedTextField(20); txtFirst.setPlaceholder("First Name");
        RoundedTextField txtLast = new RoundedTextField(20); txtLast.setPlaceholder("Last Name");
        RoundedTextField txtUser = new RoundedTextField(20); txtUser.setPlaceholder("Username");
        RoundedPasswordField txtPass = new RoundedPasswordField(20); txtPass.setPlaceholder("Password");
        RoundedTextField txtPhone = new RoundedTextField(20); txtPhone.setPlaceholder("Phone");
        RoundedTextField txtEmail = new RoundedTextField(20); txtEmail.setPlaceholder("Email");
        RoundedTextField txtAddr = new RoundedTextField(20); txtAddr.setPlaceholder("Address");
        JComboBox<Role> cmbRole = new JComboBox<>(Role.values()); cmbRole.setBackground(white);

        addSuperInput(form, c, "First Name", txtFirst, 0, 0);
        addSuperInput(form, c, "Last Name", txtLast, 1, 0);
        addSuperInput(form, c, "Username", txtUser, 0, 1);
        addSuperInput(form, c, "Password", txtPass, 1, 1);
        addSuperInput(form, c, "Role", cmbRole, 0, 2);
        addSuperInput(form, c, "Phone", txtPhone, 1, 2);
        addSuperInput(form, c, "Email", txtEmail, 0, 3);
        addSuperInput(form, c, "Address", txtAddr, 1, 3);

        c.gridy = 4; c.gridx = 0; c.gridwidth = 2; c.insets = new Insets(40, 20, 10, 20);
        c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.CENTER;

        UiPalette.FlatButton btnSave = new UiPalette.FlatButton("Onboard Employee");
        btnSave.setPreferredSize(new Dimension(300, 55));

        btnSave.addActionListener(e -> {
            Employee emp = new Employee();
            emp.setFirstName(txtFirst.getText());
            emp.setLastName(txtLast.getText());
            emp.setUsername(txtUser.getText());
            emp.setPassword(new String(txtPass.getPassword()));
            emp.setRole((Role) cmbRole.getSelectedItem());
            emp.setPhone(txtPhone.getText());
            emp.setEmail(txtEmail.getText());
            emp.setAddress(txtAddr.getText());
            emp.setHireDate(new java.util.Date());

            if(superService.addEmployee(emp)) {
                JOptionPane.showMessageDialog(this, "Success!");
                refreshStaffTable();
                cardLayout.show(contentPanel, PANEL_STAFF);
            } else {
                JOptionPane.showMessageDialog(this, "Error saving employee.");
            }
        });

        form.add(btnSave, c);
        p.add(new JScrollPane(form), BorderLayout.CENTER);
        return p;
    }

    private void addSuperInput(JPanel p, GridBagConstraints c, String lbl, JComponent comp, int x, int y) {
        c.gridx = x; c.gridy = y;
        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setOpaque(false);
        JLabel l = new JLabel(lbl); l.setFont(new Font("Segoe UI", Font.BOLD, 14)); l.setForeground(textLight);
        wrap.add(l, BorderLayout.NORTH);
        comp.setPreferredSize(new Dimension(200, 50));
        wrap.add(comp, BorderLayout.CENTER);
        p.add(wrap, c);
    }



    private java.util.List<JTable> findTables(Container root) {
        java.util.List<JTable> out = new java.util.ArrayList<>();
        for (Component c : root.getComponents()) {
            if (c instanceof JTable) out.add((JTable)c);
            if (c instanceof Container) out.addAll(findTables((Container)c));
        }
        return out;
    }

    @Override public void openSecretaryConsole() { refreshSecretaryConsole(); cardLayout.show(contentPanel, PANEL_SECRETARY_CONSOLE); }
    @Override public JPanel getSecretaryPanel() { return secretaryPanel; }
    @Override public void refreshSecretaryConsole() { try { if (secretaryPanel instanceof SecretaryPanel) ((SecretaryPanel)secretaryPanel).refreshPatientTable(); } catch (Exception ex) { /* ignore */ } }

    @Override public void openTechnicianConsole() { refreshTechnicianConsole(); cardLayout.show(contentPanel, PANEL_TECHNICIAN_CONSOLE); }
    @Override public JPanel getTechnicianPanel() { return technicianPanel; }
    @Override public void refreshTechnicianConsole() { try { if (technicianPanel instanceof TechnicianPanel) ((TechnicianPanel)technicianPanel).refreshTodayOrders(); } catch (Exception ex) { /* ignore */ } }

    @Override public void openFinanceConsole() { refreshFinanceConsole(); cardLayout.show(contentPanel, PANEL_FINANCE_CONSOLE); }
    @Override public JPanel getFinancePanel() { return financePanel; }
    @Override public void refreshFinanceConsole() { try { if (financePanel instanceof FinancialPanel) ((FinancialPanel)financePanel).refreshStats(); } catch (Exception ex) { /* ignore */ } }

    private JPanel createUnifiedHomePanel() {
        JPanel root = new JPanel(new BorderLayout(18,18));
        root.setBackground(bgMain);
        root.setBorder(new EmptyBorder(18,18,18,18));


        JPanel topBar = new JPanel(new BorderLayout(12,0)); topBar.setOpaque(false);
        JPanel titleBox = new JPanel(new GridLayout(2,1)); titleBox.setOpaque(false);
        JLabel title = new JLabel("Super Admin"); title.setFont(new Font("Segoe UI", Font.BOLD, 28)); title.setForeground(textDark);
        JLabel subtitle = new JLabel("System overview and quick actions"); subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12)); subtitle.setForeground(textLight);
        titleBox.add(title); titleBox.add(subtitle);
        topBar.add(titleBox, BorderLayout.WEST);

        JPanel centerPlaceholder = new JPanel(); centerPlaceholder.setOpaque(false);
        topBar.add(centerPlaceholder, BorderLayout.CENTER);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); rightActions.setOpaque(false);
        UiPalette.FlatButton btnRefresh = new UiPalette.FlatButton("Refresh"); btnRefresh.setPreferredSize(new Dimension(110,36));
        JLabel avatar = new JLabel(); avatar.setPreferredSize(new Dimension(42,42)); avatar.setOpaque(false);
        try {
            String initials = "S";
            if (currentUser != null && currentUser.getFirstName() != null && !currentUser.getFirstName().isEmpty()) initials = currentUser.getFirstName().substring(0,1).toUpperCase();
            int asz = 42;
            BufferedImage img = new BufferedImage(asz, asz, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UiPalette.MEDICAL_BLUE);
            g2.fillOval(0,0,asz,asz);
            g2.setColor(UiPalette.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (asz - fm.stringWidth(initials)) / 2;
            int ty = (asz + fm.getAscent()) / 2 - 3;
            g2.drawString(initials, tx, ty);
            g2.dispose();
            avatar.setIcon(new ImageIcon(img));
        } catch (Exception ignored) { }
        rightActions.add(btnRefresh); rightActions.add(avatar);
        topBar.add(rightActions, BorderLayout.EAST);

        root.add(topBar, BorderLayout.NORTH);

        JPanel kpiRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8)); kpiRow.setOpaque(false);
        int staffCount = 0; try { java.util.List<ApplicationTier.Model.Employee> emps = adminService.getAllEmployees(); staffCount = emps != null ? emps.size() : 0; } catch (Exception ignored) {}
        int pendingResults = 0; try { pendingResults = secService.countValidatedResults(); } catch (Exception ignored) {}
        int pendingInvoices = 0; try { java.util.List<ApplicationTier.Model.Invoice> invs = fmServiceLocal.findAllInvoices(); if (invs != null) { for (ApplicationTier.Model.Invoice i : invs) if (i.getPaymentStatus() == null || !i.getPaymentStatus().toString().equalsIgnoreCase(ApplicationTier.Model.Enums.PaymentStatus.PAID.toString())) pendingInvoices++; } } catch (Exception ignored) {}
        int apptsToday = 0; try { java.util.List<ApplicationTier.Model.Appointment> all = secService.viewSchedule(); if (all != null) { java.time.LocalDate t = java.time.LocalDate.now(); for (ApplicationTier.Model.Appointment a: all) if (a.getDate()!=null && new java.sql.Date(a.getDate().getTime()).toLocalDate().equals(t)) apptsToday++; } } catch (Exception ignored) {}

        kpiRow.add(createSolidKpiCard("Appointments", apptsToday, null, UiPalette.MEDICAL_BLUE));
        kpiRow.add(createSolidKpiCard("Pending Results", pendingResults, null, UiPalette.SECONDARY));
        kpiRow.add(createSolidKpiCard("Pending Invoices", pendingInvoices, null, UiPalette.ACCENT));
        kpiRow.add(createSolidKpiCard("Staff", staffCount, null, UiPalette.TEXT));

        root.add(kpiRow, BorderLayout.CENTER);

        JPanel main = new JPanel(new BorderLayout(12,12)); main.setOpaque(false);

        JPanel charts = new JPanel(new GridLayout(2,1,12,12)); charts.setOpaque(false);
        RoundedPanel pieCard = new RoundedPanel(12, UiPalette.WHITE); pieCard.setLayout(new BorderLayout()); pieCard.setBorder(new EmptyBorder(12,12,12,12));
        pieCard.add(new JLabel("Role Distribution", SwingConstants.LEFT), BorderLayout.NORTH);
        PieChartPanel pie = new PieChartPanel(); pieCard.add(pie, BorderLayout.CENTER);
        charts.add(pieCard);

        RoundedPanel barCard = new RoundedPanel(12, UiPalette.WHITE); barCard.setLayout(new BorderLayout()); barCard.setBorder(new EmptyBorder(12,12,12,12));
        barCard.add(new JLabel("Invoices (Paid vs Pending)", SwingConstants.LEFT), BorderLayout.NORTH);
        InvoiceBarChartPanel bar = new InvoiceBarChartPanel(); barCard.add(bar, BorderLayout.CENTER);
        charts.add(barCard);

        main.add(charts, BorderLayout.WEST);

        JPanel previews = new JPanel(); previews.setLayout(new BoxLayout(previews, BoxLayout.Y_AXIS)); previews.setOpaque(false);
        previews.add(Box.createVerticalStrut(6));

        RoundedPanel apptCard = new RoundedPanel(12, UiPalette.WHITE); apptCard.setLayout(new BorderLayout()); apptCard.setBorder(new EmptyBorder(12,12,12,12));
        JPanel apptHead = new JPanel(new BorderLayout()); apptHead.setOpaque(false); apptHead.add(new JLabel("Upcoming Appointments"), BorderLayout.WEST);
        UiPalette.FlatButton vAppts = new UiPalette.FlatButton("Open"); vAppts.setPreferredSize(new Dimension(80,28)); apptHead.add(vAppts, BorderLayout.EAST);
        apptCard.add(apptHead, BorderLayout.NORTH);
        javax.swing.table.DefaultTableModel apptModel = new javax.swing.table.DefaultTableModel(new Object[]{"Appt ID","Patient","Date"}, 0){ public boolean isCellEditable(int r,int c){return false;} };
        JTable apptTable = new JTable(apptModel); apptTable.setRowHeight(34); apptCard.add(new JScrollPane(apptTable), BorderLayout.CENTER);
        previews.add(apptCard);
        previews.add(Box.createVerticalStrut(12));

        RoundedPanel ordCard = new RoundedPanel(12, UiPalette.WHITE); ordCard.setLayout(new BorderLayout()); ordCard.setBorder(new EmptyBorder(12,12,12,12));
        JPanel ordHead = new JPanel(new BorderLayout()); ordHead.setOpaque(false); ordHead.add(new JLabel("Pending Orders"), BorderLayout.WEST);
        UiPalette.FlatButton vOrders = new UiPalette.FlatButton("Open"); vOrders.setPreferredSize(new Dimension(80,28)); ordHead.add(vOrders, BorderLayout.EAST);
        ordCard.add(ordHead, BorderLayout.NORTH);
        javax.swing.table.DefaultTableModel ordModel = new javax.swing.table.DefaultTableModel(new Object[]{"Order ID","Patient","Status"},0){ public boolean isCellEditable(int r,int c){return false;} };
        JTable ordTable = new JTable(ordModel); ordTable.setRowHeight(34); ordCard.add(new JScrollPane(ordTable), BorderLayout.CENTER);
        previews.add(ordCard);
        previews.add(Box.createVerticalStrut(12));

        RoundedPanel invCard = new RoundedPanel(12, UiPalette.WHITE); invCard.setLayout(new BorderLayout()); invCard.setBorder(new EmptyBorder(12,12,12,12));
        JPanel invHead = new JPanel(new BorderLayout()); invHead.setOpaque(false); invHead.add(new JLabel("Recent Invoices"), BorderLayout.WEST);
        UiPalette.FlatButton vInv = new UiPalette.FlatButton("Open"); vInv.setPreferredSize(new Dimension(80,28)); invHead.add(vInv, BorderLayout.EAST);
        invCard.add(invHead, BorderLayout.NORTH);
        javax.swing.table.DefaultTableModel invModel = new javax.swing.table.DefaultTableModel(new Object[]{"Inv ID","Order","Amt","Status"},0){ public boolean isCellEditable(int r,int c){return false;} };
        JTable invTable = new JTable(invModel); invTable.setRowHeight(34); invCard.add(new JScrollPane(invTable), BorderLayout.CENTER);
        previews.add(invCard);
        previews.add(Box.createVerticalStrut(6));

        main.add(previews, BorderLayout.CENTER);

        root.add(main, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> {
             try {
                refreshStaffTable();


                apptModel.setRowCount(0);
                java.util.List<ApplicationTier.Model.Appointment> appts = secService.viewSchedule();
                if (appts != null) {
                    int c = 0; java.time.LocalDate today = java.time.LocalDate.now();
                    for (ApplicationTier.Model.Appointment a : appts) {
                        if (c++ >= 6) break;
                        apptModel.addRow(new Object[]{a.getAppointmentId(), String.valueOf(a.getPatientId()), a.getDate()});
                    }
                }


                ordModel.setRowCount(0);
                java.util.List<ApplicationTier.Model.TestOrder> orders = techServiceLocal.getPendingOrders();
                if (orders != null) {
                    int c = 0;
                    for (ApplicationTier.Model.TestOrder o : orders) {
                        if (c++ >= 6) break;
                        ordModel.addRow(new Object[]{o.getOrderId(), o.getPatientId(), o.getStatus()});
                    }
                }


                invModel.setRowCount(0);
                java.util.List<ApplicationTier.Model.Invoice> invoices = fmServiceLocal.findAllInvoices();
                if (invoices != null) {
                    int c = 0;
                    for (ApplicationTier.Model.Invoice inv : invoices) {
                        if (c++ >= 6) break;
                        invModel.addRow(new Object[]{inv.getInvoiceId(), inv.getOrderId(), String.format("%.2f", inv.getTotalAmount()), inv.getPaymentStatus()});
                    }
                }

                try { java.util.List<ApplicationTier.Model.Employee> emps = adminService.getAllEmployees(); } catch (Exception ignored) {}

                pie.repaint();
                bar.setInvoices(invoices != null ? invoices : java.util.Collections.emptyList()); bar.repaint();
             } catch (Exception ex) {
                 LOGGER.log(java.util.logging.Level.WARNING, "Failed to refresh unified home", ex);
                 JOptionPane.showMessageDialog(this, "Failed to refresh unified home: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             }
         });

        vAppts.addActionListener(ae -> { try { refreshSecretaryConsole(); cardLayout.show(contentPanel, PANEL_SECRETARY_CONSOLE); } catch (Exception ignored) {} });
        vOrders.addActionListener(ae -> { try { refreshTechnicianConsole(); cardLayout.show(contentPanel, PANEL_TECHNICIAN_CONSOLE); } catch (Exception ignored) {} });
        vInv.addActionListener(ae -> { try { refreshFinanceConsole(); cardLayout.show(contentPanel, PANEL_FINANCE_CONSOLE); } catch (Exception ignored) {} });

        apptTable.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { if (e.getClickCount()==2) { vAppts.doClick(); } } });
        ordTable.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { if (e.getClickCount()==2) { vOrders.doClick(); } } });
        invTable.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { if (e.getClickCount()==2) { vInv.doClick(); } } });

        btnRefresh.doClick();

        return root;
    }

      private JPanel createAuditPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(bgMain);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Login Audit Viewer");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26)); title.setForeground(textDark);
        p.add(title, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)); controls.setOpaque(false);
        JTextField txtUser = new JTextField(12);
        JTextField txtFrom = new JTextField(10); txtFrom.setToolTipText("YYYY-MM-DD");
        JTextField txtTo = new JTextField(10); txtTo.setToolTipText("YYYY-MM-DD");
        JComboBox<String> cmbSuccess = new JComboBox<>(new String[]{"Any","Success","Failure"});
        JButton btnSearch = new JButton("Search");
        JButton btnPrev = new JButton("Recent");
        JButton btnNext = new JButton("Prev");

        styleDarkButton(btnSearch, UiPalette.PRIMARY);
        styleDarkButton(btnPrev, UiPalette.PRIMARY_DARK);
        styleDarkButton(btnNext, UiPalette.PRIMARY_DARK);

        controls.add(new JLabel("User:")); controls.add(txtUser);
        controls.add(new JLabel("From:")); controls.add(txtFrom);
        controls.add(new JLabel("To:")); controls.add(txtTo);
        controls.add(new JLabel("Result:")); controls.add(cmbSuccess);
        controls.add(btnSearch); controls.add(btnPrev); controls.add(btnNext);

        JTextArea area = new JTextArea(); area.setFont(new Font("Monospaced", Font.PLAIN, 12)); area.setEditable(false);
        JScrollPane sp = new JScrollPane(area);

        JPanel center = new JPanel(new BorderLayout(6,6)); center.setOpaque(false);
        center.add(controls, BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);

        p.add(center, BorderLayout.CENTER);

        final int[] page = {0}; final int pageSize = 50;
        Runnable loadPage = () -> {
            try {
                java.util.Date from = null, to = null;
                try { if (!txtFrom.getText().trim().isEmpty()) from = java.sql.Date.valueOf(txtFrom.getText().trim()); } catch (Exception ignored) {}
                try { if (!txtTo.getText().trim().isEmpty()) to = java.sql.Date.valueOf(txtTo.getText().trim()); } catch (Exception ignored) {}
                Boolean succ = null; String sel = (String)cmbSuccess.getSelectedItem(); if ("Success".equals(sel)) succ = true; else if ("Failure".equals(sel)) succ = false;
                String username = txtUser.getText().trim().isEmpty() ? null : txtUser.getText().trim();
                DAO.LoginAuditDAO dao = new DAO.LoginAuditDAO();
                java.util.List<ApplicationTier.Model.LoginAudit> rows = dao.queryWithFilters(from, to, succ, page[0]*pageSize, pageSize, username);
                StringBuilder sb = new StringBuilder();
                for (ApplicationTier.Model.LoginAudit r : rows) {
                    sb.append(String.format("%s | %s | %s | %s\n", r.getTimestamp(), r.getUsername(), r.isSuccess()?"SUCCESS":"FAIL", r.getMessage()));
                }
                area.setText(sb.toString());
            } catch (Exception ex) { area.setText("Failed to load audit: " + ex.getMessage()); }
        };

        btnSearch.addActionListener(e -> { page[0] = 0; loadPage.run(); });
        btnPrev.addActionListener(e -> { if (page[0] > 0) { page[0]--; loadPage.run(); } });
        btnNext.addActionListener(e -> { page[0]++; loadPage.run(); });

        loadPage.run();
        return p;
    }

    static class InvoiceBarChartPanel extends JPanel {
        private java.util.List<ApplicationTier.Model.Invoice> invoices = java.util.Collections.emptyList();
        private static final Color[] SHARED_PIE_COLORS = new Color[]{
            new Color(0xE4C1F9),
            new Color(0xA9DEF9),
            new Color(0xD0F4DE),
            new Color(0xFCF6BD),
            new Color(0xFF99C8)
        };

        public InvoiceBarChartPanel() {
            setPreferredSize(new Dimension(300, 200));
            setBackground(UiPalette.PANEL.brighter());
        }

        public void setInvoices(java.util.List<ApplicationTier.Model.Invoice> invoices) {
            this.invoices = invoices != null ? invoices : java.util.Collections.emptyList();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            if (width <= 0 || height <= 0) { g2.dispose(); return; }

            int paidCount = 0;
            int pendingCount = 0;
            for (ApplicationTier.Model.Invoice inv : invoices) {
                if (inv.getPaymentStatus() != null && inv.getPaymentStatus() == ApplicationTier.Model.Enums.PaymentStatus.PAID)
                    paidCount++;
                else
                    pendingCount++;
            }

            int total = paidCount + pendingCount;

            if (total == 0) {
                g2.setColor(UiPalette.TEXT_LIGHT);
                String txt = "No invoices";
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(txt);
                g2.drawString(txt, (width - tw) / 2, height / 2);
                g2.dispose();
                return;
            }

            int padding = 20;
            int barAreaW = width - padding * 2;
            int barW = Math.max(36, (barAreaW - 40) / 2);
            int maxBarHeight = height - 90;

            double paidRatio = (double) paidCount / (double) total;
            double pendingRatio = (double) pendingCount / (double) total;
            int paidBarHeight = (int) Math.round(paidRatio * maxBarHeight);
            int pendingBarHeight = (int) Math.round(pendingRatio * maxBarHeight);

            int leftX = padding + 10;
            int rightX = leftX + barW + 30;

            g2.setColor(UiPalette.withAlpha(UiPalette.TEXT_LIGHT, 30));
            g2.fillRoundRect(padding, height - 42, barAreaW, 6, 6, 6);

            Color paidColor = SHARED_PIE_COLORS[1];
            Color pendingColor = SHARED_PIE_COLORS[0];


            g2.setColor(paidColor);
            int paidY = height - 46 - paidBarHeight;
            g2.fillRoundRect(leftX, paidY, barW, paidBarHeight, 10, 10);

            g2.setColor(pendingColor);
            int pendingY = height - 46 - pendingBarHeight;
            g2.fillRoundRect(rightX, pendingY, barW, pendingBarHeight, 10, 10);

             g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.06f));
            g2.setColor(UiPalette.WHITE);
            g2.fillRoundRect(leftX, paidY, barW, Math.max(6, paidBarHeight), 10, 10);
            g2.fillRoundRect(rightX, pendingY, barW, Math.max(6, pendingBarHeight), 10, 10);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2.setColor(UiPalette.TEXT);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            String paidLabel = String.valueOf(paidCount);
            String pendingLabel = String.valueOf(pendingCount);
            FontMetrics fm = g2.getFontMetrics();
            int plw = fm.stringWidth(paidLabel);
            g2.drawString(paidLabel, leftX + (barW - plw) / 2, Math.max(18, paidY - 10));
            int plw2 = fm.stringWidth(pendingLabel);
            g2.drawString(pendingLabel, rightX + (barW - plw2) / 2, Math.max(18, pendingY - 10));

            g2.setColor(UiPalette.TEXT_LIGHT);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            String paidText = "Paid";
            String pendingText = "Pending";
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(paidText, leftX + (barW - fm2.stringWidth(paidText)) / 2, height - 14);
            g2.drawString(pendingText, rightX + (barW - fm2.stringWidth(pendingText)) / 2, height - 14);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(UiPalette.WHITE);
            String paidPct = String.format("%.0f%%", paidRatio * 100.0);
            String pendingPct = String.format("%.0f%%", pendingRatio * 100.0);
            int badgeW = 48, badgeH = 20;
            int bx = leftX + (barW - badgeW) / 2;
            int by = paidY + Math.max(4, (paidBarHeight - badgeH) / 2);
            g2.setColor(darken(paidColor, 0.9f));
            g2.fillRoundRect(bx, by, badgeW, badgeH, 10, 10);
            g2.setColor(UiPalette.WHITE);
            g2.drawString(paidPct, bx + (badgeW - g2.getFontMetrics().stringWidth(paidPct)) / 2, by + badgeH - 6);

            int bx2 = rightX + (barW - badgeW) / 2;
            int by2 = pendingY + Math.max(4, (pendingBarHeight - badgeH) / 2);
            g2.setColor(darken(pendingColor, 0.9f));
            g2.fillRoundRect(bx2, by2, badgeW, badgeH, 10, 10);
            g2.setColor(UiPalette.WHITE);
            g2.drawString(pendingPct, bx2 + (badgeW - g2.getFontMetrics().stringWidth(pendingPct)) / 2, by2 + badgeH - 6);

            g2.dispose();
        }

         private Color darken(Color c, float factor) {
            if (c == null) return UiPalette.TEXT;
            int r = Math.max(0, Math.min(255, (int) (c.getRed() * factor)));
            int g = Math.max(0, Math.min(255, (int) (c.getGreen() * factor)));
            int b = Math.max(0, Math.min(255, (int) (c.getBlue() * factor)));
            return new Color(r, g, b);
        }
    }
}

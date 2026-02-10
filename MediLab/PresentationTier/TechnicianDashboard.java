package PresentationTier;

import ApplicationTier.Model.*;
import ApplicationTier.TechnicianService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"unused","squid:UnusedPrivateField"})
public class TechnicianDashboard extends JFrame {

    private final Employee currentUser;
    private final TechnicianService techService;
    private static final Logger LOGGER = Logger.getLogger(TechnicianDashboard.class.getName());

    private JPanel contentPanel;
    private CardLayout cardLayout;

    private final Color bgMain = UiPalette.BG;
    private final Color textDark = UiPalette.TEXT;
    private final Color textLight = UiPalette.TEXT_LIGHT;
    private final Color white = UiPalette.WHITE;

    private static final String PANEL_HOME = "HOME";
    private static final String PANEL_WORKLIST = "WORKLIST";
    private static final String PANEL_BENCH = "BENCH";
    private static final String PANEL_HISTORY = "HISTORY";
    private static final String PANEL_CATALOG = "CATALOG";
    public static final String KEY_HOME = PANEL_HOME;
    public static final String KEY_WORKLIST = PANEL_WORKLIST;
    public static final String KEY_BENCH = PANEL_BENCH;
    public static final String KEY_HISTORY = PANEL_HISTORY;
    public static final String KEY_CATALOG = PANEL_CATALOG;

    private DefaultTableModel todayModel;
    private JTable todayTable;
    private JLabel ordersCountLabel;
    private JLabel pendingResultsLabel;
    private JLabel validatedResultsLabel;

    private DefaultTableModel worklistModel;
    private JTable worklistTable;

    private DefaultTableModel benchModel;
    private JTable benchTable;

    private DefaultTableModel historyModel;

    private DefaultTableModel catalogModel;

    private final java.util.List<MenuButton> menuButtons = new java.util.ArrayList<>();

    public TechnicianDashboard(Employee user) {
        this.currentUser = user;
        this.techService = new TechnicianService();
        initUI();
    }

    public JPanel getMainPanel() { return contentPanel; }

    public void showSubPanel(String key) {
        if (key == null) return;
        try {
            switch (key) {
                case KEY_HOME: cardLayout.show(contentPanel, PANEL_HOME); break;
                case KEY_WORKLIST: cardLayout.show(contentPanel, PANEL_WORKLIST); break;
                case KEY_BENCH: cardLayout.show(contentPanel, PANEL_BENCH); break;
                case KEY_HISTORY: cardLayout.show(contentPanel, PANEL_HISTORY); break;
                case KEY_CATALOG: cardLayout.show(contentPanel, PANEL_CATALOG); break;
                default: break;
            }
        } catch (Exception ignored) {}
    }

    private void initUI() {
        setTitle("MediLab - Lab | " + (currentUser != null ? currentUser.getFirstName() : "Technician"));
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel sidebar = new JPanel();
        sidebar.setBackground(white);
        sidebar.setPreferredSize(new Dimension(260, getHeight()));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(28, 20, 28, 20));

        JLabel brand = new JLabel("MediLab"); brand.setFont(new Font("Segoe UI", Font.BOLD, 28)); brand.setForeground(UiPalette.MEDICAL_BLUE); brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(6));
        JLabel role = new JLabel("LAB TECHNICIAN"); role.setFont(new Font("Segoe UI", Font.BOLD, 11)); role.setForeground(UiPalette.TEXT_LIGHT); role.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(role);
        sidebar.add(Box.createVerticalStrut(20));

        JPanel navPanel = new JPanel(); navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS)); navPanel.setOpaque(false); navPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        addSidebarButtons(navPanel);
        sidebar.add(navPanel);
        sidebar.add(Box.createVerticalGlue());

        MenuButton btnProfile = createNavButton("My Profile", null); btnProfile.addActionListener(e -> openProfile()); sidebar.add(btnProfile);
        MenuButton btnLogout = createNavButton("Logout", null); btnLogout.addActionListener(e -> { dispose(); new LoginPage().setVisible(true); }); btnLogout.setForeground(UiPalette.ERROR); sidebar.add(btnLogout);

        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(bgMain);
        contentPanel.setBorder(new EmptyBorder(22,22,22,22));

        contentPanel.add(createHomePanel(), PANEL_HOME);
        contentPanel.add(createWorklistPanel(), PANEL_WORKLIST);
        contentPanel.add(createLabBenchPanel(), PANEL_BENCH);
        contentPanel.add(createHistoryPanel(), PANEL_HISTORY);
        contentPanel.add(createCatalogPanel(), PANEL_CATALOG);

        add(contentPanel, BorderLayout.CENTER);

        refreshTodayOrders();
        cardLayout.show(contentPanel, PANEL_HOME);
    }

    private JPanel createHomePanel() {
        JPanel p = new JPanel(new BorderLayout(16,16)); p.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JPanel titleWrap = new JPanel(new GridLayout(2,1)); titleWrap.setOpaque(false);
        JLabel title = new JLabel("Good Morning, " + (currentUser != null ? currentUser.getFirstName() : "")); title.setFont(new Font("Segoe UI", Font.BOLD, 24)); title.setForeground(textDark);
        JLabel subtitle = new JLabel("Today's Appointments"); subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13)); subtitle.setForeground(textLight);
        titleWrap.add(title); titleWrap.add(subtitle); header.add(titleWrap, BorderLayout.WEST);

        JPanel hdrActions = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); hdrActions.setOpaque(false);
        JTextField search = new JTextField(); search.setPreferredSize(new Dimension(300,34));
        UiPalette.FlatButton refresh = new UiPalette.FlatButton("Refresh"); refresh.setPreferredSize(new Dimension(110,34)); hdrActions.add(search); hdrActions.add(refresh);
        header.add(hdrActions, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1,3,12,12)); stats.setOpaque(false);
        ordersCountLabel = new JLabel("0", SwingConstants.CENTER); ordersCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pendingResultsLabel = new JLabel("0", SwingConstants.CENTER); pendingResultsLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        validatedResultsLabel = new JLabel("0", SwingConstants.CENTER); validatedResultsLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        stats.add(createStatPanel("Appointments Today", ordersCountLabel, UiPalette.MEDICAL_BLUE));
        stats.add(createStatPanel("Pending Results", pendingResultsLabel, UiPalette.SECONDARY));
        stats.add(createStatPanel("Validated Results", validatedResultsLabel, UiPalette.MEDICAL_BLUE.darker()));

        JPanel center = new JPanel(new BorderLayout(12,12)); center.setOpaque(false); center.add(stats, BorderLayout.NORTH);

        String[] cols = {"Appt ID","Order ID","Patient ID","Date","Reason","Status"};
        todayModel = new DefaultTableModel(cols,0) { public boolean isCellEditable(int r,int c){ return c == 5; } };

        JTable tt = new JTable(todayModel) {
            @Override public String getToolTipText(MouseEvent e) {
                Point p = e.getPoint(); int row = rowAtPoint(p); int col = columnAtPoint(p);
                if (row < 0 || col < 0) return super.getToolTipText(e);
                int mr = convertRowIndexToModel(row);
                try {
                    if (col == 1) {
                        Object oobj = todayModel.getValueAt(mr, 1);
                        if (oobj instanceof Integer) {
                            int oid = (Integer) oobj;
                            TestOrder ord = techService.getOrderById(oid);
                            if (ord == null) return "Order not found";
                            String details = "Order #" + ord.getOrderId() + " — Status: " + ord.getStatus();
                            return details;
                        }
                    }
                    Object apptObj = todayModel.getValueAt(mr, 0);
                    if (apptObj instanceof Integer) {
                        int apptId = (Integer) apptObj;
                        List<TestOrder> orders = techService.getOrdersByAppointment(apptId);
                        if (orders == null || orders.isEmpty()) return "No orders linked to this appointment";
                        StringBuilder sb = new StringBuilder(); boolean first=true;
                        for (TestOrder o : orders) { if (!first) sb.append(", "); sb.append("#").append(o.getOrderId()); if (o.getStatus()!=null) sb.append(" (").append(o.getStatus()).append(")"); first=false; }
                        String html = sb.toString().replaceAll(", ", ",<br>"); return "<html>"+html+"</html>";
                    }
                } catch (Exception ex) { return super.getToolTipText(e); }
                return super.getToolTipText(e);
            }
        };

        todayTable = styleTable(tt);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(todayModel);
        todayTable.setRowSorter(sorter);
        search.addKeyListener(new KeyAdapter(){ public void keyReleased(KeyEvent e){ String t = search.getText(); if (t==null||t.trim().isEmpty()) sorter.setRowFilter(null); else sorter.setRowFilter(RowFilter.regexFilter("(?i)"+t)); }});

        UiPalette.FlatButton actionsBtn = new UiPalette.FlatButton("Actions"); actionsBtn.setPreferredSize(new Dimension(110,34));
        JPopupMenu actionsMenu = new JPopupMenu();
        JMenuItem miOpen = new JMenuItem("Open Order");
        JMenuItem miRecord = new JMenuItem("Open Order (Record)");
        JMenuItem miMarkCollected = new JMenuItem("Mark Sample Collected");
        JMenuItem miMarkComplete = new JMenuItem("Mark Completed");
        actionsMenu.add(miOpen); actionsMenu.add(miRecord); actionsMenu.addSeparator(); actionsMenu.add(miMarkCollected); actionsMenu.add(miMarkComplete);
        actionsBtn.addActionListener(ae -> { actionsMenu.show(actionsBtn, 0, actionsBtn.getHeight()); });
        hdrActions.add(Box.createHorizontalStrut(8)); hdrActions.add(actionsBtn);

        java.util.function.Supplier<Integer> resolveSelectedOrderId = () -> {
            int r = todayTable.getSelectedRow(); if (r == -1) return null;
            int mr = todayTable.convertRowIndexToModel(r);
            try { Object o = todayModel.getValueAt(mr, 1); if (o instanceof Integer && ((Integer)o) > 0) return (Integer)o; } catch (Exception ignored) {}
            try { int apptId = (int) todayModel.getValueAt(mr, 0); java.util.List<TestOrder> orders = techService.getOrdersByAppointment(apptId); if (orders != null && !orders.isEmpty()) return orders.get(0).getOrderId(); } catch (Exception ignored) {}
            return null;
        };

        miOpen.addActionListener(ev -> {
            Integer oid = resolveSelectedOrderId.get(); if (oid == null) { JOptionPane.showMessageDialog(this, "Select an appointment with an order first."); return; }
            viewOrderResultsDialog(oid);
        });

        miRecord.addActionListener(ev -> {
            Integer oid = resolveSelectedOrderId.get(); if (oid == null) { JOptionPane.showMessageDialog(this, "Select an appointment with an order first."); return; }
            viewOrderResultsDialog(oid);
        });

        miMarkCollected.addActionListener(ev -> {
            Integer oid = resolveSelectedOrderId.get(); if (oid == null) { JOptionPane.showMessageDialog(this, "Select an appointment with an order first."); return; }
            boolean ok = techService.updateOrderStatus(oid, ApplicationTier.Model.Enums.TestOrderStatus.SAMPLE_COLLECTED);
            JOptionPane.showMessageDialog(this, ok?"Marked SAMPLE_COLLECTED":"Failed to update status"); refreshWorklistTable(); refreshTodayOrders();
        });

        miMarkComplete.addActionListener(ev -> {
            Integer oid = resolveSelectedOrderId.get(); if (oid == null) { JOptionPane.showMessageDialog(this, "Select an appointment with an order first."); return; }
            boolean ok = techService.updateOrderStatus(oid, ApplicationTier.Model.Enums.TestOrderStatus.COMPLETED);
            JOptionPane.showMessageDialog(this, ok?"Marked COMPLETED":"Failed to update status"); refreshWorklistTable(); refreshTodayOrders();
        });

       class StatusCellRenderer extends DefaultTableCellRenderer {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                int mr = table.convertRowIndexToModel(row);
                Object o = todayModel.getValueAt(mr, 1);
                if (o instanceof Integer && ((Integer)o) > 0) {
                    int oid = (Integer) o;
                    TestOrder ord = techService.getOrderById(oid);
                    String s = (ord != null && ord.getStatus() != null) ? ord.getStatus().toString() : "N/A";
                    setText(s);
                    setForeground(UiPalette.TEXT);
                } else {
                    setText(value != null ? value.toString() : "");
                    setForeground(UiPalette.TEXT_LIGHT);
                }
                return this;
            }
        }

        JComboBox<String> statusCombo = new JComboBox<>();
        for (ApplicationTier.Model.Enums.TestOrderStatus s : ApplicationTier.Model.Enums.TestOrderStatus.values()) statusCombo.addItem(s.toString());

        class ConfirmingStatusEditor extends DefaultCellEditor {
            private boolean programmatic = false;
            private String previous = null;

            public ConfirmingStatusEditor(JComboBox<String> combo) {
                super(combo);
                combo.addItemListener(e -> {
                    if (e.getStateChange() != ItemEvent.SELECTED) return;
                    if (programmatic) return;
                    String newVal = (String) e.getItem();

                    int editingRow = todayTable.getEditingRow();
                    int modelRow = -1;
                    if (editingRow != -1) modelRow = todayTable.convertRowIndexToModel(editingRow);

                    Integer orderId = null;
                    Integer apptId = null;
                    if (modelRow != -1) {
                        try { Object o = todayModel.getValueAt(modelRow, 1); if (o instanceof Integer) orderId = (Integer) o; } catch (Exception ignore) {}
                        try { Object a = todayModel.getValueAt(modelRow, 0); if (a instanceof Integer) apptId = (Integer) a; } catch (Exception ignore) {}
                    }

                    String targetDesc = orderId != null ? "Order #" + orderId : (apptId != null ? "Appointment #" + apptId : "Selected row");
                    String msg = "Change status for " + targetDesc + " from '" + previous + "' to '" + newVal + "'?";
                    int ans = JOptionPane.showConfirmDialog(TechnicianDashboard.this, msg, "Confirm status change", JOptionPane.YES_NO_OPTION);
                    if (ans != JOptionPane.YES_OPTION) {
                        programmatic = true;
                        ((JComboBox)getComponent()).setSelectedItem(previous);
                        programmatic = false;
                        return;
                    }

                    if (orderId != null) {
                        try {
                            boolean ok = techService.updateOrderStatus(orderId, ApplicationTier.Model.Enums.TestOrderStatus.valueOf(newVal));
                            if (!ok) JOptionPane.showMessageDialog(TechnicianDashboard.this, "Failed to update order status.");
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(TechnicianDashboard.this, "Invalid status selected.");
                        }
                        refreshWorklistTable();
                        refreshTodayOrders();
                    } else if (apptId != null) {
                        try {
                            ApplicationTier.SecretaryService sec = new ApplicationTier.SecretaryService();
                            boolean ok = sec.updateAppointmentStatus(apptId, ApplicationTier.Model.Enums.AppointmentStatus.valueOf(newVal));
                            if (!ok) JOptionPane.showMessageDialog(TechnicianDashboard.this, "Failed to update appointment status.");
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(TechnicianDashboard.this, "Failed to update appointment status: " + ex.getMessage());
                        }
                        refreshTodayOrders();
                    }

                    if (todayTable.isEditing()) stopCellEditing();
                });
            }

            @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                int mr = table.convertRowIndexToModel(row);
                Object o = todayModel.getValueAt(mr, 1);
                programmatic = true;
                if (o instanceof Integer) {
                    int oid = (Integer)o;
                    TestOrder ord = techService.getOrderById(oid);
                    previous = (ord != null && ord.getStatus() != null) ? ord.getStatus().toString() : ApplicationTier.Model.Enums.TestOrderStatus.CREATED.toString();
                    ((JComboBox)getComponent()).setSelectedItem(previous);
                } else {
                    Object appStatus = todayModel.getValueAt(mr, 5);
                    previous = appStatus != null ? appStatus.toString() : "";
                    ((JComboBox)getComponent()).setSelectedItem(previous);
                }
                programmatic = false;
                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }
        }

        ConfirmingStatusEditor statusEditor = new ConfirmingStatusEditor(statusCombo);
        todayTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
        todayTable.getColumnModel().getColumn(5).setCellEditor(statusEditor);

        JScrollPane sp = new JScrollPane(todayTable); sp.setBorder(BorderFactory.createEmptyBorder());

        JPanel card = new JPanel(new BorderLayout()); card.setBackground(white); card.setBorder(new EmptyBorder(12,12,12,12)); card.add(sp, BorderLayout.CENTER);
        center.add(card, BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);

        refresh.addActionListener(e -> refreshTodayOrders());

        return p;
    }

    private JPanel createWorklistPanel() {
        JPanel p = new JPanel(new BorderLayout(12,12)); p.setOpaque(false);
        JLabel title = new JLabel("Pending Orders"); title.setFont(new Font("Segoe UI", Font.BOLD, 22)); title.setForeground(textDark); p.add(title, BorderLayout.NORTH);

        String[] cols = {"Order ID","Patient ID","Date Ordered","Status","Technician"};
        worklistModel = new DefaultTableModel(cols,0) { public boolean isCellEditable(int r,int c){return false;} };
        worklistTable = styleTable(new JTable(worklistModel));
        worklistTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        javax.swing.table.TableColumnModel tcm = worklistTable.getColumnModel(); if (tcm.getColumnCount()>=5) { tcm.getColumn(0).setPreferredWidth(80); tcm.getColumn(1).setPreferredWidth(80); tcm.getColumn(2).setPreferredWidth(180); tcm.getColumn(3).setPreferredWidth(140); tcm.getColumn(4).setPreferredWidth(160); }

        JScrollPane sp = new JScrollPane(worklistTable); sp.setBorder(BorderFactory.createEmptyBorder()); p.add(sp, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false);
        UiPalette.FlatButton btnRefresh = new UiPalette.FlatButton("Refresh"); btnRefresh.addActionListener(e -> refreshWorklistTable()); actions.add(btnRefresh);
        UiPalette.FlatButton btnProcess = new UiPalette.FlatButton("Process Selected"); UiPalette.FlatButton btnCreateRes = new UiPalette.FlatButton("Create Results");
        actions.add(btnCreateRes); actions.add(btnProcess);

        btnCreateRes.addActionListener(e -> {
            int[] sel = worklistTable.getSelectedRows(); if (sel==null||sel.length==0){ JOptionPane.showMessageDialog(this, "Select one or more orders first."); return; }
            java.util.List<Integer> orderIds = new java.util.ArrayList<>(); for (int r: sel) orderIds.add((Integer) worklistModel.getValueAt(worklistTable.convertRowIndexToModel(r),0));
            java.util.List<TestType> catalog = techService.getTestCatalog(); if (catalog==null||catalog.isEmpty()){ JOptionPane.showMessageDialog(this, "No test catalog available."); return; }
            JDialog dlg = new JDialog(this, "Choose Test Types", true); dlg.setSize(520,560); dlg.setLocationRelativeTo(this);
            JPanel cp = new JPanel(new BorderLayout(8,8)); cp.setBorder(new EmptyBorder(12,12,12,12)); cp.setBackground(white);
            JPanel listPanel = new JPanel(); listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS)); listPanel.setOpaque(false);
            java.util.List<JCheckBox> boxes = new java.util.ArrayList<>();
            for (TestType t : catalog) { JCheckBox cb = new JCheckBox("#"+t.getTestTypeId()+" • "+t.getName()+" ("+t.getPrice()+")"); cb.putClientProperty("tt", t); cb.setOpaque(false); boxes.add(cb); listPanel.add(cb); }
            JScrollPane spSel = new JScrollPane(listPanel); spSel.setBorder(BorderFactory.createEmptyBorder()); cp.add(new JLabel("Select one or more test types to add as results:"), BorderLayout.NORTH); cp.add(spSel, BorderLayout.CENTER);
            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT)); foot.setOpaque(false); UiPalette.FlatButton btnCancel = new UiPalette.FlatButton("Cancel"); UiPalette.FlatButton btnOk = new UiPalette.FlatButton("Create"); foot.add(btnCancel); foot.add(btnOk); cp.add(foot, BorderLayout.SOUTH);
            btnCancel.addActionListener(ae -> dlg.dispose()); btnOk.addActionListener(ae -> { java.util.List<TestType> selected = new java.util.ArrayList<>(); for (JCheckBox cb : boxes) if (cb.isSelected()) selected.add((TestType)cb.getClientProperty("tt")); if (selected.isEmpty()){ JOptionPane.showMessageDialog(dlg, "Pick at least one test type."); return; } int success=0; for (Integer oid: orderIds) { boolean ok = techService.createResultsForOrder(oid, currentUser.getEmployeeId(), selected); if (ok) success++; } JOptionPane.showMessageDialog(dlg, "Created results for "+success+" order(s)."); dlg.dispose(); refreshWorklistTable(); refreshBenchTable(); refreshHistoryTable(); refreshTodayOrders(); }); dlg.add(cp); dlg.setVisible(true);
        });

        btnProcess.addActionListener(e -> { int[] sel = worklistTable.getSelectedRows(); if (sel==null||sel.length==0){ JOptionPane.showMessageDialog(this, "Select one or more orders to process."); return; } for (int r: sel) { int modelRow = worklistTable.convertRowIndexToModel(r); Integer orderId = (Integer) worklistModel.getValueAt(modelRow,0); if (orderId!=null) viewOrderResultsDialog(orderId); } refreshWorklistTable(); refreshBenchTable(); refreshHistoryTable(); refreshTodayOrders(); });

        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    public void refreshWorklistTable() {
        worklistModel.setRowCount(0);
        try {
            List<TestOrder> orders = techService.getPendingOrders();
            if (orders == null) return;
            for (TestOrder o : orders) {
                String techName = techService.getTechnicianName(o.getTechnicianId());
                worklistModel.addRow(new Object[]{o.getOrderId(), o.getPatientId(), o.getDateOrdered(), o.getStatus(), techName});
            }
        } catch (Throwable ex) {
            LOGGER.log(Level.WARNING, "Failed to refresh worklist", ex);
            JOptionPane.showMessageDialog(this, "Failed to load pending orders: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createLabBenchPanel() {
        JPanel p = new JPanel(new BorderLayout(12, 12)); p.setOpaque(false);
        JLabel title = new JLabel("Lab Bench - Pending Results"); title.setFont(new Font("Segoe UI", Font.BOLD, 22)); title.setForeground(textDark); p.add(title, BorderLayout.NORTH);
        String[] cols = {"Result ID","Order ID","Test Name","Value","Validated"}; benchModel = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        benchTable = styleTable(new JTable(benchModel)); JScrollPane sp = new JScrollPane(benchTable); sp.setBorder(BorderFactory.createEmptyBorder()); p.add(sp, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); UiPalette.FlatButton btnRefresh = new UiPalette.FlatButton("Refresh"); btnRefresh.addActionListener(e->refreshBenchTable()); actions.add(btnRefresh); p.add(actions, BorderLayout.SOUTH);
        benchTable.addMouseListener(new MouseAdapter(){ public void mouseClicked(MouseEvent e){ if (e.getClickCount()==2){ int r = benchTable.getSelectedRow(); if (r!=-1){ int mr = benchTable.convertRowIndexToModel(r); int rid = (int)benchModel.getValueAt(mr,0); String testName = (String) benchModel.getValueAt(mr,2); showResultEntryDialog(rid,testName); } } } });
        return p;
    }

    public void refreshBenchTable() {
        benchModel.setRowCount(0);
        try {
            List<TestResult> pending;
            if (currentUser != null && currentUser.getRole() != null && currentUser.getRole() == ApplicationTier.Model.Enums.Role.SUPER_ADMIN) {
                pending = techService.getPendingResultsAll();
            } else {
                pending = techService.getPendingResultsForTechnician(currentUser.getEmployeeId());
            }
            if (pending == null) return;
            for (TestResult r : pending) benchModel.addRow(new Object[]{r.getResultId(), r.getOrderId(), r.getTestName(), r.getValue(), r.isValidated()});
        } catch (Throwable ex) {
            LOGGER.log(Level.WARNING, "Failed to refresh bench", ex);
            JOptionPane.showMessageDialog(this, "Failed to load bench: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createHistoryPanel() {
        JPanel p = new JPanel(new BorderLayout(12,12)); p.setOpaque(false); JLabel title = new JLabel("Test History"); title.setFont(new Font("Segoe UI", Font.BOLD, 22)); title.setForeground(textDark); p.add(title, BorderLayout.NORTH);
        String[] cols = {"Result ID","Order ID","Test Name","Value","Result Date","Validated"}; historyModel = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        JTable historyTable = styleTable(new JTable(historyModel)); JScrollPane sp = new JScrollPane(historyTable); sp.setBorder(BorderFactory.createEmptyBorder()); p.add(sp, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); UiPalette.FlatButton btnRefresh = new UiPalette.FlatButton("Refresh"); btnRefresh.addActionListener(e->refreshHistoryTable()); actions.add(btnRefresh); p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    public void refreshHistoryTable() {
        historyModel.setRowCount(0);
        try {
            List<TestResult> all;
            if (currentUser != null && currentUser.getRole() != null && currentUser.getRole() == ApplicationTier.Model.Enums.Role.SUPER_ADMIN) {
                all = techService.getAllResultsAll();
            } else {
                all = techService.getAllResultsForTechnician(currentUser.getEmployeeId());
            }
            if (all == null) return;
            for (TestResult r : all) historyModel.addRow(new Object[]{r.getResultId(), r.getOrderId(), r.getTestName(), r.getValue(), r.getResultDate(), r.isValidated()});
        } catch (Throwable ex) {
            LOGGER.log(Level.WARNING, "Failed to refresh history", ex);
            JOptionPane.showMessageDialog(this, "Failed to load history: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshCatalogTable() {
        if (catalogModel == null) return;
        catalogModel.setRowCount(0);
        try {
            List<TestType> tests = techService.getTestCatalog();
            if (tests == null) return;
            for (TestType t : tests) {
                catalogModel.addRow(new Object[]{t.getTestTypeId(), t.getCategoryId(), t.getName(), t.getPrice(), t.getNormalRange(), t.getDescription()});
            }
        } catch (Throwable ex) {
            LOGGER.log(Level.WARNING, "Failed to refresh catalog", ex);
            JOptionPane.showMessageDialog(this, "Failed to load catalog: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshTodayOrders() {
        if (todayModel == null) return;
        todayModel.setRowCount(0);
        try {
            java.util.List<Appointment> appts = techService.getTodayAppointments();
            if (appts == null) return;
            int apptCount = 0;
            int pendingResults = 0;
            int validated = 0;
            for (Appointment a : appts) {
                if (a == null) continue;
                apptCount++;
                Integer linkedOrderId = null;
                java.util.List<TestOrder> orders = techService.getOrRestoreOrdersForAppointment(a.getAppointmentId());
                if (orders != null && !orders.isEmpty()) linkedOrderId = orders.get(0).getOrderId();
                todayModel.addRow(new Object[]{a.getAppointmentId(), linkedOrderId, a.getPatientId(), a.getDate(), a.getReason(), a.getStatus()});

                if (orders != null) {
                    for (TestOrder o : orders) {
                        java.util.List<TestResult> res = techService.getResultsForOrder(o.getOrderId());
                        if (res != null) {
                            for (TestResult r : res) {
                                if (r.isValidated()) validated++; else pendingResults++;
                            }
                        }
                    }
                }
            }
            ordersCountLabel.setText(String.valueOf(apptCount));
            pendingResultsLabel.setText(String.valueOf(pendingResults));
            validatedResultsLabel.setText(String.valueOf(validated));
        } catch (Throwable ex) {
            LOGGER.log(Level.WARNING, "refreshTodayOrders failed", ex);
        }
    }

    private JPanel createCatalogPanel() {
        JPanel p = new JPanel(new BorderLayout(12,12)); p.setOpaque(false); JLabel title = new JLabel("Test Catalog"); title.setFont(new Font("Segoe UI", Font.BOLD, 22)); title.setForeground(textDark); p.add(title, BorderLayout.NORTH);
        String[] cols = {"Test ID","Category ID","Name","Price","Ref Range","Description"}; catalogModel = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        JTable catalogTable = styleTable(new JTable(catalogModel)); JScrollPane sp = new JScrollPane(catalogTable); sp.setBorder(BorderFactory.createEmptyBorder()); p.add(sp, BorderLayout.CENTER); return p;
    }

    private JPanel createStatPanel(String title, JLabel valueLabel, Color color){ JPanel card = new JPanel(new BorderLayout()); card.setBackground(white); card.setBorder(new EmptyBorder(18,18,18,18)); JLabel t = new JLabel(title.toUpperCase()); t.setFont(new Font("Segoe UI", Font.BOLD, 11)); t.setForeground(textLight); valueLabel.setForeground(color); valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 34)); card.add(t, BorderLayout.NORTH); card.add(valueLabel, BorderLayout.CENTER); return card; }

    private MenuButton createNavButton(String text, String panelKey) {
        MenuButton btn = new MenuButton(text);
        btn.addActionListener(e -> {
            for (MenuButton b : menuButtons) b.setSelected(false);
            btn.setSelected(true);
            if (panelKey != null) cardLayout.show(contentPanel, panelKey);
        });
        menuButtons.add(btn);
        return btn;
    }

    private void addSidebarButtons(JPanel navPanel) {
        MenuButton btnHome = createNavButton("Home", PANEL_HOME);
        navPanel.add(btnHome);
        navPanel.add(Box.createVerticalStrut(6));

        MenuButton btnWorklist = createNavButton("Worklist", PANEL_WORKLIST);
        btnWorklist.addActionListener(e -> refreshWorklistTable());
        navPanel.add(btnWorklist);
        navPanel.add(Box.createVerticalStrut(6));

        MenuButton btnBench = createNavButton("Lab Bench", PANEL_BENCH);
        btnBench.addActionListener(e -> refreshBenchTable());
        navPanel.add(btnBench);
        navPanel.add(Box.createVerticalStrut(6));

        MenuButton btnHistory = createNavButton("History", PANEL_HISTORY);
        btnHistory.addActionListener(e -> refreshHistoryTable());
        navPanel.add(btnHistory);
        navPanel.add(Box.createVerticalStrut(6));

        MenuButton btnCatalog = createNavButton("Catalog", PANEL_CATALOG);
        btnCatalog.addActionListener(e -> refreshCatalogTable());
        navPanel.add(btnCatalog);
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
        JLabel role = new JLabel(currentUser.getRole().toString()); role.setFont(new Font("Segoe UI", Font.BOLD, 12)); role.setForeground(UiPalette.MEDICAL_BLUE);
        p.add(role, c);

        // Info grid 3x2
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
        UiPalette.FlatButton btnClose = new UiPalette.FlatButton("Close"); btnClose.setPreferredSize(new Dimension(160,40)); btnClose.addActionListener(e -> profileDialog.dispose());
        p.add(btnClose, c);

        profileDialog.add(p);
        profileDialog.setVisible(true);
    }

    private void addProfileField(JPanel p, String label, String value) {
        JPanel field = new JPanel(new BorderLayout(0,6)); field.setOpaque(false);
        JLabel l = new JLabel(label.toUpperCase()); l.setFont(new Font("Segoe UI", Font.BOLD, 11)); l.setForeground(UiPalette.TEXT_LIGHT);
        JLabel v = new JLabel(value != null ? value : "-"); v.setFont(new Font("Segoe UI", Font.PLAIN, 14)); v.setForeground(UiPalette.TEXT);
        field.add(l, BorderLayout.NORTH); field.add(v, BorderLayout.CENTER);
        p.add(field);
    }

    private JTable styleTable(JTable table){ table.setRowHeight(44); table.setFont(new Font("Segoe UI", Font.PLAIN, 13)); table.setGridColor(UiPalette.PANEL); table.setSelectionBackground(UiPalette.MEDICAL_BLUE); table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12)); table.getTableHeader().setBackground(white); table.getTableHeader().setForeground(textLight); return table; }

    private void viewOrderResultsDialog(int orderId) {
        List<TestResult> results = techService.getResultsForOrder(orderId);
        if (results == null || results.isEmpty()) { JOptionPane.showMessageDialog(this, "No test results for this order."); return; }
        JDialog d = new JDialog(this, "Order Results - " + orderId, true); d.setSize(760,520); d.setLocationRelativeTo(this);
        String[] cols = {"Result ID","Test Name","Value","Validated","Price"}; DefaultTableModel m = new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} };
        JTable t = styleTable(new JTable(m)); t.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        java.util.Map<Integer, Double> priceMap = new java.util.HashMap<>(); java.util.List<TestType> catalog = techService.getTestCatalog(); if (catalog!=null) for (TestType tt: catalog) priceMap.put(tt.getTestTypeId(), tt.getPrice());
        double total = 0.0; for (TestResult r: results) { double p = 0.0; if (r.getTestTypeId()>0 && priceMap.containsKey(r.getTestTypeId())) p = priceMap.get(r.getTestTypeId()); m.addRow(new Object[]{r.getResultId(), r.getTestName(), r.getValue(), r.isValidated(), String.format("%.2f", p)}); total += p; }
        JPanel tool = new JPanel(new FlowLayout(FlowLayout.LEFT)); tool.setOpaque(false); UiPalette.FlatButton btnEditSel = new UiPalette.FlatButton("Edit Selected"); UiPalette.FlatButton btnValidateSel = new UiPalette.FlatButton("Validate Selected"); UiPalette.FlatButton btnValidateAll = new UiPalette.FlatButton("Validate All"); btnEditSel.setPreferredSize(new Dimension(140,36)); btnValidateSel.setPreferredSize(new Dimension(160,36)); btnValidateAll.setPreferredSize(new Dimension(140,36)); tool.add(btnEditSel); tool.add(btnValidateSel); tool.add(btnValidateAll);
        JLabel lblTotal = new JLabel(String.format("Order Total: %.2f EUR", total)); lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblTotal.setForeground(UiPalette.MEDICAL_BLUE); JPanel rightTool = new JPanel(new FlowLayout(FlowLayout.RIGHT)); rightTool.setOpaque(false); rightTool.add(lblTotal);
        JPanel topRow = new JPanel(new BorderLayout()); topRow.setOpaque(false); topRow.add(tool, BorderLayout.WEST); topRow.add(rightTool, BorderLayout.EAST);
        JScrollPane sp = new JScrollPane(t); JPanel cp = new JPanel(new BorderLayout(8,8)); cp.setBorder(new EmptyBorder(8,8,8,8)); cp.add(topRow, BorderLayout.NORTH); cp.add(sp, BorderLayout.CENTER);

        btnEditSel.addActionListener(ae -> { int r = t.getSelectedRow(); if (r==-1){ JOptionPane.showMessageDialog(d, "Select a result to edit."); return; } int mr = t.convertRowIndexToModel(r); int rid = (int) m.getValueAt(mr,0); String testName = (String) m.getValueAt(mr,1); showResultEntryDialog(rid,testName); java.util.List<TestResult> ref = techService.getResultsForOrder(orderId); m.setRowCount(0); double tot2=0.0; if (ref!=null) for (TestResult rr: ref){ double p=0.0; if (rr.getTestTypeId()>0 && priceMap.containsKey(rr.getTestTypeId())) p = priceMap.get(rr.getTestTypeId()); m.addRow(new Object[]{rr.getResultId(), rr.getTestName(), rr.getValue(), rr.isValidated(), String.format("%.2f", p)}); tot2 += p; } lblTotal.setText(String.format("Order Total: %.2f EUR", tot2)); refreshBenchTable(); refreshHistoryTable(); refreshTodayOrders(); });

        btnValidateSel.addActionListener(ae -> { int[] sel = t.getSelectedRows(); if (sel==null || sel.length==0){ JOptionPane.showMessageDialog(d, "Select one or more results to validate."); return; } int validatedCount=0; for (int row: sel){ int mr = t.convertRowIndexToModel(row); int rid = (int) m.getValueAt(mr,0); boolean ok = techService.validateResultAs(currentUser.getEmployeeId(), currentUser.getRole(), rid); if (ok) validatedCount++; } if (validatedCount>0) { techService.postValidationOrderCheck(orderId); JOptionPane.showMessageDialog(d, "Validated " + validatedCount + " result(s)."); } else JOptionPane.showMessageDialog(d, "No results validated (permission denied or error)."); java.util.List<TestResult> ref = techService.getResultsForOrder(orderId); m.setRowCount(0); double tot3=0.0; if (ref!=null) for (TestResult rr: ref){ double p=0.0; if (rr.getTestTypeId()>0 && priceMap.containsKey(rr.getTestTypeId())) p = priceMap.get(rr.getTestTypeId()); m.addRow(new Object[]{rr.getResultId(), rr.getTestName(), rr.getValue(), rr.isValidated(), String.format("%.2f", p)}); tot3 += p; } lblTotal.setText(String.format("Order Total: %.2f EUR", tot3)); refreshBenchTable(); refreshHistoryTable(); refreshTodayOrders(); });

        btnValidateAll.addActionListener(ae -> { java.util.List<TestResult> all = techService.getResultsForOrder(orderId); if (all==null||all.isEmpty()){ JOptionPane.showMessageDialog(d, "No results to validate."); return; } int okCount=0; for (TestResult rr: all) { if (!rr.isValidated()) { if (techService.validateResultAs(currentUser.getEmployeeId(), currentUser.getRole(), rr.getResultId())) okCount++; } } if (okCount>0) techService.postValidationOrderCheck(orderId); JOptionPane.showMessageDialog(d, "Validated " + okCount + " result(s)."); java.util.List<TestResult> ref = techService.getResultsForOrder(orderId); m.setRowCount(0); double tot4=0.0; if (ref!=null) for (TestResult rrr: ref){ double p = 0.0; if (rrr.getTestTypeId()>0 && priceMap.containsKey(rrr.getTestTypeId())) p = priceMap.get(rrr.getTestTypeId()); m.addRow(new Object[]{rrr.getResultId(), rrr.getTestName(), rrr.getValue(), rrr.isValidated(), String.format("%.2f", p)}); tot4 += p; } lblTotal.setText(String.format("Order Total: %.2f EUR", tot4)); refreshBenchTable(); refreshHistoryTable(); refreshTodayOrders(); });

        d.add(cp); d.setVisible(true);
    }

    private void showResultEntryDialog(int resultId, String testName){ JDialog d = new JDialog(this, "Record Result - " + testName, true); d.setSize(420,360); d.setLocationRelativeTo(this); JPanel p = new JPanel(new GridBagLayout()); p.setBackground(white); p.setBorder(new EmptyBorder(12,12,12,12)); GridBagConstraints c = new GridBagConstraints(); c.fill = GridBagConstraints.HORIZONTAL; c.gridx=0; c.gridy=0; c.insets = new Insets(8,0,8,0); JLabel lval = new JLabel("Value"); lval.setFont(new Font("Segoe UI", Font.BOLD, 12)); lval.setForeground(UiPalette.TEXT_LIGHT); p.add(lval,c); c.gridy++; JTextField txtVal = new JTextField(20); txtVal.setPreferredSize(new Dimension(300,36)); p.add(txtVal,c); c.gridy++; JLabel lint = new JLabel("Interpretation"); lint.setFont(new Font("Segoe UI", Font.BOLD,12)); lint.setForeground(UiPalette.TEXT_LIGHT); p.add(lint,c); c.gridy++; JTextField txtInt = new JTextField(20); txtInt.setPreferredSize(new Dimension(300,36)); p.add(txtInt,c); c.gridy++; JCheckBox chk = new JCheckBox("Validate & finalize"); chk.setOpaque(false); p.add(chk,c); c.gridy++; UiPalette.FlatButton btnSave = new UiPalette.FlatButton("Save"); btnSave.addActionListener(e->{ try{ boolean saved = techService.enterResultDataAs(currentUser.getRole(), resultId, txtVal.getText(), txtInt.getText()); if (!saved){ JOptionPane.showMessageDialog(d, "Failed to save result (permission denied or DB error)."); return; } boolean validated=false; if (chk.isSelected()){ validated = techService.validateResultAs(currentUser.getEmployeeId(), currentUser.getRole(), resultId); if (!validated) JOptionPane.showMessageDialog(d, "Result saved but validation failed or permission denied."); } if (validated){ TestResult updated = techService.getResultById(resultId); if (updated!=null) techService.postValidationOrderCheck(updated.getOrderId()); } JOptionPane.showMessageDialog(d, "Saved successfully."); refreshTodayOrders(); refreshBenchTable(); refreshHistoryTable(); d.dispose(); } catch(Exception ex){ LOGGER.log(Level.WARNING, "Failed saving result", ex); JOptionPane.showMessageDialog(d, "An error occurred: " + ex.getMessage()); } }); p.add(btnSave,c); d.add(p); d.setVisible(true); }
    class MenuButton extends JButton {
        private boolean isSelected = false;

        public MenuButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(textLight);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(12, 25, 12, 10));
            setMaximumSize(new Dimension(240, 50));
            setOpaque(false);
        }

        public void setSelected(boolean b) {
            isSelected = b;
            setForeground(b ? UiPalette.MEDICAL_BLUE : textLight);
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

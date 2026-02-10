package PresentationTier;
import ApplicationTier.Model.Employee;
import ApplicationTier.Model.Enums.Role;
import ApplicationTier.AdminService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AdminDashboard extends JFrame {

    //color palette
    protected final Color bgMain = UiPalette.BG;
    protected final Color white = UiPalette.WHITE;
    protected final Color textDark = UiPalette.TEXT;
    protected static final Color textLight = UiPalette.TEXT_LIGHT;
    protected final Color primaryAction = UiPalette.MEDICAL_BLUE;
    protected final Color accentYellow = UiPalette.ACCENT;
    protected final Color errorColor = UiPalette.ERROR;
    // pie chart palette
    private static final Color[] PIE_COLORS = new Color[]{
        new Color(0xe4c1f9),
        new Color(0xa9def9),
        new Color(0xd0f4de),
        new Color(0xfcf6bd),
        new Color(0xff99c8)
    };

    //core fields
    protected Employee currentUser;
    protected AdminService adminService;
    private Font iconFont = null;
    // animation config
    private static final long KPI_ANIM_DURATION_MS = 800L;
    // UI components
    protected JPanel sidebar;
    protected JPanel contentPanel;
    protected JPanel homePanel;
    protected CardLayout cardLayout;
    private List<MenuButton> menuButtons = new ArrayList<>();
    protected List<Employee> employeeCache;

    protected DefaultTableModel staffModel;
    protected JTable staffTable;
    protected TableRowSorter<DefaultTableModel> sorter;

    protected static final String PANEL_HOME = "HOME";
    protected static final String PANEL_STAFF = "STAFF";
    protected static final String PANEL_ADD = "ADD";

    protected static final Logger LOGGER = Logger.getLogger(AdminDashboard.class.getName());

    public AdminDashboard(Employee user) {
        this.currentUser = user;
        this.adminService = new AdminService();

        // Setup
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("TextComponent.arc", 12);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "L&F init failed", ex);
        }

        try { this.iconFont = loadIconFont(); } catch (Throwable ignored) {}

        initUI();
    }

    // ui initialization
    private void initUI() {
        setTitle("MediLab - Admin Portal | " + currentUser.getFirstName());
        setSize(1300, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Sidebar
        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Content Area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(bgMain);
        homePanel = createHomePanel();
        contentPanel.add(homePanel, PANEL_HOME);
        contentPanel.add(createStaffListPanel(), PANEL_STAFF);
        contentPanel.add(createAddStaffPanel(), PANEL_ADD);

        add(contentPanel, BorderLayout.CENTER);

        // Initial Data Load
        refreshStaffTable();
    }

    //sidebar creation
    private JPanel createSidebar() {
        JPanel p = new JPanel();
        p.setBackground(white);
        p.setPreferredSize(new Dimension(260, getHeight()));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(30, 20, 30, 20));

        // Brand
        JLabel brand = new JLabel("MediLab");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 28));
        brand.setForeground(primaryAction);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(brand);

        JLabel subBrand = new JLabel("ADMIN DASHBOARD");
        subBrand.setFont(new Font("Segoe UI", Font.BOLD, 11));
        subBrand.setForeground(textLight);
        subBrand.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(subBrand);

        p.add(Box.createVerticalStrut(50));

        // Menu
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setOpaque(false);
        menu.setAlignmentX(Component.LEFT_ALIGNMENT);

        addSidebarButtons(menu);

        JScrollPane menuScroll = new JScrollPane(menu, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.setBorder(BorderFactory.createEmptyBorder());
        menuScroll.getViewport().setBackground(white);
        menuScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuScroll.getVerticalScrollBar().setUnitIncrement(16);

        p.add(menuScroll);

        return p;
    }

    protected void addSidebarButtons(JPanel menu) {
        menu.add(createSectionLabel("MAIN MENU"));
        menu.add(Box.createVerticalStrut(10));
        menu.add(createNavButton("Dashboard", PANEL_HOME));
        menu.add(Box.createVerticalStrut(5));
        menu.add(createNavButton("Staff Directory", PANEL_STAFF));
        menu.add(Box.createVerticalStrut(5));
        menu.add(createNavButton("Add New Staff", PANEL_ADD));

        menu.add(Box.createVerticalGlue());

        menu.add(createSectionLabel("ACCOUNT"));
        menu.add(Box.createVerticalStrut(10));

        MenuButton btnProfile = createNavButton("My Profile", null);
        btnProfile.addActionListener(e -> openProfileWindow());
        menu.add(btnProfile);

        menu.add(Box.createVerticalStrut(5));

        MenuButton btnLogout = createNavButton("Logout", null);
        btnLogout.setForeground(errorColor);
        btnLogout.addActionListener(e -> logout());
        menu.add(btnLogout);
    }

    protected JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(textLight);
        lbl.setBorder(new EmptyBorder(0, 15, 5, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    protected MenuButton createNavButton(String text, String targetPanel) {
        MenuButton btn = new MenuButton(text);
        btn.addActionListener(e -> {
            if (targetPanel != null) {
                for (MenuButton b : menuButtons) b.setSelected(false);
                btn.setSelected(true);
                cardLayout.show(contentPanel, targetPanel);
                if (PANEL_STAFF.equals(targetPanel)) refreshStaffTable();
            }
        });
        if (targetPanel != null && PANEL_HOME.equals(targetPanel)) {
            btn.setSelected(true);
        }
        if (targetPanel != null) menuButtons.add(btn);
        return btn;
    }

    // home panel creation
    private JPanel createHomePanel() {
        JPanel p = new JPanel(new BorderLayout(14, 14));
        p.setBackground(bgMain);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBox = new JPanel(new BorderLayout());
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Welcome back, " + currentUser.getFirstName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(textDark);
        JLabel sub = new JLabel("Overview of today's activity");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(textLight);

        titleBox.add(title, BorderLayout.NORTH);
        titleBox.add(sub, BorderLayout.SOUTH);

        header.add(titleBox, BorderLayout.WEST);

        p.add(header, BorderLayout.NORTH);
        p.add(createHomeContent(), BorderLayout.CENTER);
        return p;
    }

    private JScrollPane createHomeContent() {
        // Top row: KPIs (centered and larger) with a refresh action aligned to right
        JPanel topRow = new JPanel(new BorderLayout(10, 0));
        topRow.setOpaque(false);

        // KPI grid with 2 large cards centered
        JPanel kpiContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        kpiContainer.setOpaque(false);
        JPanel kpiGrid = new JPanel(new GridLayout(1, 2, 18, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.add(createSolidKpiCard("Total Employees", getEmployeeCount(), null, primaryAction));
        kpiGrid.add(createSolidKpiCard("Active Patients", adminService.getPatientCount(), null, new Color(0x16A085)));
        kpiGrid.setPreferredSize(new Dimension(680, 140));
        kpiContainer.add(kpiGrid);

        RoundedPanel kpiWrap = new RoundedPanel(10, UiPalette.WHITE);
        kpiWrap.setLayout(new BorderLayout());
        kpiWrap.setBorder(new EmptyBorder(16, 16, 16, 16));
        kpiWrap.add(kpiContainer, BorderLayout.CENTER);
        kpiWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // actions aligned right in the top row (only Refresh)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); actions.setOpaque(false);
        ModernButton btnRefresh = new ModernButton("Refresh", primaryAction);
        btnRefresh.setPreferredSize(new Dimension(100, 36));
        btnRefresh.addActionListener(e -> { refreshStaffTable(); rebuildHomePanel(); });
        actions.add(btnRefresh);

        RoundedPanel actionsWrap = new RoundedPanel(6, UiPalette.WHITE);
        actionsWrap.setLayout(new BorderLayout());
        actionsWrap.setBorder(new EmptyBorder(6,6,6,6));
        actionsWrap.add(actions, BorderLayout.CENTER);

        topRow.add(kpiWrap, BorderLayout.CENTER);
        topRow.add(actionsWrap, BorderLayout.EAST);

        // Main content: left = compact legend/info, right = very small pie chart
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setOpaque(false);

        // prepare counts for legend
        Map<String, Integer> counts = new LinkedHashMap<>();
        if (employeeCache != null && !employeeCache.isEmpty()) {
            for (Employee e : employeeCache) {
                String r = e.getRole().toString();
                counts.put(r, counts.getOrDefault(r, 0) + 1);
            }
        }

        JPanel legend = createLegendPanel(counts);
        // widen legend and make the pie chart much larger for visual impact
        legend.setPreferredSize(new Dimension(300, 380));

        RoundedPanel chartWrap = new RoundedPanel(14, UiPalette.WHITE);
        chartWrap.setLayout(new BorderLayout());
        chartWrap.setBorder(new EmptyBorder(16, 16, 16, 16));
        PieChartPanel pie = new PieChartPanel(false); // no internal legend
        // much larger pie to dominate the right area and balance KPI cards
        pie.setPreferredSize(new Dimension(480, 380));
        chartWrap.add(pie, BorderLayout.CENTER);

        // Right-align the small chart so layout feels balanced
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); rightBox.setOpaque(false);
        rightBox.add(chartWrap);

        main.add(legend, BorderLayout.WEST);
        main.add(rightBox, BorderLayout.CENTER);

        // Compose holder (top KPIs + main content) with slightly more breathing space below KPIs
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout(12, 14));
        container.setOpaque(false);
        container.add(topRow, BorderLayout.NORTH);
        container.add(main, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    JPanel createSolidKpiCard(String title, int value, String icon, Color accent) {
        // Bigger KPI card: increase padding and font sizes for emphasis
        RoundedPanel card = new RoundedPanel(12, UiPalette.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setPreferredSize(new Dimension(320, 120));

        JPanel accentBar = new JPanel();
        accentBar.setBackground(accent);
        accentBar.setPreferredSize(new Dimension(8, 0));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JPanel txt = new JPanel(new BorderLayout()); txt.setOpaque(false);
        JLabel lblTitle = new JLabel(title.toUpperCase()); lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13)); lblTitle.setForeground(UiPalette.TEXT_LIGHT);
        JLabel lblValue = new JLabel("0"); lblValue.setFont(new Font("Segoe UI", Font.BOLD, 30)); lblValue.setForeground(UiPalette.TEXT);
        txt.add(lblTitle, BorderLayout.NORTH);
        txt.add(lblValue, BorderLayout.CENTER);

        content.add(txt, BorderLayout.CENTER);

        if (icon != null && iconFont != null) {
            JLabel iconLbl = new JLabel(icon);
            try { iconLbl.setFont(iconFont.deriveFont(Font.PLAIN, 18f)); iconLbl.setForeground(accent); }
            catch (Exception ex) { iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16)); }
            iconLbl.setBorder(new EmptyBorder(6,8,6,0));
            content.add(iconLbl, BorderLayout.EAST);
        }

        card.add(accentBar, BorderLayout.WEST);
        card.add(content, BorderLayout.CENTER);

        animateLabel(lblValue, value);
        return card;
    }

    // Keep per-label animation timers so we don't run multiple animations for the same JLabel
    private final java.util.Map<JLabel, Timer> activeLabelTimers = new java.util.WeakHashMap<>();

    private void animateLabel(JLabel label, int target) {
        if (label == null) return;
        Timer prev = activeLabelTimers.get(label);
        if (prev != null && prev.isRunning()) prev.stop();

        if (target <= 0) { label.setText(String.valueOf(target)); return; }

        final long durationMs = KPI_ANIM_DURATION_MS;
        final long startTime = System.currentTimeMillis();
        final int startValue = 0;

        Timer t = new Timer(16, null);
        t.addActionListener(e -> {
            double elapsed = Math.max(0, System.currentTimeMillis() - startTime);
            double p = Math.min(1.0, elapsed / (double) durationMs);
            double eased = (1 - Math.cos(p * Math.PI)) / 2.0;
            int current = startValue + (int) Math.round(eased * (target - startValue));
            label.setText(String.valueOf(current));
            if (p >= 1.0) {
                ((Timer) e.getSource()).stop();
                activeLabelTimers.remove(label);
            }
        });
        t.setInitialDelay(0);
        activeLabelTimers.put(label, t);
        t.start();
    }

    // improved legend: role name with percentage underneath (and count)
    private JPanel createLegendPanel(Map<String,Integer> counts) {
        RoundedPanel p = new RoundedPanel(8, UiPalette.WHITE);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(12,12,12,12));

        JLabel title = new JLabel("Roles");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(textDark);
        p.add(title, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        list.setBorder(new EmptyBorder(10,0,0,0));

        if (counts == null || counts.isEmpty()) {
            JLabel no = new JLabel("No data"); no.setForeground(UiPalette.TEXT_LIGHT); no.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            list.add(Box.createVerticalStrut(8)); list.add(no);
        } else {
            int i = 0; int total = counts.values().stream().mapToInt(Integer::intValue).sum();
            for (Map.Entry<String,Integer> e : counts.entrySet()) {
                JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false);

                // swatch + name block (left)
                Color c = PIE_COLORS[i % PIE_COLORS.length];
                JPanel sw = new JPanel(); sw.setBackground(c); sw.setPreferredSize(new Dimension(16,12)); sw.setBorder(BorderFactory.createLineBorder(UiPalette.PANEL));

                // role name (wrap if long) using HTML and a small percentage label below
                String nameHtml = String.format("<html><div style='width:140px; font-family: Segoe UI; font-size:13px; color:#000000;'>%s</div></html>", e.getKey());
                JLabel lblName = new JLabel(nameHtml);
                lblName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lblName.setForeground(textDark);
                lblName.setToolTipText(e.getKey());

                double perc = e.getValue() * 100.0 / Math.max(1, total);
                String meta = String.format("%.1f%%  •  %d", perc, e.getValue());
                JLabel lblMeta = new JLabel(meta);
                lblMeta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblMeta.setForeground(UiPalette.TEXT_LIGHT);

                JPanel nameBlock = new JPanel(); nameBlock.setLayout(new BoxLayout(nameBlock, BoxLayout.Y_AXIS)); nameBlock.setOpaque(false);
                nameBlock.add(lblName);
                nameBlock.add(lblMeta);

                JPanel leftWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6)); leftWrap.setOpaque(false);
                leftWrap.add(sw); leftWrap.add(nameBlock);

                row.add(leftWrap, BorderLayout.WEST);

                list.add(row);
                list.add(Box.createVerticalStrut(8));
                i++;
            }
        }

        JScrollPane sp = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null); sp.setOpaque(false); sp.getViewport().setOpaque(false);
        sp.setPreferredSize(new Dimension(200, 220));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // add staff panel creation
    private JPanel createAddStaffPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setBackground(bgMain);
        p.setBorder(new EmptyBorder(30, 50, 30, 50));

        JLabel title = new JLabel("Add New Staff Member");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(textDark);
        p.add(title, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(20, white);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 15, 5, 15);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridy = 0;

        RoundedTextField txtFirst = new RoundedTextField(20); txtFirst.setPlaceholder("First Name");
        RoundedTextField txtLast = new RoundedTextField(20); txtLast.setPlaceholder("Last Name");
        RoundedTextField txtUser = new RoundedTextField(20); txtUser.setPlaceholder("Username");
        RoundedPasswordField txtPass = new RoundedPasswordField(20); txtPass.setPlaceholder("Password");
        RoundedTextField txtPhone = new RoundedTextField(20); txtPhone.setPlaceholder("Phone");
        RoundedTextField txtEmail = new RoundedTextField(20); txtEmail.setPlaceholder("Email");
        RoundedTextField txtAddr = new RoundedTextField(20); txtAddr.setPlaceholder("Address");
        JComboBox<Role> cmbRole = new JComboBox<>(Role.values()); cmbRole.setBackground(white);

        addFormInput(card, c, "First Name", txtFirst, 0);
        addFormInput(card, c, "Last Name", txtLast, 1);
        c.gridy++;
        addFormInput(card, c, "Username", txtUser, 0);
        addFormInput(card, c, "Password", txtPass, 1);
        c.gridy++;
        addFormInput(card, c, "Role", cmbRole, 0);
        addFormInput(card, c, "Phone", txtPhone, 1);
        c.gridy++;
        addFormInput(card, c, "Email", txtEmail, 0);
        addFormInput(card, c, "Address", txtAddr, 1);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(40, 15, 10, 15);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;

        ModernButton btnSave = new ModernButton("Create Account", primaryAction);
        btnSave.setPreferredSize(new Dimension(250, 50));
        btnSave.addActionListener(e -> {
            Employee emp = new Employee( );
            emp.setFirstName(txtFirst.getText());
            emp.setLastName(txtLast.getText());
            emp.setUsername(txtUser.getText());
            emp.setPassword(new String(txtPass.getPassword()));
            emp.setRole((Role) cmbRole.getSelectedItem());
            emp.setPhone(txtPhone.getText());
            emp.setEmail(txtEmail.getText());
            emp.setAddress(txtAddr.getText());
            emp.setHireDate(new java.util.Date());

            if (adminService.addEmployee(emp)) {
                JOptionPane.showMessageDialog(this, "Employee added successfully!");
                refreshStaffTable();
                cardLayout.show(contentPanel, PANEL_STAFF);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add employee. Check inputs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(btnSave, c);

        p.add(new JScrollPane(card), BorderLayout.CENTER);
        return p;
    }

    private void addFormInput(JPanel p, GridBagConstraints c, String label, JComponent comp, int x) {
        c.gridx = x;
        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(textLight);
        wrap.add(lbl, BorderLayout.NORTH);
        comp.setPreferredSize(new Dimension(200, 45));
        wrap.add(comp, BorderLayout.CENTER);
        p.add(wrap, c);
    }



    // helpers and actions
    protected void refreshStaffTable() {
        staffModel.setRowCount(0);
        try {
            employeeCache = adminService.getAllEmployees();
            if (employeeCache != null) {
                for (Employee e : employeeCache) {
                    staffModel.addRow(new Object[]{
                        e.getEmployeeId(), e.getFirstName(), e.getLastName(),
                        e.getUsername(), e.getRole(), e.getHireDate(),
                        e.getPhone(), e.getEmail(), e.getAddress()
                    });
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error refreshing staff", ex);
        }
    }

    private int getEmployeeCount() {
        return employeeCache != null ? employeeCache.size() : 0;
    }

    private void editSelectedEmployee() {
        int row = staffTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee.");
            return;
        }
        int id = (int) staffTable.getValueAt(row, 0);
        Employee target = employeeCache.stream().filter(e -> e.getEmployeeId() == id).findFirst().orElse(null);
        if (target != null) showEditDialog(target);
    }

    private void showEditDialog(Employee emp) {
        JDialog d = new JDialog(this, "Edit Employee: " + emp.getFullName(), true);
        d.setSize(500, 640);
        d.setLocationRelativeTo(this);

        RoundedPanel card = new RoundedPanel(20, white);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 5, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridy = 0;

        RoundedTextField txtFirst = new RoundedTextField(20); txtFirst.setText(emp.getFirstName());
        RoundedTextField txtLast = new RoundedTextField(20); txtLast.setText(emp.getLastName());
        RoundedTextField txtPhone = new RoundedTextField(20); txtPhone.setText(emp.getPhone());
        RoundedTextField txtEmail = new RoundedTextField(20); txtEmail.setText(emp.getEmail());
        RoundedTextField txtAddr = new RoundedTextField(20); txtAddr.setText(emp.getAddress());
        RoundedPasswordField txtPass = new RoundedPasswordField(20); txtPass.setPlaceholder("Leave blank to keep current password");
        JComboBox<Role> cmbRole = new JComboBox<>(Role.values()); cmbRole.setSelectedItem(emp.getRole());
        cmbRole.setBackground(white);

        addFormInput(card, c, "First Name", txtFirst, 0); c.gridy++;
        addFormInput(card, c, "Last Name", txtLast, 0); c.gridy++;
        addFormInput(card, c, "Phone", txtPhone, 0); c.gridy++;
        addFormInput(card, c, "Email", txtEmail, 0); c.gridy++;
        addFormInput(card, c, "Address", txtAddr, 0); c.gridy++;
        addFormInput(card, c, "Password", txtPass, 0); c.gridy++;
        addFormInput(card, c, "Role", cmbRole, 0); c.gridy++;

        c.insets = new Insets(30, 10, 10, 10);
        ModernButton btnSave = new ModernButton("Save Changes", primaryAction);
        btnSave.setPreferredSize(new Dimension(200, 45));
        btnSave.addActionListener(e -> {
            emp.setFirstName(txtFirst.getText());
            emp.setLastName(txtLast.getText());
            emp.setPhone(txtPhone.getText());
            emp.setEmail(txtEmail.getText());
            emp.setAddress(txtAddr.getText());


            String newPass = new String(txtPass.getPassword());
            if (newPass != null && !newPass.trim().isEmpty()) {
                emp.setPassword(newPass);
            }

            Role newRole = (Role) cmbRole.getSelectedItem();
            boolean success;
            if (newRole != emp.getRole()) {
                success = adminService.modifyRoles(emp, newRole);
            } else {
                success = adminService.updateEmployeeDetails(emp);
            }

            if (success) {
                JOptionPane.showMessageDialog(d, "Employee updated successfully!");
                refreshStaffTable();
                d.dispose();
            } else {
                JOptionPane.showMessageDialog(d, "Failed to update employee.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        card.add(btnSave, c);

        d.add(new JScrollPane(card));
        d.setVisible(true);
    }

    private void deleteSelectedEmployee() {
        int row = staffTable.getSelectedRow();
        if (row == -1) return;
        int id = (int) staffTable.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete employee ID " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (adminService.removeEmployee(id)) refreshStaffTable();
        }
    }

    private void logout() {
        dispose();
        new LoginPage().setVisible(true);
    }

    protected void openProfileWindow() {
        JDialog profileDialog = new JDialog(this, "My Profile", true);
        // Compact, centered profile dialog
        profileDialog.setSize(520, 620);
        profileDialog.setResizable(false);
        // center on the screen
        profileDialog.setLocationRelativeTo(null);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(white);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 20, 0);

        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // draw circular badge
                g2.setColor(primaryAction);
                g2.fillOval(0,0,120,120);
                String initials = "";
                if (currentUser.getFirstName() != null && !currentUser.getFirstName().isEmpty()) initials += currentUser.getFirstName().charAt(0);
                if (currentUser.getLastName() != null && !currentUser.getLastName().isEmpty()) initials += currentUser.getLastName().charAt(0);
                g2.setColor(UiPalette.WHITE);
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
        UiPalette.FlatButton btnClose = new UiPalette.FlatButton("Close"); btnClose.setPreferredSize(new Dimension(160,40));
        btnClose.addActionListener(e -> profileDialog.dispose());
        p.add(btnClose, c);

        profileDialog.add(p);
        profileDialog.setVisible(true);
    }

    private void addProfileField(JPanel p, String label, String value) {
         JPanel field = new JPanel(new BorderLayout(0,6)); field.setOpaque(false);
         JLabel l = new JLabel(label.toUpperCase()); l.setFont(new Font("Segoe UI", Font.BOLD, 11)); l.setForeground(UiPalette.TEXT_LIGHT);
         JLabel v = new JLabel(value != null ? value : "-"); v.setFont(new Font("Segoe UI", Font.PLAIN, 14)); v.setForeground(textDark);
         field.add(l, BorderLayout.NORTH); field.add(v, BorderLayout.CENTER);
         p.add(field);
     }


    static class RoundedPanel extends JPanel {
        private int radius;
        private Color bg;
        public RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Menu Button
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

        }
        public void setSelected(boolean b) {
            isSelected = b;
            setForeground(b ? primaryAction : textLight);
            repaint();
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected) {
                g2.setColor(UiPalette.withAlpha(UiPalette.MEDICAL_BLUE, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(primaryAction);
                g2.fillRect(0, 8, 4, getHeight()-16);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Modern Action Button
    static class ModernButton extends JButton {
        private Color color;
        public ModernButton(String text, Color color) {
            super(text);
            this.color = color;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(UiPalette.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Rounded Text Field
    static class RoundedTextField extends JTextField {
        private String placeholder;
        public RoundedTextField(int cols) {
            super(cols);
            setOpaque(false);
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
        public void setPlaceholder(String p) { this.placeholder = p; }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UiPalette.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(UiPalette.PANEL);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            g2.dispose();
            super.paintComponent(g);
        }
        @Override public void paint(Graphics g) {
            super.paint(g);
            if (getText().isEmpty() && placeholder != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiPalette.TEXT_LIGHT);
                g2.setFont(getFont());
                g2.drawString(placeholder, 15, getHeight()/2 + 5);
                g2.dispose();
            }
        }
    }

    // Rounded Password Field
    static class RoundedPasswordField extends JPasswordField {
        private String placeholder;
        public RoundedPasswordField(int cols) {
            super(cols);
            setOpaque(false);
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
        public void setPlaceholder(String p) { this.placeholder = p; }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UiPalette.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(UiPalette.PANEL);
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            g2.dispose();
            super.paintComponent(g);
        }
        @Override public void paint(Graphics g) {
            super.paint(g);
            if (getPassword().length == 0 && placeholder != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiPalette.TEXT_LIGHT);
                g2.setFont(getFont());
                g2.drawString(placeholder, 15, getHeight()/2 + 5);
                g2.dispose();
            }
        }
    }

    // Custom Pie Chart
    class PieChartPanel extends JPanel {
        private int hoveredIndex = -1;
        private java.util.List<Double> angles = new java.util.ArrayList<>();
        private boolean showLegend = true;

        public PieChartPanel() { this(true); }
        public PieChartPanel(boolean showLegend) {
            this.showLegend = showLegend;
            setOpaque(false);
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) { handleHover(e.getPoint()); }
            });
            addMouseListener(new MouseAdapter() { public void mouseExited(MouseEvent e) { hoveredIndex = -1; repaint(); } });
        }

        private void handleHover(Point p) {
             if (employeeCache == null || employeeCache.isEmpty()) return;
             int w = getWidth(); int h = getHeight();
             int legendW = showLegend ? Math.max(140, w/5) : 0;
             int availW = Math.max(100, w - legendW) - 20;
             int size = Math.min(availW, h) - 20; // margin
             int x = (w - legendW - size) / 2; int y = (h - size) / 2;
             int cx = x + size/2; int cy = y + size/2;
             double dx = p.x - cx; double dy = p.y - cy;
             double dist = Math.hypot(dx, dy);
             double r = size/2.0;
             if (dist > r + 6) { if (hoveredIndex != -1) { hoveredIndex = -1; setToolTipText(null); repaint(); } return; }

             double angleDeg = Math.toDegrees(Math.atan2(-dy, dx));
             angleDeg = (angleDeg + 360) % 360; // normalize

             // find slice by cumulative angles
             double acc = 0; int idx = -1;
             for (int i=0;i<angles.size();i++) {
                 acc += angles.get(i);
                 if (angleDeg <= acc) { idx = i; break; }
             }
             if (idx != hoveredIndex) {
                 hoveredIndex = idx;
                 // update tooltip with friendly text
                 if (hoveredIndex >= 0 && hoveredIndex < angles.size()) {
                     Map<String, Integer> countsLocal = new LinkedHashMap<>();
                     for (Employee e2 : employeeCache) {
                         String ro = e2.getRole().toString();
                         countsLocal.put(ro, countsLocal.getOrDefault(ro, 0) + 1);
                     }
                     java.util.List<Map.Entry<String,Integer>> list = new java.util.ArrayList<>(countsLocal.entrySet());
                     int totalLocal = employeeCache.size();
                     int idx2 = hoveredIndex % Math.max(1, list.size());
                     if (idx2 < list.size()) {
                         Map.Entry<String,Integer> ent = list.get(idx2);
                         double perc = ent.getValue() * 100.0 / (double) totalLocal;
                         String tip = String.format("%s — %.1f%% (%d)", ent.getKey(), perc, ent.getValue());
                         setToolTipText(tip);
                     } else setToolTipText(null);
                 } else {
                     setToolTipText(null);
                 }
                 repaint();
             }
         }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (employeeCache == null || employeeCache.isEmpty()) {
                g2.setColor(UiPalette.TEXT_LIGHT);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                String msg = "No staff data";
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(msg);
                g2.drawString(msg, (getWidth()-tw)/2, getHeight()/2);
                g2.dispose();
                return;
            }


            Map<String, Integer> counts = new LinkedHashMap<>();
            for (Employee e : employeeCache) {
                String r = e.getRole().toString();
                counts.put(r, counts.getOrDefault(r, 0) + 1);
            }
            int total = employeeCache.size();

            int w = getWidth(); int h = getHeight();
            int legendW = showLegend ? Math.max(140, w/5) : 0;
            int availW = Math.max(100, w - legendW) - 20;

            int size = Math.min(availW, h) - 20; // compact sizing
            int x = (w - legendW - size) / 2;
            int y = (h - size) / 2;

            double startAngle = 90;
            Color[] colors = PIE_COLORS;
            angles.clear();

            int i = 0;
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                double angle = 360.0 * entry.getValue() / total;
                angles.add(angle);
                Color sliceColor = colors[i % colors.length];

                double mid = startAngle + angle/2.0;
                double rad = Math.toRadians(mid);
                int off = (i == hoveredIndex) ? 10 : 0;
                int ox = (int) Math.round(Math.cos(rad) * off);
                int oy = (int) Math.round(Math.sin(rad) * off);

                g2.setColor(sliceColor);
                g2.fillArc(x + ox, y + oy, size, size, (int)Math.round(startAngle), (int)Math.ceil(angle));


                g2.setColor(UiPalette.withAlpha(UiPalette.TEXT_PRIMARY, 18));
                g2.setStroke(new BasicStroke(1f));
                g2.drawArc(x + ox, y + oy, size, size, (int)Math.round(startAngle), (int)Math.ceil(angle));

                startAngle += angle;
                i++;
            }

            // Draw donut hole for modern look
            int hole = (int) Math.round(size * 0.48);
            int hx = x + (size - hole)/2;
            int hy = y + (size - hole)/2;
            g2.setColor(UiPalette.WHITE);
            g2.fillOval(hx, hy, hole, hole);

            // Center totals
            g2.setColor(UiPalette.TEXT);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            String main = String.valueOf(total);
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(main);
            g2.drawString(main, hx + (hole - tw)/2, hy + hole/2 + fm.getAscent()/2 - 2);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            String small = "Staff";
            int sw = g2.getFontMetrics().stringWidth(small);
            g2.setColor(UiPalette.TEXT_LIGHT);
            g2.drawString(small, hx + (hole - sw)/2, hy + hole - 8);

            // Legend (right side) - compact modern dots + label
            if (showLegend) {
                int lx = x + size + 14;
                int ly = y + 8;
                int idx2 = 0;
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                for (Map.Entry<String,Integer> entry : counts.entrySet()) {
                    Color c = colors[idx2 % colors.length];
                    // draw dot
                    int dotY = ly + idx2 * 22;
                    g2.setColor(c);
                    g2.fillOval(lx, dotY, 12, 12);
                    // text
                    g2.setColor(UiPalette.TEXT);
                    String label = entry.getKey() + " (" + entry.getValue() + ")";
                    g2.drawString(label, lx + 18, dotY + 10);
                    idx2++;
                }
            }

            g2.dispose();
        }
    }


    // Rebuilds the home panel and shows it. Useful after refreshing data.
    private void rebuildHomePanel() {
        try {
            if (homePanel != null) {
                contentPanel.remove(homePanel);
            }
            homePanel = createHomePanel();
            contentPanel.add(homePanel, PANEL_HOME);
            contentPanel.revalidate();
            contentPanel.repaint();
            cardLayout.show(contentPanel, PANEL_HOME);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to rebuild home panel", ex);
        }
    }

    private JPanel createStaffListPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setBackground(bgMain);
        p.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Staff Directory");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(textDark);

        // Search
        RoundedTextField txtSearch = new RoundedTextField(20);
        txtSearch.setPlaceholder("Search by name, role, or email...");
        txtSearch.setPreferredSize(new Dimension(350, 45));
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = txtSearch.getText();
                if (text.trim().isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        header.add(title, BorderLayout.WEST);
        header.add(txtSearch, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        ModernButton btnEdit = new ModernButton("Edit Selected", UiPalette.ACCENT);
        btnEdit.setForeground(textDark);

        ModernButton btnDelete = new ModernButton("Delete Selected", errorColor);

        btnEdit.addActionListener(e -> editSelectedEmployee());
        btnDelete.addActionListener(e -> deleteSelectedEmployee());

        toolbar.add(btnEdit);
        toolbar.add(btnDelete);

        // Table
        String[] cols = {"ID", "First Name", "Last Name", "Username", "Role", "Hire Date", "Phone", "Email", "Address"};
        staffModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        staffTable = new JTable(staffModel);
        staffTable.setRowHeight(45);
        staffTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        staffTable.setSelectionBackground(UiPalette.withAlpha(UiPalette.MEDICAL_BLUE, 30));
        staffTable.setSelectionForeground(textDark);
        staffTable.setShowVerticalLines(false);
        staffTable.setShowHorizontalLines(true);
        staffTable.setGridColor(UiPalette.PANEL);

        // Header Styling
        JTableHeader th = staffTable.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setBackground(white);
        th.setForeground(textLight);
        th.setPreferredSize(new Dimension(0, 50));

        sorter = new TableRowSorter<>(staffModel);
        staffTable.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(staffTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(white);

        // Card container for table
        RoundedPanel tableCard = new RoundedPanel(15, white);
        tableCard.setLayout(new BorderLayout());
        // Add padding inside card
        JPanel inner = new JPanel(new BorderLayout(0, 15));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(20, 20, 20, 20));
        inner.add(toolbar, BorderLayout.NORTH);
        inner.add(scroll, BorderLayout.CENTER);
        tableCard.add(inner);

        p.add(tableCard, BorderLayout.CENTER);
        return p;
    }

    private Font loadIconFont() {
        try {
            String cwd = System.getProperty("user.dir");
            java.io.File fontsDir = new java.io.File(cwd, "resources/fonts");
            if (!fontsDir.exists() || !fontsDir.isDirectory()) return null;
            java.io.File[] files = fontsDir.listFiles((d, name) -> name.toLowerCase().endsWith(".ttf") || name.toLowerCase().endsWith(".otf"));
            if (files == null || files.length == 0) return null;
            java.io.File f = files[0];
            Font base = Font.createFont(Font.TRUETYPE_FONT, f);
            return base.deriveFont(Font.PLAIN, 18f);
        } catch (Throwable ex) {
            LOGGER.log(Level.FINER, "Failed to load icon font", ex);
            return null;
        }
    }

}

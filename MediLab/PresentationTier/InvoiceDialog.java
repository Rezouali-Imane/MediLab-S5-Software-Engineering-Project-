package PresentationTier;

import ApplicationTier.SecretaryService;
import ApplicationTier.Model.TestOrder;
import ApplicationTier.Model.Invoice;
import ApplicationTier.Model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class InvoiceDialog extends JDialog {
    private final SecretaryService secretaryService = new SecretaryService();

    private JComboBox<String> cmbOrders;
    private JTextField txtAmount;

    private final int appointmentId;
    private final int patientId;

    public InvoiceDialog(Window owner, int appointmentId, int patientId) {
        super(owner, "Create Invoice", ModalityType.APPLICATION_MODAL);
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        initUI();
    }

    private void initUI() {
        setSize(480, 280);
        setLocationRelativeTo(getOwner());
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(12,12,12,12));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.gridx = 0; c.gridy = 0; c.insets = new Insets(6,6,6,6);

        JLabel lbl = new JLabel("Select Order (optional)");
        p.add(lbl, c);

        c.gridy++;
        cmbOrders = new JComboBox<>();
        cmbOrders.setPreferredSize(new Dimension(420, 30));
        p.add(cmbOrders, c);
        List<TestOrder> orders = null;
        if (appointmentId > 0) {
            orders = secretaryService.getOrdersByAppointment(appointmentId);
        }
        if (orders == null || orders.isEmpty()) orders = secretaryService.getOrdersForPatient(patientId);

        cmbOrders.addItem("-- No Order (create without order) --");
        if (orders != null) {
            for (TestOrder o : orders) {
                cmbOrders.addItem("#" + o.getOrderId() + " — " + o.getStatus());
            }
        }


        c.gridy++;
        p.add(new JLabel("Amount (EUR)"), c);

        c.gridy++;
        txtAmount = new JTextField(); txtAmount.setPreferredSize(new Dimension(420,30));
        p.add(txtAmount, c);


        c.gridy++;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnCreate = new UiPalette.FlatButton("Create");
        JButton btnExport = new UiPalette.FlatButton("Export HTML");
        JButton btnEmail = new UiPalette.FlatButton("Send Email (simulated)");
        actions.add(btnExport); actions.add(btnEmail); actions.add(btnCreate);
        p.add(actions, c);


        cmbOrders.addActionListener(e -> onOrderSelected());
        btnCreate.addActionListener(e -> onCreate());
        btnExport.addActionListener(e -> onExport());
        btnEmail.addActionListener(e -> onEmail());


        try {
            Patient pat = null;
            var list = secretaryService.getAllPatients();
            if (list != null) {
                for (Patient pp : list) if (pp.getPatientId() == patientId) { pat = pp; break; }
            }
            if (pat != null && pat.getEmail() != null) this.getRootPane().putClientProperty("defaultRecipient", pat.getEmail());
        } catch (Exception ignored) {}

        setContentPane(p);
        if (orders != null && !orders.isEmpty()) cmbOrders.setSelectedIndex(1);
    }

    private void onOrderSelected() {
        String sel = (String) cmbOrders.getSelectedItem();
        if (sel != null && sel.startsWith("#")) {
            try {
                int orderId = Integer.parseInt(sel.substring(1, sel.indexOf(' ')));

                TestOrder o = null;
                for (TestOrder to : secretaryService.getOrdersForPatient(patientId)) if (to.getOrderId() == orderId) { o = to; break; }
                double total = 0;
                if (o != null) {

                    var inv = secretaryService.getInvoiceByOrder(orderId);
                    if (inv != null) total = inv.getTotalAmount();
                }
                if (total > 0) txtAmount.setText(String.valueOf(total)); else txtAmount.setText("");


                Patient p = null; var pats = secretaryService.getAllPatients(); if (pats != null) { for (Patient pp : pats) if (pp.getPatientId() == patientId) { p = pp; break; } }
                if (p != null && p.getEmail() != null) this.getRootPane().putClientProperty("defaultRecipient", p.getEmail());
            } catch (Exception ex) {
                txtAmount.setText("");
            }
        } else {
            txtAmount.setText("");
            try { Patient p = null; var pats = secretaryService.getAllPatients(); if (pats != null) { for (Patient pp : pats) if (pp.getPatientId() == patientId) { p = pp; break; } } if (p != null && p.getEmail() != null) this.getRootPane().putClientProperty("defaultRecipient", p.getEmail()); } catch (Exception ignored) {}
        }
    }

    private void onCreate() {
        Integer orderId = getSelectedOrderId();
        double total;
        try {
            total = Double.parseDouble(txtAmount.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean created = secretaryService.createInvoiceForOrder(orderId != null ? orderId : 0, patientId, total);

        if (created) {
            JOptionPane.showMessageDialog(this, "Invoice created successfully.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create invoice. Check logs for details.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onExport() {
        String html = buildInvoiceHtmlPreview();
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Invoice as HTML");
        int rv = fc.showSaveDialog(this);
        if (rv == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter fw = new java.io.FileWriter(fc.getSelectedFile())) {
                fw.write(html);
                JOptionPane.showMessageDialog(this, "Invoice exported. You can open this file and print to PDF.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEmail() {
        String defaultRecipient = (String) this.getRootPane().getClientProperty("defaultRecipient");
        Integer orderId = getSelectedOrderId();

        double total;
        try {
            total = Double.parseDouble(txtAmount.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Invoice inv = null;
        if (orderId != null) {
            inv = secretaryService.getInvoiceByOrder(orderId);
        }

         if (inv == null) {
            boolean created = secretaryService.createInvoiceForOrder(orderId != null ? orderId : 0, patientId, total);
            if (!created) {
                JOptionPane.showMessageDialog(this, "Failed to create invoice before sending.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (orderId != null) {
                inv = secretaryService.getInvoiceByOrder(orderId);
            } else {
                   inv = new Invoice(0, patientId, total);
                inv.setOrderId(0);
            }
        }

        if (inv == null) {
             JOptionPane.showMessageDialog(this, "Could not retrieve or create the invoice for emailing.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
        }

         JDialog dlg = new JDialog(this, "Simulate Invoice Email", true);
        dlg.setSize(600, 420);
        dlg.setLocationRelativeTo(this);
        JPanel panel = new JPanel(new BorderLayout(12,12));
        panel.setBorder(new javax.swing.border.EmptyBorder(12,12,12,12));
        panel.setBackground(UiPalette.WHITE);

        JPanel fields = new JPanel(new GridLayout(3,1,6,6)); fields.setOpaque(false);
        JTextField txtTo = new JTextField(defaultRecipient != null ? defaultRecipient : "");
        JTextField txtSubject = new JTextField("Your MediLab Invoice " + (inv.getInvoiceId() > 0 ? ("#" + inv.getInvoiceId()) : "(draft)"));
        JPanel toWrap = new JPanel(new BorderLayout()); toWrap.setOpaque(false); toWrap.add(new JLabel("To"), BorderLayout.NORTH); toWrap.add(txtTo, BorderLayout.CENTER);
        JPanel subWrap = new JPanel(new BorderLayout()); subWrap.setOpaque(false); subWrap.add(new JLabel("Subject"), BorderLayout.NORTH); subWrap.add(txtSubject, BorderLayout.CENTER);
        fields.add(toWrap); fields.add(subWrap);

        JTextArea body = new JTextArea(); body.setLineWrap(true); body.setWrapStyleWord(true);
        String orderLabel = (orderId != null) ? ("#" + orderId) : "(none)";
        body.setText("Dear patient,\n\nPlease find your invoice attached (simulated).\n\n" +
                "Invoice: " + (inv.getInvoiceId() > 0 ? ("#" + inv.getInvoiceId()) : "(new)") +
                "\nOrder: " + orderLabel +
                "\nAmount: " + inv.getTotalAmount() + " EUR\n\nRegards,\nMediLab Front Desk");
        JScrollPane bodyScroll = new JScrollPane(body); bodyScroll.setPreferredSize(new Dimension(540, 180));
        bodyScroll.getViewport().setBackground(UiPalette.WHITE);
        panel.add(fields, BorderLayout.NORTH);
        panel.add(bodyScroll, BorderLayout.CENTER);

        UiPalette.FlatButton sendSim = new UiPalette.FlatButton("Send (Simulated)");

        JDialog progress = new JDialog(dlg, "Sending…", true);
        progress.setUndecorated(true);
        JPanel pContent = new JPanel(new BorderLayout(12,12)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiPalette.withAlpha(UiPalette.WHITE, 245));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.dispose();
            }
        };
        pContent.setBorder(new javax.swing.border.EmptyBorder(18,20,18,20));
        pContent.setBackground(UiPalette.withAlpha(UiPalette.WHITE, 245));
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); header.setOpaque(false);
        JLabel hdrIcon = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
        JLabel hdrTitle = new JLabel("Sending email…"); hdrTitle.setFont(new Font("Segoe UI", Font.BOLD, 14)); hdrTitle.setForeground(UiPalette.TEXT);
        header.add(hdrIcon); header.add(hdrTitle);
        JProgressBar bar = new JProgressBar(); bar.setIndeterminate(false); bar.setBorder(new javax.swing.border.EmptyBorder(8,0,0,0));
        bar.setMinimum(0); bar.setMaximum(100); bar.setValue(0); bar.setForeground(UiPalette.MEDICAL_BLUE);
        JLabel sub = new JLabel("Connecting to SMTP… (simulated)"); sub.setForeground(UiPalette.TEXT_LIGHT);
        pContent.add(header, BorderLayout.NORTH);
        JPanel centerWrap = new JPanel(new BorderLayout(8,8)); centerWrap.setOpaque(false);
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
        };
        shadow.setOpaque(false); shadow.setBorder(new javax.swing.border.EmptyBorder(6,6,6,6)); shadow.add(pContent);
        progress.setContentPane(shadow); progress.setSize(380, 160); progress.setLocationRelativeTo(dlg);

        final Invoice finalInv = inv;
        sendSim.addActionListener(ae -> {
             String to = txtTo.getText();
             if (to == null || to.trim().isEmpty()) { JOptionPane.showMessageDialog(dlg, "Recipient email required."); return; }
             sendSim.setEnabled(false); txtTo.setEnabled(false); txtSubject.setEnabled(false); body.setEnabled(false);
             SwingUtilities.invokeLater(() -> progress.setVisible(true));

             SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                 @Override protected Void doInBackground() throws Exception {
                     publish(5); Thread.sleep(300);
                     for (int i=6;i<=35;i+=3) { Thread.sleep(30); publish(i); }
                     publish(40); Thread.sleep(250);
                     for (int i=41;i<=75;i+=4) { Thread.sleep(45); publish(i); }
                     publish(85); Thread.sleep(250);
                     for (int i=86;i<=98;i+=2) { Thread.sleep(25); publish(i); }
                     publish(100); Thread.sleep(120);
                     return null;
                 }
                 @Override protected void process(java.util.List<Integer> chunks) {
                     int v = chunks.get(chunks.size()-1);
                     bar.setValue(v);
                     if (v < 20) sub.setText("Connecting to SMTP… (simulated)");
                     else if (v < 50) sub.setText("Authenticating… (simulated)");
                     else if (v < 80) sub.setText("Uploading attachment… (simulated)");
                     else if (v < 100) sub.setText("Sending email… (simulated)");
                     else sub.setText("Completed");
                 }
                 @Override protected void done() {
                     try { progress.setVisible(false); progress.dispose(); } catch (Exception ignored) {}
                     JOptionPane.showMessageDialog(dlg, "Email sent successfully (simulated) to " + txtTo.getText() + "\nInvoice: " + (finalInv.getInvoiceId()>0?"#"+finalInv.getInvoiceId():"(new)"));
                     dlg.dispose();
                 }
             };
             worker.execute();
        });
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT)); foot.setOpaque(false); foot.add(sendSim);
        panel.add(foot, BorderLayout.SOUTH);

        dlg.add(panel);
        dlg.setVisible(true);
    }

    private Integer getSelectedOrderId() {
        String sel = (String) cmbOrders.getSelectedItem();
        if (sel != null && sel.startsWith("#")) {
            try {
                return Integer.parseInt(sel.substring(1, sel.indexOf(' ')));
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String buildInvoiceHtmlPreview() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset=\"utf-8\"><title>Invoice</title></head><body style='font-family:Segoe UI,Arial,sans-serif;'>");
        sb.append("<h2>MediLab Invoice</h2>");
        sb.append("<p><b>Patient ID:</b> ").append(patientId).append("</p>");
        String sel = (String) cmbOrders.getSelectedItem();
        sb.append("<p><b>Order:</b> ").append(sel == null ? "None" : sel).append("</p>");
        sb.append("<p><b>Amount:</b> ").append(txtAmount.getText()).append(" EUR</p>");
        sb.append("<hr><p>Thank you for choosing MediLab.</p>");
        sb.append("</body></html>");
        return sb.toString();
    }
}


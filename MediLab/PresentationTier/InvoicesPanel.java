package PresentationTier;

import ApplicationTier.FinancialManagerService;
import ApplicationTier.Model.Invoice;
import ApplicationTier.Model.Enums.PaymentStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.util.List;

public class InvoicesPanel extends JPanel {
    private final FinancialManagerService fmService = new FinancialManagerService();
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(InvoicesPanel.class.getName());

    private DefaultTableModel invoiceModel;
    private JTable invoiceTable;

    public InvoicesPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(UiPalette.BG);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Invoices");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UiPalette.TEXT);
        add(title, BorderLayout.NORTH);

        String[] cols = {"Invoice ID", "Order ID", "Patient ID", "Amount", "Status", "Date"};
        invoiceModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        invoiceTable = new JTable(invoiceModel);
        invoiceTable.setRowHeight(40);
        invoiceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        invoiceTable.getTableHeader().setBackground(UiPalette.WHITE);
        invoiceTable.getTableHeader().setForeground(UiPalette.TEXT_LIGHT);
        invoiceTable.setSelectionBackground(UiPalette.MEDICAL_BLUE);
        invoiceTable.setGridColor(UiPalette.PANEL);

        JScrollPane scroll = new JScrollPane(invoiceTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        UiPalette.FlatButton btnRefresh = new UiPalette.FlatButton("Refresh");
        UiPalette.FlatButton btnView = new UiPalette.FlatButton("View");
        UiPalette.FlatButton btnMarkPaid = new UiPalette.FlatButton("Mark Paid (FM only)");
        UiPalette.FlatButton btnPrint = new UiPalette.FlatButton("Print");
        UiPalette.FlatButton btnEmail = new UiPalette.FlatButton("Send Email (simulated)");
        UiPalette.FlatButton btnExport = new UiPalette.FlatButton("Export CSV");

        actions.add(btnRefresh); actions.add(btnView); actions.add(btnMarkPaid); actions.add(btnPrint); actions.add(btnEmail); actions.add(btnExport);
        add(actions, BorderLayout.SOUTH);

        btnRefresh.addActionListener(evt -> refreshInvoices());
        btnMarkPaid.addActionListener(evt -> {
            int row = invoiceTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Select an invoice first."); return; }
            int modelRow = invoiceTable.convertRowIndexToModel(row);
            int invoiceId = (int) invoiceModel.getValueAt(modelRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Mark invoice #"+invoiceId+" as PAID?","Confirm Payment", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                boolean ok = fmService.validatePayment(invoiceId, 1.0);
                if (ok) { JOptionPane.showMessageDialog(this, "Invoice marked as PAID by Financial Manager."); refreshInvoices(); }
                else JOptionPane.showMessageDialog(this, "Failed to update payment status.");
            } catch (Exception ex) {
                LOGGER.log(java.util.logging.Level.WARNING, "validatePayment failed", ex);
                JOptionPane.showMessageDialog(this, "Payment validation failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnView.addActionListener(evt -> viewSelectedInvoice());
        btnPrint.addActionListener(evt -> printSelectedInvoice());
        btnEmail.addActionListener(evt -> emailSelectedInvoiceSimulated());
        btnExport.addActionListener(evt -> exportCsv());

        refreshInvoices();
    }


    private void refreshInvoices() {
        invoiceModel.setRowCount(0);
        try {
            List<Invoice> list = fmService.findAllInvoices();
            if (list != null) {
                for (Invoice i : list) {
                    invoiceModel.addRow(new Object[]{i.getInvoiceId(), i.getOrderId(), i.getPatientId(), i.getTotalAmount(), i.getPaymentStatus(), i.getDate()});
                }
            }
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "refreshInvoices failed", ex);
        }
    }

    private void viewSelectedInvoice() {
        int row = invoiceTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an invoice first."); return; }
        int modelRow = invoiceTable.convertRowIndexToModel(row);
        int invoiceId = (int) invoiceModel.getValueAt(modelRow, 0);
        Invoice inv = fmService.findInvoiceById(invoiceId);
        if (inv == null) { JOptionPane.showMessageDialog(this, "Invoice not found."); return; }
        JTextArea t = new JTextArea();
        t.setEditable(false);
        t.setText(buildInvoiceText(inv));
        t.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane s = new JScrollPane(t);
        s.setPreferredSize(new Dimension(500, 400));
        JOptionPane.showMessageDialog(this, s, "Invoice " + inv.getInvoiceId(), JOptionPane.PLAIN_MESSAGE);
    }

    private String buildInvoiceText(Invoice inv) {
        StringBuilder sb = new StringBuilder();
        sb.append("INVOICE #").append(inv.getInvoiceId()).append("\n");
        sb.append("Order ID: ").append(inv.getOrderId()).append("\n");
        sb.append("Patient ID: ").append(inv.getPatientId()).append("\n");
        sb.append("Date: ").append(inv.getDate()).append("\n\n");
        sb.append(String.format("Total: %.2f\n", inv.getTotalAmount()));
        sb.append("Status: ").append(inv.getPaymentStatus()).append("\n");
        return sb.toString();
    }

    private void printSelectedInvoice() {
        int row = invoiceTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an invoice first."); return; }
        int modelRow = invoiceTable.convertRowIndexToModel(row);
        int invoiceId = (int) invoiceModel.getValueAt(modelRow, 0);
        Invoice inv = fmService.findInvoiceById(invoiceId);
        if (inv == null) { JOptionPane.showMessageDialog(this, "Invoice not found."); return; }
        // If invoice unpaid, ask for confirmation before printing (Financial Manager may still print)
        PaymentStatus ps = inv.getPaymentStatus();
        if (ps == null || ps != PaymentStatus.PAID) {
            int c = JOptionPane.showConfirmDialog(this, "Invoice #"+invoiceId+" is not marked PAID. Print anyway?", "Unpaid Invoice", JOptionPane.YES_NO_OPTION);
            if (c != JOptionPane.YES_OPTION) return;
        }
        JTextArea t = new JTextArea(buildInvoiceText(inv));
        try {
            boolean done = t.print();
            if (done) JOptionPane.showMessageDialog(this, "Print job sent."); else JOptionPane.showMessageDialog(this, "Print canceled or failed.");
        } catch (PrinterException ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "Print failed", ex);
            JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage());
        }
    }

    private void emailSelectedInvoiceSimulated() {
        int row = invoiceTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select an invoice first."); return; }
        int modelRow = invoiceTable.convertRowIndexToModel(row);
        int invoiceId = (int) invoiceModel.getValueAt(modelRow, 0);
        Invoice inv = fmService.findInvoiceById(invoiceId);
        if (inv == null) { JOptionPane.showMessageDialog(this, "Invoice not found."); return; }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Simulate Invoice Email", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(640, 420);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(12, 12)); panel.setBackground(UiPalette.WHITE); panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new GridLayout(2, 1)); top.setOpaque(false);
        JTextField txtTo = new JTextField(); txtTo.setToolTipText("Recipient email");
        JTextField txtSubject = new JTextField("Your MediLab Invoice #" + inv.getInvoiceId());
        top.add(wrapField("To", txtTo)); top.add(wrapField("Subject", txtSubject));

        JTextArea body = new JTextArea(); body.setText("Dear patient,\n\nPlease find your invoice attached (simulated).\n\nInvoice #: " + inv.getInvoiceId() + "\nAmount: " + inv.getTotalAmount() + " EUR\n\nRegards,\nMediLab Finance");
        body.setLineWrap(true); body.setWrapStyleWord(true);
        JScrollPane bodyScroll = new JScrollPane(body); bodyScroll.setPreferredSize(new Dimension(580, 180));

        panel.add(top, BorderLayout.NORTH);
        panel.add(bodyScroll, BorderLayout.CENTER);

        UiPalette.FlatButton sendSim = new UiPalette.FlatButton("Send (Simulated)"); sendSim.setPreferredSize(new Dimension(160, 40));
        JProgressBar bar = new JProgressBar(); bar.setStringPainted(true); bar.setValue(0); bar.setForeground(UiPalette.MEDICAL_BLUE);
        JLabel status = new JLabel("Ready to send"); status.setForeground(UiPalette.TEXT_LIGHT);

        sendSim.addActionListener(ae -> {
            String to = txtTo.getText();
            if (to == null || to.trim().isEmpty()) { JOptionPane.showMessageDialog(dlg, "Recipient email required."); return; }
            sendSim.setEnabled(false); txtTo.setEnabled(false); txtSubject.setEnabled(false); body.setEnabled(false);

            SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                @Override protected Void doInBackground() throws Exception {
                    int[] steps = {5, 20, 45, 70, 90, 100};
                    for (int t : steps) { Thread.sleep(180); publish(t); }
                    return null;
                }
                @Override protected void process(java.util.List<Integer> chunks) {
                    int v = chunks.get(chunks.size()-1); bar.setValue(v);
                    if (v < 20) status.setText("Connecting (simulated)");
                    else if (v < 45) status.setText("Authenticating (simulated)");
                    else if (v < 70) status.setText("Uploading attachment (simulated)");
                    else if (v < 95) status.setText("Transmitting (simulated)");
                    else status.setText("Finalizing...");
                }
                @Override protected void done() {
                    JOptionPane.showMessageDialog(dlg, "Email sent successfully (simulated) to " + to + "\nInvoice #" + inv.getInvoiceId());
                    dlg.dispose();
                }
            };
            worker.execute();
        });

        JPanel foot = new JPanel(new BorderLayout()); foot.setOpaque(false);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT)); right.setOpaque(false); right.add(sendSim);
        foot.add(status, BorderLayout.WEST); foot.add(bar, BorderLayout.CENTER); foot.add(right, BorderLayout.EAST);

        panel.add(foot, BorderLayout.SOUTH);
        dlg.add(panel); dlg.setVisible(true);
    }

    // Export invoices as CSV for easy accounting export
    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File f = fc.getSelectedFile();
        try (var fw = new java.io.FileWriter(f)) {
            fw.write("InvoiceId,OrderId,PatientId,Amount,Status,Date\n");
            List<Invoice> list = fmService.findAllInvoices();
            if (list != null) {
                for (Invoice i : list) {
                    String status = i.getPaymentStatus() != null ? i.getPaymentStatus().toString() : "";
                    fw.write(String.format("%d,%d,%d,%.2f,%s,%s\n", i.getInvoiceId(), i.getOrderId(), i.getPatientId(), i.getTotalAmount(), status, i.getDate()));
                }
            }
            JOptionPane.showMessageDialog(this, "Exported CSV to " + f.getAbsolutePath());
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.WARNING, "Export CSV failed", ex);
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
        }
    }


    private JPanel wrapField(String label, JComponent field) {
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false);
        JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", Font.BOLD, 12)); l.setForeground(UiPalette.TEXT_LIGHT);
        wrap.add(l, BorderLayout.NORTH); wrap.add(field, BorderLayout.CENTER);
        return wrap;
    }
}

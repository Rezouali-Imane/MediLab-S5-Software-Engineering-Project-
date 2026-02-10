package PresentationTier;

import ApplicationTier.AuthService;
import ApplicationTier.Model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;


public class LoginPage extends JFrame {
    private final AuthService authService;
    private RoundedTextField txtUsername;
    private RoundedPasswordField txtPassword;
    private UiPalette.FlatButton btnLogin;
    private JCheckBox chkShowPassword;
    private JLabel lblForgot;
    private final Color bgMain = UiPalette.BG;
    private final Color textDark = UiPalette.TEXT;
    private final Color textLight = UiPalette.TEXT_LIGHT;
    private final Color white = UiPalette.WHITE;
    private final Color primaryAction = UiPalette.MEDICAL_BLUE;

    public LoginPage() {
        this.authService = new AuthService();
        initUI();
        applyTheme();
    }

    private void initUI() {
        setTitle("MediLab - Secure Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 540);
        setLocationRelativeTo(null);
        setResizable(false);
        GradientPanel root = new GradientPanel();
        root.setLayout(new GridBagLayout());
        setContentPane(root);

        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(18, 18, 18, 18)); // Keep the padding

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 0, 6, 0);
        c.weightx = 1.0;

        JLabel logo = new JLabel("<html><div style='text-align:center;'>" +
                "<span style='font-size:28px;font-weight:700;color:rgb(0, 119, 182)'>MediLab</span><br>" +
                "<span style='font-size:11px;color:rgba(0,0,0,0.55)'>Laboratory Management</span>" +
                "</div></html>");
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setOpaque(false);
        c.gridy = 0;
        c.insets = new Insets(6, 0, 12, 0);
        formPanel.add(logo, c);

        JLabel title = new JLabel("Welcome to MediLab");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(textDark);
        title.setBorder(new javax.swing.border.EmptyBorder(0,0,10,0));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy++;
        c.insets = new Insets(4, 0, 2, 0);
        formPanel.add(title, c);

        JLabel lblSub = new JLabel("Sign in to your account");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy++;
        c.insets = new Insets(2, 0, 12, 0);
        formPanel.add(lblSub, c);

        txtUsername = new RoundedTextField(20);
        txtUsername.setPlaceholder("username");
        txtUsername.setPreferredSize(new Dimension(360, 44));
        txtUsername.setIcon(new GlyphIcon(GlyphIcon.Type.USER, 18));
        c.gridy++;
        c.insets = new Insets(6, 0, 6, 0);
        formPanel.add(txtUsername, c);

        txtPassword = new RoundedPasswordField(20);
        txtPassword.setPlaceholder("password");
        txtPassword.setPreferredSize(new Dimension(360, 44));
        txtPassword.setIcon(new GlyphIcon(GlyphIcon.Type.LOCK, 18));
        txtPassword.setEchoChar('•');
        c.gridy++;
        formPanel.add(txtPassword, c);

        JPanel opts = new JPanel(new BorderLayout());
        opts.setOpaque(false);
        chkShowPassword = new JCheckBox("Show");
        chkShowPassword.setOpaque(false);
        chkShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShowPassword.addActionListener(e -> {
            boolean show = chkShowPassword.isSelected();
            txtPassword.setEchoChar(show ? (char) 0 : '•');
        });

        lblForgot = new JLabel("Forgot password?");
        lblForgot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblForgot.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginPage.this,
                        "Please contact your administrator to reset your password.",
                        "Forgot Password",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        opts.add(chkShowPassword, BorderLayout.WEST);
        opts.add(lblForgot, BorderLayout.EAST);
        c.gridy++;
        c.insets = new Insets(6, 0, 10, 0);
        formPanel.add(opts, c);

        JPanel buttonsRow = new JPanel();
        buttonsRow.setOpaque(false);
        buttonsRow.setLayout(new BoxLayout(buttonsRow, BoxLayout.Y_AXIS));

        btnLogin = new UiPalette.FlatButton("Sign in");
        btnLogin.setPreferredSize(new Dimension(360, 50));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(this::handleLogin);
        btnLogin.setBackground(UiPalette.MEDICAL_BLUE);
        btnLogin.setForeground(UiPalette.contrastText(UiPalette.MEDICAL_BLUE));
        btnLogin.setOpaque(true);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.repaint();
        System.out.println("[DEBUG] btnLogin background: " + btnLogin.getBackground() + " hex=" + PresentationTier.UiPalette.toHex(btnLogin.getBackground()));

        buttonsRow.add(btnLogin);

        c.gridy++;
        c.insets = new Insets(2, 0, 6, 0);
        formPanel.add(buttonsRow, c);

        JLabel bottom = new JLabel("Need an account? Contact Super Admin");
        bottom.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bottom.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy++;
        c.insets = new Insets(8, 0, 8, 0);
        formPanel.add(bottom, c);

        JPanel footerWrap = new JPanel(new BorderLayout()); footerWrap.setOpaque(false);
        footerWrap.add(bottom, BorderLayout.CENTER);
        JButton btnSettings = new JButton("⚙");
        btnSettings.setPreferredSize(new Dimension(36, 24));
        btnSettings.setToolTipText("Settings");
        footerWrap.add(btnSettings, BorderLayout.EAST);
        formPanel.add(footerWrap, c);

        btnSettings.addActionListener(e -> {
            JDialog dlg = new JDialog(LoginPage.this, "Settings", true);
            dlg.setSize(520, 170);
            dlg.setLocationRelativeTo(LoginPage.this);
            JPanel dp = new JPanel(new GridBagLayout()); dp.setBorder(new EmptyBorder(12,12,12,12)); dp.setBackground(UiPalette.BG);
            GridBagConstraints gc = new GridBagConstraints(); gc.gridx=0; gc.gridy=0; gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(6,6,6,6);
            JLabel lbl = new JLabel("Log file path (optional):"); lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13)); dp.add(lbl, gc);
            gc.gridy++; JTextField txtPath = new JTextField(); txtPath.setColumns(40); txtPath.setText(new ApplicationTier.SuperAdminService().getActiveLogPath()); dp.add(txtPath, gc);
            gc.gridy++; JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btns.setOpaque(false);
            JButton ok = new JButton("Save"); JButton cancel = new JButton("Cancel");
            styleSettingsButton(ok); styleSettingsButton(cancel);
            btns.add(ok); btns.add(cancel); dp.add(btns, gc);
            ok.addActionListener(a -> {
                String v = txtPath.getText();
                try { new ApplicationTier.SuperAdminService().setActiveLogPath(v); JOptionPane.showMessageDialog(dlg, "Saved."); dlg.dispose(); } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Failed to save: " + ex.getMessage()); }
            });
            cancel.addActionListener(a -> dlg.dispose());
            dlg.setContentPane(dp); dlg.setVisible(true);
        });

        root.add(formPanel, new GridBagConstraints());

        getRootPane().setDefaultButton(btnLogin);
    }

    private void applyTheme() {
        GradientPanel root = (GradientPanel) getContentPane();
        root.setGradient(UiPalette.BG_LIGHT, UiPalette.PANEL_BG);

        for (Component comp : getAllComponents(this.getContentPane())) {
            if (comp instanceof JLabel) {
                if (comp == lblForgot) {
                    ((JLabel) comp).setForeground(UiPalette.MEDICAL_BLUE);
                } else {
                    ((JLabel) comp).setForeground(UiPalette.TEXT);
                }
            } else if (comp instanceof RoundedTextField) {
                ((RoundedTextField) comp).setColors(
                        UiPalette.WHITE,
                        UiPalette.TEXT,
                        UiPalette.TEXT_LIGHT,
                        UiPalette.TEXT_PRIMARY
                );
            } else if (comp instanceof RoundedPasswordField) {
                ((RoundedPasswordField) comp).setColors(
                        UiPalette.WHITE,
                        UiPalette.TEXT,
                        UiPalette.TEXT_LIGHT,
                        UiPalette.TEXT_PRIMARY
                );
            } else if (comp instanceof UiPalette.FlatButton) {
                ((UiPalette.FlatButton) comp).setPrimary(UiPalette.MEDICAL_BLUE);
            } else if (comp instanceof JButton) {
                  JButton jb = (JButton) comp;
                jb.setBackground(UiPalette.MEDICAL_BLUE);
                jb.setForeground(UiPalette.contrastText(UiPalette.MEDICAL_BLUE));
                jb.setOpaque(true);
                jb.setBorderPainted(false);
                jb.setFocusPainted(false);
            } else if (comp instanceof JCheckBox) {
                ((JCheckBox) comp).setForeground(UiPalette.TEXT_LIGHT);
            }
        }

        repaint();

           for (Component comp : getAllComponents(this.getContentPane())) {
            if (comp instanceof JButton) {
                JButton jb = (JButton) comp;
                String txt = jb.getText();
                if (txt != null) {
                    String s = txt.trim().toLowerCase();
                    if (s.contains("sign up") || s.contains("signup") || s.contains("register") || s.contains("create account") || s.contains("sign up")) {
                        jb.setBackground(UiPalette.MEDICAL_BLUE);
                        jb.setForeground(UiPalette.contrastText(UiPalette.MEDICAL_BLUE));
                        jb.setOpaque(true);
                        jb.setBorderPainted(false);
                        jb.setFocusPainted(false);
                    }
                }
            }
        }
    }

    private void styleSettingsButton(AbstractButton b) {
        if (b == null) return;
        Color bg = UiPalette.PRIMARY;
        b.setBackground(bg);
        b.setForeground(UiPalette.contrastText(bg));
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(UiPalette.UI_FONT_BOLD);
    }

    private Component[] getAllComponents(Container c) {
        java.util.List<Component> list = new java.util.ArrayList<>();
        for (Component comp : c.getComponents()) {
            list.add(comp);
            if (comp instanceof Container) {
                java.util.Collections.addAll(list, getAllComponents((Container) comp));
            }
        }
        return list.toArray(new Component[0]);
    }

     private void handleLogin(ActionEvent ev) {
        String user = txtUsername.getText();
        String pass = new String(txtPassword.getPassword());

        Employee employee = authService.login(user, pass);

        if (employee != null) {
            dispose();
            DashboardRouter.routeUser(employee);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid Username or Password",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

     private static class RoundedTextField extends JTextField {
        private final int arc = 14;
        private String placeholder = "";
        private Icon icon;
        private Color bgColor = UiPalette.WHITE;
        private Color textColor = UiPalette.TEXT;
        private Color hintColor = UiPalette.TEXT_LIGHT;
        private final Insets textInsets = new Insets(10, 44, 10, 14);

        RoundedTextField(int cols) {
            super(cols);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(textColor);
            setCaretColor(textColor);
            // improve focus behavior
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
        }

        public void setIcon(Icon icon) {
            this.icon = icon;
        }

        public void setColors(Color bg, Color text, Color hint, Color iconColor) {
            this.bgColor = bg;
            this.textColor = text;
            this.hintColor = hint;
            setForeground(textColor);
            setCaretColor(textColor);
            if (this.icon instanceof GlyphIcon) {
                ((GlyphIcon) this.icon).setColor(iconColor);
            }
            repaint();
        }

        @Override
        public Insets getInsets() {
            return textInsets;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();

             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, w, h, arc, arc);
            g2.setColor(UiPalette.withAlpha(UiPalette.TEXT_PRIMARY, 25));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            if (icon != null) {
                int ix = 12;
                int iy = (h - icon.getIconHeight()) / 2;
                icon.paintIcon(this, g2, ix, iy);
            }

             g2.dispose();

            super.paintComponent(g);
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int dx = 36;
            g2.translate(dx, 0);
            super.paintChildren(g2);
            g2.dispose();

        }

        @Override
        public void paint(Graphics g) {

            super.paint(g);

            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                g2.setColor(hintColor);
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() + fm.getAscent()) / 2 - 2;

                int x = 36;
                g2.drawString(placeholder, x, y);
                g2.dispose();
            }
        }
    }


    private static class RoundedPasswordField extends JPasswordField {
        private final int arc = 14;
        private String placeholder = "";
        private Icon icon;
        private Color bgColor = UiPalette.WHITE;
        private Color textColor = UiPalette.TEXT;
        private Color hintColor = UiPalette.TEXT_LIGHT;
        private final Insets textInsets = new Insets(10, 44, 10, 14);

        RoundedPasswordField(int cols) {
            super(cols);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(textColor);
            setCaretColor(textColor);
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
        }

        public void setIcon(Icon icon) {
            this.icon = icon;
        }

        public void setColors(Color bg, Color text, Color hint, Color iconColor) {
            this.bgColor = bg;
            this.textColor = text;
            this.hintColor = hint;
            setForeground(textColor);
            setCaretColor(textColor);
            if (this.icon instanceof GlyphIcon) {
                ((GlyphIcon) this.icon).setColor(iconColor);
            }
            repaint();
        }

        @Override
        public Insets getInsets() {
            return textInsets;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            g2.setColor(UiPalette.withAlpha(UiPalette.TEXT_PRIMARY, 25));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            if (icon != null) {
                int ix = 12;
                int iy = (h - icon.getIconHeight()) / 2;
                icon.paintIcon(this, g2, ix, iy);
            }

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int dx = 36;
            g2.translate(dx, 0);
            super.paintChildren(g2);
            g2.dispose();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (getPassword().length == 0 && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                g2.setColor(hintColor);
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                int x = 36;
                g2.drawString(placeholder, x, y);
                g2.dispose();
            }
        }
    }


    private static class GlyphIcon implements Icon {
        enum Type {USER, LOCK}

        private final Type type;
        private final int size;
        private Color color = UiPalette.TEXT_PRIMARY;

        GlyphIcon(Type type, int size) {
            this.type = type;
            this.size = size;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(this.color);
            if (type == Type.USER) {

                int head = size / 2;
                g2.fillOval(x + size / 4, y, head, head);

                g2.fillRoundRect(x + size / 6, y + size / 2, size * 2 / 3, size / 3, size / 6, size / 6);
            } else {

                g2.fillRoundRect(x + 2, y + size / 3 + 2, size - 4, size * 2 / 3 - 4, 4, 4);

                g2.drawArc(x + 4, y - 2, size - 8, size - 8, 0, 180);
            }
            g2.dispose();
        }
    }



    private static class GradientPanel extends JPanel {
        private Color start = UiPalette.BG_LIGHT;
        private Color end = UiPalette.PANEL_BG;

        GradientPanel() {
            setOpaque(true);
        }

        void setGradient(Color a, Color b) {
            this.start = a;
            this.end = b;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, start, w, h, end);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
            g2.dispose();
            super.paintComponent(g);
        }
    }


}

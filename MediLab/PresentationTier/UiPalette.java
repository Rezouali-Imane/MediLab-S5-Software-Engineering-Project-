package PresentationTier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public final class UiPalette {
    private UiPalette() {}

    public static final Color PRIMARY = Color.decode("#005887");
    public static final Color SECONDARY = Color.decode("#48CAE4");
    public static final Color BG_LIGHT = Color.decode("#F8F9FA");
    public static final Color PANEL_BG = Color.decode("#E9ECEF");
    public static final Color TEXT_PRIMARY = Color.decode("#343A40");
    public static final Color TEXT_MUTED = Color.decode("#6C757D");
    public static final Color ERROR = Color.decode("#F94144");
    public static final Color SUCCESS = Color.decode("#43AA8B");
    public static final Color WHITE = BG_LIGHT;
    public static final Color PRIMARY_DARK = darken(PRIMARY, 0.85);

    public static final Color BG = BG_LIGHT;
    public static final Color PANEL = PANEL_BG;
    public static final Color TEXT = TEXT_PRIMARY;
    public static final Color TEXT_LIGHT = TEXT_MUTED;
    public static final Color MEDICAL_BLUE = PRIMARY;
    public static final Color ACCENT = SECONDARY;

     public static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 13);
     public static final Font UI_FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    public static Color withAlpha(Color base, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    private static Color toColor(Color c) { return c == null ? WHITE : c; }

     public static String toHex(Color c) {
         Color cc = toColor(c);
         return String.format("#%02X%02X%02X", cc.getRed(), cc.getGreen(), cc.getBlue());
     }

      public static Color contrastText(Color background) {
         Color bg = toColor(background);
         double lum = (0.2126 * bg.getRed() + 0.7152 * bg.getGreen() + 0.0722 * bg.getBlue()) / 255.0;
         return lum < 0.55 ? WHITE : TEXT_PRIMARY;
     }

    public static Color darken(Color c, double factor) {
        Color cc = toColor(c);
        factor = Math.max(0.0, Math.min(1.0, factor));
        int r = (int) Math.max(0, Math.min(255, Math.round(cc.getRed() * factor)));
        int g = (int) Math.max(0, Math.min(255, Math.round(cc.getGreen() * factor)));
        int b = (int) Math.max(0, Math.min(255, Math.round(cc.getBlue() * factor)));
        return new Color(r, g, b);
    }

    public static void applyTheme() {
        try {
            UIDefaults d = UIManager.getDefaults();

            d.put("control", toColor(BG_LIGHT));
            d.put("Panel.background", PANEL_BG);
            d.put("Button.background", PRIMARY);
            d.put("Button.foreground", contrastText(PRIMARY));
            d.put("ToggleButton.background", ACCENT);
            d.put("Table.background", WHITE);
            d.put("Table.foreground", TEXT_PRIMARY);
            d.put("Table.selectionBackground", ACCENT);
            d.put("Table.selectionForeground", WHITE);
            d.put("TextField.background", WHITE);
            d.put("TextField.foreground", TEXT_PRIMARY);
            d.put("TextArea.background", BG_LIGHT);
            d.put("Label.font", UI_FONT);
            d.put("Button.font", UI_FONT_BOLD);
            d.put("Table.font", UI_FONT);
            d.put("PopupMenu.border", new javax.swing.border.LineBorder(PANEL_BG));
            d.put("ScrollPane.background", BG_LIGHT);
            d.put("ToolTip.background", BG_LIGHT);
            d.put("ToolTip.foreground", TEXT_PRIMARY);
            d.put("ComboBox.background", WHITE);

            d.put("Button.arc", 10);
            d.put("Component.arc", 10);
            d.put("TextComponent.arc", 8);
            d.put("ProgressBar.arc", 6);

        } catch (Throwable ignored) {}
    }


    public static class FlatButton extends JButton {
        public FlatButton(String text) {
            super(text);
            setContentAreaFilled(true);
            setBorderPainted(false);
            setOpaque(true);
            setBackground(PRIMARY);
            setForeground(contrastText(PRIMARY));
            setFont(UI_FONT_BOLD);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFocusPainted(false);
            setBorder(new EmptyBorder(8, 16, 8, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            super.paintComponent(g);
            g2.dispose();
        }

        public void setPrimary(Color c) { setBackground(c); setForeground(contrastText(c)); }
    }

}

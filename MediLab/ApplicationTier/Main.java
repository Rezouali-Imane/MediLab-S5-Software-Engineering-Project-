package ApplicationTier;

import PresentationTier.LoginPage;
import javax.swing.*;


public class Main {
    public static void main(String[] args) {
        setupProfessionalTheme();
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }

    private static void setupProfessionalTheme() {
        try {
            try {
                Class<?> flat = Class.forName("com.formdev.flatlaf.FlatLightLaf");
                UIManager.setLookAndFeel((LookAndFeel) flat.getDeclaredConstructor().newInstance());
            } catch (Throwable ignore) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                }
            }

            PresentationTier.UiPalette.applyTheme();

            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("Component.focusWidth", 1);
        } catch (Exception ex) {
            System.err.println("Failed to initialize Lab Theme. Using default.");
            ex.printStackTrace();
        }
    }
}

package com.tuyensinh.admin;

import com.formdev.flatlaf.FlatLightLaf;
import com.tuyensinh.admin.ui.*;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import com.tuyensinh.util.DataSeeder;
import com.tuyensinh.util.HibernateUtil;
import javax.swing.*;
import java.awt.*;

public class MainApp {

    public static NguoiDung currentUser = null;
    public static AuthService authService = new AuthService();

    public static void main(String[] args) {
        DataSeeder.seedIfNeeded();

        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("TextField.arc", 8);
            UIManager.put("PasswordField.arc", 8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setLocationRelativeTo(null);
            loginFrame.setVisible(true);
        });
    }

    public static void openMainFrame() {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
        });
    }

    public static void logout() {
        currentUser = null;
        SwingUtilities.invokeLater(() -> {
            for (Frame f : Frame.getFrames()) {
                if (f instanceof MainFrame) {
                    f.dispose();
                }
            }
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setLocationRelativeTo(null);
            loginFrame.setVisible(true);
        });
    }
}

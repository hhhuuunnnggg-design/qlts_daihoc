package com.tuyensinh.admin.ui;

import com.tuyensinh.admin.MainApp;
import com.tuyensinh.entity.NguoiDung;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblError;

    public LoginFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Dang nhap - Quan ly Tuyen Sinh Dai Hoc 2026");
        setSize(420, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Background gradient panel
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 102, 204),
                        0, getHeight(), new Color(0, 51, 153));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("PHAN MEM TUYEN SINH");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        JLabel lblSubtitle = new JLabel("Dai Hoc 2026 - Admin");
        lblSubtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(200, 220, 255));
        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(lblSubtitle);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblUser = new JLabel("Username:");
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(lblUser, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 13));
        txtUsername.setPreferredSize(new Dimension(220, 32));
        formPanel.add(txtUsername, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setForeground(Color.WHITE);
        lblPass.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(lblPass, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 13));
        txtPassword.setPreferredSize(new Dimension(220, 32));
        txtPassword.addActionListener(e -> doLogin());
        formPanel.add(txtPassword, gbc);

        // Error label
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        lblError = new JLabel("");
        lblError.setForeground(new Color(255, 180, 180));
        lblError.setFont(new Font("Arial", Font.ITALIC, 12));
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(lblError, gbc);

        // Button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        btnLogin = new JButton("Dang Nhap");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(220, 40));
        btnLogin.setBackground(new Color(255, 193, 7));
        btnLogin.setForeground(new Color(33, 33, 33));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> doLogin());
        formPanel.add(btnLogin, gbc);

        // Info label
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JLabel lblInfo = new JLabel("(Username: admin | Password: admin123)");
        lblInfo.setForeground(new Color(180, 200, 230));
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(lblInfo, gbc);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Vui long nhap username va password!");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Dang xu ly...");

        SwingWorker<NguoiDung, Void> worker = new SwingWorker<>() {
            @Override
            protected NguoiDung doInBackground() {
                return MainApp.authService.login(username, password).orElse(null);
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                btnLogin.setText("Dang Nhap");

                NguoiDung user = null;
                try {
                    user = get();
                } catch (Exception e) {
                    lblError.setText("Loi ket noi: " + e.getMessage());
                    return;
                }

                if (user == null) {
                    lblError.setText("Sai username hoac password!");
                    txtPassword.setText("");
                    txtUsername.requestFocus();
                    return;
                }

                if (!user.isAdmin()) {
                    lblError.setText("Ban khong co quyen truy cap admin!");
                    return;
                }

                MainApp.currentUser = user;
                dispose();
                MainApp.openMainFrame();
            }
        };
        worker.execute();
    }
}

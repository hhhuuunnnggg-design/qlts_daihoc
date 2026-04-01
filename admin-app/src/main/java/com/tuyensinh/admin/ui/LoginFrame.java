package com.tuyensinh.admin.ui;

import com.tuyensinh.admin.MainApp;
import com.tuyensinh.entity.NguoiDung;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
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
        setTitle("Quan ly Tuyen Sinh Dai Hoc 2026");
        setSize(520, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // ===== Root background =====
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();

        // ===== Left branding panel (fixed 200px) =====
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(30, 60, 114),
                    0, getHeight(), new Color(13, 110, 253)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(true);
        leftPanel.setPreferredSize(new Dimension(200, 580));

        // Logo
        JPanel logoArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                // Outer glow ring
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillOval(cx - 56, cy - 56, 112, 112);
                // White ring
                g2.setColor(new Color(255, 255, 255, 60));
                g2.fillOval(cx - 48, cy - 48, 96, 96);
                g2.setColor(Color.WHITE);
                g2.fillOval(cx - 40, cy - 40, 80, 80);
                // Inner circle
                g2.setColor(new Color(13, 110, 253));
                g2.fillOval(cx - 34, cy - 34, 68, 68);

                // Cap diamond
                g2.setColor(Color.WHITE);
                Path2D.Double cap = new Path2D.Double();
                cap.moveTo(cx, cy - 16);
                cap.lineTo(cx + 16, cy);
                cap.lineTo(cx, cy + 16);
                cap.lineTo(cx - 16, cy);
                cap.closePath();
                g2.fill(cap);
                // Cap top ellipse
                g2.fill(new Ellipse2D.Double(cx - 7, cy - 18, 14, 7));
                // Tassel
                g2.setStroke(new BasicStroke(2));
                g2.setColor(new Color(255, 193, 7));
                g2.drawLine(cx + 11, cy, cx + 20, cy + 12);
                g2.fill(new Ellipse2D.Double(cx + 17, cy + 10, 7, 9));

                g2.dispose();
            }
        };
        logoArea.setOpaque(false);
        logoArea.setMaximumSize(new Dimension(120, 120));
        logoArea.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandTitle = new JLabel("TUYỂN SINH");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandTitle.setForeground(Color.WHITE);
        brandTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandSub = new JLabel("ĐẠI HỌC 2026");
        brandSub.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandSub.setForeground(new Color(200, 220, 255));
        brandSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandDesc = new JLabel("Hệ thống quản trị");
        brandDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        brandDesc.setForeground(new Color(180, 200, 240));
        brandDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(logoArea);
        leftPanel.add(Box.createVerticalStrut(18));
        leftPanel.add(brandTitle);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(brandSub);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(brandDesc);
        leftPanel.add(Box.createVerticalGlue());

        // ===== Right form panel =====
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(55, 48, 55, 48));

        JLabel rightTitle = new JLabel("ĐĂNG NHẬP");
        rightTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        rightTitle.setForeground(new Color(30, 30, 30));
        rightTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel rightSub = new JLabel("Vui lòng nhập thông tin tài khoản");
        rightSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rightSub.setForeground(new Color(110, 110, 110));
        rightSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error label
        lblError = new JLabel();
        lblError.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblError.setForeground(new Color(220, 53, 69));
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblError.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        lblError.setVisible(false);

        // Username
        JLabel lblUser = new JLabel("Tên đăng nhập");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUser.setForeground(new Color(50, 50, 50));

        txtUsername = new JTextField();
        txtUsername.putClientProperty(FlatClientProperties.STYLE, ""
            + "arc: 8;"
            + "borderColor: #e0e0e0;"
            + "focusedBorderColor: #0d6efd;"
            + "background: #fafafa;");
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername.setMargin(new Insets(10, 14, 10, 14));
        txtUsername.setCaretColor(new Color(13, 110, 253));

        // Password
        JLabel lblPass = new JLabel("Mật khẩu");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPass.setForeground(new Color(50, 50, 50));

        txtPassword = new JPasswordField();
        txtPassword.putClientProperty(FlatClientProperties.STYLE, ""
            + "arc: 8;"
            + "borderColor: #e0e0e0;"
            + "focusedBorderColor: #0d6efd;"
            + "background: #fafafa;");
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.setMargin(new Insets(10, 14, 10, 14));
        txtPassword.setCaretColor(new Color(13, 110, 253));
        txtPassword.addActionListener(e -> doLogin());

        // Button
        btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.putClientProperty(FlatClientProperties.STYLE, ""
            + "arc: 8;"
            + "background: #0d6efd;"
            + "foreground: #ffffff;"
            + "hoverBackground: #0a58ca;"
            + "pressedBackground: #084298;"
            + "font: Segoe UI bold 14;");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> doLogin());

        // Footer hint
        JLabel hint = new JLabel("admin / admin123");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(new Color(150, 150, 150));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Assemble right panel
        rightPanel.add(rightTitle);
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(rightSub);
        rightPanel.add(Box.createVerticalStrut(30));
        rightPanel.add(lblError);
        rightPanel.add(Box.createVerticalStrut(4));
        rightPanel.add(lblUser);
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(txtUsername);
        rightPanel.add(Box.createVerticalStrut(18));
        rightPanel.add(lblPass);
        rightPanel.add(Box.createVerticalStrut(6));
        rightPanel.add(txtPassword);
        rightPanel.add(Box.createVerticalStrut(24));
        rightPanel.add(btnLogin);
        rightPanel.add(Box.createVerticalStrut(16));
        rightPanel.add(hint);
        rightPanel.add(Box.createVerticalGlue());

        // ===== Assemble root =====
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        root.add(leftPanel, gbc);  // column 0

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        root.add(rightPanel, gbc); // column 1

        setContentPane(root);
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Vui lòng nhập đầy đủ thông tin!");
            lblError.setVisible(true);
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("ĐANG XỬ LÝ...");

        SwingWorker<NguoiDung, Void> worker = new SwingWorker<>() {
            @Override
            protected NguoiDung doInBackground() {
                return MainApp.authService.login(username, password).orElse(null);
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                btnLogin.setText("ĐĂNG NHẬP");

                NguoiDung user = null;
                try {
                    user = get();
                } catch (Exception e) {
                    lblError.setText("Lỗi kết nối: " + e.getMessage());
                    lblError.setVisible(true);
                    return;
                }

                if (user == null) {
                    lblError.setText("Sai tên đăng nhập hoặc mật khẩu!");
                    lblError.setVisible(true);
                    txtPassword.setText("");
                    txtUsername.requestFocus();
                    return;
                }

                if (!user.isAdmin()) {
                    lblError.setText("Bạn không có quyền truy cập!");
                    lblError.setVisible(true);
                    return;
                }

                lblError.setVisible(false);
                MainApp.currentUser = user;
                dispose();
                MainApp.openMainFrame();
            }
        };
        worker.execute();
    }
}

package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.util.Map;
import java.util.HashMap;

public class HomePanel extends JPanel {

    private MainFrame mainFrame;
    private Map<String, Color> cardColors;

    public HomePanel() {
        this(null);
    }

    public HomePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initColors();
        initUI();
    }

    private void initColors() {
        cardColors = new HashMap<>();
        cardColors.put("nguoidung", new Color(59, 130, 246));   // blue
        cardColors.put("thisinh", new Color(34, 197, 94));      // green
        cardColors.put("nganh", new Color(249, 115, 22));      // orange
        cardColors.put("diemthi", new Color(168, 85, 247));   // purple
        cardColors.put("diemcong", new Color(20, 184, 166));   // teal
        cardColors.put("nguyenvong", new Color(239, 68, 68));  // red
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(243, 244, 246));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // ===== Welcome Header =====
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(0, 4, 20, 4));

        String userName = "Admin";
        try {
            Class<?> c = Class.forName("com.tuyensinh.admin.MainApp");
            java.lang.reflect.Field f = c.getField("currentUser");
            Object user = f.get(null);
            if (user != null) {
                java.lang.reflect.Method m = user.getClass().getMethod("getUsername");
                userName = (String) m.invoke(user);
            }
        } catch (Exception ignored) {}

        JLabel welcome = new JLabel("Xin chao, " + userName + "!");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcome.setForeground(new Color(30, 30, 30));
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Chao mung ban den voi he thong quan ly tuyen sinh dai hoc 2026");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(100, 100, 100));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(welcome);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(sub);

        // ===== Stats Row =====
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);

        statsRow.add(makeStatCard("Tong so nganh", "25", new Color(59, 130, 246), "📚"));
        statsRow.add(makeStatCard("Thi sinh dang ky", "1,250", new Color(34, 197, 94), "🎓"));
        statsRow.add(makeStatCard("Nguyen vong", "3,180", new Color(249, 115, 22), "📝"));
        statsRow.add(makeStatCard("Ket qua trung tuyen", "890", new Color(20, 184, 166), "✅"));

        // ===== Module Cards Grid =====
        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 16, 16));
        gridPanel.setOpaque(false);

        gridPanel.add(makeModuleCard("Quan ly nguoi dung",
            "Them, sua, xoa tai khoan\nPhan quyen nguoi dung\nKhoa / mo tai khoan",
            "nguoidung", new Color(59, 130, 246)));

        gridPanel.add(makeModuleCard("Quan ly thi sinh",
            "Import DSSV tu file Excel\nTim kiem theo CCCD, ho ten\nPhan trang 20 dong/trang",
            "thisinh", new Color(34, 197, 94)));

        gridPanel.add(makeModuleCard("Quan ly nganh",
            "Them, sua, xoa nganh\nGan to hop mon thi\nQuan ly chi tieu",
            "nganh", new Color(249, 115, 22)));

        gridPanel.add(makeModuleCard("Quan ly diem thi",
            "Import diem 5 cot\nDiem XTT, VSAT, DGNL, THPT\nThong ke theo mon hoc",
            "diemthi", new Color(168, 85, 247)));

        gridPanel.add(makeModuleCard("Quan ly diem cong",
            "Diem chung chi, diem uu tien\nTu dong tinh tong diem\nImport danh sach",
            "diemcong", new Color(20, 184, 166)));

        gridPanel.add(makeModuleCard("Quan ly nguyen vong",
            "Xet tuyen tu dong\nCap nhat ket qua\nThong ke trung / trot",
            "nguyenvong", new Color(239, 68, 68)));

        // ===== Assemble =====
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(headerPanel);
        wrapper.add(statsRow);
        wrapper.add(Box.createVerticalStrut(20));
        wrapper.add(gridPanel);

        add(wrapper, BorderLayout.NORTH);
    }

    private JPanel makeStatCard(String title, String value, Color color, String icon) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        iconLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLbl.setForeground(Color.WHITE);
        valueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(new Color(255, 255, 255, 200));
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(iconLbl);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLbl);
        card.add(Box.createVerticalStrut(2));
        card.add(titleLbl);

        return card;
    }

    private JPanel makeModuleCard(String title, String desc, String pageKey, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hovered = Boolean.TRUE.equals(getClientProperty("hovered"));
                if (hovered) {
                    // shadow layer
                    g2.setColor(new Color(0, 0, 0, 18));
                    g2.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 14, 14);
                    // white card
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(0, 0));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Top accent bar
        JPanel accentBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(new Color(255, 255, 255));
                g2.fillRect(0, 10, getWidth(), getHeight() - 10);
                g2.dispose();
            }
        };
        accentBar.setOpaque(false);
        accentBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 8));

        // Title + icon
        JPanel topSection = new JPanel();
        topSection.setOpaque(false);
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.X_AXIS));
        topSection.setBorder(new EmptyBorder(18, 20, 0, 20));

        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(
                    accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 25));
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(accentColor);
                g2.fillOval(4, 4, 32, 32);
                g2.dispose();
            }
        };
        iconCircle.setOpaque(false);
        iconCircle.setMaximumSize(new Dimension(40, 40));

        JLabel iconDot = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(12, 14, 6, 6);
                g2.dispose();
            }
        };
        iconDot.setOpaque(false);
        iconDot.setMaximumSize(new Dimension(40, 40));

        iconCircle.add(iconDot);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(new Color(30, 30, 30));

        JLabel arrowLbl = new JLabel("→");
        arrowLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        arrowLbl.setForeground(new Color(180, 180, 180));

        topSection.add(iconCircle);
        topSection.add(Box.createHorizontalStrut(12));
        topSection.add(titleLbl);
        topSection.add(Box.createHorizontalGlue());
        topSection.add(arrowLbl);

        // Description
        JTextArea descArea = new JTextArea(desc);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descArea.setForeground(new Color(100, 100, 100));
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setOpaque(false);
        descArea.setBorder(BorderFactory.createEmptyBorder(10, 20, 16, 20));

        card.add(accentBar, BorderLayout.NORTH);
        card.add(topSection, BorderLayout.CENTER);
        card.add(descArea, BorderLayout.SOUTH);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.putClientProperty("hovered", Boolean.TRUE);
                card.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.putClientProperty("hovered", Boolean.FALSE);
                card.repaint();
            }
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (mainFrame != null) {
                    mainFrame.showPanel(pageKey);
                }
            }
        });

        return card;
    }
}

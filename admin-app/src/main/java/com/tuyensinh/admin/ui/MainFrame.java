package com.tuyensinh.admin.ui;

import com.tuyensinh.admin.MainApp;
import com.tuyensinh.admin.ui.panels.*;
import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;

public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel sidebar;
    private JLabel lblPageTitle;
    private String currentPage = "home";
    private final Color sidebarBgTop = new Color(15, 32, 70);
    private final Color sidebarBgBottom = new Color(30, 64, 144);
    private final Color navHover = new Color(255, 255, 255, 18);
    private final Color navActive = new Color(255, 255, 255, 25);
    private final Color accentBlue = new Color(59, 130, 246);
    private final Map<String, JComponent> navItems = new LinkedHashMap<>();
    private final Map<String, JComponent> pagePanels = new LinkedHashMap<>();

    public MainFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Quản lý Tuyển Sinh Đại Học 2026");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(1024, 680));

        // ===== ROOT: BorderLayout =====
        setLayout(new BorderLayout());

        // ===== SIDEBAR (Left, fixed 240px) =====
        sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, sidebarBgTop, 0, getHeight(), sidebarBgBottom);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, Integer.MAX_VALUE));
        sidebar.setOpaque(true);

        // ---- Sidebar Header ----
        JPanel sidebarHeader = new JPanel();
        sidebarHeader.setOpaque(false);
        sidebarHeader.setLayout(new BoxLayout(sidebarHeader, BoxLayout.Y_AXIS));
        sidebarHeader.setBorder(new EmptyBorder(20, 20, 16, 20));
        sidebarHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Logo icon
        JPanel logoIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                // Circle
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillOval(cx - 22, cy - 22, 44, 44);
                g2.setColor(Color.WHITE);
                g2.fillOval(cx - 18, cy - 18, 36, 36);
                // Cap
                g2.setColor(accentBlue);
                Path2D.Double cap = new Path2D.Double();
                cap.moveTo(cx, cy - 10);
                cap.lineTo(cx + 10, cy);
                cap.lineTo(cx, cy + 10);
                cap.lineTo(cx - 10, cy);
                cap.closePath();
                g2.fill(cap);
                g2.fill(new Ellipse2D.Double(cx - 5, cy - 12, 10, 5));
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(255, 193, 7));
                g2.drawLine(cx + 7, cy, cx + 13, cy + 7);
                g2.dispose();
            }
        };
        logoIcon.setOpaque(false);
        logoIcon.setMaximumSize(new Dimension(44, 44));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoIcon.setPreferredSize(new Dimension(44, 44));
        logoIcon.setMinimumSize(new Dimension(44, 44));

        JLabel sidebarTitle = new JLabel("TUYỂN SINH");
        sidebarTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sidebarTitle.setForeground(Color.WHITE);
        sidebarTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sidebarSub = new JLabel("ĐẠI HỌC 2026");
        sidebarSub.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sidebarSub.setForeground(new Color(180, 200, 255));
        sidebarSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebarHeader.add(logoIcon);
        sidebarHeader.add(Box.createVerticalStrut(8));
        sidebarHeader.add(sidebarTitle);
        sidebarHeader.add(sidebarSub);

        // ---- Divider ----
        JSeparator sidebarSep = new JSeparator();
        sidebarSep.setForeground(new Color(255, 255, 255, 30));
        sidebarSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebarSep.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---- Nav section labels ----

        JLabel navLabelMain = makeNavSectionLabel("QUẢN LÝ");
        JLabel navLabelSys = makeNavSectionLabel("HỆ THỐNG");

        // ---- Navigation Items ----
        // Main nav
        addNavItem("Trang chủ", "home", null);
        addNavItem("Người dùng", "nguoidung", null);
        addNavItem("Thí sinh", "thisinh", null);
        addNavSpacer(12);
        addNavItem("Ngành", "nganh", null);
        addNavItem("Tổ hợp", "tohop", null);
        addNavItem("Ngành - Tổ hợp", "nganhtohop", null);
        addNavItem("Mã xét tuyển", "ma_xettuyen", null);
        addNavItem("Điểm thi", "diemthi", null);
        addNavItem("Điểm cộng", "diemcong", null);
        addNavSpacer(12);
        addNavItem("Nguyện vọng", "nguyenvong", null);
        addNavItem("Bảng quy đổi", "bangquydoi", null);

        // ---- Sidebar bottom: user info + logout ----
        JPanel sidebarBottom = new JPanel();
        sidebarBottom.setOpaque(false);
        sidebarBottom.setLayout(new BoxLayout(sidebarBottom, BoxLayout.Y_AXIS));
        sidebarBottom.setBorder(new EmptyBorder(0, 12, 16, 12));
        sidebarBottom.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator bottomSep = new JSeparator();
        bottomSep.setForeground(new Color(255, 255, 255, 30));
        bottomSep.setMaximumSize(new Dimension(216, 1));
        bottomSep.setAlignmentX(Component.CENTER_ALIGNMENT);

        // User info panel
        JPanel userInfo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        userInfo.setOpaque(false);
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setBorder(new EmptyBorder(10, 10, 10, 10));
        userInfo.setMaximumSize(new Dimension(216, 70));
        userInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        String userName = MainApp.currentUser != null ? MainApp.currentUser.getUsername() : "Admin";
        String userRole = MainApp.currentUser != null && MainApp.currentUser.getVaiTro() != null
                ? MainApp.currentUser.getVaiTro().getTenVaitro()
                : "Quản Trị";

        JLabel userAvatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.setColor(accentBlue);
                g2.fillOval(cx - 16, cy - 16, 32, 32);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                String initial = userName.length() > 0 ? userName.substring(0, 1).toUpperCase() : "A";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, cx - fm.stringWidth(initial) / 2, cy + fm.getAscent() / 2 - 2);
                g2.dispose();
            }
        };
        userAvatar.setMaximumSize(new Dimension(32, 32));
        userAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userNameLbl = new JLabel(userName);
        userNameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userNameLbl.setForeground(Color.WHITE);
        userNameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userRoleLbl = new JLabel(userRole);
        userRoleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        userRoleLbl.setForeground(new Color(180, 200, 240));
        userRoleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        userInfo.add(userAvatar);
        userInfo.add(Box.createVerticalStrut(4));
        userInfo.add(userNameLbl);
        userInfo.add(userRoleLbl);

        // Logout button
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setIconTextGap(6);
        btnLogout.setHorizontalTextPosition(SwingConstants.CENTER);
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(216, 36));
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLogout.setForeground(new Color(220, 180, 180));
        btnLogout.setFocusPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(MainFrame.this,
                    "Bạn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                MainApp.logout();
            }
        });

        sidebarBottom.add(bottomSep);
        sidebarBottom.add(Box.createVerticalStrut(12));
        sidebarBottom.add(userInfo);
        sidebarBottom.add(Box.createVerticalStrut(10));
        sidebarBottom.add(btnLogout);

        // ---- Assemble sidebar ----
        sidebar.add(sidebarHeader);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(sidebarSep);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navLabelMain);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(navItems.get("home"));
        sidebar.add(navItems.get("nguoidung"));
        sidebar.add(navItems.get("thisinh"));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(navItems.get("nganh"));
        sidebar.add(navItems.get("tohop"));
        sidebar.add(navItems.get("nganhtohop"));
        sidebar.add(navItems.get("ma_xettuyen"));
        sidebar.add(navItems.get("diemthi"));
        sidebar.add(navItems.get("diemcong"));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(navItems.get("nguyenvong"));
        sidebar.add(navItems.get("bangquydoi"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navLabelSys);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(sidebarBottom);

        // ===== TOP BAR =====
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(248, 249, 250));
        topBar.setBorder(new EmptyBorder(0, 0, 1, 0));
        topBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 60));

        // Left: page title
        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        topLeft.setOpaque(false);
        topLeft.setBorder(null);
        JLabel lblMenuToggle = new JLabel("☰");
        lblMenuToggle.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18)); // 🔥 đổi font
        lblMenuToggle.setForeground(new Color(80, 80, 80));
        lblMenuToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblMenuToggle.setBorder(new EmptyBorder(0, 0, 0, 8));

        lblPageTitle = new JLabel("Trang chủ");
        lblPageTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPageTitle.setForeground(new Color(30, 30, 30));

        topLeft.add(lblMenuToggle);
        topLeft.add(lblPageTitle);

        // Right: actions
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        topRight.setOpaque(false);
        topRight.setBorder(null);

        JButton btnNotify = new JButton() {
            {
                setPreferredSize(new Dimension(36, 36));
                setFocusPainted(false);
                setContentAreaFilled(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setToolTipText("Thông báo");
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(100, 100, 100));
                // Bell shape
                g2.fillOval(8, 12, 20, 18);
                g2.fill(new Ellipse2D.Double(10, 26, 16, 6));
                g2.dispose();
            }
        };

        JButton btnProfile = new JButton() {
            {
                setPreferredSize(new Dimension(36, 36));
                setFocusPainted(false);
                setContentAreaFilled(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setToolTipText(userName);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentBlue);
                g2.fillOval(4, 4, 28, 28);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String initial = userName.length() > 0 ? userName.substring(0, 1).toUpperCase() : "A";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, 18 - fm.stringWidth(initial) / 2, 22);
                g2.dispose();
            }
        };

        topRight.add(btnNotify);
        topRight.add(btnProfile);
        topRight.setBorder(new EmptyBorder(0, 0, 0, 16));

        topBar.add(topLeft, BorderLayout.WEST);
        topBar.add(topRight, BorderLayout.EAST);

        // ===== CONTENT PANEL (CardLayout) =====
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(243, 244, 246));

        // Add all panels
        JComponent homePanel = new HomePanel(this);
        JComponent nguoiDungPanel = new NguoiDungPanel(this);
        JComponent thiSinhPanel = new ThiSinhPanel(this);
        JComponent thiSinhImportPanel = new ThiSinhImportPanel(this);
        JComponent nganhPanel = new NganhPanel(this);
        JComponent toHopPanel = new ToHopPanel(this);
        JComponent nganhToHopPanel = new NganhToHopPanel(this);
        JComponent maXetTuyenPanel = new MaXetTuyenPanel(this);
        JComponent diemThiPanel = new DiemThiPanel(this);
        JComponent diemImportPanel = new DiemImportPanel(this);
        JComponent diemThongKePanel = new DiemThongKePanel(this);
        JComponent diemCongPanel = new DiemCongPanel(this);
        JComponent nguyenVongPanel = new NguyenVongPanel(this);
        JComponent xetTuyenPanel = new XetTuyenPanel(this);
        JComponent bangQuyDoiPanel = new BangQuyDoiPanel(this);

        contentPanel.add(homePanel, "home");
        contentPanel.add(nguoiDungPanel, "nguoidung");
        contentPanel.add(thiSinhPanel, "thisinh");
        contentPanel.add(thiSinhImportPanel, "thisinh_import");
        contentPanel.add(nganhPanel, "nganh");
        contentPanel.add(toHopPanel, "tohop");
        contentPanel.add(nganhToHopPanel, "nganhtohop");
        contentPanel.add(maXetTuyenPanel, "ma_xettuyen");
        contentPanel.add(diemThiPanel, "diemthi");
        contentPanel.add(diemImportPanel, "diem_import");
        contentPanel.add(diemThongKePanel, "diem_thongke");
        contentPanel.add(diemCongPanel, "diemcong");
        contentPanel.add(nguyenVongPanel, "nguyenvong");
        contentPanel.add(xetTuyenPanel, "xettuyen");
        contentPanel.add(bangQuyDoiPanel, "bangquydoi");

        pagePanels.put("home", homePanel);
        pagePanels.put("nguoidung", nguoiDungPanel);
        pagePanels.put("thisinh", thiSinhPanel);
        pagePanels.put("thisinh_import", thiSinhImportPanel);
        pagePanels.put("nganh", nganhPanel);
        pagePanels.put("tohop", toHopPanel);
        pagePanels.put("nganhtohop", nganhToHopPanel);
        pagePanels.put("ma_xettuyen", maXetTuyenPanel);
        pagePanels.put("diemthi", diemThiPanel);
        pagePanels.put("diem_import", diemImportPanel);
        pagePanels.put("diem_thongke", diemThongKePanel);
        pagePanels.put("diemcong", diemCongPanel);
        pagePanels.put("nguyenvong", nguyenVongPanel);
        pagePanels.put("xettuyen", xetTuyenPanel);
        pagePanels.put("bangquydoi", bangQuyDoiPanel);

        // ===== WRAP content in a padding container =====
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.setBorder(new EmptyBorder(0, 0, 0, 0));
        contentWrapper.add(contentPanel, BorderLayout.CENTER);

        // ===== MAIN BODY (topBar + content) =====
        JPanel body = new JPanel(new BorderLayout());
        body.add(topBar, BorderLayout.NORTH);
        body.add(contentWrapper, BorderLayout.CENTER);

        // ===== ASSEMBLE =====
        add(sidebar, BorderLayout.WEST);
        add(body, BorderLayout.CENTER);

        // Select default
        setActiveNav("home");
        showPanel("home");
    }

    private JLabel makeNavSectionLabel(String text) {
        JLabel label = new JLabel(text.toUpperCase()); // in hoa
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(200, 210, 230)); // màu nhạt hơn, dịu mắt
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(12, 16, 4, 0)); // padding trên & trái
        return label;
    }

    private void addNavItem(String text, String pageKey, String icon) {
        JPanel navItem = new JPanel() {
            private boolean hovered = false;
            private boolean active = false;

            {
                setOpaque(false);
                setMaximumSize(new Dimension(240, 42));
                setAlignmentX(Component.LEFT_ALIGNMENT);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                setBorder(new EmptyBorder(0, 16, 0, 16));

                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hovered = false;
                        repaint();
                    }

                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        setActiveNav(pageKey);
                        showPanel(pageKey);
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (active) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 20));
                    g2.fillRoundRect(0, 4, getWidth(), 34, 6, 6);
                    // Accent bar on left
                    g2.setColor(accentBlue);
                    g2.fillRoundRect(0, 4, 3, 34, 2, 2);
                    g2.dispose();
                } else if (hovered) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 12));
                    g2.fillRoundRect(0, 4, getWidth(), 34, 6, 6);
                    g2.dispose();
                }
            }

            public void setActive(boolean a) {
                this.active = a;
                repaint();
            }
        };

        // Icon placeholder (colored dot for now)
        JPanel iconArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cy = getHeight() / 2; // chính giữa trục Y
                g2.setColor(new Color(255, 255, 255, 160));
                g2.fillOval(2, cy - 3, 6, 6); // 6 là đường kính, nên -3 để căn giữa
                g2.dispose();
            }
        };
        iconArea.setOpaque(false);

        iconArea.setMaximumSize(new Dimension(16, 42));
        iconArea.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel textLbl = new JLabel(text);
        textLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textLbl.setForeground(Color.WHITE);
        textLbl.setBorder(new EmptyBorder(0, 1, 0, 0));
        textLbl.setAlignmentY(Component.CENTER_ALIGNMENT);

        navItem.setLayout(new BoxLayout(navItem, BoxLayout.X_AXIS));
        navItem.add(iconArea);
        navItem.add(textLbl);
        navItem.setBorder(new EmptyBorder(0, 0, 0, 30));
        navItems.put(pageKey, navItem);
    }

    private void addNavSpacer(int height) {
        navItems.put("__spacer__" + navItems.size(), (JComponent) Box.createVerticalStrut(height));
    }

    private void setActiveNav(String pageKey) {
        for (Map.Entry<String, JComponent> e : navItems.entrySet()) {
            if (e.getValue() instanceof JPanel) {
                ((JPanel) e.getValue()).putClientProperty("active", e.getKey().equals(pageKey));
                repaintSidebarNav(e.getValue(), e.getKey().equals(pageKey));
            }
        }
    }

    private void repaintSidebarNav(JComponent comp, boolean active) {
        if (comp instanceof JPanel) {
            for (Component c : ((JPanel) comp).getComponents()) {
                if (c instanceof JPanel) {
                    ((JPanel) c).repaint();
                }
                if (c instanceof JLabel) {
                    JLabel lbl = (JLabel) c;
                    if (active) {
                        lbl.setForeground(Color.WHITE);
                        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else {
                        lbl.setForeground(new Color(220, 225, 235));
                        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    }
                }
            }
            comp.repaint();
        }
    }

    private void repaintSidebar() {
        sidebar.revalidate();
        sidebar.repaint();
    }

    public void showPanel(String name) {
        currentPage = name;
        cardLayout.show(contentPanel, name);

        JComponent panel = pagePanels.get(name);
        if (panel instanceof BasePanel) {
            try {
                ((BasePanel) panel).loadData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        String title = getPageTitle(name);
        lblPageTitle.setText(title);

        for (Map.Entry<String, JComponent> e : navItems.entrySet()) {
            if (e.getValue() instanceof JPanel) {
                repaintSidebarNav(e.getValue(), e.getKey().equals(name));
            }
        }
        repaintSidebar();
    }

    private String getPageTitle(String name) {
        switch (name) {
            case "home":
                return "Trang chủ";
            case "nguoidung":
                return "Quản lý người dùng";
            case "thisinh":
                return "Quản lý thí sinh";
            case "thisinh_import":
                return "Import thí sinh";
            case "nganh":
                return "Quản lý ngành";
            case "tohop":
                return "Quản lý tổ hợp môn";
            case "nganhtohop":
                return "Ngành - Tổ hợp";
            case "ma_xettuyen":
                return "Quản lý mã xét tuyển";
            case "diemthi":
                return "Quản lý điểm thi";
            case "diem_import":
                return "Import điểm";
            case "diem_thongke":
                return "Thống kê điểm";
            case "diemcong":
                return "Quản lý điểm cộng";
            case "nguyenvong":
                return "Quản lý nguyện vọng";
            case "xettuyen":
                return "Xét tuyển";
            case "bangquydoi":
                return "Bảng quy đổi";
            default:
                return name;
        }
    }

    public void setStatus(String msg) {
        // Future: update status bar
    }
}

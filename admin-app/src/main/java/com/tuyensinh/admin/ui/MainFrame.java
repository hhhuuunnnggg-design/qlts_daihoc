package com.tuyensinh.admin.ui;

import com.tuyensinh.admin.MainApp;
import com.tuyensinh.admin.ui.panels.*;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel lblStatus;

    public MainFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Quan ly Tuyen Sinh Dai Hoc 2026 - Admin");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Menu Bar ---
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(new Font("Arial", Font.PLAIN, 13));

        // File menu
        JMenu mnFile = new JMenu("He thong");
        mnFile.setFont(new Font("Arial", Font.BOLD, 13));

        JMenuItem miLogout = new JMenuItem("Dang xuat");
        miLogout.setIcon(new ImageIcon(getClass().getResource("/icons/logout.png")));
        miLogout.setAccelerator(KeyStroke.getKeyStroke("ctrl L"));
        miLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Ban muon dang xuat?", "Xac nhan", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                MainApp.logout();
            }
        });

        JMenuItem miExit = new JMenuItem("Thoat");
        miExit.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        miExit.addActionListener(e -> System.exit(0));

        mnFile.add(miLogout);
        mnFile.addSeparator();
        mnFile.add(miExit);

        // Management menus
        JMenu mnNguoiDung = new JMenu("Nguoi dung");
        mnNguoiDung.setFont(new Font("Arial", Font.BOLD, 13));
        JMenuItem miQLND = new JMenuItem("Quan ly nguoi dung");
        miQLND.addActionListener(e -> showPanel("nguoidung"));
        mnNguoiDung.add(miQLND);

        JMenu mnThiSinh = new JMenu("Thi sinh");
        mnThiSinh.setFont(new Font("Arial", Font.BOLD, 13));
        JMenuItem miQLTS = new JMenuItem("Quan ly thi sinh");
        miQLTS.addActionListener(e -> showPanel("thisinh"));
        JMenuItem miImportTS = new JMenuItem("Import thi sinh");
        miImportTS.addActionListener(e -> showPanel("thisinh_import"));
        mnThiSinh.add(miQLTS);
        mnThiSinh.add(miImportTS);

        JMenu mnNganh = new JMenu("Nganh & To hop");
        mnNganh.setFont(new Font("Arial", Font.BOLD, 13));
        JMenuItem miQLN = new JMenuItem("Quan ly nganh");
        miQLN.addActionListener(e -> showPanel("nganh"));
        JMenuItem miQLTH = new JMenuItem("Quan ly to hop mon");
        miQLTH.addActionListener(e -> showPanel("tohop"));
        JMenuItem miQLNTH = new JMenuItem("Nganh - To hop");
        miQLNTH.addActionListener(e -> showPanel("nganhtohop"));
        mnNganh.add(miQLN);
        mnNganh.add(miQLTH);
        mnNganh.add(miQLNTH);

        JMenu mnDiem = new JMenu("Diem thi");
        mnDiem.setFont(new Font("Arial", Font.BOLD, 13));
        JMenuItem miQLDiem = new JMenuItem("Quan ly diem");
        miQLDiem.addActionListener(e -> showPanel("diemthi"));
        JMenuItem miImportDiem = new JMenuItem("Import diem");
        miImportDiem.addActionListener(e -> showPanel("diem_import"));
        JMenuItem miThongKe = new JMenuItem("Thong ke diem");
        miThongKe.addActionListener(e -> showPanel("diem_thongke"));
        mnDiem.add(miQLDiem);
        mnDiem.add(miImportDiem);
        mnDiem.add(miThongKe);

        JMenu mnDiemCong = new JMenu("Diem cong");
        mnDiemCong.setFont(new Font("Arial", Font.BOLD, 13));
        JMenuItem miQLDC = new JMenuItem("Quan ly diem cong");
        miQLDC.addActionListener(e -> showPanel("diemcong"));
        mnDiemCong.add(miQLDC);

        JMenu mnNguyenVong = new JMenu("Nguyen vong");
        mnNguyenVong.setFont(new Font("Arial", Font.BOLD, 13));
        JMenuItem miQLNV = new JMenuItem("Quan ly nguyen vong");
        miQLNV.addActionListener(e -> showPanel("nguyenvong"));
        JMenuItem miXetTuyen = new JMenuItem("Xet tuyen");
        miXetTuyen.addActionListener(e -> showPanel("xettuyen"));
        mnNguyenVong.add(miQLNV);
        mnNguyenVong.add(miXetTuyen);

        JMenu mnBangQuyDoi = new JMenu("Bang quy doi");
        mnBangQuyDoi.setFont(new Font("Arial", Font.BOLD, 13));
        JMenuItem miBQD = new JMenuItem("Quan ly bang quy doi");
        miBQD.addActionListener(e -> showPanel("bangquydoi"));
        mnBangQuyDoi.add(miBQD);

        menuBar.add(mnFile);
        menuBar.add(mnNguoiDung);
        menuBar.add(mnThiSinh);
        menuBar.add(mnNganh);
        menuBar.add(mnDiem);
        menuBar.add(mnDiemCong);
        menuBar.add(mnNguyenVong);
        menuBar.add(mnBangQuyDoi);

        setJMenuBar(menuBar);

        // --- Toolbar ---
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton(new ImageIcon(getClass().getResource("/icons/home.png"))) {{
            setToolTipText("Trang chu");
            addActionListener(e -> showPanel("home"));
        }});
        toolbar.addSeparator();

        JButton btnND = new JButton("Nguoi dung");
        btnND.addActionListener(e -> showPanel("nguoidung"));
        toolbar.add(btnND);

        JButton btnTS = new JButton("Thi sinh");
        btnTS.addActionListener(e -> showPanel("thisinh"));
        toolbar.add(btnTS);

        JButton btnNganh = new JButton("Nganh");
        btnNganh.addActionListener(e -> showPanel("nganh"));
        toolbar.add(btnNganh);

        JButton btnDiem = new JButton("Diem");
        btnDiem.addActionListener(e -> showPanel("diemthi"));
        toolbar.add(btnDiem);

        JButton btnNV = new JButton("Nguyen Vong");
        btnNV.addActionListener(e -> showPanel("nguyenvong"));
        toolbar.add(btnNV);

        JButton btnXet = new JButton("Xet Tuyen");
        btnXet.addActionListener(e -> showPanel("xettuyen"));
        toolbar.add(btnXet);

        add(toolbar, BorderLayout.NORTH);

        // --- Status Bar ---
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        lblStatus = new JLabel("  Ready");
        lblStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        JLabel lblUser = new JLabel("  User: " + MainApp.currentUser.getUsername() + " (" + MainApp.currentUser.getVaiTro().getTenVaitro() + ")  ");
        lblUser.setFont(new Font("Arial", Font.PLAIN, 12));
        lblUser.setForeground(new Color(0, 102, 204));
        statusBar.add(lblStatus, BorderLayout.WEST);
        statusBar.add(lblUser, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        // --- Content Panel (CardLayout) ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Add all panels
        contentPanel.add(new HomePanel(this), "home");
        contentPanel.add(new NguoiDungPanel(this), "nguoidung");
        contentPanel.add(new ThiSinhPanel(this), "thisinh");
        contentPanel.add(new ThiSinhImportPanel(this), "thisinh_import");
        contentPanel.add(new NganhPanel(this), "nganh");
        contentPanel.add(new ToHopPanel(this), "tohop");
        contentPanel.add(new NganhToHopPanel(this), "nganhtohop");
        contentPanel.add(new DiemThiPanel(this), "diemthi");
        contentPanel.add(new DiemImportPanel(this), "diem_import");
        contentPanel.add(new DiemThongKePanel(this), "diem_thongke");
        contentPanel.add(new DiemCongPanel(this), "diemcong");
        contentPanel.add(new NguyenVongPanel(this), "nguyenvong");
        contentPanel.add(new XetTuyenPanel(this), "xettuyen");
        contentPanel.add(new BangQuyDoiPanel(this), "bangquydoi");

        add(contentPanel, BorderLayout.CENTER);

        showPanel("home");
    }

    public void showPanel(String name) {
        cardLayout.show(contentPanel, name);
        lblStatus.setText("  " + name);
    }

    public void setStatus(String msg) {
        lblStatus.setText("  " + msg);
    }
}

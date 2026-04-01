package com.tuyensinh.admin.ui;

import javax.swing.*;
import java.awt.*;

public class HomePanel extends JPanel {

    public HomePanel(MainFrame mainFrame) {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 102, 204),
                        getWidth(), 0, new Color(0, 51, 153));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 120));
        header.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 30));

        JLabel lblTitle = new JLabel("HE THONG QUAN LY TUYEN SINH DAI HOC 2026");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle);

        JLabel lblSub = new JLabel("Ung dung quan ly cho Admin");
        lblSub.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSub.setForeground(new Color(200, 220, 255));
        header.add(lblSub);

        add(header, BorderLayout.NORTH);

        // Grid of info cards
        JPanel grid = new JPanel(new GridLayout(2, 3, 20, 20));
        grid.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        grid.setBackground(Color.WHITE);

        String[][] cards = {
            {"QUAN LY NGUOI DUNG", "Them, sua, xoa, phan quyen\nKhoa/mo tai khoan\nDoi mat khau", "color: #3498db"},
            {"QUAN LY THI SINH", "Import DSSV tu Excel\nTim kiem theo CCCD/Ho ten\nPhan trang 20 dong/trang", "color: #2ecc71"},
            {"QUAN LY NGANH & TO HOP", "Them, sua, xoa nganh\nQuan ly to hop mon\nGan to hop cho nganh", "color: #e67e22"},
            {"QUAN LY DIEM THI", "Import diem 5 cot diem\nXTT, VHAT, DGNL, THPT, NK\nThong ke diem theo mon", "color: #9b59b6"},
            {"QUAN LY DIEM CONG", "Diem chung chi, diem UT\nTu dong tinh tong diem\nImport danh sach", "color: #1abc9c"},
            {"QUAN LY NGUYEN VONG", "Xet tuyen tu dong\nCap nhat ket qua\nThong ke trung/trot", "color: #e74c3c"},
        };

        for (String[] card : cards) {
            JPanel cardPanel = new JPanel(new BorderLayout());
            cardPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            cardPanel.setBackground(Color.WHITE);

            JPanel titleBar = new JPanel();
            titleBar.setBackground(new Color(240, 240, 240));
            titleBar.add(new JLabel(card[0], SwingConstants.CENTER) {{
                setFont(new Font("Arial", Font.BOLD, 13));
                setForeground(new Color(50, 50, 50));
            }});

            JTextArea desc = new JTextArea(card[1]);
            desc.setFont(new Font("Arial", Font.PLAIN, 12));
            desc.setEditable(false);
            desc.setLineWrap(true);
            desc.setWrapStyleWord(true);
            desc.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            desc.setBackground(Color.WHITE);

            cardPanel.add(titleBar, BorderLayout.NORTH);
            cardPanel.add(desc, BorderLayout.CENTER);
            grid.add(cardPanel);
        }

        add(grid, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel();
        footer.setBackground(new Color(240, 240, 240));
        footer.add(new JLabel("Phan mem quan ly tuyen sinh dai hoc 2026 - Swing + Hibernate + MySQL 8.0"));
        add(footer, BorderLayout.SOUTH);
    }
}

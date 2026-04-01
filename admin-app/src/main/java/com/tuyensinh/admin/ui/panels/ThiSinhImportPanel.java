package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ThiSinhImportPanel extends JPanel {

    private MainFrame mainFrame;
    private ThiSinhService service = new ThiSinhService();
    private JTextArea taLog;
    private JButton btnSelect, btnImport;
    private JLabel lblFile;
    private JFileChooser fileChooser;

    public ThiSinhImportPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel("Huong dan import thi sinh tu file Excel (.xlsx):"));
        add(header, BorderLayout.NORTH);

        // Info
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Thong tin import"));
        infoPanel.add(new JLabel("File:"));
        lblFile = new JLabel("Chua chon file...");
        lblFile.setForeground(Color.BLUE);
        infoPanel.add(lblFile);

        btnSelect = new JButton("Chon file Excel");
        btnSelect.addActionListener(e -> selectFile());
        infoPanel.add(btnSelect);

        btnImport = new JButton("Import du lieu");
        btnImport.setEnabled(false);
        btnImport.addActionListener(e -> importData());
        infoPanel.add(btnImport);

        // Template info
        JPanel templatePanel = new JPanel(new BorderLayout());
        templatePanel.setBorder(BorderFactory.createTitledBorder("Mau cot trong file Excel"));
        JTextArea taTemplate = new JTextArea(
            "STT | CCCD(*) | SoBaoDanh | Ho(*) | Ten(*) | NgaySinh | GioiTinh | DienThoai | Email | NoiSinh | DT_UT | KV_UT\n" +
            "1   | 001234567890 | TS0001 | Nguyen Van | An | 2005-05-10 | Nam | 0912345001 | an@email.com | Ha Noi | DT01 | KV3\n" +
            "2   | 001234567891 | TS0002 | Tran Thi | Binh | 2005-07-15 | Nu | 0912345002 | binh@email.com | HCM | DT02 | KV2\n\n" +
            "Ghi chu:\n" +
            "- Cot (*) la bat buoc\n" +
            "- NgaySinh dinh dang: yyyy-MM-dd\n" +
            "- GioiTinh: Nam / Nu\n" +
            "- DT_UT: DT01, DT02, DT03, DT04, DT05 (trong bang doi tuong uu tien)\n" +
            "- KV_UT: KV1, KV2, KV2NT, KV3 (trong bang khu vuc uu tien)\n" +
            "- File Excel chi can co dong tieu de + du lieu (khong can cot STT)"
        );
        taTemplate.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taTemplate.setEditable(false);
        taTemplate.setBackground(new Color(245, 245, 245));
        templatePanel.add(new JScrollPane(taTemplate), BorderLayout.CENTER);

        // Log area
        taLog = new JTextArea();
        taLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taLog.setEditable(false);
        taLog.setBackground(new Color(30, 30, 30));
        taLog.setForeground(new Color(0, 255, 0));
        JScrollPane logPane = new JScrollPane(taLog);
        logPane.setBorder(BorderFactory.createTitledBorder("Nhat ky import"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, infoPanel, templatePanel);
        split.setResizeWeight(0.3);

        JSplitPane split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, split, logPane);
        split2.setResizeWeight(0.4);

        add(split2, BorderLayout.CENTER);
    }

    private void selectFile() {
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Chon file Excel de import");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            lblFile.setText(file.getAbsolutePath());
            btnImport.setEnabled(true);
            log("Da chon file: " + file.getName());
        }
    }

    private void importData() {
        if (fileChooser == null || fileChooser.getSelectedFile() == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon file!");
            return;
        }

        btnImport.setEnabled(false);
        btnSelect.setEnabled(false);

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                publish("Bat dau import... (Can thu vien Apache POI de doc file Excel)");
                publish("Vui long dam bao Apache POI da duoc them vao project.");
                publish("Hoac chuyen file Excel thanh CSV va goi ham import CSV.");
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String line : chunks) {
                    log(line);
                }
            }

            @Override
            protected void done() {
                btnImport.setEnabled(true);
                btnSelect.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void log(String msg) {
        taLog.append("[" + java.time.LocalDateTime.now().toString().substring(11, 19) + "] " + msg + "\n");
        taLog.setCaretPosition(taLog.getDocument().getLength());
    }
}

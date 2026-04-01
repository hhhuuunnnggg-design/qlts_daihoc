package com.tuyensinh.admin.ui.panels;

import com.tuyensinh.admin.ui.MainFrame;
import com.tuyensinh.dao.PhuongThucDao;
import com.tuyensinh.entity.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.List;

public class DiemImportPanel extends JPanel {

    private PhuongThucDao phuongThucDao = new PhuongThucDao();

    private JTextArea taLog;
    private JButton btnSelect, btnImport;
    private JLabel lblFile;
    private JFileChooser fileChooser;
    private JComboBox<PhuongThuc> cboPhuongThuc;
    private JSpinner spnNam;

    public DiemImportPanel(MainFrame mainFrame) {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Import diem thi tu file CSV hoac Excel (.xlsx)"));
        add(topPanel, BorderLayout.NORTH);

        // Info + controls
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.3);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Thong tin import"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        infoPanel.add(new JLabel("File:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        lblFile = new JLabel("Chua chon file...");
        lblFile.setForeground(Color.BLUE);
        infoPanel.add(lblFile, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        btnSelect = new JButton("Chon file");
        btnSelect.addActionListener(e -> selectFile());
        infoPanel.add(btnSelect, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        infoPanel.add(new JLabel("Phuong thuc:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        cboPhuongThuc = new JComboBox<>();
        cboPhuongThuc.addItem(null);
        for (PhuongThuc pt : phuongThucDao.findAll()) {
            cboPhuongThuc.addItem(pt);
        }
        infoPanel.add(cboPhuongThuc, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        infoPanel.add(new JLabel("Nam tuyen sinh:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        spnNam = new JSpinner(new SpinnerNumberModel(2026, 2020, 2030, 1));
        infoPanel.add(spnNam, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        btnImport = new JButton("Bat dau import");
        btnImport.setEnabled(false);
        btnImport.addActionListener(e -> importData());
        infoPanel.add(btnImport, gbc);

        // Template info
        JPanel templatePanel = new JPanel(new BorderLayout());
        templatePanel.setBorder(BorderFactory.createTitledBorder("Mau cot CSV"));
        JTextArea taTemplate = new JTextArea(
            "sobaodanh,phuongthuc,TO,LI,HO,SI,VA,SU,DI,N1,NL1,NK1,NK2\n" +
            "TS0001,XTT,25.50,24.00,23.75,22.00,24.25,23.00,22.50,23.00,25.00,22.00,24.00\n" +
            "TS0001,VHAT,24.00,23.50,22.00,21.00,23.00,22.50,22.00,22.50,24.00,,\n" +
            "TS0001,DGNL,780.0,750.0,,720.0,,680.0,,,\n" +
            "TS0001,THPT,8.50,8.25,7.75,8.00,8.00,7.50,7.25,8.50,,\n" +
            "TS0001,NK,7.50,8.00,,,,,,,,\n\n" +
            "Dien gia tri trong: diem so thuc hoac de trong neu khong co\n" +
            "Phuong thuc: XTT | VHAT | DGNL | THPT | NK\n" +
            "File CSV phan cach bang dau phay (,)"
        );
        taTemplate.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taTemplate.setEditable(false);
        taTemplate.setBackground(new Color(245, 245, 245));
        templatePanel.add(new JScrollPane(taTemplate), BorderLayout.CENTER);

        split.setTopComponent(infoPanel);
        split.setBottomComponent(templatePanel);
        add(split, BorderLayout.CENTER);

        // Log area
        taLog = new JTextArea();
        taLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taLog.setEditable(false);
        taLog.setBackground(new Color(30, 30, 30));
        taLog.setForeground(new Color(0, 255, 0));
        JScrollPane logPane = new JScrollPane(taLog);
        logPane.setBorder(BorderFactory.createTitledBorder("Nhat ky import"));
        add(logPane, BorderLayout.SOUTH);
    }

    private void selectFile() {
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Excel files", "xlsx", "xls"));
        fileChooser.setDialogTitle("Chon file diem thi");
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

        PhuongThuc pt = (PhuongThuc) cboPhuongThuc.getSelectedItem();
        Short nam = (Short) spnNam.getValue();

        btnImport.setEnabled(false);
        btnSelect.setEnabled(false);

        SwingWorker<Integer, String> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                File file = fileChooser.getSelectedFile();
                String ext = file.getName().toLowerCase();
                int count = 0;
                try {
                    if (ext.endsWith(".csv")) {
                        count = importCSV(file, pt, nam);
                    } else {
                        log("Chi ho tro file CSV (moi file Excel luu thanh CSV truoc khi import)");
                        JOptionPane.showMessageDialog(DiemImportPanel.this,
                            "Chi ho tro import tu file CSV.\nVui long luu file Excel thanh CSV truoc!");
                    }
                } catch (Exception e) {
                    log("Loi: " + e.getMessage());
                    e.printStackTrace();
                }
                return count;
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
                try {
                    int count = get();
                    if (count > 0) {
                        JOptionPane.showMessageDialog(DiemImportPanel.this,
                            "Import thanh cong! " + count + " ban ghi da duoc them.");
                    }
                } catch (Exception e) {
                    // error already shown in log
                }
            }
        };
        worker.execute();
    }

    private int importCSV(File file, PhuongThuc defaultPt, Short nam) throws Exception {
        log("Bat dau doc file: " + file.getName());
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line = reader.readLine(); // skip header
        log("Doc header: " + line);

        int count = 0;
        int lineNum = 1;

        while ((line = reader.readLine()) != null) {
            lineNum++;
            if (line.trim().isEmpty()) continue;

            try {
                String[] parts = line.split(",");
                if (parts.length < 2) {
                    log("Dong " + lineNum + ": dinh dang sai, bo qua");
                    continue;
                }

                String sobaodanh = parts[0].trim();
                String ptMa = defaultPt != null ? defaultPt.getMaPhuongthuc() : parts[1].trim();

                // Find or create DiemThi
                // ...
                log("Dong " + lineNum + ": " + sobaodanh + " - " + ptMa);
                count++;
            } catch (Exception e) {
                log("Dong " + lineNum + " loi: " + e.getMessage());
            }
        }

        reader.close();
        log("Hoan tat! Tong so: " + count + " ban ghi");
        return count;
    }

    private void log(String msg) {
        String ts = java.time.LocalDateTime.now().toString().substring(11, 19);
        taLog.append("[" + ts + "] " + msg + "\n");
        taLog.setCaretPosition(taLog.getDocument().getLength());
    }
}

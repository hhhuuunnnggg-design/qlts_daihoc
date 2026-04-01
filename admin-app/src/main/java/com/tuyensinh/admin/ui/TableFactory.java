package com.tuyensinh.admin.ui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * Factory tạo JTable với styling nhất quán trong toàn bộ ứng dụng.
 */
public final class TableFactory {

    private TableFactory() {}

    /**
     * Tạo JTable với model cho sẵn.
     * Áp dụng styling chuẩn: row height, header font, selection mode.
     */
    public static JTable create(DefaultTableModel model) {
        JTable table = new JTable(model);
        applyStyle(table);
        return table;
    }

    /**
     * Tạo JTable với column headers cho sẵn.
     */
    public static JTable create(String[] columnNames) {
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        return create(model);
    }

    /**
     * Tạo JTable với column headers và row height tùy chỉnh.
     */
    public static JTable create(String[] columnNames, int rowHeight) {
        JTable table = create(columnNames);
        table.setRowHeight(rowHeight);
        return table;
    }

    /** Áp dụng styling chuẩn cho bất kỳ JTable nào */
    public static void applyStyle(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(UIConstants.TABLE_ROW_HEIGHT);
        table.setFont(UIConstants.FONT_BODY);
        table.setGridColor(new Color(220, 220, 220));

        JTableHeader header = table.getTableHeader();
        header.setFont(UIConstants.TABLE_HEADER);
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(UIConstants.TEXT_PRIMARY);
        header.setReorderingAllowed(false);

        table.setSelectionBackground(new Color(200, 220, 255));
        table.setSelectionForeground(UIConstants.TEXT_PRIMARY);
        table.setShowGrid(true);

        // Alternate row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }
                return c;
            }
        });
    }

    /** Tạo JScrollPane chứa table */
    public static JScrollPane wrap(JTable table) {
        return new JScrollPane(table,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    /** Tạo read-only model với column names */
    public static DefaultTableModel newReadOnlyModel(String... columnNames) {
        return new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
    }
}

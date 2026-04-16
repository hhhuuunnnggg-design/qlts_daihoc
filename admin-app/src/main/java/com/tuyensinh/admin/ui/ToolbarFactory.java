package com.tuyensinh.admin.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Factory tạo toolbar CRUD chuẩn cho mọi panel.
 */
public final class ToolbarFactory {

    private ToolbarFactory() {}

    /**
     * Tạo toolbar với search field + action buttons.
     * @param searchField con trỏ để gán JTextField search (sẽ được khởi tạo bên trong)
     * @param onSearch callback khi nhấn nút Tim / Enter trong search field
     * @param buttons cặp (label, action) cho mỗi nút action
     */
    public static JPanel createSearchToolbar(JTextField[] searchFieldOut,
            Runnable onSearch, ActionButton... buttons) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        toolbar.add(new JLabel("Tim kiem:"));
        JTextField txtSearch = new JTextField(20);
        if (searchFieldOut != null) searchFieldOut[0] = txtSearch;
        txtSearch.addActionListener(e -> onSearch.run());
        toolbar.add(txtSearch);

        JButton btnSearch = new JButton("Tim kiem");
        btnSearch.addActionListener(e -> onSearch.run());
        toolbar.add(btnSearch);

        if (buttons.length > 0) {
            toolbar.add(Box.createHorizontalStrut(16));
        }
        for (ActionButton btn : buttons) {
            JButton b = new JButton(btn.label);
            b.addActionListener(e -> btn.action.run());
            toolbar.add(b);
        }

        return toolbar;
    }

    /**
     * Tạo bottom bar: total label bên trái, paging bên phải.
     * @param totalLabel nhãn hiển thị tổng số bản ghi
     * @param pagingPanel panel phân trang (null nếu không cần)
     */
    public static JPanel createBottomBar(JLabel totalLabel, JPanel pagingPanel) {
        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        if (totalLabel != null) {
            totalLabel.setFont(UIConstants.FONT_SMALL);
            bottom.add(totalLabel, BorderLayout.WEST);
        }
        if (pagingPanel != null) {
            bottom.add(pagingPanel, BorderLayout.EAST);
        }
        return bottom;
    }

    /**
     * Tạo panel phân trang đơn giản với spinner + nút prev/next.
     */
    public static JPanel createPagingPanel(JSpinner spinner, Runnable onPageChange) {
        JPanel paging = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        paging.add(new JLabel("Trang:"));

        spinner.addChangeListener(e -> onPageChange.run());
        paging.add(spinner);

        JButton btnPrev = new JButton("<<");
        btnPrev.addActionListener(e -> {
            int current = (Integer) spinner.getValue();
            if (current > 1) {
                spinner.setValue(current - 1);
            }
        });
        paging.add(btnPrev);

        JButton btnNext = new JButton(">>");
        btnNext.addActionListener(e -> {
            SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
            int max = ((Number) model.getMaximum()).intValue();
            int current = (Integer) spinner.getValue();
            if (current < max) {
                spinner.setValue(current + 1);
            }
        });
        paging.add(btnNext);

        return paging;
    }

    /** Cập nhật spinner model với trang hiện tại và tổng số trang */
    public static void updatePagingSpinner(JSpinner spinner, int currentPage, int totalItems, int pageSize) {
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / pageSize));

        SpinnerNumberModel model;
        if (spinner.getModel() instanceof SpinnerNumberModel) {
            model = (SpinnerNumberModel) spinner.getModel();
            model.setMinimum(1);
            model.setMaximum(totalPages);
            model.setStepSize(1);
        } else {
            model = new SpinnerNumberModel(1, 1, totalPages, 1);
            spinner.setModel(model);
        }

        int safePage = Math.max(1, Math.min(currentPage, totalPages));
        Integer currentValue = (Integer) spinner.getValue();
        if (!currentValue.equals(safePage)) {
            spinner.setValue(safePage);
        }
    }

    // ─── Helper class ─────────────────────────────────────────────────────

    public static class ActionButton {
        public final String label;
        public final Runnable action;

        public ActionButton(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }
    }
}

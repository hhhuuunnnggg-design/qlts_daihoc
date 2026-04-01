package com.tuyensinh.admin.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Lớp nền cho tất cả panel trong ứng dụng.
 */
public abstract class BasePanel extends JPanel {

    protected final MainFrame mainFrame;

    protected BasePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(UIConstants.LAYOUT_GAP, UIConstants.LAYOUT_GAP));
        setBorder(BorderFactory.createEmptyBorder(
            UIConstants.PANEL_PADDING.top,
            UIConstants.PANEL_PADDING.left,
            UIConstants.PANEL_PADDING.bottom,
            UIConstants.PANEL_PADDING.right
        ));
        setBackground(UIConstants.CONTENT_BG);
    }

    // ─── Tiện ích thông báo ─────────────────────────────────────────

    protected void showMessage(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg);
    }

    protected void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, UIConstants.MSG_ERROR_PREFIX + msg,
            "Loi", JOptionPane.ERROR_MESSAGE);
    }

    protected void showSuccess(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg,
            "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
    }

    protected int confirmDelete(Component parent, String name) {
        return JOptionPane.showConfirmDialog(parent,
            "Ban co chac muon xoa '" + name + "'?",
            UIConstants.MSG_DELETE_TITLE,
            JOptionPane.YES_NO_OPTION);
    }

    protected void showSelectRow(Component parent) {
        showMessage(parent, UIConstants.MSG_SELECT_ROW);
    }

    protected Integer parseInt(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    protected java.math.BigDecimal parseBigDecimal(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return new java.math.BigDecimal(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    /** Tiêu đề trang */
    public abstract String getPageTitle();

    /** Build UI của subclass */
    protected abstract void initUI();

    /** Load dữ liệu */
    public abstract void loadData();
}

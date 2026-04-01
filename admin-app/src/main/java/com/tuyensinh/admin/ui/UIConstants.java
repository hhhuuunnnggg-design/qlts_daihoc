package com.tuyensinh.admin.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Tập trung toàn bộ hằng số UI: màu sắc, font, kích thước, văn bản.
 * Thay đổi một chỗ → ảnh hưởng toàn bộ ứng dụng.
 */
public final class UIConstants {

    private UIConstants() {}

    // ─── Màu sắc ─────────────────────────────────────────────────────────────

    /** Nền content chính */
    public static final Color CONTENT_BG = new Color(243, 244, 246);

    /** Nền trắng thuần */
    public static final Color WHITE      = Color.WHITE;

    /** Màu chữ chính */
    public static final Color TEXT_PRIMARY   = new Color(30, 30, 30);
    public static final Color TEXT_SECONDARY = new Color(100, 100, 100);
    public static final Color TEXT_MUTED     = new Color(150, 150, 150);

    /** Màu sidebar */
    public static final Color SIDEBAR_TOP    = new Color(15, 32, 70);
    public static final Color SIDEBAR_BOTTOM = new Color(30, 64, 144);

    /** Accent */
    public static final Color ACCENT_BLUE = new Color(59, 130, 246);

    /** Màu stat cards */
    public static final Color STAT_BLUE    = new Color(59, 130, 246);
    public static final Color STAT_GREEN   = new Color(34, 197, 94);
    public static final Color STAT_ORANGE  = new Color(249, 115, 22);
    public static final Color STAT_PURPLE  = new Color(168, 85, 247);
    public static final Color STAT_TEAL    = new Color(20, 184, 166);
    public static final Color STAT_RED     = new Color(239, 68, 68);

    // ─── Font ─────────────────────────────────────────────────────────────────

    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_TINY    = new Font("Segoe UI", Font.PLAIN, 11);

    /** Font cho table header */
    public static final Font TABLE_HEADER = new Font("Arial", Font.BOLD, 12);

    // ─── Kích thước ────────────────────────────────────────────────────────────

    public static final int WINDOW_WIDTH  = 1280;
    public static final int WINDOW_HEIGHT = 800;
    public static final int WINDOW_MIN_W   = 1024;
    public static final int WINDOW_MIN_H  = 680;
    public static final int SIDEBAR_WIDTH = 240;

    /** Padding chung cho mọi panel */
    public static final Insets PANEL_PADDING = new Insets(10, 10, 10, 10);

    /** Khoảng cách giữa các thành phần trong BorderLayout */
    public static final int LAYOUT_GAP = 5;

    /** Chiều cao dòng table */
    public static final int TABLE_ROW_HEIGHT = 25;

    // ─── Thông báo ────────────────────────────────────────────────────────────

    public static final String MSG_SELECT_ROW   = "Chon mot dong trong bang!";
    public static final String MSG_DELETE_CONFIRM = "Ban co chac muon xoa?";
    public static final String MSG_DELETE_TITLE  = "Xac nhan xoa";
    public static final String MSG_SAVE_SUCCESS   = "Luu thanh cong!";
    public static final String MSG_DELETE_SUCCESS = "Xoa thanh cong!";
    public static final String MSG_UPDATE_SUCCESS = "Cap nhat thanh cong!";
    public static final String MSG_REQUIRED_FIELD = "Vui long dien day du thong tin bat buoc!";
    public static final String MSG_ERROR_PREFIX   = "Loi: ";

    // ─── Tiêu đề trang ────────────────────────────────────────────────────────

    public static final String PAGE_HOME       = "Trang chu";
    public static final String PAGE_NGUOI_DUNG = "Quan ly nguoi dung";
    public static final String PAGE_THI_SINH   = "Quan ly thi sinh";
    public static final String PAGE_NGANH      = "Quan ly nganh";
    public static final String PAGE_TO_HOP     = "Quan ly to hop mon";
    public static final String PAGE_NGANH_TO_HOP = "Nganh - To hop";
    public static final String PAGE_DIEM_THI   = "Quan ly diem thi";
    public static final String PAGE_DIEM_IMPORT = "Import diem";
    public static final String PAGE_DIEM_THONG_KE = "Thong ke diem";
    public static final String PAGE_DIEM_CONG  = "Quan ly diem cong";
    public static final String PAGE_NGUYEN_VONG = "Quan ly nguyen vong";
    public static final String PAGE_XET_TUYEN  = "Xet tuyen";
    public static final String PAGE_BANG_QUY_DOI = "Bang quy doi";
}

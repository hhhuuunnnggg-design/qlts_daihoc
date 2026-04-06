-- ============================================================
-- DATABASE: xettuyen2026
-- Mo ta: Phan mem quan ly tuyen sinh dai hoc 2026
-- MySQL 8.0+
-- ============================================================

DROP DATABASE IF EXISTS xettuyen2026;
CREATE DATABASE xettuyen2026 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xettuyen2026;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------
-- 1. Bang vai tro (quyen)
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_vaitro;
CREATE TABLE xt_vaitro (
    vaitro_id         TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ma_vaitro         VARCHAR(20)     NOT NULL,
    ten_vaitro        VARCHAR(50)     NOT NULL,
    mo_ta             VARCHAR(255)    NULL,
    PRIMARY KEY (vaitro_id),
    UNIQUE KEY uk_vaitro_ma (ma_vaitro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 2. Bang nguoi dung
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_nguoidung;
CREATE TABLE xt_nguoidung (
    nguoidung_id      INT             NOT NULL AUTO_INCREMENT,
    username          VARCHAR(100)    NOT NULL,
    password_hash     VARCHAR(255)    NOT NULL,
    ho_ten            VARCHAR(150)    NULL,
    email             VARCHAR(100)    NULL,
    vaitro_id         TINYINT UNSIGNED NOT NULL,
    is_active         TINYINT(1)      NOT NULL DEFAULT 1,
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (nguoidung_id),
    UNIQUE KEY uk_nguoidung_username (username),
    UNIQUE KEY uk_nguoidung_email (email),
    KEY idx_nguoidung_vaitro (vaitro_id),
    CONSTRAINT fk_nguoidung_vaitro
        FOREIGN KEY (vaitro_id) REFERENCES xt_vaitro(vaitro_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 3. Bang doi tuong uu tien
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_doituong_uutien;
CREATE TABLE xt_doituong_uutien (
    doituong_id       INT             NOT NULL AUTO_INCREMENT,
    ma_doituong        VARCHAR(20)     NOT NULL,
    ten_doituong       VARCHAR(100)    NOT NULL,
    muc_diem           DECIMAL(4,2)    NOT NULL DEFAULT 0.00,
    ghi_chu            VARCHAR(255)    NULL,
    PRIMARY KEY (doituong_id),
    UNIQUE KEY uk_doituong_ma (ma_doituong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 4. Bang khu vuc uu tien
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_khuvuc_uutien;
CREATE TABLE xt_khuvuc_uutien (
    khuvuc_id          INT             NOT NULL AUTO_INCREMENT,
    ma_khuvuc          VARCHAR(20)     NOT NULL,
    ten_khuvuc         VARCHAR(100)    NOT NULL,
    muc_diem           DECIMAL(4,2)    NOT NULL DEFAULT 0.00,
    ghi_chu            VARCHAR(255)    NULL,
    PRIMARY KEY (khuvuc_id),
    UNIQUE KEY uk_khuvuc_ma (ma_khuvuc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 5. Bang thi sinh
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_thisinh;
CREATE TABLE xt_thisinh (
    thisinh_id         INT             NOT NULL AUTO_INCREMENT,
    nguoidung_id       INT             NULL,
    cccd               VARCHAR(20)     NOT NULL,
    sobaodanh          VARCHAR(45)     NULL,
    ho                 VARCHAR(100)    NOT NULL,
    ten                VARCHAR(100)    NOT NULL,
    ngay_sinh          DATE            NULL,
    gioi_tinh          VARCHAR(10)     NULL,
    dien_thoai         VARCHAR(20)     NULL,
    email              VARCHAR(100)    NULL,
    noi_sinh           VARCHAR(100)    NULL,
    doituong_id        INT             NULL,
    khuvuc_id          INT             NULL,
    updated_at         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (thisinh_id),
    UNIQUE KEY uk_thisinh_cccd (cccd),
    UNIQUE KEY uk_thisinh_sobaodanh (sobaodanh),
    UNIQUE KEY uk_thisinh_nguoidung (nguoidung_id),
    KEY idx_thisinh_doituong (doituong_id),
    KEY idx_thisinh_khuvuc (khuvuc_id),
    CONSTRAINT fk_thisinh_nguoidung
        FOREIGN KEY (nguoidung_id) REFERENCES xt_nguoidung(nguoidung_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_thisinh_doituong
        FOREIGN KEY (doituong_id) REFERENCES xt_doituong_uutien(doituong_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_thisinh_khuvuc
        FOREIGN KEY (khuvuc_id) REFERENCES xt_khuvuc_uutien(khuvuc_id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 6. Bang phuong thuc xet tuyen (5 cot diem theo yeu cau)
-- ----------------------------------------------------------
-- Phuong thuc:
--   1. XTT  = Xet tuyen thang
--   2. VSAT = Xet diem VSAT (danh gia hoc ba / transcript)
--   3. DGNL = Xet diem dau ra nang luc (aptitude test)
--   4. THPT = Xet diem thi THPT
--   5. NK   = Xet diem nang khieu
--        NK loai 1-2: Giao duc Mam non
--        NK loai 3-4: My thuat
--        NK loai 5-6: Am nhac
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_phuongthuc;
CREATE TABLE xt_phuongthuc (
    phuongthuc_id      TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ma_phuongthuc      VARCHAR(20)     NOT NULL,
    ten_phuongthuc     VARCHAR(100)    NOT NULL,
    thang_diem         DECIMAL(6,2)    NOT NULL DEFAULT 30.00,
    is_active          TINYINT(1)      NOT NULL DEFAULT 1,
    PRIMARY KEY (phuongthuc_id),
    UNIQUE KEY uk_phuongthuc_ma (ma_phuongthuc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 7. Bang mon hoc
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_mon;
CREATE TABLE xt_mon (
    mon_id             INT             NOT NULL AUTO_INCREMENT,
    ma_mon             VARCHAR(20)     NOT NULL,
    ten_mon            VARCHAR(100)    NOT NULL,
    loai_mon           VARCHAR(30)     NOT NULL DEFAULT 'MON_HOC',
    -- loai_mon: MON_HOC | DANH_GIA_NANG_LUC | NANG_KHIEU
    PRIMARY KEY (mon_id),
    UNIQUE KEY uk_mon_ma (ma_mon)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 8. Bang to hop mon (danh sach cac to hop xet tuyen)
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_tohop;
CREATE TABLE xt_tohop (
    tohop_id           INT             NOT NULL AUTO_INCREMENT,
    ma_tohop           VARCHAR(20)     NOT NULL,
    ten_tohop          VARCHAR(100)    NULL,
    PRIMARY KEY (tohop_id),
    UNIQUE KEY uk_tohop_ma (ma_tohop)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 9. Bang chi tiet to hop - mon (quan he nhieu-nhieu)
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_tohop_mon;
CREATE TABLE xt_tohop_mon (
    tohop_mon_id       INT             NOT NULL AUTO_INCREMENT,
    tohop_id           INT             NOT NULL,
    mon_id             INT             NOT NULL,
    thu_tu             TINYINT UNSIGNED NOT NULL,
    PRIMARY KEY (tohop_mon_id),
    UNIQUE KEY uk_tohop_mon_thutu (tohop_id, thu_tu),
    UNIQUE KEY uk_tohop_mon_mon (tohop_id, mon_id),
    KEY idx_tohop_mon_mon (mon_id),
    CONSTRAINT fk_tohop_mon_tohop
        FOREIGN KEY (tohop_id) REFERENCES xt_tohop(tohop_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_tohop_mon_mon
        FOREIGN KEY (mon_id) REFERENCES xt_mon(mon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 10. Bang nganh tuyen sinh
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_nganh;
CREATE TABLE xt_nganh (
    nganh_id           INT             NOT NULL AUTO_INCREMENT,
    ma_nganh           VARCHAR(20)     NOT NULL,
    ten_nganh          VARCHAR(150)    NOT NULL,
    tohop_goc_id       INT             NULL,
    chi_tieu           INT             NOT NULL DEFAULT 0,
    diem_san           DECIMAL(10,2)   NULL,
    diem_trung_tuyen   DECIMAL(10,2)   NULL,
    is_active          TINYINT(1)      NOT NULL DEFAULT 1,
    PRIMARY KEY (nganh_id),
    UNIQUE KEY uk_nganh_ma (ma_nganh),
    KEY idx_nganh_tohopgoc (tohop_goc_id),
    CONSTRAINT fk_nganh_tohopgoc
        FOREIGN KEY (tohop_goc_id) REFERENCES xt_tohop(tohop_id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 11. Bang nganh - phuong thuc (nganh nao duoc phep xet theo phuong thuc nao)
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_nganh_phuongthuc;
CREATE TABLE xt_nganh_phuongthuc (
    nganh_phuongthuc_id INT            NOT NULL AUTO_INCREMENT,
    nganh_id           INT             NOT NULL,
    phuongthuc_id      TINYINT UNSIGNED NOT NULL,
    chi_tieu           INT             NULL,
    so_luong_hien_tai  INT             NULL DEFAULT 0,
    is_enabled         TINYINT(1)      NOT NULL DEFAULT 1,
    PRIMARY KEY (nganh_phuongthuc_id),
    UNIQUE KEY uk_nganh_phuongthuc (nganh_id, phuongthuc_id),
    KEY idx_nganh_phuongthuc_pt (phuongthuc_id),
    CONSTRAINT fk_np_nganh
        FOREIGN KEY (nganh_id) REFERENCES xt_nganh(nganh_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_np_pt
        FOREIGN KEY (phuongthuc_id) REFERENCES xt_phuongthuc(phuongthuc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 12. Bang nganh - to hop (cac to hop mon nao duoc su dung cho nganh)
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_nganh_tohop;
CREATE TABLE xt_nganh_tohop (
    nganh_tohop_id     INT             NOT NULL AUTO_INCREMENT,
    nganh_id           INT             NOT NULL,
    tohop_id           INT             NOT NULL,
    do_lech            DECIMAL(6,2)    NOT NULL DEFAULT 0.00,
    PRIMARY KEY (nganh_tohop_id),
    UNIQUE KEY uk_nganh_tohop (nganh_id, tohop_id),
    KEY idx_nganh_tohop_tohop (tohop_id),
    CONSTRAINT fk_nt_nganh
        FOREIGN KEY (nganh_id) REFERENCES xt_nganh(nganh_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_nt_tohop
        FOREIGN KEY (tohop_id) REFERENCES xt_tohop(tohop_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 13. Bang nganh - to hop - mon (mon nao trong to hop, he so)
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_nganh_tohop_mon;
CREATE TABLE xt_nganh_tohop_mon (
    nganh_tohop_mon_id INT             NOT NULL AUTO_INCREMENT,
    nganh_tohop_id     INT             NOT NULL,
    mon_id             INT             NOT NULL,
    he_so              TINYINT UNSIGNED NOT NULL DEFAULT 1,
    is_mon_chinh       TINYINT(1)      NOT NULL DEFAULT 0,
    PRIMARY KEY (nganh_tohop_mon_id),
    UNIQUE KEY uk_nt_mon (nganh_tohop_id, mon_id),
    KEY idx_nt_mon (mon_id),
    CONSTRAINT fk_ntm_nt
        FOREIGN KEY (nganh_tohop_id) REFERENCES xt_nganh_tohop(nganh_tohop_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ntm_mon
        FOREIGN KEY (mon_id) REFERENCES xt_mon(mon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 14. Bang diem thi tong hop (thong tin diem thi cua thi sinh)
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_diemthi;
CREATE TABLE xt_diemthi (
    diemthi_id         INT             NOT NULL AUTO_INCREMENT,
    thisinh_id         INT             NOT NULL,
    phuongthuc_id      TINYINT UNSIGNED NOT NULL,
    sobaodanh          VARCHAR(45)     NULL,
    nam_tuyensinh      SMALLINT        NOT NULL DEFAULT 2026,
    ghi_chu            VARCHAR(255)    NULL,
    imported_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (diemthi_id),
    UNIQUE KEY uk_diemthi_thisinh_pt_nam (thisinh_id, phuongthuc_id, nam_tuyensinh),
    KEY idx_diemthi_pt (phuongthuc_id),
    CONSTRAINT fk_diemthi_thisinh
        FOREIGN KEY (thisinh_id) REFERENCES xt_thisinh(thisinh_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_diemthi_pt
        FOREIGN KEY (phuongthuc_id) REFERENCES xt_phuongthuc(phuongthuc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 15. Bang diem thi chi tiet (diem tung mon trong to hop)
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_diemthi_chitiet;
CREATE TABLE xt_diemthi_chitiet (
    diemthi_ct_id      BIGINT          NOT NULL AUTO_INCREMENT,
    diemthi_id         INT             NOT NULL,
    mon_id             INT             NOT NULL,
    diem_goc           DECIMAL(8,2)    NULL,
    diem_quydoi        DECIMAL(8,2)    NULL,
    diem_sudung        DECIMAL(8,2)    NULL,
    PRIMARY KEY (diemthi_ct_id),
    UNIQUE KEY uk_diemthi_ct (diemthi_id, mon_id),
    KEY idx_diemthi_ct_mon (mon_id),
    CONSTRAINT fk_diemct_diemthi
        FOREIGN KEY (diemthi_id) REFERENCES xt_diemthi(diemthi_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_diemct_mon
        FOREIGN KEY (mon_id) REFERENCES xt_mon(mon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 16. Bang bang quy doi diem
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_bangquydoi;
CREATE TABLE xt_bangquydoi (
    bangquydoi_id      INT             NOT NULL AUTO_INCREMENT,
    phuongthuc_id      TINYINT UNSIGNED NOT NULL,
    tohop_id           INT             NULL,
    mon_id             INT             NULL,
    diem_tu            DECIMAL(6,2)    NOT NULL,
    diem_den           DECIMAL(6,2)    NOT NULL,
    diem_quydoi_tu     DECIMAL(6,2)    NOT NULL,
    diem_quydoi_den    DECIMAL(6,2)    NOT NULL,
    phan_vi            INT             NULL,
    ma_quydoi          VARCHAR(50)     NOT NULL,
    PRIMARY KEY (bangquydoi_id),
    UNIQUE KEY uk_bangquydoi_ma (ma_quydoi),
    KEY idx_bangquydoi_lookup (phuongthuc_id, tohop_id, mon_id, diem_tu, diem_den),
    CONSTRAINT fk_bqd_pt
        FOREIGN KEY (phuongthuc_id) REFERENCES xt_phuongthuc(phuongthuc_id),
    CONSTRAINT fk_bqd_tohop
        FOREIGN KEY (tohop_id) REFERENCES xt_tohop(tohop_id),
    CONSTRAINT fk_bqd_mon
        FOREIGN KEY (mon_id) REFERENCES xt_mon(mon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- MODULE DIEM CONG / UU TIEN / THANH TICH
-- Huong thiet ke:
-- 1) Bang du lieu goc cua thi sinh:
--    - xt_thisinh_chungchi
--    - xt_thisinh_thanh_tich
-- 2) Bang ket qua tinh tong:
--    - xt_diemcong
-- 3) Bang chi tiet nguon diem cong da ap:
--    - xt_diemcong_chitiet
-- =========================================================

SET FOREIGN_KEY_CHECKS = 0;

-- Xoa bang chi tiet truoc
DROP TABLE IF EXISTS xt_diemcong_chitiet;
DROP TABLE IF EXISTS xt_diemcong;

-- Xoa bang du lieu goc
DROP TABLE IF EXISTS xt_thisinh_thanh_tich;
DROP TABLE IF EXISTS xt_thisinh_chungchi;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- BANG HO SO CHUNG CHI CUA THI SINH
-- Moi dong = 1 chung chi ngoai ngu / chung chi duoc khai bao
-- =========================================================
CREATE TABLE xt_thisinh_chungchi (
     chungchi_id INT AUTO_INCREMENT PRIMARY KEY,
     thisinh_id INT NOT NULL,

     loai_chungchi VARCHAR(50) NOT NULL,
     ten_chungchi VARCHAR(255) NULL,

     diem_goc DECIMAL(6,2) NULL,
     bac_chungchi VARCHAR(50) NULL,

     so_hieu VARCHAR(100) NULL,
     don_vi_cap VARCHAR(255) NULL,

     ngay_cap DATE NULL,
     ngay_het_han DATE NULL,

     is_hop_le TINYINT(1) NOT NULL DEFAULT 1,,
     trang_thai_xac_minh VARCHAR(30) NOT NULL DEFAULT 'CHUA_XAC_MINH',
     ghi_chu TEXT NULL,

     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

     CONSTRAINT fk_xt_thisinh_chungchi_thisinh
         FOREIGN KEY (thisinh_id) REFERENCES xt_thisinh(thisinh_id)
             ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_xt_tscc_thisinh ON xt_thisinh_chungchi(thisinh_id);
CREATE INDEX idx_xt_tscc_loai ON xt_thisinh_chungchi(loai_chungchi);
CREATE INDEX idx_xt_tscc_hople ON xt_thisinh_chungchi(is_hop_le);

-- =========================================================
-- BANG HO SO THANH TICH / GIAI THUONG CUA THI SINH
-- Moi dong = 1 thanh tich co the duoc xet uu tien / diem cong
-- =========================================================
CREATE TABLE xt_thisinh_thanh_tich (
   thanhtich_id INT AUTO_INCREMENT PRIMARY KEY,
   thisinh_id INT NOT NULL,

   nhom_thanh_tich VARCHAR(50) NOT NULL,
   cap_thanh_tich VARCHAR(50) NULL,
   loai_giai VARCHAR(50) NULL,

   ten_thanh_tich VARCHAR(255) NULL,
   mon_dat_giai VARCHAR(100) NULL,
   linh_vuc VARCHAR(100) NULL,

   nam_dat_giai SMALLINT NULL,
   don_vi_to_chuc VARCHAR(255) NULL,

   so_hieu_minh_chung VARCHAR(100) NULL,
   is_hop_le TINYINT(1) NOT NULL DEFAULT 1,
   trang_thai_xac_minh VARCHAR(30) NOT NULL DEFAULT 'CHUA_XAC_MINH',
   ghi_chu TEXT NULL,

   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

   CONSTRAINT fk_xt_tstt_thisinh
       FOREIGN KEY (thisinh_id) REFERENCES xt_thisinh(thisinh_id)
           ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_xt_tstt_thisinh ON xt_thisinh_thanh_tich(thisinh_id);
CREATE INDEX idx_xt_tstt_nhom ON xt_thisinh_thanh_tich(nhom_thanh_tich);
CREATE INDEX idx_xt_tstt_cap ON xt_thisinh_thanh_tich(cap_thanh_tich);
CREATE INDEX idx_xt_tstt_hople ON xt_thisinh_thanh_tich(is_hop_le);

-- =========================================================
-- BANG TONG DIEM CONG
-- Moi dong = 1 thi sinh + 1 nganh-tohop + 1 phuong thuc
-- Day la KET QUA TINH TOAN, khong phai du lieu goc
-- =========================================================
CREATE TABLE xt_diemcong (
     diemcong_id INT AUTO_INCREMENT PRIMARY KEY,
     thisinh_id INT NOT NULL,
     nganh_tohop_id INT NOT NULL,
     phuongthuc_id TINYINT UNSIGNED NOT NULL,,

     tong_diem_chungchi DECIMAL(6,2) NOT NULL DEFAULT 0.00,
     tong_diem_uutien_xt DECIMAL(6,2) NOT NULL DEFAULT 0.00,
     tong_diem_uutien_quyche DECIMAL(6,2) NOT NULL DEFAULT 0.00,
     tong_diem_cong DECIMAL(6,2) NOT NULL DEFAULT 0.00,

     ghi_chu_tong TEXT NULL,

     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

     CONSTRAINT fk_xt_diemcong_thisinh
         FOREIGN KEY (thisinh_id) REFERENCES xt_thisinh(thisinh_id)
             ON DELETE CASCADE,

     CONSTRAINT fk_xt_diemcong_nganh_tohop
         FOREIGN KEY (nganh_tohop_id) REFERENCES xt_nganh_tohop(nganh_tohop_id)
             ON DELETE CASCADE,

     CONSTRAINT fk_xt_diemcong_phuongthuc
         FOREIGN KEY (phuongthuc_id) REFERENCES xt_phuongthuc(phuongthuc_id)
             ON DELETE CASCADE,

     CONSTRAINT uk_xt_diemcong_unique
         UNIQUE (thisinh_id, nganh_tohop_id, phuongthuc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_xt_diemcong_thisinh ON xt_diemcong(thisinh_id);
CREATE INDEX idx_xt_diemcong_nganh_tohop ON xt_diemcong(nganh_tohop_id);
CREATE INDEX idx_xt_diemcong_phuongthuc ON xt_diemcong(phuongthuc_id);

-- =========================================================
-- BANG CHI TIET DIEM CONG
-- Moi dong = 1 nguon diem cong/uu tien da duoc ap vao ket qua tong
-- =========================================================
CREATE TABLE xt_diemcong_chitiet (
     diemcong_ct_id INT AUTO_INCREMENT PRIMARY KEY,
     diemcong_id INT NOT NULL,

     loai_nguon VARCHAR(50) NOT NULL,
     ma_nguon VARCHAR(50) NULL,
     ten_nguon VARCHAR(255) NULL,

     cap_ap_dung VARCHAR(50) NULL,
     mon_lien_quan VARCHAR(50) NULL,
     gia_tri_goc VARCHAR(100) NULL,

     diem_quy_doi DECIMAL(6,2) NOT NULL DEFAULT 0.00,
     diem_cong DECIMAL(6,2) NOT NULL DEFAULT 0.00,

     nguon_bang VARCHAR(50) NULL,
     nguon_id INT NULL,

     thu_tu_uu_tien SMALLINT NOT NULL DEFAULT 1,
     is_ap_dung BIT NOT NULL DEFAULT b'1',

     ghi_chu TEXT NULL,

     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

     CONSTRAINT fk_xt_diemcong_ct_diemcong
         FOREIGN KEY (diemcong_id) REFERENCES xt_diemcong(diemcong_id)
             ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_xt_diemcong_ct_diemcong ON xt_diemcong_chitiet(diemcong_id);
CREATE INDEX idx_xt_diemcong_ct_loai ON xt_diemcong_chitiet(loai_nguon);
CREATE INDEX idx_xt_diemcong_ct_apdung ON xt_diemcong_chitiet(is_ap_dung);
CREATE INDEX idx_xt_diemcong_ct_nguonbang_nguonid ON xt_diemcong_chitiet(nguon_bang, nguon_id);

-- ----------------------------------------------------------
-- 18. Bang map ma xet tuyen -> nganh + phuong thuc + to hop
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_ma_xettuyen;
CREATE TABLE xt_ma_xettuyen (
    ma_xettuyen_id      INT              NOT NULL AUTO_INCREMENT,
    ma_xet_tuyen        VARCHAR(30)      NOT NULL,
    ten_chuong_trinh    VARCHAR(255)     NULL,
    nganh_id            INT              NOT NULL,
    phuongthuc_id       TINYINT UNSIGNED NOT NULL,
    nganh_tohop_id      INT              NULL,
    ma_tohop_nguon      VARCHAR(20)      NOT NULL DEFAULT '',
    ghi_chu             VARCHAR(255)     NULL,
    is_active           TINYINT(1)       NOT NULL DEFAULT 1,
    created_at          TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (ma_xettuyen_id),

    UNIQUE KEY uk_maxt_combo (ma_xet_tuyen, phuongthuc_id, ma_tohop_nguon),

    KEY idx_maxt_ma (ma_xet_tuyen),
    KEY idx_maxt_nganh (nganh_id),
    KEY idx_maxt_pt (phuongthuc_id),
    KEY idx_maxt_nt (nganh_tohop_id),

    CONSTRAINT fk_maxt_nganh
        FOREIGN KEY (nganh_id) REFERENCES xt_nganh(nganh_id)
            ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_maxt_pt
        FOREIGN KEY (phuongthuc_id) REFERENCES xt_phuongthuc(phuongthuc_id)
            ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_maxt_nt
        FOREIGN KEY (nganh_tohop_id) REFERENCES xt_nganh_tohop(nganh_tohop_id)
            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 18. Bang nguyen vong xet tuyen
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_nguyenvong;
CREATE TABLE xt_nguyenvong (
    nguyenvong_id      INT              NOT NULL AUTO_INCREMENT,
    thisinh_id         INT              NOT NULL,
    thu_tu             INT              NOT NULL,

    ma_xettuyen_id     INT              NULL,
    nganh_id           INT              NOT NULL,
    nganh_tohop_id     INT              NOT NULL,
    phuongthuc_id      TINYINT UNSIGNED NOT NULL,

    diem_thxt          DECIMAL(10,5)    NULL,
    diem_thgxt         DECIMAL(10,5)    NULL,
    diem_cong          DECIMAL(6,2)     NULL,
    diem_uutien        DECIMAL(6,2)     NULL,
    diem_xettuyen      DECIMAL(10,5)    NULL,
    ket_qua            VARCHAR(45)      NULL,
    ghi_chu            VARCHAR(255)     NULL,

    PRIMARY KEY (nguyenvong_id),

    UNIQUE KEY uk_nguyenvong_thutu (thisinh_id, thu_tu),
    UNIQUE KEY uk_nguyenvong_thisinh_maxt (thisinh_id, ma_xettuyen_id),

    KEY idx_nguyenvong_maxt (ma_xettuyen_id),
    KEY idx_nguyenvong_nganh (nganh_id),
    KEY idx_nguyenvong_nt (nganh_tohop_id),
    KEY idx_nguyenvong_pt (phuongthuc_id),

    CONSTRAINT fk_nv_thisinh
       FOREIGN KEY (thisinh_id) REFERENCES xt_thisinh(thisinh_id)
           ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_nv_maxt
       FOREIGN KEY (ma_xettuyen_id) REFERENCES xt_ma_xettuyen(ma_xettuyen_id)
           ON DELETE SET NULL ON UPDATE CASCADE,

    CONSTRAINT fk_nv_nganh
       FOREIGN KEY (nganh_id) REFERENCES xt_nganh(nganh_id)
           ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_nv_nt
       FOREIGN KEY (nganh_tohop_id) REFERENCES xt_nganh_tohop(nganh_tohop_id)
           ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_nv_pt
       FOREIGN KEY (phuongthuc_id) REFERENCES xt_phuongthuc(phuongthuc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- SEED DATA
-- ============================================================

-- Vai tro
INSERT INTO xt_vaitro (ma_vaitro, ten_vaitro, mo_ta) VALUES
('ADMIN', 'Admin', 'Quan tri vien he thong'),
('USER',  'User',  'Thi sinh/ nguoi dung thong thuong');

-- Nguoi dung mac dinh (password: admin123 -> BCrypt)
-- Ma BCrypt cho 'admin123': $2a$10$...
INSERT INTO xt_nguoidung (username, password_hash, ho_ten, email, vaitro_id, is_active) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye0X4tYz1x2z4v4z8Y1z4z4z4z4z4z4z4', 'Quan Tri Vien', 'admin@tuysinh.edu.vn', 1, 1),
('user1',  '$2a$10$N9qo8uLOickgx2ZMRZoMye0X4tYz1x2z4v4z8Y1z4z4z4z4z4z4z4', 'Nguyen Van A',   'user1@email.com',       2, 1);

-- Phuong thuc xet tuyen (5 cot diem)
INSERT INTO xt_phuongthuc (ma_phuongthuc, ten_phuongthuc, thang_diem) VALUES
('XTT',  'Xet tuyen thang', 30.00),
('THPT', 'Xet diem THPT',   30.00),
('VSAT', 'Xet diem V-SAT',  30.00),
('DGNL', 'Xet diem DGNL',   30.00),
('NK',   'Xet diem Nang khieu', 30.00);

INSERT INTO xt_mon (ma_mon, ten_mon, loai_mon) VALUES
-- Cac mon hoc thong thuong
('TO',   'Toan',                          'MON_HOC'),
('LI',   'Vat ly',                        'MON_HOC'),
('HO',   'Hoa hoc',                       'MON_HOC'),
('SI',   'Sinh hoc',                      'MON_HOC'),
('VA',   'Ngu van',                       'MON_HOC'),
('SU',   'Lich su',                       'MON_HOC'),
('DI',   'Dia ly',                        'MON_HOC'),
('N1',   'Ngoai ngu',                     'MON_HOC'),
('TI',   'Tin hoc',                       'MON_HOC'),
('KTPL', 'Giao duc kinh te va phap luat', 'MON_HOC'),
('CNCN', 'Cong nghe cong nghiep',         'MON_HOC'),
('CNNN', 'Cong nghe nong nghiep',         'MON_HOC'),
-- Danh gia nang luc (DGNL)
('NL1',  'Danh gia nang luc',             'DANH_GIA_NANG_LUC'),
-- Nang khieu (NK) - 1-2: Mam non, 3-4: My thuat, 5-6: Am nhac
('NK1',  'Nang khieu 1',                  'NANG_KHIEU'),
('NK2',  'Nang khieu 2',                  'NANG_KHIEU'),
('NK3',  'Nang khieu 3',                  'NANG_KHIEU'),
('NK4',  'Nang khieu 4',                  'NANG_KHIEU'),
('NK5',  'Nang khieu 5',                  'NANG_KHIEU'),
('NK6',  'Nang khieu 6',                  'NANG_KHIEU');

-- ------------------------------------------------------------
-- DOI TUONG UU TIEN
-- ------------------------------------------------------------
INSERT INTO xt_doituong_uutien (ma_doituong, ten_doituong, muc_diem, ghi_chu) VALUES
('UT1-01', 'Con liet si', 2.00, 'Con liet si, con thuong binh nang'),
('UT1-02', 'Con thuong binh nang - benh binh', 2.00, 'Con thuong binh nang, benh binh suy giam >=81%'),
('UT1-03', 'Nguoi dan toc thieu so - vung dac biet kho khan', 2.00, 'Nguoi dan toc thieu so o vung dac biet kho khan'),
('UT1-04', 'Nguoi bi nhiem chat doc hoa hoc', 2.00, 'Nguoi bi nhiem chat doc hoa hoc nang'),
('UT1-05', 'Nguoi khuyet tat nang', 2.00, 'Nguoi khuyet tat nang'),

('UT2-01', 'Con thuong binh - benh binh', 1.00, 'Con thuong binh, benh binh suy giam <81%'),
('UT2-02', 'Con nguoi co cong voi cach mang', 1.00, 'Con cua nguoi co cong voi cach mang'),
('UT2-03', 'Nguoi dan toc thieu so', 1.00, 'Nguoi dan toc thieu so (khong thuoc UT1)'),
('UT2-04', 'Nguoi o vung kho khan', 1.00, 'Nguoi o vung kho khan (khong thuoc dien dac biet kho khan)'),

('UT3-00', 'Khong thuoc dien uu tien', 0.00, 'Khong thuoc dien uu tien');


-- ------------------------------------------------------------
-- KHU VUC UU TIEN
-- ------------------------------------------------------------
INSERT INTO xt_khuvuc_uutien (ma_khuvuc, ten_khuvuc, muc_diem, ghi_chu) VALUES
('KV1',    'Khu vuc 1',               0.75, 'Vung sau, vung xa, mien nui, hai dao, vung dac biet kho khan'),
('KV2-NT', 'Khu vuc 2 - Nong thon',   0.50, 'Khu vuc nong thon (khong thuoc KV1)'),
('KV2',    'Khu vuc 2',               0.25, 'Thanh pho truc thuoc tinh, thi xa'),
('KV3',    'Khu vuc 3',               0.00, 'Quan noi thanh cua cac thanh pho lon');

-- Thi sinh mau
INSERT INTO xt_thisinh (cccd, sobaodanh, ho, ten, ngay_sinh, gioi_tinh, dien_thoai, email, noi_sinh, doituong_id, khuvuc_id) VALUES
('001234567890', 'TS0001', 'Nguyen Van', 'An',  '2005-05-10', 'Nam',  '0912345001', 'an@email.com',    'Ha Noi',  1, 3),
('001234567891', 'TS0002', 'Tran Thi',   'Binh', '2005-07-15', 'Nu',  '0912345002', 'binh@email.com',  'HCM',     2, 2),
('001234567892', 'TS0003', 'Le Van',     'Cuong','2005-03-20', 'Nam', '0912345003', 'cuong@email.com', 'Da Nang', NULL, 2),
('001234567893', 'TS0004', 'Pham Thi',   'Dung', '2005-09-08', 'Nu',  '0912345004', 'dung@email.com',  'Hue',     3, 1),
('001234567894', 'TS0005', 'Hoang Van',  'Em',   '2005-12-25', 'Nam', '0912345005', 'em@email.com',    'Hai Phong', NULL, 2);

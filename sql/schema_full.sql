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

-- ----------------------------------------------------------
-- 17. Bang diem cong (diem uu tien, diem chung chi)
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_diemcong;
CREATE TABLE xt_diemcong (
    diemcong_id         INT             NOT NULL AUTO_INCREMENT,
    thisinh_id         INT             NOT NULL,
    nganh_tohop_id     INT             NOT NULL,
    phuongthuc_id      TINYINT UNSIGNED NOT NULL,
    diem_chungchi      DECIMAL(6,2)    NOT NULL DEFAULT 0.00,
    diem_uutien_xt     DECIMAL(6,2)    NOT NULL DEFAULT 0.00,
    diem_tong           DECIMAL(6,2)    NOT NULL DEFAULT 0.00,
    ghi_chu            TEXT            NULL,
    PRIMARY KEY (diemcong_id),
    UNIQUE KEY uk_diemcong (thisinh_id, nganh_tohop_id, phuongthuc_id),
    KEY idx_diemcong_pt (phuongthuc_id),
    CONSTRAINT fk_dc_thisinh
        FOREIGN KEY (thisinh_id) REFERENCES xt_thisinh(thisinh_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_dc_nt
        FOREIGN KEY (nganh_tohop_id) REFERENCES xt_nganh_tohop(nganh_tohop_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_dc_pt
        FOREIGN KEY (phuongthuc_id) REFERENCES xt_phuongthuc(phuongthuc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------
-- 18. Bang nguyen vong xet tuyen
-- ----------------------------------------------------------
DROP TABLE IF EXISTS xt_nguyenvong;
CREATE TABLE xt_nguyenvong (
    nguyenvong_id      INT             NOT NULL AUTO_INCREMENT,
    thisinh_id         INT             NOT NULL,
    nganh_id           INT             NOT NULL,
    nganh_tohop_id     INT             NOT NULL,
    phuongthuc_id      TINYINT UNSIGNED NOT NULL,
    thu_tu             INT             NOT NULL,
    diem_thxt          DECIMAL(10,5)   NULL,
    diem_thgxt         DECIMAL(10,5)   NULL,
    diem_cong          DECIMAL(6,2)    NULL,
    diem_uutien        DECIMAL(6,2)    NULL,
    diem_xettuyen      DECIMAL(10,5)   NULL,
    ket_qua            VARCHAR(45)     NULL,
    -- ket_qua: CHO_XET | TRUNG_TUYEN | TRUOT | PHOI_DU_KIEN
    ghi_chu            VARCHAR(255)    NULL,
    PRIMARY KEY (nguyenvong_id),
    UNIQUE KEY uk_nguyenvong_thutu (thisinh_id, thu_tu),
    UNIQUE KEY uk_nguyenvong_once (thisinh_id, nganh_id, nganh_tohop_id, phuongthuc_id),
    KEY idx_nguyenvong_nganh (nganh_id),
    KEY idx_nguyenvong_nt (nganh_tohop_id),
    KEY idx_nguyenvong_pt (phuongthuc_id),
    CONSTRAINT fk_nv_thisinh
        FOREIGN KEY (thisinh_id) REFERENCES xt_thisinh(thisinh_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
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

-- To hop mon
INSERT INTO xt_tohop (ma_tohop, ten_tohop) VALUES
('A00', 'Toan - Li - Hoa'),
('A01', 'Toan - Li - Ngoai ngu'),
('A02', 'Toan - Li - Sinh hoc'),
('A05', 'Toan - Ngu van - Lich su'),
('A07', 'Toan - Dia ly - KTPL'),
('A14', 'Toan - Ngu van - KTPL'),
('B00', 'Toan - Hoa - Ngu van'),
('C00', 'Ngu van - Lich su - Dia ly'),
('C14', 'Ngu van - KTPL - Lich su'),
('D01', 'Toan - Ngu van - Ngoai ngu'),
('D07', 'Toan - Lich su - KTPL'),
('D08', 'Toan - Dia ly - Ngoai ngu'),
('NK_MN', 'Nang khieu Mam non'),
('NK_MT', 'Nang khieu My thuat'),
('NK_AN', 'Nang khieu Am nhac');

-- ============================================================
-- CHI TIET TO HOP MON (seed an toan, khong dung id cung)
-- ============================================================
-- A00 = TO, LI, HO
INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 1
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A00' AND m.ma_mon = 'TO';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 2
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A00' AND m.ma_mon = 'LI';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 3
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A00' AND m.ma_mon = 'HO';


-- A01 = TO, LI, N1
INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 1
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A01' AND m.ma_mon = 'TO';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 2
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A01' AND m.ma_mon = 'LI';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 3
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A01' AND m.ma_mon = 'N1';


-- A02 = TO, LI, SI
INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 1
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A02' AND m.ma_mon = 'TO';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 2
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A02' AND m.ma_mon = 'LI';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 3
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A02' AND m.ma_mon = 'SI';


-- A05 = TO, VA, SU
INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 1
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A05' AND m.ma_mon = 'TO';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 2
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A05' AND m.ma_mon = 'VA';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 3
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A05' AND m.ma_mon = 'SU';


-- A07 = TO, DI, KTPL
INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 1
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A07' AND m.ma_mon = 'TO';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 2
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A07' AND m.ma_mon = 'DI';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 3
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A07' AND m.ma_mon = 'KTPL';


-- A14 = TO, VA, KTPL
INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 1
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A14' AND m.ma_mon = 'TO';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 2
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A14' AND m.ma_mon = 'VA';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 3
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'A14' AND m.ma_mon = 'KTPL';


-- B00 = TO, HO, VA
INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 1
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'B00' AND m.ma_mon = 'TO';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 2
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'B00' AND m.ma_mon = 'HO';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 3
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'B00' AND m.ma_mon = 'VA';


-- C00 = VA, SU, DI
INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 1
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'C00' AND m.ma_mon = 'VA';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 2
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'C00' AND m.ma_mon = 'SU';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 3
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'C00' AND m.ma_mon = 'DI';


-- C14 = VA, KTPL, SU
INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 1
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'C14' AND m.ma_mon = 'VA';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 2
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'C14' AND m.ma_mon = 'KTPL';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 3
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'C14' AND m.ma_mon = 'SU';


-- D01 = TO, VA, N1
INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 1
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'D01' AND m.ma_mon = 'TO';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 2
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'D01' AND m.ma_mon = 'VA';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 3
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'D01' AND m.ma_mon = 'N1';


-- D07 = TO, SU, KTPL
INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 1
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'D07' AND m.ma_mon = 'TO';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 2
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'D07' AND m.ma_mon = 'SU';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 3
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'D07' AND m.ma_mon = 'KTPL';


-- D08 = TO, DI, N1
INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 1
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'D08' AND m.ma_mon = 'TO';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 2
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'D08' AND m.ma_mon = 'DI';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 3
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'D08' AND m.ma_mon = 'N1';


-- NK_MN = NK1, NK2
INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 1
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'NK_MN' AND m.ma_mon = 'NK1';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 2
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'NK_MN' AND m.ma_mon = 'NK2';


-- NK_MT = NK3, NK4
INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 1
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'NK_MT' AND m.ma_mon = 'NK3';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 2
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'NK_MT' AND m.ma_mon = 'NK4';


-- NK_AN = NK5, NK6
INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 1
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'NK_AN' AND m.ma_mon = 'NK5';

INSERT INTO xt_tohop_mon (tohop_id, mon_id, thu_tu)
SELECT t.tohop_id, m.mon_id, 2
FROM xt_tohop t JOIN xt_mon m
WHERE t.ma_tohop = 'NK_AN' AND m.ma_mon = 'NK6';

-- Nganh tuyen sinh
INSERT INTO xt_nganh (ma_nganh, ten_nganh, chi_tieu, diem_san) VALUES
('CNTT',  'Cong nghe thong tin',         200, 22.50),
('KT',    'Kinh te',                      180, 21.00),
('QTKD',  'Quan tri kinh doanh',          150, 20.50),
('NN',    'Ngoai ngu',                    100, 19.00),
('TCCN',  'Ky thuat cong nghiep',         120, 18.00),
('TNNN',  'Cong nghe nong nghiep',        100, 17.00),
('SP',    'Su pham Toan',                 80,  18.00),
('SPV',   'Su pham Van',                  80,  17.50),
('MT',    'My thuat',                     60,  16.00),
('AN',    'Am nhac',                      50,  15.00),
('MN',    'Giao duc Mam non',             80,  15.00),
('GDCD',  'Giao duc cong dan',            60,  16.50);

-- ============================================================
-- NGANH - PHUONG THUC
-- Seed an toan, khong dung nganh_id / phuongthuc_id cung
-- Phuong thuc chuan: XTT, THPT, VSAT, DGNL, NK
-- ============================================================

INSERT INTO xt_nganh_phuongthuc (nganh_id, phuongthuc_id, chi_tieu, is_enabled)
SELECT n.nganh_id, p.phuongthuc_id, s.chi_tieu, s.is_enabled
FROM (
         SELECT 'CNTT' AS ma_nganh, 'XTT'  AS ma_phuongthuc, 30 AS chi_tieu, 1 AS is_enabled
         UNION ALL SELECT 'CNTT', 'THPT', 50, 1
         UNION ALL SELECT 'CNTT', 'VSAT', 50, 1
         UNION ALL SELECT 'CNTT', 'DGNL', 60, 1
         UNION ALL SELECT 'CNTT', 'NK',   10, 1

         UNION ALL SELECT 'KT',   'XTT',  20, 1
         UNION ALL SELECT 'KT',   'THPT', 50, 1
         UNION ALL SELECT 'KT',   'VSAT', 50, 1
         UNION ALL SELECT 'KT',   'DGNL', 60, 1
         UNION ALL SELECT 'KT',   'NK',    0, 1

         UNION ALL SELECT 'QTKD', 'XTT',  20, 1
         UNION ALL SELECT 'QTKD', 'THPT', 40, 1
         UNION ALL SELECT 'QTKD', 'VSAT', 40, 1
         UNION ALL SELECT 'QTKD', 'DGNL', 50, 1
         UNION ALL SELECT 'QTKD', 'NK',    0, 1

         UNION ALL SELECT 'NN',   'XTT',  15, 1
         UNION ALL SELECT 'NN',   'THPT', 30, 1
         UNION ALL SELECT 'NN',   'VSAT', 25, 1
         UNION ALL SELECT 'NN',   'DGNL', 30, 1
         UNION ALL SELECT 'NN',   'NK',    0, 1

         UNION ALL SELECT 'TCCN', 'XTT',  20, 1
         UNION ALL SELECT 'TCCN', 'THPT', 30, 1
         UNION ALL SELECT 'TCCN', 'VSAT', 30, 1
         UNION ALL SELECT 'TCCN', 'DGNL', 40, 1
         UNION ALL SELECT 'TCCN', 'NK',    0, 1

         UNION ALL SELECT 'TNNN', 'XTT',  20, 1
         UNION ALL SELECT 'TNNN', 'THPT', 20, 1
         UNION ALL SELECT 'TNNN', 'VSAT', 30, 1
         UNION ALL SELECT 'TNNN', 'DGNL', 30, 1
         UNION ALL SELECT 'TNNN', 'NK',    0, 1

         UNION ALL SELECT 'SP',   'XTT',  10, 1
         UNION ALL SELECT 'SP',   'THPT', 20, 1
         UNION ALL SELECT 'SP',   'VSAT', 20, 1
         UNION ALL SELECT 'SP',   'DGNL', 30, 1
         UNION ALL SELECT 'SP',   'NK',    0, 1

         UNION ALL SELECT 'SPV',  'XTT',  10, 1
         UNION ALL SELECT 'SPV',  'THPT', 20, 1
         UNION ALL SELECT 'SPV',  'VSAT', 20, 1
         UNION ALL SELECT 'SPV',  'DGNL', 30, 1
         UNION ALL SELECT 'SPV',  'NK',    0, 1

         UNION ALL SELECT 'MT',   'XTT',  10, 1
         UNION ALL SELECT 'MT',   'THPT', 10, 1
         UNION ALL SELECT 'MT',   'VSAT', 10, 1
         UNION ALL SELECT 'MT',   'DGNL', 10, 1
         UNION ALL SELECT 'MT',   'NK',   20, 1

         UNION ALL SELECT 'AN',   'XTT',  10, 1
         UNION ALL SELECT 'AN',   'THPT', 10, 1
         UNION ALL SELECT 'AN',   'VSAT', 10, 1
         UNION ALL SELECT 'AN',   'DGNL', 10, 1
         UNION ALL SELECT 'AN',   'NK',   10, 1

         UNION ALL SELECT 'MN',   'XTT',  10, 1
         UNION ALL SELECT 'MN',   'THPT', 20, 1
         UNION ALL SELECT 'MN',   'VSAT', 20, 1
         UNION ALL SELECT 'MN',   'DGNL', 20, 1
         UNION ALL SELECT 'MN',   'NK',   10, 1

         UNION ALL SELECT 'GDCD', 'XTT',  10, 1
         UNION ALL SELECT 'GDCD', 'THPT', 20, 1
         UNION ALL SELECT 'GDCD', 'VSAT', 15, 1
         UNION ALL SELECT 'GDCD', 'DGNL', 15, 1
         UNION ALL SELECT 'GDCD', 'NK',    0, 1
     ) s
         JOIN xt_nganh n
              ON n.ma_nganh = s.ma_nganh
         JOIN xt_phuongthuc p
              ON p.ma_phuongthuc = s.ma_phuongthuc;

-- Nganh - To hop (gan cac to hop cho tung nganh)
-- ============================================================
-- NGANH - TO HOP
-- Seed an toan, khong dung nganh_id / tohop_id cung
-- ============================================================

INSERT INTO xt_nganh_tohop (nganh_id, tohop_id, do_lech)
SELECT n.nganh_id, t.tohop_id, s.do_lech
FROM (
         -- CNTT
         SELECT 'CNTT' AS ma_nganh, 'A00' AS ma_tohop, 0.00 AS do_lech
         UNION ALL SELECT 'CNTT', 'A01', 0.00
         UNION ALL SELECT 'CNTT', 'A02', 0.00
         UNION ALL SELECT 'CNTT', 'D01', 0.00

         -- KT
         UNION ALL SELECT 'KT', 'A00', 0.00
         UNION ALL SELECT 'KT', 'A01', 0.00
         UNION ALL SELECT 'KT', 'B00', 0.00
         UNION ALL SELECT 'KT', 'D01', 0.00

         -- QTKD
         UNION ALL SELECT 'QTKD', 'A00', 0.00
         UNION ALL SELECT 'QTKD', 'A01', 0.00
         UNION ALL SELECT 'QTKD', 'D01', 0.00
         UNION ALL SELECT 'QTKD', 'D07', 0.00

         -- NN
         UNION ALL SELECT 'NN', 'A01', 0.00
         UNION ALL SELECT 'NN', 'D01', 0.00
         UNION ALL SELECT 'NN', 'D08', 0.00

         -- TCCN
         UNION ALL SELECT 'TCCN', 'A00', 0.00
         UNION ALL SELECT 'TCCN', 'A02', 0.00
         UNION ALL SELECT 'TCCN', 'A07', 0.00

         -- TNNN
         UNION ALL SELECT 'TNNN', 'A00', 0.00
         UNION ALL SELECT 'TNNN', 'A05', 0.00
         UNION ALL SELECT 'TNNN', 'A14', 0.00

         -- SP
         UNION ALL SELECT 'SP', 'A00', 0.00
         UNION ALL SELECT 'SP', 'A01', 0.00
         UNION ALL SELECT 'SP', 'D01', 0.00

         -- SPV
         UNION ALL SELECT 'SPV', 'A05', 0.00
         UNION ALL SELECT 'SPV', 'A14', 0.00
         UNION ALL SELECT 'SPV', 'C00', 0.00

         -- MT
         UNION ALL SELECT 'MT', 'NK_MT', 0.00

         -- AN
         UNION ALL SELECT 'AN', 'NK_AN', 0.00

         -- MN
         UNION ALL SELECT 'MN', 'NK_MN', 0.00

         -- GDCD
         UNION ALL SELECT 'GDCD', 'A05', 0.00
         UNION ALL SELECT 'GDCD', 'A14', 0.00
         UNION ALL SELECT 'GDCD', 'C14', 0.00
     ) s
         JOIN xt_nganh n
              ON n.ma_nganh = s.ma_nganh
         JOIN xt_tohop t
              ON t.ma_tohop = s.ma_tohop;


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

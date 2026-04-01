SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS xt_nguyenvong;
DROP TABLE IF EXISTS xt_diemcong;
DROP TABLE IF EXISTS xt_bangquydoi;
DROP TABLE IF EXISTS xt_diemthi_chitiet;
DROP TABLE IF EXISTS xt_diemthi;
DROP TABLE IF EXISTS xt_nganh_tohop_mon;
DROP TABLE IF EXISTS xt_nganh_tohop;
DROP TABLE IF EXISTS xt_nganh_phuongthuc;
DROP TABLE IF EXISTS xt_nganh;
DROP TABLE IF EXISTS xt_tohop_mon;
DROP TABLE IF EXISTS xt_tohop;
DROP TABLE IF EXISTS xt_mon;
DROP TABLE IF EXISTS xt_phuongthuc;
DROP TABLE IF EXISTS xt_thisinh;
DROP TABLE IF EXISTS xt_khuvuc_uutien;
DROP TABLE IF EXISTS xt_doituong_uutien;
DROP TABLE IF EXISTS xt_nguoidung;
DROP TABLE IF EXISTS xt_vaitro;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE xt_vaitro (
    vaitro_id TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ma_vaitro VARCHAR(20) NOT NULL,
    ten_vaitro VARCHAR(50) NOT NULL,
    PRIMARY KEY (vaitro_id),
    UNIQUE KEY uk_xt_vaitro_ma (ma_vaitro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_nguoidung (
    nguoidung_id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    ho_ten VARCHAR(150) NULL,
    email VARCHAR(100) NULL,
    vaitro_id TINYINT UNSIGNED NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (nguoidung_id),
    UNIQUE KEY uk_xt_nguoidung_username (username),
    UNIQUE KEY uk_xt_nguoidung_email (email),
    KEY idx_xt_nguoidung_vaitro (vaitro_id),
    CONSTRAINT fk_xt_nguoidung_vaitro
        FOREIGN KEY (vaitro_id) REFERENCES xt_vaitro(vaitro_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_doituong_uutien (
    doituong_id INT NOT NULL AUTO_INCREMENT,
    ma_doituong VARCHAR(20) NOT NULL,
    ten_doituong VARCHAR(100) NOT NULL,
    muc_diem DECIMAL(4,2) NOT NULL DEFAULT 0.00,
    ghi_chu VARCHAR(255) NULL,
    PRIMARY KEY (doituong_id),
    UNIQUE KEY uk_xt_doituong_ma (ma_doituong)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_khuvuc_uutien (
    khuvuc_id INT NOT NULL AUTO_INCREMENT,
    ma_khuvuc VARCHAR(20) NOT NULL,
    ten_khuvuc VARCHAR(100) NOT NULL,
    muc_diem DECIMAL(4,2) NOT NULL DEFAULT 0.00,
    ghi_chu VARCHAR(255) NULL,
    PRIMARY KEY (khuvuc_id),
    UNIQUE KEY uk_xt_khuvuc_ma (ma_khuvuc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_thisinh (
    thisinh_id INT NOT NULL AUTO_INCREMENT,
    nguoidung_id INT NULL,
    cccd VARCHAR(20) NOT NULL,
    sobaodanh VARCHAR(45) NULL,
    ho VARCHAR(100) NOT NULL,
    ten VARCHAR(100) NOT NULL,
    ngay_sinh DATE NULL,
    gioi_tinh VARCHAR(10) NULL,
    dien_thoai VARCHAR(20) NULL,
    email VARCHAR(100) NULL,
    noi_sinh VARCHAR(100) NULL,
    doituong_id INT NULL,
    khuvuc_id INT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (thisinh_id),
    UNIQUE KEY uk_xt_thisinh_cccd (cccd),
    UNIQUE KEY uk_xt_thisinh_sobaodanh (sobaodanh),
    UNIQUE KEY uk_xt_thisinh_nguoidung (nguoidung_id),
    KEY idx_xt_thisinh_doituong (doituong_id),
    KEY idx_xt_thisinh_khuvuc (khuvuc_id),
    CONSTRAINT fk_xt_thisinh_nguoidung
        FOREIGN KEY (nguoidung_id) REFERENCES xt_nguoidung(nguoidung_id),
    CONSTRAINT fk_xt_thisinh_doituong
        FOREIGN KEY (doituong_id) REFERENCES xt_doituong_uutien(doituong_id),
    CONSTRAINT fk_xt_thisinh_khuvuc
        FOREIGN KEY (khuvuc_id) REFERENCES xt_khuvuc_uutien(khuvuc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_phuongthuc (
    phuongthuc_id TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    ma_phuongthuc VARCHAR(20) NOT NULL,
    ten_phuongthuc VARCHAR(100) NOT NULL,
    thang_diem DECIMAL(6,2) NOT NULL DEFAULT 30.00,
    PRIMARY KEY (phuongthuc_id),
    UNIQUE KEY uk_xt_phuongthuc_ma (ma_phuongthuc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_mon (
    mon_id INT NOT NULL AUTO_INCREMENT,
    ma_mon VARCHAR(20) NOT NULL,
    ten_mon VARCHAR(100) NOT NULL,
    loai_mon VARCHAR(30) NOT NULL DEFAULT 'MON_HOC',
    PRIMARY KEY (mon_id),
    UNIQUE KEY uk_xt_mon_ma (ma_mon)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_tohop (
    tohop_id INT NOT NULL AUTO_INCREMENT,
    ma_tohop VARCHAR(20) NOT NULL,
    ten_tohop VARCHAR(100) NULL,
    PRIMARY KEY (tohop_id),
    UNIQUE KEY uk_xt_tohop_ma (ma_tohop)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_tohop_mon (
    tohop_mon_id INT NOT NULL AUTO_INCREMENT,
    tohop_id INT NOT NULL,
    mon_id INT NOT NULL,
    thu_tu TINYINT UNSIGNED NOT NULL,
    PRIMARY KEY (tohop_mon_id),
    UNIQUE KEY uk_xt_tohop_mon_thutu (tohop_id, thu_tu),
    UNIQUE KEY uk_xt_tohop_mon_mon (tohop_id, mon_id),
    KEY idx_xt_tohop_mon_mon (mon_id),
    CONSTRAINT fk_xt_tohop_mon_tohop
        FOREIGN KEY (tohop_id) REFERENCES xt_tohop(tohop_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_xt_tohop_mon_mon
        FOREIGN KEY (mon_id) REFERENCES xt_mon(mon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_nganh (
    nganh_id INT NOT NULL AUTO_INCREMENT,
    ma_nganh VARCHAR(20) NOT NULL,
    ten_nganh VARCHAR(150) NOT NULL,
    tohop_goc_id INT NULL,
    chi_tieu INT NOT NULL DEFAULT 0,
    diem_san DECIMAL(10,2) NULL,
    diem_trung_tuyen DECIMAL(10,2) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (nganh_id),
    UNIQUE KEY uk_xt_nganh_ma (ma_nganh),
    KEY idx_xt_nganh_tohopgoc (tohop_goc_id),
    CONSTRAINT fk_xt_nganh_tohopgoc
        FOREIGN KEY (tohop_goc_id) REFERENCES xt_tohop(tohop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_nganh_phuongthuc (
    nganh_phuongthuc_id INT NOT NULL AUTO_INCREMENT,
    nganh_id INT NOT NULL,
    phuongthuc_id TINYINT UNSIGNED NOT NULL,
    chi_tieu INT NULL,
    so_luong_hien_tai INT NULL,
    is_enabled TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (nganh_phuongthuc_id),
    UNIQUE KEY uk_xt_nganh_phuongthuc (nganh_id, phuongthuc_id),
    KEY idx_xt_nganh_phuongthuc_pt (phuongthuc_id),
    CONSTRAINT fk_xt_nganh_phuongthuc_nganh
        FOREIGN KEY (nganh_id) REFERENCES xt_nganh(nganh_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_xt_nganh_phuongthuc_pt
        FOREIGN KEY (phuongthuc_id) REFERENCES xt_phuongthuc(phuongthuc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_nganh_tohop (
    nganh_tohop_id INT NOT NULL AUTO_INCREMENT,
    nganh_id INT NOT NULL,
    tohop_id INT NOT NULL,
    do_lech DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (nganh_tohop_id),
    UNIQUE KEY uk_xt_nganh_tohop (nganh_id, tohop_id),
    UNIQUE KEY uk_xt_nganh_tohop_pair (nganh_tohop_id, nganh_id),
    KEY idx_xt_nganh_tohop_tohop (tohop_id),
    CONSTRAINT fk_xt_nganh_tohop_nganh
        FOREIGN KEY (nganh_id) REFERENCES xt_nganh(nganh_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_xt_nganh_tohop_tohop
        FOREIGN KEY (tohop_id) REFERENCES xt_tohop(tohop_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_nganh_tohop_mon (
    nganh_tohop_mon_id INT NOT NULL AUTO_INCREMENT,
    nganh_tohop_id INT NOT NULL,
    mon_id INT NOT NULL,
    he_so TINYINT UNSIGNED NOT NULL DEFAULT 1,
    is_mon_chinh TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (nganh_tohop_mon_id),
    UNIQUE KEY uk_xt_nganh_tohop_mon (nganh_tohop_id, mon_id),
    KEY idx_xt_nganh_tohop_mon_mon (mon_id),
    CONSTRAINT fk_xt_nganh_tohop_mon_nt
        FOREIGN KEY (nganh_tohop_id) REFERENCES xt_nganh_tohop(nganh_tohop_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_xt_nganh_tohop_mon_mon
        FOREIGN KEY (mon_id) REFERENCES xt_mon(mon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_diemthi (
    diemthi_id INT NOT NULL AUTO_INCREMENT,
    thisinh_id INT NOT NULL,
    phuongthuc_id TINYINT UNSIGNED NOT NULL,
    sobaodanh VARCHAR(45) NULL,
    nam_tuyensinh SMALLINT NOT NULL DEFAULT 2025,
    ghi_chu VARCHAR(255) NULL,
    imported_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (diemthi_id),
    UNIQUE KEY uk_xt_diemthi_thisinh_pt_nam (thisinh_id, phuongthuc_id, nam_tuyensinh),
    KEY idx_xt_diemthi_pt (phuongthuc_id),
    CONSTRAINT fk_xt_diemthi_thisinh
        FOREIGN KEY (thisinh_id) REFERENCES xt_thisinh(thisinh_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_xt_diemthi_pt
        FOREIGN KEY (phuongthuc_id) REFERENCES xt_phuongthuc(phuongthuc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_diemthi_chitiet (
    diemthi_ct_id BIGINT NOT NULL AUTO_INCREMENT,
    diemthi_id INT NOT NULL,
    mon_id INT NOT NULL,
    diem_goc DECIMAL(8,2) NULL,
    diem_quydoi DECIMAL(8,2) NULL,
    diem_sudung DECIMAL(8,2) NULL,
    PRIMARY KEY (diemthi_ct_id),
    UNIQUE KEY uk_xt_diemthi_ct (diemthi_id, mon_id),
    KEY idx_xt_diemthi_ct_mon (mon_id),
    CONSTRAINT fk_xt_diemthi_ct_diemthi
        FOREIGN KEY (diemthi_id) REFERENCES xt_diemthi(diemthi_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_xt_diemthi_ct_mon
        FOREIGN KEY (mon_id) REFERENCES xt_mon(mon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_bangquydoi (
    bangquydoi_id INT NOT NULL AUTO_INCREMENT,
    phuongthuc_id TINYINT UNSIGNED NOT NULL,
    tohop_id INT NULL,
    mon_id INT NULL,
    diem_tu DECIMAL(6,2) NOT NULL,
    diem_den DECIMAL(6,2) NOT NULL,
    diem_quydoi_tu DECIMAL(6,2) NOT NULL,
    diem_quydoi_den DECIMAL(6,2) NOT NULL,
    phan_vi INT NULL,
    ma_quydoi VARCHAR(50) NOT NULL,
    PRIMARY KEY (bangquydoi_id),
    UNIQUE KEY uk_xt_bangquydoi_ma (ma_quydoi),
    KEY idx_xt_bangquydoi_lookup (phuongthuc_id, tohop_id, mon_id, diem_tu, diem_den),
    CONSTRAINT fk_xt_bangquydoi_pt
        FOREIGN KEY (phuongthuc_id) REFERENCES xt_phuongthuc(phuongthuc_id),
    CONSTRAINT fk_xt_bangquydoi_tohop
        FOREIGN KEY (tohop_id) REFERENCES xt_tohop(tohop_id),
    CONSTRAINT fk_xt_bangquydoi_mon
        FOREIGN KEY (mon_id) REFERENCES xt_mon(mon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_diemcong (
    diemcong_id INT NOT NULL AUTO_INCREMENT,
    thisinh_id INT NOT NULL,
    nganh_tohop_id INT NOT NULL,
    phuongthuc_id TINYINT UNSIGNED NOT NULL,
    diem_chungchi DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    diem_uutien_xt DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    diem_tong DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    ghi_chu TEXT NULL,
    PRIMARY KEY (diemcong_id),
    UNIQUE KEY uk_xt_diemcong (thisinh_id, nganh_tohop_id, phuongthuc_id),
    KEY idx_xt_diemcong_pt (phuongthuc_id),
    CONSTRAINT fk_xt_diemcong_thisinh
        FOREIGN KEY (thisinh_id) REFERENCES xt_thisinh(thisinh_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_xt_diemcong_nganh_tohop
        FOREIGN KEY (nganh_tohop_id) REFERENCES xt_nganh_tohop(nganh_tohop_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_xt_diemcong_pt
        FOREIGN KEY (phuongthuc_id) REFERENCES xt_phuongthuc(phuongthuc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE xt_nguyenvong (
    nguyenvong_id INT NOT NULL AUTO_INCREMENT,
    thisinh_id INT NOT NULL,
    nganh_id INT NOT NULL,
    nganh_tohop_id INT NOT NULL,
    phuongthuc_id TINYINT UNSIGNED NOT NULL,
    thu_tu INT NOT NULL,
    diem_thxt DECIMAL(10,5) NULL,
    diem_thgxt DECIMAL(10,5) NULL,
    diem_cong DECIMAL(6,2) NULL,
    diem_uutien DECIMAL(6,2) NULL,
    diem_xettuyen DECIMAL(10,5) NULL,
    ket_qua VARCHAR(45) NULL,
    ghi_chu VARCHAR(255) NULL,
    PRIMARY KEY (nguyenvong_id),
    UNIQUE KEY uk_xt_nguyenvong_thutu (thisinh_id, thu_tu),
    UNIQUE KEY uk_xt_nguyenvong_once (thisinh_id, nganh_id, nganh_tohop_id, phuongthuc_id),
    KEY idx_xt_nguyenvong_nganh (nganh_id),
    KEY idx_xt_nguyenvong_nganh_tohop (nganh_tohop_id),
    KEY idx_xt_nguyenvong_pt (phuongthuc_id),
    CONSTRAINT fk_xt_nguyenvong_thisinh
        FOREIGN KEY (thisinh_id) REFERENCES xt_thisinh(thisinh_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_xt_nguyenvong_nganh
        FOREIGN KEY (nganh_id) REFERENCES xt_nganh(nganh_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_xt_nguyenvong_nganh_tohop_pair
        FOREIGN KEY (nganh_tohop_id, nganh_id) REFERENCES xt_nganh_tohop(nganh_tohop_id, nganh_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_xt_nguyenvong_pt
        FOREIGN KEY (phuongthuc_id) REFERENCES xt_phuongthuc(phuongthuc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO xt_vaitro (ma_vaitro, ten_vaitro) VALUES
('ADMIN', 'Admin'),
('USER', 'User');

INSERT INTO xt_phuongthuc (ma_phuongthuc, ten_phuongthuc, thang_diem) VALUES
('THPT', 'Xet diem THPT', 30.00),
('VSAT', 'Xet diem V-SAT', 30.00),
('DGNL', 'Xet diem DGNL', 30.00),
('XTT', 'Xet tuyen thang', 30.00);

INSERT INTO xt_mon (ma_mon, ten_mon, loai_mon) VALUES
('TO', 'Toan', 'MON_HOC'),
('LI', 'Vat ly', 'MON_HOC'),
('HO', 'Hoa hoc', 'MON_HOC'),
('SI', 'Sinh hoc', 'MON_HOC'),
('VA', 'Ngu van', 'MON_HOC'),
('SU', 'Lich su', 'MON_HOC'),
('DI', 'Dia ly', 'MON_HOC'),
('N1', 'Ngoai ngu', 'MON_HOC'),
('TI', 'Tin hoc', 'MON_HOC'),
('KTPL', 'Giao duc kinh te va phap luat', 'MON_HOC'),
('CNCN', 'Cong nghe cong nghiep', 'MON_HOC'),
('CNNN', 'Cong nghe nong nghiep', 'MON_HOC'),
('NL1', 'Danh gia nang luc', 'DANH_GIA_NANG_LUC'),
('NK1', 'Nang khieu 1', 'NANG_KHIEU'),
('NK2', 'Nang khieu 2', 'NANG_KHIEU');

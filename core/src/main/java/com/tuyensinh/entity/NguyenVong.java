package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_nguyenvong")
@ToString(exclude = {"thiSinh", "maXetTuyenMap", "nganh", "nganhToHop", "phuongThuc"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NguyenVong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nguyenvong_id")
    private Integer nguyenvongId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thisinh_id", nullable = false)
    private ThiSinh thiSinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_xettuyen_id")
    private MaXetTuyenMap maXetTuyenMap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nganh_id", nullable = false)
    private Nganh nganh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nganh_tohop_id", nullable = false)
    private NganhToHop nganhToHop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phuongthuc_id", nullable = false)
    private PhuongThuc phuongThuc;

    @Column(name = "thu_tu", nullable = false)
    private Integer thuTu;

    @Column(name = "diem_thxt", precision = 10, scale = 5)
    private BigDecimal diemThxt;

    @Column(name = "diem_thgxt", precision = 10, scale = 5)
    private BigDecimal diemThgxt;

    @Column(name = "diem_cong", precision = 6, scale = 2)
    private BigDecimal diemCong;

    @Column(name = "diem_uutien", precision = 6, scale = 2)
    private BigDecimal diemUutien;

    @Column(name = "diem_xettuyen", precision = 10, scale = 5)
    private BigDecimal diemXettuyen;

    @Column(name = "ket_qua", length = 45)
    private String ketQua;

    /**
     * Nguon diem sau khi so sanh THPT / VSAT / DGNL.
     * Gia tri du kien: THPT, VSAT, DGNL.
     */
    @Column(name = "phuong_thuc_diem_tot_nhat", length = 20)
    private String phuongThucDiemTotNhat;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    public static final class KetQua {
        public static final String CHO_XET = "CHO_XET";
        public static final String TRUNG_TUYEN = "TRUNG_TUYEN";
        public static final String TRUOT = "TRUOT";
        public static final String PHOI_DU_KIEN = "PHOI_DU_KIEN";
    }
}
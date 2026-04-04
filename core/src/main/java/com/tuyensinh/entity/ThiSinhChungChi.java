package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "xt_thisinh_chungchi")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"thiSinh"})
public class ThiSinhChungChi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chungchi_id")
    private Integer chungchiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thisinh_id", nullable = false)
    private ThiSinh thiSinh;

    @Column(name = "loai_chungchi", nullable = false, length = 50)
    private String loaiChungChi;

    @Column(name = "ten_chungchi", length = 255)
    private String tenChungChi;

    @Column(name = "diem_goc", precision = 6, scale = 2)
    private BigDecimal diemGoc;

    @Column(name = "bac_chungchi", length = 50)
    private String bacChungChi;

    @Column(name = "so_hieu", length = 100)
    private String soHieu;

    @Column(name = "don_vi_cap", length = 255)
    private String donViCap;

    @Column(name = "ngay_cap")
    private LocalDate ngayCap;

    @Column(name = "ngay_het_han")
    private LocalDate ngayHetHan;

    @Column(name = "is_hop_le", nullable = false)
    private Boolean isHopLe = true;

    @Column(name = "trang_thai_xac_minh", nullable = false, length = 30)
    private String trangThaiXacMinh = "CHUA_XAC_MINH";

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
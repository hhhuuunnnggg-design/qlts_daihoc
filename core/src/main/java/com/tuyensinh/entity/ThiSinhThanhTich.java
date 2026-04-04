package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "xt_thisinh_thanh_tich")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"thiSinh"})
public class ThiSinhThanhTich {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "thanhtich_id")
    private Integer thanhtichId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thisinh_id", nullable = false)
    private ThiSinh thiSinh;

    @Column(name = "nhom_thanh_tich", nullable = false, length = 50)
    private String nhomThanhTich;

    @Column(name = "cap_thanh_tich", length = 50)
    private String capThanhTich;

    @Column(name = "loai_giai", length = 50)
    private String loaiGiai;

    @Column(name = "ten_thanh_tich", length = 255)
    private String tenThanhTich;

    @Column(name = "mon_dat_giai", length = 100)
    private String monDatGiai;

    @Column(name = "linh_vuc", length = 100)
    private String linhVuc;

    @Column(name = "nam_dat_giai")
    private Short namDatGiai;

    @Column(name = "don_vi_to_chuc", length = 255)
    private String donViToChuc;

    @Column(name = "so_hieu_minh_chung", length = 100)
    private String soHieuMinhChung;

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
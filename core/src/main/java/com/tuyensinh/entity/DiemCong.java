package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "xt_diemcong",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_xt_diemcong_unique",
                        columnNames = {"thisinh_id", "nganh_tohop_id", "phuongthuc_id"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"thiSinh", "nganhToHop", "phuongThuc", "chiTietList"})
public class DiemCong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diemcong_id")
    private Integer diemcongId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thisinh_id", nullable = false)
    private ThiSinh thiSinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nganh_tohop_id", nullable = false)
    private NganhToHop nganhToHop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phuongthuc_id", nullable = false)
    private PhuongThuc phuongThuc;

    @Column(name = "tong_diem_chungchi", nullable = false, precision = 6, scale = 2)
    private BigDecimal tongDiemChungChi = BigDecimal.ZERO;

    @Column(name = "tong_diem_uutien_xt", nullable = false, precision = 6, scale = 2)
    private BigDecimal tongDiemUutienXt = BigDecimal.ZERO;

    @Column(name = "tong_diem_uutien_quyche", nullable = false, precision = 6, scale = 2)
    private BigDecimal tongDiemUutienQuyChe = BigDecimal.ZERO;

    @Column(name = "tong_diem_cong", nullable = false, precision = 6, scale = 2)
    private BigDecimal tongDiemCong = BigDecimal.ZERO;

    @Column(name = "ghi_chu_tong", columnDefinition = "TEXT")
    private String ghiChuTong;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "diemCong", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiemCongChiTiet> chiTietList = new ArrayList<>();
}
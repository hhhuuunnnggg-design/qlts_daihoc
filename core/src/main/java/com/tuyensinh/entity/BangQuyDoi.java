package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "xt_bangquydoi")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BangQuyDoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bangquydoi_id")
    private Integer bangquydoiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phuongthuc_id", nullable = false)
    private PhuongThuc phuongThuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tohop_id")
    private ToHop toHop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mon_id")
    private Mon mon;

    @Column(name = "diem_tu", nullable = false, precision = 6, scale = 2)
    private BigDecimal diemTu;

    @Column(name = "diem_den", nullable = false, precision = 6, scale = 2)
    private BigDecimal diemDen;

    @Column(name = "diem_quydoi_tu", nullable = false, precision = 6, scale = 2)
    private BigDecimal diemQuydoiTu;

    @Column(name = "diem_quydoi_den", nullable = false, precision = 6, scale = 2)
    private BigDecimal diemQuydoiDen;

    @Column(name = "phan_vi")
    private Integer phanVi;

    @Column(name = "ma_quydoi", nullable = false, unique = true, length = 50)
    private String maQuydoi;
}

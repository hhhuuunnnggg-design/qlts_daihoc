package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_diemcong")
@ToString(exclude = {"nganhToHop"})
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "diem_chungchi", nullable = false, precision = 6, scale = 2)
    private BigDecimal diemChungchi = BigDecimal.ZERO;

    @Column(name = "diem_uutien_xt", nullable = false, precision = 6, scale = 2)
    private BigDecimal diemUutienXt = BigDecimal.ZERO;

    @Column(name = "diem_tong", nullable = false, precision = 6, scale = 2)
    private BigDecimal diemTong = BigDecimal.ZERO;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;
}

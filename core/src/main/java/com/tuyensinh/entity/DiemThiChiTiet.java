package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_diemthi_chitiet")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiemThiChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diemthi_ct_id")
    private Long diemthiCtId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diemthi_id", nullable = false)
    private DiemThi diemThi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mon_id", nullable = false)
    private Mon mon;

    @Column(name = "diem_goc", precision = 8, scale = 2)
    private BigDecimal diemGoc;

    @Column(name = "diem_quydoi", precision = 8, scale = 2)
    private BigDecimal diemQuydoi;

    @Column(name = "diem_sudung", precision = 8, scale = 2)
    private BigDecimal diemSudung;
}

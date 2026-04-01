package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "xt_nganh")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nganh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nganh_id")
    private Integer nganhId;

    @Column(name = "ma_nganh", nullable = false, unique = true, length = 20)
    private String maNganh;

    @Column(name = "ten_nganh", nullable = false, length = 150)
    private String tenNganh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tohop_goc_id")
    private ToHop toHopGoc;

    @Column(name = "chi_tieu", nullable = false)
    private Integer chiTieu = 0;

    @Column(name = "diem_san", precision = 10, scale = 2)
    private BigDecimal diemSan;

    @Column(name = "diem_trung_tuyen", precision = 10, scale = 2)
    private BigDecimal diemTrungTuyen;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "nganh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NganhPhuongThuc> danhSachNganhPhuongThuc;

    @OneToMany(mappedBy = "nganh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NganhToHop> danhSachNganhToHop;

    @OneToMany(mappedBy = "nganh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NguyenVong> danhSachNguyenVong;
}

package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "xt_nganh_phuongthuc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NganhPhuongThuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nganh_phuongthuc_id")
    private Integer nganhPhuongthucId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nganh_id", nullable = false)
    private Nganh nganh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phuongthuc_id", nullable = false)
    private PhuongThuc phuongThuc;

    @Column(name = "chi_tieu")
    private Integer chiTieu;

    @Column(name = "so_luong_hien_tai")
    private Integer soLuongHienTai = 0;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;
}

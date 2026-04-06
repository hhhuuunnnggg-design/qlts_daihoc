package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(
        name = "xt_ma_xettuyen",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_maxt_combo",
                        columnNames = {"ma_xet_tuyen", "phuongthuc_id", "ma_tohop_nguon"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"nganh", "phuongThuc", "nganhToHop"})
public class MaXetTuyenMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_xettuyen_id")
    private Integer maXettuyenId;

    @Column(name = "ma_xet_tuyen", nullable = false, length = 30)
    private String maXetTuyen;

    @Column(name = "ten_chuong_trinh", length = 255)
    private String tenChuongTrinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nganh_id", nullable = false)
    private Nganh nganh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phuongthuc_id", nullable = false)
    private PhuongThuc phuongThuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nganh_tohop_id")
    private NganhToHop nganhToHop;

    @Column(name = "ma_tohop_nguon", nullable = false, length = 20)
    private String maTohopNguon = "";

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
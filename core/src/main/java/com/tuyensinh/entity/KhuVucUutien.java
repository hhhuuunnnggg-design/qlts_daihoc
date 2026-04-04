package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "xt_khuvuc_uutien")
@ToString(exclude = {"danhSachThiSinh"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KhuVucUutien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "khuvuc_id")
    private Integer khuvucId;

    @Column(name = "ma_khuvuc", nullable = false, unique = true, length = 20)
    private String maKhuvuc;

    @Column(name = "ten_khuvuc", nullable = false, length = 100)
    private String tenKhuvuc;

    @Column(name = "muc_diem", nullable = false, precision = 4, scale = 2)
    private BigDecimal mucDiem = BigDecimal.ZERO;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @OneToMany(mappedBy = "khuVucUutien", fetch = FetchType.LAZY)
    private List<ThiSinh> danhSachThiSinh;

    public String getMaKhuVuc() {
        return this.maKhuvuc;
    }
}

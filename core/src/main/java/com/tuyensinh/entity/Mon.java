package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "xt_mon")
@ToString(exclude = {"danhSachToHopMon", "danhSachDiemChiTiet"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mon_id")
    private Integer monId;

    @Column(name = "ma_mon", nullable = false, unique = true, length = 20)
    private String maMon;

    @Column(name = "ten_mon", nullable = false, length = 100)
    private String tenMon;

    @Column(name = "loai_mon", nullable = false, length = 30)
    private String loaiMon = LoaiMon.MON_HOC;

    @OneToMany(mappedBy = "mon", fetch = FetchType.LAZY)
    private List<ToHopMon> danhSachToHopMon;

    @OneToMany(mappedBy = "mon", fetch = FetchType.LAZY)
    private List<DiemThiChiTiet> danhSachDiemChiTiet;

    public static final class LoaiMon {
        public static final String MON_HOC              = "MON_HOC";
        public static final String DANH_GIA_NANG_LUC     = "DANH_GIA_NANG_LUC";
        public static final String NANG_KHIEU             = "NANG_KHIEU";
    }
}

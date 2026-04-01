package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "xt_phuongthuc")
@ToString(exclude = {"danhSachDiemThi", "danhSachNganhPhuongThuc"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhuongThuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "phuongthuc_id")
    private Short phuongthucId;

    @Column(name = "ma_phuongthuc", nullable = false, unique = true, length = 20)
    private String maPhuongthuc;

    @Column(name = "ten_phuongthuc", nullable = false, length = 100)
    private String tenPhuongthuc;

    @Column(name = "thang_diem", nullable = false, precision = 6, scale = 2)
    private BigDecimal thangDiem = new BigDecimal("30.00");

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "phuongThuc", fetch = FetchType.LAZY)
    private List<DiemThi> danhSachDiemThi;

    @OneToMany(mappedBy = "phuongThuc", fetch = FetchType.LAZY)
    private List<NganhPhuongThuc> danhSachNganhPhuongThuc;

    public static final String XTT  = "XTT";
    public static final String VHAT = "VHAT";
    public static final String DGNL = "DGNL";
    public static final String THPT = "THPT";
    public static final String NK   = "NK";
}

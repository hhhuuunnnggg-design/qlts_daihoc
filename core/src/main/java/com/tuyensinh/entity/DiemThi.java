package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "xt_diemthi")
@ToString(exclude = {"danhSachDiemChiTiet", "thiSinh"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiemThi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diemthi_id")
    private Integer diemthiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thisinh_id", nullable = false)
    private ThiSinh thiSinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phuongthuc_id", nullable = false)
    private PhuongThuc phuongThuc;

    @Column(name = "sobaodanh", length = 45)
    private String sobaodanh;

    @Column(name = "nam_tuyensinh", nullable = false)
    private Short namTuyensinh = 2026;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @CreationTimestamp
    @Column(name = "imported_at", nullable = false, updatable = false)
    private LocalDateTime importedAt;

    @OneToMany(mappedBy = "diemThi", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiemThiChiTiet> danhSachDiemChiTiet = new ArrayList<>();
}

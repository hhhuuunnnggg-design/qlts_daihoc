package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "xt_thisinh")
@ToString(exclude = {"danhSachDiemThi", "danhSachDiemCong", "danhSachNguyenVong", "nguoiDung"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThiSinh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "thisinh_id")
    private Integer thisinhId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoidung_id", unique = true)
    private NguoiDung nguoiDung;

    @Column(name = "cccd", nullable = false, unique = true, length = 20)
    private String cccd;

    @Column(name = "sobaodanh", unique = true, length = 45)
    private String sobaodanh;

    @Column(name = "ho", nullable = false, length = 100)
    private String ho;

    @Column(name = "ten", nullable = false, length = 100)
    private String ten;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh", length = 10)
    private String gioiTinh;

    @Column(name = "dien_thoai", length = 20)
    private String dienThoai;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "noi_sinh", length = 100)
    private String noiSinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doituong_id")
    private DoiTuongUutien doiTuongUutien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khuvuc_id")
    private KhuVucUutien khuVucUutien;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "thiSinh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiemThi> danhSachDiemThi;

    @OneToMany(mappedBy = "thiSinh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiemCong> danhSachDiemCong;

    @OneToMany(mappedBy = "thiSinh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NguyenVong> danhSachNguyenVong;

    public String getHoVaTen() {
        return (ho != null ? ho : "") + " " + (ten != null ? ten : "");
    }

    /** Tra ve chuoi ngay sinh dang dd/MM/yyyy, tien cho hien thi tren JSP. */
    public String getNgaySinhDisplay() {
        return com.tuyensinh.util.DateUtil.formatDate(ngaySinh);
    }
}

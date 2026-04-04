package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "xt_diemcong_chitiet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"diemCong"})
public class DiemCongChiTiet {

    public enum LoaiNguon {
        CC_NGOAI_NGU,       // Nguồn cộng/quy đổi từ chứng chỉ ngoại ngữ.
        UTXT_HSG_QUOCGIA,   // Ưu tiên xét tuyển do thành tích học sinh giỏi cấp quốc gia.
        UTXT_HSG_TINH,      // Ưu tiên xét tuyển do thành tích học sinh giỏi cấp tỉnh/thành.
        UTXT_KHKT,          // Ưu tiên xét tuyển do giải khoa học kỹ thuật.
        UTXT_NGHE_THUAT,    // Ưu tiên xét tuyển do thành tích nghệ thuật, thể thao hoặc nhóm giải đặc thù
        UUTIEN_KHUVUC,      // Điểm ưu tiên quy chế theo khu vực.
        UUTIEN_DOITUONG     // Điểm ưu tiên quy chế theo đối tượng.
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diemcong_ct_id")
    private Integer diemcongCtId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diemcong_id", nullable = false)
    private DiemCong diemCong;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_nguon", nullable = false, length = 50)
    private LoaiNguon loaiNguon;

    @Column(name = "ma_nguon", length = 50)
    private String maNguon;

    @Column(name = "ten_nguon", length = 255)
    private String tenNguon;

    @Column(name = "cap_ap_dung", length = 50)
    private String capApDung;

    @Column(name = "mon_lien_quan", length = 50)
    private String monLienQuan;

    @Column(name = "gia_tri_goc", length = 100)
    private String giaTriGoc;

    @Column(name = "diem_quy_doi", nullable = false, precision = 6, scale = 2)
    private BigDecimal diemQuyDoi = BigDecimal.ZERO;

    @Column(name = "diem_cong", nullable = false, precision = 6, scale = 2)
    private BigDecimal diemCongGiaTri = BigDecimal.ZERO;

    @Column(name = "thu_tu_uu_tien", nullable = false)
    private Short thuTuUuTien = 1;

    @Column(name = "is_ap_dung", nullable = false)
    private Boolean isApDung = true;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
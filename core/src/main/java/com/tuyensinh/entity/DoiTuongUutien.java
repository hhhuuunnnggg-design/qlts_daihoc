package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "xt_doituong_uutien")
@ToString(exclude = {"danhSachThiSinh"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoiTuongUutien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doituong_id")
    private Integer doituongId;

    @Column(name = "ma_doituong", nullable = false, unique = true, length = 20)
    private String maDoituong;

    @Column(name = "ten_doituong", nullable = false, length = 100)
    private String tenDoituong;

    @Column(name = "muc_diem", nullable = false, precision = 4, scale = 2)
    private BigDecimal mucDiem = BigDecimal.ZERO;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @OneToMany(mappedBy = "doiTuongUutien", fetch = FetchType.LAZY)
    private List<ThiSinh> danhSachThiSinh;
}

package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "xt_vaitro")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaiTro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vaitro_id")
    private Short vaitroId;

    @Column(name = "ma_vaitro", nullable = false, unique = true, length = 20)
    private String maVaitro;

    @Column(name = "ten_vaitro", nullable = false, length = 50)
    private String tenVaitro;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    @OneToMany(mappedBy = "vaiTro", fetch = FetchType.LAZY)
    private List<NguoiDung> danhSachNguoiDung;

    public static final String ADMIN = "ADMIN";
    public static final String USER  = "USER";
}

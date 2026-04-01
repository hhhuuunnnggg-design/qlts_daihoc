package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "xt_nguoidung")
@ToString(exclude = {"danhSachThiSinh", "vaiTro"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nguoidung_id")
    private Integer nguoidungId;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "ho_ten", length = 150)
    private String hoTen;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaitro_id", nullable = false)
    private VaiTro vaiTro;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "nguoiDung", fetch = FetchType.LAZY)
    private List<ThiSinh> danhSachThiSinh;

    public boolean isAdmin() {
        return vaiTro != null && VaiTro.ADMIN.equals(vaiTro.getMaVaitro());
    }
}

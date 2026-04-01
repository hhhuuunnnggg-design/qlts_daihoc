package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "xt_nganh_tohop")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NganhToHop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nganh_tohop_id")
    private Integer nganhTohopId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nganh_id", nullable = false)
    private Nganh nganh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tohop_id", nullable = false)
    private ToHop toHop;

    @Column(name = "do_lech", nullable = false, precision = 6, scale = 2)
    private BigDecimal doLech = BigDecimal.ZERO;

    @OneToMany(mappedBy = "nganhToHop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NganhToHopMon> danhSachNganhToHopMon;

    @OneToMany(mappedBy = "nganhToHop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiemCong> danhSachDiemCong;

    @OneToMany(mappedBy = "nganhToHop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NguyenVong> danhSachNguyenVong;
}

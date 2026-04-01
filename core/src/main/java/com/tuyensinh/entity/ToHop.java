package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "xt_tohop")
@ToString(exclude = {"danhSachToHopMon", "danhSachNganhToHop"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToHop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tohop_id")
    private Integer tohopId;

    @Column(name = "ma_tohop", nullable = false, unique = true, length = 20)
    private String maTohop;

    @Column(name = "ten_tohop", length = 100)
    private String tenTohop;

    @OneToMany(mappedBy = "toHop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ToHopMon> danhSachToHopMon;

    @OneToMany(mappedBy = "toHop", fetch = FetchType.LAZY)
    private List<NganhToHop> danhSachNganhToHop;

    @OneToMany(mappedBy = "toHopGoc", fetch = FetchType.LAZY)
    private List<Nganh> danhSachNganhGoc;
}

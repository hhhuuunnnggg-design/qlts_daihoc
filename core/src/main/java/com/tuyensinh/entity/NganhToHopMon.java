package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "xt_nganh_tohop_mon")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NganhToHopMon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nganh_tohop_mon_id")
    private Integer nganhTohopMonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nganh_tohop_id", nullable = false)
    private NganhToHop nganhToHop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mon_id", nullable = false)
    private Mon mon;

    @Column(name = "he_so", nullable = false)
    private Short heSo = 1;

    @Column(name = "is_mon_chinh", nullable = false)
    private Boolean isMonChinh = false;
}

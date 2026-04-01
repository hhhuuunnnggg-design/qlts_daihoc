package com.tuyensinh.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "xt_tohop_mon")
@ToString(exclude = {})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToHopMon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tohop_mon_id")
    private Integer tohopMonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tohop_id", nullable = false)
    private ToHop toHop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mon_id", nullable = false)
    private Mon mon;

    @Column(name = "thu_tu", nullable = false)
    private Short thuTu;
}

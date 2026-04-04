package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.ThiSinhChungChi;

import java.util.List;

public interface IThiSinhChungChiService {

    List<ThiSinhChungChi> findAll();

    ThiSinhChungChi findById(Integer id);

    List<ThiSinhChungChi> findByThiSinhId(Integer thisinhId);

    List<ThiSinhChungChi> findHopLeByThiSinhId(Integer thisinhId);

    List<ThiSinhChungChi> findByLoaiChungChi(String loaiChungChi);

    ThiSinhChungChi save(ThiSinhChungChi entity);

    void update(ThiSinhChungChi entity);

    void delete(ThiSinhChungChi entity);
}
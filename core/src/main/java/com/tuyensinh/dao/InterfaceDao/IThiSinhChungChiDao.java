package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.ThiSinhChungChi;

import java.util.List;

public interface IThiSinhChungChiDao extends IBaseDao<ThiSinhChungChi> {

    List<ThiSinhChungChi> findByThiSinhId(Integer thisinhId);

    List<ThiSinhChungChi> findHopLeByThiSinhId(Integer thisinhId);

    List<ThiSinhChungChi> findByLoaiChungChi(String loaiChungChi);
}
package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.ThiSinhThanhTich;

import java.util.List;

public interface IThiSinhThanhTichDao extends IBaseDao<ThiSinhThanhTich> {

    List<ThiSinhThanhTich> findByThiSinhId(Integer thisinhId);

    List<ThiSinhThanhTich> findHopLeByThiSinhId(Integer thisinhId);

    List<ThiSinhThanhTich> findByNhomThanhTich(String nhomThanhTich);

    List<ThiSinhThanhTich> findByCapThanhTich(String capThanhTich);
}
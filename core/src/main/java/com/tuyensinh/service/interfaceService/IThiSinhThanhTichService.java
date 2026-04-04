package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.ThiSinhThanhTich;

import java.util.List;

public interface IThiSinhThanhTichService {

    List<ThiSinhThanhTich> findAll();

    ThiSinhThanhTich findById(Integer id);

    List<ThiSinhThanhTich> findByThiSinhId(Integer thisinhId);

    List<ThiSinhThanhTich> findHopLeByThiSinhId(Integer thisinhId);

    List<ThiSinhThanhTich> findByNhomThanhTich(String nhomThanhTich);

    List<ThiSinhThanhTich> findByCapThanhTich(String capThanhTich);

    ThiSinhThanhTich save(ThiSinhThanhTich entity);

    void update(ThiSinhThanhTich entity);

    void delete(ThiSinhThanhTich entity);
}
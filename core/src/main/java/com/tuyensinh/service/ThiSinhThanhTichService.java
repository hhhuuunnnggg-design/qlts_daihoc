package com.tuyensinh.service;

import com.tuyensinh.dao.ThiSinhThanhTichDao;
import com.tuyensinh.entity.ThiSinhThanhTich;
import com.tuyensinh.service.interfaceService.IThiSinhThanhTichService;

import java.util.List;

public class ThiSinhThanhTichService implements IThiSinhThanhTichService {

    private final ThiSinhThanhTichDao dao = new ThiSinhThanhTichDao();

    @Override
    public List<ThiSinhThanhTich> findAll() {
        return dao.findAll();
    }

    @Override
    public ThiSinhThanhTich findById(Integer id) {
        return dao.findById(id);
    }

    @Override
    public List<ThiSinhThanhTich> findByThiSinhId(Integer thisinhId) {
        return dao.findByThiSinhId(thisinhId);
    }

    @Override
    public List<ThiSinhThanhTich> findHopLeByThiSinhId(Integer thisinhId) {
        return dao.findHopLeByThiSinhId(thisinhId);
    }

    @Override
    public List<ThiSinhThanhTich> findByNhomThanhTich(String nhomThanhTich) {
        return dao.findByNhomThanhTich(nhomThanhTich);
    }

    @Override
    public List<ThiSinhThanhTich> findByCapThanhTich(String capThanhTich) {
        return dao.findByCapThanhTich(capThanhTich);
    }

    @Override
    public ThiSinhThanhTich save(ThiSinhThanhTich entity) {
        return dao.save(entity);
    }

    @Override
    public void update(ThiSinhThanhTich entity) {
        dao.update(entity);
    }

    @Override
    public void delete(ThiSinhThanhTich entity) {
        dao.delete(entity);
    }
}
package com.tuyensinh.service;

import com.tuyensinh.dao.ThiSinhChungChiDao;
import com.tuyensinh.entity.ThiSinhChungChi;
import com.tuyensinh.service.interfaceService.IThiSinhChungChiService;

import java.util.List;

public class ThiSinhChungChiService implements IThiSinhChungChiService {

    private final ThiSinhChungChiDao dao = new ThiSinhChungChiDao();

    @Override
    public List<ThiSinhChungChi> findAll() {
        return dao.findAll();
    }

    @Override
    public ThiSinhChungChi findById(Integer id) {
        return dao.findById(id);
    }

    @Override
    public List<ThiSinhChungChi> findByThiSinhId(Integer thisinhId) {
        return dao.findByThiSinhId(thisinhId);
    }

    @Override
    public List<ThiSinhChungChi> findHopLeByThiSinhId(Integer thisinhId) {
        return dao.findHopLeByThiSinhId(thisinhId);
    }

    @Override
    public List<ThiSinhChungChi> findByLoaiChungChi(String loaiChungChi) {
        return dao.findByLoaiChungChi(loaiChungChi);
    }

    @Override
    public ThiSinhChungChi save(ThiSinhChungChi entity) {
        return dao.save(entity);
    }

    @Override
    public void update(ThiSinhChungChi entity) {
        dao.update(entity);
    }

    @Override
    public void delete(ThiSinhChungChi entity) {
        dao.delete(entity);
    }
}
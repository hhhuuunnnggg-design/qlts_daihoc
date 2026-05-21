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

    public List<ThiSinhChungChi> findPage(int page, int pageSize) {
        return dao.findPage(page, pageSize);
    }

    public long countAll() {
        return dao.countAll();
    }

    public List<ThiSinhChungChi> searchPage(String keyword, int page, int pageSize) {
        return dao.searchPage(keyword, page, pageSize);
    }

    public long countSearch(String keyword) {
        return dao.countSearch(keyword);
    }

    public List<ThiSinhChungChi> searchPage(String field, String keyword, int page, int pageSize) {
        return dao.searchPageByField(field, keyword, page, pageSize);
    }

    public long countSearch(String field, String keyword) {
        return dao.countSearchByField(field, keyword);
    }

    public void updateXacMinh(Integer chungchiId, String trangThaiXacMinh, boolean hopLe) {
        ThiSinhChungChi entity = dao.findById(chungchiId);
        if (entity == null) {
            throw new IllegalArgumentException("Khong tim thay chung chi id=" + chungchiId);
        }
        entity.setTrangThaiXacMinh(trangThaiXacMinh);
        entity.setIsHopLe(hopLe);
        dao.update(entity);
    }

}
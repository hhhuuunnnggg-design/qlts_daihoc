package com.tuyensinh.service;

import com.tuyensinh.dao.*;
import com.tuyensinh.entity.*;
import com.tuyensinh.service.interfaceService.IThiSinhService;

import java.util.List;
import java.util.Optional;

public class ThiSinhService implements IThiSinhService {

    private final ThiSinhDao dao = new ThiSinhDao();
    private final DiemThiDao diemThiDao = new DiemThiDao();

    public List<ThiSinh> findAll() {
        return dao.findAll();
    }

    public List<ThiSinh> findByPage(int page, int pageSize) {
        return dao.findByPage(page, pageSize);
    }

    public List<ThiSinh> findByPageWithSearch(String keyword, int page, int pageSize) {
        return dao.findByPageWithSearch(keyword, page, pageSize);
    }

    public ThiSinh findById(Integer id) {
        return dao.findById(id);
    }

    public Optional<ThiSinh> findByCccd(String cccd) {
        return dao.findByCccd(cccd);
    }

    public Optional<ThiSinh> findBySoBaoDanh(String sobaodanh) {
        return dao.findBySoBaoDanh(sobaodanh);
    }

    public ThiSinh save(ThiSinh ts) {
        return dao.save(ts);
    }

    public void update(ThiSinh ts) {
        dao.update(ts);
    }

    public void delete(ThiSinh ts) {
        dao.delete(ts);
    }

    public List<ThiSinh> search(String keyword) {
        return dao.searchByCccdOrHoTen(keyword);
    }

    public long countBySearch(String keyword) {
        return dao.countBySearch(keyword);
    }

    public String generateSoBaoDanh() {
        return dao.generateSoBaoDanh();
    }

    public List<DiemThi> getDiemThiList(Integer thisinhId) {
        return diemThiDao.findByThiSinhId(thisinhId);
    }

    public int getTotalPages(long total, int pageSize) {
        return (int) Math.ceil((double) total / pageSize);
    }
}

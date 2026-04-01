package com.tuyensinh.service;

import com.tuyensinh.dao.MonDao;
import com.tuyensinh.entity.Mon;
import java.util.List;

public class MonService {

    private final MonDao dao = new MonDao();

    public List<Mon> findAll() {
        return dao.findAll();
    }

    public List<Mon> findByLoaiMon(String loaiMon) {
        return dao.findByLoaiMon(loaiMon);
    }

    public List<Mon> findNangKhieuMon() {
        return dao.findNangKhieuMon();
    }

    public List<Mon> findDanhGiaNangLuc() {
        return dao.findDanhGiaNangLuc();
    }

    public List<Mon> findMonHoc() {
        return dao.findMonHoc();
    }

    public Mon findById(Integer id) {
        return dao.findById(id);
    }

    public Mon save(Mon entity) {
        return dao.save(entity);
    }

    public void update(Mon entity) {
        dao.update(entity);
    }
}

package com.tuyensinh.service;

import com.tuyensinh.dao.NguyenVongDao;
import com.tuyensinh.entity.NguyenVong;
import com.tuyensinh.service.interfaceService.INguyenVongService;

import java.util.List;

public class NguyenVongService implements INguyenVongService {

    private final NguyenVongDao dao = new NguyenVongDao();

    public List<NguyenVong> findAll() {
        return dao.findAll();
    }

    public List<NguyenVong> findByThiSinhId(Integer thisinhId) {
        return dao.findByThiSinhId(thisinhId);
    }

    public NguyenVong findById(Integer id) {
        return dao.findById(id);
    }

    public NguyenVong save(NguyenVong entity) {
        return dao.save(entity);
    }

    public void update(NguyenVong entity) {
        dao.update(entity);
    }

    public void delete(NguyenVong entity) {
        dao.delete(entity);
    }

    public List<NguyenVong> findByKetQua(String ketQua) {
        return dao.findByKetQua(ketQua);
    }

    public int countByNganhAndPhuongThuc(Integer nganhId, Short phuongthucId, String ketQua) {
        return dao.countByNganhAndPhuongThuc(nganhId, phuongthucId, ketQua);
    }
}

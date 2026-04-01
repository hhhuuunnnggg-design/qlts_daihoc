package com.tuyensinh.service;

import com.tuyensinh.dao.DiemCongDao;
import com.tuyensinh.entity.DiemCong;
import java.util.List;

public class DiemCongService {

    private final DiemCongDao dao = new DiemCongDao();

    public List<DiemCong> findAll() {
        return dao.findAll();
    }

    public DiemCong findById(Integer id) {
        return dao.findById(id);
    }

    public List<DiemCong> findByThiSinhId(Integer thisinhId) {
        return dao.findByThiSinhId(thisinhId);
    }

    public DiemCong save(DiemCong entity) {
        return dao.save(entity);
    }

    public void update(DiemCong entity) {
        dao.update(entity);
    }

    public void delete(DiemCong entity) {
        dao.delete(entity);
    }
}

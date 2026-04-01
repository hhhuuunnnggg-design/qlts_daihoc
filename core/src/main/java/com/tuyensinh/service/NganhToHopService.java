package com.tuyensinh.service;

import com.tuyensinh.dao.NganhToHopDao;
import com.tuyensinh.entity.NganhToHop;
import java.util.List;

public class NganhToHopService {

    private final NganhToHopDao dao = new NganhToHopDao();

    public List<NganhToHop> findAll() {
        return dao.findAll();
    }

    public List<NganhToHop> findByNganhId(Integer nganhId) {
        return dao.findByNganhId(nganhId);
    }

    public NganhToHop findById(Integer id) {
        return dao.findById(id);
    }

    public NganhToHop save(NganhToHop entity) {
        return dao.save(entity);
    }

    public void delete(NganhToHop entity) {
        dao.delete(entity);
    }
}

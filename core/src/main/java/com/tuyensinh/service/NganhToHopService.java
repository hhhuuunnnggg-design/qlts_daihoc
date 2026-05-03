package com.tuyensinh.service;

import com.tuyensinh.dao.NganhToHopDao;
import com.tuyensinh.entity.NganhToHop;
import com.tuyensinh.service.interfaceService.INganhToHopService;

import java.util.List;

public class NganhToHopService implements INganhToHopService {

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

    public void update(NganhToHop entity) {
        dao.update(entity);
    }

    public void delete(NganhToHop entity) {
        dao.delete(entity);
    }

    public List<NganhToHop> findPage(int page, int pageSize) {
        return dao.findPage(page, pageSize);
    }

    public long countAll() {
        return dao.countAll();
    }

    public List<NganhToHop> searchPage(String keyword, int page, int pageSize) {
        return dao.searchPage(keyword, page, pageSize);
    }

    public long countSearch(String keyword) {
        return dao.countSearch(keyword);
    }

}

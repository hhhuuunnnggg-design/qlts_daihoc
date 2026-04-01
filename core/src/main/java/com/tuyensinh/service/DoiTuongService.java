package com.tuyensinh.service;

import com.tuyensinh.dao.DoiTuongUutienDao;
import com.tuyensinh.entity.DoiTuongUutien;
import com.tuyensinh.service.interfaceService.IDoiTuongService;

import java.util.List;

public class DoiTuongService implements IDoiTuongService {

    private final DoiTuongUutienDao dao = new DoiTuongUutienDao();

    public List<DoiTuongUutien> findAll() {
        return dao.findAll();
    }

    public DoiTuongUutien findById(Integer id) {
        return dao.findById(id);
    }

    public DoiTuongUutien save(DoiTuongUutien d) {
        return dao.save(d);
    }

    public void update(DoiTuongUutien d) {
        dao.update(d);
    }

    public void delete(DoiTuongUutien d) {
        dao.delete(d);
    }
}

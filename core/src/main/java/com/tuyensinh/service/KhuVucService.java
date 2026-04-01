package com.tuyensinh.service;

import com.tuyensinh.dao.KhuVucUutienDao;
import com.tuyensinh.entity.KhuVucUutien;
import com.tuyensinh.service.interfaceService.IKhuVucService;

import java.util.List;

public class KhuVucService implements IKhuVucService {

    private final KhuVucUutienDao dao = new KhuVucUutienDao();

    public List<KhuVucUutien> findAll() {
        return dao.findAll();
    }

    public KhuVucUutien findById(Integer id) {
        return dao.findById(id);
    }

    public KhuVucUutien save(KhuVucUutien k) {
        return dao.save(k);
    }

    public void update(KhuVucUutien k) {
        dao.update(k);
    }

    public void delete(KhuVucUutien k) {
        dao.delete(k);
    }
}

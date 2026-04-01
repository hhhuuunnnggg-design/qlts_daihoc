package com.tuyensinh.service;

import com.tuyensinh.dao.KhuVucUutienDao;
import com.tuyensinh.entity.KhuVucUutien;
import java.util.List;

public class KhuVucService {

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

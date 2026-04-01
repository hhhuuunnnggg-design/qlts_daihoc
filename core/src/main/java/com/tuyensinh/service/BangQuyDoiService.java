package com.tuyensinh.service;

import com.tuyensinh.dao.BangQuyDoiDao;
import com.tuyensinh.entity.BangQuyDoi;
import java.util.List;

public class BangQuyDoiService {

    private final BangQuyDoiDao dao = new BangQuyDoiDao();

    public List<BangQuyDoi> findAll() {
        return dao.findAll();
    }

    public BangQuyDoi findById(Integer id) {
        return dao.findById(id);
    }

    public BangQuyDoi findByMa(String maQuydoi) {
        return dao.findByMa(maQuydoi).orElse(null);
    }

    public List<BangQuyDoi> findByPhuongThuc(Short phuongthucId) {
        return dao.findByPhuongThuc(phuongthucId);
    }

    public List<BangQuyDoi> search(String keyword) {
        return dao.search(keyword);
    }

    public BangQuyDoi save(BangQuyDoi entity) {
        return dao.save(entity);
    }

    public void update(BangQuyDoi entity) {
        dao.update(entity);
    }

    public void delete(BangQuyDoi entity) {
        dao.delete(entity);
    }
}

package com.tuyensinh.service;

import com.tuyensinh.dao.MaXetTuyenMapDao;
import com.tuyensinh.dao.NganhDao;
import com.tuyensinh.dao.NganhToHopDao;
import com.tuyensinh.dao.PhuongThucDao;
import com.tuyensinh.entity.MaXetTuyenMap;
import com.tuyensinh.entity.Nganh;
import com.tuyensinh.entity.NganhToHop;
import com.tuyensinh.entity.PhuongThuc;

import java.util.List;
import java.util.Optional;

public class MaXetTuyenMapService {

    private final MaXetTuyenMapDao dao = new MaXetTuyenMapDao();
    private final NganhDao nganhDao = new NganhDao();
    private final PhuongThucDao phuongThucDao = new PhuongThucDao();
    private final NganhToHopDao nganhToHopDao = new NganhToHopDao();

    public List<MaXetTuyenMap> findAll() {
        return dao.findAll();
    }

    public List<MaXetTuyenMap> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return dao.findAll();
        }
        return dao.search(keyword);
    }

    public MaXetTuyenMap findById(Integer id) {
        return dao.findById(id);
    }

    public MaXetTuyenMap save(MaXetTuyenMap entity) {
        return dao.save(entity);
    }

    public void update(MaXetTuyenMap entity) {
        dao.update(entity);
    }

    public void delete(MaXetTuyenMap entity) {
        dao.delete(entity);
    }

    public void deleteAll() {
        dao.deleteAll();
    }

    public List<MaXetTuyenMap> findByMaXetTuyen(String maXetTuyen) {
        return dao.findByMaXetTuyen(maXetTuyen);
    }

    public Optional<MaXetTuyenMap> findExact(String maXetTuyen, Short phuongthucId, String maTohopNguon) {
        return dao.findExact(maXetTuyen, phuongthucId, maTohopNguon);
    }

    public List<MaXetTuyenMap> findByNganhId(Integer nganhId) {
        return dao.findByNganhId(nganhId);
    }

    public List<Nganh> findAllNganh() {
        return nganhDao.findAll();
    }

    public List<PhuongThuc> findAllPhuongThuc() {
        return phuongThucDao.findAll();
    }

    public List<NganhToHop> findNganhToHopByNganh(Integer nganhId) {
        return nganhToHopDao.findByNganhId(nganhId);
    }
}
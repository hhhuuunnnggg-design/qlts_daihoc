package com.tuyensinh.service;

import com.tuyensinh.dao.NganhToHopMonDao;
import com.tuyensinh.entity.NganhToHopMon;
import com.tuyensinh.service.interfaceService.INganhToHopMonService;

import java.util.List;
import java.util.Optional;

public class NganhToHopMonService implements INganhToHopMonService {

    private final NganhToHopMonDao dao = new NganhToHopMonDao();

    @Override
    public List<NganhToHopMon> findByNganhToHopId(Integer nganhToHopId) {
        return dao.findByNganhToHopId(nganhToHopId);
    }

    @Override
    public Optional<NganhToHopMon> findByNganhToHopAndMon(Integer nganhToHopId, Integer monId) {
        return dao.findByNganhToHopAndMon(nganhToHopId, monId);
    }

    @Override
    public NganhToHopMon save(NganhToHopMon entity) {
        return dao.save(entity);
    }

    @Override
    public void update(NganhToHopMon entity) {
        dao.update(entity);
    }

    @Override
    public void delete(NganhToHopMon entity) {
        dao.delete(entity);
    }

    @Override
    public List<NganhToHopMon> findAll() {
        return dao.findAll();
    }

    @Override
    public long countAll() {
        return dao.countAll();
    }
}

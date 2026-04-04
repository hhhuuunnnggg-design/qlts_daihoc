package com.tuyensinh.service;

import com.tuyensinh.dao.DiemCongChiTietDao;
import com.tuyensinh.entity.DiemCongChiTiet;
import com.tuyensinh.service.interfaceService.IDiemCongChiTietService;

import java.util.List;

public class DiemCongChiTietService implements IDiemCongChiTietService {

    private final DiemCongChiTietDao dao = new DiemCongChiTietDao();

    @Override
    public List<DiemCongChiTiet> findByDiemCongId(Integer diemcongId) {
        return dao.findByDiemCongId(diemcongId);
    }

    @Override
    public List<DiemCongChiTiet> findAppliedByDiemCongId(Integer diemcongId) {
        return dao.findAppliedByDiemCongId(diemcongId);
    }

    @Override
    public DiemCongChiTiet save(DiemCongChiTiet entity) {
        return dao.save(entity);
    }

    @Override
    public void update(DiemCongChiTiet entity) {
        dao.update(entity);
    }

    @Override
    public void delete(DiemCongChiTiet entity) {
        dao.delete(entity);
    }

    @Override
    public void deleteByDiemCongId(Integer diemcongId) {
        dao.deleteByDiemCongId(diemcongId);
    }
}
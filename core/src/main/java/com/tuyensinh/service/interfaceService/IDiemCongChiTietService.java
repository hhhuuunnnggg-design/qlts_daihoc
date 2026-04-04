package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.DiemCongChiTiet;

import java.util.List;

public interface IDiemCongChiTietService {

    List<DiemCongChiTiet> findByDiemCongId(Integer diemcongId);

    List<DiemCongChiTiet> findAppliedByDiemCongId(Integer diemcongId);

    DiemCongChiTiet save(DiemCongChiTiet entity);

    void update(DiemCongChiTiet entity);

    void delete(DiemCongChiTiet entity);

    void deleteByDiemCongId(Integer diemcongId);
}
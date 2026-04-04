package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.DiemCongChiTiet;

import java.util.List;

public interface IDiemCongChiTietDao extends IBaseDao<DiemCongChiTiet> {

    List<DiemCongChiTiet> findByDiemCongId(Integer diemcongId);

    List<DiemCongChiTiet> findAppliedByDiemCongId(Integer diemcongId);

    void deleteByDiemCongId(Integer diemcongId);
}
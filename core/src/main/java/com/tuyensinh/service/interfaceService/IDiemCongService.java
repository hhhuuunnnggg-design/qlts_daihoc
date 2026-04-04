package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.DiemCong;

import java.util.List;
import java.util.Optional;

public interface IDiemCongService {

    List<DiemCong> findAll();

    DiemCong findById(Integer id);

    List<DiemCong> findByThiSinhId(Integer thisinhId);

    List<DiemCong> findByPhuongThuc(Short phuongthucId);

    List<DiemCong> findByNganhToHopId(Integer nganhToHopId);

    Optional<DiemCong> findByThiSinhNganhToHopPhuongThuc(Integer thisinhId, Integer nganhToHopId, Short phuongthucId);

    DiemCong save(DiemCong entity);

    void update(DiemCong entity);

    void delete(DiemCong entity);

    void recalculateTongHop(Integer diemcongId);
}
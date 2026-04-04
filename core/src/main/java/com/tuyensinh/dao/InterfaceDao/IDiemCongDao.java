package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.DiemCong;

import java.util.List;
import java.util.Optional;

public interface IDiemCongDao extends IBaseDao<DiemCong> {

    List<DiemCong> findByThiSinhId(Integer thisinhId);

    List<DiemCong> findByPhuongThuc(Short phuongthucId);

    List<DiemCong> findByNganhToHopId(Integer nganhToHopId);

    Optional<DiemCong> findByThiSinhNganhToHopPhuongThuc(Integer thisinhId, Integer nganhToHopId, Short phuongthucId);
}
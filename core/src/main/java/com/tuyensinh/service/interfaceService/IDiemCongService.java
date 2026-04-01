package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.DiemCong;
import java.util.List;

public interface IDiemCongService {

    List<DiemCong> findAll();

    DiemCong findById(Integer id);

    List<DiemCong> findByThiSinhId(Integer thisinhId);

    DiemCong save(DiemCong entity);

    void update(DiemCong entity);

    void delete(DiemCong entity);
}

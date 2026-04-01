package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.DoiTuongUutien;
import java.util.List;

public interface IDoiTuongService {

    List<DoiTuongUutien> findAll();

    DoiTuongUutien findById(Integer id);

    DoiTuongUutien save(DoiTuongUutien d);

    void update(DoiTuongUutien d);

    void delete(DoiTuongUutien d);
}

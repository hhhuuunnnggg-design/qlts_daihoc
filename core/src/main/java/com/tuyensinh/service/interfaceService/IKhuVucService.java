package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.KhuVucUutien;
import java.util.List;

public interface IKhuVucService {

    List<KhuVucUutien> findAll();

    KhuVucUutien findById(Integer id);

    KhuVucUutien save(KhuVucUutien k);

    void update(KhuVucUutien k);

    void delete(KhuVucUutien k);
}

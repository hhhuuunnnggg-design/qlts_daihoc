package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.NguyenVong;
import java.util.List;

public interface INguyenVongService {

    List<NguyenVong> findAll();

    List<NguyenVong> findByThiSinhId(Integer thisinhId);

    NguyenVong findById(Integer id);

    NguyenVong save(NguyenVong entity);

    void update(NguyenVong entity);

    void delete(NguyenVong entity);

    List<NguyenVong> findByKetQua(String ketQua);

    int countByNganhAndPhuongThuc(Integer nganhId, Short phuongthucId, String ketQua);
}

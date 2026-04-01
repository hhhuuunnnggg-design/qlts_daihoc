package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.Mon;
import java.util.List;

public interface IMonService {

    List<Mon> findAll();

    List<Mon> findByLoaiMon(String loaiMon);

    List<Mon> findNangKhieuMon();

    List<Mon> findDanhGiaNangLuc();

    List<Mon> findMonHoc();

    Mon findById(Integer id);

    Mon save(Mon entity);

    void update(Mon entity);
}

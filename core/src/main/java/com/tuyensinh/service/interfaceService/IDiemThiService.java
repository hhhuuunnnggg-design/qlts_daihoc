package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.DiemThi;
import java.util.List;

public interface IDiemThiService {

    List<DiemThi> findAll();

    DiemThi findById(Integer id);

    List<DiemThi> findByThiSinhId(Integer thisinhId);

    List<DiemThi> findByPhuongThuc(Short phuongthucId);

    List<DiemThi> searchDiemThi(String keyword, Short phuongthucId);

    DiemThi save(DiemThi entity);

    void update(DiemThi entity);

    void delete(DiemThi entity);

    List<Object[]> thongKeDiemTheoMon(Short phuongthucId);
}

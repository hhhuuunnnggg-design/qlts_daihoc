package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.DiemThi;
import java.util.List;
import com.tuyensinh.entity.Mon;
import com.tuyensinh.entity.PhuongThuc;
import com.tuyensinh.entity.ThiSinh;

import java.math.BigDecimal;
import java.util.Map;

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
    DiemThi importOrReplaceScoreSheet(ThiSinh thiSinh,
                                      PhuongThuc phuongThuc,
                                      Short namTuyensinh,
                                      String soBaoDanh,
                                      String ghiChu,
                                      Map<Mon, BigDecimal> diemTheoMon);

    DiemThi findByIdWithDetails(Integer id);

    List<DiemThi> findByThiSinhIdWithDetails(Integer thisinhId);
}

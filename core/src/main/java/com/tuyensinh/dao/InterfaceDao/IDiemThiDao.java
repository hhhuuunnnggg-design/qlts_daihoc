package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.DiemThi;
import java.util.List;
import java.util.Optional;

public interface IDiemThiDao extends IBaseDao<DiemThi> {

    Optional<DiemThi> findByThiSinhAndPhuongThuc(Integer thisinhId, Short phuongthucId, Short namTuyensinh);

    List<DiemThi> findByThiSinhId(Integer thisinhId);

    List<DiemThi> findByPhuongThuc(Short phuongthucId);

    List<DiemThi> findByPhuongThucAndPage(Short phuongthucId, int page, int pageSize);

    List<Object[]> thongKeDiemByPhuongThucMon(Short phuongthucId, Integer monId);

    List<Object[]> thongKeDiemTheoMon(Short phuongthucId);

    List<DiemThi> searchByCccdOrSoBaoDanh(String keyword, Short phuongthucId);
}

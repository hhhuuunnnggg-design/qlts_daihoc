package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.Mon;
import java.util.List;
import java.util.Optional;

public interface IMonDao extends IBaseDao<Mon> {

    Optional<Mon> findByMa(String maMon);

    List<Mon> findByLoaiMon(String loaiMon);

    List<Mon> findNangKhieuMon();

    List<Mon> findDanhGiaNangLuc();

    List<Mon> findMonHoc();
}

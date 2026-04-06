package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.MaXetTuyenMap;

import java.util.List;
import java.util.Optional;

public interface IMaXetTuyenMapDao extends IBaseDao<MaXetTuyenMap> {

    List<MaXetTuyenMap> findByMaXetTuyen(String maXetTuyen);

    Optional<MaXetTuyenMap> findExact(String maXetTuyen, Short phuongthucId, String maTohopNguon);

    List<MaXetTuyenMap> findByNganhId(Integer nganhId);

    List<MaXetTuyenMap> search(String keyword);

    void deleteAll();
}
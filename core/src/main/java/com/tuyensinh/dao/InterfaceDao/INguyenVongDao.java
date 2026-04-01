package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.NguyenVong;
import java.util.List;

public interface INguyenVongDao extends IBaseDao<NguyenVong> {

    List<NguyenVong> findByThiSinhId(Integer thisinhId);

    List<NguyenVong> findByKetQua(String ketQua);

    int countByNganhAndPhuongThuc(Integer nganhId, Short phuongthucId, String ketQua);

    List<NguyenVong> findByNganhIdAndPhuongThuc(Integer nganhId, Short phuongthucId);
}

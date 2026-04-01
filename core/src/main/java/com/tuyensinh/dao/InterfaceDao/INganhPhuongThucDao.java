package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.NganhPhuongThuc;
import java.util.List;

public interface INganhPhuongThucDao extends IBaseDao<NganhPhuongThuc> {

    List<NganhPhuongThuc> findByNganhId(Integer nganhId);

    List<NganhPhuongThuc> findByPhuongThucId(Short phuongthucId);

    NganhPhuongThuc findByNganhAndPhuongThuc(Integer nganhId, Short phuongthucId);
}

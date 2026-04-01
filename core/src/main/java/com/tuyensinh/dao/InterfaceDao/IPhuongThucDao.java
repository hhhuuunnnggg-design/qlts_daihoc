package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.PhuongThuc;
import java.util.List;
import java.util.Optional;

public interface IPhuongThucDao extends IBaseDao<PhuongThuc> {

    Optional<PhuongThuc> findByMa(String maPhuongthuc);

    List<PhuongThuc> findActive();
}

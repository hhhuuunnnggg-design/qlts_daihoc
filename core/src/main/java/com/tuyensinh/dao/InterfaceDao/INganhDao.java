package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.Nganh;
import java.util.List;
import java.util.Optional;

public interface INganhDao extends IBaseDao<Nganh> {

    Optional<Nganh> findByMa(String maNganh);

    List<Nganh> findActive();

    List<Nganh> searchByMaOrTen(String keyword);

    List<Object[]> thongKeNganh();
}

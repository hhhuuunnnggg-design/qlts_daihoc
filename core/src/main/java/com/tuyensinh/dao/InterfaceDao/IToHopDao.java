package com.tuyensinh.dao.InterfaceDao;

import com.tuyensinh.entity.ToHop;
import com.tuyensinh.entity.ToHopMon;
import java.util.List;
import java.util.Optional;

public interface IToHopDao extends IBaseDao<ToHop> {

    Optional<ToHop> findByMa(String maTohop);

    List<ToHopMon> findMonByToHopId(Integer tohopId);

    void saveToHopMon(ToHopMon entity);

    List<ToHop> searchByMaOrTen(String keyword);

    List<ToHop> findNangKhieuToHop();
}

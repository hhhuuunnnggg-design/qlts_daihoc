package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.ToHop;
import com.tuyensinh.entity.ToHopMon;
import java.util.List;

public interface IToHopService {

    List<ToHop> findAll();

    ToHop findById(Integer id);

    ToHop save(ToHop entity);

    void update(ToHop entity);

    void delete(ToHop entity);

    List<ToHopMon> getMonByToHop(Integer tohopId);

    void saveToHopMon(ToHopMon entity);
}

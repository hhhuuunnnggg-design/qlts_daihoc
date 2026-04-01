package com.tuyensinh.service;

import com.tuyensinh.dao.ToHopDao;
import com.tuyensinh.dao.MonDao;
import com.tuyensinh.entity.ToHop;
import com.tuyensinh.entity.ToHopMon;
import com.tuyensinh.entity.Mon;
import java.util.List;

public class ToHopService {

    private final ToHopDao dao = new ToHopDao();
    private final MonDao monDao = new MonDao();

    public List<ToHop> findAll() {
        return dao.findAll();
    }

    public ToHop save(ToHop entity) {
        return dao.save(entity);
    }

    public void update(ToHop entity) {
        dao.update(entity);
    }

    public void delete(ToHop entity) {
        dao.delete(entity);
    }

    public List<ToHopMon> getMonByToHop(Integer tohopId) {
        return dao.findMonByToHopId(tohopId);
    }

    public void saveToHopMon(ToHopMon entity) {
        dao.saveToHopMon(entity);
    }
}

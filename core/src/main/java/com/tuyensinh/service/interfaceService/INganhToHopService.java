package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.NganhToHop;
import java.util.List;

public interface INganhToHopService {

    List<NganhToHop> findAll();

    List<NganhToHop> findByNganhId(Integer nganhId);

    NganhToHop findById(Integer id);

    NganhToHop save(NganhToHop entity);

    void delete(NganhToHop entity);
}

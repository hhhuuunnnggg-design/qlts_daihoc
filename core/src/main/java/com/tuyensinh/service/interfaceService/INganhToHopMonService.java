package com.tuyensinh.service.interfaceService;

import com.tuyensinh.entity.NganhToHopMon;
import java.util.List;
import java.util.Optional;

public interface INganhToHopMonService {
    List<NganhToHopMon> findByNganhToHopId(Integer nganhToHopId);
    Optional<NganhToHopMon> findByNganhToHopAndMon(Integer nganhToHopId, Integer monId);
    NganhToHopMon save(NganhToHopMon entity);
    void update(NganhToHopMon entity);
    void delete(NganhToHopMon entity);
    List<NganhToHopMon> findAll();
    long countAll();
}
